package lua.value;

import lua.StackState;

public class LFunction extends LValue {

	public String luaAsString() {
		return "function: "+hashCode();
	}

	public void luaSetTable(StackState state, int base, LValue table, LValue key, LValue val) {
		state.top = base;
		state.push( this );
		state.push( table );
		state.push( key );
		state.push( val );
		this.luaStackCall(state, base, state.top, 1);
	}

	public void luaGetTable(StackState state, int base, LValue table, LValue key) {
		state.top = base;
		state.push( this );
		state.push( table );
		state.push( key );
		this.luaStackCall(state, base, state.top, 1);
	}
	
}
