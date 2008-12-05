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
package org.luaj.lib.j2se;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.luaj.lib.BaseLib;
import org.luaj.lib.IoLib;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;


public class J2seIoLib extends IoLib {
	
	public static void install( LTable globals ) {
		new J2seIoLib().initialize(globals);
	}

	public J2seIoLib() {
		super();
	}
	
	public J2seIoLib(int index) {
		super(index);
	}

	protected IoLib newInstance(int index) {
		return new J2seIoLib(index);
	}
	
	protected File openFile(String filename, String mode) throws IOException {
		boolean isstdfile = "-".equals(filename);
		boolean isreadmode = mode.startsWith("r");
		if ( isstdfile ) 
			return isreadmode? 
					new FileImpl(BaseLib.STDIN != null? BaseLib.STDIN: System.in): 
					new FileImpl(BaseLib.STDOUT != null? BaseLib.STDOUT: System.out);
		boolean isappend = mode.startsWith("a");
		// TODO: handle update mode 
		// boolean isupdate = mode.endsWith("+");
		RandomAccessFile f = new RandomAccessFile(filename,isreadmode? "r": "rw");
		if ( isappend ) {
			f.seek(f.length());
		} else {
			if ( ! isreadmode )
				f.setLength(0);
		}
		return new FileImpl( f );
	}

	private static void notimplemented() {
		throw new RuntimeException("not implemented");
	}
	
	private static final class FileImpl implements File {
		private final RandomAccessFile file;
		private final InputStream is;
		private final OutputStream os;
		private boolean closed = false;
		private FileImpl( RandomAccessFile file, InputStream is, OutputStream os ) {
			this.file = file;
			this.is = is!=null? is.markSupported()? is: new BufferedInputStream(is): null;
			this.os = os;
		}
		private FileImpl( RandomAccessFile f ) {
			this( f, null, null );
		}
		private FileImpl( InputStream i ) {
			this( null, i, null );
		}
		private FileImpl( OutputStream o ) {
			this( null, null, o );
		}
		public String toString() {
			return "file ("+this.hashCode()+")";
		}
		public void close() throws IOException  {
			closed = true;
			if ( file != null )
				file.close();
			else if ( os != null )
				os.close();
			else if ( is != null )
				is.close();
		}
		public void flush() throws IOException {
			if ( os != null )
				os.flush();
		}
		public void write(LString s) throws IOException {
			if ( os != null )
				os.write( s.m_bytes, s.m_offset, s.m_length );
			else if ( file != null )
				file.write( s.m_bytes, s.m_offset, s.m_length );
			else
				notimplemented();
		}
		public boolean isclosed() {
			return closed;
		}
		public int seek(String option, int pos) throws IOException {
			if ( file != null ) {
				if ( "set".equals(option) ) {
					file.seek(pos);
					return (int) file.getFilePointer();
				} else if ( "end".equals(option) ) {
					file.seek(file.length()+1+pos);
					return (int) file.length()+1;
				} else {
					file.seek(file.getFilePointer()+pos);
					return (int) file.getFilePointer();
				}
			}
			notimplemented();
			return 0;
		}
		public LValue readBytes(int count) throws IOException {
			if ( file != null ) {
				byte[] b = new byte[count];
				file.readFully(b);
				return new LString(b);
			}
			notimplemented();
			return LNil.NIL;
		}
		public LValue readLine() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int c;
			while ( true ) { 
				if ( is != null ) {
					c = is.read();
				} else {
					c = file.read();
				}
				if ( c < 0 || c == '\n' )
					break;
				baos.write(c);
			}
			return ( c < 0 && baos.size() == 0 )? 
				LNil.NIL:
				new LString(baos.toByteArray());
		}
		public LValue readFile() throws IOException {
			if ( file != null ) {
				return readBytes((int) (file.length() - file.getFilePointer()));
			} 
			notimplemented();
			return null;
		}
		public Double readNumber() throws IOException {
			if ( is == null && file == null )
				notimplemented();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			readChars(" \t\r\n",null);
			readChars("-+",baos);
			//readChars("0",baos);
			//readChars("xX",baos);
			readChars("0123456789",baos);
			readChars(".",baos);
			readChars("0123456789",baos);
			//readChars("eEfFgG",baos);
			// readChars("+-",baos);
			//readChars("0123456789",baos);
			String s = baos.toString();
			return s.length()>0? Double.valueOf(s): null;
		}
		private void readChars(String chars, ByteArrayOutputStream baos) throws IOException {
			int c;
			while ( true ) {
				if ( is != null ) {
					is.mark(1);
					c = is.read();
				} else {
					c = file.read();
				}
				if ( chars.indexOf(c) < 0 ) {
					if ( is != null )
						is.reset();
					else if ( file != null )
						file.seek(file.getFilePointer()-1);
					return;
				}
				if ( baos != null )
					baos.write( c );
			}
		}		
	}
	
	protected File openProgram(String prog, String mode) throws IOException {
		final Process p = Runtime.getRuntime().exec(prog);
		return "w".equals(mode)? 
				new FileImpl( p.getOutputStream() ):  
				new FileImpl( p.getInputStream() ); 
	}
}
