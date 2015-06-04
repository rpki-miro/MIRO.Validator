/*
 * Copyright (c) 2015, Andreas Reuter, Freie Universität Berlin 

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 * 
 * */
package miro.validator.types;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import miro.validator.ResourceCertificateTreeValidator;
import miro.validator.fetcher.RsyncDownloader;
import miro.validator.validation.ResourceCertificateLocatorImpl;
import miro.validator.validation.TopDownValidator;
import net.ripe.rpki.commons.crypto.CertificateRepositoryObject;
import net.ripe.rpki.commons.crypto.cms.roa.RoaCms;
import net.ripe.rpki.commons.crypto.crl.X509Crl;
import net.ripe.rpki.commons.crypto.x509cert.X509CertificateInformationAccessDescriptor;
import net.ripe.rpki.commons.crypto.x509cert.X509ResourceCertificate;
import net.ripe.rpki.commons.validation.ValidationResult;

public class ResourceCertificateTree {
	
	public static final Logger log = Logger.getGlobal();
	
	private String repositoryName;
	
	private ValidationResult result;
	
	private CertificateObject trustAnchor;
	
	private String timestamp;
	
	private String BASE_DIR;
	
	private ResourceCertificateTreeValidator validator;
	
	public ResourceCertificateTree(ResourceCertificateTreeValidator validator, String name, CertificateObject ta, ValidationResult r, String d, String baseDir){
		repositoryName = name;
		result = r;
		trustAnchor = ta;
		timestamp = d;
		BASE_DIR = baseDir;
		this.validator = validator;
		
	}
	
	public String getTimeStamp(){
		return timestamp;
	}
	
	public String getName(){
		return repositoryName;
	}
	
	public CertificateObject getTrustAnchor(){
		return trustAnchor;
	}

	public void populate() {
		Queue<CertificateObject> workingQueue = new LinkedList<CertificateObject>();
		workingQueue.add(trustAnchor);
		CertificateObject cert;
		while(!workingQueue.isEmpty()){
			cert = workingQueue.poll();
			getChildren(cert);
			workingQueue.addAll(getCertificateObjectChildren(cert.getChildren()));
		}
		log.log(Level.INFO,"Reading done");
	}
	
	
	private ArrayList<CertificateObject> getCertificateObjectChildren(List<ResourceHoldingObject> children) {
		ArrayList<CertificateObject> certKids = new ArrayList<CertificateObject>();
		for(ResourceHoldingObject child : children){
			if(child instanceof CertificateObject){
				certKids.add((CertificateObject) child);
			}
		}
		return certKids;
	}
	
	public void findManifest(CertificateObject cw) {
		String path = ResourceCertificateTreeValidator.toPath(cw.getCertificate().getManifestUri());
		ManifestObject manifest = null;
		try {
			manifest = RepositoryObjectFactory.createManifestObject(path, result);
		} catch (Exception e) {
			log.log(Level.WARNING, "Could not read manifest " + path);
		}
		manifest.setRemoteLocation(cw.getCertificate().getManifestUri());
		cw.setManifest(manifest);
	}
	
	public void findCRL(URI location, CertificateObject cw){
		String path = ResourceCertificateTreeValidator.toPath(location);
		String[] crlList = getCrlFiles(new File(path));
		CRLObject crlWrap = null;
		boolean foundCrl = false;
		String filepath;
		for (String filename : crlList) {
			filepath = path + "/" + filename;
			try {
				crlWrap = RepositoryObjectFactory.createCRLObject(filepath,
						result);
			} catch (Exception e) {
				log.log(Level.SEVERE, e.toString(), e);
				log.log(Level.WARNING, "Could not read " + filepath);
			}

			if (cw.getSubject().equals(crlWrap.getCrl().getIssuer())) {
				cw.setCrl(crlWrap);
				crlWrap.setRemoteLocation(location);
				foundCrl = true;
				break;
			}
		}

		if (!foundCrl) {
			log.log(Level.WARNING, "Could not find CRL for " + cw.getFilename());
		}
	}
	
