/*******************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
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
package org.luaj.vm2;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.luajc.LuaJC;

/**
 * Compatibility tests for the Luaj VM
 * 
 * Results are compared for exact match with 
 * the installed C-based lua environment. 
 */
public class CompatibiltyTest {

	private static final String dir = "test/lua";
	
	abstract protected static class CompatibiltyTestSuite extends ScriptDrivenTest {	
		protected CompatibiltyTestSuite(PlatformType platform) {
			super(platform,dir);
		}
		
		public void testBaseLib()       { runTest("baselib");   }
		public void testCoroutineLib()  { runTest("coroutinelib"); }	
		public void testDebugLib()      { runTest("debuglib"); }	
		public void testErrors()        { runTest("errors"); }	
		public void testFunctions()     { runTest("functions"); }	
		public void testIoLib()         { runTest("iolib");     }
		public void testMathLib()       { runTest("mathlib"); }
		public void testOsLib()         { runTest("oslib"); }
		public void testStringLib()     { runTest("stringlib"); }
		public void testTableLib()      { runTest("tablelib"); }
		public void testTailcalls()     { runTest("tailcalls"); }
		public void testUpvalues()      { runTest("upvalues"); }
		public void testVm()            { runTest("vm"); }
	}


	public static Test suite() {
		TestSuite suite = new TestSuite("Compatibility Tests");
		suite.addTest( new TestSuite( JseCompatibilityTest.class,   "JSE Tests" ) );
		suite.addTest( new TestSuite( JmeCompatibilityTest.class,   "JME Tests" ) );
		suite.addTest( new TestSuite( JseBytecodeTest.class,        "JSE Bytecode Tests" ) );
		return suite;
	}

	public static class JmeCompatibilityTest extends CompatibiltyTestSuite {
		public JmeCompatibilityTest() {
			super(ScriptDrivenTest.PlatformType.JME);
		}
		protected void setUp() throws Exception {
			System.setProperty("JME", "true");
			super.setUp();
		}
	}
	public static class JseCompatibilityTest extends CompatibiltyTestSuite {
		public JseCompatibilityTest() {
			super(ScriptDrivenTest.PlatformType.JSE);
		}
		protected void setUp() throws Exception {
			super.setUp();
			System.setProperty("JME", "false");
			LuaC.install();
		}
	}
	public static class JseBytecodeTest extends CompatibiltyTestSuite {
		public JseBytecodeTest() {
			super(ScriptDrivenTest.PlatformType.LUAJIT);
		}
		protected void setUp() throws Exception {
			super.setUp();
			System.setProperty("JME", "false");
			LuaJC.install();
		}
		// not supported on this platform - don't test
		public void testDebugLib()      {}	
	}
}
