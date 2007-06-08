package lua.value;

import lua.Lua;

public class LDouble extends LNumber {

	private final double m_value;
	
	public LDouble(double value) {
		this.m_value = value;
	}

	public String luaAsString() {
		return String.valueOf(m_value);
	}
	
	// binary operations on integers, first dispatch
	public LValue luaBinOpUnknown(int opcode, LValue lhs) {
		return lhs.luaBinOpDouble( opcode, this.m_value );
	}
	
	// binary operations on mixtures of doubles and integers
	public LValue luaBinOpInteger(int opcode, int rhs) {
		return luaBinOpDoubleDouble( opcode, m_value, (double) rhs );
	}
	
	// binary operations on doubles
	public LValue luaBinOpDouble(int opcode, double rhs) {
		return luaBinOpDoubleDouble( opcode, m_value, rhs );
	}
	
	public static LValue luaBinOpDoubleDouble( int opcode, double lhs, double rhs ) {
		switch ( opcode ) {
		case Lua.OP_ADD: return new LDouble( lhs + rhs );
		case Lua.OP_SUB: return new LDouble( lhs - rhs );
		case Lua.OP_MUL: return new LDouble( lhs * rhs );
		case Lua.OP_DIV: return new LDouble( lhs / rhs );
		case Lua.OP_MOD: return new LDouble( lhs % rhs );
		case Lua.OP_POW: return new LDouble( Math.pow(lhs, rhs) );
		}
		return luaUnsupportedOperation();
	}
	
	public int luaAsInt() {
		return (int) m_value;
	}
	
}
