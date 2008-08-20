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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.luaj.compiler.DumpState;
import org.luaj.compiler.LuaC;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LPrototype;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;
import org.luaj.vm.Print;


/**
 * luac command for use in java se environments.
 */
public class luac {
	private static final String version = Lua._VERSION + "Copyright (C) 2008 luaj.org";

	private static final String usage = 
		"usage: java -cp luaj-j2se.jar luac [options] [filenames].\n" +
		"Available options are:\n" +
		"  -        process stdin\n" +
		"  -l       list\n" +
		"  -o name  output to file 'name' (default is \"luac.out\")\n" +
		"  -p       parse only\n" +
		"  -s       strip debug information\n" +
		"  -e       little endian format for numbers\n" +
		"  -i       int32 format for all numbers\n" +
		"  -v       show version information\n" +
		"  --       stop handling options\n";
	
	private static void usageExit() {
		System.out.println(usage);
		System.exit(-1);		
	}

	private boolean list = false;
	private String output = "luac.out";
	private boolean parseonly = false;
	private boolean stripdebug = false;
	private boolean littleendian = false;
	private boolean intsonly = false;
	private boolean versioninfo = false;
	private boolean processing = true;

	public static void main( String[] args ) throws IOException {
		new luac( args );
	}

	private luac( String[] args ) throws IOException {
		// new lua state
		Platform.setInstance(new J2sePlatform());
		LuaC.install();		
		LuaState vm = Platform.newLuaState();
		
		// process args
		try {
			// get stateful args
			for ( int i=0; i<args.length; i++ ) {
				if ( ! processing || ! args[i].startsWith("-") ) {
					// input file - defer to next stage
				} else if ( args[i].length() <= 1 ) {
					// input file - defer to next stage
				} else {
					switch ( args[i].charAt(1) ) {
					case 'l':
						list = true;
						break;
					case 'o':
						if ( ++i >= args.length )
							usageExit();
						output = args[i];
						break;
					case 'p':
						parseonly = true;
						break;
					case 's':
						stripdebug = true;
						break;
					case 'e':
						littleendian = true;
						break;
					case 'i':
						intsonly = true;
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

			// open output file
			OutputStream fos = new FileOutputStream( output );
			
			// process input files
			try {
				processing = true;
				for ( int i=0; i<args.length; i++ ) {
					if ( ! processing || ! args[i].startsWith("-") ) {
						processScript( vm, new FileInputStream(args[i]), args[i], fos );
					} else if ( args[i].length() <= 1 ) {
						processScript( vm, System.in, "-", fos );
					} else {
						switch ( args[i].charAt(1) ) {
						case 'o':
							++i;
							break;
						case '-':
							processing = false;
							break;
						}
					}
				}
			} finally {
				fos.close();
			}
			
		} catch ( IOException ioe ) {
			System.err.println( ioe.toString() );
			System.exit(-2);
		}
	}
	
	private void processScript( LuaState vm, InputStream script, String chunkname, OutputStream out ) throws IOException {
		try {
	        // create the chunk
	        LPrototype chunk = org.luaj.compiler.LuaC.compile(script, chunkname);

	        // list the chunk
	        if (list)
	            Print.printCode(chunk);

	        // write out the chunk
	        if (!parseonly) {
	            DumpState.dump(chunk, out, stripdebug, intsonly, littleendian);
	        }
	        
		} catch ( Throwable t ) {
			t.printStackTrace( System.err );
		} finally {
			script.close();
		}
	}
}
