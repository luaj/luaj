package lua;

import lua.value.LFunction;


/**
Type for Java&nbsp;functions.


<p>
In order to communicate properly with Lua,
a Java&nbsp;function must use the following protocol,
which defines the way parameters and results are passed:
a Java&nbsp;function receives its arguments from Lua in its stack
in direct order (the first argument is pushed first).
So, when the function starts,
<code>lua_gettop(L)</code> returns the number of arguments received by the function.
The first argument (if any) is at index 1
and its last argument is at index <code>lua_gettop(L)</code>.
To return values to Lua, a Java&nbsp;function just pushes them onto the stack,
in direct order (the first result is pushed first),
and returns the number of results.
Any other value in the stack below the results will be properly
discarded by Lua.
Like a Lua function, a Java&nbsp;function called by Lua can also return
many results.


<p>
As an example, the following function receives a variable number
of numerical arguments and returns their average and sum:

<pre><code>
     int foo (VM lua) {
		int n = lua.gettop();    // number of arguments 
		double sum = 0;
		int i;
		for (i = 1; i &lt;= n; i++) {
			if (!lua.isnumber(L, i)) {
				lua.pushstring(L, "incorrect argument");
				lua.error(L);
			}
			sum += lua.tonumber(L, i);
		}
		lua.pushnumber(L, sum/n);   // first result 
		lua.pushnumber(L, sum);     // second result 
		return 2;                   // number of results 
	}
</code></pre>
 */

abstract public class JavaFunction extends LFunction {
	
	/**
	 * Called to invoke a JavaFunction. 
	 *
	 * The implementation should manipulate the stack 
	 * via the VM Java API in the same way that lua_CFunctions 
	 * do so in standard lua.  
	 * 
	 * @param lua the LuaState calling this function.
	 * @return number of results pushed onto the stack.
	 */ 
	abstract public int invoke( VM lua );
	
	/**
	 * Set up a Java invocation, and fix up the results
	 * when it returns. 
	 */
	public boolean luaStackCall(VM vm) {
		vm.invokeJavaFunction( this );
		return true;
	}
}
