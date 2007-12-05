package org.luaj.vm.require;

import org.luaj.vm.LFunction;
import org.luaj.vm.LuaState;

/**
 * This should fail while trying to load via "require()" because it throws a LuaError
 * 
 */
public class RequireSampleLoadLuaError extends LFunction {
	
	public RequireSampleLoadLuaError() {		
	}
	
	public boolean luaStackCall( LuaState vm ) {
		System.out.println("called "+this.getClass().getName()+" with vm.topointer(1)=="+vm.topointer(1) );
		vm.error("lua error thrown by "+this.getClass().getName());
		return false;
	}	
}
