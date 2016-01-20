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
package test.java.miro.validator.export;

import static org.junit.Assert.*;

import java.io.IOException;

import main.java.miro.validator.ResourceCertificateTreeValidator;
import main.java.miro.validator.export.json.JsonExporter;
import main.java.miro.validator.fetcher.ObjectFetcher;
import main.java.miro.validator.fetcher.RsyncFetcher;
import main.java.miro.validator.types.ResourceCertificateTree;
import main.java.miro.validator.types.TrustAnchorLocator;

import org.junit.Test;

public class JsonExporterTest {

	@Test
	public void exportResourceCertificateTreeTest() {
		try {
			ObjectFetcher fetcher = new RsyncFetcher("src/test/resources/fetcher/repository/",
					"src/test/resources/fetcher/prefetching/AFRINIC_prefetchURIs");
			ResourceCertificateTreeValidator validator = new ResourceCertificateTreeValidator(
					fetcher);

			TrustAnchorLocator tal = new TrustAnchorLocator("src/test/resources/tals/AFRINIC.tal");
			ResourceCertificateTree tree = validator.withTAL(tal);
			assertNotNull(tree.getTrustAnchor());
			assertTrue(tree.getName().equals("AFRINIC"));
			JsonExporter exporter = new JsonExporter("src/test/resources/export/AFRINIC.json");
			exporter.export(tree);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
