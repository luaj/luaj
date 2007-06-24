package lua.addon.luajava;


/** LuaJava-like bindings to Java scripting. 
 * 
 * TODO: coerce types on way in and out, pick method base on arg count ant types.
 */
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import lua.GlobalState;
import lua.StackState;
import lua.value.LFunction;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LValue;

public final class LuaJava extends LFunction {

	public static void install() {
		LTable luajava = new LTable();
		for ( int i=0; i<NAMES.length; i++ )
			luajava.m_hash.put( new LString( NAMES[i] ), new LuaJava(i) );
		GlobalState.getGlobalsTable().m_hash.put( new LString( "luajava" ), luajava );
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
	public void luaStackCall(StackState state, int base, int top, int nresults) {
		String className;
		switch ( id ) {
		case BINDCLASS:
			className = state.stack[base+1].luaAsString();
			try {
				Class clazz = Class.forName(className);
				state.stack[base] = new LInstance( clazz, clazz );
				state.top = base+1;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			break;
		case NEWINSTANCE:
			className = state.stack[base+1].luaAsString();
			try {
				Class clazz = Class.forName(className);
				Constructor[] cons = clazz.getConstructors();
				Constructor con = cons[0];
				Class[] paramTypes = con.getParameterTypes();
				int paramsBase = base + 2;
				int nargs = top - paramsBase;
				int score = CoerceLuaToJava.scoreParamTypes( state.stack, paramsBase, nargs, paramTypes );
				for ( int i=1; i<cons.length; i++ ) {
					Constructor c = cons[i];
					Class[] p = c.getParameterTypes();
					int s = CoerceLuaToJava.scoreParamTypes( state.stack, paramsBase, nargs, p );
					if ( s < score ) {
						con = c;
						paramTypes = p;
						score = s;
					}
				}
				Object[] args = CoerceLuaToJava.coerceArgs( state, paramsBase, nargs, paramTypes );
				Object o = con.newInstance( args );
				state.stack[base] = new LInstance( o, clazz );
				state.top = base+1;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			break;
		default:
			luaUnsupportedOperation();
		}
		if (nresults >= 0)
			state.adjustTop(base + nresults);
	}
	
	static class LInstance extends LValue {
		Object instance;
		private Class clazz;
		public LInstance(Object o, Class clazz) {
			this.instance = o;
			this.clazz = clazz;
		}
		public String luaAsString() {
			return instance.toString();
		}
		public void luaGetTable(StackState state, int base, LValue table, LValue key) {
			final String s = key.luaAsString();
			try {
				Field f = clazz.getField(s);
				Object o = f.get(instance);
				LValue v = CoerceJavaToLua.coerce( o );
				state.stack[base] = v;
				state.top = base + 1;
			} catch (NoSuchFieldException nsfe) {
				state.stack[base] = new LMethod(instance,clazz,s);
				state.top = base + 1;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		public void luaSetTable(StackState state, int base, LValue table, LValue key, LValue val) {
			Class c = instance.getClass();
			String s = key.luaAsString();
			try {
				Field f = c.getField(s);
				Object v = CoerceLuaToJava.coerceArg(val, f.getType());
				f.set(instance,v);
				state.top = base;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
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
		public void luaStackCall(StackState state, int base, int top, int nresults) {
			try {
				Method[] meths = clazz.getMethods();
				Method meth = null;
				Class[] paramTypes = null;
				int score = Integer.MAX_VALUE;
				int paramsBase = base + 2;
				int nargs = top - paramsBase;
				for ( int i=0; i<meths.length; i++ ) {
					Method m = meths[i];
					String name = m.getName();
					if ( s.equals(name) ) {
						Class[] p = m.getParameterTypes();
						int s = CoerceLuaToJava.scoreParamTypes( state.stack, paramsBase, nargs, p );
						if ( s < score ) {
							meth = m;
							paramTypes = p;
							score = s;
						}
					}
				}
				Object[] args = CoerceLuaToJava.coerceArgs( state, paramsBase, nargs, paramTypes );
				Object result = meth.invoke( instance, args );
				state.stack[base] = CoerceJavaToLua.coerce(result);
				state.top = base + 1;
				state.adjustTop(base+nresults);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}