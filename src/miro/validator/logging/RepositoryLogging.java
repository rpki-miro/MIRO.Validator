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

package miro.validator.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import miro.validator.ResourceCertificateTreeValidator;

public class RepositoryLogging {
	
	
	private static String logDir = null;
	private static FileHandler filehandler;
	private static Level lev = Level.INFO;
	
	
	/*
	 * Logging Levels:
	 * SEVERE - Only fatal errors that lead to program crashing
	 * WARNING - Caught exception, but program can continue (for example missing file)
	 * INFO - Progress messages
	 * FINE - Logs exception stack traces. Active with -d or -v
	 * FINER - Logs successful reading of repository objects. Active with -v
	 */
	
	public static void logTime(long start, long end, String desc) {
		double elapsed = (double)(end - start)/1000000000;
		ResourceCertificateTreeValidator.log.log(Level.INFO,desc + " done in " + elapsed + " seconds");
	}
	
	public static Logger getLogger(Class clazz) {
		Logger log = Logger.getLogger( clazz.getName() );
		
		log.setUseParentHandlers(false);
		log.setLevel(lev);
		removeHandlers(log);
		log.addHandler(getConsoleHandler(lev));
		if(logDir != null){
			log.addHandler(getFileHandler(lev, logDir));
		}
		return log;
	}
	
	private static void removeHandlers(Logger log){
		for(Handler h : log.getHandlers()){
			log.removeHandler(h);
		}
	}
	
	
	private static ConsoleHandler getConsoleHandler(Level lev){
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(lev);
		return ch;
	}
	
	private static FileHandler getFileHandler(Level lev, String filepath) {
		
		if(filehandler == null){
			try {
				filehandler = new FileHandler(filepath);
				filehandler.setLevel(lev);
				filehandler.setFormatter(new RepositoryLogFormatter());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return filehandler;
	}




	public static void setLogFile(String string) {
		logDir = string;
		
	}
	
	
}
