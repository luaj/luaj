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



abstract
public class LuaValue extends Varargs {

	public static final int TINT            = (-2);
	public static final int TNONE			= (-1);
	public static final int TNIL			= 0;
	public static final int TBOOLEAN		= 1;
	public static final int TLIGHTUSERDATA	= 2;
	public static final int TNUMBER			= 3;
	public static final int TSTRING			= 4;
	public static final int TTABLE			= 5;
	public static final int TFUNCTION		= 6;
	public static final int TUSERDATA		= 7;
	public static final int TTHREAD			= 8;
	public static final int TVALUE          = 9;

	public static final String[] TYPE_NAMES = {
		"nil", 
		"boolean",
		"lightuserdata",
		"number",
		"string",
		"table",
		"function",
		"userdata",
		"thread",
		"value",
	};
	
	public static final LuaValue   NIL       = new LuaNil();
	public static final LuaBoolean TRUE      = new LuaBoolean(true);
	public static final LuaBoolean FALSE     = new LuaBoolean(false);
	public static final LuaValue   NONE      = new None();
	public static final LuaNumber  ZERO      = LuaInteger.valueOf(0);
	public static final LuaNumber  ONE       = LuaInteger.valueOf(1);
	public static final LuaNumber  MINUSONE  = LuaInteger.valueOf(-1);
	public static final LuaValue[] NOVALS    = {};
	
	public static final LuaString INDEX       = valueOf("__index");
	public static final LuaString NEWINDEX    = valueOf("__newindex");
	public static final LuaString CALL        = valueOf("__call");
	public static final LuaString MODE        = valueOf("__mode");
	public static final LuaString METATABLE   = valueOf("__metatable");
	public static final LuaString ADD         = valueOf("__add");
	public static final LuaString SUB         = valueOf("__sub");
	public static final LuaString DIV         = valueOf("__div");
	public static final LuaString MUL         = valueOf("__mul");
	public static final LuaString POW         = valueOf("__pow");
	public static final LuaString MOD         = valueOf("__mod");
	public static final LuaString UNM         = valueOf("__unm");
	public static final LuaString LEN         = valueOf("__len");
	public static final LuaString EQ          = valueOf("__eq");
	public static final LuaString LT          = valueOf("__lt");
	public static final LuaString LE          = valueOf("__le");
	public static final LuaString TOSTRING    = valueOf("__tostring");
	public static final LuaString CONCAT      = valueOf("__concat");
	public static final LuaString EMPTYSTRING = valueOf("");
	
	private static int MAXSTACK = 250;
	public static final LuaValue[] NILS = new LuaValue[MAXSTACK];	
	static {
		for ( int i=0; i<MAXSTACK; i++ )
			NILS[i] = NIL;
	}
	
	// type
	abstract public int type();
	abstract public String  typename();

	// type checks
	public boolean isboolean()           { return false; }
	public boolean isclosure()           { return false; }
	public boolean isfunction()          { return false; }
	public boolean isint()               { return false; } // may convert from string
	public boolean isinttype()           { return false; } // will not convert string
	public boolean islong()              { return false; }
	public boolean isnil()               { return false; }
	public boolean isnumber()            { return false; } // may convert from string
	public boolean isstring()            { return false; }
	public boolean isthread()            { return false; }
	public boolean istable()             { return false; }
	public boolean isuserdata()          { return false; }
	public boolean isuserdata(Class c)   { return false; }
	
	// type coercion to java primitives
	public boolean toboolean()           { return true; }
	public byte    tobyte()              { return 0; }
	public char    tochar()              { return 0; }
	public double  todouble()            { return 0; }
	public float   tofloat()             { return 0; }
	public int     toint()               { return 0; }
	public long    tolong()              { return 0; }
	public short   toshort()             { return 0; }
	public String  tojstring()           { return typename() + ": " + Integer.toHexString(hashCode()); }
	public Object  touserdata()          { return null; }
	public Object  touserdata(Class c)   { return null; }

	// Object.toString() maps to tojstring() 
	public String toString() { return tojstring(); }
	
	// type coercion to lua values
	/** @return NIL if not a number or convertible to a number */ 
	public LuaValue    tonumber()     { return NIL; }
	
