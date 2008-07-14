package org.luaj.jit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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

public class LuaJitPerfTest extends TestCase {

	static {
	    Platform.setInstance(new J2sePlatform());
		LuaC.install();
	}
//
//	public void testFannkuch() throws IOException {
//		timedFileTest( "fannkuch.lua" );
//	}
//	
//	public void testMandelbrot() throws IOException {
//		timedFileTest( "mandelbrot.lua" );
//	}
	
	public void testNbody() throws IOException {
		timedFileTest( "nbody.lua" );
	}
//	
//    public void testForLoop() throws IOException {    	
//    	timedTest( "for loop",  
//			"local sum=0\n" +
//			"for i=1,10000,1 do\n" +
//			"	sum = sum + i\n" +
//			"end");
//    }
//
    
    private void timedFileTest(String filename) throws IOException {
    	File file = new File("src/test/perf/"+filename);
    	int len = (int) file.length();
    	byte[] b = new byte[len];
    	DataInputStream dis = new DataInputStream( new FileInputStream( file ) );
    	dis.readFully(b);
    	dis.close();
    	timedTest( filename, new String(b) ); 
    	
    }
   
	private void timedTest(String testName, String script) throws IOException {
		System.out.println("---- "+testName+" ----");
		InputStream is = new ByteArrayInputStream(script.getBytes());
		LPrototype p = LuaC.compile(is, "script");
		int plain = timeTrial( "plain", p );
		LPrototype q = LuaJit.jitCompile( p );
		assertTrue(p!=q);
		int jit = timeTrial( "jit", q );
		System.out.println("plain="+plain+" jit="+jit+" ratio="+(double)jit/(double)plain);
		assertTrue( "jit faster than plain", jit > plain );
	}
	
	private static int timeTrial(String type, LPrototype p) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BaseLib.redirectOutput(outputStream);
		LuaState vm = Platform.newLuaState();
		LClosure c = p.newClosure(vm._G);
		int globalCount = 0;
		for ( int i=0; i<5; i++ ) {
			int localCount = 0;
			long t1 = System.currentTimeMillis() + 1000;
			while ( t1 > System.currentTimeMillis() ) {
				vm.pushlvalue(c);
				vm.call(0, 0);
				localCount++;
			}
			System.out.println(type+": "+(localCount));
			System.out.flush();
			globalCount += localCount;
		}
		return globalCount;
	}
}
