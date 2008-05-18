package org.luaj.jit;
import java.io.IOException;

import org.luaj.vm.LPrototype;
import org.luaj.vm.LuaJTest;
import org.luaj.vm.LuaState;

/**
 * Suite of standard tests, but using the LuaJit compiler 
 * for all loaded prototypes.
 */
public class LuaJitTest extends LuaJTest {
        
    protected LPrototype loadScriptResource( LuaState state, String name ) throws IOException {
    	LPrototype p = super.loadScriptResource(state, name);
    	return LuaJit.jitCompile(p);
    }
    
}
