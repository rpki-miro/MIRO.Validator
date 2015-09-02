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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.ripe.rpki.commons.rsync.Rsync;

public class RsyncDownloaderTest {
	public static void main(String[] args) {

		RsyncDownloaderTest tester = new RsyncDownloaderTest();
		tester.fetchFileTest("/var/data/MIRO/rsync_test/source", "/var/data/MIRO/rsync_test/destination");
		tester.fetchDirectoryTest("/var/data/MIRO/rsync_test/source","/var/data/MIRO/rsync_test/destination");
		
	}
	
	private void fetchDirectoryTest(String sourceDir, String destDir) {
		RsyncDownloader fetcher = new RsyncDownloader();
		
		File sourceDirFile = new File(sourceDir);
		sourceDirFile.mkdir();
		
		File destDirFile = new File(destDir);
		destDirFile.mkdir();
		
		List<DummyDir> dummyDirs = new ArrayList<DummyDir>();
		
		//create some dummy directories with dummy files
		String[] words = new String[] {"asdkj","asdo","waiod","asdok"};
		for(int i = 0; i< 70;i++) {
			File dummyDirFile = createRandomDirFile(sourceDir, words);
			dummyDirFile.mkdirs();
			
			DummyDir dummyDir = new DummyDir(dummyDirFile.toString());
			dummyDirs.add(dummyDir);
			
			for(int j = 0; j< 40;j++){
				dummyDir.addFile(j);
			}
		}
		
		//Fetch them, then see if they are where they should be and no errors occured
		URI sourceURI;
		for(DummyDir dd : dummyDirs) {
			try {
				sourceURI = new URI(dd.path);
				File file = new File(destDirFile, dd.path);
				assert( file.toString().equals(fetcher.fetchDataKeepPath(sourceURI, destDir, false)));
				assert(file.isDirectory());
				
				for(String filepath : dd.dummyFiles) {
					file = new File(destDirFile, filepath);
					assert(file.isFile());
				}
				
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		deleteDir(destDirFile);
		deleteDir(sourceDirFile);
	}
	
	private void fetchFileTest(String sourceDir, String destDir) {
		RsyncDownloader fetcher = new RsyncDownloader();
		
		File sourceDirFile = new File(sourceDir);
		sourceDirFile.mkdir();
		
		File destDirFile = new File(destDir);
		destDirFile.mkdir();
		
		List<String> filePaths = new ArrayList<String>();
		
		
		//Create random files in random dirs
		Random r = new Random();
		String[] words = new String[] {"asdkj","asdo","waiod","asdok"};
		File file;
		for(int i =0; i<20 ; i++){
			
			file = createRandomFile(sourceDir, words);
			try {
				if(file.createNewFile()){
					filePaths.add(file.getPath());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Fetch them
		for(String filepath : filePaths) {
			try {
				URI uri = new URI(filepath);
				File f = new File(destDirFile,filepath);
				assert(f.toString().equals(fetcher.fetchDataKeepPath(uri, destDir, true)));
				assert(f.isFile());
				
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		
		deleteDir(destDirFile);
		deleteDir(sourceDirFile);
	}
	
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	
	private File createRandomFile(String base, String[] words) {
		File f = createRandomDirFile(base, words);
		Random r = new Random();
		
		String filename = "";
		for(int i = 0 ; i< 7;i++){
			filename += words[r.nextInt(words.length)];
		}

		return new File(f,filename+".file");
	}
	
	private File createRandomDirFile(String base, String[] words) {
		
		Random r = new Random();
		int length = r.nextInt(8);
		
		File result = new File(base);
		
		for(int i = 0; i< length;i++){
			result = new File(result, words[r.nextInt(words.length)]);
		}
		result.mkdirs();
		return result;
	}
	
	private class DummyDir {
		
		String path;
		List<String> dummyFiles;
		
		public DummyDir(String f) {
			path = f;
			dummyFiles = new ArrayList<String>();
		}
		
		public void addFile(int i) {
			File dummyFile = new File(path, "dummyFile"+i);
			try {
				dummyFile.createNewFile();
				dummyFiles.add(dummyFile.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
