package lua.value;

import lua.StackState;

public class LString extends LValue {

	final String m_string;
	
	public LString(String string) {
		this.m_string = string;
	}

	// TODO: what to do with LuaState? 
	public LString(StackState l, String string) {
		this(string);
	}

	public String luaAsString() {
		return m_string;
	}
}
