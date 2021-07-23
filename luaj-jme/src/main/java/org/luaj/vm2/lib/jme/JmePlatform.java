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
package org.luaj.vm2.lib.jme;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.MathLib;
import org.luaj.vm2.lib.OsLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;

/**
 * The {@link org.luaj.vm2.lib.jme.JmePlatform} class is a convenience class to
 * standardize how globals tables are initialized for the JME platform.
 * <p>
 * The JME platform, being limited, cannot implement all libraries in all
 * aspects. The main limitations are
 * <ul>
 * <li>Some math functions are not implemented, see {@link MathLib} for
 * details</li>
 * <li>Scripts are loaded via Class.getResourceAsStream(), see {@link BaseLib}
 * for details</li>
 * <li>OS functions execute(), remove(), rename(), and tmpname() vary, see
 * {@link OsLib} for details</li>
 * <li>I/O seek is not implemented, see {@link org.luaj.vm2.lib.jme.JmeIoLib}
 * for details</li>
 * <li>luajava is not available, see {@link org.luaj.vm2.lib.jse.LuajavaLib} for
 * details</li>
 * </ul>
 * <p>
 * It is used to allocate either a set of standard globals using
 * {@link #standardGlobals()} or debug globals using {@link #debugGlobals()}
 * <p>
 * A simple example of initializing globals and using them from Java is:
 *
 * <pre>
 * {
 * 	&#64;code
 * 	Globals global = JmePlatform.standardGlobals();
 * 	global.get("print").call(LuaValue.valueOf("hello, world"));
 * }
 * </pre>
 * <p>
 * Once globals are created, a simple way to load and run a script is:
 *
 * <pre>
 *  {@code
 * LoadState.load( getClass().getResourceAsStream("main.lua"), "main.lua", globals ).call();
 * }
 * </pre>
 * <p>
 * although {@code require} could also be used:
 *
 * <pre>
 *  {@code
 * globals.get("require").call(LuaValue.valueOf("main"));
 * }
 * </pre>
 *
 * For this to succeed, the file "main.lua" must be a resource in the class
 * path. See {@link BaseLib} for details on finding scripts using
 * {@link ResourceFinder}.
 * <p>
 * The standard globals will contain all standard libraries in their JME
 * flavors:
 * <ul>
 * <li>{@link Globals}</li>
 * <li>{@link BaseLib}</li>
 * <li>{@link PackageLib}</li>
 * <li>{@link Bit32Lib}</li>
 * <li>{@link TableLib}</li>
 * <li>{@link StringLib}</li>
 * <li>{@link CoroutineLib}</li>
 * <li>{@link MathLib}</li>
 * <li>{@link org.luaj.vm2.lib.jme.JmeIoLib}</li>
 * <li>{@link OsLib}</li>
 * </ul>
 * In addition, the {@link LuaC} compiler is installed so lua files may be
 * loaded in their source form.
 * <p>
 * The debug globals are simply the standard globals plus the {@code debug}
 * library {@link DebugLib}.
 * <p>
 * <p>
 * The class ensures that initialization is done in the correct order.
 *
 * @see Globals
 * @see org.luaj.vm2.lib.jse.JsePlatform
 */
public class JmePlatform {

	/**
	 * Create a standard set of globals for JME including all the libraries.
	 *
	 * @return Table of globals initialized with the standard JME libraries
	 * @see #debugGlobals()
	 * @see org.luaj.vm2.lib.jse.JsePlatform
	 * @see org.luaj.vm2.lib.jme.JmePlatform
	 */
	public static Globals standardGlobals() {
		Globals globals = new Globals();
		globals.load(new BaseLib());
		globals.load(new PackageLib());
		globals.load(new Bit32Lib());
		globals.load(new OsLib());
		globals.load(new MathLib());
		globals.load(new TableLib());
		globals.load(new StringLib());
		globals.load(new CoroutineLib());
		globals.load(new JmeIoLib());
		LoadState.install(globals);
		LuaC.install(globals);
		return globals;
	}

	/**
	 * Create standard globals including the {@link DebugLib} library.
	 *
	 * @return Table of globals initialized with the standard JSE and debug
	 *         libraries
	 * @see #standardGlobals()
	 * @see org.luaj.vm2.lib.jse.JsePlatform
	 * @see org.luaj.vm2.lib.jme.JmePlatform
	 * @see DebugLib
	 */
	public static Globals debugGlobals() {
		Globals globals = standardGlobals();
		globals.load(new DebugLib());
		return globals;
	}
}
