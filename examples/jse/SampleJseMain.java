

import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.compiler.LuaC;

public class SampleJseMain {
	
	
	public static void main(String[] args) throws Exception {
		String script = "examples/lua/hello.lua";
		
		// create an environment to run in
 		LuaC.install();
		LuaValue _G = JsePlatform.standardGlobals();
		_G.get("dofile").call( LuaValue.valueOf(script) );
	}


}
