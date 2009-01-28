/*******************************************************************************
* Copyright (c) 2007 LuaJ. All rights reserved.
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
package org.luaj.compiler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.luaj.vm.LNumber;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LString;
import org.luaj.vm.LValue;
import org.luaj.vm.LocVars;
import org.luaj.vm.Lua;


public class DumpState {

	/** mark for precompiled code (`<esc>Lua') */
	public static final String LUA_SIGNATURE	= "\033Lua";

	/** for header of binary files -- this is Lua 5.1 */
	public static final int LUAC_VERSION		= 0x51;

	/** for header of binary files -- this is the official format */
	public static final int LUAC_FORMAT		= 0;

	/** size of header of binary files */
	public static final int LUAC_HEADERSIZE		= 12;

	/** expected lua header bytes */
	private static final byte[] LUAC_HEADER_SIGNATURE = { '\033', 'L', 'u', 'a' };

	/** set true to allow integer compilation */
	public static boolean ALLOW_INTEGER_CASTING = false;
	
	/** format corresponding to non-number-patched lua, all numbers are floats or doubles */
	public static final int NUMBER_FORMAT_FLOATS_OR_DOUBLES    = 0;

	/** format corresponding to non-number-patched lua, all numbers are ints */
	public static final int NUMBER_FORMAT_INTS_ONLY            = 1;
	
	/** format corresponding to number-patched lua, all numbers are 32-bit (4 byte) ints */
	public static final int NUMBER_FORMAT_NUM_PATCH_INT32      = 4;
	
	/** default number format */
	public static final int NUMBER_FORMAT_DEFAULT = NUMBER_FORMAT_FLOATS_OR_DOUBLES;

	// header fields
	private boolean IS_LITTLE_ENDIAN = false;
	private int NUMBER_FORMAT = NUMBER_FORMAT_DEFAULT;
	private int SIZEOF_LUA_NUMBER = 8;
	private static final int SIZEOF_INT = 4;
	private static final int SIZEOF_SIZET = 4;
	private static final int SIZEOF_INSTRUCTION = 4;

	DataOutputStream writer;
	boolean strip;
	int status;

	public DumpState(OutputStream w, boolean strip) {
		this.writer = new DataOutputStream( w );
		this.strip = strip;
		this.status = 0;
	}

	void dumpBlock(final byte[] b, int size) throws IOException {
		writer.write(b, 0, size);
	}

	void dumpChar(int b) throws IOException {
		writer.write( b );
	}

	void dumpInt(int x) throws IOException {
		if ( IS_LITTLE_ENDIAN ) {
			writer.writeByte(x&0xff);
			writer.writeByte((x>>8)&0xff);
			writer.writeByte((x>>16)&0xff);
			writer.writeByte((x>>24)&0xff);
		} else {
			writer.writeInt(x);
		}
	}
	
	void dumpString(LString s) throws IOException {
		final int len = s.length();
		dumpInt( len+1 );
		s.write( writer, 0, len );
		writer.write( 0 );
	}
	
	void dumpDouble(double d) throws IOException {
		long l = Double.doubleToLongBits(d);
		if ( IS_LITTLE_ENDIAN ) {
			dumpInt( (int) l );
			dumpInt( (int) (l>>32) );
		} else {
			writer.writeLong(l);
		}
	}

	void dumpCode( final LPrototype f ) throws IOException {
		final int[] code = f.code;
		int n = code.length;
		dumpInt( n );
		for ( int i=0; i<n; i++ )
			dumpInt( code[i] );
	}
	
	void dumpConstants(final LPrototype f) throws IOException {
		final LValue[] k = f.k;
		int i, n = k.length;
		dumpInt(n);
		for (i = 0; i < n; i++) {
			final LValue o = k[i];
			switch ( o.luaGetType() ) {
			case Lua.LUA_TNIL:
				writer.write(Lua.LUA_TNIL);
				break;
			case Lua.LUA_TBOOLEAN:
				writer.write(Lua.LUA_TBOOLEAN);
				dumpChar(o.toJavaBoolean() ? 1 : 0);
				break;
			case Lua.LUA_TNUMBER:
				switch (NUMBER_FORMAT) {
				case NUMBER_FORMAT_FLOATS_OR_DOUBLES:
					writer.write(Lua.LUA_TNUMBER);
					dumpDouble(o.toJavaDouble());
					break;
				case NUMBER_FORMAT_INTS_ONLY:
					if ( ! ALLOW_INTEGER_CASTING && ! o.isInteger() )
						throw new java.lang.IllegalArgumentException("not an integer: "+o);
					writer.write(Lua.LUA_TNUMBER);
					dumpInt(o.toJavaInt());
					break;
				case NUMBER_FORMAT_NUM_PATCH_INT32:
					if ( o.isInteger() ) {
						writer.write(Lua.LUA_TINT);
						dumpInt(o.toJavaInt());
					} else {
						writer.write(Lua.LUA_TNUMBER);
						dumpDouble(o.toJavaDouble());
					}
					break;
				default:
					throw new IllegalArgumentException("number format not supported: "+NUMBER_FORMAT);
				}
				break;
			case Lua.LUA_TSTRING:
				writer.write(Lua.LUA_TSTRING);
				dumpString(o.luaAsString());
				break;
			default:
				throw new IllegalArgumentException("bad type for " + o);			
			}
		}
		n = f.p.length;
		dumpInt(n);
		for (i = 0; i < n; i++)
			dumpFunction(f.p[i], f.source);
	}
	
	void dumpDebug(final LPrototype f) throws IOException {
		int i, n;
		n = (strip) ? 0 : f.lineinfo.length;
		dumpInt(n);
		for (i = 0; i < n; i++)
			dumpInt(f.lineinfo[i]);
		n = (strip) ? 0 : f.locvars.length;
		dumpInt(n);
		for (i = 0; i < n; i++) {
			LocVars lvi = f.locvars[i];
			dumpString(lvi.varname);
			dumpInt(lvi.startpc);
			dumpInt(lvi.endpc);
		}
		n = (strip) ? 0 : f.upvalues.length;
		dumpInt(n);
		for (i = 0; i < n; i++)
			dumpString(f.upvalues[i]);
	}
	
	void dumpFunction(final LPrototype f, final LString string) throws IOException {
		if ( f.source == null || f.source.equals(string) || strip )
			dumpInt(0);
		else
			dumpString(f.source);
		dumpInt(f.linedefined);
		dumpInt(f.lastlinedefined);
		dumpChar(f.nups);
		dumpChar(f.numparams);
		dumpChar(f.is_vararg);
		dumpChar(f.maxstacksize);
		dumpCode(f);
		dumpConstants(f);
		dumpDebug(f);
	}

	void dumpHeader() throws IOException {
		writer.write( LUAC_HEADER_SIGNATURE );
		writer.write( LUAC_VERSION );
		writer.write( LUAC_FORMAT );
		writer.write( IS_LITTLE_ENDIAN? 1: 0 );
		writer.write( SIZEOF_INT );
		writer.write( SIZEOF_SIZET );
		writer.write( SIZEOF_INSTRUCTION );
		writer.write( SIZEOF_LUA_NUMBER );
		writer.write( NUMBER_FORMAT );
	}

	/*
	** dump Lua function as precompiled chunk
	*/
	public static int dump( LPrototype f, OutputStream w, boolean strip ) throws IOException {
		DumpState D = new DumpState(w,strip);
		D.dumpHeader();
		D.dumpFunction(f,null);
		return D.status;
	}

	/**
	 * 
	 * @param f the function to dump
	 * @param w the output stream to dump to
	 * @param stripDebug true to strip debugging info, false otherwise
	 * @param numberFormat one of NUMBER_FORMAT_FLOATS_OR_DOUBLES, NUMBER_FORMAT_INTS_ONLY, NUMBER_FORMAT_NUM_PATCH_INT32
	 * @param littleendian true to use little endian for numbers, false for big endian
	 * @return 0 if dump succeeds
	 * @throws IOException
	 * @throws IllegalArgumentException if the number format it not supported
	 */
	public static int dump(LPrototype f, OutputStream w, boolean stripDebug, int numberFormat, boolean littleendian) throws IOException {
		switch ( numberFormat ) {
		case NUMBER_FORMAT_FLOATS_OR_DOUBLES:
		case NUMBER_FORMAT_INTS_ONLY:
		case NUMBER_FORMAT_NUM_PATCH_INT32:
			break;
		default:
			throw new IllegalArgumentException("number format not supported: "+numberFormat);
		}
		DumpState D = new DumpState(w,stripDebug);
		D.IS_LITTLE_ENDIAN = littleendian;
		D.NUMBER_FORMAT = numberFormat;
		D.SIZEOF_LUA_NUMBER = (numberFormat==NUMBER_FORMAT_INTS_ONLY? 4: 8);
		D.dumpHeader();
		D.dumpFunction(f,null);
		return D.status;
	}
}
