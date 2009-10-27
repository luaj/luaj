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

public class TableLib extends LuaTable {

	public TableLib() {
		LibFunction.bind( this, new TableFunc1().getClass(), new String[] {
			"getn", // (table) -> number
			"maxn", // (table) -> number 
		} );
		LibFunction.bind( this, new TableFuncV().getClass(), new String[] {
			"remove", // (table [, pos]) -> removed-ele
			"concat", // (table [, sep [, i [, j]]]) -> string
			"insert", // (table, [pos,] value) -> prev-ele
			"sort",	  // (table [, comp]) -> void
			"foreach", // (table, func) -> void
			"foreachi", // (table, func) -> void			
		} );
	}
	
	public static class TableFunc1 extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			switch ( opcode ) {
			case 0: return arg.checktable().getn();
			case 1: return valueOf( arg.checktable().maxn());
			}
			return NIL;
		}
		
	}
	

	public static class TableFuncV extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			switch ( opcode ) {
			case 0: { // "remove" (table [, pos]) -> removed-ele
				LuaTable table = args.checktable(1);
				int pos = args.narg()>1? args.checkint(2): 0;
				return table.remove(pos);
			}
			case 1: { // "concat" (table [, sep [, i [, j]]]) -> string
				LuaTable table = args.checktable(1);
				return table.concat(
						args.optstring(2,LuaValue.EMPTYSTRING),
						args.optint(3,1),
						args.isvalue(4)? args.checkint(4): table.length() );
			}
			case 2: { // "insert" (table, [pos,] value) -> prev-ele
				final LuaTable table = args.checktable(1);
				final int pos = args.narg()>2? args.checkint(2): 0;
				final LuaValue value = args.arg( args.narg()>2? 3: 2 );
				table.insert( pos, value );
				return NONE;
			}
			case 3: { // "sort" (table [, comp]) -> void
				args.checktable(1).sort( args.optvalue(2,NIL) );
				return NONE;
			}
			case 4: { // (table, func) -> void
				return args.checktable(1).foreach( args.checkfunction(2) );
			}
			case 5: { // "foreachi" (table, func) -> void
				return args.checktable(1).foreachi( args.checkfunction(2) );
			}
			}
			return NONE;
		}
	}
}
