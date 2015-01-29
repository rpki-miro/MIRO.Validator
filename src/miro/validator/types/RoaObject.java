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
package miro.validator.types;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

import org.joda.time.DateTime;

import net.ripe.ipresource.IpResourceSet;
import net.ripe.rpki.commons.crypto.cms.roa.RoaCms;
import net.ripe.rpki.commons.crypto.crl.CrlLocator;
import net.ripe.rpki.commons.crypto.x509cert.X509ResourceCertificate;
import net.ripe.rpki.commons.validation.ValidationCheck;
import net.ripe.rpki.commons.validation.ValidationLocation;
import net.ripe.rpki.commons.validation.ValidationOptions;
import net.ripe.rpki.commons.validation.ValidationResult;
import net.ripe.rpki.commons.validation.ValidationStatus;
import net.ripe.rpki.commons.validation.objectvalidators.CertificateRepositoryObjectValidationContext;

public class RoaObject extends ResourceHoldingObject {
	static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private RoaCms roa;
	
	private CertificateObject eeCert;
	
	
	public RoaObject(String pth, String name, RoaCms r, ResourceHoldingObject p){
		super(pth,name,r.getCertificate(),p);
		this.roa = r;
		this.eeCert = RepositoryObjectFactory.createCertWrapper(pth, name, roa.getCertificate(), p);
	}
	
	public DateTime getSigningTime(){
		return roa.getSigningTime();
	}
	
	public RoaCms getRoa() {
		return roa;
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
	
	
	public void extractOwnResults(ValidationResult result){
		ArrayList<ValidationCheck> allChecks = (ArrayList<ValidationCheck>) result.getAllValidationChecksForLocation(new ValidationLocation(filename));
		
		HashMap<ValidationStatus, ArrayList<ValidationCheck>> validationRes = new HashMap<ValidationStatus, ArrayList<ValidationCheck>>();
		ArrayList<ValidationCheck> passed = new ArrayList<ValidationCheck>();
		ArrayList<ValidationCheck> warning = new ArrayList<ValidationCheck>();
		ArrayList<ValidationCheck> error = new ArrayList<ValidationCheck>();
		
		
		for(ValidationCheck check : allChecks){
			switch(check.getStatus()){
			case ERROR:
				error.add(check);
				break;
			case PASSED:
				passed.add(check);
				break;
			case WARNING:
				warning.add(check);
				break;
			default:
				break;
			}
		}
		
		validationRes.put(ValidationStatus.ERROR, error);
		validationRes.put(ValidationStatus.PASSED, passed);
		validationRes.put(ValidationStatus.WARNING, warning);
		
		validationResults = new ValidationResults(validationRes);
		eeCert.validationResults = validationResults;
	}
	

}
