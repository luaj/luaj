/*******************************************************************************
* Copyright (c) 2007 LuaJ. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.sample;

import java.io.IOException;
import java.io.InputStream;

import org.luaj.lib.j2se.LuajavaLib;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LValue;
import org.luaj.vm.LoadState;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;


/**
 * Program to run a compiled lua chunk for test purposes, 
 * but with the LuaJava add-ons added in
 * 
 * @author jim_roseborough
 */
public class LuajavaRunner {

	public static void main( String[] args ) throws IOException {

	        Platform.setInstance(new J2sePlatform());
	        
		// new lua state 
		LuaState state = Platform.newLuaState();
		
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
