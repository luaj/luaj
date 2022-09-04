/*******************************************************************************
* Copyright (c) 2009-2011 Luaj.org. All rights reserved.
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
package org.luaj.vm2.lib.jme;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.IoLib;
import org.luaj.vm2.lib.LibFunction;

/**
 * Subclass of {@link IoLib} and therefore {@link LibFunction} which implements
 * the lua standard {@code io} library for the JSE platform.
 * <p>
 * The implementation of the is based on CLDC 1.0 and StreamConnection. However,
 * seek is not supported.
 * <p>
 * Typically, this library is included as part of a call to
 * {@link org.luaj.vm2.lib.jme.JmePlatform#standardGlobals()}
 *
 * <pre>
 * {
 * 	&#64;code
 * 	Globals globals = JmePlatform.standardGlobals();
 * 	globals.get("io").get("write").call(LuaValue.valueOf("hello, world\n"));
 * }
 * </pre>
 * <p>
 * For special cases where the smallest possible footprint is desired, a minimal
 * set of libraries could be loaded directly via {@link Globals#load(LuaValue)}
 * using code such as:
 *
 * <pre>
 * {
 * 	&#64;code
 * 	Globals globals = new Globals();
 * 	globals.load(new JmeBaseLib());
 * 	globals.load(new PackageLib());
 * 	globals.load(new JmeIoLib());
 * 	globals.get("io").get("write").call(LuaValue.valueOf("hello, world\n"));
 * }
 * </pre>
 * <p>
 * However, other libraries such as <em>MathLib</em> are not loaded in this
 * case.
 * <p>
 * This has been implemented to match as closely as possible the behavior in the
 * corresponding library in C.
 *
 * @see LibFunction
 * @see org.luaj.vm2.lib.jse.JsePlatform
 * @see org.luaj.vm2.lib.jme.JmePlatform
 * @see IoLib
 * @see org.luaj.vm2.lib.jse.JseIoLib
 * @see <a href="http://www.lua.org/manual/5.2/manual.html#6.8">Lua 5.2 I/O Lib
 *      Reference</a>
 */
public class JmeIoLib extends IoLib {

	@Override
	protected File wrapStdin() throws IOException {
		return new FileImpl(globals.STDIN);
	}

	@Override
	protected File wrapStdout() throws IOException {
		return new FileImpl(globals.STDOUT);
	}

	@Override
	protected File wrapStderr() throws IOException {
		return new FileImpl(globals.STDERR);
	}

	@Override
	protected File openFile(String filename, boolean readMode, boolean appendMode, boolean updateMode,
		boolean binaryMode) throws IOException {
		String url = "file:///" + filename;
		int mode = readMode? Connector.READ: Connector.READ_WRITE;
		StreamConnection conn = (StreamConnection) Connector.open(url, mode);

		/*
		if ( appendMode ) {
			f.seek("end",0);
		} else {
			if ( ! readMode )
				conn.truncate(0);
		}
		*/
		return readMode? new FileImpl(conn, conn.openInputStream(), null)
			: new FileImpl(conn, conn.openInputStream(), conn.openOutputStream());
	}

	private static void notimplemented() throws IOException {
		throw new IOException("not implemented");
	}

	@Override
	protected File openProgram(String prog, String mode) throws IOException {
		notimplemented();
		return null;
	}

	@Override
	protected File tmpFile() throws IOException {
		notimplemented();
		return null;
	}

	private final class FileImpl extends File {
		private final StreamConnection conn;
		private final InputStream      is;
		private final OutputStream     os;
		private boolean                closed    = false;
		private boolean                nobuffer  = false;
		private int                    lookahead = -1;

		private FileImpl(StreamConnection conn, InputStream is, OutputStream os) {
			this.conn = conn;
			this.is = is;
			this.os = os;
		}

		private FileImpl(InputStream i) {
			this(null, i, null);
		}

		private FileImpl(OutputStream o) {
			this(null, null, o);
		}

		@Override
		public String tojstring() {
			return "file (" + this.hashCode() + ")";
		}

		@Override
		public boolean isstdfile() {
			return conn == null;
		}

		@Override
		public void close() throws IOException {
			closed = true;
			if (conn != null) {
				conn.close();
			}
		}

		@Override
		public void flush() throws IOException {
			if (os != null)
				os.flush();
		}

		@Override
		public void write(LuaString s) throws IOException {
			if (os != null)
				os.write(s.m_bytes, s.m_offset, s.m_length);
			else
				notimplemented();
			if (nobuffer)
				flush();
		}

		@Override
		public boolean isclosed() {
			return closed;
		}

		@Override
		public int seek(String option, int pos) throws IOException {
			/*
			if ( conn != null ) {
				if ( "set".equals(option) ) {
					conn.seek(pos);
					return (int) conn.getFilePointer();
				} else if ( "end".equals(option) ) {
					conn.seek(conn.length()+1+pos);
					return (int) conn.length()+1;
				} else {
					conn.seek(conn.getFilePointer()+pos);
					return (int) conn.getFilePointer();
				}
			}
			*/
			notimplemented();
			return 0;
		}

		@Override
		public void setvbuf(String mode, int size) {
			nobuffer = "no".equals(mode);
		}

		// get length remaining to read
		@Override
		public int remaining() throws IOException {
			return -1;
		}

		// peek ahead one character
		@Override
		public int peek() throws IOException {
			if (lookahead < 0)
				lookahead = is.read();
			return lookahead;
		}

		// return char if read, -1 if eof, throw IOException on other exception
		@Override
		public int read() throws IOException {
			if (lookahead >= 0) {
				int c = lookahead;
				lookahead = -1;
				return c;
			}
			if (is != null)
				return is.read();
			notimplemented();
			return 0;
		}

		// return number of bytes read if positive, -1 if eof, throws IOException
		@Override
		public int read(byte[] bytes, int offset, int length) throws IOException {
			int n, i = 0;
			if (is != null) {
				if (length > 0 && lookahead >= 0) {
					bytes[offset] = (byte) lookahead;
					lookahead = -1;
					i += 1;
				}
				for (; i < length;) {
					n = is.read(bytes, offset+i, length-i);
					if (n < 0)
						return i > 0? i: -1;
					i += n;
				}
			} else {
				notimplemented();
			}
			return length;
		}
	}
}
