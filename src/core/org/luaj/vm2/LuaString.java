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

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.luaj.vm2.lib.MathLib;
import org.luaj.vm2.lib.StringLib;

public class LuaString extends LuaValue {

	public static LuaValue s_metatable;

	public final byte[] m_bytes;
	public final int    m_offset;
	public final int    m_length;

	public static LuaString valueOf(String string) {
		char[] c = string.toCharArray();
		byte[] b = new byte[lengthAsUtf8(c)];
		encodeToUtf8(c, b, 0);
		return new LuaString(b);
	}

	public LuaString(byte[] bytes, int offset, int length) {
		this.m_bytes = bytes;
		this.m_offset = offset;
		this.m_length = length;
	}
	
	public LuaString(byte[] bytes) {
		this.m_bytes = bytes;
		this.m_offset = 0;
		this.m_length = bytes.length;
	}

	public static LuaString valueOf(char[] bytes) {
		int n = bytes.length;
		byte[] b = new byte[n];
		for ( int i=0; i<n; i++ )
			b[i] = (byte) bytes[i];
		return new LuaString(b, 0, n);
	}
	
	public boolean isstring() {
		return true; 
	}
		
	public LuaValue getmetatable() { 
		return s_metatable; 
	}
	
	public int type() {
		return LuaValue.TSTRING;
	}

	public String typename() {
		return "string";
	}
	
	public String toString() {
		return decodeAsUtf8(m_bytes, m_offset, m_length);
	}

	// get is delegated to the string library
	public LuaValue get(LuaValue key) {
		return s_metatable!=null? gettable(this,key): StringLib.instance.get(key);
	}

	// unary operators
	public LuaValue neg() { return checkarith().neg(); }

	// basic binary arithmetic
	public LuaValue   add( LuaValue rhs )      { return checkarith().add(rhs); }
	public LuaValue   add( double lhs )   { return checkarith().add(lhs); }
	public LuaValue   sub( LuaValue rhs )      { return checkarith().sub(rhs); }
	public LuaValue   subFrom( double lhs ) { return checkarith().subFrom(lhs); }
	public LuaValue   mul( LuaValue rhs )      { return checkarith().mul(rhs); }
	public LuaValue   mul( double lhs ) { return checkarith().mul(lhs); }
	public LuaValue   mul( int lhs )    { return checkarith().mul(lhs); }
	public LuaValue   pow( LuaValue rhs )      { return checkarith().pow(rhs); }
	public LuaValue   powWith( double lhs ) { return checkarith().powWith(lhs); }
	public LuaValue   powWith( int lhs )    { return checkarith().powWith(lhs); }
	public LuaValue   div( LuaValue rhs )      { return checkarith().div(rhs); }
	public LuaValue   divInto( double lhs ) { return checkarith().divInto(lhs); }
	public LuaValue   mod( LuaValue rhs )      { return checkarith().mod(rhs); }
	public LuaValue   modFrom( double lhs ) { return checkarith().modFrom(lhs); }

	// relational operators, these only work with other strings
	public LuaValue   lt( LuaValue rhs )         { return rhs.strcmp(this)>0? LuaValue.TRUE: FALSE; }
	public boolean lt_b( LuaValue rhs )       { return rhs.strcmp(this)>0; }
	public boolean lt_b( int rhs )         { typerror("attempt to compare string with number"); return false; }
	public boolean lt_b( double rhs )      { typerror("attempt to compare string with number"); return false; }
	public LuaValue   lteq( LuaValue rhs )       { return rhs.strcmp(this)>=0? LuaValue.TRUE: FALSE; }
	public boolean lteq_b( LuaValue rhs )     { return rhs.strcmp(this)>=0; }
	public boolean lteq_b( int rhs )       { typerror("attempt to compare string with number"); return false; }
	public boolean lteq_b( double rhs )    { typerror("attempt to compare string with number"); return false; }
	public LuaValue   gt( LuaValue rhs )         { return rhs.strcmp(this)<0? LuaValue.TRUE: FALSE; }
	public boolean gt_b( LuaValue rhs )       { return rhs.strcmp(this)<0; }
	public boolean gt_b( int rhs )         { typerror("attempt to compare string with number"); return false; }
	public boolean gt_b( double rhs )      { typerror("attempt to compare string with number"); return false; }
	public LuaValue   gteq( LuaValue rhs )       { return rhs.strcmp(this)<=0? LuaValue.TRUE: FALSE; }
	public boolean gteq_b( LuaValue rhs )     { return rhs.strcmp(this)<=0; }
	public boolean gteq_b( int rhs )       { typerror("attempt to compare string with number"); return false; }
	public boolean gteq_b( double rhs )    { typerror("attempt to compare string with number"); return false; }

