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

import java.util.List;

public class LSStatement {

	public enum Type {
		functionCall,
		doBlock,
		whileLoop,
		repeatUntil,
		varAssign,
		forLoop,
		forList,
		varNamedFunction,
		localFunction,
		localAssign,
		returnStat,
		breakStat,
		ifStat,
	}
	
	public final Type type;
	
	LSStatement( Type type ) {	
		this.type = type;
	}

	public static LSStatement functionCallStatement(LSVariable variable) {
		return new FunctionCall( variable );
	}

	public static LSStatement doBlockStatement(List<LSStatement> statements) {
		return new DoBlock( statements );
	}

	public static LSStatement whileLoopStatement(LSExpression condition, List<LSStatement> statements) {
		return new WhileLoop( condition, statements );
	}

	public static LSStatement repeatUntilStatement(List<LSStatement> statements, LSExpression condition) {
		return new RepeatUntil( statements, condition );
	}

	public static LSStatement forLoopStatement(Name name, LSExpression initial,
			LSExpression limit, LSExpression step, List<LSStatement> statements, Scope scope) {
		return new ForLoop( name, initial, limit, step, statements, scope );
	}

	public static LSStatement forListStatement(List<Name> names,
			List<LSExpression> expressions, List<LSStatement> statements, Scope scope, LSFunction function) {
		return new ForList( names, expressions, statements, scope, function );
	}

	public static LSStatement varFunctionStatement(LSVariable funcname, LSFunction funcbody) {
		return new VarNamedFunction( funcname, funcbody );
	}

	public static LSStatement localFunctionStatement(Name name, LSFunction funcbody) {
		return new LocalFunction( name, funcbody );
	}

	public static LSStatement varAssignStatement(List<LSVariable> variables, List<LSExpression> expressions, Scope scope, LSFunction function) {
		setExprNumReturns( variables.size(), expressions, function );
		return new VarAssign( variables,  expressions, scope );
	}

	public static LSStatement localAssignStatement(List<Name> names, List<LSExpression> expressions, Scope scope, LSFunction function) {
		setExprNumReturns( names.size(), expressions, function );
		return new LocalAssign( names, expressions, scope );
	}

	public static void setExprNumReturns( int nassign, List<LSExpression> expressions, LSFunction function ) {
		int nexpr = expressions!=null? expressions.size(): 0;
		for ( int i=0; i<nexpr; i++ )
			expressions.get(i).setNumReturns(
					(nassign<=nexpr)? 
							(i<nassign? 1: 0):    // same or more expressions than names
							(i<nexpr-1? 1: -1) ); // more names than expressions
		if ( nassign > nexpr && nexpr > 0 && expressions.get(nexpr-1).getNumReturns() == -1 )
			function.hasvarargassign = true;
	}
	
	public static LSStatement returnStatement(LSFunction function, List<LSExpression> expressions) {
		int n=expressions!=null? expressions.size(): 0;
		
		// set num returns of subexpressions
		for ( int i=0; i<n; i++ )
			expressions.get(i).setNumReturns(i<n-1? 1: -1); // last in list returns vararg
		int nreturns = 0;
		if ( n > 0 ) {
			LSExpression last = expressions.get(n-1);
			int lastreturns = last.getNumReturns();
			nreturns = (lastreturns<0? -1: lastreturns+n-1);
		}
		
		// change the containing function to varargs if necessary.
		if ( function.maxReturns != -1 && function.maxReturns < nreturns )
			function.maxReturns = nreturns;
		if ( function.maxReturns == -1 || function.maxReturns > 1 || nreturns < 0 )
			function.isvararg = true;
		
		return new ReturnStat( function, expressions );
	}

	public static LSStatement breakStatement() {
		return new BreakStat();
	}

	/** Statement representing a function call on a variable, such as "foo.bar()" */
	public static final class FunctionCall extends LSStatement {
		public final LSVariable variable;
		FunctionCall(LSVariable variable) {
			super( Type.functionCall );
			this.variable = variable;
		}
		public String toString() { return variable.toString()+"();"; }
	}

	/** do block, such as "do foo = bar end" */
	public static final class DoBlock extends LSStatement {
		public final List<LSStatement> statements;
		DoBlock(List<LSStatement> statements) {
			super( Type.doBlock );
			this.statements = statements;
		}
		public String toString() { return "do "+statements+" end;"; }
		public boolean isNextStatementReachable() {
			return isNextStatementReachable(statements);
		}
	}

	/** while loop, such as "while foo = true do bar() end" */
	public static final class WhileLoop extends LSStatement {
		public final LSExpression condition;
		public final List<LSStatement> statements;
		WhileLoop(LSExpression condition, List<LSStatement> statements) {
			super( Type.whileLoop );
			this.condition = condition;
			this.statements = statements;
		}
		public String toString() { return "while "+condition+" do "+statements+" end;"; }
		public boolean isNextStatementReachable() {
			return isNextStatementReachable(statements);
		}
	}

