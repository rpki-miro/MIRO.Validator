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

import main.java.miro.validator.types.CertificateObject;
import main.java.miro.validator.types.RoaObject;
import main.java.miro.validator.types.ValidationResults;
import net.ripe.rpki.commons.crypto.cms.roa.RoaCms;
import net.ripe.rpki.commons.crypto.cms.roa.RoaPrefix;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RoaSerializer implements JsonSerializer<RoaObject> {

	public JsonElement serialize(RoaObject src, Type typeOfSrc,
			JsonSerializationContext context) {
		
		JsonObject roa_json = new JsonObject();
		
		RoaCms roa = src.getRoa();
		roa_json.add("filename", new JsonPrimitive(src.getFilename()));
		roa_json.add("asn", new JsonPrimitive(roa.getAsn().toString()));
		roa_json.add("validity_period", new JsonPrimitive(roa.getValidityPeriod().toString()));
		roa_json.add("signing_time", new JsonPrimitive(roa.getSigningTime().toString()));
		
		JsonArray prefixes_json = new JsonArray();
		JsonObject roa_prefix_json;
		for(RoaPrefix prefix : roa.getPrefixes()){
			roa_prefix_json = new JsonObject();
			roa_prefix_json.add("prefix", new JsonPrimitive(prefix.getPrefix().toString()));
			roa_prefix_json.add("maxLength", new JsonPrimitive(prefix.getEffectiveMaximumLength()));
			prefixes_json.add(roa_prefix_json);
		}
		
		roa_json.add("prefixes", prefixes_json);
		roa_json.add("validation_result", context.serialize(src.getValidationResults(),ValidationResults.class));
//		roa_json.add("eeCert", context.serialize(src.getEeCert(), CertificateObject.class));
		
		
		return roa_json;
	}

}
