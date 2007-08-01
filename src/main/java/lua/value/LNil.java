package lua.value;

public final class LNil extends LValue {
	public static final LNil NIL = new LNil();
	public static final LString TYPE_NAME = new LString("nil");
	
	public final String luaAsString() {
		return "nil";
	}

	public boolean luaAsBoolean() {
		return false;
	}

	public LString luaGetType() {
		return TYPE_NAME;
	}

	public int luaAsInt() {
		return 0;
	}

}
