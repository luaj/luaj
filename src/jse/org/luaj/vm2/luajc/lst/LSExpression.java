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

import java.io.ByteArrayOutputStream;
import java.util.List;

public class LSExpression {
	public enum Type {
		unop,
		binop,
		functionExpr,
		tableConstructor,
		nilConstant,
		trueConstant,
		falseConstant,
		varargsRef,
		numberConstant,
		stringConstant,

		// variable types
		nameVariable,
		fieldVariable,
		methodVariable,
		parenthesesVariable,
		indexVariable,
		callFunctionVariable,
		callMethodVariable,
	}

	public static final LSExpression ENIL = new LSExpression(Type.nilConstant);     // nil
	public static final LSExpression EFALSE = new LSExpression(Type.falseConstant); // false
	public static final LSExpression ETRUE = new LSExpression(Type.trueConstant);   // true
	
	public final Type type;
	
	LSExpression(Type type) {
		this.type = type;
	}
	
	public static LSExpression functionExpression(LSFunction function) {
		return new FunctionExpr(function);
	}

	public static LSExpression unopExpression(UnOp unop, LSExpression rhs, Scope scope) {
		if ( rhs instanceof BinopExpr ) {
			BinopExpr b = (BinopExpr) rhs;
			if ( BinOp.UNARY_PRECEDENCE > b.op.precedence )
				return binopExpression( unopExpression( unop, b.lhs, scope ), b.op, b.rhs, scope );
		}
		return new UnopExpr( unop, rhs );
	}

	public static LSExpression binopExpression(LSExpression lhs, BinOp binop, LSExpression rhs, Scope scope) {
		if ( lhs instanceof UnopExpr ) {
			UnopExpr u = (UnopExpr) lhs;
			if ( binop.precedence > BinOp.UNARY_PRECEDENCE )
				return unopExpression( u.op, binopExpression( u.rhs, binop, rhs, scope ), scope );
		}
		// TODO: cumulate string concatenations together
		// TODO: constant folding
		if ( lhs instanceof BinopExpr ) {
			BinopExpr b = (BinopExpr) lhs;
			if ( (binop.precedence > b.op.precedence) ||
				 ((binop.precedence == b.op.precedence) && binop.isrightassoc) )
				return binopExpression( b.lhs, b.op, binopExpression( b.rhs, binop, rhs, scope ), scope );
		}
		if ( rhs instanceof BinopExpr ) {
			BinopExpr b = (BinopExpr) rhs;
			if ( (binop.precedence > b.op.precedence) ||
				 ((binop.precedence == b.op.precedence) && ! binop.isrightassoc) )
				return binopExpression( binopExpression( lhs, binop, b.lhs, scope ), b.op, b.rhs, scope );
		}
		return new BinopExpr( lhs, binop, rhs, scope );
	}

	public static LSExpression numberExpression(String number) {
		return new NumberConstant(number);
	}

	public static LSExpression tableConstructorExpression(List<LSField> fields) {
		return new TableConstructor( fields );
	}

	public static LSExpression normalStringExpression(String luaSourceString) {
		return new StringConstant(luaSourceString.substring(1,luaSourceString.length()-1));
	}

	public static LSExpression charStringExpression(String luaSourceString) {
		return new StringConstant(luaSourceString.substring(1,luaSourceString.length()-1));
	}

	public static LSExpression longStringExpression(String luaSourceString) {
		luaSourceString = luaSourceString.substring(1,luaSourceString.length()-1);
		luaSourceString = luaSourceString.substring(luaSourceString.indexOf('[',1)+1, luaSourceString.lastIndexOf(']'));
		return new StringConstant(luaSourceString);
	}

	public static LSExpression varargsRef() {
		return new VarargsRef();		
	}
	
	/** varargs such as "..." */
	public static class VarargsRef extends LSExpression {
		public int numReturns = -1;
		public VarargsRef() {
			super( Type.varargsRef );
		}
		public void setNumReturns(int i) {
			this.numReturns = i;
		}
		public int getNumReturns() {
			return numReturns;
		}
		public String toString() { return "..."; }
	}
	
	/** prefix expression such as "(foo)(bar)" ?  */
	public static class FunctionExpr extends LSExpression {
		public final LSFunction function;
		public FunctionExpr( LSFunction function) {
			super( Type.functionExpr );
			this.function = function;
		}
		public String toString() { return function.toString(); }
	}
	
