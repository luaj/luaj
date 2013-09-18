/*******************************************************************************
 * Copyright (c) 2012 Luaj.org. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;

import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ResourceFinder;

/**
 * Global environment used by luaj.
 * <p>
 * Contains the global variables referenced by lua libraries such as stdin and stdout, 
 * the resrouce finder which is used tolook up files in a platform independent way, 
 * the installed lua compiler, the math library in use, debugging calls stack, and so on.  
 * <p>
 * In a multithreded server environment, each server thread should create one Globals instance, 
 * which will be logically distance and not interfere with each other, but share certain 
 * static immutable resources such as class data and string data.
 * <p>
 * Typically, this is constructed indirectly by a call to 
 * {@link JsePlatform.standardGlobasl()} or {@link JmePlatform.standardGlobals()}, 
 * and then used to load lua scripts for execution as in the following example. 
 * <pre> {@code
 * Globals _G = JsePlatform.standardGlobals();
 * _G.compiler.load( new ByteArrayInputStream("print 'hello'".getBytes()), "main.lua", _G ).call();
 * } </pre>
 * @see LuaCompiler
 * @see JsePlatform
 * @see JmePlatform
 * @see LuaValue
 *  
 */
public class Globals extends LuaTable {

	/** The current default input stream. */
	public InputStream STDIN  = null;

	/** The current default output stream. */
	public PrintStream STDOUT = System.out;

	/** The current default error stream. */
	public PrintStream STDERR = System.err;

	/** The installed ResourceFinder for looking files by name. */
	public ResourceFinder FINDER;
	
	/** The currently running thread.  Should not be changed by non-library code. */
	public LuaThread running = new LuaThread(this);

	/** The BaseLib instance loaded into this Globals */
	public BaseLib baselib;
	
	/** The PackageLib instance loaded into this Globals */
	public PackageLib package_;
	
	/** The DebugLib instance loaded into this Globals, or null if debugging is not enabled */
	public DebugLib debuglib;

	/** The current error handler for this Globals */
	public LuaValue errorfunc;

	/** Interface for module that converts a Prototype into a LuaFunction with an environment. */
	public interface Loader {
		/** Convert the prototype into a LuaFunction with the supplied environment. */
		LuaFunction load(Prototype prototype, String chunkname, LuaValue env) throws IOException;
	}

	/** Interface for module that converts lua source text into a prototype. */
	public interface Compiler {
		/** Compile lua source into a Prototype. The InputStream is assumed to be in UTF-8. */
		Prototype compile(InputStream stream, String chunkname) throws IOException;
	}

	/** Interface for module that loads lua binary chunk into a prototype. */
	public interface Undumper {
		/** Load the supplied input stream into a prototype. */
		Prototype undump(InputStream stream, String chunkname) throws IOException;
	}
	
	/** Check that this object is a Globals object, and return it, otherwise throw an error. */
	public Globals checkglobals() {
		return this;
	}
	
	/** The installed loader. */
	public Loader loader;

	/** The installed compiler. */
	public Compiler compiler;

	/** The installed undumper. */
	public Undumper undumper;

	/** Convenience function for loading a file that is either binary lua or lua source.
	 * @param filename Name of the file to load.
	 * @return LuaValue that can be call()'ed or invoke()'ed.
	 * @throws LuaError if the file could not be loaded.
	 */
	public LuaValue loadfile(String filename) {
		try {
			return load(FINDER.findResource(filename), "@"+filename, "bt", this);
		} catch (Exception e) {
			return error("load "+filename+": "+e);
		}
	}

	/** Convenience function to load a string value as a script.  Must be lua source.
	 * @param script Contents of a lua script, such as "print 'hello, world.'"
	 * @param chunkname Name that will be used within the chunk as the source.
	 * @return LuaValue that may be executed via .call(), .invoke(), or .method() calls.
	 * @throws LuaError if the script could not be compiled.
	 */
	public LuaValue load(String script, String chunkname) {
		return load(new StrReader(script), chunkname);
	}
	
	/** Load the content form a reader as a text file.  Must be lua source. 
	 * The source is converted to UTF-8, so any characters appearing in quoted literals 
	 * above the range 128 will be converted into multiple bytes.  */
	public LuaValue load(Reader reader, String chunkname) {
		return load(new UTF8Stream(reader), chunkname, "t", this);
	}

