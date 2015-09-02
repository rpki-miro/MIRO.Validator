package miro.validator.test;

import static org.junit.Assert.*;

import java.net.URI;

import miro.validator.TrustAnchorLocator;

import org.bouncycastle.util.Arrays;
import org.junit.Test;

public class TrustAnchorLocatorTest {

	@Test
	public void testGetFirstNewLineFromByteArray() {
		String stringWithNewLine = "dsad\nwakdpoads";
		int index = TrustAnchorLocator.getFirstNewLineIndex(stringWithNewLine.getBytes());
		assertEquals("Newline index must be 4", 4, index);
		
		stringWithNewLine = "dsadwakdpoads";
		index = TrustAnchorLocator.getFirstNewLineIndex(stringWithNewLine.getBytes());
		assertEquals("Newline index must be -1", -1, index);

		stringWithNewLine = "dsadwakdpo\nads";
		index = TrustAnchorLocator.getFirstNewLineIndex(stringWithNewLine.getBytes());
		assertEquals("Newline index must be 10", 10, index);
		
	}
	
	@Test
	public void testURIfromByteArray() {
		String uriString = "rsync://rpki.test.host/some_trust_anchor.cer";
		URI expectedUri = URI.create(uriString);
		String str = uriString + "\n1823908509180961823";
		URI uri = TrustAnchorLocator.getURIfromTALbytes(str.getBytes());
		assertEquals("URI must be " + uriString, uri,expectedUri);
	}
	
	@Test
	public void testSubjectPubKeyInfoFromByteArray() {
		byte[] bs = "ajsdasdj\n".getBytes();
		byte[] pubKeyInfo = "1234910302392".getBytes();
		byte[] input = Arrays.concatenate(bs, pubKeyInfo);
		byte[] spki = TrustAnchorLocator.getSubjectPubKeyInfoFromTALbytes(input);
		assertArrayEquals(pubKeyInfo, spki);
	}
	
	
	

}
