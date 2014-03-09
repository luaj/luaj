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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.jse.JseProcess;
import org.luaj.vm2.luajc.LuaJC;

abstract
public class ScriptDrivenTest extends TestCase implements ResourceFinder {
	public static final boolean nocompile = "true".equals(System.getProperty("nocompile"));

	public enum PlatformType {
		JME, JSE, LUAJIT,
	}
	
	private final PlatformType platform;
	private final String subdir;
	protected Globals globals;
	
	static final String zipdir = "test/lua/";
	static final String zipfile = "luaj3.0-tests.zip";

	protected ScriptDrivenTest( PlatformType platform, String subdir ) {
		this.platform = platform;
		this.subdir = subdir;
		initGlobals();
	}
	
	private void initGlobals() {
		switch ( platform ) {
		default:
		case JSE:
		case LUAJIT:
			globals = org.luaj.vm2.lib.jse.JsePlatform.debugGlobals();
			break;
		case JME:
			globals = org.luaj.vm2.lib.jme.JmePlatform.debugGlobals();
			break;
		}
	}
	
	
	protected void setUp() throws Exception {
		super.setUp();
		initGlobals();
		globals.finder = this;
	}

	// ResourceFinder implementation.
	public InputStream findResource(String filename) {
		InputStream is = findInPlainFile(filename);
		if (is != null) return is;
		is = findInPlainFileAsResource("",filename);
		if (is != null) return is;
		is = findInPlainFileAsResource("/",filename);
		if (is != null) return is;
		is = findInZipFileAsPlainFile(filename);
		if (is != null) return is;
		is = findInZipFileAsResource("",filename);
		if (is != null) return is;
		is = findInZipFileAsResource("/",filename);
		return is;
	}

	private InputStream findInPlainFileAsResource(String prefix, String filename) {
		return getClass().getResourceAsStream(prefix + subdir + filename);
	}

	private InputStream findInPlainFile(String filename) {
		try {
			File f = new File(zipdir+subdir+filename);
			if (f.exists())
				return new FileInputStream(f);
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}
		return null;
	}

	private InputStream findInZipFileAsPlainFile(String filename) {
		URL zip;
    	File file = new File(zipdir+zipfile);
		try {
	    	if ( file.exists() ) {
				zip = file.toURI().toURL();
				String path = "jar:"+zip.toExternalForm()+ "!/"+subdir+filename;
				URL url = new URL(path);
				return url.openStream();
	    	}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// Ignore and return null.
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}


	private InputStream findInZipFileAsResource(String prefix, String filename) {
    	URL zip = null;
		zip = getClass().getResource(zipfile);
		if ( zip != null ) 
			try {
				String path = "jar:"+zip.toExternalForm()+ "!/"+subdir+filename;
				URL url = new URL(path);
				return url.openStream();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		return null;
	}
	
	// */
	protected void runTest(String testName) {
		try {
			// override print()
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			final PrintStream oldps = globals.STDOUT;
			final PrintStream ps = new PrintStream( output );
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
	
				assertEquals(expectedOutput, actualOutput);
			} finally {
				globals.STDOUT = oldps;
				ps.close();
			}
		} catch ( IOException ioe ) {
			throw new RuntimeException(ioe.toString());
		} catch ( InterruptedException ie ) {
			throw new RuntimeException(ie.toString());
		}
	}

	protected LuaValue loadScript(String name, Globals globals) throws IOException {
		InputStream script = this.findResource(name+".lua");
		if ( script == null )
			fail("Could not load script for test case: " + name);
		try {
			switch ( this.platform ) {
			case LUAJIT:
				if ( nocompile ) {
					LuaValue c = (LuaValue) Class.forName(name).newInstance();
					return c;
				} else {
					LuaJC.install(globals);
					return globals.load(script, name, "bt", globals);
				}
			default:
				return globals.load(script, "@"+name+".lua", "bt", globals);
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			throw new IOException( e.toString() );
		} finally {
			script.close();
		}
	}

	private String getExpectedOutput(final String name) throws IOException,
			InterruptedException {
		InputStream output = this.findResource(name+".out");
		if (output != null)
			try {
				return readString(output);
			} finally {
				output.close();
			}
 		String expectedOutput = executeLuaProcess(name);
 		if (expectedOutput == null) 
 			throw new IOException("Failed to get comparison output or run process for "+name);
 		return expectedOutput;
	}

	private String executeLuaProcess(String name) throws IOException, InterruptedException {
		InputStream script = findResource(name+".lua");
		if ( script == null )
			throw new IOException("Failed to find source file "+script);
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
	
	public static String collectProcessOutput(String[] cmd, final InputStream input)
			throws IOException, InterruptedException {
		Runtime r = Runtime.getRuntime();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new JseProcess(cmd, input, baos, System.err).waitFor();
		return new String(baos.toByteArray());
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
