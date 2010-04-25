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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.luaj.vm2.Lua;
import org.luaj.vm2.compiler.DumpState;
import org.luaj.vm2.luajc.LuaJC;

/**
 * Compiler for lua files to compile lua sources or lua binaries into java classes. 
 */
public class luajc {
	private static final String version = Lua._VERSION + "Copyright (C) 2009 luaj.org";

	private static final String usage = 
		"usage: java -cp luaj-jse.jar,bcel-5.2.jar luajc [options] [filenames].\n" +
		"Available options are:\n" +
		"  -        process stdin\n" +
		"  -s		source directory\n" +
		"  -d		destination directory\n" +
		"  -n       no debug information (strip debug)\n" +
		"  -e       little endian format for numbers\n" +
		"  -i<n>    number format 'n', (n=0,1 or 4, default="+DumpState.NUMBER_FORMAT_DEFAULT+")\n" +
		"  -v       show version information\n" +
		"  --       stop handling options\n";
	
	private static void usageExit() {
		System.out.println(usage);
		System.exit(-1);		
	}

	private String srcdir = null;
	private String destdir = null;
	private boolean stripdebug = false;
	private boolean littleendian = false;
	private int numberformat = DumpState.NUMBER_FORMAT_DEFAULT;
	private boolean verbose = false;

	public static void main( String[] args ) throws IOException {
		new luajc( args );
	}

	private luajc( String[] args ) throws IOException {
		
		// process args
		try {
			Vector files = new Vector();
			
			// get stateful args
			for ( int i=0; i<args.length; i++ ) {
				if ( ! args[i].startsWith("-") ) {
					files.add(args[i]);
				} else {
					switch ( args[i].charAt(1) ) {
					case 's':
						if ( ++i >= args.length )
							usageExit();
						srcdir = args[i];
						break;
					case 'd':
						if ( ++i >= args.length )
							usageExit();
						destdir = args[i];
						break;
					case 'n':
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
						verbose = true;
						break;
					default:
						usageExit();
						break;
					}
				}
			}
			
			// echo version
			if ( verbose ) {
				System.out.println(version);
				System.out.println("srcdir: "+srcdir);
				System.out.println("destdir: "+srcdir);
				System.out.println("stripdebug: "+stripdebug);
				System.out.println("littleendian: "+littleendian);
				System.out.println("numberformat: "+numberformat);
				System.out.println("files: "+files);
			}

			// process input files
			for ( int i=0; i<files.size(); i++ ) {
				String filename = (String) files.elementAt(i);
				int index = filename.lastIndexOf('.');
				if ( index < 0 )
					usageExit();
				String chunkname = filename.substring(0,index);
				String sourcepath = srcdir!=null? srcdir+"/"+filename: filename;
				if ( verbose )
					System.out.println("filename="+filename+" chunkname="+filename+" sourcepath="+sourcepath);
				InputStream is = new FileInputStream( sourcepath );
				processScript( is, chunkname, filename );
			}
			
		} catch ( IOException ioe ) {
			System.err.println( ioe.toString() );
			System.exit(-2);
		}
	}
	
	private void processScript( InputStream script, String chunkname, String filename) throws IOException {
		try {
	        // create the chunk
			Hashtable t = LuaJC.getInstance().compileAll(script, chunkname, filename);

	        // write out the chunk
        	for ( Enumeration e = t.keys(); e.hasMoreElements(); ) {
        		String key = (String) e.nextElement();
        		byte[] bytes = (byte[]) t.get(key);
        		String destpath = (destdir!=null? destdir+"/": "") + key + ".class";
    			if ( verbose )
    				System.out.println( 
    						"chunk "+chunkname+
    						" from "+filename+
    						" written to "+destpath
    						+" length="+bytes.length+" bytes");
	        	FileOutputStream fos = new FileOutputStream( destpath );
	        	fos.write( bytes );
	        	fos.close();
	        }
	        
		} catch ( Throwable t ) {
			t.printStackTrace( System.err );
		} finally {
			script.close();
		}
	}
}
