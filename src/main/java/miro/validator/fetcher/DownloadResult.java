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

import net.ripe.rpki.commons.rsync.Rsync;

public class DownloadResult {
	
	private boolean success;
	
	private int rsyncRtval;
	
	private String source;
	
	private String destination;
	
	private long elapsedTime;
	
	public DownloadResult(Rsync rsync) {
		rsyncRtval = rsync.getExitStatus();
		success = rsyncRtval == 0 ? true  : false;
		source = rsync.getSource();
		destination = rsync.getDestination();
		elapsedTime = rsync.elapsedTime();
	}
	
	//TODO this is a special case DownloadResult. To be used when an object was already prefetched and rsync data wasn't saved
	public DownloadResult(String s, String d) {
		source = s;
		destination = d;
		success = true;
		elapsedTime = 0;
		rsyncRtval = 0;
	}

	//TODO better solution to bad source protocol
	public DownloadResult(String badSource) {
		source = badSource;
		destination = "";
		success = false;
		elapsedTime = 0;
		rsyncRtval = 0;
	}

	public boolean wasSuccessful() {
		return success;
	}

	public int getRsyncRtval() {
		return rsyncRtval;
	}

	public String getSource() {
		return source;
	}

	public String getDestination() {
		return destination;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

}
