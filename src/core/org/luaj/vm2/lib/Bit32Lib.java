/*******************************************************************************
* Copyright (c) 2012 Luaj.org. All rights reserved.
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

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Subclass of LibFunction that implements the Lua standard {@code bit32} library.
 */
public class Bit32Lib extends OneArgFunction {

	public Bit32Lib() {
	}

	public LuaValue call(LuaValue env) {
		LuaTable t = new LuaTable();
		bind(t, Bit32LibV.class, new String[] {
			"band", "bnot", "bor", "btest", "bxor", "extract", "replace"
		});
		bind(t, Bit32Lib2.class, new String[] {
			"arshift", "lrotate", "lshift", "rrotate", "rshift"
		});
		env.set("bit32", t);
		PackageLib.instance.LOADED.set("bit32", t);
		return t;
	}

	static final class Bit32LibV extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			switch ( opcode ) {
			case 0: return Bit32Lib.band( args );
			case 1: return Bit32Lib.bnot( args );
			case 2: return Bit32Lib.bor( args );
			case 3: return Bit32Lib.btest( args );
			case 4: return Bit32Lib.bxor( args );
			case 5:
				return Bit32Lib.extract( args.checkint(1), args.checkint(2), args.optint(3, 1) );
			case 6:
				return Bit32Lib.replace( args.checkint(1), args.checkint(2),
						args.checkint(3), args.optint(4, 1) );
			}
			return NIL;
		}
	}

	static final class Bit32Lib2 extends TwoArgFunction {

		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			switch ( opcode ) {
			case 0: return Bit32Lib.arshift(arg1.checkint(), arg2.checkint());
			case 1: return Bit32Lib.lrotate(arg1.checkint(), arg2.checkint());
			case 2: return Bit32Lib.lshift(arg1.checkint(), arg2.checkint());
			case 3: return Bit32Lib.rrotate(arg1.checkint(), arg2.checkint());
			case 4: return Bit32Lib.rshift(arg1.checkint(), arg2.checkint());
			}
			return NIL;
		}
		
	}

	static LuaValue arshift(int x, int disp) {
		if (disp >= 0) {
			return bitsToValue(x >> disp);
		} else {
			return bitsToValue(x << -disp);
		}
	}

	static LuaValue rshift(int x, int disp) {
		if (disp >= 32 || disp <= -32) {
			return ZERO;
		} else if (disp >= 0) {
			return bitsToValue(x >>> disp);
		} else {
			return bitsToValue(x << -disp);
		}
	}

	static LuaValue lshift(int x, int disp) {
		if (disp >= 32 || disp <= -32) {
			return ZERO;
		} else if (disp >= 0) {
			return bitsToValue(x << disp);
		} else {
			return bitsToValue(x >>> -disp);
		}
	}

	static Varargs band( Varargs args ) {
		int result = -1;
		for ( int i = 1; i <= args.narg(); i++ ) {
			result &= args.checkint(i);
		}
		return bitsToValue( result );
	}

	static Varargs bnot( Varargs args ) {
		return bitsToValue( ~args.checkint(1) );
	}

	static Varargs bor( Varargs args ) {
		int result = 0;
		for ( int i = 1; i <= args.narg(); i++ ) {
			result |= args.checkint(i);
		}
		return bitsToValue( result );
	}

	static Varargs btest( Varargs args ) {
		int bits = -1;
		for ( int i = 1; i <= args.narg(); i++ ) {
			bits &= args.checkint(i);
		}
		return valueOf( bits != 0 );
	}

	static Varargs bxor( Varargs args ) {
		int result = 0;
		for ( int i = 1; i <= args.narg(); i++ ) {
			result ^= args.checkint(i);
		}
		return bitsToValue( result );
	}

	static LuaValue lrotate(int x, int disp) {
		if (disp < 0) {
			return rrotate(x, -disp);
		} else {
			disp = disp & 31;
			return bitsToValue((x << disp) | (x >>> (32 - disp)));
		}
	}

	static LuaValue rrotate(int x, int disp) {
		if (disp < 0) {
			return lrotate(x, -disp);
		} else {
			disp = disp & 31;
			return bitsToValue((x >>> disp) | (x << (32 - disp)));
		}
	}

	static LuaValue extract(int n, int field, int width) {
		if (field < 0) {
			argerror(2, "field cannot be negative");
		}
		if (width < 0) {
			argerror(3, "width must be postive");
		}
		if (field + width > 32) {
			error("trying to access non-existent bits");
		}
		return bitsToValue((n >>> field) & (-1 >>> (32 - width)));
	}

	static LuaValue replace(int n, int v, int field, int width) {
		if (field < 0) {
			argerror(3, "field cannot be negative");
		}
		if (width < 0) {
			argerror(4, "width must be postive");
		}
		if (field + width > 32) {
			error("trying to access non-existent bits");
		}
		int mask = (-1 >>> (32 - width)) << field;
		n = (n & ~mask) | ((v << field) & mask);
		return bitsToValue(n);
	}

	private static LuaValue bitsToValue( int x ) {
		return ( x < 0 ) ? valueOf((double) ((long) x & 0xFFFFFFFFL)) : valueOf(x); 
	}
}
