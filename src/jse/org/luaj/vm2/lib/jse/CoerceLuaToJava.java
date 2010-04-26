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
package org.luaj.vm2.lib.jse;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;


public class CoerceLuaToJava {

	public static interface Coercion { 
		public Object coerce( LuaValue value );
		public int score( int paramType );
	};
	
	private static Map COERCIONS = new HashMap();
	private static Coercion OBJECT_COERCION;
	
	static {
		Coercion boolCoercion = new Coercion() {
			public Object coerce(LuaValue value) {
				return value.toboolean()? Boolean.TRUE: Boolean.FALSE;
			} 
			public int score(int paramType) {
				switch ( paramType ) {
				case LuaValue.TNIL:
				case LuaValue.TBOOLEAN:
					return 0;
				case LuaValue.TINT:
				case LuaValue.TNUMBER:
					return 1;
				default: 
					return 4;
				}
			}
		};
		Coercion byteCoercion = new Coercion() {
			public Object coerce(LuaValue value) {
				return new Byte( (byte) value.toint() );
			} 
			public int score(int paramType) {
				switch ( paramType ) {
				case LuaValue.TINT:
					return 1;
				case LuaValue.TNUMBER:
					return 2;
				default: 
					return 4;
				}
			}
		};
		Coercion charCoercion = new Coercion() {
			public Object coerce(LuaValue value) {
				return new Character( (char) value.toint() );
			} 
			public int score(int paramType) {
				switch ( paramType ) {
				case LuaValue.TINT:
					return 1;
				case LuaValue.TNUMBER:
					return 2;
				default: 
					return 4;
				}
			}
		};
		Coercion shortCoercion = new Coercion() {
			public Object coerce(LuaValue value) {
				return new Short( (short) value.toint() );
			} 
			public int score(int paramType) {
				switch ( paramType ) {
				case LuaValue.TINT:
					return 1;
				case LuaValue.TNUMBER:
					return 2;
				default: 
					return 4;
				}
			}
		};
		Coercion intCoercion = new Coercion() {
			public Object coerce(LuaValue value) {
				return new Integer( value.toint() );
			}
			public int score(int paramType) {
				switch ( paramType ) {
				case LuaValue.TINT:
					return 0;
				case LuaValue.TNUMBER:
					return 1;
				case LuaValue.TBOOLEAN:
				case LuaValue.TNIL:
					return 2;
				default: 
					return 4;
				}
			}
		};
		Coercion longCoercion = new Coercion() {
			public Object coerce(LuaValue value) {
				return new Long( value.tolong() );
			} 
			public int score(int paramType) {
				switch ( paramType ) {
				case LuaValue.TINT:
					return 1;
				case LuaValue.TNUMBER:
					return 2;
				default: 
					return 4;
				}
			}
		};
		Coercion floatCoercion = new Coercion() {
			public Object coerce(LuaValue value) {
				return new Float( value.tofloat() );
			}
			public int score( int paramType ) {
				switch ( paramType ) {
				case LuaValue.TINT:
				case LuaValue.TNUMBER:
					return 1;
				case LuaValue.TBOOLEAN:
					return 2;
				default:
					return 4;
				}
			}
		};
		Coercion doubleCoercion = new Coercion() {
			public Object coerce(LuaValue value) {
				return new Double( value.todouble() );
			}
			public int score(int paramType) {
				switch ( paramType ) {
				case LuaValue.TINT:
					return 1;
				case LuaValue.TNUMBER:
					return 0;
				case LuaValue.TBOOLEAN:
					return 2;
				default: 
					return 4;
				}
			}
		};
		Coercion stringCoercion = new Coercion() {
			public Object coerce(LuaValue value) {
				return value.tojstring();
			} 
			public int score(int paramType) {
				switch ( paramType ) {
				case LuaValue.TUSERDATA:
					return 0;
				default: 
					return 1;
				}
			}
		};
		Coercion objectCoercion = new Coercion() {
			public Object coerce(LuaValue value) {
				switch ( value.type() ) {
				case LuaValue.TUSERDATA:
					return value.optuserdata(Object.class, null);
				case LuaValue.TSTRING:
					return value.tojstring();
				case LuaValue.TINT:
					return new Integer(value.toint()); 
				case LuaValue.TNUMBER:
					return new Double(value.todouble());
				case LuaValue.TBOOLEAN:
					return value.toboolean()? Boolean.TRUE: Boolean.FALSE;
				case LuaValue.TNIL:
					return null;
				default:
					return value;
				}
			} 
			public int score(int paramType) {
				switch ( paramType ) {
				case LuaValue.TSTRING:
					return 0;
				default: 
					return 0x10;
				}
			}
		};
		COERCIONS.put( Boolean.TYPE, boolCoercion );
		COERCIONS.put( Boolean.class, boolCoercion );
		COERCIONS.put( Byte.TYPE, byteCoercion );
		COERCIONS.put( Byte.class, byteCoercion );
		COERCIONS.put( Character.TYPE, charCoercion );
		COERCIONS.put( Character.class, charCoercion );
		COERCIONS.put( Short.TYPE, shortCoercion );
		COERCIONS.put( Short.class, shortCoercion );
		COERCIONS.put( Integer.TYPE, intCoercion );
		COERCIONS.put( Integer.class, intCoercion );
		COERCIONS.put( Long.TYPE, longCoercion );
		COERCIONS.put( Long.class, longCoercion );
		COERCIONS.put( Float.TYPE, floatCoercion );
		COERCIONS.put( Float.class, floatCoercion );
		COERCIONS.put( Double.TYPE, doubleCoercion );
		COERCIONS.put( Double.class, doubleCoercion );
		COERCIONS.put( String.class, stringCoercion );
		COERCIONS.put( Object.class, objectCoercion );
	}
	

