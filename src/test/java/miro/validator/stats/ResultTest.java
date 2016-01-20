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
package test.java.miro.validator.stats;

import java.util.Random;
import java.util.Set;

import main.java.miro.validator.stats.StatsKeys;
import main.java.miro.validator.stats.types.Result;

public class ResultTest {
	
	
	public static void main(String[] args) {
		ResultTest test = new ResultTest();
		test.objectCounterTest();
		test.warningCounterTest();
		test.errorCounterTest();
		test.completeTest();
	}
	
	private void completeTest() {
		
		Result dummyResult = new Result("test");
		Random r = new Random();
		int cerObj = 900;
		
		/* Create some cer objects, count'em */
		for(int i = 0; i< cerObj;i++){
			dummyResult.incrementObjectCount(StatsKeys.TOTAL_CER_OBJECTS);
			dummyResult.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
			
			/* Create some valid, some invalid */
			if(r.nextBoolean()){
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_VALID_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.VALID_CER_OBJECTS);
			} else {
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.INVALID_CER_OBJECTS);
			}
			
			/* create some fake warnings */
			int createWarning = r.nextInt(4);
			if(createWarning == 2){
				dummyResult.incrementWarningCount("warning" + r.nextInt(10));
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.WARNING_CER_OBJECTS);
			}
			
		}
		assert(dummyResult.getObjectCount(StatsKeys.TOTAL_CER_OBJECTS) == cerObj);
		
		int roaObj = 400;
		for(int i = 0; i< roaObj;i++){
			dummyResult.incrementObjectCount(StatsKeys.TOTAL_ROA_OBJECTS);
			dummyResult.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
			
			if(r.nextBoolean()){
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_VALID_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.VALID_ROA_OBJECTS);
			} else {
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.INVALID_ROA_OBJECTS);
			}
			
			/* create some fake warnings */
			int createWarning = r.nextInt(4);
			if(createWarning == 2){
				dummyResult.incrementWarningCount("warning" + r.nextInt(10));
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.WARNING_ROA_OBJECTS);
			}
			
		}
		assert(dummyResult.getObjectCount(StatsKeys.TOTAL_ROA_OBJECTS) == roaObj);
		
		int mftObj = 900;
		for(int i = 0; i< mftObj;i++){
			dummyResult.incrementObjectCount(StatsKeys.TOTAL_MFT_OBJECTS);
			dummyResult.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
			
			if(r.nextBoolean()){
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_VALID_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.VALID_MFT_OBJECTS);
			} else {
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.INVALID_MFT_OBJECTS);
			}
			
			/* create some fake warnings */
			int createWarning = r.nextInt(4);
			if(createWarning == 2){
				dummyResult.incrementWarningCount("warning" + r.nextInt(10));
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.WARNING_MFT_OBJECTS);
			}
		}
		assert(dummyResult.getObjectCount(StatsKeys.TOTAL_MFT_OBJECTS) == mftObj);
		
		int crlObj = 900;
		for(int i = 0; i< crlObj;i++){
			dummyResult.incrementObjectCount(StatsKeys.TOTAL_CRL_OBJECTS);
			dummyResult.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
			
			
			if(r.nextBoolean()){
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_VALID_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.VALID_CRL_OBJECTS);
			} else {
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.INVALID_CRL_OBJECTS);
				
				dummyResult.incrementErrorCount("error" + r.nextInt(10));
				
				
				
			}
			
			/* create some fake warnings */
			int createWarning = r.nextInt(4);
			if(createWarning == 2){
				dummyResult.incrementWarningCount("warning" + r.nextInt(10));
				dummyResult.incrementObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS);
				dummyResult.incrementObjectCount(StatsKeys.WARNING_CRL_OBJECTS);
			}
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		assert(dummyResult.getObjectCount(StatsKeys.TOTAL_CRL_OBJECTS) == crlObj);
		int totalObj = crlObj + cerObj + roaObj + mftObj;
		assert dummyResult.getObjectCount(StatsKeys.TOTAL_OBJECTS) == totalObj;
		
		System.out.println(dummyResult);
		

		
		
		
		
		
		
		
	}

	public void objectCounterTest(){
		Result test = new Result("test");
		
		int cerObj = 300;
		for(int i = 0; i< cerObj;i++){
			test.incrementObjectCount(StatsKeys.TOTAL_CER_OBJECTS);
			test.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
		}
		assert(test.getObjectCount(StatsKeys.TOTAL_CER_OBJECTS) == cerObj);
		
		int roaObj = 300;
		for(int i = 0; i< roaObj;i++){
			test.incrementObjectCount(StatsKeys.TOTAL_ROA_OBJECTS);
			test.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
		}
		assert(test.getObjectCount(StatsKeys.TOTAL_ROA_OBJECTS) == roaObj);
		
		int mftObj = 300;
		for(int i = 0; i< roaObj;i++){
			test.incrementObjectCount(StatsKeys.TOTAL_MFT_OBJECTS);
			test.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
		}
		assert(test.getObjectCount(StatsKeys.TOTAL_MFT_OBJECTS) == mftObj);
		
		int crlObj = 300;
		for(int i = 0; i< crlObj;i++){
			test.incrementObjectCount(StatsKeys.TOTAL_CRL_OBJECTS);
			test.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
		}
		assert(test.getObjectCount(StatsKeys.TOTAL_CRL_OBJECTS) == crlObj);
		
		
		int totalObj = crlObj + cerObj + roaObj + mftObj;
		assert test.getObjectCount(StatsKeys.TOTAL_OBJECTS) == totalObj;
	}
	
	public void warningCounterTest(){
		
		Result test = new Result("test");
		int warningCount = 200;
		String[] dummyWarningKeys = new String[warningCount];
		int[] dummyWarningCount = new int[warningCount];
		Random r = new Random();
		
		/* Create 200 warnings and counts*/
		for(int i = 0;i<warningCount;i++){
			dummyWarningKeys[i] = "warning"+i;
			
			/* +1 so we don't have a 0 , since 0 occurences will obv not be logged. Else we'd have to log all possible keys..*/
			dummyWarningCount[i] = r.nextInt(40) + 1;
			
			/* Add them to our Result */
			for(int j = 0; j < dummyWarningCount[i]; j++){
				test.incrementWarningCount(dummyWarningKeys[i]);
			}
		}
		
		for(int i = 0;i<warningCount;i++){
			assert test.getWarningCount(dummyWarningKeys[i]) == dummyWarningCount[i];
		}
		
		Set<String> keys = test.getAllWarningKeys();
		Integer keysSize = keys.size();
		assert test.getAllWarningKeys().size() == warningCount;
		
	}
	
	public void errorCounterTest(){
		
		Result test = new Result("test");
		int errorCount = 200;
		String[] dummyErrorKeys = new String[errorCount];
		int[] dummyErrorCount = new int[errorCount];
		Random r = new Random();
		
		/* Create 200 warnings and counts*/
		for(int i = 0;i<errorCount;i++){
			dummyErrorKeys[i] = "error"+i;
			
			/* +1 so we don't have a 0 , since 0 occurences will obv not be logged. Else we'd have to log all possible keys..*/
			dummyErrorCount[i] = r.nextInt(40) + 1;
			
			/* Add them to our Result */
			for(int j = 0; j < dummyErrorCount[i]; j++){
				test.incrementErrorCount(dummyErrorKeys[i]);
			}
		}
		
		for(int i = 0;i<errorCount;i++){
			assert test.getErrorCount(dummyErrorKeys[i]) == dummyErrorCount[i];
		}
		
		assert test.getAllErrorKeys().size() == errorCount;
		
		
	}
	
	

}
