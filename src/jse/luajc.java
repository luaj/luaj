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
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.luaj.vm2.Lua;
import org.luaj.vm2.compiler.DumpState;
import org.luaj.vm2.luajc.JavaBytecodeCompiler;



/**
 * Compiler for lua files to compile lua sources into java sources. 
 */
public class luajc {
	private static final String version = Lua._VERSION + "Copyright (C) 2009 luaj.org";

	private static final String usage = 
		"usage: java -cp luaj-jse.jar,antlr-3.1.3.jar luajc [options] [filenames].\n" +
		"Available options are:\n" +
		"  -        process stdin\n" +
		"  -o name  output to file 'name' (default is \"luac.out\")\n" +
		"  -p       parse only\n" +
		"  -s       strip debug information\n" +
		"  -e       little endian format for numbers\n" +
		"  -i<n>    number format 'n', (n=0,1 or 4, default="+DumpState.NUMBER_FORMAT_DEFAULT+")\n" +
		"  -v       show version information\n" +
		"  --       stop handling options\n";
	
	private static void usageExit() {
		System.out.println(usage);
		System.exit(-1);		
	}

	private String output = "luacj.out";
	private boolean parseonly = false;
	private boolean stripdebug = false;
	private boolean littleendian = false;
	private int numberformat = DumpState.NUMBER_FORMAT_DEFAULT;
	private boolean versioninfo = false;
	private boolean processing = true;

	public static void main( String[] args ) throws IOException {
		new luajc( args );
	}

	private luajc( String[] args ) throws IOException {
		
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
						String chunkname = args[i];
						processScript( new FileInputStream(args[i]), chunkname, fos );
					} else if ( args[i].length() <= 1 ) {
						processScript( System.in, "stdin", fos );
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
	
	private void processScript( InputStream script, String chunkname, OutputStream out ) throws IOException {
		try {
	        // create the chunk
			Hashtable t = JavaBytecodeCompiler.loadClasses(script, chunkname);

	        // write out the chunk
	        if (!parseonly) {
	        	for ( Enumeration e = t.keys(); e.hasMoreElements(); ) {
	        		String key = (String) e.nextElement();
	        		byte[] bytes = (byte[]) t.get(key);
	        		String filename = key + ".class";
	    			if ( versioninfo )
	    				System.out.println(filename+": "+bytes.length+" bytes");
		        	FileOutputStream fos = new FileOutputStream( filename );
		        	fos.write( bytes );
		        	fos.close();
	        	}
	        }
	        
		} catch ( Throwable t ) {
			t.printStackTrace( System.err );
		} finally {
			script.close();
		}
	}
}
