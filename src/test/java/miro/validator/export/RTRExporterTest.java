package test.java.miro.validator.export;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import main.java.miro.validator.export.rtr.RTRExporter;
import main.java.miro.validator.types.RoaObject;
import net.ripe.ipresource.Asn;
import net.ripe.ipresource.IpRange;
import net.ripe.rpki.commons.crypto.cms.roa.RoaCms;
import net.ripe.rpki.commons.crypto.cms.roa.RoaPrefix;

import org.junit.Test;

public class RTRExporterTest {

	@Test
	public void test() {
		RoaObject roaObj1 = mock(RoaObject.class);

		RoaCms roa1 = mock(RoaCms.class);

		RoaPrefix pref1 = mock(RoaPrefix.class);
		when(pref1.getPrefix()).thenReturn(IpRange.parse("109.145.0.0/16"));
		when(pref1.getEffectiveMaximumLength()).thenReturn(new Integer(24));

		RoaPrefix pref2 = mock(RoaPrefix.class);
		when(pref2.getPrefix()).thenReturn(IpRange.parse("143.0.0.0/8"));
		when(pref2.getEffectiveMaximumLength()).thenReturn(new Integer(16));

		List<RoaPrefix> pref_list1 = new ArrayList<RoaPrefix>();
		pref_list1.add(pref1);
		pref_list1.add(pref2);
		
		when(roa1.getPrefixes()).thenReturn(pref_list1);
		
		Asn asn1 = new Asn(new BigInteger("1234"));
		
		when(roaObj1.getRoa()).thenReturn(roa1);
		when(roaObj1.getAsn()).thenReturn(asn1);
		
		RoaObject roaObj2 = mock(RoaObject.class);

		RoaCms roa2 = mock(RoaCms.class);

		RoaPrefix pref3 = mock(RoaPrefix.class);
		when(pref3.getPrefix()).thenReturn(IpRange.parse("129.131.41.0/24"));
		when(pref3.getEffectiveMaximumLength()).thenReturn(new Integer(24));

		RoaPrefix pref4 = mock(RoaPrefix.class);
		when(pref4.getPrefix()).thenReturn(IpRange.parse("123.192.0.0/10"));
		when(pref4.getEffectiveMaximumLength()).thenReturn(new Integer(16));

		List<RoaPrefix> pref_list2 = new ArrayList<RoaPrefix>();
		pref_list2.add(pref3);
		pref_list2.add(pref4);
		when(roa2.getPrefixes()).thenReturn(pref_list2);
		
		Asn asn2 = new Asn(new BigInteger("5678")); 
		
		when(roaObj2.getRoa()).thenReturn(roa2);
		when(roaObj2.getAsn()).thenReturn(asn2);
		
		List<RoaObject> roas = new ArrayList<RoaObject>();
		roas.add(roaObj1);
		roas.add(roaObj2);
		
		try {
			Files.delete(Paths.get("src/test/resources/export/roas.rtr"));
		} catch (IOException e) {
//			e.printStackTrace();
		}
		RTRExporter exporter = new RTRExporter("src/test/resources/export/roas.rtr");
		exporter.exportROAs(roas);
		exporter.exportROAs(roas);
		
		List<String> lines = readLines("src/test/resources/export/roas.rtr");
		assertTrue("1234 109.145.0.0/16 24".equals(lines.get(0)));
		assertTrue("1234 143.0.0.0/8 16".equals(lines.get(1)));
		assertTrue("5678 129.131.41.0/24 24".equals(lines.get(2)));
		assertTrue("5678 123.192.0.0/10 16".equals(lines.get(3)));
		assertTrue("1234 109.145.0.0/16 24".equals(lines.get(4)));
		assertTrue("1234 143.0.0.0/8 16".equals(lines.get(5)));
		assertTrue("5678 129.131.41.0/24 24".equals(lines.get(6)));
		assertTrue("5678 123.192.0.0/10 16".equals(lines.get(7)));
		assertTrue(lines.size() == 8);
	}
	
	public List<String> readLines(String filename){
		List<String> result = new ArrayList<String>();
		try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    for(String line; (line = br.readLine()) != null; ) {
		    	result.add(line);
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
