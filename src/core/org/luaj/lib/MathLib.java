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
package org.luaj.lib;

import org.luaj.vm.LDouble;
import org.luaj.vm.LFunction;
import org.luaj.vm.LInteger;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaState;


public class MathLib extends LFunction {

	public static final String[] NAMES = {
		"math",
		"abs",
		"cos",
		"max",
		"min",
		"modf",
		"sin",
		"sqrt",
		"ceil",
		"floor",
	};

	private static final int INSTALL = 0;
	private static final int ABS     = 1;
	private static final int COS     = 2;
	private static final int MAX     = 3;
	private static final int MIN     = 4;
	private static final int MODF    = 5;
	private static final int SIN     = 6;
	private static final int SQRT    = 7;
	private static final int CEIL    = 8;
	private static final int FLOOR   = 9;
	
	public static void install( LTable globals ) {
		LTable math = new LTable();
		for ( int i=1; i<NAMES.length; i++ )
			math.put(NAMES[i], new MathLib(i));
		math.put( "huge", new LDouble( Double.MAX_VALUE ) );
		math.put( "pi", new LDouble( Math.PI ) );		
		globals.put( "math", math );
	}

	private final int id;

	private MathLib( int id ) {
		this.id = id;
	}
	
	public String toString() {
		return NAMES[id]+"()";
	}
	
	private static void setResult( LuaState vm, LValue value ) {
		vm.settop(0);
		vm.pushlvalue( value );
	}
	
	public boolean luaStackCall( LuaState vm ) {
		switch ( id ) {
		case INSTALL:
			install( vm._G );
			break;
		case ABS:
			setResult( vm, abs( vm.topointer( 2 ) ) );
			break;
		case COS:
			setResult( vm, new LDouble( Math.cos ( vm.tonumber(2) ) ) );
			break;
		case MAX:
			setResult( vm, max( vm.topointer(2), vm.topointer(3) ) );
			break;
		case MIN:
			setResult( vm, min( vm.topointer(2), vm.topointer(3) ) );
			break;
		case MODF:
			modf( vm );
			break;
		case SIN:
			setResult( vm, new LDouble( Math.sin( vm.tonumber(2) ) ) );
			break;
		case SQRT:
			setResult( vm, new LDouble( Math.sqrt( vm.tonumber(2) ) ) );
			break;
		case CEIL:
			setResult( vm, LInteger.valueOf( (int) Math.ceil( vm.tonumber(2) ) ) );
			break;
		case FLOOR:
			setResult( vm, LInteger.valueOf( (int) Math.floor( vm.tonumber(2) ) ) );
			break;
		default:
			luaUnsupportedOperation();
		}
		return false;
	}
	private LValue abs( final LValue v ) {
		LValue nv = v.luaUnaryMinus();
		return max( v, nv );
	}
	
	private LValue max( LValue lhs, LValue rhs ) {
		return rhs.luaBinCmpUnknown( Lua.OP_LT, lhs ) ? rhs: lhs;
	}
	
	private LValue min( LValue lhs, LValue rhs ) {
		return rhs.luaBinCmpUnknown( Lua.OP_LT, lhs ) ? lhs: rhs;
	}
	
	private void modf( LuaState vm ) {
		LValue arg = vm.topointer(2);
		double v = arg.toJavaDouble();
		double intPart = ( v > 0 ) ? Math.floor( v ) : Math.ceil( v );
		double fracPart = v - intPart;
		vm.settop(0);
		vm.pushnumber( intPart );
		vm.pushnumber( fracPart );
	}
	
}
