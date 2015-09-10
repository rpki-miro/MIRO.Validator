package test.java.miro.validator.export;

import static org.junit.Assert.*;

import java.io.IOException;

import main.java.miro.validator.ResourceCertificateTreeValidator;
import main.java.miro.validator.TrustAnchorLocator;
import main.java.miro.validator.export.json.JsonExporter;
import main.java.miro.validator.fetcher.ObjectFetcher;
import main.java.miro.validator.fetcher.RsyncFetcher;
import main.java.miro.validator.types.ResourceCertificateTree;

import org.junit.Test;

public class JsonExporterTest {

	@Test
	public void exportResourceCertificateTreeTest() {
		try {
			ObjectFetcher fetcher = new RsyncFetcher("src/test/resources/fetcher/repository/",
					"src/test/resources/fetcher/prefetching/AFRINIC_prefetchURIs");
			ResourceCertificateTreeValidator validator = new ResourceCertificateTreeValidator(
					fetcher);

			TrustAnchorLocator tal = new TrustAnchorLocator("src/test/resources/tals/AFRINIC.tal");
			ResourceCertificateTree tree = validator.withTAL(tal);
			assertNotNull(tree.getTrustAnchor());
			assertTrue(tree.getName().equals("AFRINIC"));
			JsonExporter exporter = new JsonExporter("src/test/resources/export/AFRINIC.json");
			exporter.export(tree);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
