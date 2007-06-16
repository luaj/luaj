
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import lua.StackState;
import lua.io.Closure;
import lua.io.LoadState;
import lua.io.Proto;
import lua.value.LString;

/**
 * Program to run a compiled lua chunk for test purposes
 * 
 * @author jim_roseborough
 */
public class LuacRunner {

	public static void main( String[] args ) throws IOException {

		// get script name
		String script = (args.length>0? args[0]: "src/test/res/test1.luac");
		System.out.println("loading '"+script+"'");
		
		// new lua state 
		StackState state = new StackState();

		// push args onto stack
		for ( int i=1; i<args.length; i++ ) 
			state.push(new LString(args[i]));
		
		// load the file
		InputStream is = new FileInputStream( script );
		Proto p = LoadState.undump(state, is, script);
		
		// create closure to execute
		Closure c = new Closure( state, p );
		state.push( c );
		for ( int i=0; i<args.length; i++ )
			state.push( new LString(args[i]) );
		state.docall(args.length, 0);

		// print result? 
		System.out.println("stack:");
		for ( int i=0; i<state.top; i++ ) 
			System.out.println(" ["+i+"]="+state.stack[i] );
		
	}
}
