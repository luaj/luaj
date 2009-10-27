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

public class LSFunction {

	public final List<LSStatement> stats = new ArrayList<LSStatement>();
	public final List<LSFunction> functions = new ArrayList<LSFunction>();
	public final List<Name> paramnames = new ArrayList<Name>();

	/** set if this is a vararg function */
	public boolean isvararg;
	
	/** needsarg is set if the code is vararg and needs the "arg" table to be created. */ 
	public boolean hasarg,needsarg;
	
	/** max number of returns, or -1 for varargs */
	public int maxReturns = 0;
	
	/** set if there are logical subexpressions, or varargs assignment */
	public boolean hasandlogic, hasorlogic, hasvarargassign, usesvarargs;
	
	public LSFunction() {
	}
	
	public LSFunction(boolean isvararg) {
		this.isvararg = isvararg;
	}

	public void setStatements(List<LSStatement> stats) {
		this.stats.clear();
		this.stats.addAll(stats);
	}
	
	public void setParameterNames(List<Name> list) {
		this.paramnames.clear();
		this.paramnames.addAll( list );
	}
	
	public String toString() { return "function("+paramnames+") "+stats+" end"; }

	public void setUsesVarargs() {
		this.usesvarargs = true;
		if ( this.hasarg )
			this.needsarg = false;
	}

	public void setHasArg() {
		this.hasarg = true;
		if ( ! this.usesvarargs )
			this.needsarg = true;
	}
}
