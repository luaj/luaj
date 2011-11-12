/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
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
import java.util.Vector;

import org.luaj.vm2.LoadState;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.lua2java.Lua2Java;
import org.luaj.vm2.luajc.LuaJC;


/**
 * lua command for use in java se environments.
 */
public class lua {
	private static final String version = Lua._VERSION + "Copyright (c) 2009 Luaj.org.org";

	private static final String usage = 
		"usage: java -cp luaj-jse.jar lua [options] [script [args]].\n" +
		"Available options are:\n" +
		"  -e stat  execute string 'stat'\n" +
		"  -l name  require library 'name'\n" +
		"  -i       enter interactive mode after executing 'script'\n" +
		"  -v       show version information\n" +
		"  -j      	use lua2java source-to-source compiler\n" +
		"  -b      	use luajc bytecode-to-bytecode compiler (requires bcel on class path)\n" +
		"  -n      	nodebug - do not load debug library by default\n" +
		"  --       stop handling options\n" +
		"  -        execute stdin and stop handling options";

	private static void usageExit() {
		System.out.println(usage);
		System.exit(-1);		
	}

	private static LuaValue _G;
	
	public static void main( String[] args ) throws IOException {

		// process args
		boolean interactive = (args.length == 0);
		boolean versioninfo = false;
		boolean processing = true;
		boolean nodebug = false;
		boolean luajc = false;
		boolean lua2java = false;
		Vector libs = null;
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
					case 'b':
						luajc = true;
						break;
					case 'j':
						lua2java = true;
						break;
					case 'l':
						if ( ++i >= args.length )
							usageExit();
						libs = libs!=null? libs: new Vector();
						libs.addElement( args[i] );
						break;
					case 'i':
						interactive = true;
						break;
					case 'v':
						versioninfo = true;
						break;
					case 'n':
						nodebug = true;
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
			
			// new lua state
			_G = nodebug? JsePlatform.standardGlobals(): JsePlatform.debugGlobals();
			if ( luajc ) LuaJC.install();
			if ( lua2java) Lua2Java.install();
			for ( int i=0, n=libs!=null? libs.size(): 0; i<n; i++ )
				loadLibrary( (String) libs.elementAt(i) );
			
			// input script processing
			processing = true;
			for ( int i=0; i<args.length; i++ ) {
				if ( ! processing || ! args[i].startsWith("-") ) {
					processScript( new FileInputStream(args[i]), args[i], args, i );
					break;
				} else if ( "-".equals( args[i] ) ) {
					processScript( System.in, "=stdin", args, i );
					break;
				} else {
					switch ( args[i].charAt(1) ) {
					case 'l':
						++i;
						break;
					case 'e':
						++i;
						processScript( new ByteArrayInputStream(args[i].getBytes()), "string", args, i );
						break;
					case '-':
						processing = false;
						break;
					}
				}
			}
			
			if ( interactive )
				interactiveMode();
			
		} catch ( IOException ioe ) {
			System.err.println( ioe.toString() );
			System.exit(-2);
		}
	}

	private static void loadLibrary( String libname ) throws IOException {
		LuaValue slibname =LuaValue.valueOf(libname); 
		try {
			// load via plain require
			_G.get("require").call(slibname);
		} catch ( Exception e ) {
			try {
				// load as java class
				LuaValue v = (LuaValue) Class.forName(libname).newInstance(); 
				v.setfenv(_G);
				v.call(slibname, _G);
			} catch ( Exception f ) {
				throw new IOException("loadLibrary("+libname+") failed: "+e+","+f );
			}
		}
	}
	
	private static void processScript( InputStream script, String chunkname, String[] args, int firstarg ) throws IOException {
		try {
			LuaFunction c;
			try {
				c = LoadState.load(script, chunkname, _G);
			} finally {
				script.close();
			}
			Varargs scriptargs = (args!=null? setGlobalArg(args, firstarg): LuaValue.NONE);
			c.invoke( scriptargs );
		} catch ( Throwable t ) {
			t.printStackTrace( System.err );
		}
	}

	private static Varargs setGlobalArg(String[] args, int i) {
		LuaTable arg = LuaValue.tableOf();
		for ( int j=0; j<args.length; j++ )
			arg.set( j-i, LuaValue.valueOf(args[j]) );
		_G.set( "arg", arg );
		return _G.get("unpack").invoke(arg);
	}

	private static void interactiveMode( ) throws IOException {
		BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			System.out.print("> ");
			System.out.flush();
			String line = reader.readLine();
			if ( line == null )
				return;
			processScript( new ByteArrayInputStream(line.getBytes()), "=stdin", null, 0 );
		}
	}
}
