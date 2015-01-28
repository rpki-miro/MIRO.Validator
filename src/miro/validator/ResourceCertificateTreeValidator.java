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
package miro.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import miro.validator.export.ExportType;
import miro.validator.export.IRepositoryExporter;
import miro.validator.export.json.JsonExporter;
import miro.validator.fetcher.RsyncDownloader;
import miro.validator.fetcher.TrustAnchorFetcher;
import miro.validator.logging.RepositoryLogging;
import miro.validator.stats.ResultExtractor;
import miro.validator.stats.types.RPKIRepositoryStats;
import miro.validator.types.CertificateObject;
import miro.validator.types.RepositoryObject;
import miro.validator.types.RepositoryObjectFactory;
import miro.validator.types.ResourceCertificateTree;
import miro.validator.validation.ResourceCertificateLocatorImpl;
import miro.validator.validation.TopDownValidator;
import net.ripe.rpki.commons.crypto.x509cert.X509CertificateInformationAccessDescriptor;
import net.ripe.rpki.commons.validation.ValidationResult;


public class ResourceCertificateTreeValidator {
	
	//To pretty print byte[]
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static final Logger log = Logger.getGlobal();
	
	public static String BASE_DIR;
	
	private ResourceCertificateTree certTree;
	
	private List<String> prefetched;
	
	private RsyncDownloader downloader;

	public ResourceCertificateTreeValidator(String baseDir) {
		BASE_DIR = baseDir;
		prefetched = new ArrayList<String>();
		downloader = new RsyncDownloader();
	}
	
	public ResourceCertificateTree getModelByTAL(String talLocation, String name) {
		URI taLocation = getTrustAnchorURI(talLocation);
		return createResourceCertificateTree(taLocation, name, getTimestamp());
	}

	/* export path is temp until data model has been unified */
	public ResourceCertificateTree getModelByTA(String taLocation, String name) {
			/* Get name and time for the certificate tree */
			name = name == null ? new File(taLocation).getName() : name;
			String timestamp = getTimestamp();
			
			ResourceCertificateTree certTree = readAndValidate(taLocation, name, timestamp);
			return certTree;
	}
	
	public URI getTrustAnchorURI(String TALpath) {
		File TALFile = new File(TALpath);
		try {
			BufferedReader br  = new BufferedReader(new FileReader(TALFile));
			String line = br.readLine();
			br.close();
			return URI.create(line);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Could not read TAL at {0}", TALpath);
			log.log(Level.SEVERE, e.toString(), e);
			return null;
		}
	}
	

	public CertificateObject createTrustAnchor(String taLocation, ValidationResult result) {
		
		CertificateObject trustAnchor = null; 
		try {
			trustAnchor = (CertificateObject) RepositoryObjectFactory.createResourceHoldingObject(taLocation, result);
//			trustAnchor.findManifest(result);
		} catch (Exception e) {
			log.log(Level.SEVERE,e.toString(),e);
			log.log(Level.SEVERE,"Could not read trust anchor {0}",taLocation);
		}
		return trustAnchor;
	}
	
	
	public ResourceCertificateTree createResourceCertificateTree(URI taLocation, String name, String ts) {
		RepositoryObjectFactory.clearResourceObjectsMap();
		
		/* Get the trust anchor first*/
		String taPath = downloader.fetchObject(taLocation, BASE_DIR);
		
		
		ValidationResult result = ValidationResult.withLocation(taPath);
		CertificateObject trustAnchor = createTrustAnchor(taPath, result);
		trustAnchor.setRemoteLocation(taLocation); 
		
		
		ResourceCertificateTree tree = new ResourceCertificateTree(this, name, trustAnchor, result, ts, BASE_DIR);
		tree.populate();
		validate(result,trustAnchor);
		tree.extractValidationResults();
		certTree = tree;
		return certTree;
	}
	
	public ResourceCertificateTree readAndValidate(String taLocation, String name, String ts) {
		RepositoryObjectFactory.clearResourceObjectsMap();
		
		/* Get the trust anchor first*/
		ValidationResult result = ValidationResult.withLocation(taLocation);
		
		
		CertificateObject ta = createTrustAnchor(taLocation, result);
		
		ResourceCertificateTree tree = new ResourceCertificateTree(this, name, ta, result, ts, BASE_DIR);
		tree.populate();
		validate(result,ta);
		tree.extractValidationResults();
		certTree = tree;
		return certTree;
	}

	public void preFetch(URI[] uris) {
		log.log(Level.INFO, "Prefetching...");
		String destination;
		for(URI uri : uris){
			destination = RsyncDownloader.getRelativePath(uri, BASE_DIR);
			if(downloader.downloadData(uri.toString(), destination) == 0 ){
				prefetched.add(uri.toString());
			}
			
		}
		log.log(Level.INFO, "Done prefetching..");
	}

	public static void validate(ValidationResult result, CertificateObject trustAnchor) {
		TopDownValidator validator = new TopDownValidator(result, new ResourceCertificateLocatorImpl(), trustAnchor);
		validator.validate();
		
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

	public static byte[] getHash(String path) throws IOException{
		byte[] result_bytes = null;
		byte[] file_bytes;
		try {
			file_bytes = java.nio.file.Files.readAllBytes(Paths.get(path));
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-256");
			md.update(file_bytes);
			result_bytes = md.digest();
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.SEVERE,"Error: Could not find SHA-256 algorithm");
		}
		return result_bytes;
	}
	
	public static String toPath(URI uri){
		return BASE_DIR + uri.getHost()+uri.getPath();
	}
	
	public static String getBaseDir(){
		return BASE_DIR;
	}

	public ResourceCertificateTree getTree() {
		return certTree;
	}
	
	public String updateTA(String TALpath) {
		TrustAnchorFetcher TAfetcher = new TrustAnchorFetcher(null);
		String taPath = TAfetcher.fetchTA(TALpath,BASE_DIR);
		return taPath;
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

	private String getTimestamp() {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS zzz");
		f.setTimeZone(TimeZone.getTimeZone("UTC"));
		return f.format(new Date());
	}

	public void fetchIssued(URI desc,
			String result) {
		
		String descStr = desc.toString();
		for(String prefetchedLocation : prefetched) {
			if(descStr.startsWith(prefetchedLocation)){
				return;
			}
		}
		
		downloader.downloadData(descStr,result);
		
	}
}
