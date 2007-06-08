package lua.value;

public final class LNil extends LValue {
	public static final LNil NIL = new LNil();
	
	public final String luaAsString() {
		return "nil";
	}

	public boolean luaAsBoolean() {
		return false;
	}
}
