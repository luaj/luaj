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
package org.luaj.vm2.lib;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;



abstract 
public class IoLib extends VarArgFunction {

	abstract 
	protected class File extends LuaValue{
		abstract public void write( LuaString string ) throws IOException;
		abstract public void flush() throws IOException;
		abstract public boolean isstdfile();
		abstract public void close() throws IOException;
		abstract public boolean isclosed();
		// returns new position
		abstract public int seek(String option, int bytecount) throws IOException;
		abstract public void setvbuf(String mode, int size);		
		// get length remaining to read
		abstract public int remaining() throws IOException;		
		// peek ahead one character
		abstract public int peek() throws IOException, EOFException;		
		// return char if read, -1 if eof, throw IOException on other exception 
		abstract public int read() throws IOException, EOFException;
		// return number of bytes read if positive, false if eof, throw IOException on other exception
		abstract public int read(byte[] bytes, int offset, int length) throws IOException;
		
		// delegate method access to file methods table
		public LuaValue get( LuaValue key ) {
			return env.get(FILEMETHODS).get(key);
		}

		// essentially a userdata instance
		public int type() {
			return LuaValue.TUSERDATA;
		}
		public String typename() {
			return "userdata";
		}
		
		// displays as "file" type
		public String toString() {
			return "file: " + Integer.toHexString(hashCode());
		}
	}


	/** 
	 * Wrap the standard input. 
	 * @return File 
	 * @throws IOException
	 */
	abstract protected File wrapStdin() throws IOException;

	/** 
	 * Wrap the standard output. 
	 * @return File 
	 * @throws IOException
	 */
	abstract protected File wrapStdout() throws IOException;
	
	/**
	 * Open a file in a particular mode. 
	 * @param filename
	 * @param mode
	 * @return File object if successful
	 * @throws IOException if could not be opened
	 */
	abstract protected File openFile( String filename, boolean readMode, boolean appendMode, boolean updateMode, boolean binaryMode ) throws IOException;

	/**
	 * Open a temporary file. 
	 * @return File object if successful
	 * @throws IOException if could not be opened
	 */
	abstract protected File tmpFile() throws IOException;

	/**
	 * Start a new process and return a file for input or output
	 * @param prog the program to execute
	 * @param mode "r" to read, "w" to write
	 * @return File to read to or write from 
	 * @throws IOException if an i/o exception occurs
	 */
	abstract protected File openProgram(String prog, String mode) throws IOException;

	private File infile  = null;
	private File outfile = null;
	private File errfile  = null;

	private static final LuaValue STDIN       = valueOf("stdin");
	private static final LuaValue STDOUT      = valueOf("stdout");
	private static final LuaValue STDERR      = valueOf("stderr");		
	private static final LuaValue FILE        = valueOf("file");
	private static final LuaValue CLOSED_FILE = valueOf("closed file");
	private static final LuaValue FILEMETHODS = valueOf("__filemethods");
	private static final LuaValue LINESITER   = valueOf("__linesiter");
	
	public IoLib() {}
	
	protected LuaTable init() {
		
		// io lib functions
		LuaTable t = new LuaTable();
		LibFunction.bind(t, this.getClass(),  new String[] {
			"flush", "tmpfile", "close", "input", "output", 
			"type", "popen", "open", "lines", "read", 
			"write", "__linesiter" }, 1 );
		
		// create metatable
		LuaTable idx = new LuaTable();
		LibFunction.bind(idx, this.getClass(),  new String[] {
			"__index", }, 13 );
		t.setmetatable( idx );
		
		// create file methods table
		LuaTable filemethods = new LuaTable();
		LibFunction.bind(filemethods, this.getClass(),  new String[] {
			"close", "flush", "setvbuf", "lines", "read", 
			"seek", "write", }, 14 );
		t.set(FILEMETHODS, filemethods);

		// return the table
		return t;
	}
	
