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


public final class LBoolean extends LValue {

	public static final LBoolean TRUE = new LBoolean("true",true);
	
	public static final LBoolean FALSE = new LBoolean("false",false);
	
	private final String m_sname;
	private final LString m_name;
	private final boolean m_value;
	
	private LBoolean( String name, boolean value ) {
		this.m_sname = name;
		this.m_name = new LString( name );
		this.m_value = value;
	}
	
	public final String toJavaString() {
		return m_sname;
	}
	
	public final LString luaAsString() {
		return m_name;
	}
	
	public final boolean toJavaBoolean() {
		return m_value;
	}
	
	public final int toJavaInt() {
		return m_value? 1: 0;
	}
	
	public final static LBoolean valueOf(boolean value) {
		return value? TRUE: FALSE;
	}
	
	public int luaGetType() {
		return Lua.LUA_TBOOLEAN;
	}
}
