package org.luaj.lib.j2se;

import java.util.HashMap;
import java.util.Map;

import org.luaj.vm.LBoolean;
import org.luaj.vm.LDouble;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNil;
import org.luaj.vm.LNumber;
import org.luaj.vm.LString;
import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;


public class CoerceLuaToJava {

	public static interface Coercion { 
		public Object coerce( LValue value );
		public int score( LValue value );
	};
	
	private static Map COERCIONS = new HashMap();
	private static Coercion OBJECT_COERCION;
	
	static {
		Coercion boolCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return value.toJavaBoolean()? Boolean.TRUE: Boolean.FALSE;
			} 
			public int score(LValue value) {
				if ( value instanceof LBoolean || value == LNil.NIL )
					return 0;
				if ( value instanceof LNumber )
					return 1;
				return 4;
			}
		};
		Coercion intCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return Integer.valueOf( value.toJavaInt() );
			}
			public int score(LValue value) {
				if ( value instanceof LInteger )
					return 0;
				if ( value instanceof LNumber )
					return 1;
				if ( value instanceof LBoolean || value == LNil.NIL )
					return 2;
				return 4;
			}
		};
		Coercion doubleCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return Double.valueOf( value.toJavaDouble() );
			}
			public int score(LValue value) {
				if ( value instanceof LDouble )
					return 0;
				if ( value instanceof LNumber )
					return 1;
				if ( value instanceof LBoolean || value == LNil.NIL )
					return 2;
				return 4;
			}
		};
		Coercion stringCoercion = new Coercion() {
			public Object coerce(LValue value) {
				return value.toJavaString();
			} 
			public int score(LValue value) {
				if ( value instanceof LUserData )
					return 0;
				return 1;
			}
		};
		Coercion objectCoercion = new Coercion() {
			public Object coerce(LValue value) {
				if ( value instanceof LUserData )
					return ((LUserData)value).m_instance;
				if ( value instanceof LString )
					return value.toJavaString();
				if ( value instanceof LInteger )
					return Integer.valueOf(value.toJavaInt());
				if ( value instanceof LDouble )
					return Double.valueOf(value.toJavaDouble());
				if ( value instanceof LBoolean )
					return Boolean.valueOf(value.toJavaBoolean());
				if ( value == LNil.NIL )
					return null;
				return value;
			} 
			public int score(LValue value) {
				if ( value instanceof LString )
					return 0;
				return 0x10;
			}
		};
		COERCIONS.put( Boolean.TYPE, boolCoercion );
		COERCIONS.put( Boolean.class, boolCoercion );
		COERCIONS.put( Byte.TYPE, intCoercion );
		COERCIONS.put( Byte.class, intCoercion );
		COERCIONS.put( Short.TYPE, intCoercion );
		COERCIONS.put( Short.class, intCoercion );
		COERCIONS.put( Integer.TYPE, intCoercion );
		COERCIONS.put( Integer.class, intCoercion );
		COERCIONS.put( Long.TYPE, intCoercion );
		COERCIONS.put( Long.class, intCoercion );
		COERCIONS.put( Double.TYPE, doubleCoercion );
		COERCIONS.put( Double.class, doubleCoercion );
		COERCIONS.put( String.class, stringCoercion );
		COERCIONS.put( Object.class, objectCoercion );
	}
	
	static Object coerceArg(LValue v, Class type) {
		Coercion co = (Coercion) COERCIONS.get( type );
		if ( co != null )
			return co.coerce( v );
		if ( v instanceof LUserData )
			return ((LUserData) v).m_instance;
		return v;
	}

	static Object[] coerceArgs(LValue[] suppliedArgs, Class[] parameterTypes) {
		int nargs = suppliedArgs.length;
		int n = parameterTypes.length;
		Object[] args = new Object[n];
		for ( int i=0; i<n && i<nargs; i++ )
			args[i] = coerceArg( suppliedArgs[i], parameterTypes[i] );
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
	static int scoreParamTypes(LValue[] suppliedArgs, Class[] paramTypes) {
		int nargs = suppliedArgs.length;
		int njava = paramTypes.length;
		int score = (njava == nargs? 0: njava > nargs? 0x4000: 0x8000);
		for ( int i=0; i<nargs && i<njava; i++ ) {
			LValue a = suppliedArgs[i];
			Class c = paramTypes[i];
			Coercion co = (Coercion) COERCIONS.get( c );
			if ( co != null ) {
				score += co.score( a );
			} else if ( a instanceof LUserData ) {
				Object o = ((LUserData) a).m_instance;
				if ( ! c.isAssignableFrom(o.getClass()) )
						score += 0x10000;
			} else {
				score += 0x100;
			}
		}
		return score;
	}

}
