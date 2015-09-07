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
	//TODO Deal with mft
	public static RepositoryObject createRepositoryObject(String path) {
		if(path.endsWith(".cer"))
			return createCertificateObject(path);
		if(path.endsWith(".roa"))
			return createRoaObject(path);
		if(path.endsWith(".crl"))
			return createCRLObject(path);
		return null;

	}
	
	private static CertificateRepositoryObject readCertificateRepositoryObjectFile(File file, ValidationResult validationResult) throws Exception {
		
		byte[] contents;
		CertificateRepositoryObject obj = null;
		contents = Files.toByteArray(file);
		obj = CertificateRepositoryObjectFactory.createCertificateRepositoryObject(contents, validationResult);

		if(obj instanceof UnknownCertificateRepositoryObject){
			throw new Exception("Unknown object " + file.getName());
		}
		return obj;
	}
	
	//TODO throw exception here, or return null, or return some status object (ObjectCreationResult ?)
	/*
	 * Options to handle objects:
	 * 
	 * 1. Throw exception, return null, or return shallow special case object (shallow means it can't be handled like a 
	 * normal object would, so requires checks as well, just like returning null and exception
	 * 
	 * 2. Create deep special case objects, that can be handled but are empty and have some indication that they are not 'real'
	 * This means the objects could pass processing and validation without too many extra checks, but users have to remember to check 
	 * 'realness' of objects so they don't draw wrong conclusions on object availability. Also might be very hard, since its complex
	 * objects that can't easily be "faked" and still pass validation/
	 */
	//TODO these fac functions are nearly identical, need to make them polymorphic
	public static CertificateObject createCertificateObject(String pth) {
		try {
			File file = new File(pth);
			ValidationResult result = ValidationResult.withLocation(pth);
			CertificateRepositoryObject obj;
			obj = readCertificateRepositoryObjectFile(file, result);
			if (obj instanceof X509ResourceCertificate) {
				CertificateObject cw = new CertificateObject(pth,
						file.getName(), (X509ResourceCertificate) obj);
				resourceObjects.put((X509ResourceCertificate) obj, cw);
				return cw;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//TODO logging
		}
		return null;
	}
	
	//TODO these fac functions are nearly identical, need to make them polymorphic
	public static ManifestObject createManifestObject(String path) {
		try {
			File file = new File(path);
			ValidationResult result = ValidationResult.withLocation(path);
			CertificateRepositoryObject obj;
			obj = readCertificateRepositoryObjectFile(file, result);
			if (obj instanceof ManifestCms) {
				return new ManifestObject(path, file.getName(),
						(ManifestCms) obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
			//TODO logging
		}
		return null;
	}
	
	//TODO these fac functions are nearly identical, need to make them polymorphic
	public static RoaObject createRoaObject(String path) {
		try {
			File file = new File(path);
			ValidationResult result = ValidationResult.withLocation(path);
			CertificateRepositoryObject obj;
			obj = readCertificateRepositoryObjectFile(file, result);
			if (obj instanceof RoaCms) {
				return new RoaObject(path, file.getName(), (RoaCms) obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
			//TODO logging
		}
		return null;
	}
	
	//TODO these fac functions are nearly identical, need to make them polymorphic
	public static CRLObject createCRLObject(String path) {
		try {
			File file = new File(path);
			ValidationResult result = ValidationResult.withLocation(path);
			CertificateRepositoryObject obj;
			obj = readCertificateRepositoryObjectFile(file, result);
			if (obj instanceof X509Crl) {
				return new CRLObject(path, file.getName(), (X509Crl) obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
			//TODO logging
		}
		return null;
	}
	
	public static ResourceHoldingObject createResourceHoldingObject(String pth, ValidationResult result, ResourceHoldingObject p) throws Exception{
		File file = new File(pth);
		CertificateRepositoryObject obj = readCertificateRepositoryObjectFile(file, result);

		
		if(obj instanceof X509ResourceCertificate){
			return createCertificateObjectWithParent(pth, file.getName(), (X509ResourceCertificate) obj,p);
		}
		
		if(obj instanceof RoaCms) {
			return createRoaObject(pth);
		}
		throw new IllegalArgumentException("Invalid ResourceHoldingObject "+file.getName());
	}
	
	public static CertificateObject createCertificateObjectWithParent(String path, String fname, X509ResourceCertificate cert, ResourceHoldingObject p){
		CertificateObject result = new CertificateObject(path, fname, cert,p);
		resourceObjects.put(cert, result);
		return result;
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
