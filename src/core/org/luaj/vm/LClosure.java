package org.luaj.vm;



public class LClosure extends LFunction {
	public LTable env;
	public LPrototype p;
	public UpVal[] upVals;
	
	/**
	 * @deprecated construct with environment instead 
	 * @param state
	 * @param p
	 */ 
	public LClosure(LuaState state, LPrototype p) {
		this.env = state._G;
		this.p = p;
		upVals = new UpVal[p.nups];
	}

	/**
	 * Construct using a prototype and initial environment. 
	 * @param p
	 * @param env
	 */
	public LClosure(LPrototype p, LTable env) {
		this.p = p;
		this.env = env;
		upVals = new UpVal[p.nups];
	}

	// called by vm when there is an OP_CALL
	// in this case, we are on the stack, 
	// and simply need to cue the VM to treat it as a stack call
	public boolean luaStackCall(LuaState vm) {
		vm.prepStackCall();
		return true;
	}
}
