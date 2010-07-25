package org.luaj.vm2.lua2java;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.LoadState.LuaCompiler;
import org.luaj.vm2.ast.Chunk;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.parser.LuaParser;

public class Lua2Java implements LuaCompiler {

	public static final Lua2Java instance = new Lua2Java();
	
	public static final void install() {
		LoadState.compiler = instance; 
	}

	private Lua2Java() {
	}

	public LuaFunction load(InputStream stream, String filename, LuaValue env) throws IOException {

		// get first byte
		if ( ! stream.markSupported() )
			stream = new BufferedInputStream( stream );
		stream.mark( 1 );
		int firstByte = stream.read();
		stream.reset();
		
		// we can only sompile sources
		if ( firstByte != '\033' ) {
			final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null)
				LuaValue.error("no java compiler");
			
			// break into package and class
			if ( filename.endsWith( ".lua") )
				filename = filename.substring(0, filename.length()-4);
			String s = filename.replace('\\', '/').replace('/','.').replaceAll("[^\\w]", "_");
			int p = s.lastIndexOf('.');
			final String packageName = p>=0? s.substring(0,p): null;
			final String className = toClassname( s.substring(p+1) );
			
			// open output file
			final String pkgSubdir = (packageName!=null? packageName.replace('.','/'): "");
			final String srcDirRoot = "lua2java/src";
			final String binDirRoot = "lua2java/classes";
			final String srcDirname = srcDirRoot+"/"+pkgSubdir;
			final String binDirname = binDirRoot+"/"+pkgSubdir;
			final String srcFilename = srcDirname + "/" + className + ".java";

			// make directories
			new File(srcDirname).mkdirs();
			new File(binDirname).mkdirs();
			
			// generate java source
			try {
			    LuaParser parser = new LuaParser(stream,"ISO8859-1");
			    Chunk chunk = parser.Chunk();
				File source = new File(srcFilename);
			    Writer writer = new OutputStreamWriter( new FileOutputStream(source) );
				new JavaCodeGen(chunk,writer,packageName,className);
				writer.close();
					
				// set up output location 
				Iterable<? extends File> dest = Arrays.asList(new File[] { new File(binDirRoot) });
				StandardJavaFileManager fm = compiler.getStandardFileManager( null, null, null);
				fm.setLocation(StandardLocation.CLASS_OUTPUT, dest);
				
				// compile the file
				Iterable<? extends JavaFileObject> compilationUnits = fm.getJavaFileObjects(source);
				CompilationTask task = compiler.getTask(null, fm, null, null, null, compilationUnits);
				boolean success = task.call();

				// instantiate, config and return
				if (success) {
					// create instance
					ClassLoader cl = new ClassLoader() {
				         public Class findClass(String classname) throws ClassNotFoundException {
				        	 if ( classname.startsWith(className) ) {
				        		 File f = new File( binDirname+"/"+classname+".class");
				        		 long n = f.length();
				        		 byte[] b = new byte[(int) n];
				        		 try {
					        		 DataInputStream dis = new DataInputStream( new FileInputStream(f) );
					        		 dis.readFully(b);
				        		 } catch ( Exception e ) {
				        			 throw new RuntimeException("failed to read class bytes: "+e );
				        		 }
					        	 return defineClass(classname, b, 0, b.length);
				        	 }
				        	 return super.findClass(classname);
				         }
					};
					Class clazz = cl.loadClass(className);
					Object instance = clazz.newInstance();
					LuaFunction value = (LuaFunction) instance;
					value.setfenv( env );
					return value;
				} else {
				}
			} catch ( Exception e ) {
				LuaValue.error("compile task failed: "+e);
			}
				
			// report compilation error
			LuaValue.error("compile task failed:");
			return null;
		}
		
		// fall back to plain compiler
		return LuaC.instance.load( stream, filename, env);
	}
	
	/** Convert lua filename to valid class name */
	public static final String toClassname( String filename ) {
		int n=filename.length();
		int j=n;
		if ( filename.endsWith(".lua") )
			j -= 4;
		for ( int k=0; k<j; k++ ) {
			char c = filename.charAt(k);
			if ( (!isClassnamePart(c)) || (c=='/') || (c=='\\') ) {
				StringBuffer sb = new StringBuffer(j);
				for ( int i=0; i<j; i++ ) {
					c = filename.charAt(i);
					sb.append( 
							 (isClassnamePart(c))? c:
							 ((c=='/') || (c=='\\'))? '.': '_' ); 
				}
				return sb.toString();
			}
		}
		return n==j? filename: filename.substring(0,j);
	}
	
	private static final boolean isClassnamePart(char c) {
		if ( (c>='a'&&c<='z') || (c>='A'&&c<='Z') || (c>='0'&&c<='9') )
			return true;
		switch ( c ) {
		case '.':
		case '$':
		case '_':
			return true;
		default:
			return false;
		}
	}	
}
