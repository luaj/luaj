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
package org.luaj.vm2.lib;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
;


abstract public class LibFunction extends LuaFunction {
	
	protected int opcode;
	protected String name;

	public LibFunction() {		
	}
	
	public LibFunction(String name, int opcode, LuaValue env) {
		super(env);
		this.name = name;
		this.opcode = opcode;
	}

	public String toString() {
		return name!=null? name: super.toString();
	}
	
	public static LuaTable bind( LuaTable table, Class libFuncClass, String[] names ) {
		return bind( table, libFuncClass, names, 0 );
	}
	
	/** Bind a set of names to class instances, put values into the table. */
	public static LuaTable bind( LuaTable table, Class libFuncClass, String[] names, int firstOpcode ) {
		try {
			for ( int i=0, n=names.length; i<n; i++ ) {
				LibFunction f = (LibFunction) libFuncClass.newInstance();
				f.opcode = firstOpcode + i;
				f.name = names[i];
				f.env = table;
				table.set( names[i], f );
			}
		} catch (InstantiationException e) {
			throw new RuntimeException(e.toString());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.toString());
		}
		return table;
	}

	/** Bind a complete set of names and classes , put values into the table. */
	public static LuaTable bind( LuaTable table, Class[] libFuncClasses, String[][] nameLists ) {
		for ( int j=0, n=libFuncClasses.length; j<n; j++ ) {
			bind( table, libFuncClasses[j], nameLists[j] );
		}
		return table;
	}

	
	
} 
