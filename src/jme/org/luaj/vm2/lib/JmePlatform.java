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

public class JmePlatform {

	/**
	 * Create a standard set of globals for JME including all the libraries.
	 * 
	 * @return Table of globals initialized with the standard JME libraries
	 */
	public static LuaTable standardGlobals() {
		LuaTable _G = new BaseLib();
		new org.luaj.vm2.lib.PackageLib(_G);
		_G.set( "io",        new org.luaj.vm2.lib.jme.JseIoLib() );
		_G.set( "math",      new org.luaj.vm2.lib.MathLib() );
		_G.set( "os",        new org.luaj.vm2.lib.OsLib() );
		_G.set( "table",     new org.luaj.vm2.lib.TableLib() );
		_G.set( "string",    new org.luaj.vm2.lib.StringLib() );
		CoroutineLib.install( _G );
		return _G;		
	}

}
