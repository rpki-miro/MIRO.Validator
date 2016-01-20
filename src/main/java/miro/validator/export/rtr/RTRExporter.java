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
package main.java.miro.validator.export.rtr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import net.ripe.ipresource.IpResource;
import net.ripe.rpki.commons.crypto.cms.roa.RoaPrefix;
import main.java.miro.validator.export.IRepositoryExporter;
import main.java.miro.validator.types.CertificateObject;
import main.java.miro.validator.types.ResourceCertificateTree;
import main.java.miro.validator.types.ResourceHoldingObject;
import main.java.miro.validator.types.RoaObject;

public class RTRExporter implements IRepositoryExporter {
	
	private static final Logger logger = Logger.getLogger(RTRExporter.class.getName());

	public final String EXPORT_FILE;
	
	public RTRExporter(String expFile) {
		EXPORT_FILE = expFile;
	}

	@Override
	public void export(ResourceCertificateTree tree) {
		List<RoaObject> roas = getAllRoas(tree);
		exportROAs(roas);
	}
	
	public List<RoaObject> getAllRoas(ResourceCertificateTree tree) {
		Queue<CertificateObject> workingQueue = new LinkedList<CertificateObject>();
		List<RoaObject> roas = new ArrayList<RoaObject>();
		workingQueue.add(tree.getTrustAnchor());
		while (!workingQueue.isEmpty()) {
			for (ResourceHoldingObject obj : workingQueue.remove().getChildren()) {
				if (obj instanceof RoaObject)
					roas.add((RoaObject) obj);
				else if (obj instanceof CertificateObject)
					workingQueue.add((CertificateObject) obj);
			}
		}
		return roas;
	}

	public void exportROAs(List<RoaObject> roas) {
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(EXPORT_FILE, true)))){
			for(RoaObject roa : roas){
				for(RoaPrefix prefix : roa.getRoa().getPrefixes()){
					out.println(roa.getAsn().getValue()+ " " + prefix.getPrefix().toString() + " " + prefix.getMaximumLength());
				}
			}
		} catch(IOException e){
			
		}

	}
}
