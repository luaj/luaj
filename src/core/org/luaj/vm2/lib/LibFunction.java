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

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

abstract public class LibFunction extends LuaFunction {
	
	protected int opcode;
	protected String name;
	
	protected LibFunction() {		
	}
	
	public String tojstring() {
		return name != null? name: super.tojstring();
	}
	
	protected void bind(LuaValue env, Class factory,  String[] names ) {
		bind( env, factory, names, 0 );
	}
	
	protected void bind(LuaValue env, Class factory,  String[] names, int firstopcode ) {
		try {
			for ( int i=0, n=names.length; i<n; i++ ) {
				LibFunction f = (LibFunction) factory.newInstance();
				f.opcode = firstopcode + i;
				f.name = names[i];
				f.env = env;
				env.set(f.name, f);
			}
		} catch ( Exception e ) {
			throw new LuaError( "bind failed: "+e );
		}
	}	

	// allocate storage for upvalue, leave it empty
	protected static LuaValue[] newupe() {
		return new LuaValue[1];
	}

	// allocate storage for upvalue, initialize with nil
	protected static LuaValue[] newupn() {
		return new LuaValue[] { NIL };
	}
	
	// allocate storage for upvalue, initialize with value
	protected static LuaValue[] newupl(LuaValue v) {
		return new LuaValue[] { v };
	}
} 
