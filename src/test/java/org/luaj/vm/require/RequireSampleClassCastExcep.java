package org.luaj.vm.require;

import org.luaj.vm.LuaState;

/**
 * This should fail while trying to load via "require() because it is not an LFunction"
 * 
 */
public class RequireSampleClassCastExcep {
	
	public RequireSampleClassCastExcep() {		
	}
	
	public boolean luaStackCall( LuaState vm ) {
		System.out.println("called "+this.getClass().getName()+" with vm.topointer(1)=="+vm.topointer(1) );
		vm.resettop();
		return false;
	}	
}
