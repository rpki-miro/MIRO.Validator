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
package main.java.miro.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import main.java.miro.validator.fetcher.RsyncDownloader;

public class PreFetcher {

	private RsyncDownloader downloader;
	
	private List<URI> prefetchURIs = new ArrayList<URI>();
	
	private File prefetchURIstorage;
	
	private boolean writeToFile = false;

	
	public PreFetcher(String filepath) {
		prefetchURIstorage = new File(filepath);
		readPrefetchURIsFromFile();
		writeToFile = true;
	}
	
	public PreFetcher() {
		
	}

	public void preFetch() {
		String destinationPath;
		for(URI uri : prefetchURIs){
			destinationPath = ResourceCertificateTreeValidator.toPath(uri);
			downloader.downloadData(uri, destinationPath);
		}
	}
	
	private void readPrefetchURIsFromFile() {
		if(!prefetchURIstorage.canRead())
			throw new RuntimeException("Cannot read " + prefetchURIstorage.getName());
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(prefetchURIstorage));
			String line;
			URI uri;
			while((line = br.readLine()) != null){
				uri = URI.create(line);
				addURI(uri);
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		} 
	}
	
	public void writePrefetchURIsToFile() {
		if(!writeToFile)
			return;
		
		try {
			FileWriter writer = new FileWriter(prefetchURIstorage, false);
			for(URI uri : prefetchURIs){
				writer.write(uri.toString());
				writer.write('\n');
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public boolean removeURI(URI pfUri) {
		List<URI> toBeRemoved = new ArrayList<URI>();
		for(URI uri : prefetchURIs) {
			if(uri.equals(pfUri))
				toBeRemoved.add(uri);
		}
		prefetchURIs.removeAll(toBeRemoved);
		return !toBeRemoved.isEmpty();
	}
	
	public boolean addURI(URI pfUri) {
		List<URI> toBeRemoved = new ArrayList<URI>();
		boolean alreadyContained = false;
		for(URI uri : prefetchURIs) {
			if(uri.getHost().equals(pfUri.getHost())){
				
				if(pfUri.getPath().startsWith(uri.getPath()))
					alreadyContained = true;
				
				if(uri.getPath().startsWith(pfUri.getPath()))
					toBeRemoved.add(uri);
			}
		}
		if(!alreadyContained) {
			prefetchURIs.add(pfUri);
			prefetchURIs.removeAll(toBeRemoved);
		}
		return !alreadyContained;
	}

	public boolean wasPrefetched(URI uri){
		for(URI pfUri : prefetchURIs) {
			if(pfUri.getHost().equals(uri.getHost()) && uri.getPath().startsWith(pfUri.getPath()))
				return true;
		}
		return false;
	}
	
	public List<URI> getPrefetchURIs() {
		return prefetchURIs;
	}

	public boolean isWriteToFile() {
		return writeToFile;
	}
	
}
