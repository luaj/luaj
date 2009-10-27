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


public class BinOp {
	// unary precedence is between POW and MUL
	public static final int UNARY_PRECEDENCE = 7;
	
	public enum Type {
		pow,mul,div,mod,add,sub,concat,lt,lteq,gt,gteq,eq,neq,and,or,
	}
	
	public static final BinOp POW    = new BinOp(Type.pow,    8, true,  "^");
	public static final BinOp MUL    = new BinOp(Type.mul,    6, false, "*");
	public static final BinOp DIV    = new BinOp(Type.div,    6, false, "/");
	public static final BinOp MOD    = new BinOp(Type.mod,    6, false, "%");
	public static final BinOp ADD    = new BinOp(Type.add,    5, false, "+");
	public static final BinOp SUB    = new BinOp(Type.sub,    5, false, "-");
	public static final BinOp CONCAT = new BinOp(Type.concat, 4, true,  "..");
	public static final BinOp LT     = new BinOp(Type.lt,     3, false, "<");
	public static final BinOp LTEQ   = new BinOp(Type.lteq,   3, false, "<=");
	public static final BinOp GT     = new BinOp(Type.gt,     3, false, ">");
	public static final BinOp GTEQ   = new BinOp(Type.gteq,   3, false, ">=");
	public static final BinOp EQ     = new BinOp(Type.eq,     3, false, "==");
	public static final BinOp NEQ    = new BinOp(Type.neq,    3, false, "~=");
	public static final BinOp AND    = new BinOp(Type.and,    2, true,  "and");
	public static final BinOp OR     = new BinOp(Type.or,     1, true,  "or");

	public final Type type;
	public final int precedence;
	public final boolean isrightassoc;
	public final String luaop;
	
	private BinOp(Type type, int precedence, boolean isrightassoc, String luaop) {
		super();
		this.type = type;
		this.precedence = precedence;
		this.isrightassoc = isrightassoc;
		this.luaop = luaop;
	}
	
	public String toString() { return luaop; }
}