	/** repeat loop, such as "repeat foo() until bar == true" */
	public static final class RepeatUntil extends LSStatement {
		public final List<LSStatement> statements;
		public final LSExpression condition;
		RepeatUntil(List<LSStatement> statements, LSExpression condition) {
			super( Type.repeatUntil );
			this.statements = statements;
			this.condition = condition;
		}
		public String toString() { return "repeat "+statements+" until "+condition+";"; }
		public boolean isNextStatementReachable() {
			return isNextStatementReachable(statements);
		}
	}

	/** assignment to variables, such as "x.a,y.b = foo,bar" */
	public static final class VarAssign extends LSStatement {
		public final List<LSVariable> variables;
		public final List<LSExpression> expressions;
		public final Scope scope;
		VarAssign(List<LSVariable> variables, List<LSExpression> expressions, Scope scope) {
			super( Type.varAssign );
			this.variables = variables;
			this.expressions = expressions;
			this.scope = scope;
		}
		public String toString() { return variables+"="+expressions+";"; }
	}

	/** for loop with index, such as "for i=1,10,2 do ... end" */
	public static final class ForLoop extends LSStatement {
		public final Name index;
		public final LSExpression initial;
		public final LSExpression limit;
		public final LSExpression step;
		public final List<LSStatement> statements;
		public final Scope scope;
		ForLoop(Name name, LSExpression initial, LSExpression limit, LSExpression step, List<LSStatement> statements, Scope scope) {
			super( Type.forLoop );
			this.index = name;
			this.initial = initial;
			this.limit = limit;
			this.step = step;
			this.statements = statements;
			this.scope = scope;
			initial.setNumReturns(1);
			limit.setNumReturns(1);
			if ( step != null )
				step.setNumReturns(1);
		}
		public String toString() { return "for "+index+"="+initial+","+limit+","+step+" do "+statements+" end;"; }
		public boolean isNextStatementReachable() {
			return isNextStatementReachable(statements);
		}
	}

	/** for loop with variable, such as "for i,j,k in foo() do ... end" */
	public static final class ForList extends LSStatement {
		public final List<Name> names;
		public final List<LSExpression> expressions;
		public final List<LSStatement> statements;
		public final Scope scope;
		ForList(List<Name> names, List<LSExpression> expressions, List<LSStatement> statements, Scope scope, LSFunction function) {
			super( Type.forList );
			this.names = names;
			this.expressions = expressions;
			this.statements = statements;
			this.scope = scope;
			
			// set return value count for each expression in list;
			int ne = expressions.size();
			for ( int i=0; i<ne-1 && i<3; i++ )
				expressions.get(i).setNumReturns(1);
			if ( ne<=3 ) {
				expressions.get(ne-1).setNumReturns(3-(ne-1));
				function.hasvarargassign = true;
			}
		}
		public String toString() { return "for "+names+" in "+expressions+" do "+statements+" end;"; }
		public boolean isNextStatementReachable() {
			return isNextStatementReachable(statements);
		}
	}

	/** variable function declaration, such as "a.b = function() end" */
	public static final class VarNamedFunction extends LSStatement {
		public final LSVariable funcname;
		public final LSFunction funcbody;
		VarNamedFunction(LSVariable funcname, LSFunction funcbody) {
			super( Type.varNamedFunction );
			this.funcname = funcname;
			this.funcbody = funcbody;
		}
		public String toString() { return funcname+"=f:"+funcbody+";"; }
	}

	/** simple function declaration, such as "a = function() end" */
	public static final class LocalFunction extends LSStatement {
		public final Name name;
		public final LSFunction funcbody;
		LocalFunction(Name name, LSFunction funcbody) {
			super( Type.localFunction );
			this.name = name;
			this.funcbody = funcbody;
		}
		public String toString() { return name+"=f:"+funcbody+";"; }
	}

	/** assignment, such as "a,b = foo,bar" */
	public static final class LocalAssign extends LSStatement {
		public final List<Name> names;
		public final List<LSExpression> expressions;
		public final Scope scope;
		LocalAssign(List<Name> list, List<LSExpression> expressions, Scope scope) {
			super( Type.localAssign );
			this.names = list;
			this.expressions = expressions;
			this.scope = scope;
		}
		public String toString() { return names+"="+expressions+";"; }
	}

	/** return statement, such as "return foo,bar" */
	public static final class ReturnStat extends LSStatement {
		public final LSFunction function;
		public final List<LSExpression> expressions;
		ReturnStat(LSFunction function, List<LSExpression> expressions) {
			super( Type.returnStat );
			this.function = function;
			this.expressions = expressions;
		}
		public String toString() { return "return "+expressions+";"; }
		public boolean isNextStatementReachable() {
			return false;
		}
	}

	/** break statement */
	public static final class BreakStat extends LSStatement {
		BreakStat() {
			super( Type.breakStat );
		}
		public String toString() { return "break;"; }
	}

	/** True of this statment could return and therefore the next statement is reachable. */
	public boolean isNextStatementReachable() {
		return true;
	}
	
	public static boolean isNextStatementReachable(List<LSStatement> stats) {
		if ( stats == null )
			return true;
		for ( LSStatement s : stats )
			if ( ! s.isNextStatementReachable() )
				return false;
		return true;
	}
}
