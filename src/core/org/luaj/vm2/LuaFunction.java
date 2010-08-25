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

abstract
public class LuaFunction extends LuaValue {
	
	public static LuaValue s_metatable;

	protected LuaValue env;
	
	public LuaFunction() {
		this.env = NIL;
	}
	
	public LuaFunction(LuaValue env) {
		this.env = env;
	}

	public int type() {
		return TFUNCTION;
	}
	
	public String typename() {
		return "function";
	}
	
	public boolean isfunction() {
		return true;
	}

	public LuaValue checkfunction()  {
		return this;
	}
	
	public LuaFunction optfunction(LuaFunction defval) {
		return this; 
	}

	public LuaValue getmetatable() { 
		return s_metatable; 
	}
	
	public LuaValue getfenv() {
		return env;
	}

	public void setfenv(LuaValue env) {
		this.env = env!=null? env: NIL;
	}
}
