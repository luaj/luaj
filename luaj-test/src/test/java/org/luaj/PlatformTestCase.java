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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;

import org.junit.jupiter.api.BeforeEach;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jme.JmePlatform;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.lib.jse.JseProcess;
import org.luaj.vm2.luajc.LuaJC;

abstract class PlatformTestCase extends ResourcesTestCase {
	public static final boolean nocompile = "true".equals(System.getProperty("nocompile"));

	public enum PlatformType {
		JME, JSE, LUAJIT,
	}

	private PlatformType platform;

	private void initGlobals() {
		switch (platform) {
		default:
		case JSE:
		case LUAJIT:
			globals = JsePlatform.debugGlobals();
			break;
		case JME:
			globals = JmePlatform.debugGlobals();
			break;
		}
	}

	@BeforeEach
	@Override
	protected void setUp() {
		initGlobals();
		globals.finder = filename -> {
			try {
				return inputStreamOfFile(filename);
			} catch (IOException e) {
				return null;
			}
		};
	}

	protected void setPlatform(PlatformType platform) { this.platform = platform; }

	protected void runTest(String testName) {
		try {
			// override print()
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			final PrintStream oldps = globals.STDOUT;
			final PrintStream ps = new PrintStream(output);
			globals.STDOUT = ps;

			// run the script
			try {
				LuaValue chunk = loadScript(testName, globals);
				chunk.call(LuaValue.valueOf(platform.toString()));

				ps.flush();
				String actualOutput = new String(output.toByteArray());
				String expectedOutput = getExpectedOutput(testName);
				actualOutput = actualOutput.replaceAll("\r\n", "\n");
				expectedOutput = expectedOutput.replaceAll("\r\n", "\n");

				if (!expectedOutput.equals(actualOutput))
					Files.write(new File(testName + ".out").toPath(), actualOutput.getBytes(), new OpenOption[0]);
				assertEquals(expectedOutput, actualOutput);
			} finally {
				globals.STDOUT = oldps;
				ps.close();
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		} catch (InterruptedException ie) {
			throw new RuntimeException(ie.toString());
		}
	}

	private LuaValue loadScript(String name, Globals globals) throws IOException {
		InputStream script = inputStreamOfLua(name);
		if (script == null)
			fail("Could not load script for test case: " + name);
		try {
			switch (this.platform) {
			case LUAJIT:
				if (nocompile) {
					LuaValue c = (LuaValue) Class.forName(name).newInstance();
					return c;
				} else {
					LuaJC.install(globals);
					return globals.load(script, name, "bt", globals);
				}
			default:
				return globals.load(script, "@" + name + ".lua", "bt", globals);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.toString());
		} finally {
			script.close();
		}
	}

	private String getExpectedOutput(final String name) throws IOException, InterruptedException {
		InputStream output = inputStreamOfResult(platform.name().toLowerCase() + "/" + name);
		if (output != null)
			try {
				return readString(output);
			} finally {
				output.close();
			}
		String expectedOutput = executeLuaProcess(name);
		if (expectedOutput == null)
			throw new IOException("Failed to get comparison output or run process for " + name);
		return expectedOutput;
	}

	private String executeLuaProcess(String name) throws IOException, InterruptedException {
		InputStream script = inputStreamOfLua(name);
		if (script == null)
			throw new IOException("Failed to find source file " + script);
		try {
			String luaCommand = System.getProperty("LUA_COMMAND");
			if (luaCommand == null)
				luaCommand = "lua";
			String[] args = new String[] { luaCommand, "-", platform.toString() };
			return collectProcessOutput(args, script);
		} finally {
			script.close();
		}
	}

	private static String collectProcessOutput(String[] cmd, final InputStream input)
		throws IOException, InterruptedException {
		Runtime r = Runtime.getRuntime();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new JseProcess(cmd, input, baos, System.err).waitFor();
		return new String(baos.toByteArray());
	}

	private static String readString(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(is, baos);
		return new String(baos.toByteArray());
	}

	private static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
		int r;
		while ( (r = is.read(buf)) >= 0 ) {
			os.write(buf, 0, r);
		}
	}

}
