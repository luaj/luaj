package org.luaj.lib.j2se;

import java.util.HashMap;
import java.util.Map;

import org.luaj.lib.j2se.LuajavaLib.LInstance;
import org.luaj.vm.LBoolean;
import org.luaj.vm.LDouble;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LValue;


public class CoerceJavaToLua {
	
	public static interface Coercion { 
		public LValue coerce( Object javaValue );
	};
	
	private static Map COERCIONS = new HashMap();
	
	static {
		Coercion boolCoercion = new Coercion() {
			public LValue coerce( Object javaValue ) {
				Boolean b = (Boolean) javaValue;
				return b.booleanValue()? LBoolean.TRUE: LBoolean.FALSE;
			} 
		} ;
		Coercion intCoercion = new Coercion() {
			public LValue coerce( Object javaValue ) {
				Number n = (Number) javaValue;
				return LInteger.valueOf( n.intValue() );
			} 
		} ;
		Coercion doubleCoercion = new Coercion() {
			public LValue coerce( Object javaValue ) {
				Number n = (Number) javaValue;
				return new LDouble( n.doubleValue() );
			} 
		} ;
		Coercion stringCoercion = new Coercion() {
			public LValue coerce( Object javaValue ) {
				return new LString( javaValue.toString() );
			} 
		} ;
		COERCIONS.put( Boolean.class, boolCoercion );
		COERCIONS.put( Byte.class, intCoercion );
		COERCIONS.put( Short.class, intCoercion );
		COERCIONS.put( Integer.class, intCoercion );
		COERCIONS.put( Float.class, doubleCoercion );
		COERCIONS.put( Double.class, doubleCoercion );
		COERCIONS.put( String.class, stringCoercion );
	}

	public static LValue coerce(Object o) {
		if ( o == null )
			return LNil.NIL;
		Class clazz = o.getClass();
		Coercion c = (Coercion) COERCIONS.get( clazz );
		if ( c != null )
			return c.coerce( o );
		return new LInstance( o, o.getClass() );
	}

}
