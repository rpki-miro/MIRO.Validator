package main.java.miro.validator.fetcher;

import java.net.URI;

public interface ObjectFetcher {
	
	public DownloadResult fetchObject(URI uri);
	
	public void prePopulate();
	
	public void postPopulate();
}
