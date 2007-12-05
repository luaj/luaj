package org.luaj.vm.require;

import org.luaj.vm.LFunction;
import org.luaj.vm.LuaState;

/**
 * This should succeed as a library that can be loaded dynmaically via "require()"
 * 
 */
public class RequireSampleSuccess extends LFunction {
	
	public RequireSampleSuccess() {		
	}
	
	public boolean luaStackCall( LuaState vm ) {
		System.out.println("called "+this.getClass().getName()+" with vm.topointer(1)=="+vm.topointer(1) );
		vm.resettop();
		return false;
	}	
}
