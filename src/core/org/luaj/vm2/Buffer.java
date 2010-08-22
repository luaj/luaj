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
package org.luaj.vm2;


/**
 * String buffer for use in string library methods, optimized for production 
 * of StrValue instances. 
 */
public final class Buffer {
	private static final int DEFAULT_CAPACITY = 64;
	private static final byte[] NOBYTES = {};

	private byte[] bytes;
	private int length;
	private int offset;
	private LuaValue value;
	
	public Buffer() {
		this(DEFAULT_CAPACITY);
	}
	
	public Buffer( int initialCapacity ) {
		bytes = new byte[ initialCapacity ];
		length = 0;
		offset = 0;
		value = null;
	}
	
	public Buffer(LuaValue value) {
		bytes = NOBYTES;
		length = offset = 0;
		this.value = value;
	}
	
	public LuaValue value() {
		return value != null? value: this.tostring();
	}

	public Buffer setvalue(LuaValue value) {
		bytes = NOBYTES;
		offset = length = 0;
		this.value = value;
		return this;
	}
	
	public final LuaString tostring() {
		realloc( length, 0 );
		return LuaString.valueOf( bytes, offset, length );
	}
	
	public String tojstring() {
		return value().tojstring();
	}
	
	public String toString() {
		return tojstring();
	}

	public final Buffer append( byte b ) {
		makeroom( 0, 1 );
		bytes[ offset + length++ ] = b;
		return this;
	}

	public final Buffer append( LuaValue val ) {
		append( val.strvalue() );
		return this;
	}
	
	public final Buffer append( LuaString str ) {
		final int n = str.m_length;
		makeroom( 0, n );
		str.copyInto( 0, bytes, offset + length, n );
		length += n;
		return this;
	}
	
	public final Buffer append( String str ) {
		char[] chars = str.toCharArray();
		final int n = LuaString.lengthAsUtf8( chars );
		makeroom( 0, n );
		LuaString.encodeToUtf8( chars, bytes, offset + length );
		length += n;
		return this;
	}

	public Buffer prepend(LuaString s) {
		int n = s.m_length;
		makeroom( n, 0 );
		System.arraycopy( s.m_bytes, s.m_offset, bytes, offset-n, n );
		offset -= n;
		length += n;
		value = null;
		return this;
	}
	
	public final void makeroom( int nbefore, int nafter ) {
		if ( value != null ) {
			LuaString s = value.strvalue();
			value = null;
			bytes = new byte[nbefore+s.m_length+nafter];
			length = s.m_length;
			offset = nbefore;
			System.arraycopy(s.m_bytes, s.m_offset, bytes, offset, length);
		} else if ( offset+length+nafter > bytes.length || offset<nbefore ) {
			realloc( Math.max(nbefore+length+nafter,length*2), nbefore );
		}
	}
	
	private final void realloc( int newSize, int newOffset ) {
		if ( newSize != bytes.length ) {
			byte[] newBytes = new byte[ newSize ];
			System.arraycopy( bytes, offset, newBytes, newOffset, length );
			bytes = newBytes;
			offset = newOffset;
		}
	}

}
