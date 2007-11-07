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
package org.luaj.debug;

import java.io.IOException;
import java.io.InputStream;

import org.luaj.debug.DebugStackState;
import org.luaj.vm.LClosure;
import org.luaj.vm.LValue;
import org.luaj.vm.LoadState;
import org.luaj.vm.LPrototype;

import junit.framework.TestCase;

public class DebugStackStateTest extends TestCase {

	public void testDebugStackState() throws InterruptedException, IOException {
		String script = "/test6.luac";
		
		// set up the vm
		final DebugStackState state = new DebugStackState();
		InputStream is = getClass().getResourceAsStream( script );
		LPrototype p = LoadState.undump(state, is, script);
		
		// create closure and execute
		final LClosure c = new LClosure( state, p );

		// suspend the vm right away
		state.suspend();
		state.setBreakpoint(script, 14);
		
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
		
		// step for 5 steps
		for ( int i=0; i<5; i++ ) {
			state.stepOver();
			Thread.sleep(500);
			System.out.println("--- callgraph="+state.getCallgraph() );
			System.out.println("--- stack="+state.getStack(0) );
		}

		// resume the vm
		state.resume();
		Thread.sleep(500);
		System.out.println("--- callgraph="+state.getCallgraph() );
		state.resume();
		Thread.sleep(500);
		System.out.println("--- callgraph="+state.getCallgraph() );
	}
}
