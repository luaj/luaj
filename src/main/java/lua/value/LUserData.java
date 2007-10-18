package lua.value;

import lua.Lua;

public class LUserData extends LValue {
	
	public final Object m_instance;
	public LTable m_metatable;
	
	public LUserData(Object obj) {
		m_instance = obj;
	}
	
	public String toJavaString() {
		return String.valueOf(m_instance);
	}
	
	public boolean equals(Object obj) {
		return (this == obj) ||
			(obj instanceof LUserData && this.m_instance == ((LUserData) obj).m_instance);
	}
	
	public int hashCode() {
		return System.identityHashCode( m_instance );
	}

	public int luaGetType() {
		return Lua.LUA_TUSERDATA;
	}
	
	public LTable luaGetMetatable() {
		return m_metatable; 
	}

	public Object toJavaInstance() {
		return m_instance;
	}
}
