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
package org.luaj.vm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;



/**
 * A String implementation for Lua using bytes instead of chars.
 * 
 * This should have the following advantages:
 * 
 * (1) We can use strings as byte buffers, as Lua does, and therefore avoid
 * questions about how to adapt Lua APIs that use strings with binary data.
 * 
 * (2) Half the memory usage when strings are primarily ASCII
 * 
 * 
 * TODO: Decide if/when to copy the bytes to a new array to ensure memory does
 * not "leak" in the form of unused portions of byte arrays. Currently, for
 * efficiency, new LStrings and substrings never create copies.
 */
public class LString extends LValue {
	
	public final byte[] m_bytes;
	public final int m_offset;
	public final int m_length;
	public final int m_hash;
	
	private static LTable s_stringMT;
	
	public static final LString[] LTYPENAMES;
	
	static {
		int n = Lua.TYPE_NAMES.length;
		LTYPENAMES = new LString[n];
		for ( int i=0; i<n; i++ )
			LTYPENAMES[i] = new LString(Lua.TYPE_NAMES[i]);
	}
	
	/**
	 * Construct a Lua string from the given Java string. 
	 * Characters are encoded using UTF-8.
	 */
	public LString(String string) {
		
		// measure bytes required to encode
		int n = string.length();
		int b = n;
		char c;
		for ( int i=0; i<n; i++ ) {
			if ( (c = string.charAt(i)) >= 0x80 ) {
				++b;
				if ( c >= 0x800 )
					++b;
			}
		}		
		byte[] bytes = new byte[b];
		int j = 0;
		for ( int i=0; i<n; i++ ) {
			if ( (c = string.charAt(i)) < 0x80 ) {
				bytes[j++] = (byte) c;
			} else if ( c < 0x800 ) {
				bytes[j++] = (byte) (0xC0 | ((c>>6)  & 0x1f));
				bytes[j++] = (byte) (0x80 | ( c      & 0x3f));				
			} else {
				bytes[j++] = (byte) (0xE0 | ((c>>12) & 0x0f));
				bytes[j++] = (byte) (0x80 | ((c>>6)  & 0x3f));
				bytes[j++] = (byte) (0x80 | ( c      & 0x3f));				
			}
		}
		this.m_bytes = bytes;
		this.m_offset = 0;
		this.m_length = b;
		this.m_hash = hashBytes( bytes, 0, b );
	}

	/**
	 * Convert to Java string using UTF-8 encoding
	 */
	public String toJavaString() {
		char[] c = new char[m_length];
		int n=0, p=0;
		int b;
		for ( int i=0; i<m_length; i++ ) {
			switch ( (b = m_bytes[m_offset+i]) & 0xe0 ) {
			default:
				if ( b == 0 )
					return new String( c, 0, n );
				c[p=n++] = (char) (0xff & b);
				break;
			case 0x80:
			case 0xa0:
				c[p] = (char) ((c[p] << 6) | ( b & 0x3f ));
				break;
			case 0xc0:
				c[p=n++] = (char) (b & 0x1f);
				break;
			case 0xe0:
				c[p=n++] = (char) (b & 0xf);
				break;
			}			
		}
		return new String( c, 0, n );
	}
	
	/**
	 * Construct a string from the given byte array.
	 * 
	 * new LString(b) is identical to new LString(b, 0, b.length)
	 */
	public LString(byte[] bytes) {
		this( bytes, 0, bytes.length );
	}
	
	/**
	 * Construct a string from the given byte array and range. For efficiency,
	 * the byte array is not copied. Lua strings are immutable so the bytes must
	 * not be modified after the string is constructed.
	 */
	public LString(byte[] bytes, int off, int len) {
		if ( off < 0 || len < 0 || off+len > bytes.length )
			throw new IndexOutOfBoundsException();
		this.m_bytes = bytes;
		this.m_offset = off;
		this.m_length = len;
		this.m_hash = hashBytes( bytes, off, len );
	}
	
	public static LString newStringCopy(LString src) {
		return newStringCopy( src.m_bytes, src.m_offset, src.m_length );
	}
	
	public static LString newStringCopy(byte[] buf, int off, int len) {
		byte[] b = new byte[len];
		System.arraycopy( buf, off, b, 0, len );
		return new LString( b, 0, len );
	}
	
