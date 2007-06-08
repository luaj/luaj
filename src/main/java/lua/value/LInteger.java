package lua.value;

import lua.Lua;

public class LInteger extends LNumber {

	private final int m_value;
	
	public LInteger(int value) {
		this.m_value = value;
	}

	public int luaAsInt() {
		return m_value;
	}
	
	public String luaAsString() {
		return String.valueOf(m_value);
	}
	
	// binary operations on integers, first dispatch
	public LValue luaBinOpUnknown(int opcode, LValue lhs) {
		return lhs.luaBinOpInteger( opcode, this.m_value );
	}
	
	// binary operations on integers
	public LValue luaBinOpInteger(int opcode, int rhs) {
		switch ( opcode ) {
		case Lua.OP_ADD: return new LInteger( m_value + rhs );
		case Lua.OP_SUB: return new LInteger( m_value - rhs );
		case Lua.OP_MUL: return new LInteger( m_value * rhs );
		case Lua.OP_DIV: return new LInteger( m_value / rhs );
		case Lua.OP_MOD: return new LInteger( m_value % rhs );
		case Lua.OP_POW: return new LInteger( (int) Math.pow(m_value, rhs) );
		}
		return luaUnsupportedOperation();
	}
	
	// binary operations on mixed integer, double
	public LValue luaBinOpDouble(int opcode, double rhs) {
		return LDouble.luaBinOpDoubleDouble(opcode, (double) m_value, rhs );
	}
}
