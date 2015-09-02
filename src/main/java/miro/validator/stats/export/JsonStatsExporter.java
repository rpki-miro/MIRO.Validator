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
package main.java.miro.validator.stats.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.miro.validator.stats.ResultExtractor;
import main.java.miro.validator.stats.types.RPKIRepositoryStats;
import main.java.miro.validator.stats.types.Result;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonStatsExporter {
	
	public static final Logger log = Logger.getGlobal();
	
	public void exportRPKIRepositoryStats(RPKIRepositoryStats r, String outputDir) {
		GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
		builder.registerTypeAdapter(Result.class, new ResultSerializer());
		builder.registerTypeAdapter(ResultExtractor.class, new RPKIRepositoryStatsSerializer());
		
		Gson gson = builder.create();
		
		String filename = r.getFilename();
		File outputDirFile = new File(outputDir);
		File statsFile = outputDirFile.toPath().resolve(filename).toFile();
		
		
		try {
			FileWriter fw = new FileWriter(statsFile);
			gson.toJson(r,fw);
			fw.flush();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error: Exporting to file "+statsFile.getName()+" failed");
			log.log(Level.SEVERE, e.toString(),e);
		}
	}

}
