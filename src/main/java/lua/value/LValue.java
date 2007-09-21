package lua.value;

import lua.Lua;
import lua.VM;

abstract
public class LValue {

	/** Metatable tag for intercepting table gets */
	public static final LString TM_INDEX    = new LString("__index");
	
	/** Metatable tag for intercepting table sets */
	public static final LString TM_NEWINDEX = new LString("__newindex");
	
	protected static LValue luaUnsupportedOperation() {
		throw new java.lang.RuntimeException( "not supported" );
	}

	public String id() {
		return Integer.toHexString(hashCode());
	}
	
	// test if value is true
	public boolean luaAsBoolean() {
		return true;
	}
	
	/** Return true if this value can be represented as an "int" */
	public boolean isInteger() {
		return false;
	}
	
	// perform a lua call, return true if the call is to a lua function, false
	// if it ran to completion.
	public boolean luaStackCall(VM vm) {
		vm.lua_error("attempt to call "+this);
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
	public void luaSetTable(VM vm, LValue table, LValue key, LValue val) {
		LTable mt = luaGetMetatable();
		if ( mt != null ) {
			LValue event = mt.get( TM_NEWINDEX );
			if ( event != null && event != LNil.NIL ) {
				event.luaSetTable( vm, table, key, val );
				return;
			}
		}
		vm.push( LNil.NIL );
	}
	
	/** Get a value from a table 
	 * @param vm the calling vm
	 * @param table the table from which to get the value 
	 * @param key the key to look up
	 */
	public void luaGetTable(VM vm, LValue table, LValue key) {
		LTable mt = luaGetMetatable();
		if ( mt != null ) {
			LValue event = mt.get( TM_INDEX );
			if ( event != null && event != LNil.NIL ) {
				event.luaGetTable( vm, table, key );
				return;
			}
		}
		vm.push(LNil.NIL);
	}
	
	/** Get the value as a String
	 */
	public abstract LString luaAsString();
	
	/** Override standard toString with lua String conversion by default */
	public String toString() {
		return luaAsString().toJavaString();
	}

	/** Return value as an integer */
	public int luaAsInt() {
		luaUnsupportedOperation();
		return 0;
	}

	/** Return value as a double */
	public double luaAsDouble() {
		return luaAsInt();
	}

	/** Arithmetic negative */
	public LValue luaUnaryMinus() {
		return luaUnsupportedOperation();
	}

	/** Built-in opcode LEN, for Strings and Tables */
	public LValue luaLength() {
		// TODO: call meta-method TM_LEN here
		return luaUnsupportedOperation();
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

	/** Valid for all types: return the type of this value as an LString */
	public abstract LString luaGetType();

}
