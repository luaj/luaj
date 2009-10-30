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
package org.luaj.vm2.luajc;

import java.io.IOException;
import java.io.InputStream;

import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.LoadState.LuaCompiler;
import org.luaj.vm2.compiler.LuaC;

public class JavaBytecodeCompiler implements LuaCompiler {

	private static JavaBytecodeCompiler instance;
	private JavaBytecodeGenerator gen;
	private LuaC luac;
	
	public static JavaBytecodeCompiler getInstance() {
		if ( instance == null )
			instance = new JavaBytecodeCompiler();
		return instance;
	}
	
	/** 
	 * Install the compiler as the main compiler to use. 
	 * Will fall back to the LuaC prototype compiler.
	 */
	public static final void install() {
		LoadState.compiler = getInstance(); 
	}
	
	public JavaBytecodeCompiler() {
		luac = new LuaC();
		gen = new JavaBytecodeGenerator();
	}

	/** Compile int a prototype */
	public static Prototype compile(InputStream is, String chunkname) throws IOException {
		return getInstance().compile(is.read(), is, chunkname);
	}

	/** Compile and load a chunk 
	 * @throws IOException */
	public static LuaValue load(InputStream is, String filename, LuaValue _g) throws IOException {
		return getInstance().load(is.read(), is, filename, _g);
	}
	
	/** Compile into protoype form. */
	public Prototype compile(int firstByte, InputStream stream, String chunkname) throws IOException {
		return luac.compile(firstByte, stream, chunkname);
	}

	/** Compile into class form. */
	public LuaFunction load(int firstByte, InputStream stream, String filename, LuaValue env) throws IOException {
		Prototype p = compile( firstByte, stream, filename);
		try {
			String classname = filename.endsWith(".lua")? filename.substring(0,filename.length()-4): filename;
			classname = classname.replace('/', '.');
			classname = classname.replace('\\', '.');
			String sourcename = filename.substring( filename.lastIndexOf('/')+1 );
			Class c = gen.toJavaBytecode(p, classname, sourcename);
			Object o = c.newInstance();
			LuaFunction f = (LuaFunction) o;
			f.setfenv(env);
			return f;
		} catch ( Throwable t ) {
			t.printStackTrace();
			return new LuaClosure( p, env );
		}
		
	}

}
