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
package test.java.miro.validator;

import static org.junit.Assert.*;

import java.net.URI;

import main.java.miro.validator.TrustAnchorLocator;

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
