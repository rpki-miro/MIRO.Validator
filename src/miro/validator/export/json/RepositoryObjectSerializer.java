package miro.validator.export.json;

import java.lang.reflect.Type;

import miro.validator.types.CRLObject;
import miro.validator.types.CertificateObject;
import miro.validator.types.ManifestObject;
import miro.validator.types.RepositoryObject;
import miro.validator.types.RoaObject;

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
