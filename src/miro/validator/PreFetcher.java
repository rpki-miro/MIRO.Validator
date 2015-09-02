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
package miro.validator;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import miro.validator.fetcher.RsyncDownloader;

public class PreFetcher {

	private RsyncDownloader downloader;
	
	private List<URI> prefetchURIs;
	
	private File prefetchURIstorage;

	
	public PreFetcher(String filepath) {
		// Read the file and store the URIs here. At the end of fetching, overwrite the file with the current URIs
	}
	
	public PreFetcher() {
		prefetchURIs = new ArrayList<URI>();
	}

	public void preFetch() {
		for(URI uri : prefetchURIs){
			
		}
	}
	
	public void readPrefetchURIsFromFile() {
		//Reads the prefetchURIs from prefetchURIstorage and adds them to prefetchURIs
		//need to avoid null pointer in case file wasn't passed
	}
	
	public void writePrefetchURIsToFile() {
		//write URIs in prefetchURIs to prefetchURIstorage
		//need to avoid null pointer in case file wasn't passed
	}
	
	public boolean removeURI(URI pfUri) {
		//Try to remove the URI from the prefetchURIs list, if its not there then return false, else true.
		return false;
	}
	
	public boolean addURI(URI pfUri) {
		boolean alreadyContained = false;
		String buffer;
		String pfStr = pfUri.toString();
		for(URI uri : prefetchURIs) {
			buffer = uri.toString();
			if(pfStr.startsWith(buffer)){
				alreadyContained = true;
			}
		}
		
		if(!alreadyContained) {
			prefetchURIs.add(pfUri);
		}
		return !alreadyContained;
	}
}
