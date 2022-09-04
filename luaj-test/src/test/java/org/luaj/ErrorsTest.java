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

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test argument type check errors
 * 
 * Results are compared for exact match with the installed C-based lua
 * environment.
 */
class ErrorsTest extends PlatformTestCase {

	@BeforeEach
	@Override
	protected void setUp() {
		setBaseDir("errors");
		setPlatform(PlatformTestCase.PlatformType.JSE);
		super.setUp();
	}

	@Test
	void testBaseLibArgs() {
		globals.STDIN = new InputStream() {
			@Override
			public int read() throws IOException {
				return -1;
			}
		};
		runTest("baselibargs");
	}

	@Test
	void testCoroutineLibArgs() { runTest("coroutinelibargs"); }

	@Disabled("Too many failing tests")
	@Test
	void testDebugLibArgs() { runTest("debuglibargs"); }

	@Test
	void testIoLibArgs() { runTest("iolibargs"); }

	@Test
	void testMathLibArgs() { runTest("mathlibargs"); }

	@Test
	void testModuleLibArgs() { runTest("modulelibargs"); }

	@Test
	void testOperators() { runTest("operators"); }

	@Test
	void testStringLibArgs() { runTest("stringlibargs"); }

	@Test
	void testTableLibArgs() { runTest("tablelibargs"); }

}