	// concatenation
	public String concat_s(LuaValue rhs)      { return rhs.concatTo_s(toString()); }
	public String concatTo_s(String lhs)   { return lhs + toString(); }

	// string comparison 
	public int strcmp(LuaValue lhs)           { return -lhs.strcmp(this); }
	public int strcmp(LuaString rhs) {
		for ( int i=0, j=0; i<m_length && j<rhs.m_length; ++i, ++j ) {
			if ( m_bytes[m_offset+i] != rhs.m_bytes[rhs.m_offset+j] ) {
				return ((int)m_bytes[m_offset+i]) - ((int) rhs.m_bytes[rhs.m_offset+j]);
			}
		}
		return m_length - rhs.m_length;
	}
	
	/** Check for number in arithmetic, or throw aritherror */
	private LuaValue checkarith() { 
		LuaValue v = tonumber(10);
		return v.isnil()? aritherror(): v;
	}
	
	public int checkint() {
		return checknumber().toint(); 
	}
	public LuaInteger checkinteger() {
		return checknumber().checkinteger(); 
	}
	public long checklong() {
		return checknumber().tolong();
	}
	public double checkdouble() {
		return checknumber().todouble();
	}
	public LuaNumber checknumber() {
		LuaValue n = tonumber(10);
		if ( ! n.isnumber() )
			typerror("number");
		return (LuaNumber) n;
	}
	public LuaValue tonumber() {
		return tonumber(10);
	}

	public boolean isnumber() {
		return ! tonumber(10).isnil();
	}
	
	public boolean isint() {
		return tonumber(10).isint();
	}

	public boolean islong() { 
		return tonumber(10).islong(); 
	}
	
	public byte    tobyte()        { return (byte) toint(); }
	public char    tochar()        { return (char) toint(); }
	public double  todouble()      { LuaValue n=tonumber(10); return n.isnil()? 0: n.todouble(); }
	public float   tofloat()       { return (float) todouble(); }
	public int     toint()         { LuaValue n=tonumber(10); return n.isnil()? 0: n.toint(); }
	public long    tolong()        { return (long) todouble(); }
	public short   toshort()       { return (short) toint(); }

	public double optdouble(double defval) {
		return checknumber().checkdouble();
	}
	
	public int optint(int defval) {
		return checknumber().checkint();
	}
	
	public LuaInteger optinteger(LuaInteger defval) { 
		return checknumber().checkinteger();
	}
	
	public long optlong(long defval) {
		return checknumber().checklong();
	}
	
	public LuaNumber optnumber(LuaNumber defval) {
		return checknumber().checknumber();
	}
	
	public LuaString optstring(LuaString defval) {
		return this; 
	}
		
	public String optString(String defval) { 
		return toString(); 
	}
	
	public LuaString strvalue() {
		return this;
	}
	
	public LuaString substring( int beginIndex, int endIndex ) {
		return new LuaString( m_bytes, m_offset + beginIndex, endIndex - beginIndex );
	}
	
	public int hashCode() {
		int h = m_length;  /* seed */
		int step = (m_length>>5)+1;  /* if string is too long, don't hash all its chars */
		for (int l1=m_length; l1>=step; l1-=step)  /* compute hash */
		    h = h ^ ((h<<5)+(h>>2)+(((int) m_bytes[m_offset+l1-1] ) & 0x0FF ));
		return h;
	}
	