	/** Score a single parameter, including array handling */
	private static int scoreParam(int paramType, Class c) {
		if ( paramType == LuaValue.TUSERDATA && !c.isArray() ) 
			return 0;
		Coercion co = (Coercion) COERCIONS.get( c );
		if ( co != null ) {
			int b = LuajavaLib.paramBaseTypeFromParamType(paramType);
			int d = LuajavaLib.paramDepthFromParamType(paramType);
			return co.score( b ) * d;
		}
		if ( c.isArray() ) {
			Class typ = c.getComponentType();
			int d = LuajavaLib.paramDepthFromParamType(paramType);
			if ( d > 0 )
				return scoreParam( LuajavaLib.paramComponentTypeOfParamType(paramType), typ );
			else
				return 0x10 + (scoreParam(paramType, typ) << 8);
		}
		return 0x1000;
	}

	/** Do a conversion */
	static Object coerceArg(LuaValue a, Class c) {
		if ( a.isuserdata(c) ) 
			return a.touserdata(c);
		Coercion co = (Coercion) COERCIONS.get( c );
		if ( co != null ) {
			return co.coerce( a );
		}
		if ( c.isArray() ) {
			boolean istable = a.istable();
			int n = istable? a.length(): 1;
			Class typ = c.getComponentType();
			Object arr = Array.newInstance(typ, n);
			for ( int i=0; i<n; i++ ) {
				LuaValue ele = (istable? a.checktable().get(i+1): a);
				if ( ele != null )
					Array.set(arr, i, coerceArg(ele, typ));				
			}
			return arr;
		}
		if ( a.isnil() )
			return null;
		throw new LuaError("no coercion found for "+a.getClass()+" to "+c);
	}

	static Object[] coerceArgs(Varargs suppliedArgs, Class[] parameterTypes) {
		int n = parameterTypes.length;
		Object[] args = new Object[n];
		for ( int i=0; i<n; i++ )
			args[i] = coerceArg( suppliedArgs.arg(i+1), parameterTypes[i] );
		return args;
	}

	/*
	 * Score parameter types for match with supplied parameter list
	 * 
	 * 1) exact number of args
	 * 2) java has more args
	 * 3) java has less args
	 * 4) types coerce well
	 */
	static int scoreParamTypes(long paramssig, Class[] paramTypes) {
		int nargs = LuajavaLib.paramsCountFromSig(paramssig);
		int njava = paramTypes.length;
		int score = (njava == nargs? 0: njava > nargs? 0x4000: 0x8000);
		for ( int i=0; i<nargs && i<njava; i++ ) {
			int paramType = LuajavaLib.paramTypeFromSig(paramssig, i);
			Class c = paramTypes[i];
			int s = scoreParam( paramType, c );
			score += s;
		}
		return score;
	}
}
