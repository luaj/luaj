/*******************************************************************************
* Copyright (c) 2010 Luaj.org. All rights reserved.
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
package org.luaj.vm2.ast;

import java.io.ByteArrayOutputStream;

import org.luaj.vm2.LuaString;

public class Str {
	
	public final byte[] bytes;
	public final boolean isutf8;
	
	public Str(byte[] bytes) {
		this.bytes = bytes;
		this.isutf8 = true; // TODO: scan to see
	}
	public static LuaString quoteString(String image) {
		String s = image.substring(1, image.length()-1);
		byte[] bytes = unquote(s);
		// TODO: check for non-utf8
		return LuaString.valueOf(bytes);
	}
	public static LuaString charString(String image) {
		String s = image.substring(1, image.length()-1);
		byte[] bytes = unquote(s);
		// TODO: check for non-utf8
		return LuaString.valueOf(bytes);
	}
	public static LuaString longString(String image) {
		int i = image.indexOf('[', image.indexOf('[')+1);
		String s = image.substring(i,image.length()-i);
		return LuaString.valueOf(s);
	}
	public static byte[] unquote(String s) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		char[] c = s.toCharArray();
		int n = c.length;
		for ( int i=0; i<n; i++ ) {
			if ( c[i] == '\\' && i<n ) {
				switch ( c[++i] ) {
				case '0': case '1': case '2': case '3': case '4':
				case '5': case '6': case '7': case '8': case '9':
					int d=(int) (c[i++]-'0');
					for ( int j=0; i<n && j<2 && c[i]>='0' && c[i]<='9'; i++, j++ )
						d = d * 10 + (int) (c[i]-'0');
					baos.write( (byte) d );
					--i;
					continue;
				case 'a':  baos.write( (byte) 7 );    continue;
				case 'b':  baos.write( (byte) '\b' ); continue;
				case 'f':  baos.write( (byte) '\f' ); continue;
				case 'n':  baos.write( (byte) '\n' ); continue;
				case 'r':  baos.write( (byte) '\r' ); continue;
				case 't':  baos.write( (byte) '\t' ); continue;
				case 'v':  baos.write( (byte) 11 );   continue;
				case '"':  baos.write( (byte) '"' );  continue;
				case '\'': baos.write( (byte) '\'' ); continue;
				case '\\': baos.write( (byte) '\\' ); continue;
				default: baos.write( (byte) c[i] ); break;
				}
			} else {
				baos.write( (byte) c[i] );
			}
		}
		return baos.toByteArray();
	}
	/*
	private static byte[] utf8decode(String s) {
		try {
			return s.getBytes("UTF8");
		} catch ( Exception e ) {
			throw new RuntimeException("utf8 not found: "+e);
		}
	}
	*/
}
