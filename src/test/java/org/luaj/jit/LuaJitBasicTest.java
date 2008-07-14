package org.luaj.jit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.luaj.compiler.LuaC;
import org.luaj.lib.BaseLib;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

/**
 * Simple test cases for lua jit basic functional test
 */
public class LuaJitBasicTest extends TestCase {

	static {
	    Platform.setInstance(new J2sePlatform());
		LuaC.install();
	}

    public void testPrintHelloWorld() throws IOException {
    	stringTest( "print( 'hello, world' )" );
    }

    public void testArgPassing() throws IOException {
    	stringTest( 
    		"local function f(a,b)\n" +
    		"	print('a',a,'b',b)\n" +
    		"	return 'c','d'\n" +
    		"end\n" +
    		"print( 'f1', f(123) )\n" +
			"print( 'f2', f(456,789) )\n" +
    		"print( 'f3', f(12,34,56) )\n" );
    }

    public void testForInDoEnd() throws IOException {    	
    	stringTest( 
			"local t = {abc=123,def=456}\n" +
			"for k,v in pairs(t) do\n" +
			"	print( 'k,v', k, v )\n" +
			"end");
    }
        
    public void testForIEqualsDoEnd() throws IOException {    	
    	stringTest( 
			"print 'starting'\n" +
			"for i=1,5,2 do\n" +
			"	print( 'i', i )\n" +
			"end");
    }
        
    public void testRepeatUntil() throws IOException {
    	stringTest( 
       		"local i=7\n" +
    		"repeat\n"+
	        "	print(i)\n"+
	        "until i\n");
    }
        
    public void testWhileDoEnd() throws IOException {    	
    	stringTest( 
    		"local i=4\n" +
    		"while i>0 do\n"+
	        "	print( i )\n"+
	        "	i = i-1\n"+
	        "end\n");
    }
        
    public void testForIEqualsDoBreakEnd() throws IOException {    	
    	stringTest( 
			"print 'starting'\n" +
			"for i=1,5,2 do\n" +
			"	print( 'i', i )\n" +
			"	break\n" +
			"end");
    }
        
    public void testRepeatUntilBreak() throws IOException {    	
    	stringTest( 
	   		"local i=7\n" +
			"repeat\n"+
	        "	print(i)\n"+
			"	break\n"+
	        "until i\n");
    }
        
    public void testWhileDoBreak() throws IOException {    	
    	stringTest( 
    		"local i=4\n" +
    		"while i>0 do\n"+
	        "	print( i )\n"+
	        "	i = i-1\n"+
			"	break\n"+
	        "end\n");
    }
        
    public void testIfThenEnd() throws IOException {    	
    	stringTest(
    		"if a then\n" +
    		"	print(1)\n" +
    		"end\n" +
    		"print(2)\n" );
    }
        
    public void testIfThenElseEnd() throws IOException {    	
    	stringTest(
    		"if a then\n" +
    		"	print(1)\n" +
    		"else\n" +
    		"	print(2)\n" +
    		"end\n" +
    		"print(3)\n" );
    }
        
    public void testIfThenElseifElseEnd() throws IOException {    	
    	stringTest(
    		"if a then\n" +
    		"	print(1)\n" +
    		"elseif b then \n" +
    		"	print(2)\n" +
    		"else\n" +
    		"	print(3)\n" +
    		"end\n" +
    		"print(4)\n" );
    }
        
	private void stringTest(String program) throws IOException {		
		InputStream is = new ByteArrayInputStream(program.getBytes());
		LPrototype p = LuaC.compile(is, "program");
		String expected = run( p );
		LPrototype q = LuaJit.jitCompile( p );
		assertTrue(p!=q);
		String actual = run( q );
		assertEquals( expected, actual );
	}
	
	private static String run(LPrototype p) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BaseLib.redirectOutput(outputStream);
		LuaState vm = Platform.newLuaState();
		LClosure c = p.newClosure(vm._G);
		vm.pushlvalue(c);
		vm.call(0, 0);
		return outputStream.toString();
	}
    
}
