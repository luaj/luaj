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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.luaj.vm2.LuaValue;

public class LuaJCompiler {

	public static LuaValue compile( InputStream luaSource, String chunkName, LuaValue env ) throws Exception {
		String java = compileToJava( luaSource, chunkName );
		LuaValue chunk = javaCompile( java, chunkName );		
		chunk.setfenv(env);
		return chunk;
	}
	
	public static String compileToJava( InputStream luaSource, String chunkName ) throws Exception {
		// return AntlrLuaJCompiler.compile( luaSource, chunkName );
		throw new RuntimeException( "not supported" );
	}
	
	
	public static LuaValue javaCompile( String java, String className) throws Exception {
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			LuaValue.error("no java compiler");
		}

		// write the file
		new File("jit").mkdirs();
		File source = new File("jit/"+className+JavaFileObject.Kind.SOURCE.extension);
		PrintStream ps = new PrintStream(new FileOutputStream(source));
		ps.print(java);
		ps.close();

		// set up output location 
		Iterable<? extends File> dest = Arrays.asList(new File[] { new File("bin") });
		StandardJavaFileManager fm = compiler.getStandardFileManager( null, null, null);
		fm.setLocation(StandardLocation.CLASS_OUTPUT, dest);
		
		// compile the file
		Iterable<? extends JavaFileObject> compilationUnits = fm.getJavaFileObjects(source);
		CompilationTask task = compiler.getTask(null, fm, null, null, null, compilationUnits);
		boolean success = task.call();

		// instantiate, config and return
		if (success) {
			// compile sub-prototypes
//				if ( p.p != null ) {
//					for ( int i=0, n=p.p.length; i<n; i++ ) {
//						if ( ! (p.p[i] instanceof JitPrototype) ) 
//							p.p[i] = jitCompile( p.p[i] );
//					}
//				}
			
			// create JitPrototype instance
			Class clazz = Class.forName(className);
			Object instance = clazz.newInstance();
			LuaValue value = (LuaValue) instance;
			return value;
		}
		return LuaValue.error("compile task failed");
	}
	
}
