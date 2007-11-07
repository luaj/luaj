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
 * Program to run a compiled lua chunk for test purposes, 
 * but with the LuaJava add-ons added in
 * 
 * @author jim_roseborough
 */
public class LuajavaRunner {

	public static void main( String[] args ) throws IOException {

		// new lua state 
		LuaState state = new LuaState();

		// add LuaCompat bindings
		MathLib.install(state._G);
		
		// add LuaJava bindings
		LuajavaLib.install(state._G);
		
		// get script name
		String script = (args.length>0? args[0]: "/swingapp.luac");
		System.out.println("loading '"+script+"'");
		
		// load the file
		InputStream is = LuajavaRunner.class.getResourceAsStream( script );
		LPrototype p = LoadState.undump(state, is, script);
		
		// create closure and execute
		LClosure c = new LClosure( p, state._G );
		state.doCall(c, new LValue[0]);
		
	}
}
