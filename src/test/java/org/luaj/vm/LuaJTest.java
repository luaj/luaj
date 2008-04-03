package org.luaj.vm;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.luaj.TestPlatform;
import org.luaj.compiler.LuaC;
import org.luaj.lib.BaseLib;


public class LuaJTest extends TestCase {
    

        protected void setUp() throws Exception {
            super.setUp();
            Platform.setInstance(new TestPlatform());
        }

        public void testTest1() throws IOException, InterruptedException {
                runTest( "test1" );
        }

        public void testTest2() throws IOException, InterruptedException {
                runTest( "test2" );
        }

        public void testTest3() throws IOException, InterruptedException {
                runTest( "test3" );
        }

        public void testTest4() throws IOException, InterruptedException {
                runTest( "test4" );
        }

        public void testTest5() throws IOException, InterruptedException {
                runTest( "test5" );
        }

        public void testTest6() throws IOException, InterruptedException {
                runTest( "test6" );
        }

        public void testTest7() throws IOException, InterruptedException {
                runTest( "test7" );
        }

        public void testTest8() throws IOException, InterruptedException {
                runTest( "test8" );
        }

        public void testArgtypes() throws IOException, InterruptedException {
                runTest( "argtypes" );
        }

        public void testAutoload() throws IOException, InterruptedException {
                runTest( "autoload" );
        }

        public void testBaseLib() throws IOException, InterruptedException {
                runTest( "baselib" );
        }
        
        public void testBoolean() throws IOException, InterruptedException {
                runTest( "boolean" );
        }

        public void testCalls() throws IOException, InterruptedException {
                runTest( "calls" );
        }

        public void testCoercions() throws IOException, InterruptedException {
                runTest( "coercions" );
        }
        
        public void testCoroutines() throws IOException, InterruptedException {
                runTest( "coroutines" );
        }
        
        public void testCompare() throws IOException, InterruptedException {
                runTest( "compare" );
        }

        public void testErrors() throws IOException, InterruptedException {
                runTest( "errors" );
        }

        public void testHugeTable() throws IOException, InterruptedException {
            runTest( "hugetable" );
        }

       public void testLoops() throws IOException, InterruptedException {
                runTest( "loops" );
       }
       
       public void testManyLocals() throws IOException, InterruptedException {
           	runTest( "manylocals" );
       }


        public void testMathLib() throws IOException, InterruptedException {
                runTest( "mathlib" );
        }

        public void testMetatables() throws IOException, InterruptedException {
                runTest( "metatables" );
        }

        public void testModule() throws IOException, InterruptedException {
                runTest( "module" );
        }

        public void testNext() throws IOException, InterruptedException {
                runTest( "next" );
        }

        public void testPcalls() throws IOException, InterruptedException {
                runTest( "pcalls" );
        }
        
        public void testRequire() throws IOException, InterruptedException {
                runTest( "require" );
        }
        
        public void testSelect() throws IOException, InterruptedException {
                runTest( "select" );
        }

        public void testSetfenv() throws IOException, InterruptedException {
                runTest( "setfenv" );
        }

        public void testSetlist() throws IOException, InterruptedException {
                runTest( "setlist" );
        }
        
        public void testSimpleMetatables() throws IOException, InterruptedException {
                runTest( "simplemetatables" );
        }
        
        public void testStack() throws IOException, InterruptedException {
            runTest( "stack" );
        }
        
        public void testStrLib() throws IOException, InterruptedException {
            runTest( "strlib" );
        }
        
        public void testSort() throws IOException, InterruptedException {
            runTest( "sort" );
        }

        public void testTable() throws IOException, InterruptedException {
                runTest( "table" );
        }

        public void testType() throws IOException, InterruptedException {
                runTest( "type" );
        }
        
        public void testUpvalues() throws IOException, InterruptedException {
                runTest( "upvalues" );
        }
        
        public void testUpvalues2() throws IOException, InterruptedException {
                runTest( "upvalues2" );
        }
        
