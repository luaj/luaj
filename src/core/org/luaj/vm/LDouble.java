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


public class LDouble extends LNumber {
    
	private final double m_value;
	
	public static LDouble valueOf(double value) {
		return new LDouble(value);
	}
	
	public LDouble(double value) {
		this.m_value = value;
	}
    
	public int hashCode() {
		if ( m_value == 0 ) {
			return 0;
		} else {
			long bits = Double.doubleToLongBits( m_value );
			return ( (int) bits >> 32 ) + ( (int) bits );
		}
	}
	
	public String toJavaString() {
		if ( Double.isNaN(m_value) )
			return "nan";
		if ( Double.isInfinite(m_value) )
			return (m_value>0? "inf": "-inf");
		if ( m_value == 0.0 ) {
			long bits = Double.doubleToLongBits( m_value );
			return ( bits >> 63 == 0 ) ? "0" : "-0";
		}
		long l = (long) m_value;
		if ( (m_value == (double) l) && (m_value <= Long.MAX_VALUE) && (m_value >= Long.MIN_VALUE) ) {
			return Long.toString( l );
		} else {
			return Double.toString( m_value );
		}
	}
	
	// return true if value survives as an integer
	public boolean isInteger() {
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
		case Lua.OP_POW: return Platform.getInstance().mathPow(lhs, rhs);
		}
		LuaState.vmerror( "bad bin opcode" );
		return null;
	}

	public int toJavaInt() {
		return (int) m_value;
	}

	public double toJavaDouble() {
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
		LuaState.vmerror( "bad cmp opcode" );
		return false;
	}

	/** Arithmetic negative */
	public LValue luaUnaryMinus() {
		return new LDouble( -m_value );
	}
	
}
