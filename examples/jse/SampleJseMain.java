

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class SampleJseMain {
	
	
	public static void main(String[] args) throws Exception {
		String script = "examples/lua/hello.lua";
		
		// create an environment to run in
		Globals _G = JsePlatform.standardGlobals();
		_G.loadFile(script).arg1().call( LuaValue.valueOf(script) );
	}


}
