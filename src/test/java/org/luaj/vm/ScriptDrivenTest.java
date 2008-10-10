package org.luaj.vm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.luaj.compiler.LuaC;
import org.luaj.lib.BaseLib;
import org.luaj.platform.J2sePlatform;

abstract
public class ScriptDrivenTest extends TestCase {

	private final String basedir;
	
	protected ScriptDrivenTest( String directory ) {
		basedir = directory;
	}
	
	// */
	protected void runTest(String testName) throws IOException,
			InterruptedException {

		// set platform relative to directory
		Platform.setInstance(new J2sePlatform());
		
		// new lua state
		LuaState state = Platform.newLuaState();

		// install the compiler
		LuaC.install();

		// load the file
		LPrototype p = loadScript(state, testName);
		p.source = LString.valueOf("stdin");

		// Replace System.out with a ByteArrayOutputStream
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BaseLib.redirectOutput(outputStream);
		try {
			// create closure and execute
			LClosure c = p.newClosure(state._G);
			state.pushlvalue(c);
			state.call(0, 0);

			final String actualOutput = new String(outputStream.toByteArray());
			final String expectedOutput = getExpectedOutput(testName);

			assertEquals(expectedOutput, actualOutput);
		} finally {
			BaseLib.restoreStandardOutput();
			outputStream.close();
		}
	}

	protected LPrototype loadScript(LuaState state, String name)
			throws IOException {
		File file = new File(basedir+"/"+name+".luac");
		if ( !file.exists() )
			file = new File(basedir+"/"+name+".lua");
		if ( !file.exists() )
			fail("Could not load script for test case: " + name);

		InputStream script = new FileInputStream(file);
		try {
			// Use "stdin" instead of resource name so that output matches
			// standard Lua.
			return LoadState.undump(state, script, "stdin");
		} finally {
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
				return collectProcessOutput(new String[] { luaCommand, "-" }, script);
			} finally {
				script.close();
			}
		}
	}

	private String collectProcessOutput(String[] cmd, final InputStream input)
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

	private void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
		int r;
		while ((r = is.read(buf)) >= 0) {
			os.write(buf, 0, r);
		}
	}

}
