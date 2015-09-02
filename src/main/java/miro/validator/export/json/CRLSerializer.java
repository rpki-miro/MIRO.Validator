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
import java.util.Set;

import main.java.miro.validator.types.CRLObject;
import main.java.miro.validator.types.ValidationResults;
import net.ripe.rpki.commons.crypto.crl.X509Crl;
import net.ripe.rpki.commons.crypto.crl.X509Crl.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CRLSerializer implements JsonSerializer<CRLObject> {

	public JsonElement serialize(CRLObject src, Type typeOfSrc,
			JsonSerializationContext context) {
		
		X509Crl crl = src.getCrl();
		JsonObject crl_json = new JsonObject();
		crl_json.add("filename", new JsonPrimitive(src.getFilename()));
		JsonArray revoked_certs_json = new JsonArray();
		JsonObject revoked_cert_json;
		Set<Entry> revoked_certs = crl.getRevokedCertificates();
		
		for(Entry e : revoked_certs){
			revoked_cert_json = new JsonObject();
			revoked_cert_json.add("serial_nr", new JsonPrimitive(e.getSerialNumber().toString()));
			revoked_cert_json.add("revocation_time", new JsonPrimitive(e.getRevocationDateTime().toString()));
			revoked_certs_json.add(revoked_cert_json);
		}
		
		crl_json.add("validation_result", context.serialize(src.getValidationResults(),ValidationResults.class));
		crl_json.add("revoked_certificates",revoked_certs_json);
		
		return crl_json;
	}

}
