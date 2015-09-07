package main.java.miro.validator.fetcher;


import java.io.File;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ripe.rpki.commons.rsync.Rsync;

public class RsyncDownloader {

	public static final Logger log = Logger.getGlobal();

	public DownloadResult downloadData(String source, String destination) {
		createDirectories(destination);
		Rsync rsync = new Rsync(source, destination);
		rsync.addOptions("-a", "-v");
		rsync.execute();
		log.log(Level.INFO, "Downloading {0}", source);
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
