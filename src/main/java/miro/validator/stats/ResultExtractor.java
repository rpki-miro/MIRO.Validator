
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
package main.java.miro.validator.stats;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.miro.validator.stats.export.JsonStatsExporter;
import main.java.miro.validator.stats.types.RPKIRepositoryStats;
import main.java.miro.validator.stats.types.Result;
import main.java.miro.validator.types.CRLObject;
import main.java.miro.validator.types.CertificateObject;
import main.java.miro.validator.types.ManifestObject;
import main.java.miro.validator.types.RepositoryObject;
import main.java.miro.validator.types.ResourceCertificateTree;
import main.java.miro.validator.types.ResourceHoldingObject;
import main.java.miro.validator.types.RoaObject;
import main.java.miro.validator.types.ValidationResults;
import net.ripe.rpki.commons.validation.ValidationCheck;

public class ResultExtractor {
	
	private static final Logger log = Logger.getGlobal();
	
	private ResourceCertificateTree currentTree;
	
	private Result totalResult;
	
	private List<Result> hostResults;
	
	public ResultExtractor(ResourceCertificateTree tree) {
		currentTree = tree;
		totalResult = new Result("Total");
		hostResults = new ArrayList<Result>();
	}
	
	public void count() {
		CertificateObject trustAnchor = currentTree.getTrustAnchor();
		
		ArrayList<ResourceHoldingObject> currentLevel = new ArrayList<ResourceHoldingObject>();
		ArrayList<ResourceHoldingObject> nextLevel = new ArrayList<ResourceHoldingObject>();
		ArrayList<ResourceHoldingObject> buf = null;
		
		currentLevel.add(trustAnchor);
		
		RoaObject roa;
		CertificateObject cert;
		do{
			for(ResourceHoldingObject obj : currentLevel){
				countHost(obj);
				if(obj instanceof RoaObject){
					roa = (RoaObject) obj;
					if(roa != null){
						countRoa(totalResult, roa);
					}
				}
				
				if(obj instanceof CertificateObject){
					cert = (CertificateObject) obj;
					
					countCertificate(totalResult, cert);
					
					if(cert.getManifest() != null){
						countManifest(totalResult, cert.getManifest());
						countHost(cert.getManifest());
					}
					
					if(cert.getCrl() != null){
						countCrl(totalResult, cert.getCrl());
						countHost(cert.getCrl());
					}
					buf = cert.getChildren();
					nextLevel.addAll(buf);
				}
				
			}
			currentLevel = nextLevel;
			nextLevel = new ArrayList<ResourceHoldingObject>();
		} while(!currentLevel.isEmpty());
		log.log(Level.INFO,"Extracting done");
	}
	
	
	private void countHost(RepositoryObject obj) {
		URI location = obj.getRemoteLocation();
		
		if(location == null)
			return;
		
		String host = location.getHost();
		
		Result result = null;
		boolean exists = false;
		for(Result r : hostResults){
			if(r.getDescriptor().equals(host)){
				result = r;
				exists = true;
			}
		}
		
		result = result == null ? new Result(host) : result;
		if(!exists)
			hostResults.add(result);
		
		if(obj instanceof CertificateObject)
			countCertificate(result, (CertificateObject) obj);
		
		if(obj instanceof RoaObject)
			countRoa(result, (RoaObject) obj);
		
		if(obj instanceof ManifestObject)
			countManifest(result, (ManifestObject) obj);
		
		if(obj instanceof CRLObject)
			countCrl(result, (CRLObject) obj);
		
	}

