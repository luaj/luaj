
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

/** Simple program showing the minimal Java program to launch a script. */
public class SampleJseMain {
	
	public static void main(String[] args) throws Exception {
		String script = "examples/lua/hello.lua";
		
		// create an environment to run in
		Globals globals = JsePlatform.standardGlobals();
		
		// Use the convenience function on the globals to load a chunk.
		LuaValue chunk = globals.loadFile(script);
		
		// Use any of the "call()" or "invoke()" functions directly on the chunk.
		chunk.call( LuaValue.valueOf(script) );
	}


}
