package lua.value;

import lua.Lua;
import lua.StackState;

public class LString extends LValue {

	final String m_string;
	
	public LString(String string) {
		this.m_string = string;
	}

	public boolean equals(Object o) {
		return o != null && o instanceof LString && m_string.equals(((LString)o).m_string);
	}

	public int hashCode() {
		return m_string.hashCode();
	}

	// TODO: what to do with LuaState? 
	public LString(StackState l, String string) {
		this(string);
	}

	public boolean luaBinCmpUnknown(int opcode, LValue lhs) {
		return lhs.luaBinCmpString(opcode, m_string);
	}

	public boolean luaBinCmpString(int opcode, String rhs) {
		switch ( opcode ) {
		case Lua.OP_EQ: return m_string.equals(rhs);
		case Lua.OP_LT: return m_string.compareTo(rhs) < 0;
		case Lua.OP_LE: return m_string.compareTo(rhs) <= 0;
		}
		luaUnsupportedOperation();
		return false;
	}
	
	public String luaAsString() {
		return m_string;
	}

	/** Built-in opcode LEN, for Strings and Tables */
	public LValue luaLength() {
		return new LInteger( m_string.length() );
	}

}