	/** @return NIL if not a string or number */ 
	public LuaValue    tostring()     { return NIL; }

	// optional argument conversions
	public boolean     optboolean(boolean defval)          { argerror("boolean");   return false; }
	public LuaClosure  optclosure(LuaClosure defval)       { argerror("closure");   return null;  }
	public double      optdouble(double defval)            { argerror("double");    return 0;     }
	public LuaFunction optfunction(LuaFunction defval)     { argerror("function");  return null;  }
	public int         optint(int defval)                  { argerror("int");       return 0;     }
	public LuaInteger  optinteger(LuaInteger defval)       { argerror("integer");   return null;  }
	public long        optlong(long defval)                { argerror("long");      return 0;     }
	public LuaNumber   optnumber(LuaNumber defval)         { argerror("number");    return null;  }
	public String      optjstring(String defval)           { argerror("String");    return null;  }
	public LuaString   optstring(LuaString defval)         { argerror("string");    return null;  }
	public LuaTable    opttable(LuaTable defval)           { argerror("table");     return null;  }
	public LuaThread   optthread(LuaThread defval)         { argerror("thread");    return null;  }
	public Object      optuserdata(Object defval)          { argerror("object");    return null;  }
	public Object      optuserdata(Class c, Object defval) { argerror(c.getName()); return null;  }
	public LuaValue    optvalue(LuaValue defval)           { return this; }

	// argument type checks
	public boolean     checkboolean()          { argerror("boolean");   return false; }
	public LuaClosure  checkclosure()          { argerror("closure");   return null;  }
	public double      checkdouble()           { argerror("double");    return 0; }
	public LuaValue    checkfunction()         { argerror("function");  return null; }	
	public int         checkint()              { argerror("int");       return 0; }
	public LuaInteger  checkinteger()          { argerror("integer");   return null; }
	public long        checklong()             { argerror("long");      return 0; }
	public LuaNumber   checknumber()           { argerror("number");    return null; }
	public LuaNumber   checknumber(String msg) { throw new LuaError(msg); }
	public String      checkjstring()          { argerror("string");    return null; }
	public LuaString   checkstring()           { argerror("string");    return null; }
	public LuaTable    checktable()            { argerror("table");     return null; }	
	public LuaThread   checkthread()           { argerror("thread");    return null; }
	public Object      checkuserdata()         { argerror("userdata");  return null; }
	public Object      checkuserdata(Class c)  { argerror("userdata");  return null; }
	public LuaValue    checknotnil()           { return this; }
	public LuaValue    checkvalidkey()         { return this; }
	
	// errors
	public static LuaValue error(String message) { throw new LuaError(message); }
	public static void assert_(boolean b,String msg) { if(!b) throw new LuaError(msg); }
	protected LuaValue argerror(String expected) { throw new LuaError("bad argument: "+expected+" expected, got "+typename()); }
	public static LuaValue argerror(int iarg,String msg) { throw new LuaError("bad argument #"+iarg+": "+msg); }
	protected LuaValue typerror(String expected) { throw new LuaError(expected+" expected, got "+typename()); }
	protected LuaValue unimplemented(String fun) { throw new LuaError("'"+fun+"' not implemented for "+typename()); }
	protected LuaValue illegal(String op,String typename) { throw new LuaError("illegal operation '"+op+"' for "+typename); }
	protected LuaValue lenerror() { throw new LuaError("attempt to get length of "+typename()); }
	protected LuaValue aritherror() { throw new LuaError("attempt to perform arithmetic on "+typename()); }
	protected LuaValue aritherror(String fun) { throw new LuaError("attempt to perform arithmetic '"+fun+"' on "+typename()); }
	protected LuaValue compareerror(String rhs) { throw new LuaError("attempt to compare "+typename()+" with "+rhs); }
	protected LuaValue compareerror(LuaValue rhs) { throw new LuaError("attempt to compare "+typename()+" with "+rhs.typename()); }
	protected LuaValue concaterror() { throw new LuaError("attempt to concatenate "+typename()); }
	
