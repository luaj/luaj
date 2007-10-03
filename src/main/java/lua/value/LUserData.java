package lua.value;

public class LUserData extends LValue {
	public static final LString TYPE_NAME = new LString(Type.userdata.toString());
	
	public final Object m_instance;
	public LTable m_metatable;
	
	public LUserData(Object obj) {
		m_instance = obj;
	}
	
	public LString luaAsString() {
		return new LString(m_instance.toString());
	}
	
	public boolean equals(Object obj) {
		return (this == obj) ||
			(obj instanceof LUserData && this.m_instance == ((LUserData) obj).m_instance);
	}
	
	public int hashCode() {
		return System.identityHashCode( m_instance );
	}

	public LString luaGetType() {
		return TYPE_NAME;
	}
	
	public LTable luaGetMetatable() {
		return m_metatable; 
	}
}