	public Varargs invoke(Varargs args) {
		File f;
		int n;
		LuaValue v;
		try {
			switch ( opcode ) {
			case 0: // init 
				return init();
			case 1: //	io.flush() -> bool 
				checkopen(output());
				outfile.flush();
				return LuaValue.TRUE;
			case 2: //	io.tmpfile() -> file
				return tmpFile();
			case 3: //	io.close([file]) -> void
				f = args.arg1().isnil()? output(): checkfile(args.arg1());
				checkopen(f);
				return ioclose(f);
			case 4: //	io.input([file]) -> file
				infile = args.arg1().isnil()? input(): args.arg1().isstring()? 
						ioopenfile(args.checkString(1),"r"):
						checkfile(args.arg1());
				return infile;
				
			case 5: // io.output(filename) -> file
				outfile = args.arg1().isnil()? output(): args.arg1().isstring()? 
						ioopenfile(args.checkString(1),"w"):
						checkfile(args.arg1());
				return outfile;
			case 6: //	io.type(obj) -> "file" | "closed file" | nil
				if ( (f=optfile(args.arg1())) != null )
					return f.isclosed()? CLOSED_FILE: FILE;
				return NIL;
			case 7: // io.popen(prog, [mode]) -> file
				return openProgram(args.checkString(1),args.optString(2,"r"));
			case 8: //	io.open(filename, [mode]) -> file | nil,err
				return rawopenfile(args.checkString(1), args.optString(2,"r"));
			case 9: //	io.lines(filename) -> iterator
				infile = args.arg1().isnil()? input(): ioopenfile(args.checkString(1),"r");
				checkopen(infile);
				return lines(infile);
			case 10: //	io.read(...) -> (...)
				checkopen(infile);
				return ioread(infile,args);
			case 11: //	io.write(...) -> void
				checkopen(output());
				return iowrite(outfile,args);
			case 12: //	lines iterator(s,var) -> var'
				f = checkfile(args.arg1());
				return freadline(f);

			// ------------ __index metatable operation
			case 13: // __index, returns a field
				v = args.arg(2);
				return v.equals(STDOUT)?output():
					   v.equals(STDIN)?  input():
					   v.equals(STDERR)? errput(): NIL;
				
			// ------------ file metatable operations
			
			case 14: // file:close() -> void
				return ioclose(checkfile(args.arg1()));
			case 15: // file:flush() -> void
				checkfile(args.arg1()).flush();
				return LuaValue.TRUE;
			case 16: // file:setvbuf(mode,[size]) -> void
				f = checkfile(args.arg1());
				f.setvbuf(args.checkString(2),args.optint(3, 1024));
				return LuaValue.TRUE;
			case 17: // file:lines() -> iterator
				return lines(checkfile(args.arg1()));
			case 18: //	file:read(...) -> (...)
				f = checkfile(args.arg1());
				return ioread(f,args.subargs(2));
			case 19: //  file:seek([whence][,offset]) -> pos | nil,error
				f = checkfile(args.arg1());
				n = f.seek(args.optString(2,"cur"),args.optint(3,0));
				return valueOf(n);
			case 20: //	file:write(...) -> void		
				f = checkfile(args.arg1());
				return iowrite(f,args.subargs(2));
			}
		} catch ( IOException ioe ) {
			return errorresult(ioe);
		}
		return NONE;
	}
	
	private File input() {
		return infile!=null? infile: (infile=ioopenfile("-","r"));
	}
	
	private File output() {
		return outfile!=null? outfile: (outfile=ioopenfile("-","w"));
	}
	
	private File errput() {
		return errfile!=null? errfile: (errfile=ioopenfile("-","w"));
	}
	
	private File ioopenfile(String filename, String mode) {
		try {
			return rawopenfile(filename, mode);
		} catch ( Exception e ) {
			error("io error: "+e.getMessage());
			return null;
		}
	}

	private static Varargs ioclose(File f) throws IOException {
		if ( f.isstdfile() )
			return errorresult("cannot close standard file");
		else {
			f.close();
			return successresult();
		}
	}

	private static Varargs successresult() {
		return LuaValue.TRUE;
	}

	private static Varargs errorresult(IOException ioe) {
		String s = ioe.getMessage();		
		return errorresult("io error: "+(s!=null? s: ioe.toString()));
	}
	
