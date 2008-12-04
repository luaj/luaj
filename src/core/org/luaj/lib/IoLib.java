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
package org.luaj.lib;

import java.io.IOException;

import org.luaj.vm.LFunction;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LUserData;
import org.luaj.vm.LuaState;


abstract 
public class IoLib extends LFunction {

	protected interface File {
		public void write( LString string ) throws IOException;
		public void flush() throws IOException;
		public void close() throws IOException;
		public boolean isclosed();
		/** returns new position */
		public int seek(String option, int bytecount) throws IOException;
		public byte[] readBytes(int count) throws IOException;
		public Double readNumber() throws IOException;
		public LString readLine() throws IOException;
		public LString readFile() throws IOException;
	}


	/** 
	 * Create a function stub with a specific index (factory method)
	 */
	abstract protected IoLib newInstance( int index );

	/**
	 * Open a file in a particular mode. 
	 * @param filename
	 * @param mode
	 * @return File object if successful
	 * @throws IOException if could not be opened
	 */
	abstract protected File openFile(String filename, String mode) throws IOException;

	
	public static final String[] NAMES = {
		"io",
		"close",
		"flush",
		"input",
		"lines",
		"open",
		"output",
		"popen",
		"read",
		"tmpfile",
		"type",
		"write",
		"close",
		"flush",
		"lines",
		"read",
		"seek",
		"setvbuf",
		"write",
	};
	
	private static final int INSTALL      = 0;
	private static final int IO_CLOSE     = 1;
	private static final int IO_FLUSH     = 2;
	private static final int IO_INPUT     = 3;
	private static final int IO_LINES     = 4;
	private static final int IO_OPEN      = 5;
	private static final int IO_OUTPUT    = 6;
	private static final int IO_POPEN     = 7;
	private static final int IO_READ      = 8;
	private static final int IO_TMPFILE   = 9;
	private static final int IO_TYPE      = 10;
	private static final int IO_WRITE     = 11;
	private static final int FILE_CLOSE   = 12;
	private static final int FILE_FLUSH   = 13;
	private static final int FILE_LINES   = 14;
	private static final int FILE_READ    = 15;
	private static final int FILE_SEEK    = 16;
	private static final int FILE_SETVBUF = 17;
	private static final int FILE_WRITE   = 18;

	private static File INPUT  = null;
	private static File OUTPUT = null;
	private static File ERROR  = null;
	
	private static final LTable FILE_MT = new LTable();

	protected void initialize( LTable globals ) {
		try {
			LTable io = new LTable();
			for ( int i=IO_CLOSE; i<=IO_WRITE; i++ )
				io.put(NAMES[i], newInstance(i));
			INPUT = openFile("-", "r");
			OUTPUT = openFile("-", "w");
			ERROR = OUTPUT;
			io.put("stdin", new LUserData(INPUT));
			io.put("stdout", new LUserData(OUTPUT));
			io.put("stderr", new LUserData(ERROR));
			for ( int i=FILE_CLOSE; i<=FILE_WRITE; i++ )
				FILE_MT.put(NAMES[i], newInstance(i));
			FILE_MT.put("__index", FILE_MT);
			globals.put( "io", io );
			PackageLib.setIsLoaded("io", io);
		} catch ( IOException ioe ) {
			throw new RuntimeException("io error: "+ioe.getMessage());
		}
	}
	
	private final int id;

	protected IoLib() {
		id = 0;
	}

	protected IoLib( int id ) {
		this.id = id;
	}

	public String toString() {
		return NAMES[id]+"()";
	}
		
