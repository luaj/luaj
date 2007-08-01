package lua.value;

import lua.Lua;
import lua.VM;

abstract
public class LValue {

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

	// perform a lua call, return true if the call is to a lua function, false
	// if it ran to completion.
	public boolean luaStackCall(VM vm) {
		luaUnsupportedOperation();
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
	public boolean luaBinCmpString(int opcode, String rhs) {
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
	 * @param vm the calling vm
	 * @param table the table to operate on
	 * @param the key to set
	 * @param the value to set
	 */
	public void luaSetTable(VM vm, LValue table, LValue key, LValue val) {
		luaUnsupportedOperation();
	}

	/** Get a value from a table 
	 * @param vm the calling vm
	 * @param table the table from which to get the value 
	 * @param key the key to look up
	 */
	public void luaGetTable(VM vm, LValue table, LValue key) {
		luaUnsupportedOperation();
	}
	
	/** Get the value as a String
	 */
	public String luaAsString() {
		return super.toString();
	}
	
	/** Override standard toString with lua String conversion by default */
	public String toString() {
		return luaAsString();
	}

	/** Return value as an integer */
	public int luaAsInt() {
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

	/** Valid for tables */
	public LValue luaPairs() {
		return luaUnsupportedOperation();
	}

	/** Valid for tables */
	public LValue luaGetMetatable() {
		return luaUnsupportedOperation();
	}

	/** Valid for tables */
	public void luaSetMetatable(LValue metatable) {
		luaUnsupportedOperation();
	}

	/** Valid for all types: return the type of this value as an LString */
	public abstract LString luaGetType();
}
