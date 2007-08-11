package lua.debug;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import lua.io.Closure;
import lua.io.LoadState;
import lua.io.Proto;
import lua.value.LValue;

public class DebugStackStateTest extends TestCase {

	public void testDebugStackState() throws InterruptedException, IOException {
		String script = "/test6.luac";
		
		// set up the vm
		final DebugStackState state = new DebugStackState();
		InputStream is = getClass().getResourceAsStream( script );
		Proto p = LoadState.undump(state, is, script);
		
		// create closure and execute
		final Closure c = new Closure( state, p );

		// suspend the vm right away
		state.suspend();
		
		// start the call processing in its own thread
		new Thread() {
			public void run() {
				try {
					state.doCall( c, new LValue[0] );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
		}.start();
		
		// step for 25 steps
		for ( int i=0; i<10; i++ ) {
			state.step();
			Thread.sleep(1000);
			System.out.println("--- callgraph="+state.callgraph() );
			System.out.println("--- stack="+state.stack() );
			System.out.println("--- variable(1,0)="+state.variable(1,0) );
		}

		// resume the vm
		state.resume();		
	}
}
