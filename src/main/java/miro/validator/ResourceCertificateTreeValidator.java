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
package main.java.miro.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import main.java.miro.validator.export.ExportType;
import main.java.miro.validator.export.IRepositoryExporter;
import main.java.miro.validator.export.json.JsonExporter;
import main.java.miro.validator.fetcher.DownloadResult;
import main.java.miro.validator.fetcher.ObjectFetcher;
import main.java.miro.validator.fetcher.RsyncFetcher;
import main.java.miro.validator.logging.RepositoryLogging;
import main.java.miro.validator.stats.ResultExtractor;
import main.java.miro.validator.stats.types.RPKIRepositoryStats;
import main.java.miro.validator.types.CRLObject;
import main.java.miro.validator.types.CertificateObject;
import main.java.miro.validator.types.ManifestObject;
import main.java.miro.validator.types.RepositoryObject;
import main.java.miro.validator.types.RepositoryObjectFactory;
import main.java.miro.validator.types.ResourceCertificateTree;
import main.java.miro.validator.types.ResourceHoldingObject;
import main.java.miro.validator.types.RoaObject;
import main.java.miro.validator.validation.ResourceCertificateLocatorImpl;
import main.java.miro.validator.validation.TopDownValidator;
import net.ripe.rpki.commons.crypto.x509cert.X509CertificateInformationAccessDescriptor;
import net.ripe.rpki.commons.validation.ValidationResult;


public class ResourceCertificateTreeValidator {
	
	//To pretty print byte[]
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static final Logger log = Logger.getLogger(ResourceCertificateTreeValidator.class.getName());
	
	private ObjectFetcher fetcher;

	public ResourceCertificateTreeValidator(ObjectFetcher fetc) {
		fetcher = fetc;
	}
	
	public ResourceCertificateTree withTAL(TrustAnchorLocator tal){
		CertificateObject trustAnchor = obtainTrustAnchor(tal.getTrustAnchorLocation());
		if(trustAnchor == null){
			log.log(Level.SEVERE, "Could not obtain trust anchor at " + tal.getTrustAnchorLocation());
			//TODO handle null object return, same prob as in factory
			return null;
		}
		log.log(Level.INFO, "Creating ResourceCertificateTree with "+ tal.getTrustAnchorLocation());
		DateTime timestamp = new DateTime();
		fetcher.prePopulate();
		trustAnchor = populate(trustAnchor);
		fetcher.postPopulate();
		TopDownValidator validator = new TopDownValidator(ValidationResult.withLocation(trustAnchor.getFilename()), 
				new ResourceCertificateLocatorImpl(), trustAnchor);
		validator.validate();
		ResourceCertificateTree tree = new ResourceCertificateTree(tal.getName(), trustAnchor, timestamp);
		return tree;
	}

	public CertificateObject obtainTrustAnchor(URI trustAnchorLocation) {
		DownloadResult dlResult = fetcher.fetchObject(trustAnchorLocation);
		CertificateObject trustAnchor = null;
		if(dlResult.wasSuccessful()) {
			trustAnchor = RepositoryObjectFactory.createCertificateObject(dlResult.getDestination());
			trustAnchor.setRemoteLocation(trustAnchorLocation);
			//TODO log here if trust anchor is null, for better debugging
		} 
		return trustAnchor;
	}
	
	/**
	 * Populates the complete logical repository starting at the trust anchor. This means that the trust anchor's
	 * "children", "mft", and "crl" fields are set. Every CertificateObject in the trust anchor's "children" list
	 * is also populated in the same way.
	 * @param trustAnchor
	 * @param preFetcher
	 * @return
	 */
	public CertificateObject populate(CertificateObject trustAnchor) {

		Queue<CertificateObject> workingQueue = new LinkedList<CertificateObject>();
		workingQueue.add(trustAnchor);
		
		CertificateObject certificate;
		while(!workingQueue.isEmpty()){
			certificate = workingQueue.poll();
			workingQueue.addAll(populateCertificateObject(certificate));
		}
		return trustAnchor;
	}
	
