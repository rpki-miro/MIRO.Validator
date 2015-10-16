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

import java.math.BigInteger;

import javax.security.auth.x500.X500Principal;

import org.joda.time.DateTime;

import net.ripe.ipresource.Asn;
import net.ripe.rpki.commons.crypto.cms.roa.RoaCms;
import net.ripe.rpki.commons.crypto.crl.CrlLocator;
import net.ripe.rpki.commons.validation.ValidationLocation;
import net.ripe.rpki.commons.validation.ValidationOptions;
import net.ripe.rpki.commons.validation.ValidationResult;
import net.ripe.rpki.commons.validation.objectvalidators.CertificateRepositoryObjectValidationContext;

public class RoaObject extends ResourceHoldingObject {
	
	private RoaCms roa;
	
	private CertificateObject eeCert;
	
	public RoaObject(String name, RoaCms r) {
		super(name, r.getCertificate());
		this.roa = r;
	}
	
	public RoaObject(String name, RoaCms r, CertificateObject p){
		this(name,r);
		parent = p;
		//TODO eeCerts parent..?
		this.eeCert = RepositoryObjectFactory.createCertificateObjectWithCertificateAndParent(name + "_eeCert" , roa.getCertificate(), p);
		this.eeCert.validationResults = this.validationResults;
	}
	
	public DateTime getSigningTime(){
		return roa.getSigningTime();
	}
	
	public RoaCms getRoa() {
		return roa;
	}
	
	public Asn getAsn() {
		return roa.getAsn();
	}

	@Override
	public X500Principal getIssuer() {
		return roa.getCertificateIssuer();
	}
	
	public X500Principal getSubject() {
		return roa.getCertificateSubject();
	}
	
	public CertificateObject getEeCert(){
		return eeCert;
	}
	
	public BigInteger getSerialNr() {
		return eeCert.getSerialNr();
	}

	@Override
	public void validate(CertificateRepositoryObjectValidationContext context,
			CrlLocator crlLocator, ValidationOptions options,ValidationResult result) {
		
		roa.validate(getFilename(), context, crlLocator, options, result);
		ValidationResults.transformToValidationResultsWithLocation(
				getValidationResults(), result, new ValidationLocation(
						getFilename()));
	}
	
}
