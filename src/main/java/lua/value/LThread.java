package lua.value;

public class LThread extends LValue {
	public static final LString TYPE_NAME = new LString("thread");
	
	public LString luaGetType() {
		return TYPE_NAME;
	}

}