	public boolean luaStackCall( LuaState vm ) {
		File f;
		String s;
		int i,n;
		try {
		switch ( id ) {
			/* Load the table library dynamically */
			case INSTALL:
				initialize(vm._G);
				break;
			case IO_CLOSE:
				optfile(vm, 2, OUTPUT).close();
				vm.resettop();
				vm.pushboolean(true);
				break;
			case IO_FLUSH:
				checkopen(vm,OUTPUT);
				OUTPUT.flush();
				vm.resettop();
				vm.pushboolean(true);
				break;
			case IO_INPUT:
				INPUT = ((s = vm.optstring(1, null)) != null)? 
						ioopenfile(vm,s,"r"):
						optfile(vm,1,INPUT);
				setresult(vm, INPUT);
				break;
			case IO_LINES:
				break;
			case IO_OPEN:
				setresult(vm, openFile(vm.checkstring(2), vm.optstring(3,"r")));
				break;
			case IO_OUTPUT:
				OUTPUT = ((s = vm.optstring(2, null)) != null)? 
						ioopenfile(vm,s,"w"):
						optfile(vm,1,OUTPUT);
				setresult(vm, OUTPUT);
				break;
			case IO_POPEN:
				break;
			case IO_READ:
				ioread( vm, INPUT );
				break;
			case IO_TMPFILE:
				break;
			case IO_TYPE:
				f = optfile(vm,2,null);
				vm.resettop();
				if ( f != null )
					vm.pushstring(f.isclosed()? "closed file": "file");
				else 
					vm.pushnil();
				break;
			case IO_WRITE:
				iowrite( vm, OUTPUT );
				break;
			case FILE_CLOSE:
				checkfile(vm,2).close();
				vm.resettop();
				vm.pushboolean(true);
				break;
			case FILE_FLUSH:
				checkfile(vm,2).flush();
				vm.resettop();
				vm.pushboolean(true);
				break;
			case FILE_LINES:
				break;
			case FILE_READ:
				f = checkfile(vm,2);
				vm.remove(2);
				ioread(vm, f);
				break;
			case FILE_SEEK:
				n = checkfile(vm,2).seek(vm.optstring(1,"cur"),vm.optint(3, 0));
				vm.resettop();
				vm.pushinteger(n);
				break;
			case FILE_SETVBUF:
				break;
			case FILE_WRITE:
				f = checkfile(vm,2);
				vm.remove(2);
				iowrite( vm, f );
				break;
			default:
				LuaState.vmerror( "bad io id" );
			}
		} catch ( IOException ioe ) {
			vm.resettop();
			vm.pushnil();
			vm.pushstring("io error: "+ioe.getMessage());
		}
		return false;
	}
	
	private static void iowrite(LuaState vm, File f) throws IOException {
		checkopen(vm,f);
		for ( int i=2, n=vm.gettop(); i<=n; i++ )
			f.write( vm.tolstring(i) );
		vm.resettop();
		vm.pushboolean(true);
	}

	private static void ioread(LuaState vm, File f) throws IOException {
		checkopen( vm, f );
		int i,n=vm.gettop();
		for ( i=2; i<=n; i++ ) {
			if ( vm.isnumber(i) ) {
				vm.pushlstring(f.readBytes(vm.tointeger(i)));
			} else {
				String format = vm.checkstring(i);
				if ( "*n".equals(format) ) 
					vm.pushnumber(f.readNumber());
				else if ( "*a".equals(format) ) 
					vm.pushlstring(f.readFile());
				else if ( "*l".equals(format) )
					vm.pushlstring(f.readLine());
				else
					vm.typerror( i, "(invalid format)" );
			}
		}
		for ( i=1; i<=n; i++ )
			vm.remove(1);
	}

	private static File checkfile(LuaState vm, int index) {
		return (File) vm.checkudata(index, File.class);
	}
	
	private static File optfile(LuaState vm, int index, File defval) {
		Object u = vm.touserdata(index);
		return (u instanceof File? (File) u: defval);
	}
	
	private static void checkopen(LuaState vm, File file) {
		if ( file.isclosed() )
			vm.error("attempt to use a closed file");
	}
	
	private static void setresult(LuaState vm, File file) {
		vm.settop(0);
		vm.pushlvalue(new LUserData(file, FILE_MT));
	}

	private File ioopenfile(LuaState vm, String filename, String mode) {
		try {
			File f = openFile( filename, mode );
			setresult( vm, f );
			return f;
		} catch ( Exception e ) {
			vm.error("io error: "+e.getMessage());
			return null;
		}
	}
	
}
