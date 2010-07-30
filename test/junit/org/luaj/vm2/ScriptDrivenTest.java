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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.luajc.LuaJC;

abstract
public class ScriptDrivenTest extends TestCase {
	public static final boolean nocompile = "true".equals(System.getProperty("nocompile"));

	public enum PlatformType {
		JME, JSE, LUAJIT, LUA2JAVA,
	}
	
	private final PlatformType platform;
	private final String basedir;
	private LuaTable _G;
	
	protected ScriptDrivenTest( PlatformType platform, String directory ) {
		this.platform = platform;
		this.basedir = directory;
		initGlobals();
	}
	
	private void initGlobals() {
		switch ( platform ) {
		default:
		case JSE:
		case LUAJIT:
		case LUA2JAVA:
			_G = org.luaj.vm2.lib.jse.JsePlatform.debugGlobals();
			break;
		case JME:
			_G = org.luaj.vm2.lib.jme.JmePlatform.debugGlobals();
			break;
		}
	}
	
	
	protected void setUp() throws Exception {
		super.setUp();
		initGlobals();
	}

	// */
	protected void runTest(String testName) {
		try {
			// override print()
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			final PrintStream oldps = BaseLib.instance.STDOUT;
			final PrintStream ps = new PrintStream( output );
			BaseLib.instance.STDOUT = ps;
	
			// run the script
			try {
				LuaValue chunk = loadScript(testName, _G);
				chunk.call(LuaValue.valueOf(platform.toString()));
	
				ps.flush();
				String actualOutput = new String(output.toByteArray());
				String expectedOutput = getExpectedOutput(testName);
				actualOutput = actualOutput.replaceAll("\r\n", "\n");
				expectedOutput = expectedOutput.replaceAll("\r\n", "\n");
	
				assertEquals(expectedOutput, actualOutput);
			} finally {
				BaseLib.instance.STDOUT = oldps;
				ps.close();
			}
		} catch ( IOException ioe ) {
			throw new RuntimeException(ioe.toString());
		} catch ( InterruptedException ie ) {
			throw new RuntimeException(ie.toString());
		}
	}

	protected LuaValue loadScript(String name, LuaTable _G) throws IOException {
		File file = new File(basedir+"/"+name+".lua");
		if ( !file.exists() )
			fail("Could not load script for test case: " + name);

		InputStream script=null;
		try {
			// Use "stdin" instead of resource name so that output matches
			// standard Lua.
			switch ( this.platform ) {
			case LUAJIT:
				if ( nocompile ) {
					LuaValue c = (LuaValue) Class.forName(name).newInstance();
					c.setfenv(_G);
					return c;
				} else {
					script = new FileInputStream(file);
					return LuaJC.getInstance().load( script, name, _G);
				}
			default:
				script = new FileInputStream(file);
				return LoadState.load(script, "=stdin", _G);
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			throw new IOException( e.toString() );
		} finally {
			if ( script != null )
				script.close();
		}
	}

	private String getExpectedOutput(final String name) throws IOException,
			InterruptedException {
		String expectedOutputName = basedir+"/"+name+"-expected.out";
		File file = new File( expectedOutputName );
		if ( file.exists() ) {
			InputStream is = new FileInputStream(file);
			try {
				return readString(is);
			} finally {
				is.close();
			}
		} else {
			file = new File(basedir+"/"+name+".lua");
			if ( !file.exists() )
				fail("Could not load script for test case: " + name);
			InputStream script = new FileInputStream(file);
			// }
			try {
			    String luaCommand = System.getProperty("LUA_COMMAND");
			    if ( luaCommand == null )
			        luaCommand = "lua";
			    String[] args = new String[] { luaCommand, "-", platform.toString() };
				return collectProcessOutput(args, script);
			} finally {
				script.close();
			}
		}
	}

	public static String collectProcessOutput(String[] cmd, final InputStream input)
			throws IOException, InterruptedException {
		Runtime r = Runtime.getRuntime();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final Process p = r.exec(cmd);
		try {
			// start a thread to write the given input to the subprocess.
			Thread inputCopier = (new Thread() {
				public void run() {
					try {
						OutputStream processStdIn = p.getOutputStream();
						try {
							copy(input, processStdIn);
						} finally {
							processStdIn.close();
						}
					} catch (IOException e) {
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
							copy(processStdOut, baos);
						} finally {
							processStdOut.close();
						}
					} catch (IOException ioe) {
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
							copy(processError, System.err);
						} finally {
							processError.close();
						}
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			});
			errorCopier.start();

			p.waitFor();
			inputCopier.join();
			outputCopier.join();
			errorCopier.join();

			return new String(baos.toByteArray());

		} finally {
			p.destroy();
		}
	}

	private String readString(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(is, baos);
		return new String(baos.toByteArray());
	}

	private static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
		int r;
		while ((r = is.read(buf)) >= 0) {
			os.write(buf, 0, r);
		}
	}

}
