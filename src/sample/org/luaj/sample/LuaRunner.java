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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.luaj.compiler.LuaC;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LValue;
import org.luaj.vm.LoadState;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;


/**
 * Program to run a lua chunk
 * 
 * @author jim_roseborough
 */
public class LuaRunner {

	public static void main( String[] args ) throws IOException {

		// new lua state 
	        Platform.setInstance(new J2sePlatform());
		LuaState state = Platform.newLuaState();
		LuaC.install();
		
		// get script name
		for ( int i=0; i<args.length; i++ ) {
			String script = args[i];
			try {
				System.out.println("loading '"+script+"'");
										
				// load the file
				InputStream is = null;
				File f = new File(script);
				if ( f.exists() )
					is = new FileInputStream( f );
				else
					is = LuaRunner.class.getResourceAsStream( script );
				if ( is == null )			
					throw new java.io.FileNotFoundException( "not found: "+script );
				LPrototype p = LoadState.undump(state, is, script);
				
				// create closure and execute
				LClosure c = p.newClosure( state._G );
		
				// do the call
				state.doCall( c, new LValue[0] );
			} catch ( LuaErrorException lee ) {
				System.err.println(script+" lua error, "+lee.getMessage() );
			} catch ( Throwable t ) {
				System.err.println(script+" threw "+t);
				t.printStackTrace();
			} finally {
				System.out.flush();
				System.err.flush();
			}
		}
	}
}