	// table operations
	public LuaValue get( LuaValue key ) { return gettable(this,key); }
	public LuaValue get( int key ) { return get(LuaInteger.valueOf(key)); }
	public LuaValue get( String key ) { return get(valueOf(key)); }
	public void set( LuaValue key, LuaValue value ) { settable(this, key, value); }
	public void set( int key, LuaValue value ) { set(LuaInteger.valueOf(key), value ); }
	public void set( int key, String value ) { set(key, valueOf(value) ); }
	public void set( String key, LuaValue value ) { set(valueOf(key), value ); }
	public void set( String key, double value ) { set(valueOf(key), valueOf(value) ); }
	public void set( String key, int value ) { set(valueOf(key), valueOf(value) ); }
	public void set( String key, String value ) { set(valueOf(key), valueOf(value) ); }
	public LuaValue rawget( LuaValue key ) { return unimplemented("rawget"); }
	public LuaValue rawget( int key ) { return rawget(valueOf(key)); }
	public LuaValue rawget( String key ) { return rawget(valueOf(key)); }
	public void rawset( LuaValue key, LuaValue value ) { unimplemented("rawset"); }
	public void rawset( int key, LuaValue value ) { rawset(valueOf(key),value); }
	public void rawset( int key, String value ) { rawset(key,valueOf(value)); }
	public void rawset( String key, LuaValue value ) { rawset(valueOf(key),value); }
	public void rawset( String key, double value ) { rawset(valueOf(key),valueOf(value)); }
	public void rawset( String key, int value ) { rawset(valueOf(key),valueOf(value)); }
	public void rawset( String key, String value ) { rawset(valueOf(key),valueOf(value)); }
	public void rawsetlist( int key0, Varargs values ) { for ( int i=0, n=values.narg(); i<n; i++ ) rawset(key0+i,values.arg(i+1)); }
	public void presize( int i) { typerror("table"); }
	public Varargs next(LuaValue index) { return typerror("table"); }
	public Varargs inext(LuaValue index) { return typerror("table"); }
	public LuaValue load(LuaValue library) { library.setfenv(this); return library.call(); }

	// varargs references
	public LuaValue arg(int index) { return index==1? this: NIL; }
	public int narg() { return 1; };
	public LuaValue arg1() { return this; }
	
	// metatable operations
	public LuaValue getmetatable() { return null; };
	public LuaValue setmetatable(LuaValue metatable) { return argerror("table"); }
	public LuaValue getfenv() { typerror("function or thread"); return null; }
	public void setfenv(LuaValue env) { typerror("function or thread"); }
		
