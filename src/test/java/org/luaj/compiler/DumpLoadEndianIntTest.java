package org.luaj.compiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LFunction;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

public class DumpLoadEndianIntTest extends TestCase {
	private static final String SAVECHUNKS = "SAVECHUNKS";

	private static final boolean SHOULDPASS = true;
	private static final boolean SHOULDFAIL = false;
	private static final String mixedscript = "return tostring(1234)..'-#!-'..tostring(23.75)";
	private static final String intscript = "return tostring(1234)..'-#!-'..tostring(23)";
	private static final String withdoubles = "1234-#!-23.75";
	private static final String withints = "1234-#!-23";
	
    protected void setUp() throws Exception {
        super.setUp();
        Platform.setInstance(new J2sePlatform());
        DumpState.ALLOW_INTEGER_CASTING = false;
    }

	public void testBigDoubleCompile() {
		doTest( false, DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES, false, mixedscript, withdoubles, withdoubles, SHOULDPASS );
		doTest( false, DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES, true, mixedscript, withdoubles, withdoubles, SHOULDPASS );
	}
	
	public void testLittleDoubleCompile() {
		doTest( true, DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES, false, mixedscript, withdoubles, withdoubles, SHOULDPASS );
		doTest( true, DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES, true, mixedscript, withdoubles, withdoubles, SHOULDPASS );
	}
	
	public void testBigIntCompile() {
        DumpState.ALLOW_INTEGER_CASTING = true;
		doTest( false, DumpState.NUMBER_FORMAT_INTS_ONLY, false, mixedscript, withdoubles, withints, SHOULDPASS );
		doTest( false, DumpState.NUMBER_FORMAT_INTS_ONLY, true, mixedscript, withdoubles, withints, SHOULDPASS );
        DumpState.ALLOW_INTEGER_CASTING = false;
		doTest( false, DumpState.NUMBER_FORMAT_INTS_ONLY, false, mixedscript, withdoubles, withints, SHOULDFAIL );
		doTest( false, DumpState.NUMBER_FORMAT_INTS_ONLY, true, mixedscript, withdoubles, withints, SHOULDFAIL );
		doTest( false, DumpState.NUMBER_FORMAT_INTS_ONLY, false, intscript, withints, withints, SHOULDPASS );
		doTest( false, DumpState.NUMBER_FORMAT_INTS_ONLY, true, intscript, withints, withints, SHOULDPASS );
	}
	
	public void testLittleIntCompile() {
        DumpState.ALLOW_INTEGER_CASTING = true;
		doTest( true, DumpState.NUMBER_FORMAT_INTS_ONLY, false, mixedscript, withdoubles, withints, SHOULDPASS );
		doTest( true, DumpState.NUMBER_FORMAT_INTS_ONLY, true, mixedscript, withdoubles, withints, SHOULDPASS );
        DumpState.ALLOW_INTEGER_CASTING = false;
		doTest( true, DumpState.NUMBER_FORMAT_INTS_ONLY, false, mixedscript, withdoubles, withints, SHOULDFAIL );
		doTest( true, DumpState.NUMBER_FORMAT_INTS_ONLY, true, mixedscript, withdoubles, withints, SHOULDFAIL );
		doTest( true, DumpState.NUMBER_FORMAT_INTS_ONLY, false, intscript, withints, withints, SHOULDPASS );
		doTest( true, DumpState.NUMBER_FORMAT_INTS_ONLY, true, intscript, withints, withints, SHOULDPASS );
	}
	
	public void testBigNumpatchCompile() {
		doTest( false, DumpState.NUMBER_FORMAT_NUM_PATCH_INT32, false, mixedscript, withdoubles, withdoubles, SHOULDPASS );
		doTest( false, DumpState.NUMBER_FORMAT_NUM_PATCH_INT32, true, mixedscript, withdoubles, withdoubles, SHOULDPASS );
	}
	
	public void testLittleNumpatchCompile() {
		doTest( true, DumpState.NUMBER_FORMAT_NUM_PATCH_INT32, false, mixedscript, withdoubles, withdoubles, SHOULDPASS );
		doTest( true, DumpState.NUMBER_FORMAT_NUM_PATCH_INT32, true, mixedscript, withdoubles, withdoubles, SHOULDPASS );
	}
	
	public void doTest( boolean littleEndian, int numberFormat, boolean stripDebug, 
			String script, String expectedPriorDump, String expectedPostDump, boolean shouldPass ) {
        try {
            LuaState vm = Platform.newLuaState();
            
            // compile into prototype
            InputStream is = new ByteArrayInputStream(script.getBytes());
            LPrototype p = LuaC.compile(is, "script");
            
            // double check script result before dumping
            LFunction f = p.newClosure(vm._G);
            vm.pushfunction(f);
            vm.call(0,1);
            String actual = vm.poplvalue().toJavaString();
            assertEquals( expectedPriorDump, actual );
            
            // dump into bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                DumpState.dump(p, baos, stripDebug, numberFormat, littleEndian);
            	if ( ! shouldPass )
            		fail( "dump should not have succeeded" );
            } catch ( Exception e ) {
            	if ( shouldPass )
            		fail( "dump threw "+e );
            	else
            		return;
            }
            byte[] dumped = baos.toByteArray();
            
            // load again using compiler
            is = new ByteArrayInputStream(dumped);
            vm.load(is, "dumped");
            vm.call(0,1);
            actual = vm.poplvalue().toJavaString();
            assertEquals( expectedPostDump, actual );

            // write test chunk
            if ( System.getProperty(SAVECHUNKS) != null && script.equals(mixedscript) ) {
            	new File("build").mkdirs();
	            String filename = "build/test-"
	            	+(littleEndian? "little-": "big-")
	            	+(numberFormat==DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES? "double-":
	            	  numberFormat==DumpState.NUMBER_FORMAT_INTS_ONLY? "int-":
   	            	  numberFormat==DumpState.NUMBER_FORMAT_NUM_PATCH_INT32? "numpatch4-": "???-")
	            	+(stripDebug? "nodebug-": "debug-")
	            	+"bin.lua";
	            FileOutputStream fos = new FileOutputStream(filename);
	            fos.write( dumped );
	            fos.close();
            }
            
        } catch (IOException e) {
            fail(e.toString());
        }
	}
}
