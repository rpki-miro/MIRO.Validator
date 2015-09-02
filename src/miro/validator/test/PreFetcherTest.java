package miro.validator.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.List;

import miro.validator.PreFetcher;

import org.junit.Test;

public class PreFetcherTest {

	@Test
	public void testAddURI() {
		PreFetcher preFetcher = new PreFetcher();
		URI u1 = URI.create("rsync://rpki.example.org/");
		URI u2 = URI.create("rsync://rpki.example2.org/");
		URI u3 = URI.create("rsync://rpki.example.org/abc");
		
		preFetcher.addURI(u1);
		preFetcher.addURI(u2);
		preFetcher.addURI(u3);
		
		List<URI> prefetchedURIs = preFetcher.getPrefetchURIs();
		assertTrue(prefetchedURIs.contains(u1));
		assertTrue(prefetchedURIs.contains(u2));
		assertFalse(prefetchedURIs.contains(u3));
		
		
		preFetcher = new PreFetcher();
		u1 = URI.create("rsync://rpki.example.com");
		u2 = URI.create("rsync://rpki.example.org/");
		
		preFetcher.addURI(u1);
		preFetcher.addURI(u2);
		
		prefetchedURIs = preFetcher.getPrefetchURIs();
		assertTrue(prefetchedURIs.contains(u1));
		assertTrue(prefetchedURIs.contains(u2));
		

		preFetcher = new PreFetcher();
		u1 = URI.create("rsync://rpki.example.com/abc/def");
		u2 = URI.create("rsync://rpki.example.org/abc/def");
		u3 = URI.create("rsync://rpki.example.com/abc");
		URI u4 = URI.create("rsync://rpki.example.org/abc");
		
		preFetcher.addURI(u1);
		preFetcher.addURI(u2);
		preFetcher.addURI(u3);
		preFetcher.addURI(u4);
		
		prefetchedURIs = preFetcher.getPrefetchURIs();
		assertFalse(prefetchedURIs.contains(u1));
		assertFalse(prefetchedURIs.contains(u2));
		assertTrue(prefetchedURIs.contains(u3));
		assertTrue(prefetchedURIs.contains(u4));
	}
	
	@Test
	public void testRemoveURI() {
		PreFetcher preFetcher = new PreFetcher();
		URI u1 = URI.create("rsync://rpki.example.org");
		URI u2 = URI.create("rsync://rpki.example2.org");
		
		preFetcher.addURI(u1);
		preFetcher.addURI(u2);
		
		List<URI> prefetchedURIs = preFetcher.getPrefetchURIs();
		assertTrue(prefetchedURIs.contains(u1));
		assertTrue(prefetchedURIs.contains(u2));
		
		preFetcher.removeURI(URI.create("rsync://rpki.example.org"));
		preFetcher.removeURI(URI.create("rsync://rpki.example2.org"));
		assertFalse(prefetchedURIs.contains(u1));
		assertFalse(prefetchedURIs.contains(u2));
	}

}
