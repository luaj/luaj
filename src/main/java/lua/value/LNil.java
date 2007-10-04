package lua.value;

import lua.Lua;

public final class LNil extends LValue {
	public static final LNil NIL = new LNil();
	
	public final LString luaAsString() {
		return luaGetTypeName();
	}

	public boolean luaAsBoolean() {
		return false;
	}

	public int luaGetType() {
		return Lua.LUA_TNIL;
	}
	
	public int luaAsInt() {
		return 0;
	}

}
