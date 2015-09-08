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
package main.java.miro.validator.stats.types;

import java.util.List;

import org.joda.time.DateTime;

public class RPKIRepositoryStats {
	
	private String name;
	
	private DateTime timestamp;
	
	private String trustAnchor;
	
	private Result result;
	
	private List<Result> hostResults;
	
	public RPKIRepositoryStats(String name, DateTime timestamp, String trustAnchor, Result result, List<Result> hResults) {
		this.name = name;
		this.timestamp = timestamp;
		this.trustAnchor = trustAnchor;
		this.result = result;
		hostResults = hResults;
	}

	public String getName() {
		return name;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public String getTrustAnchor() {
		return trustAnchor;
	}

	public Result getResult() {
		return result;
	}
	
	public String toString(){
		String str = "{\n";
			str += "\tname : " + name + "\n";
			str += "\ttrustAnchor : " + trustAnchor + "\n";
			str += result.toString();
		str += "}\n";
			
		return str;
	}
	
	public String getFilename() {
		return name + "_" + timestamp;
	}

	public List<Result> getHostResults() {
		return hostResults;
	}

	public void setHostResults(List<Result> hostResults) {
		this.hostResults = hostResults;
	}
	
	public void addStats(RPKIRepositoryStats stats) {
		Result res = stats.getResult();
		result.addResult(res);
		hostResults.addAll(stats.getHostResults());
	}
	
}
