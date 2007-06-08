package lua.value;

import lua.StackState;

abstract
public class LValue {

	protected static LValue luaUnsupportedOperation() {
		throw new java.lang.UnsupportedOperationException();
	}
	
	// test if value is true
	public boolean luaAsBoolean() {
		return true;
	}

	// perform a lua call
	public void luaStackCall(StackState state, int base, int nresults) {
		luaUnsupportedOperation();
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

	/** set a value in a table
	 */
	public void luaSetTable(LValue key, LValue value) {
		luaUnsupportedOperation();
	}

	/** Get a value from a table */
	public LValue luaGetTable(LValue value) {
		return luaUnsupportedOperation();
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
		luaUnsupportedOperation();
		return 0;
	}
	
}
