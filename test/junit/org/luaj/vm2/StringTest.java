package org.luaj.vm2;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.luaj.vm2.lib.JsePlatform;

public class StringTest extends TestCase {

	protected void setUp() throws Exception {
		JsePlatform.standardGlobals();
	}

	public void testToInputStream() throws IOException {
		LuaString str = LuaString.valueOf("Hello");
		
		InputStream is = str.toInputStream();
		
		assertEquals( 'H', is.read() );
		assertEquals( 'e', is.read() );
		assertEquals( 2, is.skip( 2 ) );
		assertEquals( 'o', is.read() );
		assertEquals( -1, is.read() );
		
		assertTrue( is.markSupported() );
		
		is.reset();
		
		assertEquals( 'H', is.read() );
		is.mark( 4 );
		
		assertEquals( 'e', is.read() );
		is.reset();
		assertEquals( 'e', is.read() );
		
		LuaString substr = str.substring( 1, 4 );
		assertEquals( 3, substr.length() );
		
		is.close();
		is = substr.toInputStream();
		
		assertEquals( 'e', is.read() );
		assertEquals( 'l', is.read() );
		assertEquals( 'l', is.read() );
		assertEquals( -1, is.read() );
		
		is = substr.toInputStream();
		is.reset();
		
		assertEquals( 'e', is.read() );
	}
	
	
	private static final String userFriendly( String s ) {
		StringBuffer sb = new StringBuffer();
		for ( int i=0, n=s.length(); i<n; i++ ) {
			int c = s.charAt(i);
			if ( c < ' ' || c >= 0x80 ) { 
				sb.append( "\\u"+Integer.toHexString(0x10000+c).substring(1) );
			} else {
				sb.append( (char) c );
			}
		}
		return sb.toString();
	}
	
	public void testUtf8() {		
		for ( int i=4; i<0xffff; i+=4 ) {
			char[] c = { (char) (i+0), (char) (i+1), (char) (i+2), (char) (i+3) };
			String before = new String(c)+" "+i+"-"+(i+4);
			LuaString ls = LuaString.valueOf(before);
			String after = ls.tojstring();
			assertEquals( userFriendly( before ), userFriendly( after ) );
		}
		char[] c = { (char) (1), (char) (2), (char) (3) };
		String before = new String(c)+" 1-3";
		LuaString ls = LuaString.valueOf(before);
		String after = ls.tojstring();
		assertEquals( userFriendly( before ), userFriendly( after ) );
		
	}

	public void testSpotCheckUtf8() throws UnsupportedEncodingException {
		byte[] bytes = {(byte)194,(byte)160,(byte)194,(byte)161,(byte)194,(byte)162,(byte)194,(byte)163,(byte)194,(byte)164};
		String expected = new String(bytes, "UTF8");
		String actual = LuaString.valueOf(bytes).tojstring();
		char[] d = actual.toCharArray();
		assertEquals(160, d[0]);
		assertEquals(161, d[1]);
		assertEquals(162, d[2]);
		assertEquals(163, d[3]);
		assertEquals(164, d[4]);
		
	}
	
	public void testNullTerminated() {		
		char[] c = { 'a', 'b', 'c', '\0', 'd', 'e', 'f' };
		String before = new String(c);
		LuaString ls = LuaString.valueOf(before);
		String after = ls.tojstring();
		assertEquals( userFriendly( "abc" ), userFriendly( after ) );
		
	}
}
