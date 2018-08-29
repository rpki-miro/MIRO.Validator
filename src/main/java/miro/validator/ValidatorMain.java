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
package main.java.miro.validator;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import inet.ipaddr.*;

import javax.swing.event.TreeSelectionEvent;

import net.ripe.ipresource.Asn;
import net.ripe.ipresource.IpAddress;
import net.ripe.ipresource.IpRange;
import net.ripe.ipresource.IpResource;
import net.ripe.ipresource.IpResourceRange;
import net.ripe.ipresource.IpResourceSet;
import net.ripe.rpki.commons.crypto.cms.roa.RoaPrefix;
import net.ripe.rpki.commons.crypto.crl.X509Crl.Entry;
import net.ripe.rpki.commons.crypto.x509cert.X509ResourceCertificate;
import net.ripe.rpki.commons.validation.ValidationResult;
import main.java.miro.validator.export.ExportType;
import main.java.miro.validator.export.json.JsonExporter;
import main.java.miro.validator.fetcher.ObjectFetcher;
import main.java.miro.validator.fetcher.RsyncFetcher;
import main.java.miro.validator.logging.RepositoryLogFormatter;
import main.java.miro.validator.stats.ResultExtractor;
import main.java.miro.validator.stats.StatsKeys;
import main.java.miro.validator.stats.types.RPKIRepositoryStats;
import main.java.miro.validator.stats.types.Result;
import main.java.miro.validator.types.CRLObject;
import main.java.miro.validator.types.CertificateObject;
import main.java.miro.validator.types.ManifestObject;
import main.java.miro.validator.types.ResourceCertificateTree;
import main.java.miro.validator.types.ResourceHoldingObject;
import main.java.miro.validator.types.RoaObject;
import main.java.miro.validator.types.TrustAnchorLocator;
import main.java.miro.validator.types.ValidationResults;
import main.java.miro.validator.util.ByteArrayPrinter;
import net.ripe.rpki.commons.validation.ValidationCheck;

public class ValidatorMain {
	
	public static Integer ID_COUNTER = 0;
	
	public static String BASE_DIR = "/var/lib/miro/repo/";
	
	public static String PREFETCH_DIR = "/etc/miro/prefetch/";
	
	public static String EXPORT_DIR = "/var/lib/miro/export/";
	
	public static String TALDirectory = "/etc/miro/tals/";
	
	public static String DB_HOST;

	public static String DB_NAME = "mirodb";
	
	public static String DB_PORT;
	
	public static String DB_USER = "miro";

	public static String DB_PWD = "rpki";
	
	public static final Logger log = Logger.getLogger(ValidatorMain.class.getName());
	
	public static void main(String[] args){
		checkArguments(args);
		readConfig(args[0]);
		downloadAndValidate();
	}
	
