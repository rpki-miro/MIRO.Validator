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
package main.java.miro.validator.stats;

import java.util.HashMap;

public class StatsKeys {
	
	private StatsKeys(){
		
	}
	
	/* Objects are .cer | .roa | .mft | .crl*/
	
	/* Object counters */
	public static final String TOTAL_OBJECTS = "total.objects";
	public static final String TOTAL_CER_OBJECTS = "total.cer.objects";
	public static final String TOTAL_MFT_OBJECTS = "total.mft.objects";
	public static final String TOTAL_CRL_OBJECTS = "total.crl.objects";
	public static final String TOTAL_ROA_OBJECTS = "total.roa.objects";
	
	/* Valid object counters */
	public static final String TOTAL_VALID_OBJECTS = "valid.objects";
	public static final String VALID_CER_OBJECTS = "valid.cer.objects";
	public static final String VALID_MFT_OBJECTS = "valid.mft.objects";
	public static final String VALID_CRL_OBJECTS = "valid.crl.objects";
	public static final String VALID_ROA_OBJECTS = "valid.roa.objects";
	
	/* Invalid object counters */
	public static final String TOTAL_INVALID_OBJECTS = "invalid.objects";
	public static final String INVALID_CER_OBJECTS = "invalid.cer.objects";
	public static final String INVALID_MFT_OBJECTS = "invalid.mft.objects";
	public static final String INVALID_CRL_OBJECTS = "invalid.crl.objects";
	public static final String INVALID_ROA_OBJECTS = "invalid.roa.objects";
	
	/* Objects with 1 or more warnings */
	public static final String TOTAL_WARNING_OBJECTS = "warning.objects";
	public static final String WARNING_CER_OBJECTS = "warning.cer.objects";
	public static final String WARNING_MFT_OBJECTS = "warning.mft.objects";
	public static final String WARNING_CRL_OBJECTS = "warning.crl.objects";
	public static final String WARNING_ROA_OBJECTS = "warning.roa.objects";
	
	
	
	

}