	/** Load the content form an input stream as a binary chunk or text file. */
	public LuaValue load(InputStream is, String chunkname, String mode, LuaValue env) {
		try {
			Prototype p = loadPrototype(is, chunkname, mode);
			return loader.load(p, chunkname, env);
		} catch (LuaError l) {
			throw l;
		} catch (Exception e) {
			return error("load "+chunkname+": "+e);
		}
	}

	/** Load lua source or lua binary from an input stream into a Prototype. 
	 * The InputStream is either a binary lua chunk starting with the lua binary chunk signature, 
	 * or a text input file.  If it is a text input file, it is interpreted as a UTF-8 byte sequence.  
	 */
	public Prototype loadPrototype(InputStream is, String chunkname, String mode) throws IOException {
		if (mode.indexOf('b') >= 0) {
			if (undumper == null)
				error("No undumper.");
			if (!is.markSupported())
				is = new MarkStream(is);
			is.mark(4);
			final Prototype p = undumper.undump(is, chunkname);
			if (p != null)
				return p;
			is.reset();
		}
		if (mode.indexOf('t') >= 0) {
			return compilePrototype(is, chunkname);
		}
		error("Failed to load prototype "+chunkname+" using mode '"+mode+"'");
		return null;
	}
	
	/** Compile lua source from a Reader into a Prototype. The characters in the reader 
	 * are converted to bytes using the UTF-8 encoding, so a string literal containing 
	 * characters with codepoints 128 or above will be converted into multiple bytes. 
	 */
	public Prototype compilePrototype(Reader reader, String chunkname) throws IOException {
		return compilePrototype(new UTF8Stream(reader), chunkname);
	}
	
	/** Compile lua source from an InputStream into a Prototype. 
	 * The input is assumed to be UTf-8, but since bytes in the range 128-255 are passed along as 
	 * literal bytes, any ASCII-compatible encoding such as ISO 8859-1 may also be used.  
	 */
	public Prototype compilePrototype(InputStream stream, String chunkname) throws IOException {
		if (compiler == null)
			error("No compiler.");
		return compiler.compile(stream, chunkname);
	}

	/** Function which yields the current thread. 
	 * @param args  Arguments to supply as return values in the resume function of the resuming thread.
	 * @return Values supplied as arguments to the resume() call that reactivates this thread.
	 */
	public Varargs yield(Varargs args) {
		if (running == null || running.isMainThread())
			throw new LuaError("cannot yield main thread");
		final LuaThread.State s = running.state;
		return s.lua_yield(args);
	}

	/** Reader implementation to read chars from a String in JME or JSE. */
	static class StrReader extends Reader {
		final String s;
		int i = 0, n;
		StrReader(String s) {
			this.s = s;
			n = s.length();
		}
		public void close() throws IOException {
			i = n;
		}
		public int read(char[] cbuf, int off, int len) throws IOException {
			int j = 0;
			for (; j < len && i < n; ++j, ++i)
				cbuf[off+j] = s.charAt(i);
			return j > 0 || len == 0 ? j : -1;
		}
	}

	/**  Simple converter from Reader to InputStream using UTF8 encoding that will work
	 * on both JME and JSE.
	 */
	static class UTF8Stream extends InputStream {
		final char[] c = new char[32];
		final byte[] b = new byte[96];
		int i = 0, j = 0;
		final Reader r;
		UTF8Stream(Reader r) {
			this.r = r;
		}
		public int read() throws IOException {
			if (i < j)
				return c[i++];
			int n = r.read(c);
			if (n < 0)
				return -1;
			j = LuaString.encodeToUtf8(c, n, b, i = 0);
			return b[i++];
		}
	}
	
	/** Simple InputStream that supports mark.
	 * Used to examine an InputStream for a 4-byte binary lua signature, 
	 * and fall back to text input when the signature is not found.
	 */
	static class MarkStream extends InputStream {
		private int[] b;
		private int i = 0, j = 0;
		private final InputStream s;
		MarkStream(InputStream s) {
			this.s = s;
		}
		public int read() throws IOException {
			if (i < j)
				return b[i++];
			final int c = s.read();
			if (c < 0)
				return -1;
			if (j < b.length) {
				b[j++] = c;
				i = j;
			}
			return c;
		}
		public synchronized void mark(int n) {
			b = new int[n];
			i = j = 0;
		}
		public boolean markSupported() {
			return true;
		}
		public synchronized void reset() throws IOException {
			i = 0;
		}
	}

}
