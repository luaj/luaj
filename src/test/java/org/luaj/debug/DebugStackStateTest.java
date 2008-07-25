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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.luaj.TestPlatform;
import org.luaj.compiler.LuaC;
import org.luaj.vm.DebugNetSupport;
import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LValue;
import org.luaj.vm.LoadState;
import org.luaj.vm.Platform;

public class DebugStackStateTest extends TestCase {

	protected void setUp() throws Exception {
		System.setProperty(Platform.PROPERTY_LUAJ_DEBUG, "true");
		Platform.setInstance(new TestPlatform() {
		    public DebugNetSupport getDebugSupport() throws IOException {
		        return null;
		    }
		});
		LuaC.install();
	}

	
	public void testStackStateUpvalues() throws Exception {
		tryScript(
				"print( 'aaa' ); local a = 3;\n"+
				"print( 'bbb' ); local f = function()\n"+
				"	print('in f'); local b = 4; a=33\n"+
				"	print( 'bbb' ); local g = function()\n"+
				"		print('in g'); local c = 6; b=444; a=333\n" +
				"		return c, b, a\n"+
				"	end; print( 'calling g' ); g(); return b, a\n"+
				"end\n"+
				"print('calling f'); f()\n"+
				"print( 'returned from f' ); local d = 6\n"+
				"return 123, a, b, c, d\n" );
	}
	
	public void testStackStateLocals() throws Exception {
		tryScript(
				"print('hello, world')\n"+
				"print('aaa'); local a = 3; print('aaa')\n"+
				"print('bbb'); local b = 4; print('bbb')\n"+
				"print('ccc'); local c = 5; print('ccc')\n"+
				"print('ddd'); local d = 6; print('ddd')\n"+
				"print( a,b,c,d )\n"+
				"return 123\n" );
	}
	
	private void tryScript(String script) throws Exception {

		// set up closure
		final DebugLuaState vm = (DebugLuaState) Platform.newLuaState();
		final InputStream is = new ByteArrayInputStream(script.getBytes());
		final LPrototype p = LoadState.undump(vm, is, "script");
		final LClosure c = p.newClosure( vm._G );

		// suspend the vm right away
		vm.suspend();
		
		// start the call processing in its own thread
		Thread t = new Thread() {
			public void run() {
				try {
					vm.doCall( c, new LValue[0] );
				} catch ( Throwable e ) {
					System.out.println(e.toString());
				}
			}
		};
		t.start();		

		for ( int i=0; i<20; i++ ) {
			vm.stepInto();
			printStackState(vm);
			Thread.sleep(100);
		}
		vm.stop();
	}
	
	
	public void testDebugStackState() throws InterruptedException, IOException {
		String script = "src/test/res/test6.lua";

		final DebugLuaState state = (DebugLuaState) Platform.newLuaState();
		InputStream is = new FileInputStream( script );
		LPrototype p = LoadState.undump(state, is, script);
		
		// create closure and execute
		final LClosure c = p.newClosure( state._G );

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
	

	
	private void printStackState(DebugLuaState vm) {
		int n = vm.getCallgraph().length;
		System.out.println("stacks: "+n);		
		for ( int j=0; j<n; j++ ) {
			try {
				Variable[] v = vm.getStack(j);
				for ( int i=0; i<v.length; i++ )
					System.out.println("v["+j+","+i+"]= index="+v[i].index+" "+v[i].name+"="+v[i].value+" type="+v[i].type);
			} catch ( Throwable t ) {
				System.out.println(t.toString());
				return;
			}
		}
	}


}
