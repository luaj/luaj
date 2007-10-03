package lua.value;

public final class LNil extends LValue {
	public static final LNil NIL = new LNil();
	public static final LString TYPE_NAME = new LString(Type.nil.toString());
	
	public final LString luaAsString() {
		return TYPE_NAME;
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
