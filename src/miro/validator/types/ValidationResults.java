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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.ripe.rpki.commons.validation.ValidationCheck;
import net.ripe.rpki.commons.validation.ValidationStatus;

public class ValidationResults {
	
	private HashMap<ValidationStatus, ArrayList<ValidationCheck>> validationResults;
	
	public ValidationResults(HashMap<ValidationStatus, ArrayList<ValidationCheck>> validationRes) {
		validationResults = validationRes;
	}

	public HashMap<ValidationStatus, ArrayList<ValidationCheck>> getValidationResults() {
		return validationResults;
	}
	
	public List<ValidationCheck> getWarnings(){
		return validationResults.get(ValidationStatus.WARNING);
	}
	
	public List<ValidationCheck> getErrors(){
		return validationResults.get(ValidationStatus.ERROR);
	}
	
	public ValidationStatus getValidationStatus() {
		if(!getErrors().isEmpty()){
			return ValidationStatus.ERROR;
		}
		
		if(!getWarnings().isEmpty()){
			return ValidationStatus.WARNING;
		}
		
		return ValidationStatus.PASSED;
		
		
	}
	
	
}
