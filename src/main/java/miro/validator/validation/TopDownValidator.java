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
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.miro.validator.ResourceCertificateTreeValidator;
import main.java.miro.validator.types.CRLObject;
import main.java.miro.validator.types.CertificateObject;
import main.java.miro.validator.types.ManifestObject;
import main.java.miro.validator.types.ResourceHoldingObject;
import main.java.miro.validator.types.RoaObject;
import main.java.miro.validator.types.ValidationResults;
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
		
			if(parent.getManifest() != null)
				parent.getManifest().validate(context, (CrlLocator) locator, options, result);
			
			if(parent.getCrl() != null)
				parent.getCrl().validate(context, (CrlLocator) locator, options, result);
			
			
			/* Validate children, e.g. CertificateObject, RoaObject */
			/* This also adds the validated CertificateObjects to the workQueue */
			validateChildren(parent);
		}
		log.log(Level.INFO,"Validating done");
	}

	private void validateChildren(CertificateObject certWrapper) {
		
		boolean manifestIsPresent = false;
		List<String> mftFiles = null;
		if(certWrapper.getManifest() !=null){
			mftFiles = getManifestFileListCopy(certWrapper);
			manifestIsPresent = true;
		}
		
		Iterator<ResourceHoldingObject> iter = certWrapper.getChildren().iterator();
		ResourceHoldingObject kid;
		while(iter.hasNext()){
			
			kid = iter.next();
			kid.validate(context, (CrlLocator) locator, options, result);
			
			if(kid instanceof CertificateObject)
				workQueue.add((CertificateObject) kid);
			
			if(manifestIsPresent){
				checkManifestForObject(certWrapper.getManifest(), kid);
				mftFiles.remove(kid.getFilename());
				if(!iter.hasNext())
					warnAboutMissingFiles(certWrapper.getManifest(), mftFiles);
			}
		}
	}
	
	public void checkManifestForObject(ManifestObject mft, ResourceHoldingObject obj) {
		ManifestCms manifest = mft.getManifest();
		/* Check if kid is mentioned in manifest */
		boolean isPresent = manifest.containsFile(obj.getFilename());
		result.warnIfFalse(isPresent, "missing.in.mft");

		/* Check if hash matches */
		if (isPresent) {
			byte[] kidHash = obj.getHash();
			boolean hashMatches = Arrays.equals(kidHash, manifest.getHash(obj.getFilename()));
			result.warnIfFalse(hashMatches, "wrong.hash");
		}
	}
	
	public void warnAboutMissingFiles(ManifestObject mft, List<String> missingFiles) {
		boolean changed = false;
		for (String missingFile : missingFiles) {
			result.warnForLocation(new ValidationLocation(mft.getFilename()), "missing.file", missingFile);
			changed = true;
		}
		if(changed)
			ValidationResults.transformToValidationResultsWithLocation(mft.getValidationResults(), result,
						new ValidationLocation(mft.getFilename()));
		
	}
	
	public List<String> getManifestFileListCopy(CertificateObject cert) {
		ManifestObject mft = cert.getManifest();
		List<String> mftFilesCopy = new ArrayList<String>();
		mftFilesCopy.addAll(mft.getManifest().getFileNames());
		if(cert.getCrl() != null)
			mftFilesCopy.remove(cert.getCrl().getFilename());
		return mftFilesCopy;
	}
	

	public ValidationResult getValidationResult() {
		return result;
	}

}
