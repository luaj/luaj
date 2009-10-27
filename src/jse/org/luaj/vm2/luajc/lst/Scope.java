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

import java.util.HashMap;
import java.util.Map;

/** A name scope in lua source terms.  
 * Combination of function scope and block scope. 
 */
public class Scope {
	public final Scope parent;
	public final int level; 
	public final boolean isFunctionScope;
	public boolean hasAndOrLogic;
	public final Map<String,Name> names = new HashMap<String,Name>();
	
	/** Construct a new default scope for a chunk */
	public Scope() {
		this.parent = null;
		this.isFunctionScope = true;
		this.level = 0;
	}

	/** Construct an inner scope
	 * @param parent the outer scope to fall back to, or null if a global scope
	 * @param isFunctionScope true if this is a function scope, false otherwise 
	 */
	public Scope(Scope parent, boolean isFunctionScope) {
		this.parent = parent;
		this.isFunctionScope = isFunctionScope;
		this.level = parent!=null? parent.level + 1: 0;
	}

	/** Declare a single name in the current scope, and return the Name element for it */
	public Name declare(String name) {
		boolean crossesFunctionBoundary = false;
		for ( Scope s=this; s!=null; s=s.parent ) {
			Name n = s.names.get(name);
			if ( n != null ) {
				Name result = new Name(name, 
						level,
						n.outerrevision+1,
						crossesFunctionBoundary? 0: n.innerrevision+1);
				names.put(name, result);
				return result;
			}
			if ( s.isFunctionScope ) 
				crossesFunctionBoundary = true;
		}
		Name result = new Name(name, level, 0, 0);
		names.put(name, result);
		return result;
	}

	/** Reference a name, and find either the local scope within the function, the upvalue, or a global name */
	public Name reference(String name) {
		boolean crossesFunctionBoundary = false;
		for ( Scope s=this; s!=null; s=s.parent ) {
			Name n = s.names.get(name);
			if ( n != null ) {
				if ( crossesFunctionBoundary ) {
					n.isupvalue = true;
				}
				return n;
			}
			if ( s.isFunctionScope ) 
				crossesFunctionBoundary = true;
		}
		// globally scoped name
		return new Name(name);
	}
	
	public String toString() {
		String ours = (isFunctionScope? "F": "")+names;
		return (parent==null? ours: ours+"->"+parent.toString());
	}

	/** Return true iff this scope is part the main chunk */
	public boolean isMainChunkScope() {
		return isFunctionScope? false: parent==null? true: parent.isMainChunkScope();
	}
}

