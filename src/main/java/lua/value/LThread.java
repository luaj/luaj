package lua.value;

import lua.Lua;

public class LThread extends LValue {
	
	public int luaGetType() {
		return Lua.LUA_TTHREAD;
	}
	
	public LString luaAsString() {
		return new LString("thread: "+hashCode());
	}
}
