package org.luaj.sample;

import java.io.IOException;
import java.io.InputStream;

import org.luaj.lib.MathLib;
import org.luaj.lib.j2se.LuajavaLib;
import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LValue;
import org.luaj.vm.LoadState;
import org.luaj.vm.LuaState;


/**
 * Program to run a lua chunk
 * 
 * @author jim_roseborough
 */
public class LuaRunner {

	public static void main( String[] args ) throws IOException {

		// new lua state 
		LuaState state = new LuaState();
		
		// get script name
		String script = (args.length>0? args[0]: "/test2.luac");
		System.out.println("loading '"+script+"'");
		
		// add LuaCompat bindings
		MathLib.install(state._G);
		LuajavaLib.install(state._G);		

		// load the file
		InputStream is = LuaRunner.class.getResourceAsStream( script );
		LPrototype p = LoadState.undump(state, is, script);
		
		// create closure and execute
		LClosure c = new LClosure( p, state._G );

		// do the call
		state.doCall( c, new LValue[0] );
	}
}
