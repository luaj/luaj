package org.luaj.vm2.compiler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.JsePlatform;

public class SimpleTests extends TestCase {

	private LuaTable _G;

    protected void setUp() throws Exception {
        super.setUp();
        _G = JsePlatform.standardGlobals();
    }

    private void doTest( String script ) {
    	try {
        	InputStream is = new ByteArrayInputStream( script.getBytes("UTF8") );
			LuaFunction c = LuaC.instance.load( is, "script", _G );
			c.call();
    	} catch ( Exception e ) {
    		fail("i/o exception: "+e );
    	}
    }

    public void testTrivial() {
		String s = "print( 2 )\n";
		doTest( s );
	}
	
	public void testAlmostTrivial() {
		String s = "print( 2 )\n" +
				"print( 3 )\n";
		doTest( s );
	}
	
	public void testSimple() {
		String s = "print( 'hello, world' )\n"+
			"for i = 2,4 do\n" +
			"	print( 'i', i )\n" +
			"end\n";
		doTest( s );
	}
	
	public void testBreak() {
		String s = "a=1\n"+
			"while true do\n"+
			"  if a>10 then\n"+
			"     break\n"+
			"  end\n"+
			"  a=a+1\n"+
			"  print( a )\n"+
			"end\n";
		doTest( s );
	}
	
	public void testShebang() {
		String s = "#!../lua\n"+
			"print( 2 )\n";
		doTest( s );
	}
	
	public void testInlineTable() {
		String s = "A = {g=10}\n"+
			"print( A )\n";
		doTest( s );
	}

	public void testEqualsAnd() {
		String s = "print( 1 == b and b )\n";
		doTest( s );
	}
	
	private static final int [] samehash = { 0, 1, -1, 2, -2, 4, 8, 16, 32, Integer.MAX_VALUE, Integer.MIN_VALUE };
	private static final double [] diffhash = { .5, 1, 1.5, 1, .5, 1.5, 1.25, 2.5 };
	
	public void testDoubleHashCode() {
		for ( int i=0; i<samehash.length; i++ ) {
			LuaValue j = LuaInteger.valueOf(samehash[i]);
			LuaValue d = LuaDouble.valueOf(samehash[i]);
			int hj = j.hashCode();
			int hd = d.hashCode();
			assertEquals(hj, hd);
		}
		for ( int i=0; i<diffhash.length; i+=2 ) {
			LuaValue c = LuaValue.valueOf(diffhash[i+0]);
			LuaValue d = LuaValue.valueOf(diffhash[i+1]);
			int hc = c.hashCode();
			int hd = d.hashCode();
			assertTrue("hash codes are same: "+hc,hc!=hd);
		}
	}
}
