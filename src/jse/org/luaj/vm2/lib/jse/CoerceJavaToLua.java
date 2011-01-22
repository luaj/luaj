/*******************************************************************************
* Copyright (c) 2009-2011 Luaj.org. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;

/**
 * Helper class to coerce values from Java to lua within the luajava library. 
 * 
 * @see LuajavaLib
 */
public class CoerceJavaToLua {
	
	public static interface Coercion { 
		public LuaValue coerce( Object javaValue );
	};
	
	static final Map COERCIONS = new HashMap();
	
	static {
		Coercion boolCoercion = new Coercion() {
			public LuaValue coerce( Object javaValue ) {
				Boolean b = (Boolean) javaValue;
				return b.booleanValue()? LuaValue.TRUE: LuaValue.FALSE;
			} 
		} ;
		Coercion intCoercion = new Coercion() {
			public LuaValue coerce( Object javaValue ) {
				Number n = (Number) javaValue;
				return LuaInteger.valueOf( n.intValue() );
			} 
		} ;
		Coercion charCoercion = new Coercion() {
			public LuaValue coerce( Object javaValue ) {
				Character c = (Character) javaValue;
				return LuaInteger.valueOf( c.charValue() );
			} 
		} ;
		Coercion doubleCoercion = new Coercion() {
			public LuaValue coerce( Object javaValue ) {
				Number n = (Number) javaValue;
				return LuaDouble.valueOf( n.doubleValue() );
			} 
		} ;
		Coercion stringCoercion = new Coercion() {
			public LuaValue coerce( Object javaValue ) {
				return LuaString.valueOf( javaValue.toString() );
			} 
		} ;
		COERCIONS.put( Boolean.class, boolCoercion );
		COERCIONS.put( Byte.class, intCoercion );
		COERCIONS.put( Character.class, charCoercion );
		COERCIONS.put( Short.class, intCoercion );
		COERCIONS.put( Integer.class, intCoercion );
		COERCIONS.put( Float.class, doubleCoercion );
		COERCIONS.put( Double.class, doubleCoercion );
		COERCIONS.put( String.class, stringCoercion );
	}

	public static LuaValue coerce(Object o) {
		if ( o == null )
			return LuaValue.NIL;
		Class clazz = o.getClass();
		Coercion c = (Coercion) COERCIONS.get( clazz );
		if ( c != null )
			return c.coerce( o );
		return LuajavaLib.toUserdata( o, clazz );
	}

}
