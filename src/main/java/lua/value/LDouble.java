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

	public LString luaAsString() {
		long l = (long) m_value;
		if ( m_value == (double) l ) {
			// TODO: is this a good idea?
			return new LString( Long.toString( l ) );
		} else {
			return LString.valueOf( m_value );
		}
	}
	
	public boolean isInteger() {
		// Cast to int and then back to double and see if the value
		// survives the round trip.
		return ( (double) ( (int) m_value ) ) == m_value;
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
		case Lua.OP_MOD: return new LDouble( lhs - Math.floor(lhs/rhs) * rhs );
		// case Lua.OP_POW: return new LDouble( dpow(lhs, rhs) );
		}
		return luaUnsupportedOperation();
	}

	/*
	public static double dpow(double a, double b) {
		if ( b < 0 )
			return 1 / dpow( a, -b );
		int p = 1;
		int whole = (int) b;
		for ( double v=a; whole > 0; whole>>=1, v=v*v )
			if ( (whole & 1) != 0 )
				p *= v;
		int frac = (int) (0x10000 * b);
		for ( ; (frac&0xffff)!=0; frac<<=1 ) {
			a = Math.sqrt(a);
			if ( (frac & 0x8000) != 0 )
				p *= a;
		}
		return p;
	}
	*/


	public int luaAsInt() {
		return (int) m_value;
	}

	public double luaAsDouble() {
		return m_value;
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
