/*******************************************************************************
 * Copyright (c) 2015 Luaj.org. All rights reserved.
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

import java.io.InputStream;
import java.io.Reader;

import junit.framework.TestCase;

import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.server.Launcher;
import org.luaj.vm2.server.LuajClassLoader;

// Tests using class loading orders that have caused problems for some use cases.
public class LoadOrderTest extends TestCase {

	public void testLoadGlobalsFirst() {
		Globals g = JsePlatform.standardGlobals();
		assertNotNull(g);
	}

	public void testLoadStringFirst() {
		LuaString BAR = LuaString.valueOf("bar");
		assertNotNull(BAR);
	}

	public static class TestLauncherLoadStringFirst implements Launcher {
		// Static initializer that causes LuaString->LuaValue->LuaString
		private static final LuaString FOO = LuaString.valueOf("foo");

		public Object[] launch(String script, Object[] arg) {
			return new Object[] { FOO };
		}

		public Object[] launch(InputStream script, Object[] arg) {
			return null;
		}

		public Object[] launch(Reader script, Object[] arg) {
			return null;
		}
	}

	public void testClassLoadsStringFirst() throws Exception {
		Launcher launcher = LuajClassLoader
				.NewLauncher(TestLauncherLoadStringFirst.class);
		Object[] results = launcher.launch("foo", null);
		assertNotNull(results);
	}

}
