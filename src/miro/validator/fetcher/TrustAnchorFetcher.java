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
package miro.validator.fetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrustAnchorFetcher {
	
	public static final Logger log = Logger.getGlobal();
	
	private RsyncDownloader downloader;
	
	private String TALDirectory;
	
	public TrustAnchorFetcher(String TALDir) {
		downloader = new RsyncDownloader();
		TALDirectory = TALDir;
	}
	
	
	public URI getTrustAnchorURI(String TALpath) {
		return URI.create(getTARemoteLocation(TALpath));
	}
	
	public String getTARemoteLocation(String TALfilePath) {
		File TALFile = new File(TALfilePath);
		try {
			BufferedReader br  = new BufferedReader(new FileReader(TALFile));
			String line = br.readLine();
			br.close();
			return line;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Could not read TAL at {0}", TALfilePath);
			log.log(Level.SEVERE, e.toString(), e);
			return null;
		}
	}
	
	public String fetchTA(String TALpath, String dir) {
		File TALFile = new File(TALpath);
		String taLocation = getTARemoteLocation(TALFile.toString());
		
		File taDir = new File(dir);
		String taDestination = taDir.toString() + "/" + getFilename(taLocation);
		downloader.downloadData(taLocation.toString(), taDestination );
		
		return taDestination;
	}
	
	
	public String getFilename(String path) {
		return new File(path).getName();
	}
	
	
//	public String fetchTAs(){
//		File TALDirFile = new File(TALDirectory);
//		File TADirFile = new File(TALDirFile.getParentFile(), "/TAs");
//		
//		File[] talFiles = TALDirFile.listFiles();
//		String taUri;
//		for(File talFile : talFiles) {
//			fetchTA(talFile.toString());
//			taUri = getTARemoteLocation(talFile.toString());
////			downloader.downloadData(taUri.toString(),TADirFile.toString());
//		}
//		return TADirFile.toString();
//	}

}
