package org.luaj.compiler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.luaj.TestPlatform;
import org.luaj.debug.Print;
import org.luaj.lib.BaseLib;
import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

public class SimpleTests extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        Platform.setInstance(new TestPlatform());
    }

    private void doTest( String script ) {
    	try {
        	InputStream is = new ByteArrayInputStream( script.getBytes("UTF8") );
			LPrototype p = LuaC.compile( is, "script" );
			assertNotNull( p );
			Print.printCode( p );
			
			// try running the code!
			LuaState state = Platform.newLuaState();
			BaseLib.install( state._G );
			LClosure c = p.newClosure( state._G );
			state.doCall( c, new LValue[0] );
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
}
