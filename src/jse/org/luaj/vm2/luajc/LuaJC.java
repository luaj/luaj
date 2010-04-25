/*******************************************************************************
* Copyright (c) 2010 Luaj.org. All rights reserved.
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
package org.luaj.vm2.luajc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.LoadState.LuaCompiler;
import org.luaj.vm2.compiler.LuaC;

public class LuaJC implements LuaCompiler {

	private static final String NON_IDENTIFIER = "[^a-zA-Z0-9_$]";
	
	private static LuaJC instance;
	private LuaC luac;
	
	public static LuaJC getInstance() {
		if ( instance == null )
			instance = new LuaJC();
		return instance;
	}
	
	/** 
	 * Install the compiler as the main compiler to use. 
	 * Will fall back to the LuaC prototype compiler.
	 */
	public static final void install() {
		LoadState.compiler = getInstance(); 
	}
	
	public LuaJC() {
		luac = new LuaC();
	}

	public Hashtable compileAll(InputStream script, String classname, String filename) throws IOException {
		Hashtable h = new Hashtable();
		Prototype p = luac.compile(script.read(), script, classname);
		JavaGen gen = new JavaGen(p, classname, filename);
		insert( h, gen );
		return h;
	}
	
	private void insert(Hashtable h, JavaGen gen) {
		h.put(gen.classname, gen.bytecode);
		for ( int i=0; i<gen.inners.length; i++ )
			insert(h, gen.inners[i]);
	}

	public Prototype compile(int firstByte, InputStream stream, String name) throws IOException {
		return luac.compile(firstByte, stream, name);
	}

	public LuaFunction load(int firstByte, InputStream stream, String name, LuaValue env) throws IOException {
		return load( compile(firstByte, stream, name), name, env );
	}

	public LuaFunction load(Prototype p, String filename, LuaValue env) {
		String classname = filename.endsWith(".lua")? filename.substring(0,filename.length()-4): filename;
		classname = classname.replaceAll(NON_IDENTIFIER, "_");
		JavaGen gen = new JavaGen(p, classname, filename);
		JavaLoader loader = new JavaLoader(env);
		loader.include(gen);
		return (LuaFunction) loader.load(p, classname, filename);
	}

	public LuaValue load(InputStream stream, String name, LuaValue env) throws IOException {
		return load(stream.read(), stream, name, env);
	}

}
