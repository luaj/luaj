
import java.io.IOException;
import java.io.InputStream;

import lua.StackState;
import lua.VM;
import lua.addon.luacompat.LuaCompat;
import lua.io.Closure;
import lua.io.LoadState;
import lua.io.Proto;
import lua.value.LValue;

/**
 * Program to run a compiled lua chunk for test purposes
 * 
 * @author jim_roseborough
 */
public class LuacRunner {

	public static void main( String[] args ) throws IOException {

		// get script name
		String script = (args.length>0? args[0]: "/test2.luac");
		System.out.println("loading '"+script+"'");
		
		// add LuaCompat bindings
		LuaCompat.install();
		
		// new lua state 
		StackState state = new StackState();
		VM vm = state;

		// load the file
		InputStream is = LuacRunner.class.getResourceAsStream( script );
		Proto p = LoadState.undump(state, is, script);
		
		// create closure and execute
		Closure c = new Closure( state, p );

		// do the call
		vm.doCall( c, new LValue[0] );
	}
}