	private void countCertificate(Result result, CertificateObject cert) {
		result.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
		result.incrementObjectCount(StatsKeys.TOTAL_CER_OBJECTS);
		
		ValidationResults validationResults = cert.getValidationResults();
		
		if(hasWarnings(validationResults)){
			result.incrementObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS);
			result.incrementObjectCount(StatsKeys.WARNING_CER_OBJECTS);
			countWarnings(result, validationResults.getWarnings());
		}
		if(hasErrors(validationResults)){
			result.incrementObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS);
			result.incrementObjectCount(StatsKeys.INVALID_CER_OBJECTS);
			countErrors(result, validationResults.getErrors());
		} else {
			result.incrementObjectCount(StatsKeys.TOTAL_VALID_OBJECTS);
			result.incrementObjectCount(StatsKeys.VALID_CER_OBJECTS);
		}
	}
	
	private void countRoa(Result result, RoaObject roa) {
		result.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
		result.incrementObjectCount(StatsKeys.TOTAL_ROA_OBJECTS);
		
		ValidationResults validationResults = roa.getValidationResults();
		
		if(hasWarnings(validationResults)){
			result.incrementObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS);
			result.incrementObjectCount(StatsKeys.WARNING_ROA_OBJECTS);
			countWarnings(result, validationResults.getWarnings());
		}
		
		if(hasErrors(validationResults)){
			result.incrementObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS);
			result.incrementObjectCount(StatsKeys.INVALID_ROA_OBJECTS);
			countErrors(result, validationResults.getErrors());
		} else {
			result.incrementObjectCount(StatsKeys.TOTAL_VALID_OBJECTS);
			result.incrementObjectCount(StatsKeys.VALID_ROA_OBJECTS);
		}
	}
	
	private void countManifest(Result result, ManifestObject mft) {
		result.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
		result.incrementObjectCount(StatsKeys.TOTAL_MFT_OBJECTS);
		
		ValidationResults validationResults = mft.getValidationResults();
		
		if(hasWarnings(validationResults)){
			result.incrementObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS);
			result.incrementObjectCount(StatsKeys.WARNING_MFT_OBJECTS);
			countWarnings(result, validationResults.getWarnings());
		}
		
		if(hasErrors(validationResults)){
			result.incrementObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS);
			result.incrementObjectCount(StatsKeys.INVALID_MFT_OBJECTS);
			countErrors(result, validationResults.getErrors());
		} else {
			result.incrementObjectCount(StatsKeys.TOTAL_VALID_OBJECTS);
			result.incrementObjectCount(StatsKeys.VALID_MFT_OBJECTS);
		}
	}
	
	private void countCrl(Result result, CRLObject crl) {
		
		result.incrementObjectCount(StatsKeys.TOTAL_OBJECTS);
		result.incrementObjectCount(StatsKeys.TOTAL_CRL_OBJECTS);
		
		ValidationResults validationResults = crl.getValidationResults();
		
		if(hasWarnings(validationResults)){
			result.incrementObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS);
			result.incrementObjectCount(StatsKeys.WARNING_CRL_OBJECTS);
			countWarnings(result, validationResults.getWarnings());
		}
		
		if(hasErrors(validationResults)){
			result.incrementObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS);
			result.incrementObjectCount(StatsKeys.INVALID_CRL_OBJECTS);
			countErrors(result, validationResults.getErrors());
		} else {
			result.incrementObjectCount(StatsKeys.TOTAL_VALID_OBJECTS);
			result.incrementObjectCount(StatsKeys.VALID_CRL_OBJECTS);
		}
	}
	
	private boolean hasWarnings(ValidationResults validationResults){
		if(validationResults.getWarnings().isEmpty()){
			return false;
		} else {
			return true;
		}
	}
	
	private void countWarnings(Result result, List<ValidationCheck> warningChecks){
		for(ValidationCheck check : warningChecks) {
			result.incrementWarningCount(check.getKey());
		}
	}
	
	private boolean hasErrors(ValidationResults validationResults){
		if(validationResults.getErrors().isEmpty()){
			return false;
		} else {
			return true;
		}
	}
	
	private void countErrors(Result result, List<ValidationCheck> errorChecks){
		for(ValidationCheck check : errorChecks) {
			result.incrementErrorCount(check.getKey());
		}
	}
	
	public RPKIRepositoryStats getRPKIRepositoryStats(){
		RPKIRepositoryStats stats = new RPKIRepositoryStats(currentTree.getName(), currentTree.getTimeStamp(), currentTree.getTrustAnchor().getFilename(), totalResult, hostResults);
		return stats;
	}

	public static void archiveStats(RPKIRepositoryStats stats, String outputDir) {
		
		File outputDirFile = new File(outputDir);
		outputDirFile.mkdirs();
		
		/* go to outputdir, copy files to outputdir/archive , then export stats to outputDir */
//		File archiveDir = new File(outputDir+"/archive");
//		archiveDir.mkdirs();
		
//		File[] oldFiles = outputDirFile.listFiles();
//		Path moveTo = archiveDir.toPath();
//		for(File file : oldFiles){
//			if(file.isDirectory()){
//				continue;
//			}
//			Path moveFrom = file.toPath();
//			moveTo = moveTo.resolve(file.getName());
//			try {
//				Files.move(moveFrom, moveTo, StandardCopyOption.REPLACE_EXISTING);
//			} catch (IOException e) {
//				log.log(Level.SEVERE, "Could not archive existing files in {0}. Failed to archive stats.",outputDir);
//				log.log(Level.SEVERE,e.toString(),e);
//			}
//		}
		
		/* Now write stats to outputDir */
		JsonStatsExporter statsExporter = new JsonStatsExporter();
		statsExporter.exportRPKIRepositoryStats(stats, outputDir);
		
	}

}
