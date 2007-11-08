package org.luaj.vm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.luaj.debug.DebugLuaState;
import org.luaj.lib.MathLib;

public class StandardTest extends TestCase {
	
	public static Test suite() throws IOException {
		ZipEntry file;
		
		final HashMap tests = new HashMap();
		final HashMap results = new HashMap();
		
		InputStream zipStream = StandardTest.class.getResourceAsStream( "/standard-tests.zip" );
		ZipInputStream testSuiteArchive = new ZipInputStream( zipStream );
		try {
			while ( ( file = testSuiteArchive.getNextEntry() ) != null ) {
				final String entryName = file.getName();
				if ( entryName.endsWith( ".luac" ) ) {
					LPrototype p = LoadState.undump( new LuaState(), testSuiteArchive, entryName );
					tests.put( entryName.substring( 0, entryName.length() - 5 ), p );
				} else if ( entryName.endsWith( ".out" ) ) {
					results.put( entryName.substring( 0, entryName.length() - 4 ), readString( testSuiteArchive ) );
				}
			}
		} finally {
			testSuiteArchive.close();
		}
		
		TestSuite suite = new TestSuite();
		
		for ( Iterator keys = tests.keySet().iterator(); keys.hasNext(); ) {
			String test = (String)keys.next();
			final LPrototype code = (LPrototype)tests.get( test );
			final String expectedResult = (String)results.get( test );
			
			if ( code != null && expectedResult != null ) {
				suite.addTest( new StandardTest( test, code, expectedResult ) );
			}
		}
		
		return suite;
	}
	
	private final LPrototype code;
	private final String expectedResult;
	
	public StandardTest( String name, LPrototype code, String expectedResult ) {
		super( name );
		this.code = code;
		this.expectedResult = expectedResult;
	}
	
	public void runTest() {
		LuaState state = new DebugLuaState();
		MathLib.install(state._G);
		// hack: it's unpleasant when the test cases fail to terminate;
		// unfortunately, there is a test in the standard suite that
		// relies on weak tables having their elements removed by
		// the garbage collector. Until we implement that, remove the
		// built-in collectgarbage function.
		state._G.put( "collectgarbage", LNil.NIL );
		LClosure c = new LClosure( code, state._G );
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		BaseLib.redirectOutput( output );
		try {
			try {
				state.doCall( c, new LValue[0] );
			} catch ( RuntimeException exn ) {
				final int ncalls = Math.min( state.calls.length, state.cc+1 );
				StackTraceElement[] stackTrace = new StackTraceElement[ncalls];
				
				for ( int i = 0; i < ncalls; ++i ) {
					CallInfo call = state.calls[i];
					LPrototype p = call.closure.p;
					int line = p.lineinfo[call.pc-1];
					String func = call.closure.luaAsString().toJavaString();
					stackTrace[ncalls - i - 1] = new StackTraceElement(getName(), func, getName()+".lua", line );
				}
				
				RuntimeException newExn = new RuntimeException( exn );
				newExn.setStackTrace( stackTrace );
				newExn.printStackTrace();
				throw newExn;
			}
			
			final String actualResult = new String( output.toByteArray() );
			
			assertEquals( expectedResult, actualResult );
		} finally {
			BaseLib.restoreStandardOutput();
		}
	}
	
	private static String readString( InputStream is ) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy( is, baos );
		return new String( baos.toByteArray() );
	}
	
	private static void copy( InputStream is, OutputStream os ) throws IOException {
		byte[] buf = new byte[ 1024 ];
		int r;
		while ( ( r = is.read( buf ) ) >= 0 ) {
			os.write( buf, 0, r );
		}
	}
}
