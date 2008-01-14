package org.luaj.vm;

import java.io.IOException;
import java.io.InputStream;

import org.luaj.vm.LString;

import junit.framework.TestCase;

public class LStringTest extends TestCase {
	public void testToInputStream() throws IOException {
		LString str = new LString("Hello");
		
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
		
		LString substr = str.substring( 1, 4 );
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
			LString ls = new LString(before);
			String after = ls.toJavaString();
			assertEquals( userFriendly( before ), userFriendly( after ) );
		}
		char[] c = { (char) (1), (char) (2), (char) (3) };
		String before = new String(c)+" 1-3";
		LString ls = new LString(before);
		String after = ls.toJavaString();
		assertEquals( userFriendly( before ), userFriendly( after ) );
		
	}

	public void testNullTerminated() {		
		char[] c = { 'a', 'b', 'c', '\0', 'd', 'e', 'f' };
		String before = new String(c);
		LString ls = new LString(before);
		String after = ls.toJavaString();
		assertEquals( userFriendly( "abc" ), userFriendly( after ) );
		
	}
}