	// object comparison, used in key comparison
	public boolean equals( Object o ) {
		if ( o instanceof LuaString ) {
			LuaString s = (LuaString) o;
			if ( s.m_length != m_length )
				return false;
			if ( s.hashCode() != hashCode() )
				return false;
			for ( int i=0; i<m_length; i++ )
				if ( s.m_bytes[s.m_offset+i] != m_bytes[m_offset+i] )
					return false;
			return true;
		} else {
			return false;
		}
	}

	public boolean eq_b( LuaValue val ) {
		return equals( val );
	}
	
	public static boolean equals( LuaString a, int i, LuaString b, int j, int n ) {
		return equals( a.m_bytes, a.m_offset + i, b.m_bytes, b.m_offset + j, n );
	}
	
	public static boolean equals( byte[] a, int i, byte[] b, int j, int n ) {
		if ( a.length < i + n || b.length < j + n )
			return false;
		while ( --n>=0 ) 
			if ( a[i++]!=b[j++] )
				return false;
		return true;
	}

	public void write(DataOutputStream writer, int i, int len) throws IOException {
		writer.write(m_bytes,m_offset+i,len);
	}
	
	public LuaValue len() {
		return LuaInteger.valueOf(m_length);
	}

	public int length() {
		return m_length;
	}

	public int luaByte(int index) {
		return m_bytes[m_offset + index] & 0x0FF;
	}

	public int charAt( int index ) {
		if ( index < 0 || index >= m_length )
			throw new IndexOutOfBoundsException();
		return luaByte( index );
	}
	
	public String checkString() { 
		return toString(); 
	}

	public LuaString checkstring() {
		return this;
	}
	
	public InputStream toInputStream() {
		return new ByteArrayInputStream(m_bytes, m_offset, m_length);
	}
	
	/**
	 * Copy the bytes of the string into the given byte array.
	 */
	public void copyInto( int strOffset, byte[] bytes, int arrayOffset, int len ) {
		System.arraycopy( m_bytes, m_offset+strOffset, bytes, arrayOffset, len );
	}
	
