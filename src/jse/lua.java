/*******************************************************************************
* Copyright (c) 2009-2012 Luaj.org. All rights reserved.
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

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Print;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;


/**
 * lua command for use in JSE environments.
 */
public class lua {
	private static final String version = Lua._VERSION + " Copyright (c) 2012 Luaj.org.org";

	private static final String usage = 
		"usage: java -cp luaj-jse.jar lua [options] [script [args]].\n" +
		"Available options are:\n" +
		"  -e stat  execute string 'stat'\n" +
		"  -l name  require library 'name'\n" +
		"  -i       enter interactive mode after executing 'script'\n" +
		"  -v       show version information\n" +
		"  -b      	use luajc bytecode-to-bytecode compiler (requires bcel on class path)\n" +
		"  -n      	nodebug - do not load debug library by default\n" +
		"  -p      	print the prototype\n" +
		"  -c enc  	use the supplied encoding 'enc' for input files\n" +
		"  --       stop handling options\n" +
		"  -        execute stdin and stop handling options";

	private static void usageExit() {
		System.out.println(usage);
		System.exit(-1);		
	}

	private static Globals _G;
	private static boolean print = false;
	private static String encoding = null;
	
	public static void main( String[] args ) throws IOException {

		// process args
		boolean interactive = (args.length == 0);
		boolean versioninfo = false;
		boolean processing = true;
		boolean nodebug = false;
		boolean luajc = false;
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
					case 'p':
						print = true;
						break;
					case 'c':
						if ( ++i >= args.length )
							usageExit();
						encoding = args[i];
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
			if ( luajc ) LuaJC.install(_G);
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
					case 'c':
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
				v.call(slibname, _G);
			} catch ( Exception f ) {
				throw new IOException("loadLibrary("+libname+") failed: "+e+","+f );
			}
		}
	}
	
	private static void processScript( InputStream script, String chunkname, String[] args, int firstarg ) throws IOException {
		try {
			LuaValue c;
			try {
				c = encoding != null? 
						_G.load(new InputStreamReader(script, encoding), chunkname):
						_G.load(script, chunkname, "bt", _G);
			} finally {
				script.close();
			}
			if (print && c.isclosure())
				Print.print(c.checkclosure().p);
			Varargs scriptargs = setGlobalArg(chunkname, args, firstarg, _G);
			c.invoke( scriptargs );
		} catch ( Exception e ) {
			e.printStackTrace( System.err );
		}
	}

	private static Varargs setGlobalArg(String chunkname, String[] args, int i, LuaValue _G) {
		if (args == null)
			return LuaValue.NONE;
		LuaTable arg = LuaValue.tableOf();
		for ( int j=0; j<args.length; j++ )
			arg.set( j-i, LuaValue.valueOf(args[j]) );
		arg.set(0, LuaValue.valueOf(chunkname));
		arg.set(-1, LuaValue.valueOf("luaj"));
		_G.set("arg", arg);
		return arg.unpack();
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
