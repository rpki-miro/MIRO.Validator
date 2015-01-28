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
import java.util.logging.Level;
import java.util.logging.Logger;

import miro.validator.ResourceCertificateTreeValidator;
import miro.validator.fetcher.RsyncDownloader;
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
		ArrayList<CertificateObject> currentLevel = new ArrayList<CertificateObject>();
		ArrayList<CertificateObject> nextLevel = new ArrayList<CertificateObject>();
		ArrayList<CertificateObject> buf = null;
		currentLevel.add(trustAnchor);
		
		//Read in hierarchy breadth first, top to bottom
		do{
			for(ResourceHoldingObject cw : currentLevel){
				
				//Read in all certificates issued by cw
				if(cw instanceof CertificateObject){
					readChildren((CertificateObject) cw);
					buf = getCertWrapperChildren((CertificateObject) cw);
					nextLevel.addAll(buf);
				}
				
			}
			
			//Swap out the lists
			currentLevel = nextLevel;
			nextLevel = new ArrayList<CertificateObject>();
			
		} while(!currentLevel.isEmpty());
		log.log(Level.INFO,"Reading done");
		
	}
	
	
	private ArrayList<CertificateObject> getCertWrapperChildren(
			CertificateObject cw) {
		ArrayList<CertificateObject> certKids = new ArrayList<CertificateObject>();
		for(ResourceHoldingObject child : cw.getChildren()){
			if(child instanceof CertificateObject){
				certKids.add((CertificateObject) child);
			}
		}
		return certKids;
	}


	public void readChildren(CertificateObject cw) {
		ArrayList<String> pubPoints = getIssued(cw.getCertificate().getSubjectInformationAccess());
		cw.setChildren(new ArrayList<ResourceHoldingObject>());
		
		cw.findManifest(result);
		
		File pubDir;
		String ppPath;
		String[] filelist;
		String[] crlList;
		String filepath;
		for(X509CertificateInformationAccessDescriptor accessDescriptor : cw.getCertificate().getSubjectInformationAccess()){
			
			
			ppPath = RsyncDownloader.getRelativePath(accessDescriptor.getLocation(), BASE_DIR);
			validator.fetchIssued(accessDescriptor.getLocation(), ppPath);

			pubDir = new File(ppPath);
			
			if(!pubDir.isDirectory()){
				continue;
			}
			
			crlList = getCrlFiles(pubDir);
			CRLObject crlWrap = null;
			boolean foundCrl = false;
			for(String filename : crlList){
				filepath  = ppPath + "/" + filename;
				try{
					crlWrap = RepositoryObjectFactory.createCrlWrapper(filepath, result);
					crlWrap.setRemoteLocation(accessDescriptor.getLocation());
				} catch (Exception e){
					log.log(Level.SEVERE,e.toString(),e);
					log.log(Level.WARNING,"Could not read "+ filepath);
				}
				
				if(cw.getSubject().equals(crlWrap.getCrl().getIssuer())){
					cw.setCrl(crlWrap);
					foundCrl = true;
					break;
				}
			}
			
			if(!foundCrl){
				log.log(Level.WARNING,"Could not find CRL for "+ cw.getFilename());
			}
			
		
			
			filelist = getResourceHoldingFiles(pubDir);
			for(String filename : filelist){
				filepath = ppPath + "/" + filename;

				//Don't add ourselves to our children, or else we get a loop
				if(cw.getFilename().equals(filename)){
					continue;
				}
				
				ResourceHoldingObject obj  = null;
				try{
					obj = RepositoryObjectFactory.createResourceHoldingObject(filepath, result,cw);
					obj.setRemoteLocation(accessDescriptor.getLocation());
					
				} catch (Exception e){
					log.log(Level.SEVERE,e.toString(),e);
					log.log(Level.WARNING,"Could not read "+ filepath);
				}
				
				//Make sure its our kids before adding
				if(cw.getSubject().equals(obj.getIssuer())){
					log.log(Level.FINER, "Read in certificate "+ filepath);
					cw.getChildren().add(obj);
				}
				
			}
		}
	}

	private String[] getCrlFiles(File pubDir) {
		
		final String suffix = ".crl";
		return pubDir.list(new FilenameFilter(){
			
			@Override
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
			
			@Override
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
	
	
	
	private ArrayList<String> getIssued(X509CertificateInformationAccessDescriptor[] sia){
		ArrayList<String> pubDirs = new ArrayList<String>();
		
		if(sia == null){
			return pubDirs;
		}
		
		String result;
		for(X509CertificateInformationAccessDescriptor desc : sia){
			
			result = RsyncDownloader.getRelativePath(desc.getLocation(), BASE_DIR);
			File resultFile = new File(result);
			
			validator.fetchIssued(desc.getLocation(), result);
			
			if(resultFile.isDirectory()){
				pubDirs.add(result);
			}
			
			
			
		}
		return pubDirs;
	}



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
}
