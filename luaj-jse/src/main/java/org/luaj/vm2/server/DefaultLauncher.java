/*******************************************************************************
 * Copyright (c) 2015 Luaj.org. All rights reserved.
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
package org.luaj.vm2.server;

import java.io.InputStream;
import java.io.Reader;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Default {@link Launcher} instance that creates standard globals and runs the
 * supplied scripts with chunk name 'main'.
 * <P>
 * Arguments are coerced into lua using {@link CoerceJavaToLua#coerce(Object)}.
 * <P>
 * Return values with simple types are coerced into Java simple types. Tables,
 * threads, and functions are returned as lua objects.
 *
 * @see Launcher
 * @see LuajClassLoader
 * @see LuajClassLoader#NewLauncher()
 * @see LuajClassLoader#NewLauncher(Class)
 * @since luaj 3.0.1
 */
public class DefaultLauncher implements Launcher {
	protected Globals g;

	public DefaultLauncher() {
		g = JsePlatform.standardGlobals();
	}

	/** Launches the script with chunk name 'main' */
	@Override
	public Object[] launch(String script, Object[] arg) {
		return launchChunk(g.load(script, "main"), arg);
	}

	/**
	 * Launches the script with chunk name 'main' and loading using modes 'bt'
	 */
	@Override
	public Object[] launch(InputStream script, Object[] arg) {
		return launchChunk(g.load(script, "main", "bt", g), arg);
	}

	/** Launches the script with chunk name 'main' */
	@Override
	public Object[] launch(Reader script, Object[] arg) {
		return launchChunk(g.load(script, "main"), arg);
	}

	private Object[] launchChunk(LuaValue chunk, Object[] arg) {
		LuaValue args[] = new LuaValue[arg.length];
		for (int i = 0; i < args.length; ++i)
			args[i] = CoerceJavaToLua.coerce(arg[i]);
		Varargs results = chunk.invoke(LuaValue.varargsOf(args));

		final int n = results.narg();
		Object return_values[] = new Object[n];
		for (int i = 0; i < n; ++i) {
			LuaValue r = results.arg(i+1);
			switch (r.type()) {
			case LuaValue.TBOOLEAN:
				return_values[i] = r.toboolean();
				break;
			case LuaValue.TNUMBER:
				return_values[i] = r.todouble();
				break;
			case LuaValue.TINT:
				return_values[i] = r.toint();
				break;
			case LuaValue.TNIL:
				return_values[i] = null;
				break;
			case LuaValue.TSTRING:
				return_values[i] = r.tojstring();
				break;
			case LuaValue.TUSERDATA:
				return_values[i] = r.touserdata();
				break;
			default:
				return_values[i] = r;
			}
		}
		return return_values;
	}
}
