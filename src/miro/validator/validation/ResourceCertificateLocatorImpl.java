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

package miro.validator.validation;

import java.net.URI;

import miro.validator.types.CertificateObject;
import miro.validator.types.RepositoryObjectFactory;
import miro.validator.types.ResourceHoldingObject;
import net.ripe.rpki.commons.crypto.CertificateRepositoryObjectFile;
import net.ripe.rpki.commons.crypto.crl.CrlLocator;
import net.ripe.rpki.commons.crypto.crl.X509Crl;
import net.ripe.rpki.commons.crypto.x509cert.X509ResourceCertificate;
import net.ripe.rpki.commons.validation.ValidationResult;
import net.ripe.rpki.commons.validation.objectvalidators.CertificateRepositoryObjectValidationContext;
import net.ripe.rpki.commons.validation.objectvalidators.ResourceCertificateLocator;

public class ResourceCertificateLocatorImpl implements
		ResourceCertificateLocator, CrlLocator {

	public CertificateRepositoryObjectFile<X509ResourceCertificate> findParent(
			X509ResourceCertificate certificate) {
		ResourceHoldingObject objWrap = RepositoryObjectFactory.resourceObjects.get(certificate);
		
		if(objWrap == null){
			return null;
		} else {
			ResourceHoldingObject parentWrap = objWrap.getParent();
			return new CertificateRepositoryObjectFile<X509ResourceCertificate>(X509ResourceCertificate.class, parentWrap.getCertificate().getSubject().toString(), parentWrap.getCertificate().getEncoded());
		}
		
	}

	public CertificateRepositoryObjectFile<X509Crl> findCrl(
			X509ResourceCertificate certificate) {
		
		ResourceHoldingObject objWrap = RepositoryObjectFactory.resourceObjects.get(certificate);
		if(objWrap == null){
			return null;
		}
		
		
		CertificateObject parent = (CertificateObject) objWrap.getParent();
		X509Crl crl = parent.getX509Crl();

		if(crl == null){
			return null;
		}
		
		return new CertificateRepositoryObjectFile<X509Crl>(X509Crl.class, "crl", crl.getEncoded());
		
		
	}

	public X509Crl getCrl(URI uri,
			CertificateRepositoryObjectValidationContext context,
			ValidationResult result) {
		
		
		CertificateObject objWrap = (CertificateObject) RepositoryObjectFactory.resourceObjects.get(context.getCertificate());
		return objWrap.getX509Crl();
		
	}

}