	// function calls
	public LuaValue call() { return callmt().call(this); }
	public LuaValue call(LuaValue arg) { return callmt().call(this,arg); }
	public LuaValue call(LuaValue arg1, LuaValue arg2) { return callmt().call(this,arg1,arg2); }
	public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) { return callmt().invoke(new LuaValue[]{this,arg1,arg2,arg3}).arg1(); }
	public LuaValue method(String name) { return this.get(name).call(this); }
	public LuaValue method(LuaValue name) { return this.get(name).call(this); }
	public LuaValue method(String name, LuaValue arg) { return this.get(name).call(this,arg); }
	public LuaValue method(LuaValue name, LuaValue arg) { return this.get(name).call(this,arg); }
	public LuaValue method(String name, LuaValue arg1, LuaValue arg2) { return this.get(name).call(this,arg1,arg2); }
	public LuaValue method(LuaValue name, LuaValue arg1, LuaValue arg2) { return this.get(name).call(this,arg1,arg2); }
	public Varargs invoke() { return invoke(NONE); }
	public Varargs invoke(Varargs args) { return callmt().invoke(this,args); }
	public Varargs invoke(LuaValue arg,Varargs varargs) { return invoke(varargsOf(arg,varargs)); }
	public Varargs invoke(LuaValue arg1,LuaValue arg2,Varargs varargs) { return invoke(varargsOf(arg1,arg2,varargs)); }
	public Varargs invoke(LuaValue[] args) { return invoke(varargsOf(args)); }
	public Varargs invoke(LuaValue[] args,Varargs varargs) { return invoke(varargsOf(args,varargs)); }
	public Varargs invokemethod(String name) { return get(name).invoke(this); }
	public Varargs invokemethod(LuaValue name) { return get(name).invoke(this); }
	public Varargs invokemethod(String name, Varargs args) { return get(name).invoke(varargsOf(this,args)); }
	public Varargs invokemethod(LuaValue name, Varargs args) { return get(name).invoke(varargsOf(this,args)); }
	public Varargs invokemethod(String name, LuaValue[] args) { return get(name).invoke(varargsOf(this,varargsOf(args))); }
	public Varargs invokemethod(LuaValue name, LuaValue[] args) { return get(name).invoke(varargsOf(this,varargsOf(args))); }
	protected LuaValue callmt() {
		return checkmetatag(CALL, "attempt to call ");
	}
	
	// unary operators
	public LuaValue not()  { return FALSE;  }
	public LuaValue neg()  { return checkmetatag(UNM, "attempt to perform arithmetic on ").call(this);  }
	public LuaValue len()  { return checkmetatag(LEN, "attempt to get length of ").call(this);  }
	public int length()    { return len().toint(); }
	public LuaValue getn() { return typerror("getn"); }
	
	// object equality, used for key comparison
	public boolean equals(Object obj)         { return this == obj; } 
	
	// arithmetic equality
	public LuaValue  eq( LuaValue val )       { return eq_b(val)? TRUE: FALSE; }
	public boolean eq_b( LuaValue val )       { return this == val; } 
	public boolean eq_b( LuaTable val )       { return false; }
	public boolean eq_b( LuaUserdata val )    { return false; }
	public boolean eq_b( LuaString val )      { return false; }
	public boolean eq_b( double val )         { return false; }
	public boolean eq_b( int val )            { return false; }
	public LuaValue   neq( LuaValue val )     { return eq_b(val)? FALSE: TRUE; }
	public boolean neq_b( LuaValue val )      { return ! eq_b(val); }
	public boolean neq_b( double val )        { return ! eq_b(val); }
	public boolean neq_b( int val )           { return ! eq_b(val); }
	protected boolean eqmt_b( LuaValue op2 )      { 
		LuaValue h = metatag(EQ);
		return !h.isnil() && h==op2.metatag(EQ)? h.call(this,op2).toboolean(): false;
	}
	
	// arithmetic operators
	public LuaValue   add( LuaValue rhs )        { return arithmt(ADD,rhs); }
	public LuaValue   add(double rhs)         { return aritherror("add"); }
	public LuaValue   add(int rhs)            { return add((double)rhs); }
	public LuaValue   sub( LuaValue rhs )        { return arithmt(SUB,rhs); }
	public LuaValue   sub( double rhs )        { return aritherror("sub"); }
	public LuaValue   sub( int rhs )        { return aritherror("sub"); }
	public LuaValue   subFrom(double lhs)     { return aritherror("sub"); }
	public LuaValue   subFrom(int lhs)        { return subFrom((double)lhs); }
	public LuaValue   mul( LuaValue rhs )        { return arithmt(MUL,rhs); }
	public LuaValue   mul(double rhs)         { return aritherror("mul"); }
	public LuaValue   mul(int rhs)            { return mul((double)rhs); }
	public LuaValue   pow( LuaValue rhs )        { return arithmt(POW,rhs); }
	public LuaValue   pow( double rhs )        { return aritherror("pow"); }
	public LuaValue   pow( int rhs )        { return aritherror("pow"); }
	public LuaValue   powWith(double lhs)     { return aritherror("mul"); }
	public LuaValue   powWith(int lhs)        { return powWith((double)lhs); }
	public LuaValue   div( LuaValue rhs )        { return arithmt(DIV,rhs); }
	public LuaValue   div( double rhs )        { return aritherror("div"); }
	public LuaValue   div( int rhs )        { return aritherror("div"); }
	public LuaValue   divInto(double lhs)     { return aritherror("divInto"); }
	public LuaValue   mod( LuaValue rhs )        { return arithmt(MOD,rhs); }
	public LuaValue   mod( double rhs )        { return aritherror("mod"); }
	public LuaValue   mod( int rhs )        { return aritherror("mod"); }
	public LuaValue   modFrom(double lhs)     { return aritherror("modFrom"); }
	protected LuaValue arithmt(LuaValue tag, LuaValue op2) {
		LuaValue h = this.metatag(tag);
		if ( h.isnil() )
			h = op2.checkmetatag(tag, "attempt to perform arithmetic on ");
		return h.call( this, op2 );
	}
	
	// relational operators
	public LuaValue   lt( LuaValue rhs )         { return comparemt(LT,rhs); }
	public LuaValue   lt( double rhs )         { return compareerror("number"); }
	public LuaValue   lt( int rhs )         { return compareerror("number"); }
	public boolean lt_b( LuaValue rhs )     { return comparemt(LT,rhs).toboolean(); }
	public boolean lt_b( int rhs )         { compareerror("number"); return false; }
	public boolean lt_b( double rhs )      { compareerror("number"); return false; }
	public LuaValue   lteq( LuaValue rhs )       { return comparemt(LE,rhs); }
	public LuaValue   lteq( double rhs )       { return compareerror("number"); }
	public LuaValue   lteq( int rhs )       { return compareerror("number"); }
	public boolean lteq_b( LuaValue rhs )     { return comparemt(LE,rhs).toboolean(); }
	public boolean lteq_b( int rhs )       { compareerror("number"); return false; }
	public boolean lteq_b( double rhs )    { compareerror("number"); return false; }
	public LuaValue   gt( LuaValue rhs )         { return rhs.comparemt(LE,this); }
	public LuaValue   gt( double rhs )         { return compareerror("number"); }
	public LuaValue   gt( int rhs )         { return compareerror("number"); }
	public boolean gt_b( LuaValue rhs )       { return rhs.comparemt(LE,this).toboolean(); }
	public boolean gt_b( int rhs )         { compareerror("number"); return false; }
	public boolean gt_b( double rhs )      { compareerror("number"); return false; }
	public LuaValue   gteq( LuaValue rhs )       { return rhs.comparemt(LT,this); }
	public LuaValue   gteq( double rhs )         { return compareerror("number"); }
	public LuaValue   gteq( int rhs )         { return valueOf(todouble() >= rhs); }
	public boolean gteq_b( LuaValue rhs )     { return rhs.comparemt(LT,this).toboolean(); }
	public boolean gteq_b( int rhs )       { compareerror("number"); return false; }
	public boolean gteq_b( double rhs )    { compareerror("number"); return false; }
	public LuaValue comparemt( LuaValue tag, LuaValue op1 ) { 
		if ( type() == op1.type() ) { 
			LuaValue h = metatag(tag);
			if ( !h.isnil() && h == op1.metatag(tag) )
				return h.call(this, op1);
		}
		return error("attempt to compare "+typename());
	}
	
	
	// string comparison
	public int strcmp( LuaValue rhs )         { error("attempt to compare "+typename()); return 0; }
	public int strcmp( LuaString rhs )      { error("attempt to compare "+typename()); return 0; }

	// concatenation
	public LuaValue concat(LuaValue rhs)      { return rhs.concatTo(this); }
	public LuaValue concatTo(LuaValue lhs)    { return lhs.concatmt(this); }
	public LuaValue concatTo(LuaNumber lhs)   { return concaterror(); }
	public LuaValue concatTo(LuaString lhs)   { return concaterror(); }
	public Buffer   buffer()                  { return new Buffer(this); }
	public Buffer   concat(Buffer rhs)        { return rhs.setvalue(concat(rhs.value())); }
	public LuaValue concatmt(LuaValue rhs) {
		LuaValue h=metatag(CONCAT);
		LuaValue v=this;
		if ( h.isnil() && (h=(v=rhs).metatag(CONCAT)).isnil())
			return v.concaterror();
		return h.call(this,rhs);
	}
	
	// boolean operators
	public LuaValue   and( LuaValue rhs )      { return this.toboolean()? rhs: this; }
	public LuaValue   or( LuaValue rhs )       { return this.toboolean()? this: rhs; }
	
	// for loop helpers
	/** used in for loop only */
	public boolean testfor_b(LuaValue limit, LuaValue step) { return step.gt_b(0)? lteq_b(limit): gteq_b(limit); }
	
	// lua number/string conversion
	public LuaString strvalue()     { typerror("strValue"); return null; }
	public LuaValue  strongkey()    { return strongvalue(); }
	public LuaValue  strongvalue()  { return this; }
	public boolean   isweaknil()    { return false; }

	// conversion from java values
	public static LuaBoolean  valueOf(boolean b)    { return b? LuaValue.TRUE: FALSE; };
	public static LuaInteger  valueOf(int i)        { return LuaInteger.valueOf(i); }
	public static LuaNumber   valueOf(double d)     { return LuaDouble.valueOf(d); };
	public static LuaString valueOf(String s)     { return LuaString.valueOf(s); }
	public static LuaString valueOf(byte[] bytes) { return LuaString.valueOf(bytes); }
	public static LuaString valueOf(byte[] bytes, int off, int len) { 
		return LuaString.valueOf(bytes,off,len); 
	}
	
	// table initializers
	public static LuaTable tableOf() { return new LuaTable(); }
	public static LuaTable tableOf(Varargs varargs, int firstarg) { return new LuaTable(varargs,firstarg); }
	public static LuaTable tableOf(int narray, int nhash) { return new LuaTable(narray, nhash); }	
	public static LuaTable listOf(LuaValue[] unnamedValues) { return new LuaTable(null,unnamedValues,null); }
	public static LuaTable listOf(LuaValue[] unnamedValues,Varargs lastarg) { return new LuaTable(null,unnamedValues,lastarg); }
	public static LuaTable tableOf(LuaValue[] namedValues) { return new LuaTable(namedValues,null,null); }	
	public static LuaTable tableOf(LuaValue[] namedValues, LuaValue[] unnamedValues) {return new LuaTable(namedValues,unnamedValues,null); }	
	public static LuaTable tableOf(LuaValue[] namedValues, LuaValue[] unnamedValues, Varargs lastarg) {return new LuaTable(namedValues,unnamedValues,lastarg); }	
	
	// userdata intializers
	public static LuaUserdata userdataOf(Object o) { return new LuaUserdata(o); }
	public static LuaUserdata userdataOf(Object o,LuaValue metatable) { return new LuaUserdata(o,metatable); }

	private static final int      MAXTAGLOOP = 100;
	
	// metatable processing
	/** get value from metatable operations, or NIL if not defined by metatables */
	protected static LuaValue gettable(LuaValue t, LuaValue key) {
		LuaValue tm;
		int loop = 0;
		do { 
			if (t.istable()) {
				LuaValue res = t.rawget(key);
				if ((!res.isnil()) || (tm = t.metatag(INDEX)).isnil())
					return res;
			} else if ((tm = t.metatag(INDEX)).isnil())
				t.indexerror();
			if (tm.isfunction())
				return tm.call(t, key);
			t = tm;
		}
		while ( ++loop < MAXTAGLOOP );
		error("loop in gettable");
		return NIL;
	}
	
	/** returns true if value was set via metatable processing, false otherwise */
	protected static boolean settable(LuaValue t, LuaValue key, LuaValue val) {
		LuaValue tm;
		int loop = 0;
		do { 
			if (t.istable()) {
				if ((!t.rawget(key).isnil()) || (tm = t.metatag(NEWINDEX)).isnil()) {
					t.rawset(key, val);
					return true;
				}
			} else if ((tm = t.metatag(NEWINDEX)).isnil())
				t.typerror("index");
			if (tm.isfunction()) {
				tm.call(t, key, val);
				return true;
			}
			t = tm;
		}
		while ( ++loop < MAXTAGLOOP );
		error("loop in settable");
		return false;
	}
	
    public LuaValue metatag(LuaValue tag) {
    	LuaValue mt = getmetatable();
    	if ( mt == null )
    		return NIL;
    	return mt.rawget(tag);
    }
    
	protected LuaValue checkmetatag(LuaValue tag, String reason) {
		LuaValue h = this.metatag(tag);
		if ( h.isnil() )
			throw new LuaError(reason+typename());
		return h;
	}
    
    private void indexerror() {
		error( "attempt to index ? (a "+typename()+" value)" );
	}
 	
	// common varargs constructors
	public static Varargs varargsOf(final LuaValue[] v) {
		switch ( v.length ) {
		case 0: return NONE;
		case 1: return v[0];
		case 2: return new PairVarargs(v[0],v[1]); 
		default: return new ArrayVarargs(v,NONE);
		}
	}
	public static Varargs varargsOf(final LuaValue[] v,Varargs r) { 
		switch ( v.length ) {
		case 0: return r;
		case 1: return new PairVarargs(v[0],r);
		default: return new ArrayVarargs(v,r);
		}
	}
	public static Varargs varargsOf(final LuaValue[] v, final int offset, final int length) {
		switch ( length ) {
		case 0: return NONE;
		case 1: return v[offset];
		case 2: return new PairVarargs(v[offset+0],v[offset+1]);
		default: return new ArrayPartVarargs(v,offset,length);
		}
	}
	public static Varargs varargsOf(final LuaValue[] v, final int offset, final int length,Varargs more) {
		switch ( length ) {
		case 0: return more;
		case 1: return new PairVarargs(v[offset],more);
		default: return new ArrayPartVarargs(v,offset,length,more);
		}
	}
	public static Varargs varargsOf(LuaValue v, Varargs r) {
		switch ( r.narg() ) {
		case 0: return v;
		default: return new PairVarargs(v,r);
		}
	}
	public static Varargs varargsOf(LuaValue v1,LuaValue v2,Varargs v3) { 
		switch ( v3.narg() ) {
		case 0: return new PairVarargs(v1,v2);
		default: return new ArrayVarargs(new LuaValue[] {v1,v2},v3); 
		}
	}
	
	// tail call support
	public static Varargs tailcallOf(LuaValue func, Varargs args) { 
		return new TailcallVarargs(func, args);
	}
	
	// called by TailcallVarargs to invoke the function once.  
	// may return TailcallVarargs to be evaluated by the caller. 
	public Varargs onInvoke(Varargs args) {
		return invoke(args);
	}

	// empty varargs
	private static final class None extends LuaNil {
		public LuaValue arg(int i) { return NIL; }
		public int narg() { return 0; }
		public LuaValue arg1() { return NIL; }
		public String tojstring() { return "none"; }
	}
	
	// varargs from array
	static final class ArrayVarargs extends Varargs {
		private final LuaValue[] v;
		private final Varargs r;
		ArrayVarargs(LuaValue[] v, Varargs r) {
			this.v = v;
			this.r = r ;
		}
		public LuaValue arg(int i) {
			return i >=1 && i<=v.length? v[i - 1]: r.arg(i-v.length);
		}
		public int narg() {
			return v.length+r.narg();
		}
		public LuaValue arg1() { return v.length>0? v[0]: r.arg1(); }
	}

	// varargs from array part
	static final class ArrayPartVarargs extends Varargs {
		private final int offset;
		private final LuaValue[] v;
		private final int length;
		private final Varargs more;
		ArrayPartVarargs(LuaValue[] v, int offset, int length) {
			this.v = v;
			this.offset = offset;
			this.length = length;
			this.more = NONE;
		}
		public ArrayPartVarargs(LuaValue[] v, int offset, int length, Varargs more) {
			this.v = v;
			this.offset = offset;
			this.length = length;
			this.more = more;
		}
		public LuaValue arg(int i) {
			return i>=1&&i<=length? v[i+offset-1]: more.arg(i-length);
		}
		public int narg() {
			return length + more.narg();
		}
		public LuaValue arg1() { 
			return length>0? v[offset]: more.arg1(); 
		}
	}

	// varargs from two values
	static final class PairVarargs extends Varargs {
		private final LuaValue v1;
		private final Varargs v2;
		PairVarargs(LuaValue v1, Varargs v2) {
			this.v1 = v1;
			this.v2 = v2;
		}
		public LuaValue arg(int i) {
			return i==1? v1: v2.arg(i-1);
		}
		public int narg() {
			return 1+v2.narg();
		}
		public LuaValue arg1() { 
			return v1; 
		}
	}

}
