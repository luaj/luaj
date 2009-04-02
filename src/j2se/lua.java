/*******************************************************************************
* Copyright (c) 2008 LuaJ. All rights reserved.
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.luaj.compiler.LuaC;
import org.luaj.lib.DebugLib;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LFunction;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;


/**
 * lua command for use in java se environments.
 */
public class lua {
	private static final String version = Lua._VERSION + "Copyright (C) 2008 luaj.org";

	private static final String usage = 
		"usage: java -cp luaj-j2se.jar lua [options] [script [args]].\n" +
		"Available options are:\n" +
		"  -e stat  execute string 'stat'\n" +
		"  -l name  require library 'name'\n" +
		"  -i       enter interactive mode after executing 'script'\n" +
		"  -v       show version information\n" +
		"  --       stop handling options\n" +
		"  -        execute stdin and stop handling options";

	private static void usageExit() {
		System.out.println(usage);
		System.exit(-1);		
	}
	
	public static void main( String[] args ) throws IOException {

		// new lua state
		Platform.setInstance(new J2sePlatform());
		LuaC.install();		
		LuaState vm = Platform.newLuaState();
		DebugLib.install(vm);
		
		// process args
		boolean interactive = (args.length == 0);
		boolean versioninfo = false;
		boolean processing = true;
		try {
			// stateful argument processing
			for ( int i=0; i<args.length; i++ ) {
				if ( ! processing || ! args[i].startsWith("-") ) {
					// input file - defer to last stage
					break;
				} else if ( args[i].length() <= 1 ) {
					// input file - defer to last stage
					break;
				} else {
					switch ( args[i].charAt(1) ) {
					case 'e':
						if ( ++i >= args.length )
							usageExit();
						// input script - defer to last stage
						break;
					case 'l':
						if ( ++i >= args.length )
							usageExit();
						loadLibrary( vm, args[i] );
						break;
					case 'i':
						interactive = true;
						break;
					case 'v':
						versioninfo = true;
						break;
					case '-':
						if ( args[i].length() > 2 )
							usageExit();
						processing = false;
						break;
					default:
						usageExit();
						break;
					}
				}
			}
			
			// echo version
			if ( versioninfo )
				System.out.println(version);
			
			// input script processing
			processing = true;
			for ( int i=0; i<args.length; i++ ) {
				if ( ! processing || ! args[i].startsWith("-") ) {
					processScript( vm, new FileInputStream(args[i]), args[i], args, i+1 );
					break;
				} else if ( args[i].length() <= 1 ) {
					processScript( vm, System.in, "-", args, i+1 );
					break;
				} else {
					switch ( args[i].charAt(1) ) {
					case 'l':
						++i;
						break;
					case 'e':
						++i;
						processScript( vm, new ByteArrayInputStream(args[i].getBytes()), args[i], null, 0 );
						break;
					case '-':
						processing = false;
						break;
					}
				}
			}
			
			if ( interactive )
				interactiveMode( vm );
			
		} catch ( IOException ioe ) {
			System.err.println( ioe.toString() );
			System.exit(-2);
		}
	}

	private static void loadLibrary( LuaState vm, String libname ) throws IOException {
		try {
			Class c = Class.forName( libname );
			Object i = c.newInstance();
			LFunction f = (LFunction) i;
			vm.call(f);
			
		} catch ( Exception e ) {
			throw new IOException("loadLibrary("+libname+") failed: "+e );
		}
	}
	
	private static void processScript( LuaState vm, InputStream script, String chunkname, String[] args, int offset ) throws IOException {
		try {
			switch ( vm.load(script, chunkname) ) {
			case 0:
				if ( args != null )
					for ( int i=offset; i<args.length; i++ )
						vm.pushstring(args[i]);
				vm.call(vm.gettop()-1, 0);
				break;
			case LuaState.LUA_ERRMEM:
				System.out.println("out of memory during chunk load");
				break;
			case LuaState.LUA_ERRSYNTAX:
				System.out.println("syntax error: "+vm.tostring(-1) );
				break;
			}
		} catch ( Throwable t ) {
			t.printStackTrace( System.err );
		} finally {
			script.close();
		}
	}
	
	private static void interactiveMode( LuaState vm ) throws IOException {
		BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			System.out.print("> ");
			System.out.flush();
			String line = reader.readLine();
			if ( line == null )
				return;
			processScript( vm, new ByteArrayInputStream(line.getBytes()), "-", null, 0 );
		}
	}
}