	public static LString newStringNoCopy(byte[] buf, int off, int len) {
		return new LString( buf, off, len );
	}
	
	public boolean equals(Object o) {
		if ( o != null && o instanceof LString ) {
			LString s = (LString) o;
			return m_hash == s.m_hash &&
					m_length == s.m_length &&
					( ( m_bytes == s.m_bytes && m_offset == s.m_offset ) ||
					  equals( m_bytes, m_offset, s.m_bytes, s.m_offset, m_length ) );
		}
		return false;
	}
	
	public int compareTo( LString o ) {
		final byte[] a = this.m_bytes;
		final byte[] b = o.m_bytes;
		int i = this.m_offset;
		int j = o.m_offset;
		final int imax = i + m_length;
		final int jmax = j + o.m_length;
		
		if ( a == b && i == j && imax == jmax )
			return 0;
		
		while ( i < imax && j < jmax ) {
			if ( a[i] != b[i] ) {
				return ( ( (int)a[i] ) & 0x0FF ) - ( ( (int)b[j] ) & 0x0FF );
			}
			i++;
			j++;
		}
		
		return m_length - o.m_length;
	}
	
	public int hashCode() {
		return m_hash;
	}

	public int length() {
		return m_length;
	}
	
	public LString substring( int beginIndex, int endIndex ) {
		return new LString( m_bytes, m_offset + beginIndex, endIndex - beginIndex );
	}
	
	public int charAt( int index ) {
		if ( index < 0 || index >= m_length )
			throw new IndexOutOfBoundsException();
		return luaByte( index );
	}
	
	/** Java version of strpbrk, which is a terribly named C function. */
	public int indexOfAny( LString accept ) {
		final int ilimit = m_offset + m_length;
		final int jlimit = accept.m_offset + accept.m_length;
		for ( int i = m_offset; i < ilimit; ++i ) {
			for ( int j = accept.m_offset; j < jlimit; ++j ) {
				if ( m_bytes[i] == accept.m_bytes[j] ) {
					return i - m_offset;
				}
			}
		}
		return -1;
	}
	
	public int indexOf( LString s, int start ) {
		final int slen = s.length();
		final int limit = m_offset + m_length - slen;
		for ( int i = m_offset + start; i <= limit; ++i ) {
			if ( equals( m_bytes, i, s.m_bytes, s.m_offset, slen ) ) {
				return i;
			}
		}
		return -1;
	}
	
	public int lastIndexOf( LString s ) {
		final int slen = s.length();
		final int limit = m_offset + m_length - slen;
		for ( int i = limit; i >= m_offset; --i ) {
			if ( equals( m_bytes, i, s.m_bytes, s.m_offset, slen ) ) {
				return i;
			}
		}
		return -1;
	}
	
	public static LString valueOf( double d ) {
		return new LString( String.valueOf( d ) );
	}
	
	public static LString valueOf( int x ) {
		return new LString( String.valueOf( x ) );
	}
	
	public static LString valueOf(String s) {
		return new LString( s );
	}
	
	/**
	 * Write the specified substring of this string to the given output stream.
	 */
	public void write( OutputStream os, int offset, int len ) throws IOException {
		if ( offset < 0 || len < 0 )
			throw new IndexOutOfBoundsException();
		if ( offset + len > m_length )
			throw new IndexOutOfBoundsException();

		os.write( m_bytes, m_offset+offset, len );
	}

	public void write(OutputStream os) throws IOException {
		write(os, 0, m_length);
	}
	
	/**
	 * Copy the bytes of the string into the given byte array.
	 */
	public void copyInto( int strOffset, byte[] bytes, int arrayOffset, int len ) {
		System.arraycopy( m_bytes, m_offset+strOffset, bytes, arrayOffset, len );
	}
	
