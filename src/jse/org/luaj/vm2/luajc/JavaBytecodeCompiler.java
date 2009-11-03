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
import java.util.Hashtable;

import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.LoadState.LuaCompiler;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.PackageLib;

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

	/** Compile and load a chunk 
	 * @throws IOException */
	public static byte[] loadClass(InputStream is, String filename) throws IOException {
		return getInstance().loadClass( is.read(), is, filename );
	}
	
	/** Compile into class form. */
	public LuaFunction load(int firstByte, InputStream stream, String filename, LuaValue env) throws IOException {
		Prototype p = compile( firstByte, stream, filename);
		return load( p, filename, env );
	}
		
	/** Compile into a class */
	private byte[] loadClass(int firstByte, InputStream stream, String filename) throws IOException {
		Prototype p = compile(firstByte, stream, filename);
		return gen.generateBytecode(p, PackageLib.toClassname(filename), toSourcename(filename));
	}

	/** Compile all classes produced by a prototype, and put results in a hashtable */
	public static Hashtable loadClasses( InputStream stream, String filename ) throws IOException {
		if ( LoadState.compiler == null )
			install();
		Prototype p = LoadState.compile( stream, filename );
		Hashtable t = new Hashtable();
		getInstance().genClass(t, p, PackageLib.toClassname(filename), toSourcename(filename));
		return t;
	}
	
	private void genClass( Hashtable t, Prototype p, String classname, String sourceName ) throws IOException {
		t.put( classname, gen.generateBytecode( p, classname, sourceName ) );
		for ( int i=0, n=p.p!=null? p.p.length: 0; i<n; i++ )
			genClass( t, p.p[i], classname + "$" + i, sourceName );
	}
	
	public LuaFunction load(Prototype p, String filename, LuaValue env) {
		try {
			Class c = gen.toJavaBytecode(p, PackageLib.toClassname(filename), toSourcename(filename));
			Object o = c.newInstance();
			LuaFunction f = (LuaFunction) o;
			f.setfenv(env);
			return f;
		} catch ( Throwable t ) {
			t.printStackTrace();
			return new LuaClosure( p, env );
		}
	}
	
	private static final String toSourcename( String filename ) {
		return filename.substring( filename.lastIndexOf('/')+1 );
	}

}
