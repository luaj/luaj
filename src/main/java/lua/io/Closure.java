package lua.io;

import lua.CallFrame;
import lua.StackState;
import lua.value.LFunction;
import lua.value.LValue;

public class Closure extends LFunction {
	public LValue env;
	public Proto p;
	public UpVal[] upVals;
	public Closure(StackState state, Proto p) {
		this.env = state._G;
		this.p = p;
		upVals = new UpVal[p.nups];
	}

	// perform a lua call
	public void luaStackCall(CallFrame call, int base, int top, int nresults) {
		call.state.stackCall( this, base, top, nresults );
	}
}
