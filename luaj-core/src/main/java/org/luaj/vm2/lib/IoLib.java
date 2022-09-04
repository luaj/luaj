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
package org.luaj.vm2.lib;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Abstract base class extending {@link LibFunction} which implements the core
 * of the lua standard {@code io} library.
 * <p>
 * It contains the implementation of the io library support that is common to
 * the JSE and JME platforms. In practice on of the concrete IOLib subclasses is
 * chosen: {@link org.luaj.vm2.lib.jse.JseIoLib} for the JSE platform, and
 * {@link org.luaj.vm2.lib.jme.JmeIoLib} for the JME platform.
 * <p>
 * The JSE implementation conforms almost completely to the C-based lua library,
 * while the JME implementation follows closely except in the area of
 * random-access files, which are difficult to support properly on JME.
 * <p>
 * Typically, this library is included as part of a call to either
 * {@link org.luaj.vm2.lib.jse.JsePlatform#standardGlobals()} or
 * {@link org.luaj.vm2.lib.jme.JmePlatform#standardGlobals()}
 *
 * <pre>
 * {
 * 	&#64;code
 * 	Globals globals = JsePlatform.standardGlobals();
 * 	globals.get("io").get("write").call(LuaValue.valueOf("hello, world\n"));
 * }
 * </pre>
 *
 * In this example the platform-specific {@link org.luaj.vm2.lib.jse.JseIoLib}
 * library will be loaded, which will include the base functionality provided by
 * this class, whereas the {@link org.luaj.vm2.lib.jse.JsePlatform} would load
 * the {@link org.luaj.vm2.lib.jse.JseIoLib}.
 * <p>
 * To instantiate and use it directly, link it into your globals table via
 * {@link LuaValue#load(LuaValue)} using code such as:
 *
 * <pre>
 * {
 * 	&#64;code
 * 	Globals globals = new Globals();
 * 	globals.load(new JseBaseLib());
 * 	globals.load(new PackageLib());
 * 	globals.load(new OsLib());
 * 	globals.get("io").get("write").call(LuaValue.valueOf("hello, world\n"));
 * }
 * </pre>
 * <p>
 * This has been implemented to match as closely as possible the behavior in the
 * corresponding library in C.
 *
 * @see LibFunction
 * @see org.luaj.vm2.lib.jse.JsePlatform
 * @see org.luaj.vm2.lib.jme.JmePlatform
 * @see org.luaj.vm2.lib.jse.JseIoLib
 * @see org.luaj.vm2.lib.jme.JmeIoLib
 * @see <a href=
 *      "http://www.lua.org/manual/5.1/manual.html#5.7">http://www.lua.org/manual/5.1/manual.html#5.7</a>
 */
public abstract class IoLib extends TwoArgFunction {

	protected abstract class File extends LuaValue {
		public abstract void write(LuaString string) throws IOException;

		public abstract void flush() throws IOException;

		public abstract boolean isstdfile();

		public abstract void close() throws IOException;

		public abstract boolean isclosed();

		// returns new position
		public abstract int seek(String option, int bytecount) throws IOException;

		public abstract void setvbuf(String mode, int size);

		// get length remaining to read
		public abstract int remaining() throws IOException;

		// peek ahead one character
		public abstract int peek() throws IOException, EOFException;

		// return char if read, -1 if eof, throw IOException on other exception
		public abstract int read() throws IOException, EOFException;

		// return number of bytes read if positive, false if eof, throw IOException on other exception
		public abstract int read(byte[] bytes, int offset, int length) throws IOException;

		public boolean eof() throws IOException {
			try {
				return peek() < 0;
			} catch (EOFException e) {
				return true;
			}
		}

		// delegate method access to file methods table
		@Override
		public LuaValue get(LuaValue key) {
			return filemethods.get(key);
		}

		// essentially a userdata instance
		@Override
		public int type() {
			return LuaValue.TUSERDATA;
		}

		@Override
		public String typename() {
			return "userdata";
		}

		// displays as "file" type
		@Override
		public String tojstring() {
			return "file: " + Integer.toHexString(hashCode());
		}

		@Override
		public void finalize() {
			if (!isclosed()) {
				try {
					close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	/** Enumerated value representing stdin */
	protected static final int FTYPE_STDIN  = 0;
	/** Enumerated value representing stdout */
	protected static final int FTYPE_STDOUT = 1;
	/** Enumerated value representing stderr */
	protected static final int FTYPE_STDERR = 2;
	/** Enumerated value representing a file type for a named file */
	protected static final int FTYPE_NAMED  = 3;

	/**
	 * Wrap the standard input.
	 *
	 * @return File
	 * @throws IOException
	 */
	abstract protected File wrapStdin() throws IOException;

	/**
	 * Wrap the standard output.
	 *
	 * @return File
	 * @throws IOException
	 */
	abstract protected File wrapStdout() throws IOException;

	/**
	 * Wrap the standard error output.
	 *
	 * @return File
	 * @throws IOException
	 */
	abstract protected File wrapStderr() throws IOException;

	/**
	 * Open a file in a particular mode.
	 *
	 * @param filename
	 * @param readMode   true if opening in read mode
	 * @param appendMode true if opening in append mode
	 * @param updateMode true if opening in update mode
	 * @param binaryMode true if opening in binary mode
	 * @return File object if successful
	 * @throws IOException if could not be opened
	 */
	abstract protected File openFile(String filename, boolean readMode, boolean appendMode, boolean updateMode,
		boolean binaryMode) throws IOException;

	/**
	 * Open a temporary file.
	 *
	 * @return File object if successful
	 * @throws IOException if could not be opened
	 */
	abstract protected File tmpFile() throws IOException;

	/**
	 * Start a new process and return a file for input or output
	 *
	 * @param prog the program to execute
	 * @param mode "r" to read, "w" to write
	 * @return File to read to or write from
	 * @throws IOException if an i/o exception occurs
	 */
	abstract protected File openProgram(String prog, String mode) throws IOException;

	private File infile  = null;
	private File outfile = null;
	private File errfile = null;

	private static final LuaValue STDIN       = valueOf("stdin");
	private static final LuaValue STDOUT      = valueOf("stdout");
	private static final LuaValue STDERR      = valueOf("stderr");
	private static final LuaValue FILE        = valueOf("file");
	private static final LuaValue CLOSED_FILE = valueOf("closed file");

	private static final int IO_CLOSE   = 0;
	private static final int IO_FLUSH   = 1;
	private static final int IO_INPUT   = 2;
	private static final int IO_LINES   = 3;
	private static final int IO_OPEN    = 4;
	private static final int IO_OUTPUT  = 5;
	private static final int IO_POPEN   = 6;
	private static final int IO_READ    = 7;
	private static final int IO_TMPFILE = 8;
	private static final int IO_TYPE    = 9;
	private static final int IO_WRITE   = 10;

	private static final int FILE_CLOSE   = 11;
	private static final int FILE_FLUSH   = 12;
	private static final int FILE_LINES   = 13;
	private static final int FILE_READ    = 14;
	private static final int FILE_SEEK    = 15;
	private static final int FILE_SETVBUF = 16;
	private static final int FILE_WRITE   = 17;

	private static final int IO_INDEX   = 18;
	private static final int LINES_ITER = 19;

	public static final String[] IO_NAMES = { "close", "flush", "input", "lines", "open", "output", "popen", "read",
			"tmpfile", "type", "write", };

	public static final String[] FILE_NAMES = { "close", "flush", "lines", "read", "seek", "setvbuf", "write", };

	LuaTable filemethods;

	protected Globals globals;

	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		globals = env.checkglobals();

		// io lib functions
		LuaTable t = new LuaTable();
		bind(t, IoLibV.class, IO_NAMES);

		// create file methods table
		filemethods = new LuaTable();
		bind(filemethods, IoLibV.class, FILE_NAMES, FILE_CLOSE);

		// set up file metatable
		LuaTable mt = new LuaTable();
		bind(mt, IoLibV.class, new String[] { "__index" }, IO_INDEX);
		t.setmetatable(mt);

		// all functions link to library instance
		setLibInstance(t);
		setLibInstance(filemethods);
		setLibInstance(mt);

		// return the table
		env.set("io", t);
		if (!env.get("package").isnil())
			env.get("package").get("loaded").set("io", t);
		return t;
	}

	private void setLibInstance(LuaTable t) {
		LuaValue[] k = t.keys();
		for (int i = 0, n = k.length; i < n; i++)
			((IoLibV) t.get(k[i])).iolib = this;
	}

	static final class IoLibV extends VarArgFunction {
		private File    f;
		public IoLib    iolib;
		private boolean toclose;
		private Varargs args;

		public IoLibV() {
		}

		public IoLibV(File f, String name, int opcode, IoLib iolib, boolean toclose, Varargs args) {
			this(f, name, opcode, iolib);
			this.toclose = toclose;
			this.args = args.dealias();
		}

		public IoLibV(File f, String name, int opcode, IoLib iolib) {
			this.f = f;
			this.name = name;
			this.opcode = opcode;
			this.iolib = iolib;
		}

		@Override
		public Varargs invoke(Varargs args) {
			try {
				switch (opcode) {
				case IO_FLUSH:
					return iolib._io_flush();
				case IO_TMPFILE:
					return iolib._io_tmpfile();
				case IO_CLOSE:
					return iolib._io_close(args.arg1());
				case IO_INPUT:
					return iolib._io_input(args.arg1());
				case IO_OUTPUT:
					return iolib._io_output(args.arg1());
				case IO_TYPE:
					return iolib._io_type(args.arg1());
				case IO_POPEN:
					return iolib._io_popen(args.checkjstring(1), args.optjstring(2, "r"));
				case IO_OPEN:
					return iolib._io_open(args.checkjstring(1), args.optjstring(2, "r"));
				case IO_LINES:
					return iolib._io_lines(args);
				case IO_READ:
					return iolib._io_read(args);
				case IO_WRITE:
					return iolib._io_write(args);

				case FILE_CLOSE:
					return iolib._file_close(args.arg1());
				case FILE_FLUSH:
					return iolib._file_flush(args.arg1());
				case FILE_SETVBUF:
					return iolib._file_setvbuf(args.arg1(), args.checkjstring(2), args.optint(3, 8192));
				case FILE_LINES:
					return iolib._file_lines(args);
				case FILE_READ:
					return iolib._file_read(args.arg1(), args.subargs(2));
				case FILE_SEEK:
					return iolib._file_seek(args.arg1(), args.optjstring(2, "cur"), args.optint(3, 0));
				case FILE_WRITE:
					return iolib._file_write(args.arg1(), args.subargs(2));

				case IO_INDEX:
					return iolib._io_index(args.arg(2));
				case LINES_ITER:
					return iolib._lines_iter(f, toclose, this.args);
				}
			} catch (IOException ioe) {
				if (opcode == LINES_ITER) {
					String s = ioe.getMessage();
					error(s != null? s: ioe.toString());
				}
				return errorresult(ioe);
			}
			return NONE;
		}
	}

	private File input() {
		return infile != null? infile: (infile = ioopenfile(FTYPE_STDIN, "-", "r"));
	}

	//	io.flush() -> bool
	public Varargs _io_flush() throws IOException {
		checkopen(output());
		outfile.flush();
		return LuaValue.TRUE;
	}

	//	io.tmpfile() -> file
	public Varargs _io_tmpfile() throws IOException {
		return tmpFile();
	}

	//	io.close([file]) -> void
	public Varargs _io_close(LuaValue file) throws IOException {
		File f = file.isnil()? output(): checkfile(file);
		checkopen(f);
		return ioclose(f);
	}

	//	io.input([file]) -> file
	public Varargs _io_input(LuaValue file) {
		infile = file.isnil()? input()
			: file.isstring()? ioopenfile(FTYPE_NAMED, file.checkjstring(), "r"): checkfile(file);
		return infile;
	}

	// io.output(filename) -> file
	public Varargs _io_output(LuaValue filename) {
		outfile = filename.isnil()? output()
			: filename.isstring()? ioopenfile(FTYPE_NAMED, filename.checkjstring(), "w"): checkfile(filename);
		return outfile;
	}

	//	io.type(obj) -> "file" | "closed file" | nil
	public Varargs _io_type(LuaValue obj) {
		File f = optfile(obj);
		return f != null? f.isclosed()? CLOSED_FILE: FILE: NIL;
	}

	// io.popen(prog, [mode]) -> file
	public Varargs _io_popen(String prog, String mode) throws IOException {
		if (!"r".equals(mode) && !"w".equals(mode))
			argerror(2, "invalid value: '" + mode + "'; must be one of 'r' or 'w'");
		return openProgram(prog, mode);
	}

	//	io.open(filename, [mode]) -> file | nil,err
	public Varargs _io_open(String filename, String mode) throws IOException {
		return rawopenfile(FTYPE_NAMED, filename, mode);
	}

	//	io.lines(filename, ...) -> iterator
	public Varargs _io_lines(Varargs args) {
		String filename = args.optjstring(1, null);
		File infile = filename == null? input(): ioopenfile(FTYPE_NAMED, filename, "r");
		checkopen(infile);
		return lines(infile, filename != null, args.subargs(2));
	}

	//	io.read(...) -> (...)
	public Varargs _io_read(Varargs args) throws IOException {
		checkopen(input());
		return ioread(infile, args);
	}

	//	io.write(...) -> void
	public Varargs _io_write(Varargs args) throws IOException {
		checkopen(output());
		return iowrite(outfile, args);
	}

	// file:close() -> void
	public Varargs _file_close(LuaValue file) throws IOException {
		return ioclose(checkfile(file));
	}

	// file:flush() -> void
	public Varargs _file_flush(LuaValue file) throws IOException {
		checkfile(file).flush();
		return LuaValue.TRUE;
	}

	// file:setvbuf(mode,[size]) -> void
	public Varargs _file_setvbuf(LuaValue file, String mode, int size) {
		if ("no".equals(mode)) {
		} else if ("full".equals(mode)) {
		} else if ("line".equals(mode)) {
		} else {
			argerror(1, "invalid value: '" + mode + "'; must be one of 'no', 'full' or 'line'");
		}
		checkfile(file).setvbuf(mode, size);
		return LuaValue.TRUE;
	}

	// file:lines(...) -> iterator
	public Varargs _file_lines(Varargs args) {
		return lines(checkfile(args.arg1()), false, args.subargs(2));
	}

	//	file:read(...) -> (...)
	public Varargs _file_read(LuaValue file, Varargs subargs) throws IOException {
		return ioread(checkfile(file), subargs);
	}

	//  file:seek([whence][,offset]) -> pos | nil,error
	public Varargs _file_seek(LuaValue file, String whence, int offset) throws IOException {
		if ("set".equals(whence)) {
		} else if ("end".equals(whence)) {
		} else if ("cur".equals(whence)) {
		} else {
			argerror(1, "invalid value: '" + whence + "'; must be one of 'set', 'cur' or 'end'");
		}
		return valueOf(checkfile(file).seek(whence, offset));
	}

	//	file:write(...) -> void
	public Varargs _file_write(LuaValue file, Varargs subargs) throws IOException {
		return iowrite(checkfile(file), subargs);
	}

	// __index, returns a field
	public Varargs _io_index(LuaValue v) {
		return v.equals(STDOUT)? output(): v.equals(STDIN)? input(): v.equals(STDERR)? errput(): NIL;
	}

	//	lines iterator(s,var) -> var'
	public Varargs _lines_iter(LuaValue file, boolean toclose, Varargs args) throws IOException {
		File f = optfile(file);
		if (f == null)
			argerror(1, "not a file: " + file);
		if (f.isclosed())
			error("file is already closed");
		Varargs ret = ioread(f, args);
		if (toclose && ret.isnil(1) && f.eof())
			f.close();
		return ret;
	}

	private File output() {
		return outfile != null? outfile: (outfile = ioopenfile(FTYPE_STDOUT, "-", "w"));
	}

	private File errput() {
		return errfile != null? errfile: (errfile = ioopenfile(FTYPE_STDERR, "-", "w"));
	}

	private File ioopenfile(int filetype, String filename, String mode) {
		try {
			return rawopenfile(filetype, filename, mode);
		} catch (Exception e) {
			error("io error: " + e.getMessage());
			return null;
		}
	}

	private static Varargs ioclose(File f) throws IOException {
		if (f.isstdfile())
			return errorresult("cannot close standard file");
		else {
			f.close();
			return successresult();
		}
	}

	private static Varargs successresult() {
		return LuaValue.TRUE;
	}

	static Varargs errorresult(Exception ioe) {
		String s = ioe.getMessage();
		return errorresult("io error: " + (s != null? s: ioe.toString()));
	}

	private static Varargs errorresult(String errortext) {
		return varargsOf(NIL, valueOf(errortext));
	}

	private Varargs lines(final File f, boolean toclose, Varargs args) {
		try {
			return new IoLibV(f, "lnext", LINES_ITER, this, toclose, args);
		} catch (Exception e) {
			return error("lines: " + e);
		}
	}

	private static Varargs iowrite(File f, Varargs args) throws IOException {
		for (int i = 1, n = args.narg(); i <= n; i++)
			f.write(args.checkstring(i));
		return f;
	}

	private Varargs ioread(File f, Varargs args) throws IOException {
		int i, n = args.narg();
		if (n == 0)
			return freadline(f, false);
		LuaValue[] v = new LuaValue[n];
		LuaValue ai, vi;
		LuaString fmt;
		for (i = 0; i < n;) {
			item: switch ((ai = args.arg(i+1)).type()) {
			case LuaValue.TNUMBER:
				vi = freadbytes(f, ai.toint());
				break item;
			case LuaValue.TSTRING:
				fmt = ai.checkstring();
				if (fmt.m_length >= 2 && fmt.m_bytes[fmt.m_offset] == '*') {
					switch (fmt.m_bytes[fmt.m_offset+1]) {
					case 'n':
						vi = freadnumber(f);
						break item;
					case 'l':
						vi = freadline(f, false);
						break item;
					case 'L':
						vi = freadline(f, true);
						break item;
					case 'a':
						vi = freadall(f);
						break item;
					}
				}
			default:
				return argerror(i+1, "(invalid format)");
			}
			if ((v[i++] = vi).isnil())
				break;
		}
		return i == 0? NIL: varargsOf(v, 0, i);
	}

	private static File checkfile(LuaValue val) {
		File f = optfile(val);
		if (f == null)
			argerror(1, "file");
		checkopen(f);
		return f;
	}

	private static File optfile(LuaValue val) {
		return (val instanceof File)? (File) val: null;
	}

	private static File checkopen(File file) {
		if (file.isclosed())
			error("attempt to use a closed file");
		return file;
	}

	private File rawopenfile(int filetype, String filename, String mode) throws IOException {
		int len = mode.length();
		for (int i = 0; i < len; i++) { // [rwa][+]?b*
			char ch = mode.charAt(i);
			if ((i == 0 && "rwa".indexOf(ch) >= 0) || (i == 1 && ch == '+'))
				continue;
			if (i >= 1 && ch == 'b')
				continue;
			len = -1;
			break;
		}
		if (len <= 0)
			argerror(2, "invalid mode: '" + mode + "'");

		switch (filetype) {
		case FTYPE_STDIN:
			return wrapStdin();
		case FTYPE_STDOUT:
			return wrapStdout();
		case FTYPE_STDERR:
			return wrapStderr();
		}
		boolean isreadmode = mode.startsWith("r");
		boolean isappend = mode.startsWith("a");
		boolean isupdate = mode.indexOf('+') > 0;
		boolean isbinary = mode.endsWith("b");
		return openFile(filename, isreadmode, isappend, isupdate, isbinary);
	}

	// ------------- file reading utilitied ------------------

	public static LuaValue freadbytes(File f, int count) throws IOException {
		if (count == 0)
			return f.eof()? NIL: EMPTYSTRING;
		byte[] b = new byte[count];
		int r;
		if ((r = f.read(b, 0, b.length)) < 0)
			return NIL;
		return LuaString.valueUsing(b, 0, r);
	}

	public static LuaValue freaduntil(File f, boolean lineonly, boolean withend) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int c;
		try {
			if (lineonly) {
				loop: while ( (c = f.read()) >= 0 ) {
					switch (c) {
					case '\r':
						if (withend)
							baos.write(c);
						break;
					case '\n':
						if (withend)
							baos.write(c);
						break loop;
					default:
						baos.write(c);
						break;
					}
				}
			} else {
				while ( (c = f.read()) >= 0 )
					baos.write(c);
			}
		} catch (EOFException e) {
			c = -1;
		}
		return (c < 0 && baos.size() == 0)? (LuaValue) NIL: (LuaValue) LuaString.valueUsing(baos.toByteArray());
	}

	public static LuaValue freadline(File f, boolean withend) throws IOException {
		return freaduntil(f, true, withend);
	}

	public static LuaValue freadall(File f) throws IOException {
		int n = f.remaining();
		if (n >= 0) {
			return n == 0? EMPTYSTRING: freadbytes(f, n);
		} else {
			return freaduntil(f, false, false);
		}
	}

	public static LuaValue freadnumber(File f) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		freadchars(f, " \t\r\n", null);
		freadchars(f, "-+", baos);
		//freadchars(f,"0",baos);
		//freadchars(f,"xX",baos);
		freadchars(f, "0123456789", baos);
		freadchars(f, ".", baos);
		freadchars(f, "0123456789", baos);
		//freadchars(f,"eEfFgG",baos);
		// freadchars(f,"+-",baos);
		//freadchars(f,"0123456789",baos);
		String s = baos.toString();
		return s.length() > 0? valueOf(Double.parseDouble(s)): NIL;
	}

	private static void freadchars(File f, String chars, ByteArrayOutputStream baos) throws IOException {
		int c;
		while ( true ) {
			c = f.peek();
			if (chars.indexOf(c) < 0) {
				return;
			}
			f.read();
			if (baos != null)
				baos.write(c);
		}
	}

}
