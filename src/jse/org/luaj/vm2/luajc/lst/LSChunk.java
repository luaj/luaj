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

import java.util.ArrayList;
import java.util.List;


/* A Lua Source Chunk */
public class LSChunk {
	public static final boolean SCOPES = System.getProperty("SCOPES")!=null;
	
	public final String chunkname;
	public final LSFunction function;
	public Scope scope;
	
	public LSChunk( String chunkname ) {
		this.chunkname = chunkname;
		this.function = new LSFunction( true );
		this.scope = null;
	}

	public String toString() {
		return "@"+chunkname+": "+function;
	}

	/** push a block scope onto the name scope stack */
	public void pushScope(String name) {
		scope = new Scope(scope, false);
		if(SCOPES)System.out.println(space(scope)+"push "+name+"     scope="+scope);

	}
	
	/** push a function scope onto the name scope stack */
	public void pushScope(String name,boolean isFunction) {
		scope = new Scope(scope, isFunction);
		if(SCOPES)System.out.println(space(scope)+"push "+name+"     scope="+scope);
	}
	
	/** pop a scope from the scope stack */
	public Scope popScope(String name) {		
		Scope s = scope;
		scope = scope.parent;
		if(SCOPES)System.out.println(space(s)+"pop "+name+"     scope="+scope);
		return s;
	}

	/** return the current scope */
	public Scope peekScope() {		
		return scope;
	}

	/** Declare a single name in the current scope, and return the Name element for it */
	public Name declare(String name) {
		Name n = scope.declare( name );
		if(SCOPES)System.out.println(space(scope)+"  declared "+n+"   scope="+scope);
		return n;
	}

	/** Declare a list of names in the current scope, and return List of Name for them */
	public List<Name> declare(List<String> names) {
		List<Name> results = new ArrayList<Name>(names.size());
		for ( String s : names )
			results.add( declare(s) );
		return results;
	}

	/** Reference a name, and find either the local scope within the function, the upvalue, or a global name 
	 * @param func */
	public Name reference(String name, LSFunction func) {
		Name n = scope.reference(name);
		if ("arg".equals(name) && n.isGlobal() && func.isvararg && !scope.isMainChunkScope()) {
			n = scope.declare(name);
			func.setHasArg();
		}
		if(SCOPES)System.out.println(space(scope)+"  reference "+n+"   scope="+scope);
		return n;
	}

	/** Print out indentation for a scope */
	private static final String ws = "                                                          ";
	private String space(Scope i) {
		return ws.substring(0,i.level*2);
	}


}
