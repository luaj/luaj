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
import org.luaj.vm2.Varargs;

/** 
 * Subclass of {@link LibFunction} which implements the lua standard {@code table} 
 * library. 
 * 
 * <p>
 * Typically, this library is included as part of a call to either 
 * {@link org.luaj.vm2.lib.jse.JsePlatform#standardGlobals()} or {@link org.luaj.vm2.lib.jme.JmePlatform#standardGlobals()}
 * <pre> {@code
 * Globals globals = JsePlatform.standardGlobals();
 * System.out.println( globals.get("table").get("length").call( LuaValue.tableOf() ) );
 * } </pre>
 * <p>
 * To instantiate and use it directly, 
 * link it into your globals table via {@link LuaValue#load(LuaValue)} using code such as:
 * <pre> {@code
 * Globals globals = new Globals();
 * globals.load(new JseBaseLib());
 * globals.load(new PackageLib());
 * globals.load(new TableLib());
 * System.out.println( globals.get("table").get("length").call( LuaValue.tableOf() ) );
 * } </pre>
 * <p>
 * This has been implemented to match as closely as possible the behavior in the corresponding library in C.
 * @see LibFunction
 * @see org.luaj.vm2.lib.jse.JsePlatform
 * @see org.luaj.vm2.lib.jme.JmePlatform
 * @see <a href="http://www.lua.org/manual/5.2/manual.html#6.5">Lua 5.2 Table Lib Reference</a>
 */
public class TableLib extends TwoArgFunction {

	/** Perform one-time initialization on the library by creating a table
	 * containing the library functions, adding that table to the supplied environment,
	 * adding the table to package.loaded, and returning table as the return value.
	 * @param modname the module name supplied if this is loaded via 'require'.
	 * @param env the environment to load into, typically a Globals instance.
	 */
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaTable table = new LuaTable();
		table.set("concat", new concat());
		table.set("insert", new insert());
		table.set("pack", new pack());
		table.set("remove", new remove());
		table.set("sort", new sort());
		table.set("unpack", new unpack());
		env.set("table", table);
		env.get("package").get("loaded").set("table", table);
		return NIL;
	}

	static class TableLibFunction extends LibFunction {
		public LuaValue call() {
			return argerror(1, "table expected, got no value");
		}
	}
	
	// "concat" (table [, sep [, i [, j]]]) -> string
	static class concat extends TableLibFunction {
		public LuaValue call(LuaValue list) {
			return list.checktable().concat(EMPTYSTRING,1,list.length());
		}
		public LuaValue call(LuaValue list, LuaValue sep) {
			return list.checktable().concat(sep.checkstring(),1,list.length());
		}
		public LuaValue call(LuaValue list, LuaValue sep, LuaValue i) {
			return list.checktable().concat(sep.checkstring(),i.checkint(),list.length());
		}
		public LuaValue call(LuaValue list, LuaValue sep, LuaValue i, LuaValue j) {
			return list.checktable().concat(sep.checkstring(),i.checkint(),j.checkint());
		}
	}

	// "insert" (table, [pos,] value)
	static class insert extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			switch (args.narg()) {
			case 0: case 1: {
				return argerror(2, "value expected");
			}
			case 2: {
				LuaTable table = args.arg1().checktable();
				table.insert(table.length()+1,args.arg(2));
				return NONE;
			}
			default: {
				args.arg1().checktable().insert(args.checkint(2),args.arg(3));
				return NONE;
			}
			}
		}
	}
	
	// "pack" (...) -> table
	static class pack extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			LuaValue t = tableOf(args, 1);
			t.set("n", args.narg());
			return t;
		}
	}

	// "remove" (table [, pos]) -> removed-ele
	static class remove extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			return args.arg1().checktable().remove(args.optint(2, 0));
		}
	}

	// "sort" (table [, comp])
	static class sort extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			args.arg1().checktable().sort(
					args.arg(2).isnil()? NIL: args.arg(2).checkfunction());
			return NONE;
		}
	}

	
	// "unpack", // (list [,i [,j]]) -> result1, ...
	static class unpack extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			LuaTable t = args.checktable(1);
			switch (args.narg()) {
			case 1: return t.unpack();
			case 2: return t.unpack(args.checkint(2));
			default: return t.unpack(args.checkint(2), args.checkint(3));
			}
		}
	}
}
