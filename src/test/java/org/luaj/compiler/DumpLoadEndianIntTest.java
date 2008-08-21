package org.luaj.compiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

	private static final String script = "return tostring(1234)..'-#!-'..tostring(23.75)";
	private static final String withdoubles = "1234-#!-23.75";
	private static final String withints = "1234-#!-23";
	
    protected void setUp() throws Exception {
        super.setUp();
        Platform.setInstance(new J2sePlatform());
        DumpState.ALLOW_INTEGER_CASTING = true;
    }

	public void testBidDoubleCompile() {
		doTest( false, false, false, withdoubles );
		doTest( false, false, true, withdoubles );
	}
	
	public void testLittleDoubleCompile() {
		doTest( true, false, false, withdoubles );
		doTest( true, false, true, withdoubles );
	}
	
	public void testBigIntCompile() {
		doTest( false, true, false, withints );
		doTest( false, true, true, withints );
	}
	
	public void testLittleIntCompile() {
		doTest( true, true, false, withints );
		doTest( true, true, true, withints );
	}
	
	public void doTest( boolean littleEndian, boolean intNumbers, boolean stripDebug, String expected ) {
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
            assertEquals( withdoubles, actual );
            
            // dump into bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DumpState.dump(p, baos, stripDebug, intNumbers, littleEndian);
            byte[] dumped = baos.toByteArray();
            
            // load again using compiler
            is = new ByteArrayInputStream(dumped);
            vm.load(is, "dumped");
            vm.call(0,1);
            actual = vm.poplvalue().toJavaString();
            assertEquals( expected, actual );

            // write test chunk
            if ( System.getProperty("SAVECHUNKS") != null ) {
	            String filename = "test-"
	            	+(littleEndian? "little-": "big-")
	            	+(intNumbers? "int-": "double-")
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
