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
package main.java.miro.validator.fetcher;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.io.Files;

import main.java.miro.validator.types.RepositoryObject;
import net.ripe.rpki.commons.crypto.CertificateRepositoryObject;
import net.ripe.rpki.commons.crypto.util.CertificateRepositoryObjectFactory;
import net.ripe.rpki.commons.crypto.x509cert.X509ResourceCertificate;
import net.ripe.rpki.commons.validation.ValidationLocation;
import net.ripe.rpki.commons.validation.ValidationResult;

public class RepositoryObjectFetcher {
	
	public static final Logger log = Logger.getGlobal();
	
	private RsyncDownloader downloader;
	
	private ValidationResult result;
	
	public RepositoryObjectFetcher(RsyncDownloader loader, ValidationResult res) {
		downloader = loader;
		
		result = res;
	}
	
	
	/* Downloads file at objUri and returns it as a RepositoryObject. Returns null if download fails or URI does not 
	 * point at a RPKI File (.mft, .cer, .crl, .roa)
	 */
	public RepositoryObject fetchAndRead(URI objUri, String downloadDestination) {
		
		if( !(0 == downloader.downloadData(objUri.toString(), downloadDestination))){
			return null;
		}
		
		File objFile = new File(downloadDestination);
		
		if(!objFile.exists()){
			log.log(Level.SEVERE, "Downloaded {0}, but it doesn't exist in local filesystem.",objUri.toString());
			return null;
		}
		
		if(!objFile.isFile()){
			return null;
		}
		
		try {
			CertificateRepositoryObject rawRepoObject = readCertificateRepositoryObject(objFile, result);
			
			
		} catch (Exception e) {
			log.log(Level.SEVERE, "Could not read {0}", objFile.toString());
			log.log(Level.SEVERE, e.toString(), e);
		}
		
		
		
		
		
		return null;
	}
	
	
	public RepositoryObject createRepositoryObject(File file, CertificateRepositoryObject rawObject) {
		RepositoryObject result;
		if(rawObject instanceof X509ResourceCertificate){
		}
		
		
		return null;
	}
	
	
	
	
	
	public CertificateRepositoryObject readCertificateRepositoryObject(File file, ValidationResult validationResult) throws Exception{
		byte[] contents;
		CertificateRepositoryObject obj = null;
		contents = Files.toByteArray(file);
		validationResult.setLocation(new ValidationLocation(file.getName()));
		obj = CertificateRepositoryObjectFactory.createCertificateRepositoryObject(contents, validationResult);
		return obj;
	}
	
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	
	

}
