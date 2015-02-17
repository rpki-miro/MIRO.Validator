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
import java.util.Map;

import miro.validator.types.ManifestObject;
import miro.validator.types.ValidationResults;
import net.ripe.rpki.commons.crypto.cms.manifest.ManifestCms;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ManifestSerializer implements JsonSerializer<ManifestObject> {

	@Override
	public JsonElement serialize(ManifestObject src, Type typeOfSrc,
			JsonSerializationContext context) {
		
		JsonObject manifest_json = new JsonObject();
		JsonArray mft_files_json = new JsonArray();
		JsonObject file_json;
		
		manifest_json.add("filename", new JsonPrimitive(src.getFilename()));
		ManifestCms manifest = src.getManifest();
		Map<String, byte[]> files = manifest.getFiles();
		
		for(String filename : manifest.getFileNames()){
			file_json = new JsonObject();
			file_json.add("filename", new JsonPrimitive(filename));
			file_json.add("hash", context.serialize(files.get(filename)));
			mft_files_json.add(file_json);
		}
		manifest_json.add("files", mft_files_json);
		manifest_json.add("validation_result", context.serialize(src.getValidationResults(),ValidationResults.class));
		manifest_json.add("validity_period", new JsonPrimitive(src.getValidityPeriod().toString()));
		return manifest_json;
	}

}
