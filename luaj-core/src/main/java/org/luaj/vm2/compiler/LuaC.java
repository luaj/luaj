/*******************************************************************************
* Copyright (c) 2009-2011 Luaj.org. All rights reserved.
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
package org.luaj.vm2.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.lib.BaseLib;

/**
 * Compiler for Lua.
 * 
 * <p>
 * Compiles lua source files into lua bytecode within a {@link Prototype}, 
 * loads lua binary files directly into a {@link Prototype}, 
 * and optionaly instantiates a {@link LuaClosure} around the result 
 * using a user-supplied environment.  
 * 
 * <p>
 * Implements the {@link org.luaj.vm2.Globals.Compiler} interface for loading 
 * initialized chunks, which is an interface common to 
 * lua bytecode compiling and java bytecode compiling.
 *  
 * <p> 
 * The {@link LuaC} compiler is installed by default by both the 
 * {@link org.luaj.vm2.lib.jse.JsePlatform} and {@link org.luaj.vm2.lib.jme.JmePlatform} classes, 
 * so in the following example, the default {@link LuaC} compiler 
 * will be used:
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * globals.load(new StringReader("print 'hello'"), "main.lua" ).call();
 * } </pre>
 * 
 * To load the LuaC compiler manually, use the install method:
 * <pre> {@code
 * LuaC.install(globals);
 * } </pre>
 * 
 * @see #install(Globals)
 * @see Globals#compiler
 * @see Globals#loader
 * @see org.luaj.vm2.luajc.LuaJC
 * @see org.luaj.vm2.lib.jse.JsePlatform
 * @see org.luaj.vm2.lib.jme.JmePlatform
 * @see BaseLib
 * @see LuaValue
 * @see Prototype
 */
public class LuaC extends Constants implements Globals.Compiler, Globals.Loader {

	/** A sharable instance of the LuaC compiler. */
	public static final LuaC instance = new LuaC();
	
	/** Install the compiler so that LoadState will first 
	 * try to use it when handed bytes that are 
	 * not already a compiled lua chunk.
	 * @param globals the Globals into which this is to be installed.
	 */
	public static void install(Globals globals) {
		globals.compiler = instance;
		globals.loader = instance;
	}

	protected LuaC() {}

	/** Compile lua source into a Prototype.
	 * @param stream InputStream representing the text source conforming to lua source syntax.
	 * @param chunkname String name of the chunk to use.
	 * @return Prototype representing the lua chunk for this source.
	 * @throws IOException
	 */
	public Prototype compile(InputStream stream, String chunkname) throws IOException {
		return (new CompileState()).luaY_parser(stream, chunkname);
	}

	public LuaFunction load(Prototype prototype, String chunkname, LuaValue env) throws IOException {
		return new LuaClosure(prototype, env);
	}

	/** @deprecated
	 * Use Globals.load(InputString, String, String) instead, 
	 * or LuaC.compile(InputStream, String) and construct LuaClosure directly.
	 */
	public LuaValue load(InputStream stream, String chunkname, Globals globals) throws IOException {
		return new LuaClosure(compile(stream, chunkname), globals);
	}

	static class CompileState {
		int nCcalls = 0;
		private Hashtable strings = new Hashtable();
		protected CompileState() {}
	
		/** Parse the input */
		Prototype luaY_parser(InputStream z, String name) throws IOException{
			LexState lexstate = new LexState(this, z);
			FuncState funcstate = new FuncState();
			// lexstate.buff = buff;
			lexstate.fs = funcstate;
			lexstate.setinput(this, z.read(), z, (LuaString) LuaValue.valueOf(name) );
			/* main func. is always vararg */
			funcstate.f = new Prototype();
			funcstate.f.source = (LuaString) LuaValue.valueOf(name);
			lexstate.mainfunc(funcstate);
			LuaC._assert (funcstate.prev == null);
			/* all scopes should be correctly finished */
			LuaC._assert (lexstate.dyd == null 
					|| (lexstate.dyd.n_actvar == 0 && lexstate.dyd.n_gt == 0 && lexstate.dyd.n_label == 0));
			return funcstate.f;
		}
	
		// look up and keep at most one copy of each string
		public LuaString newTString(String s) {
			return cachedLuaString(LuaString.valueOf(s));
		}
	
		// look up and keep at most one copy of each string
		public LuaString newTString(LuaString s) {
			return cachedLuaString(s);
		}
	
		public LuaString cachedLuaString(LuaString s) {
			LuaString c = (LuaString) strings.get(s);
			if (c != null) 
				return c;
			strings.put(s, s);
			return s;
		}
	
		public String pushfstring(String string) {
			return string;
		}
	}
}
