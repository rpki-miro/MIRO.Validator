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
package miro.validator;


import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import miro.validator.export.ExportType;
import miro.validator.logging.RepositoryLogFormatter;
import miro.validator.stats.ResultExtractor;
import miro.validator.stats.types.RPKIRepositoryStats;
import miro.validator.types.ResourceCertificateTree;

public class ValidatorMain {
	
	public static String BASE_DIR;
	
	public static String TA_LOCATION;
	
	public static String REPO_NAME;
	
	public static ExportType EXPORT_TYPE;
	
	public static String EXPORT_FILE;
	
	public static boolean EXPORT;
	
	public static boolean STATS;
	
	public static String STATS_DIR;
	
	public static String TIMESTAMP;
	
	public static final Logger log = Logger.getGlobal();
	
	public static void main(String[] args){
		
		checkArguments(args);
		
		readArguments(args);
		
		ResourceCertificateTreeValidator treeValidator = new ResourceCertificateTreeValidator(BASE_DIR);
		treeValidator.readAndValidate(TA_LOCATION, REPO_NAME, TIMESTAMP);
		if(EXPORT){
			treeValidator.exportResourceCertificateTree(EXPORT_TYPE, EXPORT_FILE);
		}
		
		if(STATS){
			ResultExtractor extractor = new ResultExtractor(treeValidator.getTree());
			extractor.count();
			RPKIRepositoryStats stats = extractor.getRPKIRepositoryStats();
			ResultExtractor.archiveStats(stats, STATS_DIR);
		}
	}


	private static void checkArguments(String[] args) {
		if(args.length < 2){
			log.log(Level.SEVERE, "Error: Not enough arguments. Exiting");
			printUsage();
			System.exit(0);
		}
		
		if(!new File(args[0]).isFile()){
			log.log(Level.SEVERE,"Error: {0} is not a file. Exiting", args[0]);
			printUsage();
			System.exit(0);
		}
		
		if(!new File(args[1]).isDirectory()){
			log.log(Level.SEVERE,"Error: {0} is not a directory. Exiting",args[1]);
			printUsage();
			System.exit(0);
		}
		
	}

	public static void printUsage(){
		System.out.println("Usage: java -jar repository_processor.jar trust_anchor.cer /path/to/toplevel/repository\n");
		System.out.println("Optional parameters:\n");
		System.out.println("    -e <format> <file>    Exports repository in format to given file");
		System.out.println("                          format options: 'json'\n");
		System.out.println("    -l <file>             Log to file\n");
		System.out.println("    -v                    Be verbose");
		System.out.println("    -n <name>             Set name for repository\n");
		System.out.println("    -s <directory>        Export stats about repository to directory\n");
		System.out.println("    -d <date>             Sets <date> as last update time for repository\n");
	}
	
	public static void readArguments(String[] args){
		
		TA_LOCATION = args[0];
		String baseDir = args[1] + "/";
		BASE_DIR = baseDir;
		
		for(int i = 2;i<args.length;i++){
			
			if(args[i].equals("-d")){
				TIMESTAMP = args[i+1];
			}
			
			if(args[i].equals("-n")){
				REPO_NAME = args[i+1];
			}
			
			if(args[i].equals("-s")){
				STATS = true;
				STATS_DIR = args[i+1];
			}
			
			if(args[i].equals("-l")){
				
				if(!(i+1 < args.length)){
					log.log(Level.SEVERE, "Error: No path given for -l parameter. Exiting");
					printUsage();
					System.exit(0);
				}
				
				File lf = new File(args[i+1]);
				try {
					
					if(lf.isFile()){
						lf.delete();
						lf.createNewFile();
					}
					FileHandler fh = new FileHandler(args[i+1]);
					fh.setLevel(Level.FINEST);
					fh.setFormatter(new RepositoryLogFormatter());
					log.addHandler(fh);
					
				} catch (IOException e) {
					log.log(Level.SEVERE, "Error: Could not open logging file " + args[i+1]);
				}
				
			}

			if(args[i].equals("-v")){
				log.setLevel(Level.FINER);
			}
			
			if(args[i].equals("-e")){
				EXPORT = true;
				
				if(!(i+2 < args.length)){
					log.log(Level.SEVERE, "Error: Not enough arguments given for -e parameter. Exiting");
					printUsage();
					System.exit(0);
				}
				
				if(args[i+1].equals("json")){
					EXPORT_TYPE = ExportType.JSON;
				}
				
				EXPORT_FILE = args[i+2];
				
				if(EXPORT_TYPE == null){
					log.log(Level.SEVERE, "Error: "+args[i+1]+ " is not a valid export type. Exiting");
					printUsage();
					System.exit(0);
				}
			}
		}
	}
}
