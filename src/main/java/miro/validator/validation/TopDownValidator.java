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

package main.java.miro.validator.validation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.miro.validator.ResourceCertificateTreeValidator;
import main.java.miro.validator.types.CRLObject;
import main.java.miro.validator.types.CertificateObject;
import main.java.miro.validator.types.ManifestObject;
import main.java.miro.validator.types.ResourceHoldingObject;
import main.java.miro.validator.types.RoaObject;
import net.ripe.rpki.commons.crypto.cms.manifest.ManifestCms;
import net.ripe.rpki.commons.crypto.crl.CrlLocator;
import net.ripe.rpki.commons.crypto.crl.X509Crl;
import net.ripe.rpki.commons.validation.ValidationLocation;
import net.ripe.rpki.commons.validation.ValidationOptions;
import net.ripe.rpki.commons.validation.ValidationResult;
import net.ripe.rpki.commons.validation.objectvalidators.CertificateRepositoryObjectValidationContext;
import net.ripe.rpki.commons.validation.objectvalidators.ResourceCertificateLocator;

public class TopDownValidator {
	public static final Logger log = Logger.getGlobal();
	
    private ValidationResult result;
    private ResourceCertificateLocator locator;
    private ValidationOptions options;
    private Queue<CertificateObject> workQueue;
    private CertificateRepositoryObjectValidationContext context;
	
    public TopDownValidator(ValidationResult result, ResourceCertificateLocator locator, CertificateObject trustAnchor){
    	this.result = result;
    	this.locator = locator;
    	this.options = new ValidationOptions();
    	this.workQueue = new LinkedList<CertificateObject>();
    	this.context = new CertificateRepositoryObjectValidationContext(URI.create(trustAnchor.getFilename()), trustAnchor.getCertificate());
    	this.workQueue.add(trustAnchor);
    }
	
	public void validate() {
		while(!workQueue.isEmpty()){
		
			/* Get next Certificate */
			CertificateObject parent = workQueue.remove();
			
			/* If its not a trust anchor, set up a new context */
			if(!parent.getIsRoot()){
				this.context = this.context.createChildContext(URI.create(parent.getFilename()), parent.getCertificate());
			}
			
			/* Verify that mft and crl are not missing, rpki-commons does not do this */
			result.setLocation(new ValidationLocation(parent.getFilename()));
			result.warnIfNull(parent.getManifest(), "missing.manifest"); 
			result.warnIfNull(parent.getCrl(), "missing.crl");
		
			validateManifest(parent.getManifest());
			validateCrl(parent.getCrl());
			
			/* Validate children, e.g. CertificateObject, RoaObject */
			/* This also adds the validated CertificateObjects to the workQueue */
			validateChildren(parent);
		}
		log.log(Level.INFO,"Validating done");
	}
	private void validateChildren(CertificateObject certWrapper) {
		
		ArrayList<ResourceHoldingObject> children = certWrapper.getChildren();
		if(children == null || children.isEmpty()){
			return;
		}
		
		CertificateObject certBuf;
		RoaObject roaBuf;
		
		
		ManifestObject mftWrap = certWrapper.getManifest();
		ManifestCms mft = null;
		ArrayList<String> mftFilesCopy = null;
		boolean checkMft = false;
		if(mftWrap !=null){
			mft = mftWrap.getManifest();
			mftFilesCopy = new ArrayList<String>();
			mftWrap.getManifest().getFileNames().addAll(mftFilesCopy);
			checkMft = true;
		}
		
		Iterator<ResourceHoldingObject> iter = certWrapper.getChildren().iterator();
		ResourceHoldingObject kid;
		while(iter.hasNext()){
			
			kid = iter.next();
			
			if(kid instanceof CertificateObject){
				certBuf = (CertificateObject) kid;
				certBuf.getCertificate().validate(certBuf.getFilename(), context, (CrlLocator) locator, options, result);
				workQueue.add(certBuf);
			}
			
			if(kid instanceof RoaObject){
				roaBuf = (RoaObject) kid;
				roaBuf.getRoa().validate(roaBuf.getFilename(), context, (CrlLocator) locator, options, result);
			}
			
			if(checkMft){
				
				/*Check if kid is mentioned in manifest*/
				boolean isPresent = mft.containsFile(kid.getFilename());
				result.warnIfFalse(isPresent, "missing.in.mft");
				
				/*Check if hash matches*/
				if(isPresent){
					byte[] kidHash = kid.getHash();
					byte[] mftHash = mft.getHash(kid.getFilename());
					boolean sameHash = Arrays.equals(kidHash, mftHash);
					result.warnIfFalse(sameHash,"wrong.hash");
				}
				
				mftFilesCopy.remove(kid.getFilename());
				
				/*Warn mft about missing files*/
				if(!iter.hasNext()){
					for(String missingFile : mftFilesCopy){
						result.warnForLocation(new ValidationLocation(mftWrap.getFilename()), "missing.file", missingFile);
					}
				}
			}
		}
	}
	

	public ValidationResult getValidationResult() {
		return result;
	}
	private void validateCrl(CRLObject crlWrap) {
		if(crlWrap == null){
			return;
		}
		X509Crl crl = crlWrap.getCrl();
		crl.validate(crlWrap.getFilename(), context, (CrlLocator) locator, options, result);
	}

	private void validateManifest(ManifestObject mftWrap) {
		if(mftWrap == null){
			return;
		}
		ManifestCms mft = mftWrap.getManifest();
		mft.validate(mftWrap.getFilename(), context, (CrlLocator) locator, options, result);
	}
}
