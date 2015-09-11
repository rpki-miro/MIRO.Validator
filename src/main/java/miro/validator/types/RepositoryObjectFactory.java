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
	
	public static final Logger log = Logger.getLogger(RepositoryObjectFactory.class.getName());
	
	public static HashMap<X509ResourceCertificate, ResourceHoldingObject> resourceObjects = new HashMap<X509ResourceCertificate, ResourceHoldingObject>();

	
	public static void clearResourceObjectsMap() {
		resourceObjects = new HashMap<X509ResourceCertificate, ResourceHoldingObject>();
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
			ValidationResult result = ValidationResult.withLocation(pth);
			CertificateRepositoryObject obj = createCertificateRepositoryObject(pth, result);
			if (obj instanceof X509ResourceCertificate) {
				CertificateObject cw = new CertificateObject(getFilename(pth), (X509ResourceCertificate) obj);
				ValidationResults.transformToValidationResults(cw.getValidationResults(), result);
				cw.setHash(getHash(pth));
				resourceObjects.put((X509ResourceCertificate) obj, cw);
				return cw;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//TODO logging
		}
		return null;
	}

	public static CertificateObject createCertificateObjectWithParent(String path, CertificateObject p){
		try {
			ValidationResult result = ValidationResult.withLocation(path);
			CertificateRepositoryObject obj = createCertificateRepositoryObject(path, result);
			if (obj instanceof X509ResourceCertificate) {
				CertificateObject cw = new CertificateObject(getFilename(path), (X509ResourceCertificate) obj, p);
				ValidationResults.transformToValidationResults(cw.getValidationResults(), result);
				cw.setHash(getHash(path));
				resourceObjects.put((X509ResourceCertificate) obj, cw);
				return cw;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//TODO logging
		}
		return null;
	}

	public static CertificateObject createCertificateObjectWithCertificateAndParent(String name, X509ResourceCertificate cert, CertificateObject p){
		CertificateObject cw = new CertificateObject(name, cert, p);
		resourceObjects.put(cert, cw);
		return cw;
	}
	

	public static RoaObject createRoaObjectWithParent(String path, CertificateObject p) {
		try {
			ValidationResult result = ValidationResult.withLocation(path);
			CertificateRepositoryObject obj = createCertificateRepositoryObject(path, result);
			if (obj instanceof RoaCms) {
				RoaObject roa = new RoaObject(getFilename(path), (RoaCms) obj, p);
				ValidationResults.transformToValidationResults(roa.getValidationResults(), result);
				roa.setHash(getHash(path));
				return roa;
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
			ValidationResult result = ValidationResult.withLocation(path);
			CertificateRepositoryObject obj = createCertificateRepositoryObject(path, result);
			if (obj instanceof ManifestCms) {
				ManifestObject manifest =  new ManifestObject(getFilename(path),(ManifestCms) obj);
				ValidationResults.transformToValidationResults(manifest.getValidationResults(), result);
				manifest.setHash(getHash(path));
				return manifest;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//TODO log
		}
		return null;
	}
	
	//TODO these fac functions are nearly identical, need to make them polymorphic
	public static CRLObject createCRLObject(String path) {
		try {
			ValidationResult result = ValidationResult.withLocation(path);
			CertificateRepositoryObject obj = createCertificateRepositoryObject(path, result);
			if (obj instanceof X509Crl) {
				CRLObject crl = new CRLObject(getFilename(path), (X509Crl) obj);
				ValidationResults.transformToValidationResults(crl.getValidationResults(), result);
				crl.setHash(getHash(path));
				return crl;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//TODO logging
		}
		return null;
	}
	
	//TODO throw exception here? might not be best
	public static CertificateRepositoryObject createCertificateRepositoryObject(String path, ValidationResult result) throws Exception {
		File file = new File(path);
		CertificateRepositoryObject obj;
		obj = readCertificateRepositoryObjectFile(file, result);
		return obj;
	}

	//TODO throw exception here? might not be best
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
	
	public static String getFilename(String path) {
		File f = new File(path);
		return f.getName();
	}
}