	public static void downloadAndValidate() {
		log.log(Level.INFO, "Starting download & validate");
		List<ResourceCertificateTree> trees = new ArrayList<ResourceCertificateTree>();
		for (File talGroupDirectory : getTALGroupDirectories()) {
			trees.addAll(downloadAndValidateTALs(talGroupDirectory));
		}
		try {
			Connection conn = null;
			String db_connection_str = "jdbc:postgresql://" + DB_HOST +":"+DB_PORT +"/" + DB_NAME;
			conn = DriverManager.getConnection(db_connection_str, DB_USER, DB_PWD);
			clearDB(conn);
			addTreesToDB(trees, conn);
			addStatsToDB(trees, conn);
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void clearDB(Connection conn) {
		Statement stmt;
		String query = "DELETE FROM %s;";
		String[] tables = {"roa", "manifest", "crl", "resource_certificate", "certificate_tree", "stats", "roa_resource_certificate"};
		for (String table : tables) {
			try {
				stmt = conn.createStatement();
				stmt.executeUpdate(String.format(query, table));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void addTreesToDB(List<ResourceCertificateTree> trees, Connection conn) {
		for (ResourceCertificateTree tree : trees) {
			addTreeToDB(conn, tree);
		}
	}

	public static void addTreeToDB(Connection conn, ResourceCertificateTree tree) {
		addCertificateTreeToDB(conn, tree);
		addCertificatesAndChildrenToDB(conn, tree.getTrustAnchor(), tree.getName(), null);
	}

	public static void addCertificateTreeToDB(Connection conn, ResourceCertificateTree tree) {
		try {
			String query = "INSERT INTO certificate_tree VALUES";
			query += "(";
			query += String.format("'%s', '%s', '%s'", tree.getName(), tree.getTimeStamp().toString(), tree.getTrustAnchor().getFilename());
			query += ");";
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void addCertificatesAndChildrenToDB(Connection conn, CertificateObject cert, String tree, Integer parent_id) {
		try {
			Integer cert_id = getNextID();
			String query = "INSERT INTO resource_certificate VALUES";
			query += "(";
			query += getCertificateObjectDBValueString(cert, tree, cert_id, parent_id);
			query += ");";
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
			
			ManifestObject mft = cert.getManifest();
			if (mft != null) {
				query = "INSERT INTO manifest VALUES";
				query += "(";
				query += getManifestObjectDBValueString(mft, tree, cert_id, cert.getFilename());
				query += ");";
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			}

			CRLObject crl = cert.getCrl();
			if (crl != null) {
				query = "INSERT INTO crl VALUES";
				query += "(";
				query += getCRLObjectDBValueString(crl, tree, cert_id, cert.getFilename());
				query += ");";
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			}
			
			for (ResourceHoldingObject child : cert.getChildren()) {
				if (child instanceof RoaObject) {
					Integer ee_id = getNextID();
					query = "INSERT INTO roa VALUES";
					query += "(";
					query += getRoaObjectDBValueString((RoaObject) child, tree, cert_id, ee_id);
					query += ")";
					stmt = conn.createStatement();
					stmt.executeUpdate(query);
					
					query = "INSERT INTO roa_resource_certificate VALUES";
					query += "(";
					query += getRoaObjectEECertDBValueString(child.getCertificate(), child.getFilename(), tree, cert_id, ee_id);
					query += ")";
					stmt = conn.createStatement();
					stmt.executeUpdate(query);
					
				} else if (child instanceof CertificateObject) {
					addCertificatesAndChildrenToDB(conn, (CertificateObject) child, tree, cert_id);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addStatsToDB(List<ResourceCertificateTree> trees, Connection conn) {
		List<RPKIRepositoryStats> stats_list = new ArrayList<RPKIRepositoryStats>();
		for (ResourceCertificateTree tree : trees) {
			RPKIRepositoryStats stats = getRPKIRepositoryStats(tree);
			stats_list.add(stats);
			addStatToDB(stats, conn);
		}
		RPKIRepositoryStats total = stats_list.remove(0);
		total.setName("All");
		total.setTrustAnchor("None");
		for (RPKIRepositoryStats buf : stats_list) {
			total.addStats(buf);
		}
		addStatToDB(total, conn);
	}
	
	public static void addStatToDB(RPKIRepositoryStats stats, Connection conn) {
		try {
			String query = "INSERT INTO stats VALUES";
			query += "(";
			query += getRPKIRepositoryStatsDBValueString(stats);
			query += ");";
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static String getRPKIRepositoryStatsDBValueString(RPKIRepositoryStats stats) {
		Result stats_result = stats.getResult();
		String result = String.format("'%s', '%s', '%s', ", stats.getName(), stats.getTrustAnchor(), stats.getTimestamp().toString());
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.TOTAL_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.TOTAL_CER_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.TOTAL_MFT_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.TOTAL_CRL_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.TOTAL_ROA_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.TOTAL_VALID_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.VALID_CER_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.VALID_MFT_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.VALID_CRL_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.VALID_ROA_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.TOTAL_INVALID_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.INVALID_CER_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.INVALID_MFT_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.INVALID_CRL_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.INVALID_ROA_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.TOTAL_WARNING_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.WARNING_CER_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.WARNING_MFT_OBJECTS));
		result += String.format("%d, ", stats_result.getObjectCount(StatsKeys.WARNING_CRL_OBJECTS));
		result += String.format("%d ", stats_result.getObjectCount(StatsKeys.WARNING_ROA_OBJECTS));
		return result;
	}
	
	public static String getCertificateObjectDBValueString(CertificateObject cert, String tree, Integer id, Integer parentId) {
		String result = String.format("'%s', '%s', %d, ", cert.getFilename(), cert.getSubject().toString(), cert.getSerialNr());
		result += String.format("'%s', ", cert.getIssuer().toString());

		byte[] SKI = cert.getSubjectKeyIdentifier();
		if (SKI == null)
			result += String.format("%s, ", "NULL");
		else
			result += String.format("'%s', ", ByteArrayPrinter.bytesToHex(SKI));
		
		byte[] AKI = cert.getAki();
		if (AKI == null)
			result += String.format("%s, ", "NULL");
		else
			result += String.format("'%s', ", ByteArrayPrinter.bytesToHex(AKI));
		
		result += String.format("'%s', ", cert.getPublicKey().toString());
		result += String.format("%b, %b, %b, ", cert.getIsEE(), cert.getIsCA(), cert.getIsRoot());
		
		result += String.format("'%s', ", cert.getValidityPeriod().getNotValidBefore().toString());
		result += String.format("'%s', ", cert.getValidityPeriod().getNotValidAfter().toString());
		result += String.format("'%s', ", cert.getValidationResults().getValidationStatus().toString());
		
		result += String.format("%s, ", getValidationCheckValueString(cert.getValidationResults().getErrors()));
		result += String.format("%s, ", getValidationCheckValueString(cert.getValidationResults().getWarnings()));
	
		result += String.format("%s, ", getPrefixesValueString(cert.getResources()));
		result += String.format("%s, ", getASNsValueString(cert.getResources()));
		result += String.format("%s, ", getASNRangesValueString(cert.getResources()));
		
		ManifestObject mft = cert.getManifest();
		result += String.format("%s, ", mft != null ? "'"+mft.getFilename()+"'" : "NULL");

		CRLObject crl = cert.getCrl();
		result += String.format("%s, ", crl != null ? "'"+crl.getFilename()+"'" : "NULL");
		
		ResourceHoldingObject parent = cert.getParent();
		result += String.format("%s, ", parent != null ? "'"+parent.getFilename()+"'" : "NULL");
		
		result += String.format("'%s', ", tree);
		result += String.format("%d, ", id);
		result += String.format("%d, ", parentId);
		
		result += String.format("%b, ", !cert.getChildren().isEmpty());
		result += String.format("'%s'", cert.getRemoteLocation().toString());
		
		return result;
	}

	public static String getManifestObjectDBValueString(ManifestObject mft, String tree, Integer parent_id, String parent_name) {
		String result = String.format("'%s', ", mft.getFilename());
		
		List<String> buffer_list = new ArrayList<String>();
		for (String filename : mft.getFiles().keySet()) {
			buffer_list.add("\"" + filename + " " + ByteArrayPrinter.bytesToHex(mft.getFiles().get(filename))+"\"");
		}

		if (buffer_list.isEmpty())
			result += "NULL, ";
		result += "'{" + String.join(",", buffer_list) + "}', ";

		result += String.format("'%s', ", mft.getValidityPeriod().getNotValidBefore().toString());
		result += String.format("'%s', ", mft.getValidityPeriod().getNotValidAfter().toString());
		result += String.format("'%s', ", mft.getValidationResults().getValidationStatus().toString());
		
		result += String.format("%s, ", getValidationCheckValueString(mft.getValidationResults().getErrors()));
		result += String.format("%s, ", getValidationCheckValueString(mft.getValidationResults().getWarnings()));

		result += String.format("'%s', ", parent_name);
		result += String.format("'%s', ", tree);
		result += String.format("%d, ", getNextID());
		result += String.format("%d, ", parent_id);
		result += String.format("'%s'", mft.getRemoteLocation().toString());
		return result;
	}

	public static String getCRLObjectDBValueString(CRLObject crl, String tree, Integer parent_id, String parent_name) {

		String result = String.format("'%s', ", crl.getFilename());
		
		List<String> buffer_list = new ArrayList<String>();
		for (Entry entry : crl.getCrl().getRevokedCertificates()) {
			buffer_list.add("\""+entry.getSerialNumber().toString() + " " + entry.getRevocationDateTime().toString()+"\"");
		}

		if (buffer_list.isEmpty())
			result += "NULL, ";
		else 
			result += "'{" + String.join(",", buffer_list) + "}', ";

		result += String.format("'%s', ", crl.getValidationResults().getValidationStatus().toString());
		
		result += String.format("%s, ", getValidationCheckValueString(crl.getValidationResults().getErrors()));
		result += String.format("%s, ", getValidationCheckValueString(crl.getValidationResults().getWarnings()));

		result += String.format("'%s', ", parent_name);
		result += String.format("'%s', ", tree);
		result += String.format("%d, ", getNextID());
		result += String.format("%d, ", parent_id);
		result += String.format("'%s'", crl.getRemoteLocation().toString());
		return result;
	}

	public static String getRoaObjectDBValueString(RoaObject roa, String tree, Integer parent_id, Integer ee_id) {
		String result = String.format("'%s', ", roa.getFilename());
		result += String.format("%d, ", roa.getAsn().getValue());

		result += String.format("'%s', ", roa.getValidityPeriod().getNotValidBefore().toString());
		result += String.format("'%s', ", roa.getValidityPeriod().getNotValidAfter().toString());
		result += String.format("'%s', ", roa.getValidationResults().getValidationStatus());
		
		result += String.format("%s, ", getValidationCheckValueString(roa.getValidationResults().getErrors()));
		result += String.format("%s, ", getValidationCheckValueString(roa.getValidationResults().getWarnings()));
		
		result += String.format("'%s', ", roa.getSigningTime().toString());
		
		List<String> buffer_list = new ArrayList<String>();
		for (RoaPrefix prefix : roa.getRoa().getPrefixes()) {
			buffer_list.add("ROW('" + prefix.getPrefix().toString()+"',"+Integer.toString(prefix.getEffectiveMaximumLength())+")::roaprefix");
		}
		result += "ARRAY[" + String.join(",", buffer_list) + "], ";
		
		result += String.format("'%s', ", roa.getParent().getFilename());
		result += String.format("'%s', ", tree);
		result += String.format("%d, ", getNextID());
		result += String.format("%d, ", parent_id);
		result += String.format("%d, ", ee_id);
		result += String.format("'%s'", roa.getRemoteLocation().toString());
		return result;
	}
	
	public static String getRoaObjectEECertDBValueString(X509ResourceCertificate cert, String roa_name, String tree, Integer parent_id,
			Integer ee_id) {
		String result = String.format("'%s', ", cert.getSubject().toString());
		result += String.format("%d, ", cert.getSerialNumber());
		result += String.format("'%s', ", cert.getIssuer().toString());

		byte[] SKI = cert.getSubjectKeyIdentifier();
		if (SKI == null)
			result += String.format("%s, ", "NULL");
		else
			result += String.format("'%s', ", ByteArrayPrinter.bytesToHex(SKI));
		
		byte[] AKI = cert.getAuthorityKeyIdentifier();
		if (AKI == null)
			result += String.format("%s, ", "NULL");
		else
			result += String.format("'%s', ", ByteArrayPrinter.bytesToHex(AKI));
		
		result += String.format("'%s', ", cert.getPublicKey().toString());
		result += String.format("%b, %b, %b, ", cert.isEe(), cert.isCa(), cert.isRoot());
		
		result += String.format("'%s', ", cert.getValidityPeriod().getNotValidBefore().toString());
		result += String.format("'%s', ", cert.getValidityPeriod().getNotValidAfter().toString());
	
		result += String.format("%s, ", getPrefixesValueString(cert.getResources()));
		result += String.format("%s, ", getASNsValueString(cert.getResources()));
		result += String.format("%s, ", getASNRangesValueString(cert.getResources()));
		
		
		result += String.format("'%s', ", tree);
		result += String.format("%d, ", ee_id);
		result += String.format("%d, ", parent_id);
		result += String.format("'%s'", roa_name);
		
		
		return result;
	}
	
	public static String getValidationCheckValueString(List<ValidationCheck> checklist) {
		List<String> buffer_list = new ArrayList<String>();
		for (ValidationCheck check : checklist) {
			buffer_list.add("\"" + check.getKey() + "\"");
		}
		if (buffer_list.isEmpty())
			return "NULL";
		return "'{" + String.join(",", buffer_list) + "}'";
	}
	
	public static String getPrefixesValueString(IpResourceSet rset) {
		List<String> buffer_list = new ArrayList<String>();
		IPAddress addr;
		for (IpResource ipResource : rset) {
			if (ipResource instanceof IpRange) {
				try {
					addr = new IPAddressString(ipResource.getStart().toString()).toAddress();
					IPAddress[] prefixes = addr.spanWithPrefixBlocks(new IPAddressString(ipResource.getEnd().toString()).toAddress());
					for (IPAddress prefix : prefixes) {
						buffer_list.add("\"" + prefix.toString() + "\"");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (buffer_list.isEmpty())
			return "NULL";
		return "'{" + String.join(",", buffer_list) + "}'";
	}

	public static String getASNsValueString(IpResourceSet rset) {
		List<String> buffer_list = new ArrayList<String>();
		for (IpResource ipResource : rset) {
			if (ipResource instanceof Asn) {
				buffer_list.add(((Asn) ipResource).getValue().toString());
			}
		}
		if (buffer_list.isEmpty())
			return "NULL";
		return "'{" + String.join(",", buffer_list) + "}'";
	}

	public static String getASNRangesValueString(IpResourceSet rset) {
		List<String> buffer_list = new ArrayList<String>();
		String buf;
		for (IpResource ipResource : rset) {
			if (ipResource instanceof IpResourceRange) {
				if (!(ipResource.getStart() instanceof Asn))
					continue;
				buf = ipResource.getStart().getValue().toString() + "," + ipResource.getEnd().getValue().toString();
				buf = "\"[" + buf + "]\"";
				buffer_list.add(buf);
			}
		}
		if (buffer_list.isEmpty())
			return "NULL";
		return "'{" + String.join(",", buffer_list) + "}'";
	}

	public static void readConfig(String path) {
		Properties prop = new Properties();
		log.log(Level.FINE, "Reading config file at: {0}", path);
		try {
			prop.load(new FileInputStream(path));
//			setTALDir(prop.getProperty("tal_dir", "/var/lib/MIRO/Validator/tals/"));
//			setBaseDir(prop.getProperty("repo_dir", "/var/data/MIRO/Validator/repo/"));
//			setPrefetchDir(prop.getProperty("prefetch_dir", "/var/data/MIRO/Validator/prefetch/"));
//			setExportDir(prop.getProperty("json_export_dir", "/var/data/MIRO/Validator/export/"));
//			setDatabaseCredentials(prop);
			setDatabaseLocation(prop);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error: Could not read config file at {0}. Exiting.", path);
		}
	}

	private static void setDatabaseLocation(Properties prop) {
		DB_HOST = prop.getProperty("db_host");
//		DB_NAME = prop.getProperty("db_name");
		DB_PORT = prop.getProperty("db_port");
	}
	private static void setDatabaseCredentials(Properties prop) {
		DB_USER = prop.getProperty("db_user");
		DB_PWD = prop.getProperty("db_pwd");
	}
	
	private static void checkArguments(String[] args) {
		if(args.length != 1){
			log.log(Level.SEVERE, "Error: Incorrect number of arguments. Exiting");
			System.exit(0);
		}
		
		if(!new File(args[0]).isFile()){
			log.log(Level.SEVERE,"Error: {0} is not a file. Exiting", args[0]);
			System.exit(0);
		}
	}

	private static void setTALDir(String key) {
		TALDirectory = key;
		log.log(Level.FINE, "Set TALDirectory: {0}", key);
	}
	
	private static void setBaseDir(String key) {
		BASE_DIR = key;
		log.log(Level.FINE, "Set BASE_DIR: {0}", key);
	}
	
	private static void setPrefetchDir(String key) {
		PREFETCH_DIR = key;
		log.log(Level.FINE, "Set PREFETCH_DIR: {0}", key);
	}
	
	private static void setExportDir(String key) {
		EXPORT_DIR = key;
		log.log(Level.FINE, "Set EXPORT_DIR: {0}", key);
	}
	
	public static File[] getTALGroupDirectories() {
		File talMainDir = new File(TALDirectory);
		File[] talGroupDirectories = talMainDir.listFiles(new FileFilter() {
			
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		if (talGroupDirectories == null) {
			log.log(Level.INFO, "Could not access {0}. No trust anchor locators found.", TALDirectory);
			return new File[0];
		}
		return talGroupDirectories;
		
	}

	public static List<ResourceCertificateTree> downloadAndValidateTALs(File talDirectory) {
		ResourceCertificateTreeValidator validator;
		ObjectFetcher fetcher;
		TrustAnchorLocator tal;
		ResourceCertificateTree tree;
		List<ResourceCertificateTree> tree_list = new ArrayList<ResourceCertificateTree>();
		try {
			fetcher = new RsyncFetcher(BASE_DIR, PREFETCH_DIR
					+ talDirectory.getName());
			validator = new ResourceCertificateTreeValidator(fetcher);

			for (String filename : talDirectory.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith("tal");
				}}))
			{
				try {
					tal = new TrustAnchorLocator(talDirectory.getAbsolutePath()
							+ "/" + filename);
				} catch (Exception e) {
					log.log(Level.SEVERE, "Error reading TAL " + filename +". Skipping");
					continue;
				}
				tree = validator.withTAL(tal);

				if (tree == null)
					continue;

				tree_list.add(tree);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE,
					"Could not process " + talDirectory.getName(),
					e.getMessage());
			e.printStackTrace();
		}
		return tree_list;
	}

	public static RPKIRepositoryStats getRPKIRepositoryStats(
			ResourceCertificateTree certTree) {
		ResultExtractor extractor = new ResultExtractor(certTree);
		extractor.count();
		return extractor.getRPKIRepositoryStats();
	}

	public static void exportTreeAsJson(ResourceCertificateTree tree) {
		JsonExporter exporter = new JsonExporter(EXPORT_DIR
				+ tree.getName() + ".json");
		exporter.export(tree);
	}
	
	public static Integer getNextID() {
		ID_COUNTER++;
		return ID_COUNTER;
	}
}
