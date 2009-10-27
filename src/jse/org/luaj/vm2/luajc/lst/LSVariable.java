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

public class LSVariable extends LSExpression {
	
	LSVariable(Type type) {
		super(type);
	}

	/** name, such as 'foo' */
	public static LSVariable nameVariable(Name name) {
		return new NameReference(name);
	}

	/** table field, such as 'a.b' */
	public LSVariable fieldVariable(String field) {
		return new Field(this, field);
	}

	/** method reference, such as foo:bar */
	public LSVariable methodVariable(String method) {
		return new Method(this, method);
	}

	/** parenthetical reference, such as '(foo())' */
	public static LSVariable parenthesesVariable(LSExpression expression) {
		if ( expression != null )
			expression.setNumReturns(1);
		return new Parentheses(expression);
	}

	/** table index, such as 'a[b]' */
	public LSVariable indexVariable(LSExpression expression) {
		return new Index(this, expression);
	}

	/** Variable is a method, such as 'a(x,y)' */
	public LSVariable callFuncVariable(List<LSExpression> parameters) {
		int n = parameters.size();
		for ( int i=0; i<n; i++ )
			parameters.get(i).setNumReturns(i<n-1? 1: -1);
		return new CallFunction(this, parameters);
	}

	/** Variable is a method, such as 'a:b(x,y)' */
	public LSVariable callMethVariable(String method, List<LSExpression> parameters) {
		int n = parameters.size();
		for ( int i=0; i<n; i++ )
			parameters.get(i).setNumReturns(i<n-1? 1: -1);
		return new CallMethod(this, method, parameters);
	}
	
	/** name, such as 'foo' */
	public static class NameReference extends LSVariable {
		public final Name name;
		public NameReference(Name name) {
			super( Type.nameVariable );
			this.name = name;
		}
		public String toString() { return name.toString(); }
	}
	
	/** field reference, such as foo.bar */
	public static class Field extends LSVariable {
		public final LSVariable variable;
		public final String field;
		public Field(LSVariable variable, String field) {
			super( Type.fieldVariable );
			this.variable = variable;
			this.field = field;
		}
		public String toString() { return variable+"."+field; }
	}
	
	/** method reference, such as foo:bar */
	public static class Method extends LSVariable {
		public final LSVariable variable;
		public final String method;
		public Method(LSVariable variable, String method) {
			super( Type.methodVariable );
			this.variable = variable;
			this.method = method;
		}
		public String toString() { return variable+":"+method; }
	}
	
	/** parenthetical reference, such as '(foo())' */
	public static class Parentheses extends LSVariable {
		public final LSExpression expression;
		public Parentheses(LSExpression expression) {
			super( Type.parenthesesVariable );
			this.expression = expression;
		}
		public String toString() { return "("+expression+")"; }
	}

	/** table index, such as 'a[b]' */
	public static class Index extends LSVariable {
		public final LSVariable variable;
		public final LSExpression expression;
		public Index(LSVariable variable, LSExpression expression) {
			super( Type.indexVariable );
			this.variable = variable;
			this.expression = expression;
		}
		public String toString() { return variable+"["+expression+"]"; }
	}
	

	/** Variable is a function invocation, such as 'a(x,y)' */
	public static class CallFunction extends LSVariable {
		public final LSVariable variable;
		public final List<LSExpression> parameters;
		public int numReturns = 0;
		public CallFunction(LSVariable variable, List<LSExpression> parameters) {
			super( Type.callFunctionVariable );
			this.variable = variable;
			this.parameters = parameters;
		}
		public void setNumReturns(int i) {
			this.numReturns = i;
		}
		public int getNumReturns() {
			return numReturns;
		}
		public String toString() { return variable+"("+parameters+")"; }
	}
	
	/** Variable is a method invocation, such as 'a:bc()' */
	public static class CallMethod extends LSVariable {
		public final LSVariable variable;
		public final String method;
		public final List<LSExpression> parameters;
		public int numReturns = 0;
		public CallMethod(LSVariable variable, String method, List<LSExpression> parameters) {
			super( Type.callMethodVariable );
			this.variable = variable;
			this.method = method;
			this.parameters = parameters;
		}
		public void setNumReturns(int i) {
			this.numReturns = i;
		}
		public int getNumReturns() {
			return numReturns;
		}
		public String toString() { return variable+":"+method+"("+parameters+")"; }
	}
	
}
