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
package test.java.miro.validator.fetcher;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import main.java.miro.validator.fetcher.RsyncFetcher;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import test.java.miro.validator.Utilities;

public class RsyncFetcherTest {

	@Test
	public void testAddURI() {
		Utilities.cleanFile("src/test/resources/fetcher/prefetching/prefetchURIs");
		RsyncFetcher fetcher = null;
		try {
			fetcher = new RsyncFetcher("src/test/resources/fetcher/repository/", "src/test/resources/fetcher/prefetching/prefetchURIs");
		} catch (IOException e) {
			e.printStackTrace();
		}
		URI u1 = URI.create("rsync://rpki.example.org/");
		URI u2 = URI.create("rsync://rpki.example2.org/");
		URI u3 = URI.create("rsync://rpki.example.org/abc");
		
		fetcher.addPrefetchURI(u1);
		fetcher.addPrefetchURI(u2);
		fetcher.addPrefetchURI(u3);
		
		List<URI> prefetchedURIs = fetcher.getPrefetchURIs();
		assertTrue(prefetchedURIs.contains(u1));
		assertTrue(prefetchedURIs.contains(u2));
		assertFalse(prefetchedURIs.contains(u3));
		
		
		try {
			fetcher = new RsyncFetcher("src/test/resources/fetcher/repository/", "src/test/resources/fetcher/prefetching/prefetchURIs");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		u1 = URI.create("rsync://rpki.example.com");
		u2 = URI.create("rsync://rpki.example.org/");
		
		fetcher.addPrefetchURI(u1);
		fetcher.addPrefetchURI(u2);
		
		prefetchedURIs = fetcher.getPrefetchURIs();
		assertTrue(prefetchedURIs.contains(u1));
		assertTrue(prefetchedURIs.contains(u2));
		

		try {
			fetcher = new RsyncFetcher("src/test/resources/fetcher/repository/", "src/test/resources/fetcher/prefetching/prefetchURIs");
		} catch (IOException e) {
			e.printStackTrace();
		}
		u1 = URI.create("rsync://rpki.example.com/abc/def");
		u2 = URI.create("rsync://rpki.example.org/abc/def");
		u3 = URI.create("rsync://rpki.example.com/abc");
		URI u4 = URI.create("rsync://rpki.example.org/abc");
		
		fetcher.addPrefetchURI(u1);
		fetcher.addPrefetchURI(u2);
		fetcher.addPrefetchURI(u3);
		fetcher.addPrefetchURI(u4);
		
		prefetchedURIs = fetcher.getPrefetchURIs();
		assertFalse(prefetchedURIs.contains(u1));
		assertFalse(prefetchedURIs.contains(u2));
		assertTrue(prefetchedURIs.contains(u3));
		assertTrue(prefetchedURIs.contains(u4));
	}
	
	@Test
	public void testRemoveURI() {
		Utilities.cleanFile("src/test/resources/fetcher/prefetching/prefetchURIs");
		RsyncFetcher fetcher = null;
		try {
			fetcher = new RsyncFetcher("src/test/resources/fetcher/repository/", "src/test/resources/fetcher/prefetching/prefetchURIs");
		} catch (IOException e) {
			e.printStackTrace();
		}
		URI u1 = URI.create("rsync://rpki.example.org");
		URI u2 = URI.create("rsync://rpki.example2.org");
		
		fetcher.addPrefetchURI(u1);
		fetcher.addPrefetchURI(u2);
		
		List<URI> prefetchedURIs = fetcher.getPrefetchURIs();
		assertTrue(prefetchedURIs.contains(u1));
		assertTrue(prefetchedURIs.contains(u2));
		
		fetcher.removeURI(URI.create("rsync://rpki.example.org"));
		fetcher.removeURI(URI.create("rsync://rpki.example2.org"));
		assertFalse(prefetchedURIs.contains(u1));
		assertFalse(prefetchedURIs.contains(u2));
	}
	
	@Test
	public void testReadWritePrefetchURIs() {
		Utilities.cleanFile("src/test/resources/fetcher/prefetching/prefetchURIs");
		RsyncFetcher fetcher = null;
		try {
			fetcher = new RsyncFetcher("src/test/resources/fetcher/repository/", "src/test/resources/fetcher/prefetching/prefetchURIs");
		} catch (IOException e) {
			e.printStackTrace();
		}

		URI u1 = URI.create("rsync://rpki.example.org");
		URI u2 = URI.create("rsync://rpki.example.com");
		fetcher.addPrefetchURI(u1);
		fetcher.addPrefetchURI(u2);
		fetcher.writePrefetchURIsToFile();

		try {
			fetcher = new RsyncFetcher("src/test/resources/fetcher/repository/", "src/test/resources/fetcher/prefetching/prefetchURIs");
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(fetcher.getPrefetchURIs().contains(URI.create("rsync://rpki.example.com")));
		assertTrue(fetcher.getPrefetchURIs().contains(URI.create("rsync://rpki.example.org")));
	}
	
	

}
