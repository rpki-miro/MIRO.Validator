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
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.miro.validator.export.ExportType;
import main.java.miro.validator.export.IRepositoryExporter;
import main.java.miro.validator.export.json.JsonExporter;
import main.java.miro.validator.fetcher.ObjectFetcher;
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
import main.java.miro.validator.validation.ResourceCertificateLocatorImpl;
import main.java.miro.validator.validation.TopDownValidator;
import net.ripe.rpki.commons.crypto.x509cert.X509CertificateInformationAccessDescriptor;
import net.ripe.rpki.commons.validation.ValidationResult;


public class ResourceCertificateTreeValidator {
	
	//To pretty print byte[]
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static final Logger log = Logger.getGlobal();
	
	public static String BASE_DIR;
	
	private ResourceCertificateTree certTree;
	
	private List<String> prefetched;
	
	private ObjectFetcher downloader;

	public ResourceCertificateTreeValidator(String baseDir) {
		BASE_DIR = baseDir;
		prefetched = new ArrayList<String>();
		downloader = new ObjectFetcher();
	}
	
	/**
	 * Creates a ResourceCertificateTree with the path to a trust anchor locator file
	 * @param talFilepath Path of a trust anchor locator file
	 * @return ResourceCertificateTree of the trust anchor located by the tal
	 */
	public static ResourceCertificateTree withTALfilepath(String talFilepath, PreFetcher preFetcher){
		TrustAnchorLocator tal = new TrustAnchorLocator(talFilepath);
		return withTAL(tal, preFetcher);
	}
	
	public static ResourceCertificateTree withTAL(TrustAnchorLocator tal, PreFetcher preFetcher){
		CertificateObject trustAnchor = tal.getTrustAnchor();
		preFetcher.preFetch();
		trustAnchor = populate(trustAnchor,preFetcher);
		//validate
		ResourceCertificateTree tree = ResourceCertificateTree.withTrustAnchor(trustAnchor);
		return tree;
	}
	
	/**
	 * Populates the complete logical repository starting at the trust anchor. This means that the trust anchor's
	 * "children", "mft", and "crl" fields are set. Every CertificateObject in the trust anchor's "children" list
	 * is also populated in the same way.
	 * @param trustAnchor
	 * @param preFetcher
	 * @return
	 */
	public static CertificateObject populate(CertificateObject trustAnchor,
			PreFetcher preFetcher) {

		Queue<CertificateObject> workingQueue = new LinkedList<CertificateObject>();
		workingQueue.add(trustAnchor);

		List<CertificateObject> certificateChildren;
		CertificateObject certificate;
		ManifestObject manifest;
		CRLObject crl;
		while(!workingQueue.isEmpty()){
			certificate = workingQueue.poll();
			//Get Manifest
			certificate.getCertificate().getManifestUri();
			//Get CRL
			//Get ResourceHoldingObjects(certs/roas)
			certificate = getChildren(certificate,preFetcher);
			certificateChildren = getCertificateObjects(certificate.getChildren());
			workingQueue.addAll(certificateChildren);
		}
		return trustAnchor;
	}

	public static CertificateObject getChildren(
			CertificateObject certificate, PreFetcher preFetcher) {
		// TODO Auto-generated method stub
		// For all publishing points:
		// 1. Download pp (check with prefetcher)
		// 2. Find manifest and set it
		// 3. Find all files in manifest and set them (crl, children).
		// 
		// Once this is done with all publishing points, return the certificate
		return null;
	}

	public static List<CertificateObject> getCertificateObjects(
			List<ResourceHoldingObject> children) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static ManifestObject getManifestObject(URI mftUri) {
		//Fetch it (Fetcher Object)
		//Read it (Static factory) handles null case
		//return
		return null;
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

	
	public static String toPath(URI uri){
		return BASE_DIR + uri.getHost()+uri.getPath();
	}
	

	public ResourceCertificateTree getTree() {
		return certTree;
	}
	
	public void exportResourceCertificateTree(ExportType type, String filename){
		IRepositoryExporter exporter;
		
		long exportStart = System.nanoTime();
		switch(type) {
		
		case JSON:
			exporter = new JsonExporter(filename);
			exporter.export(certTree);
			break;
			
		case CVS:
			break;
			
		case DB:
			break;
			
		default:
			break;
		}
		
		long exportEnd = System.nanoTime();
		RepositoryLogging.logTime(exportStart, exportEnd, "Exporting ");
	}

	public boolean wasPrefetched(URI uri){
		String descStr = uri.toString();
		for(String prefetchedLocation : prefetched) {
			if(descStr.startsWith(prefetchedLocation)){
				return true;
			}
		}
		return false;
	}

	public int fetchURI(URI desc) {
		String result = toPath(desc);
		if(wasPrefetched(desc))
			return 0;
		downloader.downloadData(desc);
		return 0;
	}
}
