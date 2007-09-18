package lua.value;

import java.io.IOException;
import java.io.InputStream;

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
}
