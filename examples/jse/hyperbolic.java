import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * Sample library that can be called via luaj's require() implementation.
 * 
 * This library, when loaded, creates a lua package called "hyperbolic" 
 * which has two functions, "sinh()" and "cosh()".
 *
 * Because the class is in the default Java package, it can be called using 
 * lua code such as:
 * 
 <pre> {@code 
 * require 'hyperbolic'
 * print('sinh',  hyperbolic.sinh)
 * print('sinh(1.0)',  hyperbolic.sinh(1.0))
 * }</pre>
 *
 * When require() loads the code, two things happen: 1) the public constructor
 * is called to construct a library instance, and 2) the instance is invoked
 * as a java call with no arguments.  This invocation should be used to initialize
 * the library, and add any values to globals that are desired.
 */
public class hyperbolic extends TwoArgFunction {

	/** Public constructor.  To be loaded via require(), the library class 
	 * must have a public constructor.
	 */
	public hyperbolic() {}

	/** The implementation of the TwoArgFunction interface.
	 * This will be called once when the library is loaded via require().
	 * @param modname LuaString containing the name used in the call to require().
	 * @param env LuaValue containing the environment for this function.
	 * @return Value that will be returned in the require() call.  In this case, 
	 * it is the library itself.
	 */
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaValue library = tableOf();
		library.set( "sinh", new sinh() );
		library.set( "cosh", new cosh() );
		env.set( "hyperbolic", library );
		return library;
	}

	/* Each library function is coded as a specific LibFunction based on the 
	* arguments it expects and returns.  By using OneArgFunction, rather than 
	* LibFunction directly, the number of arguments supplied will be coerced
	* to match what the implementation expects.  */
	
	/** Mathematical sinh function provided as a OneArgFunction.  */
	static class sinh extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			return LuaValue.valueOf(Math.sinh(x.checkdouble()));
		}
	}
	
	/** Mathematical cosh function provided as a OneArgFunction.  */
	static class cosh extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			return LuaValue.valueOf(Math.cosh(x.checkdouble()));
		}
	}
}