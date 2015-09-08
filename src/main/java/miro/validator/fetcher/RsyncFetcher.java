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
package main.java.miro.validator.fetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

public class RsyncFetcher implements ObjectFetcher{
	
	public static final Logger log = Logger.getGlobal();
	
	//TODO find better global solution for conf files
	private static String PREFETCH_CONF_FILE;
	
	private String baseDirectory;

	private List<URI> prefetchURIs;
	
	private List<URI> downloadedURIs;
	
	private File prefetchURIstorage;
	
	public RsyncFetcher(String baseDir, String prefetchFilepath) {
		baseDirectory = baseDir;
		clearDirectory();
		prefetchURIs = new ArrayList<URI>();
		downloadedURIs = new ArrayList<URI>();
		PREFETCH_CONF_FILE = prefetchFilepath;
		prefetchURIstorage = new File(PREFETCH_CONF_FILE);
		readPrefetchURIsFromFile();
	}
	
	public DownloadResult fetchObject(URI uri) {
		addPrefetchURI(uri);
		return fetchObjectWithoutAdd(uri);
	}
	
	public DownloadResult fetchObjectWithoutAdd(URI uri) {
		String destination = getRelativePath(uri);
		if(!alreadyDownloaded(uri)){
			DownloadResult dlResult = new RsyncDownloader().downloadData(uri.toString(), destination);
			if(dlResult.wasSuccessful())
				downloadedURIs.add(uri);
			return dlResult;
		} else {
			log.log(Level.FINE, "Skipped download of " + uri.toString());
			return new DownloadResult(uri.toString(), destination);
		}
	}

	public String getRelativePath(URI uri){
		return baseDirectory + uri.getHost() + uri.getPath();
	}

	public void prePopulate() {
		preFetch();
	}

	public void postPopulate() {
		writePrefetchURIsToFile();
	}

	public void preFetch() {
		log.log(Level.INFO, "Prefetching..");
		for(URI uri : prefetchURIs){
			fetchObjectWithoutAdd(uri);
		}
		log.log(Level.INFO, "Done with Prefetching");
	}
	
	private void readPrefetchURIsFromFile() {
		try {
			prefetchURIstorage.createNewFile();
			if(!prefetchURIstorage.canRead())
				throw new RuntimeException("Cannot read " + prefetchURIstorage.getName());
			BufferedReader br = new BufferedReader(new FileReader(prefetchURIstorage));
			String line;
			URI uri;
			while((line = br.readLine()) != null){
				if(!line.startsWith("rsync://"))
					continue;
				uri = URI.create(line);
				addPrefetchURI(uri);
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		} 
	}
	
	public void writePrefetchURIsToFile() {
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
	
	public boolean addPrefetchURI(URI pfUri) {
		if(pointsToFile(pfUri))
			return false;
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
	
	private boolean pointsToFile(URI uri) {
		for(String suffix : new String[]{".cer", ".mft", ".roa", ".crl", ".gbr"}){
			if(uri.toString().endsWith(suffix))
				return true;
		}
		return false;
	}
	
	private void clearDirectory(){
		try {
			FileUtils.cleanDirectory(new File(baseDirectory));
		} catch (Exception e) {
			//TODO maybe runtime exception?
			e.printStackTrace();
		}
	}
	
	//TODO inefficient. whole class datastructure is inefficient. needs a longest char match alg
	public boolean alreadyDownloaded(URI uri) {
		String dlUriStr;
		String uriStr = uri.toString();
		for(URI dluri : downloadedURIs) {
			dlUriStr = dluri.toString();
			if(uriStr.startsWith(dlUriStr))
				return true;
		}
		return false;
	}

	public List<URI> getPrefetchURIs() {
		return prefetchURIs;
	}
}
