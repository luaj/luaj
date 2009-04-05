/*******************************************************************************
* Copyright (c) 2007 LuaJ. All rights reserved.
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
package org.luaj.vm;

import java.io.ByteArrayOutputStream;


abstract
public class LValue {

	/** Metatable tag for intercepting table gets */
	public static final LString TM_INDEX    = new LString("__index");
	
	/** Metatable tag for intercepting table sets */
	public static final LString TM_NEWINDEX = new LString("__newindex");
	
	/** Metatable tag for intercepting table sets */
	public static final LString TM_METATABLE = new LString("__metatable");
	
	/** Metatable tag for setting table mode */
	public static final LString TM_MODE = new LString("__mode");
	
    private static final int MAXTAGLOOP	= 100;
	
	protected void conversionError(String target) {
		throw new LuaErrorException( "bad conversion: "+luaGetTypeName()+" to "+target );
	}

	private static LValue arithmeticError( Object type ) {
		throw new LuaErrorException( "attempt to perform arithmetic on ? (a "+type+" value)" );
	}

	protected static LValue compareError( Object typea, Object typeb ) {
		throw new LuaErrorException( "attempt to compare "+typea+" with "+typeb );
	}

	private LValue indexError(LuaState vm, LValue nontable) {
		vm.error( "attempt to index ? (a "+nontable.luaGetTypeName()+" value)", 1 );
		return LNil.NIL;
	}

	public String id() {
		return Integer.toHexString(hashCode());
	}
	
	/** Return true if this value can be represented as an "int" */
	public boolean isInteger() {
		return false;
	}
	
	/** Return true if this value is LNil.NIL, false otherwise */
	public boolean isNil() {
		return false;
	}
	
	// perform a lua call, return true if the call is to a lua function, false
	// if it ran to completion.
	public boolean luaStackCall(LuaState vm) {
		vm.error("attempt to call "+this);
		return false;
	}

	// unsupported except for numbers
	public LValue luaBinOpUnknown(int opcode, LValue lhs) {
		return arithmeticError(luaGetTypeName());
	}

	// unsupported except for numbers
	public LValue luaBinOpInteger(int opcode, int m_value) {
		return arithmeticError(luaGetTypeName());
	}

	// unsupported except for numbers
	public LValue luaBinOpDouble(int opcode, double m_value) {
		return arithmeticError(luaGetTypeName());
	}

	// unsupported except for numbers, strings, and == with various combinations of Nil, Boolean, etc. 
	public boolean luaBinCmpUnknown(int opcode, LValue lhs) {
		if ( opcode == Lua.OP_EQ )
			return lhs == this;
		compareError(lhs.luaGetTypeName(), luaGetTypeName());
		return false;
	}
	
	// unsupported except for strings
	public boolean luaBinCmpString(int opcode, LString rhs) {
		if ( opcode == Lua.OP_EQ )
			return false;
		compareError(luaGetTypeName(), "string");
		return false;
	}
	
	// unsupported except for numbers
	public boolean luaBinCmpInteger(int opcode, int rhs) {
		if ( opcode == Lua.OP_EQ )
			return false;
		compareError(luaGetTypeName(), "number");
		return false;
	}
	
	// unsupported except for numbers
	public boolean luaBinCmpDouble(int opcode, double rhs) {
		if ( opcode == Lua.OP_EQ )
			return false;
		compareError(luaGetTypeName(), "number");
		return false;
	}
	
	/** Dispatch a settable operation.  
	 * Default method delegates back to the vm for metatable processing.
	 */
	public void luaSetTable(LuaState vm, LValue key, LValue val) {
		vm.luaV_settable(this, key, val);
	}
	
    
	/** Dispatch a gettable operation.  
	 * Default method delegates back to the vm for metatable processing.
	 */
	public LValue luaGetTable(LuaState vm, LValue key) {
		return vm.luaV_gettable(this, key);
	}
	
	/** Get the value as a LString 
	 */
	public LString luaAsString() {
		return new LString(toJavaString());
	}
	
	/** Override standard toString with lua String conversion by default */
	public String toString() {
		return toJavaString();
	}
	
	/** Arithmetic negative */
	public LValue luaUnaryMinus() {
		return arithmeticError(luaGetTypeName());
	}

	/** Built-in opcode LEN, for Strings and Tables */
	public int luaLength() {
		throw new LuaErrorException( "attempt to get length of ? (a "+luaGetTypeName()+" value)" );
	}

	/**
	 * Valid for all types: get a metatable. Only tables and userdata can have a
	 * different metatable per instance, though, other types are restricted to
	 * one metatable per type.
	 * 
	 * Since metatables on non-tables can only be set through Java and not Lua,
	 * this function should be overridden for each value type as necessary.
	 * 
	 * @return null if there is no meta-table
	 */
	public LTable luaGetMetatable() {
		return null;
	}

	/** Valid for tables 
	 * @param the new LTable, or null or LNil.NIL to reset the metatable to none
	 * @return this if unchanged, or new LTable if copied using weak table 
	 */
	public LTable luaSetMetatable(LValue metatable) {
		throw new LuaErrorException( "cannot set metatable for "+this.luaGetTypeName());
	}

	/** Valid for all types: return the int value identifying the type of this value */
	abstract public int luaGetType();

	
	/** Valid for all types: return the type of this value as an LString */
	public LString luaGetTypeName() {
		return LString.LTYPENAMES[luaGetType()];
	}

	
	/** Convert to a Java String */
	public String toJavaString() {
		return null;
	}
	
	/** Return value as a boolean */
	public boolean toJavaBoolean() {
		return true;
	}
	
	/** Return value as a byte */
	public byte toJavaByte() {
		conversionError("number");
		return 0;
	}

	/** Return value as a char */
	public char toJavaChar() {
		conversionError("number");
		return 0;
	}

	/** Return value as a double */
	public double toJavaDouble() {
		conversionError("number");
		return 0;
	}

	/** Return value as a float */
	public float toJavaFloat() {
		conversionError("number");
		return 0;
	}

	/** Return value as an integer */
	public int toJavaInt() {
		conversionError("number");
		return 0;
	}

	/** Return value as a long */
	public long toJavaLong() {
		conversionError("number");
		return 0;
	}

	/** Return value as a double */
	public short toJavaShort() {
		conversionError("number");
		return 0;
	}

	/** Convert to a Boolean value */
	public Boolean toJavaBoxedBoolean() {
		conversionError("Boolean");
		return null;
	}

	/** Convert to a Byte value */
	public Byte toJavaBoxedByte() {
		conversionError("Byte");
		return null;
	}

	/** Convert to a boxed Character value */
	public Character toJavaBoxedCharacter() {
		conversionError("Character");
		return null;
	}

	/** Convert to a boxed Double value */
	public Double toJavaBoxedDouble() {
		conversionError("Double");
		return null;
	}

	/** Convert to a boxed Float value */
	public Float toJavaBoxedFloat() {
		conversionError("Float");
		return null;
	}

	/** Convert to a boxed Integer value */
	public Integer toJavaBoxedInteger() {
		conversionError("Integer");
		return null;
	}

	/** Convert to a boxed Long value */
	public Long toJavaBoxedLong() {
		conversionError("Long");
		return null;
	}

	/** Convert to a boxed Short value */
	public Short toJavaBoxedShort() {
		conversionError("Short");
		return null;
	}

	/** Convert to a Java Object iff this is a LUserData value */
	public Object toJavaInstance() {
		conversionError("instance");
		return null;
	}

	/** Set the environment if a thread, or closure, and return true, otherwise return false */
	public boolean luaSetEnv(LTable t) {
		return false;
	}
	
	/** Get the environment of the object if it is a closure, or d if not a closure.
	 * @param d global environment to return if this is not a closure 
	 */
	public LTable luaGetEnv(LTable d) {
		return d;
	}
	

	/** Convert to a number if possible, or nil otherwise */
	public LValue luaToNumber() {
		return LNil.NIL;
	}

	/** Dereference a potentially weak reference, and return the value */
	public LValue toStrongReference() {
		return this;
	}

	/** Concatenate this value to a ByteArrayOutputStream */
	public void luaConcatTo(ByteArrayOutputStream baos) {
		throw new LuaErrorException( "attempt to concatenate "+luaGetTypeName() );
	}

	/** Return true if this is a lua string, meaning it is 
	 * either a LString or LNumber,since all numbers are 
	 * convertible to strings in lua
	 */
	public boolean isString() {
		return false;
	}

	/** Return true if this is a LTable */
	public boolean isTable() {
		return false;
	}

	/** Return true if this is a LFunction */
	public boolean isFunction() {
		return false;
	}
	
	/** Returns true if this is an LUserData */
	public boolean isUserData() {
		return false;
	}

	/** Returns true if this is or can be made into a number */
	public boolean isNumber() {
		return false;
	}
	
	/** Returns true if this is a lua closure, false otherwise */
	public boolean isClosure() {
		return false;
	}
}
