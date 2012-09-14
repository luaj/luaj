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

	public InputStream STDIN  = null;
	public PrintStream STDOUT = System.out;
	public PrintStream STDERR = System.err;

	public ResourceFinder FINDER;
	
	public LuaCompiler compiler = null;

	public BaseLib baselib;
	
	public LuaValue errorfunc;

	public LuaThread running_thread = new LuaThread(this);

	public DebugLib debuglib;
	
	public Globals checkglobals() {
		return this;
	}

	public Varargs loadFile(String filename) {
		return baselib.loadFile(filename, "bt", this);
	}
	
	public Varargs yield(Varargs args) {
		if (running_thread == null || running_thread.isMainThread())
			throw new LuaError("cannot yield main thread");
		final LuaThread.State s = running_thread.state;
		return s.lua_yield(args);
	}

}
