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
package org.luaj.vm2.luajc.lst;

/** A name in lua source, which resolves to one of:
 *   - global reference
 *   - local that is not an upvalue
 *   - local that is an upvalue 
 */
public class Name {

	/** Name in lua source file */
	public final String luaname;
	
	/** 0 if in outermost scope, 1 if in next inner scope, ect. */
	public final int scopelevel;
	
	/** 0 if first declaration in global program, 1 if second use, etc. */
	public final int outerrevision;
	
	/** 0 if first declaration in nearest enclosing function, 1 if second use, etc */
	public final int innerrevision;
	
	/** true if used as an upvalue by some enclosed function */
	public boolean isupvalue;

	/** Construct a name instance */
	public Name(String luaname, int scopelevel, int outerrevision, int innterrevision) {
		super();
		this.luaname = luaname;
		this.scopelevel = scopelevel;
		this.outerrevision = outerrevision;
		this.innerrevision = innterrevision;
	}

	/** Construct a name reference representing a global reference */
	public Name(String name) {
		this.luaname = name;
		this.scopelevel = -1;
		this.outerrevision = -1;
		this.innerrevision = -1;
	}
	
	public String toString() { 
		return  scopelevel<0? 
				"_G$"+luaname:
				luaname+"$s"+scopelevel+"$v"+outerrevision+"$f"+innerrevision;
	}

	public boolean isGlobal() {
		return -1 == outerrevision;
	}
	
}
