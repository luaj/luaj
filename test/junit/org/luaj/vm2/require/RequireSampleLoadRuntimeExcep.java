package org.luaj.vm2.require;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
* This should fail while trying to load via "require()" because it throws a RuntimeException
 * 
 */
public class RequireSampleLoadRuntimeExcep extends ZeroArgFunction {
	
	public RequireSampleLoadRuntimeExcep() {		
	}
	
	public LuaValue call() {
		throw new RuntimeException("sample-load-runtime-exception");
	}	
}
