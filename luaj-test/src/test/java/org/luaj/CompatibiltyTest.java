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
package org.luaj;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaNil;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.luajc.LuaJC;

/**
 * Compatibility tests for the Luaj VM
 * 
 * Results are compared for exact match with the installed C-based lua
 * environment.
 */
public class CompatibiltyTest {

	abstract static class CompatibiltyTestCase extends PlatformTestCase {
		LuaValue savedStringMetatable;

		@BeforeEach
		@Override
		protected void setUp() {
			savedStringMetatable = LuaString.s_metatable;
			setBaseDir("compatibility");
			super.setUp();
		}

		@AfterEach
		protected void tearDown() {
			LuaNil.s_metatable = null;
			LuaBoolean.s_metatable = null;
			LuaNumber.s_metatable = null;
			LuaFunction.s_metatable = null;
			LuaThread.s_metatable = null;
			LuaString.s_metatable = savedStringMetatable;
		}

		@Test
		void testBaseLib() { runTest("baselib"); }

		@Test
		void testCoroutineLib() { runTest("coroutinelib"); }

		@Disabled("Too many failing tests")
		@Test
		void testDebugLib() { runTest("debuglib"); }

		@Test
		void testErrors() { runTest("errors"); }

		@Test
		void testFunctions() { runTest("functions"); }

		@Test
		void testIoLib() { runTest("iolib"); }

		@Test
		void testManyUpvals() { runTest("manyupvals"); }

		@Test
		void testMathLib() { runTest("mathlib"); }

		@Test
		void testMetatags() { runTest("metatags"); }

		@Test
		void testOsLib() { runTest("oslib"); }

		@Test
		void testStringLib() { runTest("stringlib"); }

		@Test
		void testTableLib() { runTest("tablelib"); }

		@Test
		void testTailcalls() { runTest("tailcalls"); }

		@Test
		void testUpvalues() { runTest("upvalues"); }

		@Test
		void testVm() { runTest("vm"); }
	}

	@Nested
	public static class JmeCompatibilityTest extends CompatibiltyTestCase {

		@BeforeEach
		@Override
		protected void setUp() {
			setPlatform(PlatformTestCase.PlatformType.JME);
			System.setProperty("JME", "true");
			super.setUp();
		}

		// Emulator cannot create files for writing
		@Override
		void testIoLib() {}
	}

	@Nested
	public static class JseCompatibilityTest extends CompatibiltyTestCase {

		@BeforeEach
		@Override
		protected void setUp() {
			setPlatform(PlatformTestCase.PlatformType.JSE);
			System.setProperty("JME", "false");
			super.setUp();
		}
	}

	@Nested
	public static class LuaJCCompatibilityTest extends CompatibiltyTestCase {

		@BeforeEach
		@Override
		protected void setUp() {
			setPlatform(PlatformTestCase.PlatformType.LUAJIT);
			System.setProperty("JME", "false");
			super.setUp();
			LuaJC.install(globals);
		}

		// not supported on this platform - don't test
		@Override
		void testDebugLib() {}
	}
}
