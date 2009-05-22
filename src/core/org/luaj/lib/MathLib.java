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

import java.util.Random;

import org.luaj.vm.LDouble;
import org.luaj.vm.LFunction;
import org.luaj.vm.LTable;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;


public class MathLib extends LFunction {

	public static final String[] NAMES = {
		"math",
		
		// irregular functions
		"max",
		"min",
		"modf",
		"ceil",
		"floor",
		"frexp",
		"ldexp",
		"random",
		"randomseed",

		// 2 argument, return double
		"atan2",
		"fmod",
		"pow",
		
		// single argument, return double
		"abs",
		"acos",
		"asin",
		"atan",
		"cos",
		"cosh",
		"deg",
		"exp",
		"log",
		"log10",
		"rad",
		"sin",
		"sinh",
		"sqrt",
		"tan",
		"tanh",
	};

	private static final int INSTALL = 0;

	// irregular functions
	public static final int MAX      = 1;
	public static final int MIN      = 2;
    public static final int MODF     = 3;
	public static final int CEIL     = 4;
	public static final int FLOOR    = 5;
    public static final int FREXP    = 6;
    public static final int LDEXP    = 7;
	public static final int RANDOM   = 8;
	public static final int RSEED    = 9;
    public static final int LAST_IRREGULAR = RSEED;
    
	// 2 argument, return double
    public static final int ATAN2    = 10;
    public static final int FMOD     = 11;
    public static final int POW      = 12;
    public static final int LAST_DOUBLE_ARG   = POW;
	
    /* Math operations - single argument, one function */
    public static final int ABS      = 13;
    public static final int ACOS     = 14;
    public static final int ASIN     = 15;
    public static final int ATAN     = 16;
    public static final int COS      = 17;
    public static final int COSH     = 18;
    public static final int DEG      = 19;
    public static final int EXP      = 20;
    public static final int LOG      = 21;
    public static final int LOG10    = 22;
    public static final int RAD      = 23;
    public static final int SIN      = 24;
    public static final int SINH     = 25;
    public static final int SQRT     = 26;
    public static final int TAN      = 27;
    public static final int TANH     = 28;

    private static Platform platform;
	
	public static void install( LTable globals ) {
		LTable math = new LTable();
		for ( int i=1; i<NAMES.length; i++ )
			math.put(NAMES[i], new MathLib(i));
		math.put( "huge", new LDouble( Double.MAX_VALUE ) );
		math.put( "pi", new LDouble( Math.PI ) );		
		globals.put( "math", math );
		PackageLib.setIsLoaded("math", math);
		platform = Platform.getInstance();
		
	}

	private static Random random = null;
	
	private final int id;

	private MathLib( int id ) {
		this.id = id;
	}
	
	public String toString() {
		return NAMES[id]+"()";
	}
	
	private static void setResult( LuaState vm, double d ) {
		vm.resettop();
		vm.pushlvalue( LDouble.numberOf(d) );
	}
	
	public int invoke(LuaState vm) {
		if ( id > LAST_DOUBLE_ARG ) {
			vm.pushlvalue( platform.mathop(id, vm.checknumber(1) ) );
			return 1;
		} else if ( id > LAST_IRREGULAR ) {
			vm.pushlvalue( platform.mathop(id, vm.checknumber(1), vm.checknumber(2) ) );
			return 1;
		} else {
			switch ( id ) {
			case INSTALL:
				install( vm._G );
				return 0;
			case MAX: {
				int n = vm.gettop();
				double x = vm.checkdouble(1);
				for ( int i=2; i<=n; i++ )
					x = Math.max(x, vm.checkdouble(i));
				vm.pushnumber( x );
				return 1;
			}
			case MIN: {
				int n = vm.gettop();
				double x = vm.checkdouble(1);
				for ( int i=2; i<=n; i++ )
					x = Math.min(x, vm.checkdouble(i));
				vm.pushnumber( x );
				return 1;
			}
			case MODF: {
				double x = vm.checkdouble(1);
				double intPart = ( x > 0 ) ? Math.floor( x ) : Math.ceil( x );
				double fracPart = x - intPart;
				vm.pushnumber( intPart );
				vm.pushnumber( fracPart );
				return 2;
			}
			case CEIL:
				vm.pushnumber( Math.ceil( vm.checkdouble(1) ) );
				return 1;
			case FLOOR:
				vm.pushnumber( Math.floor( vm.checkdouble(1) ) );
				return 1;
			case FREXP: {
				long bits = Double.doubleToLongBits( vm.checkdouble(1) );
				vm.pushnumber( ((bits & (~(-1L<<52))) + (1L<<52)) * ((bits >= 0)? (.5 / (1L<<52)): (-.5 / (1L<<52))) );
				vm.pushinteger( (((int) (bits >> 52)) & 0x7ff) - 1022 );
				return 2;
			}
			case LDEXP: {
				double m = vm.checkdouble(1);
				int e = vm.checkint(2);
				vm.pushnumber( m * Double.longBitsToDouble(((long)(e+1023)) << 52) );
				return 1;
			}
			case RANDOM: {
				if ( random == null )
					random = new Random(1);
				switch ( vm.gettop() ) {
				case 0:
					vm.pushnumber(random.nextDouble());
					return 1;
				case 1: {
					int m = vm.checkint(1);
					vm.argcheck(1<=m, 1, "interval is empty");
					vm.pushinteger(1+random.nextInt(m));
					return 1;
				}
				default: {
					int m = vm.checkint(1);
					int n = vm.checkint(2);
					vm.argcheck(m<=n, 2, "interval is empty");
					vm.pushinteger(m+random.nextInt(n+1-m));
					return 1;
				}
				}
			}
			case RSEED:
				random = new Random( vm.checkint(1) );
				return 0;		
			default:
				LuaState.vmerror( "bad math id" );
				return 0;
			}
		}
	}
}
