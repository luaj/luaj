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

import java.io.InputStream;
import java.io.PrintStream;

import org.luaj.vm2.LoadState.LuaCompiler;
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
	
	/** The installed compiler. */
	public LuaCompiler compiler = null;

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

	/** Check that this object is a Globals object, and return it, otherwise throw an error. */
	public Globals checkglobals() {
		return this;
	}

	/** Convenience function for loading a file.  
	 * @param filename Name of the file to load.
	 * @return LuaValue that can be call()'ed or invoke()'ed.
	 * @throws LuaError if the file could not be loaded.
	 */
	public LuaValue loadFile(String filename) {
		Varargs v = baselib.loadFile(filename, "bt", this);
		return !v.isnil(1)? v.arg1(): error(v.arg(2).tojstring());
	}

	/** Convenience function to load a string value as a script.
	 * @param script Contents of a lua script, such as "print 'hello, world.'"
	 * @param chunkname Name that will be used within the chunk as the source.
	 * @return LuaValue that may be executed via .call(), .invoke(), or .method() calls.
	 * @throws LuaError if the script could not be compiled.
	 */
	public LuaValue loadString(String script, String chunkname) {
		Varargs v = baselib.loadStream(valueOf(script).toInputStream(), chunkname, "bt", this);
		return !v.isnil(1)? v.arg1(): error(v.arg(2).tojstring());
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

}
