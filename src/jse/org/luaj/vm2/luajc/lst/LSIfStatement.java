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

public class LSIfStatement extends LSStatement {

	public final LSExpression condition;
	public final List<LSStatement> statements;
	public List<ElseIf> elseifs;
	public List<LSStatement> elsestatements;
	
	public static class ElseIf {
		public final LSExpression condition;
		public final List<LSStatement> statements;
		public ElseIf(LSExpression condition, List<LSStatement> statements) {
			this.condition = condition;
			this.statements = statements;
		}
	}
	
	public LSIfStatement(LSExpression condition, List<LSStatement> statements) {
		super( Type.ifStat );
		this.condition = condition;
		this.statements = statements;
	}

	public void addElseif(LSExpression condition, List<LSStatement> statements) {
		if ( elseifs == null )
			elseifs = new ArrayList<ElseIf>();
		elseifs.add( new ElseIf( condition, statements ) );
	}

	public void addElse(List<LSStatement> statements) {
		elsestatements = statements;
	}
	
	public String toString() {		
		return "if "+condition+" then "+statements+
			(elseifs!=null? elseifs.toString(): "")+
			(elsestatements!=null? " else "+elsestatements: "");
	}
	public boolean isNextStatementReachable() {
		if ( isNextStatementReachable(statements) )
			return true;
		if ( elseifs != null )
			for ( ElseIf e : elseifs )
				if ( isNextStatementReachable(statements) )
					return true;
		if ( isNextStatementReachable(elsestatements) )
			return true;
		return false;
	}

}
