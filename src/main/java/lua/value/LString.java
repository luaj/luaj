package lua.value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import lua.Lua;

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

	public static final LString TYPE_NAME = new LString("string");
	
	final byte[] m_bytes;
	final int m_offset;
	final int m_length;
	final int m_hash;
	
	private static LTable s_stringMT;
	
	/**
	 * Construct a Lua string from the given Java string. Characters are encoded
	 * using UTF-8.
	 */
	public LString(String string) {
		byte[] bytes;
		try {
			bytes = string.getBytes( "UTF-8" );
		} catch ( UnsupportedEncodingException exn ) {
			bytes = stringToUtf8Bytes( string );
		}
		this.m_bytes = bytes;
		this.m_offset = 0;
		this.m_length = m_bytes.length;
		this.m_hash = hashBytes( m_bytes, 0, m_length );
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
	
	public static LString valueOf( double d ) {
		return new LString( String.valueOf( d ) );
	}
	
	public static LString valueOf( int x ) {
		return new LString( String.valueOf( x ) );
	}
	
	public static LString concat( final LString[] strings ) {
		int length = 0;
		for ( int i = 0; i < strings.length; ++i ) {
			length += strings[i].length();
		}
		byte[] bytes = new byte[length];
		
		for ( int i = 0, offset = 0; i < strings.length; ++i ) {
			LString s = strings[i];
			final int len = s.length();
			System.arraycopy( s.m_bytes, s.m_offset, bytes, offset, len );
			offset += len;
		}
		
		return new LString( bytes );
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
		luaUnsupportedOperation();
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
				return new LInteger( Integer.parseInt( str, base ) );
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
	
	public String toJavaString() {
		try {
			return new String( m_bytes, m_offset, m_length, "UTF-8" );
		} catch ( UnsupportedEncodingException uee ) {
			throw new RuntimeException("toJavaString: UTF-8 decoding not implemented");
		}
	}
	
	/** Built-in opcode LEN, for Strings and Tables */
	public LValue luaLength() {
		return new LInteger( length() );
	}

	public LString luaGetType() {
		return TYPE_NAME;
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
	
	public static boolean equals( byte[] a, int i, byte[] b, int j, int n ) {
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
	
	private static byte[] stringToUtf8Bytes( final String string ) {
		final int strlen = string.length();
		byte[] bytes = new byte[ strlen ];
		byte b1 = 0, b2 = 0, b3 = 0;
		
		int j = 0;
		for ( int i = 0; i < strlen; ++i ) {
			int c = string.charAt( i );
			// TODO: combine 2-character combinations
			int count;
			if ( c > 0x07FF ) {
				count = 3;
				b3 = (byte)( 0xE0 | (   c >> 12 ) );
				b2 = (byte)( 0x80 | ( ( c >> 6  ) & 0x03F ) );
				b1 = (byte)( 0x80 | ( ( c       ) & 0x03F ) );
			} else if ( c > 0x07F ) {
				count = 2;
				b2 = (byte)( 0xC0 | ( c >> 6 ) );
				b1 = (byte)( 0x80 | ( c & 0x03F ) );
			} else {
				count = 1;
				b1 = (byte) c;
			}
			if ( j + count > bytes.length ) {
				bytes = realloc( bytes, ( j + count ) * 2 );
			}
			switch ( count ) {
			case 3:
				bytes[j++] = b3;
			case 2:
				bytes[j++] = b2;
			case 1:
				bytes[j++] = b1;
			}
		}
		
		if ( j != bytes.length ) {
			bytes = realloc( bytes, j );
		}
		return bytes;
	}
	
	private static byte[] realloc( byte[] a, int newSize ) {
		final byte[] newbytes = new byte[ newSize ];
		System.arraycopy( a, 0, newbytes, 0, Math.min( newSize, a.length ) );
		return newbytes;
	}
}
