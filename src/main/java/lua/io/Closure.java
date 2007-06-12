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
	public void luaStackCall(StackState state, int base) {
		if ( (! p.is_vararg) || (state.top < base+1+p.numparams) )
			state.adjustTop( base+1+p.numparams );
		state.vmExecute( this, base+1 );
	}
}
