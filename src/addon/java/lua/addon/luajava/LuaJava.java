package lua.addon.luajava;


/** LuaJava-like bindings to Java scripting. 
 * 
 * TODO: coerce types on way in and out, pick method base on arg count ant types.
 */
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import lua.CallFrame;
import lua.GlobalState;
import lua.value.LFunction;
import lua.value.LString;
import lua.value.LTable;
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
	public void luaStackCall(CallFrame call, int base, int top, int nresults) {
		String className;
		switch ( id ) {
		case BINDCLASS:
			className = call.stack[base+1].luaAsString();
			try {
				Class clazz = Class.forName(className);
				call.stack[base] = new LInstance( clazz, clazz );
				call.top = base+1;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			break;
		case NEWINSTANCE:
			className = call.stack[base+1].luaAsString();
			try {
				Class clazz = Class.forName(className);
				Constructor[] cons = clazz.getConstructors();
				Constructor con = cons[0];
				Class[] paramTypes = con.getParameterTypes();
				int paramsBase = base + 2;
				int nargs = top - paramsBase;
				int score = CoerceLuaToJava.scoreParamTypes( call.stack, paramsBase, nargs, paramTypes );
				for ( int i=1; i<cons.length; i++ ) {
					Constructor c = cons[i];
					Class[] p = c.getParameterTypes();
					int s = CoerceLuaToJava.scoreParamTypes( call.stack, paramsBase, nargs, p );
					if ( s < score ) {
						con = c;
						paramTypes = p;
						score = s;
					}
				}
				Object[] args = CoerceLuaToJava.coerceArgs( call, paramsBase, nargs, paramTypes );
				Object o = con.newInstance( args );
				call.stack[base] = new LInstance( o, clazz );
				call.top = base+1;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			break;
		default:
			luaUnsupportedOperation();
		}
		if (nresults >= 0)
			call.adjustTop(base + nresults);
	}
	
	public static class LInstance extends LValue {
		Object instance;
		private Class clazz;
		public LInstance(Object o, Class clazz) {
			this.instance = o;
			this.clazz = clazz;
		}
		public String luaAsString() {
			return instance.toString();
		}
		public void luaGetTable(CallFrame call, int base, LValue table, LValue key) {
			final String s = key.luaAsString();
			try {
				Field f = clazz.getField(s);
				Object o = f.get(instance);
				LValue v = CoerceJavaToLua.coerce( o );
				call.stack[base] = v;
				call.top = base + 1;
			} catch (NoSuchFieldException nsfe) {
				call.stack[base] = new LMethod(instance,clazz,s);
				call.top = base + 1;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		public void luaSetTable(CallFrame call, int base, LValue table, LValue key, LValue val) {
			Class c = instance.getClass();
			String s = key.luaAsString();
			try {
				Field f = c.getField(s);
				Object v = CoerceLuaToJava.coerceArg(val, f.getType());
				f.set(instance,v);
				call.top = base;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		public LString luaGetType() {
			return new LString("userdata");
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
		public void luaStackCall(CallFrame call, int base, int top, int nresults) {
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
						int s = CoerceLuaToJava.scoreParamTypes( call.stack, paramsBase, nargs, p );
						if ( s < score ) {
							meth = m;
							paramTypes = p;
							score = s;
						}
					}
				}
				Object[] args = CoerceLuaToJava.coerceArgs( call, paramsBase, nargs, paramTypes );
				Object result = meth.invoke( instance, args );
				call.stack[base] = CoerceJavaToLua.coerce(result);
				call.top = base + 1;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}