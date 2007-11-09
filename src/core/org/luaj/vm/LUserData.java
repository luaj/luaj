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


public class LUserData extends LValue {
	
	public final Object m_instance;
	public LTable m_metatable;
	
	public LUserData(Object obj) {
		m_instance = obj;
	}
	
	public String toJavaString() {
		return String.valueOf(m_instance);
	}
	
	public boolean equals(Object obj) {
		return (this == obj) ||
			(obj instanceof LUserData && this.m_instance == ((LUserData) obj).m_instance);
	}
	
	public int hashCode() {
		return System.identityHashCode( m_instance );
	}

	public int luaGetType() {
		return Lua.LUA_TUSERDATA;
	}
	
	public LTable luaGetMetatable() {
		return m_metatable; 
	}

	public Object toJavaInstance() {
		return m_instance;
	}
}
