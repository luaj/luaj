package lua.value;

import lua.Lua;

public class LDouble extends LNumber {

	private final double m_value;
	
	public LDouble(double value) {
		this.m_value = value;
	}

	public int hashCode() {
		return (int) m_value;
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

	// binary compares on integers, first dispatch
	public boolean luaBinCmpUnknown(int opcode, LValue lhs) {
		return lhs.luaBinCmpDouble( opcode, this.m_value );
	}
	
	// binary compares on mixtures of doubles and integers
	public boolean luaBinCmpInteger(int opcode, int rhs) {
		return luaBinCmpDoubleDouble( opcode, m_value, (double) rhs );
	}
	
	// binary compares on doubles
	public boolean luaBinCmpDouble(int opcode, double rhs) {
		return luaBinCmpDoubleDouble( opcode, m_value, rhs );
	}
	
	// compare two doubles
	public static boolean luaBinCmpDoubleDouble( int opcode, double lhs, double rhs ) {
		switch ( opcode ) {
		case Lua.OP_EQ: return lhs == rhs;
		case Lua.OP_LT: return lhs < rhs;
		case Lua.OP_LE: return lhs <= rhs;
		}
		luaUnsupportedOperation();
		return false;
	}

	/** Arithmetic negative */
	public LValue luaUnaryMinus() {
		return new LDouble( -m_value );
	}
	
}
