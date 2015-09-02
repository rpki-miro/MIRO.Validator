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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import main.java.miro.validator.stats.StatsKeys;

public class Result {
	
	private HashMap<String,Integer> counter;
	
	private HashMap<String,Integer> warning;
	
	private HashMap<String,Integer> error;
	
	private String descriptor;
	
	
	public Result(String desc) {
		descriptor = desc;
		
		counter = new HashMap<String, Integer>();
		
		warning = new HashMap<String, Integer>();
		
		error = new HashMap<String, Integer>();
	}
	
	public void incrementObjectCount(String key){
		incrementHashMapCount(counter,key);
	}

	public void addToObjectCount(String key, int amount){
		addToHashMapCount(counter,key,amount);
	}
	public Integer getObjectCount(String key){
		return getHashMapCount(counter,key);
	}
	
	public void incrementWarningCount(String key){
		incrementHashMapCount(warning, key);
	}
	
	public void addToWarningCount(String key, int amount){
		addToHashMapCount(warning, key, amount);
	}
	
	public Integer getWarningCount(String key){
		return getHashMapCount(warning,key);
	}
	
	public Set<String> getAllWarningKeys(){
		return warning.keySet();
	}
	
	public Set<String> getAllObjectKeys() {
		return counter.keySet();
	}
	
	public void incrementErrorCount(String key){
		incrementHashMapCount(error, key);
	}
	
	public void addToErrorCount(String key, int amount){
		addToHashMapCount(error, key, amount);
	}
	
	public Integer getErrorCount(String key){
		return getHashMapCount(error, key);
	}
	
	public Set<String> getAllErrorKeys(){
		return error.keySet();
	}
	
	private void addToHashMapCount(HashMap<String,Integer> map, String key, int amount){
		if(!map.containsKey(key)){
			map.put(key, new Integer(amount));
			return;
		}
		Integer count = map.get(key);
		count += amount;
		map.put(key, count);
	}
	
	private void incrementHashMapCount(HashMap<String,Integer> map, String key){
		if(!map.containsKey(key)){
			map.put(key, new Integer(0));
		}
		
		Integer count = map.get(key);
		count++;
		map.put(key, count);
	}
	
	private Integer getHashMapCount(HashMap<String,Integer> map, String key){
		if(!map.containsKey(key)){
			return 0;
		}
		return map.get(key);
	}
	
	public String toString(){
		String str = "\tDescriptor: " + descriptor 
				+ "\n\tObject counts: {\n";
		
			str += "\t\t" + StatsKeys.TOTAL_OBJECTS + " : " + getObjectCount(StatsKeys.TOTAL_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.TOTAL_CER_OBJECTS + " : " + getObjectCount(StatsKeys.TOTAL_CER_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.TOTAL_MFT_OBJECTS + " : " + getObjectCount(StatsKeys.TOTAL_MFT_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.TOTAL_CRL_OBJECTS + " : " + getObjectCount(StatsKeys.TOTAL_CRL_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.TOTAL_ROA_OBJECTS + " : " + getObjectCount(StatsKeys.TOTAL_ROA_OBJECTS) + "\n";
			str += "\n";
			str += "\t\t" + StatsKeys.TOTAL_VALID_OBJECTS + " : " + getObjectCount(StatsKeys.TOTAL_VALID_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.VALID_CER_OBJECTS + " : " + getObjectCount(StatsKeys.VALID_CER_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.VALID_MFT_OBJECTS + " : " + getObjectCount(StatsKeys.VALID_MFT_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.VALID_CRL_OBJECTS + " : " + getObjectCount(StatsKeys.VALID_CRL_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.VALID_ROA_OBJECTS + " : " + getObjectCount(StatsKeys.VALID_ROA_OBJECTS) + "\n";
			str += "\n";
			str += "\t\t" + StatsKeys.TOTAL_INVALID_OBJECTS + " : " + getObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.INVALID_CER_OBJECTS + " : " + getObjectCount(StatsKeys.INVALID_CER_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.INVALID_MFT_OBJECTS + " : " + getObjectCount(StatsKeys.INVALID_MFT_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.INVALID_CRL_OBJECTS + " : " + getObjectCount(StatsKeys.INVALID_CRL_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.INVALID_ROA_OBJECTS + " : " + getObjectCount(StatsKeys.INVALID_ROA_OBJECTS) + "\n";
			str += "\n";
			str += "\t\t" + StatsKeys.TOTAL_WARNING_OBJECTS + " : " + getObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.WARNING_CER_OBJECTS + " : " + getObjectCount(StatsKeys.WARNING_CER_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.WARNING_MFT_OBJECTS + " : " + getObjectCount(StatsKeys.WARNING_MFT_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.WARNING_CRL_OBJECTS + " : " + getObjectCount(StatsKeys.WARNING_CRL_OBJECTS) + "\n";
			str += "\t\t" + StatsKeys.WARNING_ROA_OBJECTS + " : " + getObjectCount(StatsKeys.WARNING_ROA_OBJECTS) + "\n";

		str += "\t}\n\n";

		ArrayList<String> warningKeys = new ArrayList<String>(getAllWarningKeys());
		str += "\tWarnings: {\n";
			for(String warningKey : warningKeys){
				str += "\t\t" + warningKey + " : " + getWarningCount(warningKey) + "\n";
			}
		str += "\t}\n\n";
	
		ArrayList<String> errorKeys = new ArrayList<String>(getAllErrorKeys());
		str += "\tErrors: {\n";
			for(String errorKey : errorKeys){
				str += "\t\t" + errorKey+ " : " + getErrorCount(errorKey) + "\n";
			}
		str += "\t}\n\n";
		
		return str;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}
	
	public void addResult(Result res){
		for(String objKey : res.getAllObjectKeys()){
			addToObjectCount(objKey, res.getObjectCount(objKey));
		}
		
		for(String warningKey : res.getAllWarningKeys()){
			addToWarningCount(warningKey, res.getWarningCount(warningKey));
		}
		
		for(String errorKey : res.getAllErrorKeys()){
			addToErrorCount(errorKey, res.getErrorCount(errorKey));
		}
	}
	
	
}
