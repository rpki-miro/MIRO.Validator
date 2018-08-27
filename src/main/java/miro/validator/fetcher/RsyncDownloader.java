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
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ripe.rpki.commons.rsync.Rsync;

public class RsyncDownloader {

	public static final Logger log = Logger.getGlobal();

	public DownloadResult downloadData(String source, String destination) {
		if(source.startsWith("http"))
			return new DownloadResult(source);
		createDirectories(destination);
		Rsync rsync = new Rsync(source, destination);
		rsync.addOptions("-a", "-v");
		log.log(Level.INFO, "Downloading {0}", source);
		rsync.execute();
		log.log(Level.FINE,
				"Rsync: Source {0} , Dest. {1}, Duration {2}",
				new Object[] { rsync.getSource(), rsync.getDestination(),
						rsync.elapsedTime() });
		return new DownloadResult(rsync);
	}

	public static void createDirectories(String dest) {
		File destinationFile = new File(dest);
		try {
			Files.createDirectories(destinationFile.getParentFile().toPath());
		} catch (Exception e){ 
			log.log(Level.SEVERE, e.toString(), e);
		}
	}
}
