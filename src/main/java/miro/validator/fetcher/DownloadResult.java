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
