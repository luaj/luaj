
import java.io.IOException;
import java.io.InputStream;

import lua.StackState;
import lua.io.Closure;
import lua.io.LoadState;
import lua.io.Proto;
import lua.value.LString;
import lua.value.LValue;

/**
 * Program to run a compiled lua chunk for test purposes
 * 
 * @author jim_roseborough
 */
public class LuacRunner {

	public static void main( String[] args ) throws IOException {

		// get script name
		String script = (args.length>0? args[0]: "/test1.luac");
		System.out.println("loading '"+script+"'");
		
		// new lua state 
		StackState state = new StackState();

		// convert args to lua
		LValue[] vargs = new LValue[args.length];
		for ( int i=1; i<args.length; i++ ) 
			vargs[i] = new LString(args[i]);
		
		// load the file
		InputStream is = LuacRunner.class.getResourceAsStream( script );
		Proto p = LoadState.undump(state, is, script);
		
		// create closure and execute
		Closure c = new Closure( state, p );
		state.doCall(c, vargs, 0);
	}
}
