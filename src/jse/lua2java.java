/*******************************************************************************
* Copyright (c) 2010 Luaj.org. All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.luaj.vm2.Lua;
import org.luaj.vm2.ast.Chunk;
import org.luaj.vm2.lib.JsePlatform;
import org.luaj.vm2.lua2java.JavaCodeGen;
import org.luaj.vm2.parser.LuaParser;

/**
 * Compile lua sources into java sources. 
 */
public class lua2java {
	private static final String version = Lua._VERSION + "Copyright (C) 2010 luaj.org";

	private static final String usage = 
		"usage: java -cp luaj-jse.jar lua2java [options] [filenames].\n" +
		"Available options are:\n" +
		"  -		process stdin\n" +
		"  -s src	source directory\n" +
		"  -d dir	destination directory\n" +
		"  -p pkg	package prefix to apply to all classes\n" +
		"  -r		recursively compile all\n" +
		"  -v   	verbose\n";
	
	private static void usageExit() {
		System.out.println(usage);
		System.exit(-1);		
	}

	private String srcdir = null;
	private String destdir = null;
	private String pkgprefix = null;
	private boolean recurse = false;
	private boolean verbose = false;
	private List<InputFile> files = new ArrayList<InputFile>();

	public static void main( String[] args ) throws IOException {
		new lua2java( args );
	}

	private lua2java( String[] args ) throws IOException {
		
		// process args
		try {
			List<String> seeds = new ArrayList<String> ();
			
			// get stateful args
			for ( int i=0; i<args.length; i++ ) {
				if ( ! args[i].startsWith("-") ) {
					seeds.add(args[i]);
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
					case 'p':
						if ( ++i >= args.length )
							usageExit();
						pkgprefix = args[i];
						break;
					case 'r':
						recurse = true;
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
				System.out.println("destdir: "+destdir);
				System.out.println("files: "+files);
				System.out.println("recurse: "+recurse);
			}

			// collect up files to process
			for ( int i=0; i<seeds.size(); i++ )
				collectFiles( seeds.get(i) );
			
			// check for at least one file
			if ( files.size() <= 0 ) {
				System.err.println("no files found in "+seeds);
				System.exit(-1);
			}
			
			// process input files
			JsePlatform.standardGlobals();
			for ( InputFile inf : files )
				processFile( inf );
			
		} catch ( Exception ioe ) {
			System.err.println( ioe.toString() );
			System.exit(-2);
		}
	}
	
	private void collectFiles(String path) {
		File f = new File(path);
		if ( f.isDirectory() && recurse )
			scandir(f,pkgprefix);
		else if ( f.isFile() ) {
			File dir = f.getAbsoluteFile().getParentFile();
			if ( dir != null )
				scanfile( dir, f, pkgprefix );
		}
	}
	private void scandir(File dir, String javapackage) {
		File[] f = dir.listFiles();
		for ( int i=0; i<f.length; i++ ) 
			scanfile( dir, f[i], javapackage );
	}

	private void scanfile(File dir, File f, String javapackage) {
		if ( f.exists() ) {
			if ( f.isDirectory() && recurse )
				scandir( f, (javapackage!=null? javapackage+"."+f.getName(): f.getName()) );
			else if ( f.isFile() && f.getName().endsWith(".lua") )
				files.add( new InputFile(dir,f,javapackage) );
		}
	}

	class InputFile {
		public File infile;
		public File outdir;
		public File outfile;
		public String javapackage;
		public String javaclassname;
		public InputFile(File dir, File f, String javapackage) {
			String outdirpath = javapackage!=null? destdir+"/"+javapackage.replace('.', '/'): destdir;
			this.javaclassname = f.getName().substring(0,f.getName().lastIndexOf('.'));
			this.javapackage = javapackage;
			this.infile = f;
			this.outdir = new File(outdirpath);
			this.outfile = new File(outdirpath+"/"+this.javaclassname+".java");
		}
	}
	
	private void processFile( InputFile inf ) {
		inf.outdir.mkdirs();
		try {
			if ( verbose ) 
				System.out.println(
					"pkg="+inf.javapackage+" file="+inf.javaclassname+".java dest="+inf.outfile+" src="+inf.infile);
			FileInputStream in = new FileInputStream(inf.infile);
			FileOutputStream out = new FileOutputStream(inf.outfile);
			PrintWriter pw = new PrintWriter(out);
		    LuaParser parser = new LuaParser(in);
		    Chunk chunk = parser.Chunk();
			new JavaCodeGen(chunk,pw,inf.javapackage,inf.javaclassname);
			pw.close();
			out.close();
			in.close();
		} catch ( Throwable t ) {
			t.printStackTrace( System.err );
		}
	}
}
