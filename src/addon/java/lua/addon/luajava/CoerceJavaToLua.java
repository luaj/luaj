package lua.addon.luajava;

import java.util.HashMap;
import java.util.Map;

import lua.addon.luajava.LuaJava.LInstance;
import lua.value.LBoolean;
import lua.value.LDouble;
import lua.value.LInteger;
import lua.value.LNil;
import lua.value.LString;
import lua.value.LValue;

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
