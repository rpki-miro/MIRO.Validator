package main.java.miro.validator.export.json;

import java.lang.reflect.Type;

import main.java.miro.validator.types.CRLObject;
import main.java.miro.validator.types.CertificateObject;
import main.java.miro.validator.types.ManifestObject;
import main.java.miro.validator.types.RepositoryObject;
import main.java.miro.validator.types.RoaObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RepositoryObjectSerializer implements JsonSerializer<RepositoryObject> {

	public JsonElement serialize(RepositoryObject src, Type typeOfSrc,
			JsonSerializationContext context) {
		
		
		if(src instanceof CertificateObject){
			return context.serialize(src, CertificateObject.class);
		}
		
		if(src instanceof ManifestObject){
			return context.serialize(src, ManifestObject.class);
		}
		
		if(src instanceof CRLObject){
			return context.serialize(src, CRLObject.class);
		}
		
		if(src instanceof RoaObject){
			return context.serialize(src, RoaObject.class);
		}
		
		return null;
	}

}