	private static Varargs errorresult(String errortext) {
		return varargsOf(NIL, valueOf(errortext));
	}

	// TODO: how to close on finalization
	private Varargs lines(final File f) throws IOException {
		LuaValue linesiter = env.get(LINESITER);
		return varargsOf( linesiter, f );
	}

	private static Varargs iowrite(File f, Varargs args) throws IOException {
		for ( int i=1, n=args.narg(); i<=n; i++ )
			f.write( args.checkstring(i) );
		return LuaValue.TRUE;
	}

	private Varargs ioread(File f, Varargs args) throws IOException {
		int i,n=args.narg();
		LuaValue[] v = new LuaValue[n];
		for ( i=0; i<n; i++ ) {
			if ( args.isnumber(i+1) ) {
				v[i] = freadbytes(f,args.checkint(i+1));
			} else {
				String format = args.checkString(i+1);
				if ( "*n".equals(format) ) 
					v[i] = valueOf( freadnumber(f) );
				else if ( "*a".equals(format) ) 
					v[i] = freadall(f);
				else if ( "*l".equals(format) )
					v[i] = freadline(f);
				else
					typerror( i+1, "(invalid format)" );
			}
			if ( v[i].isnil() )
				return varargsOf(v,0,i);
		}
		return varargsOf(v);
	}

	private static File checkfile(LuaValue val) {
		File f = optfile(val);
		if ( f == null )
			argerror(1,"file");
		checkopen( f );
		return f;
	}
	
	private static File optfile(LuaValue val) {
		return (val instanceof File)? (File) val: null;
	}
	
	private static File checkopen(File file) {
		if ( file.isclosed() )
			error("attempt to use a closed file");
		return file;
	}
	
	private File rawopenfile(String filename, String mode) throws IOException {
		boolean isstdfile = "-".equals(filename);
		boolean isreadmode = mode.startsWith("r");
		if ( isstdfile ) {
			return isreadmode? 
				wrapStdin():
				wrapStdout();
		}
		boolean isappend = mode.startsWith("a");
		boolean isupdate = mode.indexOf("+") > 0;
		boolean isbinary = mode.endsWith("b");
		return openFile( filename, isreadmode, isappend, isupdate, isbinary );
	}


	// ------------- file reading utilitied ------------------
	
	public static LuaValue freadbytes(File f, int count) throws IOException {
		byte[] b = new byte[count];
		int r;
		if ( ( r = f.read(b,0,b.length) ) < 0 )
			return NIL;
		return valueOf(b, 0, r);
	}
	public static LuaValue freaduntil(File f,int delim) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int c;
		try {
			while ( true ) { 
				c = f.read();
				if ( c < 0 || c == delim )
					break;
				baos.write(c);
			}
		} catch ( EOFException e ) {
			c = -1;
		}
		return ( c < 0 && baos.size() == 0 )? 
			(LuaValue) NIL:
			(LuaValue) valueOf(baos.toByteArray());
	}
	public static LuaValue freadline(File f) throws IOException {
		return freaduntil(f,'\n');
	}
	public static LuaValue freadall(File f) throws IOException {
		int n = f.remaining();
		if ( n >= 0 ) {
			return freadbytes(f, n);
		} else {
			return freaduntil(f,-1);
		}
	}
	public static double freadnumber(File f) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		freadchars(f," \t\r\n",null);
		freadchars(f,"-+",baos);
		//freadchars(f,"0",baos);
		//freadchars(f,"xX",baos);
		freadchars(f,"0123456789",baos);
		freadchars(f,".",baos);
		freadchars(f,"0123456789",baos);
		//freadchars(f,"eEfFgG",baos);
		// freadchars(f,"+-",baos);
		//freadchars(f,"0123456789",baos);
		String s = baos.toString();
		return s.length()>0? Double.parseDouble(s): 0;
	}
	private static void freadchars(File f, String chars, ByteArrayOutputStream baos) throws IOException {
		int c;
		while ( true ) {
			c = f.peek();
			if ( chars.indexOf(c) < 0 ) {
				return;
			}
			f.read();
			if ( baos != null )
				baos.write( c );
		}
	}		
	
	
	
}
