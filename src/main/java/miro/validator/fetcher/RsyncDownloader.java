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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ripe.rpki.commons.rsync.Rsync;

public class RsyncDownloader {
	
	public static final Logger log = Logger.getGlobal();
	
	
	/* Fetches the data at dataLocation, saves it in baseDirectory/relativePath, returns
	 * path to fetched data*/
	public String fetchDataKeepPath(URI dataLocation, String baseDirectory, boolean isFile) {
		String sourcePath = dataLocation.toString();
		String relativePath;
		if(isFile){
			relativePath = URIToFile(dataLocation).getParent();
		} else{
			relativePath = URIToFile(dataLocation).toString();
			//Make sure source has trailing slash, else rsync creates a new dir in destination
			if(!sourcePath.endsWith("/")){
				sourcePath += "/";
			}
		}
		
		File destinationFile = joinToFile(baseDirectory, relativePath);
		String destination = destinationFile.toString();
		
		try {
			Files.createDirectories(destinationFile.toPath());
		} catch (IOException e) {
			log.log(Level.SEVERE, e.toString(), e);
			return null;
		}
		int rtval = downloadData(sourcePath, destination);
		
		if(rtval != 0){
			log.log(Level.SEVERE, "Could not fetch {0}, rsync error code: {1}", new Object[]{sourcePath, rtval});
		}
		
		/* Add the filename to the destination directory path */
		if(isFile) {
			destination += "/";
			destination += new File(sourcePath).getName();
		}
		
		return destination;
	}
	
	public void createDirectories(String dest) {
		File destinationFile = new File(dest);
		try {
			Files.createDirectories(destinationFile.getParentFile().toPath());
		} catch (Exception e){ 
			log.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	public String fetchObject(URI objectLocation, String baseDirectory) {
		String localPath = getRelativePath(objectLocation, baseDirectory);
		if(downloadData(objectLocation.toString(),localPath) == 0)
			return localPath;
		return null;
		
	}
	
	
	public int downloadData(URI source, String destination){
		return downloadData(source.toString(), destination);
	}
	public int downloadData(String source, String destination) {
		
		createDirectories(destination);

		Rsync rsync = new Rsync(source, destination);
		rsync.addOptions("-a", "-v");
		log.log(Level.INFO, "Fetching {0}", source);
		int rtval = rsync.execute();
		log.log(Level.FINE,
				"Rsync: Source {0} , Dest. {1}, Duration {2}",
				new Object[] { rsync.getSource(), rsync.getDestination(),
						rsync.elapsedTime() });
		return rtval;
	}
	
	/* Input : rsync://wat.wot.lel/test/, /var/data/test
	 * Output: /var/data/test/wat.wot.lel/test/
	 * 
	 * Input: rsync://wat.wot.lel/test/wut.file , /var/data/test
	 * Output: /var/data/test/wat.wot.lel/test/wut.file
	 * 
	 * */
	public static String getRelativePath(URI source, String baseDir){
		
		File baseFile = new File(baseDir);
		File resultFile = new File(baseFile, URIToFile(source).toString());
		
		return resultFile.toString();
	}
	
	
	
	
	public static File URIToFile(URI location) {
		String host = location.getHost();
		host = host == null ? "" : host;
		return joinToFile(host, location.getPath());
	}
	
	public static File joinToFile(String path1, String path2) {
		File file1 = new File(path1);
		File result = new File(file1, path2);
		return result;
	}
	

}
