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

package miro.validator.export.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import miro.validator.ResourceCertificateTreeValidator;
import miro.validator.types.CertificateObject;
import miro.validator.types.CRLObject;
import miro.validator.types.ManifestObject;
import miro.validator.types.ResourceHoldingObject;
import miro.validator.types.ValidationResults;
import net.ripe.ipresource.IpResourceSet;
import net.ripe.rpki.commons.validation.ValidationCheck;
import net.ripe.rpki.commons.validation.ValidationStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CertificateObjectJsonSerializer implements JsonSerializer<CertificateObject> {

	public JsonElement serialize(CertificateObject cw, Type typeOfSrc,
			JsonSerializationContext context) {
		
		JsonObject cwJson = new JsonObject();
		
		cwJson.add("filename", new JsonPrimitive(cw.getFilename()));
		cwJson.add("subject", new JsonPrimitive(cw.getSubject().toString()));
		cwJson.add("serial_nr", new JsonPrimitive(cw.getSerialNr().toString()));
		cwJson.add("issuer", new JsonPrimitive(cw.getIssuer().toString()));
		cwJson.add("subject_key_identifier", new JsonPrimitive(ResourceCertificateTreeValidator.bytesToHex(cw.getSubjectKeyIdentifier())));
		cwJson.add("public_key", new JsonPrimitive((cw.getPublicKey().toString())));
		cwJson.add("isEE", new JsonPrimitive(cw.getIsEE()));
		cwJson.add("isCA", new JsonPrimitive(cw.getIsCA()));
		cwJson.add("isRoot", new JsonPrimitive(cw.getIsRoot()));
		cwJson.add("validity_period", new JsonPrimitive(cw.getValidityPeriod().toString()));
		
		
		if(cw.getAki() != null){
			cwJson.add("authority_key_identifier", new JsonPrimitive(ResourceCertificateTreeValidator.bytesToHex(cw.getAki())));
		}
		cwJson.add("validation_result", context.serialize(cw.getValidationResults(),ValidationResults.class));
		cwJson.add("resources", context.serialize(cw.getResources(), IpResourceSet.class));
		
//		if(cw.getManifest() != null){
//			cwJson.add("manifest", context.serialize(cw.getManifest(),ManifestObject.class));
//		}
		
//		if(cw.getX509Crl() != null){
//			cwJson.add("crl",context.serialize(cw.getCrl(),CRLObject.class));
//		}
		
//		JsonArray children_json = new JsonArray();
//		JsonObject childJson;
//		String type;
//		if(cw.getChildren() != null){
//			for(ResourceHoldingObject kid : cw.getChildren()){
//				childJson = new JsonObject();
//				if(kid instanceof CertificateObject){
//					type = "cer";
//				} else {
//					type = "roa";
//				}
//				childJson.add("type", new JsonPrimitive(type));
//				childJson.add("child", context.serialize(kid,ResourceHoldingObject.class));
//				children_json.add(childJson);
//			}
//		}
//		cwJson.add("children",children_json);
		
		
		return cwJson;
	}
		

	public JsonObject validationCheckSerialize(ValidationCheck check){
		JsonObject result = new JsonObject();
		result.add("key", new JsonPrimitive(check.getKey()));
		
		JsonArray params = new JsonArray();
		for(String p : check.getParams()){
			params.add(new JsonPrimitive(p));
		}
		result.add("params", params);
		return result;
	}
	
	public JsonObject validationResultSerialize(HashMap<ValidationStatus, ArrayList<ValidationCheck>> validation_result) {

		JsonObject validation_results_json = new JsonObject();
		
		JsonArray passed_results_json = new JsonArray();
		JsonArray error_results_json = new JsonArray();
		JsonArray warning_results_json = new JsonArray();
		
		ArrayList<ValidationCheck> passed_checks = validation_result.get(ValidationStatus.PASSED);
		ArrayList<ValidationCheck> error_checks = validation_result.get(ValidationStatus.ERROR);
		ArrayList<ValidationCheck> warning_checks = validation_result.get(ValidationStatus.WARNING);
		
		if(passed_checks != null){
			for(ValidationCheck check : passed_checks){
				passed_results_json.add(validationCheckSerialize(check));
			}
			validation_results_json.add("passed", passed_results_json);
		}
		
		if(error_checks != null){
			for(ValidationCheck check : error_checks){
				error_results_json.add(validationCheckSerialize(check));
			}
			validation_results_json.add("error", error_results_json);
		}
		
		if(warning_checks != null){
			for(ValidationCheck check : warning_checks){
				warning_results_json.add(validationCheckSerialize(check));
			}
			validation_results_json.add("warning", warning_results_json);
		}
		
		return validation_results_json;

	}

}
