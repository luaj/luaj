package org.luaj.vm;


abstract
public class LFunction extends LValue {

	public String toJavaString() {
		return "function: "+hashCode();
	}
	
	public void luaSetTable(LuaState vm, LValue table, LValue key, LValue val) {
		vm.pushlvalue( this );
		vm.pushlvalue( table );
		vm.pushlvalue( key );
		vm.pushlvalue( val );
		vm.call( 3, 0 );
	}

	public void luaGetTable(LuaState vm, LValue table, LValue key) {
		vm.pushlvalue( this );
		vm.pushlvalue( table );
		vm.pushlvalue( key );
		vm.call( 2, 1 );
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
	
}
