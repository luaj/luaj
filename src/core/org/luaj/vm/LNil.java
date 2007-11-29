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


public final class LNil extends LValue {
	public static final LNil NIL = new LNil();
	
	public final LString luaAsString() {
		return luaGetTypeName();
	}

	public boolean toJavaBoolean() {
		return false;
	}

	public int luaGetType() {
		return Lua.LUA_TNIL;
	}
	
	public int toJavaInt() {
		return 0;
	}

	public String toJavaString() {
		return "nil";
	}

	public Byte toJavaBoxedByte() {
		return null;
	}

	public Character toJavaBoxedCharacter() {
		return null;
	}

	public Double toJavaBoxedDouble() {
		return null;
	}

	public Float toJavaBoxedFloat() {
		return null;
	}

	public Integer toJavaBoxedInteger() {
		return null;
	}

	public Long toJavaBoxedLong() {
		return null;
	}

	public Short toJavaBoxedShort() {
		return null;
	}
	
}