	public boolean isPublishingPoint(X509CertificateInformationAccessDescriptor accessDescriptor) {
		return !accessDescriptor.getLocation().toString().endsWith(".mft");
	}
	public void getChildren(CertificateObject cw) {
		cw.setChildren(new ArrayList<ResourceHoldingObject>());
		for(X509CertificateInformationAccessDescriptor accessDescriptor : cw.getCertificate().getSubjectInformationAccess()){
			
			if(!isPublishingPoint(accessDescriptor))
				continue;
			
			int rtval = validator.fetchURI(accessDescriptor.getLocation());
			if(rtval != 0){
				log.log(Level.WARNING, "Could not download publishing point: " + accessDescriptor.getLocation());
				continue;
			}
			findManifest(cw);
			findCRL(accessDescriptor.getLocation(),cw);
			findChildren(accessDescriptor.getLocation(),cw);
		}
	}

	private void findChildren(URI location, CertificateObject cw) {
		cw.setChildren(new ArrayList<ResourceHoldingObject>());
		String ppPath = ResourceCertificateTreeValidator.toPath(location);
		String[] filelist = getResourceHoldingFiles(new File(ppPath));
		for (String filename : filelist) {
			String filepath = ppPath + "/" + filename;

			// Don't add ourselves to our children, or else we get a loop
			if (cw.getFilename().equals(filename)) {
				continue;
			}

			ResourceHoldingObject obj = null;
			try {
				obj = RepositoryObjectFactory.createResourceHoldingObject(
						filepath, result, cw);
				obj.setRemoteLocation(location);

			} catch (Exception e) {
				log.log(Level.SEVERE, e.toString(), e);
				log.log(Level.WARNING, "Could not read " + filepath);
			}

			// Make sure its our kids before adding
			if (cw.getSubject().equals(obj.getIssuer())) {
				log.log(Level.FINER, "Read in certificate " + filepath);
				cw.getChildren().add(obj);
			}

		}
	}

	private String[] getCrlFiles(File pubDir) {
		
		final String suffix = ".crl";
		return pubDir.list(new FilenameFilter(){
			
			public boolean accept(File arg0, String arg1) {
				
				if(arg1.endsWith(suffix)){
					return true;
				}
				return false;
			}
			
		});
	}

	private String[] getResourceHoldingFiles(File pubDir) {
		final String[] suffixes = new String[]{".cer",".roa"};
		return pubDir.list(new FilenameFilter(){
			
			public boolean accept(File arg0, String name) {
				
				for(String suffix : suffixes){
					if(name.endsWith(suffix)){
						return true;
					}
				}
				return false;
			}
			
			
		});
	}
	
	
	
//	private ArrayList<String> getIssued(X509CertificateInformationAccessDescriptor[] sia){
//		ArrayList<String> pubDirs = new ArrayList<String>();
//		
//		if(sia == null){
//			return pubDirs;
//		}
//		
//		String result;
//		for(X509CertificateInformationAccessDescriptor desc : sia){
//			
//			result = RsyncDownloader.getRelativePath(desc.getLocation(), BASE_DIR);
//			File resultFile = new File(result);
//			
//			validator.fetchIssued(desc.getLocation(), result);
//			
//			if(resultFile.isDirectory()){
//				pubDirs.add(result);
//			}
//			
//			
//			
//		}
//		return pubDirs;
//	}



	public void extractValidationResults() {
		
		ArrayList<ResourceHoldingObject> currentLevel = new ArrayList<ResourceHoldingObject>();
		ArrayList<ResourceHoldingObject> nextLevel = new ArrayList<ResourceHoldingObject>();
		ArrayList<ResourceHoldingObject> buf = null;
		currentLevel.add(trustAnchor);
		
		do{
			for(ResourceHoldingObject cw : currentLevel){
				
				cw.extractOwnResults(result);
				
				
				//Read in all certificates issued by cw
				if(cw instanceof CertificateObject){
					CRLObject crlWrap = ((CertificateObject) cw).getCrl();
					if(crlWrap != null){
						crlWrap.extractOwnResults(result);
					}
					
					ManifestObject mftWrap = ((CertificateObject) cw).getManifest();
					if(mftWrap != null){
						mftWrap.extractOwnResults(result);
					}
					
					buf = ((CertificateObject) cw).getChildren();
					nextLevel.addAll(buf);
				}
				
			}
			
			//Swap out the lists
			currentLevel = nextLevel;
			nextLevel = new ArrayList<ResourceHoldingObject>();
			
		} while(!currentLevel.isEmpty());
	}

	public void validate() {
		TopDownValidator validator = new TopDownValidator(result, new ResourceCertificateLocatorImpl(), trustAnchor);
		validator.validate();
	}
}
