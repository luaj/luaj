/*******************************************************************************
 * Copyright (c) 2014 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm2;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.luaj.vm2.Globals.BufferedStream;


public class BufferedStreamTest extends TestCase {

	public BufferedStreamTest() {}
	
	private BufferedStream NewBufferedStream(int buflen, String contents) {
		return new BufferedStream(buflen, new ByteArrayInputStream(contents.getBytes()));
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testReadEmptyStream() throws java.io.IOException {
		BufferedStream bs = NewBufferedStream(4, "");
		assertEquals(-1, bs.read());
		assertEquals(-1, bs.read(new byte[10]));
		assertEquals(-1, bs.read(new byte[10], 0, 10));
	}
	
	public void testReadByte() throws java.io.IOException {
		BufferedStream bs = NewBufferedStream(2, "abc");
		assertEquals('a', bs.read());
		assertEquals('b', bs.read());
		assertEquals('c', bs.read());
		assertEquals(-1, bs.read());
	}
	
	public void testReadByteArray() throws java.io.IOException {
		byte[] array = new byte[3];
		BufferedStream bs = NewBufferedStream(4, "abcdef");
		assertEquals(3, bs.read(array));
		assertEquals("abc", new String(array));
		assertEquals(1, bs.read(array));
		assertEquals("d", new String(array, 0, 1));
		assertEquals(2, bs.read(array));
		assertEquals("ef", new String(array, 0, 2));
		assertEquals(-1, bs.read());
	}
	
	public void testReadByteArrayOffsetLength() throws java.io.IOException {
		byte[] array = new byte[10];
		BufferedStream bs = NewBufferedStream(8, "abcdefghijklmn");
		assertEquals(4, bs.read(array, 0, 4));
		assertEquals("abcd", new String(array, 0, 4));
		assertEquals(4, bs.read(array, 2, 8));
		assertEquals("efgh", new String(array, 2, 4));
		assertEquals(6, bs.read(array, 0, 10));
		assertEquals("ijklmn", new String(array, 0, 6));
		assertEquals(-1, bs.read());
	}
	
	public void testMarkOffsetBeginningOfStream() throws java.io.IOException {
		byte[] array = new byte[4];
		BufferedStream bs = NewBufferedStream(8, "abcdefghijkl");
		assertEquals(true, bs.markSupported());
		bs.mark(4);
		assertEquals(4, bs.read(array));
		assertEquals("abcd", new String(array));
		bs.reset();
		assertEquals(4, bs.read(array));
		assertEquals("abcd", new String(array));
		assertEquals(4, bs.read(array));
		assertEquals("efgh", new String(array));
		assertEquals(4, bs.read(array));
		assertEquals("ijkl", new String(array));
		assertEquals(-1, bs.read());
	}

	public void testMarkOffsetMiddleOfStream() throws java.io.IOException {
		byte[] array = new byte[4];
		BufferedStream bs = NewBufferedStream(8, "abcdefghijkl");
		assertEquals(true, bs.markSupported());
		assertEquals(4, bs.read(array));
		assertEquals("abcd", new String(array));
		bs.mark(4);
		assertEquals(4, bs.read(array));
		assertEquals("efgh", new String(array));
		bs.reset();
		assertEquals(4, bs.read(array));
		assertEquals("efgh", new String(array));
		assertEquals(4, bs.read(array));
		assertEquals("ijkl", new String(array));
		assertEquals(-1, bs.read());
	}
}
