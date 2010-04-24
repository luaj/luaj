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
package org.luaj.vm2;

/**
 * Class to encapsulate varargs values. 
 */
public abstract class Varargs {

	/**
	 * Get the n-th argument value (1-based)
	 * 
	 * @param i 1 for the first argument, 2 for the second, etc.
	 * @return Value at position i, or Value.NIL if there is none.
	 */
	abstract public LuaValue arg( int i );
	
	/**
	 * Get the number of arguments, or 0 if there are none. 
	 * @return number of arguments. 
	 */
	abstract public int narg();
	
	/**
	 * Get the first argument
	 * @return Value
	 */
	abstract public LuaValue arg1();

	/** 
	 * Evaluate any pending tail call and return result
	 * @return the evaluated tail call result 
	 */
	public Varargs eval() { return this; }
	
	/**
	 * Return true if this is a TailcallVarargs
	 * @return true if a tail call, false otherwise
	 */
	public boolean isTailcall() {
		return false;
	}
	// -----------------------------------------------------------------------
	// utilities to get specific arguments and type-check them.
	// -----------------------------------------------------------------------
	
	// argument types
	public int type(int i)             { return arg(i).type(); }
	public boolean isnil(int i)        { return arg(i).isnil(); }
	public boolean isfunction(int i)   { return arg(i).isfunction(); }
	public boolean isnumber(int i)     { return arg(i).isnumber(); }
	public boolean isstring(int i)     { return arg(i).isstring(); }
	public boolean istable(int i)      { return arg(i).istable(); }
	public boolean isthread(int i)     { return arg(i).isthread(); }
	public boolean isuserdata(int i)   { return arg(i).isuserdata(); }
	public boolean isvalue(int i)      { return i>0 && i<=narg(); }
	
	// optional argument types 
	public boolean      optboolean(int i, boolean defval)          { return arg(i).optboolean(defval); }
	public LuaClosure   optclosure(int i, LuaClosure defval)       { return arg(i).optclosure(defval); }
	public double       optdouble(int i, double defval)            { return arg(i).optdouble(defval); }
	public LuaFunction  optfunction(int i, LuaFunction defval)     { return arg(i).optfunction(defval); }
	public int          optint(int i, int defval)                  { return arg(i).optint(defval); }
	public LuaInteger   optinteger(int i, LuaInteger defval)       { return arg(i).optinteger(defval); }
	public long         optlong(int i, long defval)                { return arg(i).optlong(defval); }
	public LuaNumber    optnumber(int i, LuaNumber defval)         { return arg(i).optnumber(defval); }
	public String       optString(int i, String defval)            { return arg(i).optString(defval); }
	public LuaString    optstring(int i, LuaString defval)         { return arg(i).optstring(defval); }
	public LuaTable     opttable(int i, LuaTable defval)           { return arg(i).opttable(defval); }
	public LuaThread    optthread(int i, LuaThread defval)         { return arg(i).optthread(defval); }
	public Object       optuserdata(int i, Object defval)          { return arg(i).optuserdata(defval); }
	public Object       optuserdata(int i, Class c, Object defval) { return arg(i).optuserdata(c,defval); }
	public LuaValue     optvalue(int i, LuaValue defval)           { return i>0 && i<=narg()? arg(i): defval; }
	
	// required argument types 
	public boolean      checkboolean(int i)          { return arg(i).checkboolean(); }
	public LuaClosure   checkclosure(int i)          { return arg(i).checkclosure(); }
	public double       checkdouble(int i)           { return arg(i).checknumber().todouble(); }
	public LuaValue     checkfunction(int i)         { return arg(i).checkfunction(); }
	public int          checkint(int i)              { return arg(i).checknumber().toint(); }
	public LuaInteger   checkinteger(int i)          { return arg(i).checkinteger(); }
	public long         checklong(int i)             { return arg(i).checknumber().tolong(); }
	public LuaNumber    checknumber(int i)           { return arg(i).checknumber(); }
	public String       checkString(int i)           { return arg(i).checkString(); }
	public LuaString    checkstring(int i)           { return arg(i).checkstring(); }
	public LuaTable     checktable(int i)            { return arg(i).checktable(); }
	public LuaThread    checkthread(int i)           { return arg(i).checkthread(); }
	public Object       checkuserdata(int i)         { return arg(i).checkuserdata(); }
	public Object       checkuserdata(int i,Class c) { return arg(i).checkuserdata(c); }
	public LuaValue     checkvalue(int i)            { return i<=narg()? arg(i): LuaValue.error("value expected"); }
	public LuaValue     checknotnil(int i)           { return arg(i).checknotnil(); }
	

	public void         argcheck(boolean test, int i, String msg) { if (!test) LuaValue.argerror(i,msg); }
	
	public boolean isnoneornil(int i) {
		return i>narg() || arg(i).isnil();
	}
	
	public boolean toboolean(int i)           { return arg(i).toboolean(); }
	public byte    tobyte(int i)              { return arg(i).tobyte(); }
	public char    tochar(int i)              { return arg(i).tochar(); }
	public double  todouble(int i)            { return arg(i).todouble(); }
	public float   tofloat(int i)             { return arg(i).tofloat(); }
	public int     toint(int i)               { return arg(i).toint(); }
	public long    tolong(int i)              { return arg(i).tolong(); }
	public short   toshort(int i)             { return arg(i).toshort(); }
	public Object  touserdata(int i)          { return arg(i).touserdata(); }
	public Object  touserdata(int i,Class c)  { return arg(i).touserdata(c); }
	
	public String toString() {
		Buffer sb = new Buffer();
		sb.append( "(" );
		for ( int i=1,n=narg(); i<=n; i++ ) {
			if (i>1) sb.append( "," );
			sb.append( arg(i).toString() );
		}
		sb.append( ")" );
		return sb.toString();
	}

	public Varargs subargs(final int start) {
		int end = narg();
		switch ( end-start ) {
		case 0: return arg(start);
		case 1: return new LuaValue.PairVarargs(arg(start),arg(end));
		}
		return end<start? (Varargs) LuaValue.NONE: new SubVarargs(this,start,end); 
	}
	
	private static class SubVarargs extends Varargs {
		private final Varargs v;
		private final int start;
		private final int end;
		public SubVarargs(Varargs varargs, int start, int end) {
			this.v = varargs;
			this.start = start;
			this.end = end;
		}
		public LuaValue arg(int i) {
			i += start-1;
			return i>=start && i<=end? v.arg(i): LuaValue.NIL;
		}
		public LuaValue arg1() {
			return v.arg(start);
		}
		public int narg() {
			return end+1-start;
		}
	}
}
