package lua.value;

import lua.CallFrame;


public class LFunction extends LValue {

	public static final LString TYPE_NAME = new LString("function");
	
	public String luaAsString() {
		return "function: "+hashCode();
	}

	public void luaSetTable(CallFrame call, int base, LValue table, LValue key, LValue val) {
		call.top = base;
		call.push( this );
		call.push( table );
		call.push( key );
		call.push( val );
		this.luaStackCall(call, base, call.top, 1);
	}

	public void luaGetTable(CallFrame call, int base, LValue table, LValue key) {
		call.top = base;
		call.push( this );
		call.push( table );
		call.push( key );
		this.luaStackCall(call, base, call.top, 1);
	}
	
	public LString luaGetType() {
		return TYPE_NAME;
	}
	
}
