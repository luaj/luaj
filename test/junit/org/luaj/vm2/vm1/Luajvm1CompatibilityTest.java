/*******************************************************************************
 * Copyright (c) 2010 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm2.vm1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ScriptDrivenTest;

/**
 * Test for compatiblity between luaj 1.0 and 2.0.
 * 
 * Runs an archive of the tests that were used in 
 * the luaj-vm 1.0 development, modified to account 
 * for changes to interpetation of the lua vm spec. 
 */
public class Luajvm1CompatibilityTest extends TestCase {

	private static final String zipfile = "luajvm1-tests.zip";
	private static String jarpath;

	protected void runTest(String test) {
		try {
	    	URL zip = null;
			zip = getClass().getResource(zipfile);
			if ( zip == null ) {
		    	File file = new File("test/junit/org/luaj/vm2/vm1/"+zipfile);
				try {
			    	if ( file.exists() )
						zip = file.toURI().toURL();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			if ( zip == null )
				throw new RuntimeException("not found: "+zipfile);			
			jarpath = "jar:"+zip.toExternalForm()+"!/";
			String lua = luaRun(test);
			String luaj20 = luaj20Run(test);
			lua = lua.replaceAll("\r\n", "\n");
			luaj20 = luaj20.replaceAll("\r\n", "\n");
			assertEquals( lua, luaj20 );
		} catch ( Exception ioe ) {
			fail( ioe.toString() );
		}
	}
	
	private static InputStream open(String file) {
		try {
			File f = new File(file);
			return f.exists()? 
				new FileInputStream(f): 
				new URL(jarpath+file).openStream();
		} catch ( Exception e ) {
			return null;
		}
	}

	private String luaRun(String test) throws Exception {
		InputStream script =open(test+".lua");
		if ( script == null )
			fail("Could not load script for test case: " + test);
		try {
		    String luaCommand = System.getProperty("LUA_COMMAND");
		    if ( luaCommand == null )
		        luaCommand = "lua";
		    String[] args = new String[] { luaCommand, "-", "jse" };
			return ScriptDrivenTest.collectProcessOutput(args, script);
		} finally {
			script.close();
		}
	}

	/*
	private String luaj10Run(String test) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			org.luaj.vm.Platform.setInstance(new org.luaj.platform.J2sePlatform() {
				public InputStream openFile(String fileName) {
					return open( fileName );
				}
			});
			org.luaj.vm.LuaState vm = org.luaj.vm.Platform.newLuaState();
			org.luaj.compiler.LuaC.install();
			org.luaj.lib.DebugLib.install( vm );
			org.luaj.lib.BaseLib.redirectOutput(outputStream);
			vm.getglobal("require");
			vm.pushstring(test);
			vm.call(1,0);
			return outputStream.toString();
		} finally {
			org.luaj.lib.BaseLib.restoreStandardOutput();
			outputStream.close();
		}
	}
	*/
	
	private String luaj20Run(String test) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream( outputStream );
		try {
			org.luaj.vm2.LuaTable _G = org.luaj.vm2.lib.JsePlatform.debugGlobals();
			LuaThread.getRunning().setfenv(_G);
			LuaValue loader = new org.luaj.vm2.lib.OneArgFunction(_G) {
				public LuaValue call(LuaValue arg) {
					String name = arg.tojstring();
					String file = name + ".lua";
					InputStream is = open( file );
					if ( is == null )
						return LuaValue.valueOf("not found: "+file);
					try {
						return org.luaj.vm2.LoadState.load(is, "@stdin", env);
					} catch (IOException e) {
						return LuaValue.valueOf(e.toString());
					} finally {
						try { is.close(); } catch ( IOException ioe ) {}
					}
				}
			};
			LuaTable loaders = _G.get("package").get("loaders").checktable();
			loaders.insert(1, loader);
			org.luaj.vm2.lib.BaseLib.instance.STDOUT = printStream;
			loader.call(LuaValue.valueOf(test)).invoke(LuaValue.valueOf(test));
		} finally {
			printStream.close();
		}
		return outputStream.toString();
	}

	public Luajvm1CompatibilityTest() {
	}

	public void testTest1() { runTest("test1"); }
	public void testTest2() { runTest("test2"); }
	public void testTest3() { runTest("test3"); }
	public void testTest4() { runTest("test4"); }
	public void testTest5() { runTest("test5"); }
	public void testTest6() { runTest("test6"); }
	public void testTest8() { runTest("test8"); }
	public void testTest9() { runTest("test9"); }
	public void testAutoload() { runTest("autoload"); }
	public void testBaseLib() { runTest("baselib"); }
	public void testBoolean() { runTest("boolean"); }
	public void testCalls() { runTest("calls"); }
	public void testCoercions() { runTest("coercions"); }
	public void testCoroutines() { runTest("coroutines"); }
	public void testCompare() { runTest("compare"); }
	public void testDebugLib() { runTest("debuglib"); }
	public void testErrors() { runTest("errors"); }
	public void testHugeTable() { runTest("hugetable"); }
	public void testIoLib() { runTest("iolib"); }
	public void testLoops() { runTest("loops"); }
	public void testManyLocals() { runTest("manylocals"); }
	public void testMathLib() { runTest("mathlib"); }
	public void testMetatables() { runTest("metatables"); }
	public void testModule() { runTest("module"); }
	public void testNext() { runTest("next"); }
	public void testOsLib() { runTest("oslib"); }
	public void testPcalls() { runTest("pcalls"); }
	public void testPrint() { runTest("print"); }
	public void testRequire() { runTest("require"); }
	public void testSelect() { runTest("select"); }
	public void testSetfenv() { runTest("setfenv"); }
	public void testSetlist() { runTest("setlist"); }
	public void testSimpleMetatables() { runTest("simplemetatables"); }
	public void testStack() { runTest("stack"); }
	public void testStrLib() { runTest("strlib"); }
	public void testSort() { runTest("sort"); }
	public void testTable() { runTest("table"); }
	public void testTailcall() { runTest("tailcall"); }
	public void testType() { runTest("type"); }
	public void testUpvalues() { runTest("upvalues"); }
	public void testUpvalues2() { runTest("upvalues2"); }
	public void testUpvalues3() { runTest("upvalues3"); }
	public void testVarargs() { runTest("varargs"); }
	public void testWeakTable() { runTest("weaktable"); }
}
