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
    
	public int toJavaInt() {
		return m_value;
	}
	
	public long toJavaLong() {
		return m_value;
	}

	public float toJavaFloat() {
		return m_value;
	}

	public double toJavaDouble() {
		return m_value;
	}
	
	public LString luaAsString() {
		return LString.valueOf(m_value);
	}

	public String toJavaString() {
		return String.valueOf(m_value);
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
		case Lua.OP_DIV:
		case Lua.OP_MOD: 
		case Lua.OP_POW: 
			return LDouble.luaBinOpDoubleDouble(opcode, m_value, rhs);
		}
		LuaState.vmerror( "bad bin opcode" );
		return null;
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
		LuaState.vmerror( "bad cmp opcode" );
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
