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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.luaj.vm2.Globals;
import org.luaj.vm2.Lua;
import org.luaj.vm2.Print;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.DumpState;
import org.luaj.vm2.lib.jse.JsePlatform;


/**
 * Compiler for lua files to lua bytecode. 
 */
public class luac {
	private static final String version = Lua._VERSION + "Copyright (C) 2009 luaj.org";

	private static final String usage = 
		"usage: java -cp luaj-jse.jar luac [options] [filenames].\n" +
		"Available options are:\n" +
		"  -        process stdin\n" +
		"  -l       list\n" +
		"  -o name  output to file 'name' (default is \"luac.out\")\n" +
		"  -p       parse only\n" +
		"  -s       strip debug information\n" +
		"  -e       little endian format for numbers\n" +
		"  -i<n>    number format 'n', (n=0,1 or 4, default="+DumpState.NUMBER_FORMAT_DEFAULT+")\n" +
		"  -v       show version information\n" +
		"  -c enc  	use the supplied encoding 'enc' for input files\n" +
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
	private int numberformat = DumpState.NUMBER_FORMAT_DEFAULT;
	private boolean versioninfo = false;
	private boolean processing = true;
	private String encoding = null;

	public static void main( String[] args ) throws IOException {
		new luac( args );
	}

	private luac( String[] args ) throws IOException {
		
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
						if ( args[i].length() <= 2 )
							usageExit();
						numberformat = Integer.parseInt(args[i].substring(2));
						break;
					case 'v':
						versioninfo = true;
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

			// open output file
			OutputStream fos = new FileOutputStream( output );
			
			// process input files
			try {
				Globals globals = JsePlatform.standardGlobals();
				processing = true;
				for ( int i=0; i<args.length; i++ ) {
					if ( ! processing || ! args[i].startsWith("-") ) {
						String chunkname = args[i].substring(0,args[i].length()-4);
						processScript( globals, new FileInputStream(args[i]), chunkname, fos );
					} else if ( args[i].length() <= 1 ) {
						processScript( globals, System.in, "=stdin", fos );
					} else {
						switch ( args[i].charAt(1) ) {
						case 'o':
						case 'c':
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
	
	private void processScript( Globals globals, InputStream script, String chunkname, OutputStream out ) throws IOException {
		try {
	        // create the chunk
	        Prototype chunk = encoding != null?
	        		globals.compilePrototype(new InputStreamReader(script, encoding), chunkname):
	        		globals.compilePrototype(script, chunkname);

	        // list the chunk
	        if (list)
	            Print.printCode(chunk);

	        // write out the chunk
	        if (!parseonly) {
	            DumpState.dump(chunk, out, stripdebug, numberformat, littleendian);
	        }
	        
		} catch ( Exception e ) {
			e.printStackTrace( System.err );
		} finally {
			script.close();
		}
	}
}
