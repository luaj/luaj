package org.luaj.jit;

import java.io.IOException;

import org.luaj.jit.LuaJit;
import org.luaj.vm.LPrototype;
import org.luaj.vm.CompatibiltyTest;
import org.luaj.vm.LuaState;

/**
 * Suite of standard tests, but using the LuaJit compiler 
 * for all loaded prototypes.
 */
public class LuaJitTest extends CompatibiltyTest {
        
    protected LPrototype loadScript( LuaState state, String name ) throws IOException {
    	LPrototype p = super.loadScript(state, name);
    	return LuaJit.jitCompile(p);
    }
    
}
