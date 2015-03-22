import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

/** Simple program that illustrates basic sand-boxing of client scripts
 * in a server environment.
 * 
 * <p>Although this sandboxing is done primarily in Java here, most of the
 * same techniques can be done directly from lua using metatables.
 * 
 * <p> This class makes particular use of two utility classes, 
 * {@link ReadOnlyTable} which is used to wrap shared global metatables 
 * such as the string metatable or the number metatable, and 
 * {@link ReadWriteShadowTable} which can provide a lightweight user
 * environment around an arbitrarily deep shared globals instance while 
 * limiting the resources at startup for small scripts that use few globals.
 * 
 * @see Globals
 * @see LuaValue
 * @see ReadOnlyTable
 * @see ReadWriteShadowTable
 */
public class SampleSandboxed {

	// Globals use by the server itself, say to compile scripts that are loaded.
	// In a real server there should be one of these per server thread.
	// See SampleMultiThreaded.java for an example of multi-threaded setup.
	static final Globals server_globals = JsePlatform.debugGlobals();

	// A set of global functions and packages that are shared across users.
	// These are exposed in a read-only fashion through user environment
	// shadow tables.
	static final Globals shared_globals = new Globals();
	static {
		// Load only packages known to be safe for multiple users.
		shared_globals.load(new JseBaseLib());
		shared_globals.load(new PackageLib());
		shared_globals.load(new Bit32Lib());
		shared_globals.load(new TableLib());
		shared_globals.load(new StringLib());
		shared_globals.load(new JseMathLib());
		LoadState.install(shared_globals);
		LuaC.install(shared_globals);
	}
	
	// Create a new user environment which refers to, but does not modify the
	// shared globals.  Each top-level user script should get their own copy
	// of these which are a lightweight shadow of the shared globals.
	// Writes to these shadow tables do not affect the original table.
	static LuaTable create_user_environment() {
		LuaTable user_environment = new ReadWriteShadowTable(shared_globals);
		user_environment.set("_G", user_environment);
		return user_environment;
	}
	
	public static void main(String[] args) {

		// Should be able to see and use globals as if they are owned by this environment.
		expectSuccess("print('_G', _G)");
		expectSuccess("print('math.pi', math.pi)");;
		expectSuccess("x = 'abc'; print('_G.x', _G.x); assert(_G.x == 'abc')");
		expectSuccess("print('_G.x', _G.x); assert(x == nil)");

		// Should not be able to write to global shared metatables.
		expectSuccess("print('string meta', getmetatable('abc'))");
		expectException("print('string meta.x=foo'); getmetatable('abc')['x']='foo'");
		expectException("print('setmetatable(abc)', setmetatable('abc', {}))");
		expectException("print('setmetatable(true)', setmetatable(true, {}))");
		
		// Should be able to provide useful global server metatable behavior
		// Example use of shared global metatable. 
		// Allows bools to be added to numbers.  
		LuaBoolean.s_metatable = new ReadOnlyTable(new LuaValue[] {
				LuaValue.ADD, new TwoArgFunction() {
					public LuaValue call(LuaValue x, LuaValue y) {
						return LuaValue.valueOf(
								(x == TRUE ? 1.0 : x.todouble()) + 
								(y == TRUE ? 1.0 : y.todouble()) );
					}				
				},
		});
		expectSuccess("print('pi + true', math.pi + true)");		

		// Should be able to use the metatable for our own globals.
		expectSuccess("setmetatable(_G, {__index={foo='bar'}}); print('foo', foo); assert(foo == 'bar')");

		// Subtables of globals can be modified but are shadow tables and don't affect
		// shared globals or environment of other scripts.
		expectSuccess("print('setmetatable(math)', setmetatable(math, {foo='bar'}))");
		expectSuccess("print('getmetatable(math)', getmetatable(math)); assert(getmetatable(math) == nil)");
	}


	// Run a script and return the results.
	static Varargs runScript(String script) {
		LuaTable user_environment = create_user_environment();
		LuaValue chunk = server_globals.load(script, "main", user_environment);
		return chunk.invoke();
	}

	// Run a script, expecting it to succeed, to illustrate various uses
	// that should succeed without affecting shared resources.
	static Varargs expectSuccess(String script) {
		try {
			return runScript(script);
		} catch (Throwable t) {
			System.out.println("script failed: "+t);
			return LuaValue.NONE;
		}
	}
	
	// Run a script, expecting it to fail, to illustrate various expected ways
	// rogue attempts will fail to abuse resources.
	static Varargs expectException(String script) {
		try {
			Varargs result = runScript(script);
			System.out.println("failure: script returned "+ result);
			return result;
		} catch (Throwable t) {
			System.out.println("success: "+t.getMessage());
			return LuaValue.NONE;
		}
	}
}
