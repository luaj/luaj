package lua.value;

public final class LBoolean extends LValue {

	public static final LBoolean TRUE = new LBoolean("true",true);
	
	public static final LBoolean FALSE = new LBoolean("false",false);
	
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
	
	public final static LBoolean valueOf(boolean value) {
		return value? TRUE: FALSE;
	}
}
