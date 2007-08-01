package lua.io;

import lua.StackState;
import lua.VM;
import lua.value.LFunction;
import lua.value.LValue;

public class Closure extends LFunction {
	public LValue env;
	public Proto p;
	public UpVal[] upVals;
	
	// TODO: change arg type to VM? 
	public Closure(StackState state, Proto p) {
		this.env = state._G;
		this.p = p;
		upVals = new UpVal[p.nups];
	}

	// called by vm when there is an OP_CALL
	// in this case, we are on the stack, 
	// and simply need to cue the VM to treat it as a stack call
	public boolean luaStackCall(VM vm) {
		vm.prepStackCall();
		return true;
	}
}
