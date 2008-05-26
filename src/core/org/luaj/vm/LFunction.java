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


abstract
public class LFunction extends LValue {

	public String toJavaString() {
		return "function: "+hashCode();
	}
	
	public boolean isFunction() {
		return true;
	}
	
	public int luaGetType() {
		return Lua.LUA_TFUNCTION;
	}

	/**
	 * Set up a Java invocation, and leave the results on the stack 
	 * starting at base.  The default implementation for LFunction
	 * delegates to the VM which provides convenience. 
	 */
	public boolean luaStackCall(LuaState vm) {
		vm.invokeJavaFunction( this );
		return false;
	}
	
	/**
	 * Called to invoke a JavaFunction. 
	 *
	 * The implementation should manipulate the stack 
	 * via the VM Java API in the same way that lua_CFunctions 
	 * do so in standard lua.  
	 * 
	 * Arguments to the function will be in position 1-n.
	 * Return values can be pushed onto the stack, and will be 
	 * copied down to the appropriate location by the calling LuaState. 
	 * 
	 * 
	 * @param lua the LuaState calling this function.
	 * @return number of results pushed onto the stack.
	 */ 
	public int invoke( LuaState lua ) {
		return 0;
	}

	/** 
	 * Process lua tag method __index when it points to a function.
	 * Default method calls the function using the vm.
	 * 
	 * @param vm
	 * @param table
	 * @param key
	 * @return
	 */
	public LValue __index(LuaState vm, LValue table, LValue key) {
		return vm.call(this, table, key);
	}

	/**
	 * Process lua tag method __newindex when it points to a function 
	 * Default method calls the function using the vm.
	 * 
	 * @param vm
	 * @param table
	 * @param key
	 * @param val
	 */
	public void __newindex(LuaState vm, LValue table, LValue key, LValue val) {
		vm.call(this, table, key, val);
	}
	
}
