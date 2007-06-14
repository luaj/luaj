package lua.io;

import lua.StackState;
import lua.value.LInteger;
import lua.value.LNil;
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
			upVals[i] = new UpVal( p.upvalues[i] );
	}


	// perform a lua call
	public void luaStackCall(StackState state, int base) {
		// skip over closure
		base++;
		if ( p.is_vararg ) {
			
			// adjust stack to bury varargs under base
			int top = state.top;
			int nsupplied = top-base;
			int nrequired = p.numparams;
			int nvarargs = Math.max( 0, nsupplied - nrequired );
			for ( int i=0; i<nrequired; i++ )
				state.stack[top+i+1] = (i<nsupplied? state.stack[base+i]: LNil.NIL);
			state.stack[top] = new LInteger( nvarargs );
			state.top = top + nrequired + 1;
			base = top+1;
			
		} else {
			
			// normal non-varargs call
			state.adjustTop( base+p.numparams );
		}
		state.vmExecute( this, base );
	}
}
