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
package main.java.miro.validator.types;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ripe.rpki.commons.crypto.CertificateRepositoryObject;
import net.ripe.rpki.commons.crypto.UnknownCertificateRepositoryObject;
import net.ripe.rpki.commons.crypto.cms.manifest.ManifestCms;
import net.ripe.rpki.commons.crypto.cms.roa.RoaCms;
import net.ripe.rpki.commons.crypto.crl.X509Crl;
import net.ripe.rpki.commons.crypto.util.CertificateRepositoryObjectFactory;
import net.ripe.rpki.commons.crypto.x509cert.X509ResourceCertificate;
import net.ripe.rpki.commons.validation.ValidationLocation;
import net.ripe.rpki.commons.validation.ValidationResult;

import com.google.common.io.Files;

public class RepositoryObjectFactory {
	
	public static final Logger log = Logger.getGlobal();
	
	public static HashMap<X509ResourceCertificate, ResourceHoldingObject> resourceObjects = new HashMap<X509ResourceCertificate, ResourceHoldingObject>();

	
	public static void clearResourceObjectsMap() {
		resourceObjects = new HashMap<X509ResourceCertificate, ResourceHoldingObject>();
	}
	
	private static CertificateRepositoryObject readCertificateRepositoryObjectFile(File file, ValidationResult validationResult) throws Exception {
		
		byte[] contents;
		CertificateRepositoryObject obj = null;
		contents = Files.toByteArray(file);
		validationResult.setLocation(new ValidationLocation(file.getName()));
		obj = CertificateRepositoryObjectFactory.createCertificateRepositoryObject(contents, validationResult);

		if(obj instanceof UnknownCertificateRepositoryObject){
			throw new Exception("Unknown object " + file.getName());
		}
		
		return obj;
	}
	
	public static CertificateObject createCertificateObject(String pth, ValidationResult result) throws Exception {
		
		File file = new File(pth);
		CertificateRepositoryObject obj = readCertificateRepositoryObjectFile(file, result);
		if(obj instanceof X509ResourceCertificate){
			CertificateObject cw = new CertificateObject(pth, file.getName(), (X509ResourceCertificate) obj);
			resourceObjects.put((X509ResourceCertificate)obj, cw);
			return cw;
		}
		
		throw new IllegalArgumentException("Invalid Certificate file "+file.getName());
	}
	
	public static ResourceHoldingObject createResourceHoldingObject(String pth, ValidationResult result, ResourceHoldingObject p) throws Exception{
		File file = new File(pth);
		CertificateRepositoryObject obj = readCertificateRepositoryObjectFile(file, result);

		
		if(obj instanceof X509ResourceCertificate){
			return createCertificateObjectWithParent(pth, file.getName(), (X509ResourceCertificate) obj,p);
		}
		
		if(obj instanceof RoaCms) {
			return createRoaObject(pth,file.getName(),(RoaCms) obj,p);
		}
		throw new IllegalArgumentException("Invalid ResourceHoldingObject "+file.getName());
	}
	
	public static CertificateObject createCertificateObjectWithParent(String path, String fname, X509ResourceCertificate cert, ResourceHoldingObject p){
		CertificateObject result = new CertificateObject(path, fname, cert,p);
		resourceObjects.put(cert, result);
		return result;
	}
	
	public static RoaObject createRoaObject(String path, String fname, RoaCms roa, ResourceHoldingObject p){
		RoaObject result = new RoaObject(path, fname, roa,p);
		resourceObjects.put(result.getCertificate(), result);
		return result;
	}
	
	public static ManifestObject createManifestObject(String path, ValidationResult result) throws Exception{
		File file = new File(path);
		CertificateRepositoryObject obj = readCertificateRepositoryObjectFile(file, result);
		
		if(obj instanceof ManifestCms){
			return new ManifestObject(path,file.getName(),(ManifestCms) obj);
		}
		throw new IllegalArgumentException("Invalid Manifest " + file.getName());
	}
	
	public static CRLObject createCRLObject(String path, ValidationResult result) throws Exception{
		File file = new File(path);
		CertificateRepositoryObject obj = readCertificateRepositoryObjectFile(file, result);
		
		if(obj instanceof X509Crl){
			return new CRLObject(path,file.getName(),(X509Crl) obj);
		}
		throw new IllegalArgumentException("Invalid CRL " + file.getName());
	}

	public static byte[] getHash(String path) throws Exception{
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
}
