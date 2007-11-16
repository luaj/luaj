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


abstract
public class LValue {

	/** Metatable tag for intercepting table gets */
	public static final LString TM_INDEX    = new LString("__index");
	
	/** Metatable tag for intercepting table sets */
	public static final LString TM_NEWINDEX = new LString("__newindex");
	
	protected static LValue luaUnsupportedOperation() {
		throw new LuaErrorException( "not supported" );
	}

	protected void luaConversionError(String target) {
		throw new LuaErrorException( "bad conversion: "+luaGetTypeName()+" to "+target );
	}


	public String id() {
		return Integer.toHexString(hashCode());
	}
	
	/** Return true if this value can be represented as an "int" */
	public boolean isInteger() {
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
		return luaUnsupportedOperation();
	}

	// unsupported except for numbers
	public LValue luaBinOpInteger(int opcode, int m_value) {
		return luaUnsupportedOperation();
	}

	// unsupported except for numbers
	public LValue luaBinOpDouble(int opcode, double m_value) {
		return luaUnsupportedOperation();
	}

	// unsupported except for numbers, strings, and == with various combinations of Nil, Boolean, etc. 
	public boolean luaBinCmpUnknown(int opcode, LValue lhs) {
		if ( opcode == Lua.OP_EQ )
			return lhs == this;
		luaUnsupportedOperation();
		return false;
	}
	
	// unsupported except for strings
	public boolean luaBinCmpString(int opcode, LString rhs) {
		if ( opcode == Lua.OP_EQ )
			return false;
		luaUnsupportedOperation();
		return false;
	}
	
	// unsupported except for numbers
	public boolean luaBinCmpInteger(int opcode, int rhs) {
		if ( opcode == Lua.OP_EQ )
			return false;
		luaUnsupportedOperation();
		return false;
	}
	
	// unsupported except for numbers
	public boolean luaBinCmpDouble(int opcode, double rhs) {
		if ( opcode == Lua.OP_EQ )
			return false;
		luaUnsupportedOperation();
		return false;
	}
	
	/** set a value in a table
	 * For non-tables, goes straight to the meta-table.
	 * @param vm the calling vm
	 * @param table the table to operate on
	 * @param the key to set
	 * @param the value to set
	 */
	public void luaSetTable(LuaState vm, LValue table, LValue key, LValue val) {
		LTable mt = luaGetMetatable();
		if ( mt != null ) {
			LValue event = mt.get( TM_NEWINDEX );
			if ( event != null && event != LNil.NIL ) {
				event.luaSetTable( vm, table, key, val );
				return;
			}
		}
		vm.pushnil();
	}
	
	/** Get a value from a table 
	 * @param vm the calling vm
	 * @param table the table from which to get the value 
	 * @param key the key to look up
	 */
	public void luaGetTable(LuaState vm, LValue table, LValue key) {
		LTable mt = luaGetMetatable();
		if ( mt != null ) {
			LValue event = mt.get( TM_INDEX );
			if ( event != null && event != LNil.NIL ) {
				event.luaGetTable( vm, table, key );
				return;
			}
		}
		vm.pushnil();
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
		return luaUnsupportedOperation();
	}

	/** Built-in opcode LEN, for Strings and Tables */
	public int luaLength() {
		// TODO: call meta-method TM_LEN here
		luaUnsupportedOperation();
		return 0;
	}

	/** Valid for tables 
	 * @param isPairs true to iterate over non-integers as well */
	public LValue luaPairs(boolean isPairs) {
		return luaUnsupportedOperation();
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

	/** Valid for tables */
	public void luaSetMetatable(LValue metatable) {
		luaUnsupportedOperation();
	}

	/** Valid for all types: return the int value identifying the type of this value */
	public abstract int luaGetType();

	
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
		return (byte) toJavaInt();
	}

	/** Return value as a char */
	public char toJavaChar() {
		return (char) toJavaInt();
	}

	/** Return value as a double */
	public double toJavaDouble() {
		return toJavaInt();
	}

	/** Return value as a float */
	public float toJavaFloat() {
		return (float) toJavaDouble();
	}

	/** Return value as an integer */
	public int toJavaInt() {
		luaConversionError("number");
		return 0;
	}

	/** Return value as a long */
	public long toJavaLong() {
		return (long) toJavaDouble();
	}

	/** Return value as a double */
	public short toJavaShort() {
		return (short) toJavaInt();
	}

	/** Convert to a Boolean value */
	public Boolean toJavaBoxedBoolean() {
		luaConversionError("Boolean");
		return null;
	}

	/** Convert to a Byte value */
	public Byte toJavaBoxedByte() {
		luaConversionError("Byte");
		return null;
	}

	/** Convert to a boxed Character value */
	public Character toJavaBoxedCharacter() {
		luaConversionError("Character");
		return null;
	}

	/** Convert to a boxed Double value */
	public Double toJavaBoxedDouble() {
		luaConversionError("Double");
		return null;
	}

	/** Convert to a boxed Float value */
	public Float toJavaBoxedFloat() {
		luaConversionError("Float");
		return null;
	}

	/** Convert to a boxed Integer value */
	public Integer toJavaBoxedInteger() {
		luaConversionError("Integer");
		return null;
	}

	/** Convert to a boxed Long value */
	public Long toJavaBoxedLong() {
		luaConversionError("Long");
		return null;
	}

	/** Convert to a boxed Short value */
	public Short toJavaBoxedShort() {
		luaConversionError("Short");
		return null;
	}

	/** Convert to a Java Object iff this is a LUserData value */
	public Object toJavaInstance() {
		luaConversionError("instance");
		return null;
	}

	/** Set the environment if a thread, or closure, and return 1, otherwise return 0 */
	public int luaSetEnv(LTable t) {
		return 0;
	}

}
