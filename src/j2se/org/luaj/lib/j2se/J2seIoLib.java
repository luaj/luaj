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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.luaj.lib.BaseLib;
import org.luaj.lib.IoLib;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;


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
		return ( mode == null || mode.startsWith("r") )? 
			new InputFileImpl(
					"-".equals(filename)? 
							System.in:
							new FileInputStream(filename) ):
			new OutputFileImpl(
					"-".equals(filename)?
							System.out:
							new FileOutputStream(filename,  mode.endsWith("+")) );
	}

	private static void notimplemented() {
		throw new RuntimeException("not implemented");
	}
	
	private static final class InputFileImpl implements File {
		private final InputStream is;
		private boolean closed = false;
		private InputFileImpl( InputStream is ) {
			this.is = is.markSupported()? is: new BufferedInputStream(is);
		}
		public void close() throws IOException  {
			closed = true;
			is.close();
		}
		public void flush() {
			notimplemented();
		}
		public void write(LString string) {
			notimplemented();
		}
		public boolean isclosed() {
			return closed;
		}
		public int seek(String option, int bytecount) throws IOException {
			notimplemented();
			return 0;
		}
		public byte[] readBytes(int count) throws IOException {
			byte[] b = new byte[count];
			int n;
			for ( int i=0; i<count; ) {
				n = is.read(b,i,count-i);
				if ( n < 0 )
					throw new java.io.EOFException("eof");
				i += n;
			}
			return b;
		}
		public LString readLine() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ( true ) { 
				int c = is.read();
				if ( c < 0 || c == '\n' )
					break;
				baos.write(c);
			}
			return new LString(baos.toByteArray());
		}
		public LString readFile() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ( true ) { 
				int c = is.read();
				if ( c < 0 )
					break;
				baos.write(c);
			}
			return new LString(baos.toByteArray());
		}
		public Double readNumber() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int c;
			while ( true ) {
				c = is.read();
				if (  c < 0 )
					return null;
				if ( "\t\r\n ".indexOf(c) < 0 )
					break;
			}
			if ( (c < '0' || c > '9') && c != '-' && c != '.' )
				return null;
			baos.write(c);
			while ( true ) { 
				is.mark(1);
				c = is.read();
				if ( c < 0 )
					break;
				if ( (c < '0' || c > '9') && c != '-' && c != '.' ) {
					is.reset();
					break;
				} else {
					baos.write( c );
				}
			}
			return Double.valueOf(baos.toString());
		}		
	}

	private static final class OutputFileImpl implements File {
		private final OutputStream os;
		private boolean closed = false;
		private OutputFileImpl( OutputStream os ) {
			this.os = os;
		}
		public void close() throws IOException {
			closed = true;
			os.close();
		}
		public void flush() throws IOException {
			os.flush();
		}
		public void write(LString s) throws IOException {
			os.write(s.m_bytes, s.m_offset, s.m_length);
		}
		public boolean isclosed() {
			return closed;
		}		
		public int seek(String option, int bytecount) throws IOException {
			notimplemented();
			return 0;
		}		
		public byte[] readBytes(int count) throws IOException {
			notimplemented();
			return null;
		}
		public LString readLine() throws IOException {
			notimplemented();
			return null;
		}
		public Double readNumber() throws IOException {
			notimplemented();
			return null;
		}		
		public LString readFile() throws IOException {
			notimplemented();
			return null;
		}
	}
	
}
