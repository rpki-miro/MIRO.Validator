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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.miro.validator.export.IRepositoryExporter;
import main.java.miro.validator.types.CRLObject;
import main.java.miro.validator.types.CertificateObject;
import main.java.miro.validator.types.ManifestObject;
import main.java.miro.validator.types.ResourceCertificateTree;
import main.java.miro.validator.types.ResourceHoldingObject;
import main.java.miro.validator.types.RoaObject;
import main.java.miro.validator.types.ValidationResults;
import net.ripe.ipresource.IpResourceSet;
import net.ripe.rpki.commons.validation.ValidationCheck;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonExporter implements IRepositoryExporter {

	private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public final String EXPORT_FILE;
	
	public JsonExporter(String export_file) {
		this.EXPORT_FILE = export_file;
	}
	
	public void export(ResourceCertificateTree tree) {
		GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
		builder.registerTypeAdapter(ResourceCertificateTree.class, new ResourceCertificateTreeSerializer());
		builder.registerTypeAdapter(CertificateObject.class, new CertificateObjectJsonSerializer());
		builder.registerTypeAdapter(IpResourceSet.class, new IpResourceSetSerializer());
		builder.registerTypeAdapter(byte[].class, new ByteArrayToHexSerializer());
		builder.registerTypeAdapter(ManifestObject.class, new ManifestSerializer());
		builder.registerTypeAdapter(RoaObject.class, new RoaSerializer());
		builder.registerTypeAdapter(CRLObject.class, new CRLSerializer());
		builder.registerTypeAdapter(ResourceHoldingObject.class, new ResourceHoldingObjectSerializer());
		builder.registerTypeAdapter(ValidationCheck.class, new ValidationCheckSerializer());
		builder.registerTypeAdapter(ValidationResults.class, new ValidationResultsSerializer());
		
		
		
		Gson gson = builder.setPrettyPrinting().create();	
		File f = new File(EXPORT_FILE);
		
		try {
			FileWriter fw = new FileWriter(f);
			gson.toJson(tree,fw);
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Error: Exporting to file "+EXPORT_FILE+" failed. Exiting");
		}
	}
}
