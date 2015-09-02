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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

import net.ripe.ipresource.IpResourceSet;
import net.ripe.rpki.commons.crypto.ValidityPeriod;
import net.ripe.rpki.commons.crypto.x509cert.X509ResourceCertificate;
import net.ripe.rpki.commons.validation.ValidationCheck;
import net.ripe.rpki.commons.validation.ValidationLocation;
import net.ripe.rpki.commons.validation.ValidationResult;
import net.ripe.rpki.commons.validation.ValidationStatus;

public abstract class ResourceHoldingObject extends RepositoryObject {
	
	public static final Logger log = Logger.getGlobal();
	
	protected ResourceHoldingObject parent;
	
	protected X509ResourceCertificate certificate;
	
	protected IpResourceSet resources;
	
	protected ValidityPeriod validityPeriod;
	
	public abstract X500Principal getIssuer();
	
	public abstract X500Principal getSubject();
	
	public abstract BigInteger getSerialNr();
	
	public ResourceHoldingObject(String path, String fname, X509ResourceCertificate cert){
		super(path,fname);
		this.certificate = cert;
		validityPeriod = certificate.getValidityPeriod();
	}
	
	public ResourceHoldingObject(String path, String fname, X509ResourceCertificate cert, ResourceHoldingObject p){
		this(path,fname,cert);
		parent = p;
	}
	
	
	public ValidityPeriod getValidityPeriod(){
		return validityPeriod;
	}
	
	public IpResourceSet getResources() {
		return resources;
	}
	
	
	public X509ResourceCertificate getCertificate() {
		return certificate;
	}
	
	public String getFilename(){
		return filename;
	}
	
	public ResourceHoldingObject getParent() { 
		return parent;
	}
	
	public void setParent(ResourceHoldingObject p ){
		parent = p;
	}
	
	public void setHash(byte[] h){
		hash = h;
	}
	
	public byte[] getHash(){
		return hash;
	}
}
