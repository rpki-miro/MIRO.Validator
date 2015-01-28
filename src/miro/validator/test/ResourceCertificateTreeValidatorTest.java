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
package miro.validator.test;

import java.io.File;
import java.net.URI;

import miro.validator.ResourceCertificateTreeValidator;
import miro.validator.fetcher.RsyncDownloader;

public class ResourceCertificateTreeValidatorTest {

	public static void main(String[] args) {
//		getTaURITest("/var/data/MIRO/rsync_test/TALs");
//		fetchTAsTest("/var/data/MIRO/rsync_test/TALs");
		
	}

//	private static void fetchTAsTest(String string) {
//		ResourceCertificateTreeValidator rctv = new ResourceCertificateTreeValidator("");
//		rctv.fetchTAs(string); 
//	}
//
//	private static void getTaURITest(String string) {
//		ResourceCertificateTreeValidator rctv = new ResourceCertificateTreeValidator("");
//		RsyncDownloader fetcher = new RsyncDownloader();
//		
//		File TALDir = new File(string);
//		String[] talPaths = TALDir.list();
//		
//		URI talURI;
//		for(String talPath : talPaths) {
//			File talFile = new File(TALDir, talPath);
//			talURI = rctv.getTaURI(talFile.toString());
//			fetcher.downloadData(talURI.toString(),"/var/data/MIRO/rsync_test/TAs");
//		}
//		
//	}
	
}
