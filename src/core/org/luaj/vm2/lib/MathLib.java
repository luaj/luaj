/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
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
package org.luaj.vm2.lib;

import java.util.Random;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/** 
 * Base math library with JME support.  
 * 
 * For j2se support use org.luaj.lib.j2se.MathLib
 * 
 * @see org.luaj.vm2.lib.jse.JseMathLib
 */
public class MathLib extends OneArgFunction {
	
	public static MathLib MATHLIB = null;

	private Random random;
	
	public MathLib() {
		MATHLIB = this;
	}

	public LuaValue call(LuaValue arg) {
		LuaTable t = new LuaTable(0,30);
		t.set( "pi", Math.PI );
		t.set( "huge", LuaDouble.POSINF );
		bind( t, MathLib1.class, new String[] {
			"abs", "ceil", "cos", "deg", 
			"exp", "floor", "rad", "sin", 
			"sqrt", "tan" } );
		bind( t, MathLib2.class, new String[] {
			"fmod", "ldexp", "pow", } );
		bind( t, MathLibV.class, new String[] {
			"frexp", "max", "min", "modf", 
			"randomseed", "random", } );
		((MathLibV) t.get("randomseed")).mathlib = this;
		((MathLibV) t.get("random"    )).mathlib = this;
		env.set("math", t);
		return t;
	}

	public static final class MathLib1 extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			switch ( opcode ) {
			case 0: return valueOf(Math.abs(arg.checkdouble())); 
			case 1: return valueOf(Math.ceil(arg.checkdouble())); 
			case 2: return valueOf(Math.cos(arg.checkdouble())); 
			case 3: return valueOf(Math.toDegrees(arg.checkdouble())); 
			case 4: return dpow(Math.E,arg.checkdouble());
			case 5: return valueOf(Math.floor(arg.checkdouble()));
			case 6: return valueOf(Math.toRadians(arg.checkdouble())); 
			case 7: return valueOf(Math.sin(arg.checkdouble())); 
			case 8: return valueOf(Math.sqrt(arg.checkdouble())); 
			case 9: return valueOf(Math.tan(arg.checkdouble())); 
			}
			return NIL;
		}
	}
	
	public static final class MathLib2 extends TwoArgFunction {
		protected MathLib mathlib;
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			switch ( opcode ) {
			case 0: { // fmod
				double x = arg1.checkdouble();
				double y = arg2.checkdouble();
				double q = x/y;
				double f = x - y * (q>=0? Math.floor(q): Math.ceil(q));
				return valueOf( f );
			}
			case 1: { // ldexp
				double x = arg1.checkdouble();
				double y = arg2.checkdouble()+1023.5;
				long e = (long) ((0!=(1&((int)y)))? Math.floor(y): Math.ceil(y-1));
				return valueOf(x * Double.longBitsToDouble(e << 52));
			}
			case 2: { // pow
				return dpow(arg1.checkdouble(), arg2.checkdouble());
			}
			}
			return NIL;
		}
	}

	/** compute power using installed math library, or default if there is no math library installed */
	public static LuaValue dpow(double a, double b) {
		return LuaDouble.valueOf( 
				MATHLIB!=null?
				MATHLIB.dpow_d(a,b):
				dpow_default(a,b) );
	}
	
	/** 
	 * Hook to override default dpow behavior with faster implementation.  
	 */
	public double dpow_d(double a, double b) {
		return dpow_default(a,b);
	}

	/** 
	 * Default JME version computes using longhand heuristics. 
	 */
	protected static double dpow_default(double a, double b) {
		if ( b < 0 )
			return 1 / dpow_default( a, -b );
		double p = 1;
		int whole = (int) b;
		for ( double v=a; whole > 0; whole>>=1, v*=v )
			if ( (whole & 1) != 0 )
				p *= v;
		if ( (b -= whole) > 0 ) {
			int frac = (int) (0x10000 * b);
			for ( ; (frac&0xffff)!=0; frac<<=1 ) {
				a = Math.sqrt(a);
				if ( (frac & 0x8000) != 0 )
					p *= a;
			}
		}
		return p;
	}

	public static final class MathLibV extends VarArgFunction {
		protected MathLib mathlib;
		public Varargs invoke(Varargs args) {
			switch ( opcode ) {
			case 0: { // frexp
				double x = args.checkdouble(1);
				if ( x == 0 ) return varargsOf(ZERO,ZERO);
				long bits = Double.doubleToLongBits( x );
				double m = ((bits & (~(-1L<<52))) + (1L<<52)) * ((bits >= 0)? (.5 / (1L<<52)): (-.5 / (1L<<52)));
				double e = (((int) (bits >> 52)) & 0x7ff) - 1022;
				return varargsOf( valueOf(m), valueOf(e) );
			}
			case 1: { // max
				double m = args.checkdouble(1);
				for ( int i=2,n=args.narg(); i<=n; ++i )
					m = Math.max(m,args.checkdouble(i));
				return valueOf(m);
			}
			case 2: { // min
				double m = args.checkdouble(1);
				for ( int i=2,n=args.narg(); i<=n; ++i )
					m = Math.min(m,args.checkdouble(i));
				return valueOf(m);
			}
			case 3: { // modf
				double x = args.checkdouble(1);
				double intPart = ( x > 0 ) ? Math.floor( x ) : Math.ceil( x );
				double fracPart = x - intPart;
				return varargsOf( valueOf(intPart), valueOf(fracPart) );
			}
			case 4: { // randomseed 
				long seed = args.checklong(1);
				mathlib.random = new Random(seed);
				return NONE;
			}
			case 5: { // random
				if ( mathlib.random == null )
					mathlib.random = new Random();
				
				switch ( args.narg() ) {
				case 0:
					return valueOf( mathlib.random.nextDouble() );
				case 1: {
					int m = args.checkint(1);
					if (m<1) argerror(1, "interval is empty");
					return valueOf( 1 + mathlib.random.nextInt(m) );
				}
				default: {
					int m = args.checkint(1);
					int n = args.checkint(2);
					if (n<m) argerror(2, "interval is empty");
					return valueOf( m + mathlib.random.nextInt(n+1-m) );
				}
				}
			}
			}
			return NONE;
		}
	}
}
