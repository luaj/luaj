package lua.addon.luacompat;

import lua.value.LString;

public class LBuffer {

	private byte[] bytes;
	private int length;
	
	public LBuffer( int initialCapacity ) {
		bytes = new byte[ initialCapacity ];
		length = 0;
	}
	
	public void append( byte b ) {
		ensureCapacity( length + 1 );
		bytes[ length++ ] = b;
	}
	
	public void append( LString str ) {
		final int alen = str.length();
		ensureCapacity( length + alen );
		str.copyInto( 0, bytes, length, alen );
		length += alen;
	}
	
	public void setLength( int length ) {
		ensureCapacity( length );
		this.length = length;
	}
	
	public LString toLuaString() {
		return new LString( realloc( bytes, length ) );
	}
	
	public void ensureCapacity( int minSize ) {
		if ( minSize > bytes.length )
			realloc( minSize );
	}
	
	private void realloc( int minSize ) {
		bytes = realloc( bytes, Math.max( bytes.length * 2, minSize ) ); 
	}
	
	private static byte[] realloc( byte[] b, int newSize ) {
		byte[] newBytes = new byte[ newSize ];
		System.arraycopy( b, 0, newBytes, 0, Math.min( b.length, newSize ) );
		return newBytes;
	}
}
