/*******************************************************************************
* Copyright (c) 2007 LuaJ. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.vm;



public class LClosure extends LFunction {
	public LPrototype p;
	public UpVal[] upVals;
	public LTable env;
	
	
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
	
	/** Set the environment if a thread, or closure, and return 1, otherwise return 0 */
	public int luaSetEnv(LTable t) {
		this.env = t;
		return 1;
	}
}
