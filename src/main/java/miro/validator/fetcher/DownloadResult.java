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
