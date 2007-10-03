package lua.value;

public class LThread extends LValue {
	public static final LString TYPE_NAME = new LString(Type.thread.toString());
	
	public LString luaGetType() {
		return TYPE_NAME;
	}

	public LString luaAsString() {
		return new LString("thread: "+hashCode());
	}
}
