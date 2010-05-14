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
package org.luaj.luajc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Print;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.JsePlatform;

/** Test the plain old bytecode interpreter */
public class TestLuaJ {
	// create the script
	public static String name = "script";
	public static String script =
		"function r(q,...)\n"+
		"	local a=arg\n"+
		"	return a and a[2]\n"+
		"end\n" +
		"function s(q,...)\n"+
		"	local a=arg\n"+
		"	local b=...\n"+
		"	return a and a[2],b\n"+
		"end\n" +
		"print( r(111,222,333),s(111,222,333) )";
		
	public static void main(String[] args) throws Exception {
		System.out.println(script);
		
		// create an environment to run in
		LuaTable _G = JsePlatform.standardGlobals();
		
		// compile into a chunk, or load as a class
		InputStream is =  new ByteArrayInputStream( script.getBytes() );
		Prototype p = LuaC.compile(is, "script");
		print( p );
		LuaValue chunk = new LuaClosure(p,_G);
		chunk.call();
	}

	private static void print(Prototype p) {
		System.out.println("--- "+p.is_vararg);
		Print.printCode(p);
		if (p.p!=null)
			for ( int i=0,n=p.p.length; i<n; i++ )
				print( p.p[i] );
	}
		
}
