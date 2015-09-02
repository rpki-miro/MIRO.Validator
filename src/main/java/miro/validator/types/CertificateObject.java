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
import java.math.BigInteger;
import java.net.URI;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

import main.java.miro.validator.ResourceCertificateTreeValidator;
import net.ripe.ipresource.IpResourceSet;
import net.ripe.rpki.commons.crypto.ValidityPeriod;
import net.ripe.rpki.commons.crypto.cms.manifest.ManifestCms;
import net.ripe.rpki.commons.crypto.cms.roa.RoaCms;
import net.ripe.rpki.commons.crypto.crl.CrlLocator;
import net.ripe.rpki.commons.crypto.crl.X509Crl;
import net.ripe.rpki.commons.crypto.x509cert.X509CertificateInformationAccessDescriptor;
import net.ripe.rpki.commons.crypto.x509cert.X509ResourceCertificate;
import net.ripe.rpki.commons.validation.ValidationCheck;
import net.ripe.rpki.commons.validation.ValidationOptions;
import net.ripe.rpki.commons.validation.ValidationResult;
import net.ripe.rpki.commons.validation.ValidationStatus;
import net.ripe.rpki.commons.validation.objectvalidators.CertificateRepositoryObjectValidationContext;

public class CertificateObject extends ResourceHoldingObject {
	static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private BigInteger serialNr;
	private X500Principal subject;
	private byte[] subjectKeyIdentifier;
	private PublicKey publicKey;
	private X500Principal issuer;
	private boolean isEE;
	private boolean isCA;
	private boolean isRoot;
	private byte[] aki;
	private X509CertificateInformationAccessDescriptor[] aias;
	
	private ManifestObject manifest;
	private CRLObject crl;
	private ArrayList<ResourceHoldingObject> children;

	public CertificateObject(String pth, String fname, X509ResourceCertificate cert) {
		super(pth,fname,cert);
		issuer = certificate.getIssuer();
		subjectKeyIdentifier = certificate.getSubjectKeyIdentifier();
		subject = certificate.getSubject();
		publicKey = certificate.getPublicKey();
		serialNr = certificate.getSerialNumber();
		
		isEE = certificate.isEe();
		isCA = certificate.isCa();
		isRoot = certificate.isRoot();
		
		aki = certificate.getAuthorityKeyIdentifier();
		aias = certificate.getAuthorityInformationAccess();
		if(isRoot){
			resources = certificate.getResources();
		} 
	}
	
	public CertificateObject(String pth, String fname, X509ResourceCertificate cert, ResourceHoldingObject parent) {
		this(pth,fname,cert);
		this.parent = parent;
		this.resources = certificate.deriveResources(parent.getResources());
	}
	
	public void findManifest(ValidationResult result){
		URI mftUri = certificate.getManifestUri();
		String path = ResourceCertificateTreeValidator.toPath(mftUri);
		try {
			manifest = RepositoryObjectFactory.createManifestObject(path, result);
			manifest.setRemoteLocation(mftUri);
		} catch (Exception e) {
			log.log(Level.WARNING, "Could not read manifest " + path + " for " + getFilename());
			return;
		}
	}
	
	public X500Principal getIssuer() {
		return this.issuer;
	}
	
	
	public X509ResourceCertificate getCertificate() {
		return certificate;
	}
	
	public ArrayList<ResourceHoldingObject> getChildren() {
		return children;
	}

	public X500Principal getSubject() {
		return subject;
	}

	public boolean getIsEE() {
		return isEE;
	}

	public boolean getIsCA() {
		return isCA;
	}

	public boolean getIsRoot() {
		return isRoot;
	}

	public X509Crl getX509Crl() {
		if(crl == null){
			return null;
		}
		return crl.getCrl();
	}
	
	public CRLObject getCrl() {
		return crl;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public BigInteger getSerialNr() {
		return serialNr;
	}

	public byte[] getSubjectKeyIdentifier() {
		return subjectKeyIdentifier;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public ManifestObject getManifest() {
		return manifest;
	}

	public byte[] getAki() {
		return aki;
	}

	public X509CertificateInformationAccessDescriptor[] getAias() {
		return aias;
	}


	public void setChildren(ArrayList<ResourceHoldingObject> children) {
		this.children = children;
	}

	public void setCrl(CRLObject crl) {
		this.crl = crl;
	}

	public void setParent(CertificateObject parent) {
		this.parent = parent;
	}

	public IpResourceSet getResources() {
		return resources;
	}
	
	public ArrayList<String> getChildrenFilenames(){
		ArrayList<String> kid_filenames = new ArrayList<String>();
		if(this.children == null){
			return kid_filenames;
		}
		
		for(ResourceHoldingObject c : this.children){
			kid_filenames.add(c.getFilename());
		}
		return kid_filenames;
	}

	public void setManifest(ManifestObject manifest) {
		this.manifest = manifest;
	}



}
