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
package org.luaj.vm2.lib;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class JmePlatform {

	/**
	 * Create a standard set of globals for JME including all the libraries.
	 * 
	 * @return Table of globals initialized with the standard JME libraries
	 */
	public static LuaTable standardGlobals() {
		LuaTable _G = new BaseLib();
		new org.luaj.vm2.lib.PackageLib(_G);
		set(_G, "coroutine", new org.luaj.vm2.lib.CoroutineLib() );
		set(_G, "io",        new org.luaj.vm2.lib.jme.JseIoLib() );
		set(_G, "math",      new org.luaj.vm2.lib.MathLib() );
		set(_G, "os",        new org.luaj.vm2.lib.OsLib() );
		set(_G, "table",     new org.luaj.vm2.lib.TableLib() );
		set(_G, "string",    new org.luaj.vm2.lib.StringLib() );
		return _G;		
	}
	
	public static LuaTable debugGlobals() {
		LuaTable _G = standardGlobals();
		set(_G, "string",    new org.luaj.vm2.lib.DebugLib() );
		return _G;
	}

	private static void set( LuaTable _G, String name, LuaValue chunk ) {
		chunk.setfenv(_G);
		LuaValue pkg = chunk.call(LuaValue.valueOf(name));
		_G.set( name, pkg );
	}

}
