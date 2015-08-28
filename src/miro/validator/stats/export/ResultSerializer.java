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
package miro.validator.stats.export;

import java.lang.reflect.Type;
import java.util.ArrayList;

import miro.validator.stats.StatsKeys;
import miro.validator.stats.types.Result;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ResultSerializer implements JsonSerializer<Result> {

	public JsonElement serialize(Result src, Type typeOfSrc,
			JsonSerializationContext context) {
		
		
		JsonObject resultObj  = new JsonObject();
		
		resultObj.add(StatsKeys.TOTAL_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.TOTAL_OBJECTS)));
		resultObj.add(StatsKeys.TOTAL_CER_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.TOTAL_CER_OBJECTS)));
		resultObj.add(StatsKeys.TOTAL_MFT_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.TOTAL_MFT_OBJECTS)));
		resultObj.add(StatsKeys.TOTAL_CRL_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.TOTAL_CRL_OBJECTS)));
		resultObj.add(StatsKeys.TOTAL_ROA_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.TOTAL_ROA_OBJECTS)));
		
		resultObj.add(StatsKeys.TOTAL_VALID_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.TOTAL_VALID_OBJECTS)));
		resultObj.add(StatsKeys.VALID_CER_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.VALID_CER_OBJECTS)));
		resultObj.add(StatsKeys.VALID_MFT_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.VALID_MFT_OBJECTS)));
		resultObj.add(StatsKeys.VALID_CRL_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.VALID_CRL_OBJECTS)));
		resultObj.add(StatsKeys.VALID_ROA_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.VALID_ROA_OBJECTS)));
		
		resultObj.add(StatsKeys.TOTAL_INVALID_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS)));
		resultObj.add(StatsKeys.INVALID_CER_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.INVALID_CER_OBJECTS)));
		resultObj.add(StatsKeys.INVALID_MFT_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.INVALID_MFT_OBJECTS)));
		resultObj.add(StatsKeys.INVALID_CRL_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.INVALID_CRL_OBJECTS)));
		resultObj.add(StatsKeys.INVALID_ROA_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.INVALID_ROA_OBJECTS)));
		
		resultObj.add(StatsKeys.TOTAL_WARNING_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS)));
		resultObj.add(StatsKeys.WARNING_CER_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.WARNING_CER_OBJECTS)));
		resultObj.add(StatsKeys.WARNING_MFT_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.WARNING_MFT_OBJECTS)));
		resultObj.add(StatsKeys.WARNING_CRL_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.WARNING_CRL_OBJECTS)));
		resultObj.add(StatsKeys.WARNING_ROA_OBJECTS, new JsonPrimitive(src.getObjectCount(StatsKeys.WARNING_ROA_OBJECTS)));
		
		JsonObject warningsJson = new JsonObject();
		ArrayList<String> warningKeys = new ArrayList<String>(src.getAllWarningKeys());
		for(String warningKey : warningKeys){
			warningsJson.add(warningKey, new JsonPrimitive(src.getWarningCount(warningKey)));
		}
		resultObj.add("warnings", warningsJson);
		
		JsonObject errorsJson = new JsonObject();
		ArrayList<String> errorKeys = new ArrayList<String>(src.getAllErrorKeys());
		for(String errorKey : errorKeys){
			errorsJson.add(errorKey, new JsonPrimitive(src.getErrorCount(errorKey)));
		}
		resultObj.add("errors", errorsJson);
		
		return resultObj;
	}

}
