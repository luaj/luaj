import java.io.InputStream;
import java.io.Reader;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.server.Launcher;
import org.luaj.vm2.server.LuajClassLoader;

/** Example of using {@link LuajClassLoader} to launch scripts that are blocked from
 * interfering with globals from other scripts including shared static metatables.
 * <P>
 * This technique is useful in a server environment to expose the full set of 
 * lua features to each script, while preventing scripts from interfering with
 * each other.
 * <P>
 * Because each Launch gets its own {@link LuajClassLoader}, it should be possible
 * to include the debug library, or let scripts manipulate shared metatables, 
 * or luajava, which otherwise present challenges in a server environment.
 * <P>
 */
public class SampleUsingClassLoader {

	/** Script that manipulates the shared string metatable.
	 * When loaded by the {@link LuajClassLoader} via a {@link Launcher} 
	 * created by that class, each instance of {@link LuajClassLoader} will
	 * have a completely separate version of all static variables.
	 */
	static String script = 
			"print('args:', ...)\n" + 
			"print('abc.foo', ('abc').foo)\n" +
			"getmetatable('abc').__index.foo = function() return 'bar' end\n" +
			"print('abc.foo', ('abc').foo)\n" +
			"print('abc:foo()', ('abc'):foo())\n" +
			"return math.pi\n";

	public static void main(String[] s) throws Exception {
		// The default launcher used standard globals.
		RunUsingDefaultLauncher();
		RunUsingDefaultLauncher();
		// Example using custom launcher class that instantiates debug globals.
		RunUsingCustomLauncherClass();
		RunUsingCustomLauncherClass();
	}

	static void RunUsingDefaultLauncher() throws Exception {
		Launcher launcher = LuajClassLoader.NewLauncher();
		// starts with pristine Globals including all luaj static variables.
		print(launcher.launch(script, new Object[] { "--------" }));
		// reuses Globals and static variables from previous step.
		print(launcher.launch(script, new Object[] {}));
	}
	
	static void RunUsingCustomLauncherClass() throws Exception {
		Launcher launcher = LuajClassLoader.NewLauncher(MyLauncher.class);
		// starts with pristine Globals including all luaj static variables.
		print(launcher.launch(script, new Object[] { "=========" }));
		// reuses Globals and static variables from previous step.
		print(launcher.launch(script, new Object[] { "" })); 
	}

	/** Example of Launcher implementation performing specialized launching. 
	 * When loaded by the {@link LuajClassLoader} all luaj classes will be loaded
	 * for each instance of the {@link Launcher} and not interfere with other 
	 * classes loaded by other instances.
	 */
	public static class MyLauncher implements Launcher {
		Globals g;
		public MyLauncher() {
			g = JsePlatform.debugGlobals();
			// ... plus any other customization of the user environment
		}
		
		public Object[] launch(String script, Object[] arg) {
			LuaValue chunk = g.load(script, "main");
			return new Object[] { chunk.call(LuaValue.valueOf(arg[0].toString())) };
		}

		public Object[] launch(InputStream script, Object[] arg) {
			LuaValue chunk = g.load(script, "main", "bt", g);
			return new Object[] { chunk.call(LuaValue.valueOf(arg[0].toString())) };
		}

		public Object[] launch(Reader script, Object[] arg) {
			LuaValue chunk = g.load(script, "main");
			return new Object[] { chunk.call(LuaValue.valueOf(arg[0].toString())) };
		}
	}

	/** Print the return values as strings. */
	private static void print(Object[] return_values) {
		for (int i =0; i<return_values.length; ++i)
			System.out.println("Return value " + return_values[i]);
	}
}
