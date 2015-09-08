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
package test.java.miro.validator;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import main.java.miro.validator.types.ValidationResults;
import net.ripe.rpki.commons.validation.ValidationCheck;
import net.ripe.rpki.commons.validation.ValidationStatus;

import org.junit.Test;

public class ValidationResultsTest {

	@Test
	public void addChecksToValidationResultTest() {
		ValidationCheck errorCheck1 = new ValidationCheck(ValidationStatus.ERROR, "error1");
		ValidationCheck errorCheck2 = new ValidationCheck(ValidationStatus.ERROR, "error2");

		ValidationCheck warningCheck1 = new ValidationCheck(ValidationStatus.WARNING, "warning1");
		ValidationCheck warningCheck2 = new ValidationCheck(ValidationStatus.WARNING, "warning2");
		
		ValidationCheck passedCheck1 = new ValidationCheck(ValidationStatus.PASSED, "passed1");
		ValidationCheck passedCheck2 = new ValidationCheck(ValidationStatus.PASSED, "passed2");
		
		List<ValidationCheck> checks = new ArrayList<ValidationCheck>();
		checks.add(passedCheck2);
		checks.add(passedCheck1);
		checks.add(warningCheck2);
		checks.add(warningCheck1);
		checks.add(errorCheck2);
		checks.add(errorCheck1);
		
		
		ValidationResults results = new ValidationResults();
		ValidationResults.addValidationChecksToValidationResults(results, checks);
		
		assertTrue(results.getErrors().contains(errorCheck2));
		assertTrue(results.getErrors().contains(errorCheck1));
		
		assertTrue(results.getWarnings().contains(warningCheck1));
		assertTrue(results.getWarnings().contains(warningCheck2));
		
		assertTrue(results.getPassed().contains(passedCheck1));
		assertTrue(results.getPassed().contains(passedCheck2));
	}

}
