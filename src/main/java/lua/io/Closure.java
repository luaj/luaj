package lua.io;

import lua.StackState;
import lua.value.LValue;

public class Closure extends LValue {
	public LValue env;
	public Proto p;
	public UpVal[] upVals;
	public Closure(StackState state, Proto p) {
		this.env = state.gt();
		this.p = p;
		upVals = new UpVal[p.nups];
		for ( int i=0; i<p.nups; i++ )
			upVals[i] = new UpVal();
	}


	// perform a lua call
	public int luaStackCall(StackState state, int base, int nargs) {
		state.clear( base+1+nargs, p.numparams-nargs );
		return state.vmExecute( this, base+1 );
	}
}
