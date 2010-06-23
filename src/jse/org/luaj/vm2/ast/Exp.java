/*******************************************************************************
* Copyright (c) 2010 Luaj.org. All rights reserved.
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
package org.luaj.vm2.ast;

import org.luaj.vm2.LuaValue;

abstract
public class Exp {
	abstract public void accept(Visitor visitor);

	public static Exp constant(LuaValue value) {
		return new Constant(value);
	}

	public static Exp varargs() {
		return new VarargsExp();
	}

	public static Exp tableconstructor(TableConstructor tc) {
		return tc;
	}

	public static Exp unaryexp(int op, Exp rhs) {
		return new UnopExp(op, rhs);
	}

	public static Exp binaryexp(Exp lhs, int op, Exp rhs) {
		return new BinopExp(lhs, op, rhs);
	}

	public static Exp anonymousfunction(FuncBody funcbody) {
		return new AnonFuncDef(funcbody);
	}

	public static class Constant extends Exp {
		public final LuaValue value;
		public Constant(LuaValue value) {
			this.value = value;
		}

		public void accept(Visitor visitor) {
			visitor.visit(this);
		}		
	}

	public static class VarargsExp extends Exp {
		
		public void accept(Visitor visitor) {
			visitor.visit(this);
		}		
	}
	
	public static class UnopExp extends Exp {
		public final int op;
		public final Exp rhs;
		public UnopExp(int op, Exp rhs) {
			this.op = op;
			this.rhs = rhs;
		}

		public void accept(Visitor visitor) {
			visitor.visit(this);
		}		
	}
	
	public static class BinopExp extends Exp {
		public final Exp lhs,rhs;
		public final int op;
		public BinopExp(Exp lhs, int op, Exp rhs) {
			this.lhs = lhs;
			this.op = op;
			this.rhs = rhs;
		}

		public void accept(Visitor visitor) {
			visitor.visit(this);
		}		
	}
	
	public static class AnonFuncDef extends Exp {
		public final FuncBody funcbody;
		public AnonFuncDef(FuncBody funcbody) {
			this.funcbody = funcbody;
		}

		public void accept(Visitor visitor) {
			visitor.visit(this);
		}		
	}
}