	/** Java version of strpbrk, which is a terribly named C function. */
	public int indexOfAny( LuaString accept ) {
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
	
	public int indexOf( byte b, int start ) {
		for ( int i=0, j=m_offset+start; i < m_length; ++i ) {
			if ( m_bytes[j++] == b )
				return i;
		}
		return -1;
	}
	
	public int indexOf( LuaString s, int start ) {
		final int slen = s.length();
		final int limit = m_offset + m_length - slen;
		for ( int i = m_offset + start; i <= limit; ++i ) {
			if ( equals( m_bytes, i, s.m_bytes, s.m_offset, slen ) ) {
				return i;
			}
		}
		return -1;
	}
	
	public int lastIndexOf( LuaString s ) {
		final int slen = s.length();
		final int limit = m_offset + m_length - slen;
		for ( int i = limit; i >= m_offset; --i ) {
			if ( equals( m_bytes, i, s.m_bytes, s.m_offset, slen ) ) {
				return i;
			}
		}
		return -1;
	}

	// --------------------- utf8 conversion -------------------------
	
	/**
	 * Convert to Java String interpreting as utf8 characters 
	 */
	public static String decodeAsUtf8(byte[] bytes, int offset, int length) {
		int i,j,n,b;
		for ( i=offset,j=offset+length,n=0; i<j; ++n ) {
			switch ( 0xC0 & bytes[i++] ) {
			case 0xE0: ++i;
			case 0xC0: ++i;
			}
		}
		char[] chars=new char[n];
		for ( i=offset,j=offset+length,n=0; i<j; ) {
			chars[n++] = (char) (
				((b=bytes[i++])>=0||i>=j)? b:
				(b<-32||i+1>=j)? (((b&0x3f) << 6) | (bytes[i++]&0x3f)):
					(((b&0xf) << 12) | ((bytes[i++]&0x3f)<<6) | (bytes[i++]&0x3f)));
		}
		return new String(chars);
	}
	
	/**
	 * Count the number of bytes required to encode the string as UTF-8.
	 */
	public static int lengthAsUtf8(char[] chars) {		
		int i,b;
		char c;
		for ( i=b=chars.length; --i>=0; )
			if ( (c=chars[i]) >=0x80 )
				b += (c>=0x800)? 2: 1;
		return b;
	}
	
	/**
	 * Encode the given Java string as UTF-8 bytes, writing the result to bytes
	 * starting at offset. The string should be measured first with lengthAsUtf8
	 * to make sure the given byte array is large enough.
	 */
	public static void encodeToUtf8(char[] chars, byte[] bytes, int off) {
		final int n = chars.length;
		char c;
		for ( int i=0, j=off; i<n; i++ ) {
			if ( (c = chars[i]) < 0x80 ) {
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
	}

	public boolean isValidUtf8() {
		int i,j,n,b,e=0;
		for ( i=m_offset,j=m_offset+m_length,n=0; i<j; ++n ) {
			int c = m_bytes[i++];
			if ( c >= 0 ) continue;
			if ( ((c & 0xE0) == 0xC0) 
					&& i<j 
					&& (m_bytes[i++] & 0xC0) == 0x80) continue;
			if ( ((c & 0xF0) == 0xE0) 
					&& i+1<j 
					&& (m_bytes[i++] & 0xC0) == 0x80 
					&& (m_bytes[i++] & 0xC0) == 0x80) continue;
			return false;
		}
		return true;
	}
	
	// --------------------- number conversion -----------------------
	
	/** 
	 * convert to a number using a supplied base, or NIL if it can't be converted
	 * @return IntValue, DoubleValue, or NIL depending on the content of the string. 
	 */
	public LuaValue tonumber( int base ) {
		if ( base >= 2 && base <= 36 ) {
			int i=m_offset,j=m_offset+m_length;
			while ( i<j && m_bytes[i]==' ' ) ++i;
			while ( i<j && m_bytes[j-1]==' ' ) --j;
			if ( i>=j ) return FALSE;
			if ( ( base == 10 || base == 16 ) && ( m_bytes[i]=='0' && i+1<j && (m_bytes[i+1]=='x'||m_bytes[i+1]=='X') ) ) {
				base = 16;
				i+=2;
			}
			LuaValue l = scanlong( base, i, j );
			return l!=NIL? l: base==10? scandouble(i,j): NIL;
		}
		
		return NIL;
	}
	
	/**
	 * Scan and convert a long value, or return NIL if not found.
	 * @return DoubleValue, IntValue, or NIL depending on what is found.
	 */
	private LuaValue scanlong( int base, int start, int end ) {
		long x = 0;
		boolean neg = (m_bytes[start] == '-');
		for ( int i=(neg?start+1:start); i<end; i++ ) {
			int digit = m_bytes[i] - (base<=10||(m_bytes[i]>='0'&&m_bytes[i]<='9')? '0':
					m_bytes[i]>='A'&&m_bytes[i]<='Z'? ('A'-10): ('a'-10));
			if ( digit < 0 || digit >= base )
				return NIL;		
			x = x * base + digit;
		}
		return valueOf(neg? -x: x);
	}
	
	/**
	 * Scan and convert a double value, or return NIL if not a double.
	 * @return DoubleValue, IntValue, or NIL depending on what is found.
	 */
	private LuaValue scandouble(int start, int end) {
		if ( end>start+64 ) end=start+64;
		for ( int i=start; i<end; i++ ) {
			switch ( m_bytes[i] ) {
			case '-':
			case '+':
			case '.':
			case 'e': case 'E':
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				break;
			default:
				return NIL;
			}
		}
		char [] c = new char[end-start];
		for ( int i=start; i<end; i++ )
			c[i-start] = (char) m_bytes[i];
		try {
			return valueOf( Double.parseDouble(new String(c)));
		} catch ( Exception e ) {			
		}
		return NIL;
	}

}
