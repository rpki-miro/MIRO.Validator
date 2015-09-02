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
package main.java.miro.validator.export.json;

import java.lang.reflect.Type;
import java.util.ArrayList;

import main.java.miro.validator.types.ValidationResults;
import net.ripe.rpki.commons.validation.ValidationCheck;
import net.ripe.rpki.commons.validation.ValidationStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ValidationResultsSerializer implements JsonSerializer<ValidationResults> {

	public JsonElement serialize(
			ValidationResults src,
			Type typeOfSrc, JsonSerializationContext context) {
		
		JsonObject validation_results_json = new JsonObject();
		
		JsonArray passed_results_json = new JsonArray();
		JsonArray error_results_json = new JsonArray();
		JsonArray warning_results_json = new JsonArray();
		
		ArrayList<ValidationCheck> passed_checks = src.getValidationResults().get(ValidationStatus.PASSED);
		ArrayList<ValidationCheck> error_checks = src.getValidationResults().get(ValidationStatus.ERROR);
		ArrayList<ValidationCheck> warning_checks = src.getValidationResults().get(ValidationStatus.WARNING);
		
//		if(passed_checks != null){
//			for(ValidationCheck check : passed_checks){
//				passed_results_json.add(context.serialize(check,ValidationCheck.class));
//			}
//			validation_results_json.add("passed", passed_results_json);
//		}
		
		
		validation_results_json.add("isValid", new JsonPrimitive(error_checks.isEmpty()));
		
		if(error_checks != null){
			for(ValidationCheck check : error_checks){
				error_results_json.add(context.serialize(check,ValidationCheck.class));
			}
			validation_results_json.add("error", error_results_json);
		}
		
		if(warning_checks != null){
			for(ValidationCheck check : warning_checks){
				warning_results_json.add(context.serialize(check,ValidationCheck.class));
			}
			validation_results_json.add("warning", warning_results_json);
		}
		
		return validation_results_json;
	}

}
