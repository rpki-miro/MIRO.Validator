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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.ripe.rpki.commons.validation.ValidationCheck;
import net.ripe.rpki.commons.validation.ValidationResult;
import net.ripe.rpki.commons.validation.ValidationStatus;

public class ValidationResults {
	
	private HashMap<ValidationStatus, ArrayList<ValidationCheck>> validationResults;
	
	private ValidationStatus validationStatus;
	
	public ValidationResults() {
		validationResults = new HashMap<ValidationStatus, ArrayList<ValidationCheck>>();
		validationResults.put(ValidationStatus.ERROR, new ArrayList<ValidationCheck>());
		validationResults.put(ValidationStatus.WARNING, new ArrayList<ValidationCheck>());
		validationResults.put(ValidationStatus.PASSED, new ArrayList<ValidationCheck>());
	}

	private void setValidationStatus() {
		if(!getErrors().isEmpty()){
			validationStatus = ValidationStatus.ERROR;
		}
		else if(!getWarnings().isEmpty()){
			validationStatus = ValidationStatus.WARNING;
		}
		else {
			validationStatus = ValidationStatus.PASSED;
		}
		
	}
	
	public ValidationStatus getValidationStatus() {
		return validationStatus;
	}

	public HashMap<ValidationStatus, ArrayList<ValidationCheck>> getValidationResults() {
		return validationResults;
	}
	
	public List<ValidationCheck> getPassed(){
		return validationResults.get(ValidationStatus.PASSED);
	}
	
	public List<ValidationCheck> getWarnings(){
		return validationResults.get(ValidationStatus.WARNING);
	}
	
	public List<ValidationCheck> getErrors(){
		return validationResults.get(ValidationStatus.ERROR);
	}
	
	/** 
	 * Copies the ValidationCheck's from the current location in oldResult to newResult
	 * @param oldResult
	 * @param newResult
	 */
	public static void transformToValidationResults(ValidationResults newResult, ValidationResult oldResult) {
		List<ValidationCheck> allChecks = (ArrayList<ValidationCheck>) oldResult.getAllValidationChecksForCurrentLocation();
		
		List<ValidationCheck> passed = newResult.getPassed();
		List<ValidationCheck> warning = newResult.getWarnings();
		List<ValidationCheck> error = newResult.getErrors();
		
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
		newResult.setValidationStatus();
	}
}
