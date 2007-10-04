package lua.value;

import lua.Lua;

public class LInteger extends LNumber {
	private final int m_value;
	
	/* local cache of commonly used LInteger values */
	private static final int INTS_MIN =  -16;
	private static final int INTS_MAX  =  32;
	private static final LInteger s_ints[] = new LInteger[1+INTS_MAX-INTS_MIN];
	static {
		for ( int i=INTS_MIN; i<=INTS_MAX; i++ )
			s_ints[i-INTS_MIN] = new LInteger(i);
	}
	
	/** Get an LInteger corresponding to a particular int value */
	public static LInteger valueOf(int n) {
		if ( n >= INTS_MIN && n <= INTS_MAX )
			return s_ints[n-INTS_MIN];
		return new LInteger(n);
	}

	/** use LInteger.valueOf() instead */
	private LInteger(int value) {
		this.m_value = value;
	}
	
	public final int hashCode() {
		return hashCodeOf( m_value );
	}
	
	public static int hashCodeOf( int v ) {
		return v;
	}
    
	public int luaAsInt() {
		return m_value;
	}
	
	public LString luaAsString() {
		return LString.valueOf(m_value);
	}
	
	public boolean isInteger() {
		return true;
	}
	
	// binary operations on integers, first dispatch
	public LValue luaBinOpUnknown(int opcode, LValue lhs) {
		return lhs.luaBinOpInteger( opcode, this.m_value );
	}
	
	// binary operations on integers
	public LValue luaBinOpInteger(int opcode, int rhs) {
		switch ( opcode ) {
		case Lua.OP_ADD: return LInteger.valueOf( m_value + rhs );
		case Lua.OP_SUB: return LInteger.valueOf( m_value - rhs );
		case Lua.OP_MUL: return LInteger.valueOf( m_value * rhs );
		case Lua.OP_DIV: return LInteger.valueOf( m_value / rhs );
		case Lua.OP_MOD: return LInteger.valueOf( m_value - ((int) Math.floor(m_value/(double)rhs)) * rhs );
		case Lua.OP_POW: return LInteger.valueOf( ipow(m_value, rhs) );
		}
		return luaUnsupportedOperation();
	}
	
	private static int ipow(int v, int rhs) {
		int p = 1;
		for ( ; rhs > 0; rhs>>=1, v=v*v )
			if ( (rhs & 1) != 0 )
				p *= v;
		return p;
	}

	// binary operations on mixed integer, double
	public LValue luaBinOpDouble(int opcode, double rhs) {
		return LDouble.luaBinOpDoubleDouble(opcode, (double) m_value, rhs );
	}
	
	// binary compare for integers, first dispatch
	public boolean luaBinCmpUnknown(int opcode, LValue lhs) {
		return lhs.luaBinCmpInteger( opcode, this.m_value );
	}
	
	// unsupported except for numbers
	public boolean luaBinCmpInteger(int opcode, int rhs) {
		switch ( opcode ) {
		case Lua.OP_EQ: return m_value == rhs;
		case Lua.OP_LT: return m_value < rhs;
		case Lua.OP_LE: return m_value <= rhs;
		}
		luaUnsupportedOperation();
		return false;
	}
	
	// unsupported except for numbers
	public boolean luaBinCmpDouble(int opcode, double rhs) {
		return LDouble.luaBinCmpDoubleDouble(opcode, (double) m_value, rhs );
	}

	/** Arithmetic negative */
	public LValue luaUnaryMinus() {
		return LInteger.valueOf( -m_value );
	}

}