	/**
	 * Produce an InputStream instance from which the bytes of this LString can be read.
	 * Underlying byte array is not copied.
	 */
	public ByteArrayInputStream toInputStream() {
		// Well, this is really something.
		// Javadoc for java versions 1.3 and earlier states that if reset() is
		// called on a ByteArrayInputStream constructed with the 3-argument
		// constructor, then bytes 0 .. offset will be returned by the next
		// calls to read(). In JDK 1.4, the behavior improved, so that the
		// initial mark is set to the initial offset. We still need to
		// override ByteArrayInputStream here just in case we run on a
		// JVM with the older behavior.
		return new ByteArrayInputStream( m_bytes, m_offset, m_length ) {
			public synchronized void reset() {
				pos = Math.max( m_offset, mark );
			}
		};
	}
	
	public boolean luaBinCmpUnknown(int opcode, LValue lhs) {
		return lhs.luaBinCmpString(opcode, this);
	}

	public boolean luaBinCmpString(int opcode, LString rhs) {
		switch ( opcode ) {
		case Lua.OP_EQ: return equals(rhs);
		case Lua.OP_LT: return compareTo(rhs) < 0;
		case Lua.OP_LE: return compareTo(rhs) <= 0;
		}
		LuaState.vmerror( "bad cmp opcode" );
		return false;
	}
	
	public LValue luaBinOpDouble( int opcode, double m_value ) {
		return luaToNumber().luaBinOpDouble( opcode, m_value );
	}
	
	public LValue luaBinOpInteger( int opcode, int m_value ) {
		return luaToNumber().luaBinOpInteger( opcode, m_value );
	}
	
	public LValue luaBinOpUnknown( int opcode, LValue lhs ) {
		return luaToNumber().luaBinOpUnknown( opcode, lhs );
	}
	
	public LValue luaUnaryMinus() {
		return luaToNumber().luaUnaryMinus();
	}
	
	public LValue luaToNumber() {
		return luaToNumber( 10 );
	}
	
	public LValue luaToNumber( int base ) {
		if ( base >= 2 && base <= 36 ) {
			String str = toJavaString().trim();
			try {
				return LInteger.valueOf( Integer.parseInt( str, base ) );
			} catch ( NumberFormatException nfe ) {
				if ( base == 10 ) {
					try {
						return new LDouble( Double.parseDouble( str ) );
					} catch ( NumberFormatException nfe2 ) {
					}
				}
			}
		}
		
		return LNil.NIL;
	}
	
	public LString luaAsString() {
		return this;
	}
	
	/** Built-in opcode LEN, for Strings and Tables */
	public int luaLength() {
		return m_length;
	}

	public int luaGetType() {
		return Lua.LUA_TSTRING;
	}
	
	public LTable luaGetMetatable() {
		synchronized ( LString.class ) {
			return s_stringMT;
		}
	}
	
	/**
	 * Get the metatable for all string values. Creates the table if it does not
	 * exist yet, and sets its __index entry to point to itself.
	 * 
	 * @return metatable that will be used for all strings
	 */
	public static synchronized LTable getMetatable() {
		if ( s_stringMT == null ) {
			s_stringMT = new LTable();
			s_stringMT.put( TM_INDEX, s_stringMT );
		}
		return s_stringMT;
	}
	
	public static boolean equals( LString a, int i, LString b, int j, int n ) {
		return equals( a.m_bytes, a.m_offset + i, b.m_bytes, b.m_offset + j, n );
	}
	
	public static boolean equals( byte[] a, int i, byte[] b, int j, int n ) {
		if ( a.length < i + n || b.length < j + n )
			return false;
		final int imax = i + n;
		final int jmax = j + n;
		while ( i < imax && j < jmax ) {
			if ( a[i++] != b[j++] )
				return false;
		}
		return true;
	}
	
	private static int hashBytes( byte[] bytes, int offset, int length ) {
		// Compute the hash of the given bytes.
		// This code comes right out of Lua 5.1.2 (translated from C to Java)
		int h = length;  /* seed */
		int step = (length>>5)+1;  /* if string is too long, don't hash all its chars */
		for (int l1=length; l1>=step; l1-=step)  /* compute hash */
		    h = h ^ ((h<<5)+(h>>2)+(((int) bytes[offset+l1-1] ) & 0x0FF ));
		return h;
	}
	
	public int luaByte(int index) {
		return m_bytes[m_offset + index] & 0x0FF;
	}

	public void luaConcatTo(ByteArrayOutputStream baos) {
		baos.write( m_bytes, m_offset, m_length );
	}

}