        public void testUpvalues3() throws IOException, InterruptedException {
                runTest( "upvalues3" );
        }
        
//*/
        private void runTest( String testName ) throws IOException, InterruptedException {

                // new lua state 
                LuaState state = Platform.newLuaState();
                
                // install the compiler
                LuaC.install();
                
                // load the file
                LPrototype p = loadScriptResource( state, testName );
                p.source = LString.valueOf("stdin");
                
                // Replace System.out with a ByteArrayOutputStream
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BaseLib.redirectOutput( outputStream );
                try {
                        // create closure and execute
                        LClosure c = new LClosure( p, state._G );
                        state.doCall(c, new LValue[0]);
                        
                        final String actualOutput = new String( outputStream.toByteArray() );
                        final String expectedOutput = getExpectedOutput( testName );
                        
                        assertEquals( expectedOutput, actualOutput );
                } finally {
                        BaseLib.restoreStandardOutput();
                        outputStream.close();
                }
        }
        
        private LPrototype loadScriptResource( LuaState state, String name ) throws IOException {
                InputStream script = getClass().getResourceAsStream( "/"+name+".luac" );
                if ( script == null ) {
                        script = getClass().getResourceAsStream( "/"+name+".lua" );
                        if ( script == null ) {
                                fail( "Could not load script for test case: "+name );
                        }
                }
                
                try {
                        // Use "stdin" instead of resource name so that output matches
                        // standard Lua.
                        return LoadState.undump(state, script, "stdin");
                } finally {
                        script.close();
                }
        }
        
        private String getExpectedOutput( final String testName ) throws IOException, InterruptedException {
                String expectedOutputName = "/" + testName + "-expected.out";
                InputStream is = getClass().getResourceAsStream( expectedOutputName );
                if ( is != null ) {
                        try {
                                return readString( is );
                        } finally {
                                is.close();
                        }
                } else {
                        InputStream script;
//                      script = getClass().getResourceAsStream( "/" + testName + ".luac" );
//                      if ( script == null ) {
                                script = getClass().getResourceAsStream( "/" + testName + ".lua" );
                                if ( script == null ) {
                                        fail( "Could not find script for test case: "+testName );
                                }
//                      }
                        try {
                                return collectProcessOutput( new String[] { "lua", "-" }, script );
                        } finally {
                                script.close();
                        }
                }
        }
        
        private String collectProcessOutput( String[] cmd, final InputStream input ) throws IOException, InterruptedException {
                Runtime r = Runtime.getRuntime();
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final Process p = r.exec( cmd );
                try {
                        // start a thread to write the given input to the subprocess.
                        Thread inputCopier = (new Thread() {
                                public void run() {
                                        try {
                                                OutputStream processStdIn = p.getOutputStream();
                                                try {
                                                        copy( input, processStdIn );
                                                } finally {
                                                        processStdIn.close();
                                                }
                                        } catch ( IOException e ) {
                                                e.printStackTrace();
                                        }
                                }
                        });
                        inputCopier.start();
                        
                        // start another thread to read output from the subprocess.
                        Thread outputCopier = (new Thread() {
                                public void run() {
                                        try {
                                                InputStream processStdOut = p.getInputStream();
                                                try {
                                                        copy( processStdOut, baos );
                                                } finally {
                                                        processStdOut.close();
                                                }
                                        } catch ( IOException ioe ) {
                                                ioe.printStackTrace();
                                        }
                                }
                        });
                        outputCopier.start();
                        
                        // start another thread to read output from the subprocess.
                        Thread errorCopier = (new Thread() {
                                public void run() {
                                        try {
                                                InputStream processError = p.getErrorStream();
                                                try {
                                                        copy( processError, System.err );
                                                } finally {
                                                        processError.close();
                                                }
                                        } catch ( IOException ioe ) {
                                                ioe.printStackTrace();
                                        }
                                }
                        });
                        errorCopier.start();
                        
                        p.waitFor();
                        inputCopier.join();
                        outputCopier.join();
                        errorCopier.join();
                        
                        return new String( baos.toByteArray() );
                        
                } finally {
                        p.destroy();
                }
        }
        
        private String readString( InputStream is ) throws IOException {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                copy( is, baos );
                return new String( baos.toByteArray() );
        }
        
        private void copy( InputStream is, OutputStream os ) throws IOException {
                byte[] buf = new byte[ 1024 ];
                int r;
                while ( ( r = is.read( buf ) ) >= 0 ) {
                        os.write( buf, 0, r );
                }
        }

}
