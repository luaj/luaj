/**
 * 
 */
package lua;

/** LuaJava-like bindings to Java scripting. 
 * 
 * TODO: coerce types on way in and out, pick method base on arg count ant types.
 */
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import lua.value.LFunction;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LValue;

final class LuaJava extends LFunction {

	static void addBuiltins(LTable table) {
		LTable luajava = new LTable();
		for ( int i=0; i<NAMES.length; i++ )
			luajava.m_hash.put( new LString( NAMES[i] ), new LuaJava(i) );
		table.m_hash.put( new LString( "luajava" ), luajava );
	}

	private static final int NEWINSTANCE  = 0;
	private static final int BINDCLASS    = 1;
	private static final int NEW          = 2;
	private static final int CREATEPROXY  = 3;
	private static final int LOADLIB      = 4;
	
	private static final String[] NAMES = { "newInstance", "bindClass", "new", "createProxy", "loadLib" };
	
	private int id;
	private LuaJava( int id ) {			
		this.id = id;
	}

	public String toString() {
		return "builtin."+NAMES[id];
	}
	
	// perform a lua call
	public void luaStackCall(StackState state, int base, int top, int nresults) {
		switch ( id ) {
		case NEWINSTANCE:
			String className = state.stack[base+1].luaAsString();
				try {
					Class c = Class.forName(className);
					Object o = c.newInstance();
					state.stack[base] = new LInstance( o );
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
		private Object instance;
		public LInstance(Object o) {
			this.instance = o;
		}
		public String luaAsString() {
			return instance.toString();
		}
		public void luaGetTable(StackState state, int base, LValue table, LValue key) {
			Class c = instance.getClass();
			final String s = key.luaAsString();
			try {
				Field f = c.getField(s);
				Object o = f.get(instance);
				String v = String.valueOf(o);
				state.stack[base] = new LString( v );
				state.top = base + 1;
			} catch (NoSuchFieldException nsfe) {
				state.stack[base] = new LMethod(instance,s);
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
				String v = val.luaAsString();
				f.set(instance,v);
				state.top = base;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	private static final class LMethod extends LFunction {
		private final Object instance;
		private final String s;
		private LMethod(Object instance, String s) {
			this.instance = instance;
			this.s = s;
		}

		public void luaStackCall(StackState state, int base, int top, int nresults) {
			Class c = instance.getClass();
			try {
				Method m = c.getMethod(s,new Class[0]);
				Object o = m.invoke(instance,new Object[0]);
				String v = String.valueOf(o);
				state.stack[base] = new LString( v );
				state.top = base + 1;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}