	//TODO this method isn't good. whole populate process needs to be refactored at some point to be more clear
	public List<CertificateObject> populateCertificateObject(CertificateObject certificate) {
		List<RepositoryObject> issuedObjects;
		List<CertificateObject> issuedCertificates = new ArrayList<CertificateObject>();
		ArrayList<ResourceHoldingObject> issuedChildren = new ArrayList<ResourceHoldingObject>();
		
		ManifestObject manifest = obtainManifestObject(certificate.getCertificate().getManifestUri());
		certificate.setManifest(manifest);
		
		issuedObjects = getIssuedObjects(certificate);
		for(RepositoryObject obj : issuedObjects) {
			
			//TODO check staleness in case of multiple crls
			if(obj instanceof CRLObject)
				certificate.setCrl((CRLObject) obj);
			
			if(obj instanceof RoaObject)
				issuedChildren.add((ResourceHoldingObject) obj);
			
			if(obj instanceof CertificateObject) {
				issuedChildren.add((ResourceHoldingObject) obj);
				issuedCertificates.add((CertificateObject) obj);
			}
		}
		certificate.setChildren(issuedChildren);
		return issuedCertificates;
	}
	
	public ManifestObject obtainManifestObject(URI mftLocation) {
		try {
			DownloadResult result = fetcher.fetchObject(mftLocation);
			if(result.wasSuccessful()){
				ManifestObject manifest = RepositoryObjectFactory
						.createManifestObject(result.getDestination());
				manifest.setRemoteLocation(mftLocation);
				return manifest;
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Could not read manifest " + mftLocation.getPath());
		}
		//TODO again, same problem returning a null object
		return null;
	}

	public List<RepositoryObject> getIssuedObjects(CertificateObject certificate) {

		DownloadResult dlResult;
		List<RepositoryObject> objects = new ArrayList<RepositoryObject>();
		for(X509CertificateInformationAccessDescriptor accessDescriptor : certificate.getCertificate().getSubjectInformationAccess()) {
			if(!isPublishingPoint(accessDescriptor))
				continue;
			
			dlResult = fetcher.fetchObject(accessDescriptor.getLocation());
			if(!dlResult.wasSuccessful())
				continue;
			objects.addAll(readIssuedObjects(certificate,dlResult.getDestination(),accessDescriptor.getLocation()));
		}
		return objects;
	}
	
	public List<RepositoryObject> readIssuedObjects(CertificateObject certificate, String location, URI remoteLocation) {
		File localPubPoint;
		RepositoryObject object;
		String[] suffixes = new String[] { ".cer", ".crl", ".roa" };
		localPubPoint = new File(location);
		String filepath;
		List<RepositoryObject> objects = new ArrayList<RepositoryObject>();
		for (String filename : localPubPoint.list(new RepositoryObjectFilenameFilter(suffixes))) {
			filepath = localPubPoint.getPath() + "/" + filename;
			
			if(filename.endsWith(".crl")){
				object = RepositoryObjectFactory.createCRLObject(filepath);
			} else 
			if(filename.endsWith(".roa")){
				object = RepositoryObjectFactory.createRoaObjectWithParent(filepath, certificate);
			} else 
			if(filename.endsWith(".cer")){
				object = RepositoryObjectFactory.createCertificateObjectWithParent(filepath, certificate);
			} else {
				continue;
			}
			
			if(object == null)
				continue;
			
			object.setRemoteLocation(remoteLocation);
			if (isLegitimateIssuedObject(certificate, object))
				objects.add(object);
		}
		return objects;
	}

	public boolean isPublishingPoint(X509CertificateInformationAccessDescriptor accessDescriptor) {
		return !accessDescriptor.getLocation().toString().endsWith(".mft");
	}
	
	public boolean isLegitimateIssuedObject(CertificateObject parent, RepositoryObject child) {
		boolean wasIssuedBy = false;
		boolean isSelfSigned = false;
		if(parent.getSubject().equals(child.getIssuer())){
			wasIssuedBy = true;
		}
		if(child instanceof CertificateObject && ((CertificateObject)child).getSubject().equals(parent.getSubject())){
			isSelfSigned = true;
		}
		return wasIssuedBy && !isSelfSigned;
	}

	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
