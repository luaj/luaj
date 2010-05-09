package org.luaj.vm2.require;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * This should succeed as a library that can be loaded dynamically via "require()"
 */
public class RequireSampleSuccess extends ZeroArgFunction {
	
	public RequireSampleSuccess() {		
	}
	
	public LuaValue call() {
		return LuaValue.valueOf("require-sample-success");
	}	
}
