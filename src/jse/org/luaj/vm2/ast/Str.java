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
		// TODO: unquote string data
		return utf8decode(s);
	}
	private static byte[] utf8decode(String s) {
		try {
			return s.getBytes("UTF8");
		} catch ( Exception e ) {
			throw new RuntimeException("utf8 not found: "+e);
		}
	}
}
