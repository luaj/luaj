package lua.addon.luajava;


/** LuaJava-like bindings to Java scripting. 
 * 
 * TODO: coerce types on way in and out, pick method base on arg count ant types.
 */
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lua.GlobalState;
import lua.VM;
import lua.value.LFunction;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LUserData;
import lua.value.LValue;

public final class LuaJava extends LFunction {

	public static void install() {
		LTable luajava = new LTable();
		for ( int i=0; i<NAMES.length; i++ )
			luajava.put( NAMES[i], new LuaJava(i) );
		GlobalState.getGlobalsTable().put( "luajava", luajava );
	}

	private static final int NEWINSTANCE  = 0;
	private static final int BINDCLASS    = 1;
	private static final int NEW          = 2;
	private static final int CREATEPROXY  = 3;
	private static final int LOADLIB      = 4;
	
	private static final String[] NAMES = { 
		"newInstance", 
		"bindClass", 
		"new", 
		"createProxy", 
		"loadLib" };
	
	private int id;
	private LuaJava( int id ) {			
		this.id = id;
	}

	public String toString() {
		return "luajava."+NAMES[id];
	}
	
	// perform a lua call
	public boolean luaStackCall(VM vm) {
		String className;
		switch ( id ) {
		case BINDCLASS:
			className = vm.getArgAsString(0);
			try {
				Class clazz = Class.forName(className);
				vm.setResult( new LInstance( clazz, clazz ) );
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			break;
		case NEWINSTANCE:
			className = vm.getArgAsString(0);
			try {
				// get constructor
				Class clazz = Class.forName(className);
				ParamsList params = new ParamsList( vm );
				Constructor con = resolveConstructor( clazz, params );

				// coerce args 
				Object[] args = CoerceLuaToJava.coerceArgs( params.values, con.getParameterTypes() );
				Object o = con.newInstance( args );
				
				// set the result
				vm.setResult( new LInstance( o, clazz ) );
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			break;
		default:
			luaUnsupportedOperation();
		}
		return false;
	}

	public static class ParamsList {
		public final LValue[] values;
		public final Class[] classes;
		public int hash;
		ParamsList( VM vm ) {
			int n = vm.getArgCount()-1;
			values = new LValue[n];
			classes = new Class[n];
			for ( int i=0; i<n; i++ ) {
				values[i] = vm.getArg(i+1);
				classes[i] = values[i].getClass();
				hash += classes[i].hashCode();
			}
		}
		public int hashCode() {
			return hash;
		}
		public boolean equals( Object o ) {
			return ( o instanceof ParamsList )? 
				Arrays.equals( classes, ((ParamsList) o).classes ):
				false;
		}
	}
	
	public static class LInstance extends LUserData {
		private Class clazz;
		public LInstance(Object o, Class clazz) {
			super(o);
			this.clazz = clazz;
		}
		public void luaGetTable(VM vm, LValue table, LValue key) {
			final String s = key.luaAsString().toJavaString();
			try {
				Field f = clazz.getField(s);
				Object o = f.get(m_instance);
				LValue v = CoerceJavaToLua.coerce( o );
				vm.push( v );
			} catch (NoSuchFieldException nsfe) {
				vm.push( new LMethod(m_instance,clazz,s) );
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		public void luaSetTable(VM vm, LValue table, LValue key, LValue val) {
			Class c = m_instance.getClass();
			String s = key.luaAsString().toJavaString();
			try {
				Field f = c.getField(s);
				Object v = CoerceLuaToJava.coerceArg(val, f.getType());
				f.set(m_instance,v);
				vm.setResult();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public boolean luaStackCall(VM vm) {
			// TODO Auto-generated method stub
			return super.luaStackCall(vm);
		}
		
	}

	private static final class LMethod extends LFunction {
		private final Object instance;
		private final Class clazz;
		private final String s;
		private LMethod(Object instance, Class clazz, String s) {
			this.instance = instance;
			this.clazz = clazz;
			this.s = s;
		}
		public String toString() {
			return clazz.getName()+"."+s+"()";
		}
		public boolean luaStackCall(VM vm) {
			try {
				// find the method 
				ParamsList params = new ParamsList( vm );
				Method meth = resolveMethod( clazz, s, params );

				// coerce the arguments
				Object[] args = CoerceLuaToJava.coerceArgs( params.values, meth.getParameterTypes() );
				Object result = meth.invoke( instance, args );
				
				// coerce the result
				vm.setResult( CoerceJavaToLua.coerce(result) );
				return false;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static Map consCache =
		new HashMap();
	
	private static Map consIndex =
		new HashMap();
	
	private static Constructor resolveConstructor(Class clazz, ParamsList params ) {

		// get the cache
		Map cache = (Map) consCache.get( clazz );
		if ( cache == null )
			consCache.put( clazz, cache = new HashMap() );
		
		// look up in the cache
		Constructor c = (Constructor) cache.get( params );
		if ( c != null )
			return c;

		// get index
		Map index = (Map) consIndex.get( clazz );
		if ( index == null ) {
			consIndex.put( clazz, index = new HashMap() );
			Constructor[] cons = clazz.getConstructors();
			for ( int i=0; i<cons.length; i++ ) {
				Constructor con = cons[i];
				Integer n = Integer.valueOf( con.getParameterTypes().length );
				List list = (List) index.get(n);
				if ( list == null )
					index.put( n, list = new ArrayList() );
				list.add( con );
			}
		}
		
		// figure out best list of arguments == supplied args
		Integer n = Integer.valueOf( params.classes.length );
		List list = (List) index.get(n);
		if ( list == null )
			throw new IllegalArgumentException("no constructor with "+n+" args");

		// find constructor with best score
		int bests = Integer.MAX_VALUE;
		int besti = 0;
		for ( int i=0, size=list.size(); i<size; i++ ) {
			Constructor con = (Constructor) list.get(i);
			int s = CoerceLuaToJava.scoreParamTypes(params.values, con.getParameterTypes());
			if ( s < bests ) {
				 bests = s;
				 besti = i;
			}
		}
		
		// put into cache
		c = (Constructor) list.get(besti);
		cache.put( params, c );
		return c;
	}

	
	private static Map methCache = 
		new HashMap();
	
	private static Map methIndex = 
		new HashMap();

	private static Method resolveMethod(Class clazz, String methodName, ParamsList params ) {

		// get the cache
		Map nameCache = (Map) methCache.get( clazz );
		if ( nameCache == null )
			methCache.put( clazz, nameCache = new HashMap() );
		Map cache = (Map) nameCache.get( methodName );
		if ( cache == null )
			nameCache.put( methodName, cache = new HashMap() );
		
		// look up in the cache
		Method m = (Method) cache.get( params );
		if ( m != null )
			return m;

		// get index
		Map index = (Map) methIndex.get( clazz );
		if ( index == null ) {
			methIndex.put( clazz, index = new HashMap() );
			Method[] meths = clazz.getMethods();
			for ( int i=0; i<meths.length; i++ ) {
				Method meth = meths[i];
				String s = meth.getName();
				Integer n = Integer.valueOf(meth.getParameterTypes().length);
				Map map = (Map) index.get(s);
				if ( map == null )
					index.put( s, map = new HashMap() );
				List list = (List) map.get(n);
				if ( list == null )
					map.put( n, list = new ArrayList() );
				list.add( meth );
			}
		}
		
		// figure out best list of arguments == supplied args
		Map map = (Map) index.get(methodName);
		if ( map == null )
			throw new IllegalArgumentException("no method named '"+methodName+"'");
		Integer n = Integer.valueOf( params.classes.length );
		List list = (List) map.get(n);
		if ( list == null )
			throw new IllegalArgumentException("no method named '"+methodName+"' with "+n+" args");

		// find constructor with best score
		int bests = Integer.MAX_VALUE;
		int besti = 0;
		for ( int i=0, size=list.size(); i<size; i++ ) {
			Method meth = (Method) list.get(i);
			int s = CoerceLuaToJava.scoreParamTypes(params.values, meth.getParameterTypes());
			if ( s < bests ) {
				 bests = s;
				 besti = i;
			}
		}
		
		// put into cache
		m = (Method) list.get(besti);
		cache.put( params, m );
		return m;
	}
	
}