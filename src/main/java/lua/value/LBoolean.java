package lua.value;

public final class LBoolean extends LValue {

	public static final LBoolean TRUE = new LBoolean("true",true);
	
	public static final LBoolean FALSE = new LBoolean("false",false);
	
	public static final LString TYPE_NAME = new LString("boolean");
	
	private final String m_name;
	private final boolean m_value;
	
	private LBoolean( String name, boolean value ) {
		this.m_name = name;
		this.m_value = value;
	}
	
	public final String luaAsString() {
		return m_name;
	}
	
	public final boolean luaAsBoolean() {
		return m_value;
	}
	
	public final int luaAsInt() {
		return m_value? 1: 0;
	}
	
	public final static LBoolean valueOf(boolean value) {
		return value? TRUE: FALSE;
	}
	
	public LString luaGetType() {
		return TYPE_NAME;
	}
}
