package lua.value;

import lua.Lua;

public final class LBoolean extends LValue {

	public static final LBoolean TRUE = new LBoolean("true",true);
	
	public static final LBoolean FALSE = new LBoolean("false",false);
	
	private final String m_sname;
	private final LString m_name;
	private final boolean m_value;
	
	private LBoolean( String name, boolean value ) {
		this.m_sname = name;
		this.m_name = new LString( name );
		this.m_value = value;
	}
	
	public final String toJavaString() {
		return m_sname;
	}
	
	public final LString luaAsString() {
		return m_name;
	}
	
	public final boolean toJavaBoolean() {
		return m_value;
	}
	
	public final int toJavaInt() {
		return m_value? 1: 0;
	}
	
	public final static LBoolean valueOf(boolean value) {
		return value? TRUE: FALSE;
	}
	
	public int luaGetType() {
		return Lua.LUA_TBOOLEAN;
	}
}
