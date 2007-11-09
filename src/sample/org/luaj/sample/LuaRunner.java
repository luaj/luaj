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

import org.luaj.lib.CoroutineLib;
import org.luaj.lib.MathLib;
import org.luaj.lib.PackageLib;
import org.luaj.lib.StringLib;
import org.luaj.lib.TableLib;
import org.luaj.lib.j2se.LuajavaLib;
import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LTable;
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
		
        // add standard bindings
		state.installStandardLibs();
				
		// load the file
		InputStream is = LuaRunner.class.getResourceAsStream( script );
		LPrototype p = LoadState.undump(state, is, script);
		
		// create closure and execute
		LClosure c = new LClosure( p, state._G );

		// do the call
		state.doCall( c, new LValue[0] );
	}
}
