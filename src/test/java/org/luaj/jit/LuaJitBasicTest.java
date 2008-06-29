package org.luaj.jit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.luaj.compiler.LuaC;
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

    public void testForLoop() throws IOException {
    	stringTest( "print 'starting'\n" +
				"for i=1,3 do\n" +
				"	print( 'i', i )\n" +
				"end");
    }
        
	private void stringTest(String program) throws IOException {		
		InputStream is = new ByteArrayInputStream(program.getBytes());
		LPrototype p = LuaC.compile(is, "program");
		run( p );
		LPrototype q = LuaJit.jitCompile( p );
		assertTrue(p!=q);
		run( q );
	}
	
	private static void run(LPrototype p) {
		LuaState vm = Platform.newLuaState();
		LClosure c = p.newClosure(vm._G);
		vm.pushlvalue(c);
		vm.call(0, 0);
	}
    
}