	/** unary operator such as "not foo" */
	public static class UnopExpr extends LSExpression {
		public final UnOp op;
		public final LSExpression rhs;
		public UnopExpr( UnOp op, LSExpression rhs ) {
			super( Type.unop );
			this.op = op;
			this.rhs = rhs;
		}
		public String toString() { return op.luaop+rhs; }
	}
	
	/** binary operator such as "a + b" */
	public static class BinopExpr extends LSExpression {
		public final LSExpression lhs;
		public final BinOp op;
		public final LSExpression rhs;
		public final Scope scope;
		public BinopExpr( LSExpression lhs, BinOp op, LSExpression rhs, Scope scope ) {
			super( Type.binop );
			this.lhs = lhs;
			this.op = op;
			this.rhs = rhs;
			this.scope = scope;
		}
		public String toString() { return lhs+op.luaop+rhs; }
	}
	
	/** table constructor such as "{ 'foo', [0]='bar' }" */
	public static class TableConstructor extends LSExpression {
		public final List<LSField> fields;
		public TableConstructor( List<LSField> fields ) {
			super( Type.tableConstructor );
			this.fields = fields;
			int n = fields.size();
			for ( int i=0; i<n-1; i++ )
				fields.get(i).setNumReturns(1);
			if ( n>0 )
				fields.get(n-1).setNumReturns(-1);
		}
		public String toString() { return "{"+fields+"}"; }
	}
	
	/** number constants such as '123', "4.56", "0x11fe */
	public static class NumberConstant extends LSExpression {
		public final Number value;
		public NumberConstant( String number ) {
			super( Type.numberConstant );
			number = number.toLowerCase();
			if ( number.startsWith("0x") ) {
				Long l = Long.parseLong(number.substring(2), 16); 
				value = (l.intValue()==l.longValue()? (Number) Integer.valueOf(l.intValue()): (Number) l);
			} else {
				Double d = Double.parseDouble(number);
				value = (d.doubleValue()==(double)d.intValue()? (Number) Integer.valueOf(d.intValue()): (Number) d);
			}
		}
		public String toString() { return value.toString(); }
	}
	
	/** string constants such as 'abc', "def", [[ghi]], and [==[pqr]==] */
	public static class StringConstant extends LSExpression {
		public final byte[] bytes;
		public StringConstant( String luaSourceChars ) {
			super( Type.stringConstant );
			this.bytes = unquoteLua( luaSourceChars );
		}
		public String toString() { return "\""+new String(bytes)+"\""; }
	}
	
	/** Unquote lua quoted sequences, and convert to the bytes represented by the source string. */
	public static byte[] unquoteLua( String luaSourceChars ) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		char[] c = luaSourceChars.toCharArray();
		int n = c.length;
		for ( int i=0; i<n; i++ ) {
			if ( c[i] == '\\' && i<n ) {
				switch ( c[++i] ) {
				case '0': case '1': case '2': case '3': case '4':
				case '5': case '6': case '7': case '8': case '9':
					int d=(int) (c[i++]-'0');
					for ( int j=0; i<n && j<2 && c[i]>='0' && c[i]<='9'; i++, j++ )
						d = d * 10 + (int) (c[i]-'0');
					baos.write( (byte) d );
					--i;
					continue;
				case 'a':  baos.write( (byte) 7 );    continue;
				case 'b':  baos.write( (byte) '\b' ); continue;
				case 'f':  baos.write( (byte) '\f' ); continue;
				case 'n':  baos.write( (byte) '\n' ); continue;
				case 'r':  baos.write( (byte) '\r' ); continue;
				case 't':  baos.write( (byte) '\t' ); continue;
				case 'v':  baos.write( (byte) 11 );   continue;
				case '"':  baos.write( (byte) '"' );  continue;
				case '\'': baos.write( (byte) '\'' ); continue;
				case '\\': baos.write( (byte) '\\' ); continue;
				default: baos.write( (byte) c[i] ); break;
				}
			} else {
				baos.write( (byte) c[i] );
			}
		}
		return baos.toByteArray();
	}

	/** Set number of return values, return actual number of returns in practice. 
	 * 
	 * @param i desired number of returns, or -1 for varargs.
	 */
	public void setNumReturns(int i) {
	}

	/** Get actual number of returns for this subexpression, or -1 for varargs. 
	 * 
	 * @return actual number of returns, or -1 for varargs.
	 */
	public int getNumReturns() {
		return 1;
	}
	
}
