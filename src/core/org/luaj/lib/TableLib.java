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
package org.luaj.lib;

import java.io.ByteArrayOutputStream;

import org.luaj.vm.LFunction;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;


public class TableLib extends LFunction {

	public static final String[] NAMES = {
		"table",
		"concat",
		"foreach",
		"foreachi",
		"getn",
		"insert",
		"maxn",
		"remove",
		"sort",
	};
	
	private static final int INSTALL  = 0;
	private static final int CONCAT   = 1;
	private static final int FOREACH  = 2;
	private static final int FOREACHI = 3;
	private static final int GETN     = 4;
	private static final int INSERT   = 5;
	private static final int MAXN     = 6;
	private static final int REMOVE   = 7;
	private static final int SORT     = 8;
	
	public static void install( LTable globals ) {
		LTable table = new LTable();
		for ( int i=1; i<NAMES.length; i++ )
			table.put( NAMES[i], new TableLib(i) );
		globals.put( "table", table );
		PackageLib.setIsLoaded("table", table);
	}

	private final int id;

	/** Default constructor for loading the library dynamically */
	private TableLib() {
		id = 0;
	}

	/** Private constructor to construct a table function instance */
	private TableLib( int id ) {
		this.id = id;
	}

	public String toString() {
		return NAMES[id]+"()";
	}
		
	public int invoke( LuaState vm ) {
		switch ( id ) {

		/* Load the table library dynamically 
		 */
		case INSTALL:
			install(vm._G);
			return 0;
			
			
		/* table.concat (table [, sep [, i [, j]]])
		 * 
		 * Given an array where all elements are strings or numbers, returns table[i]..sep..table[i+1] ··· sep..table[j]. 
		 * The default value for sep is the empty string, the default for i is 1, and the default for j is the length of the table. 
		 * If i is greater than j, returns the empty string.
		 */
		case CONCAT: { 
			// int n = vm.gettop();
			LTable table = vm.checktable(1);
			LString sep = vm.optlstring(2,null);
			int i = vm.optint(3,1);
			int j = vm.optint(4,-1);
			if ( j == -1 )
				j = table.luaLength();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for ( int k=i; k<=j; k++ ) {
				LValue v = table.get(k);
				if ( ! v.isString() )
					vm.argerror(1, "table contains non-strings");
				v.luaConcatTo(baos);
				if ( k<j && sep!=null )
					sep.luaConcatTo( baos );
			}
			vm.pushlstring( baos.toByteArray() );
			return 1;
		}
		
		/* table.getn (table)
		 * 
		 * Get length of table t.
		 */ 
		case FOREACH: 
		case FOREACHI: 
		{ 
			LTable table = vm.checktable(1);
			LFunction function = vm.checkfunction(2);
			vm.pushlvalue( table.foreach( vm, function, id==FOREACHI ) );
			return 1;
		}
		
		/* table.getn (table)
		 * 
		 * Get length of table t.
		 */ 
		case GETN: { 
			LTable table = vm.checktable(1);
			vm.pushinteger(table.luaLength());
			return 1;
		}
		
		/* table.insert (table, [pos,] value)
		 * 
		 * Inserts element value at position pos in table, shifting up other elements to open space, if necessary. 
		 * The default value for pos is n+1, where n is the length of the table (see §2.5.5), so that a call 
		 * table.insert(t,x) inserts x at the end of table t.
		 */ 
		case INSERT: {
			LTable table = vm.checktable(1);
			int pos = 0;
			switch ( vm.gettop() ) {
		    case 2: 
		    	break;
		    case 3:
		    	pos = vm.checkint(2);
		    	break;
		    default:
		    	vm.error( "wrong number of arguments to 'insert'" );
		    }
			table.luaInsertPos( pos, vm.topointer(-1) );
			return 0;
		}

		/* table.maxn (table)
		 * 
		 * Returns the largest positive numerical index of the given table, or zero if the table has no positive numerical 
		 * indices. (To do its job this function does a linear traversal of the whole table.)
		 */ 
		case MAXN: { 
			LTable table = vm.checktable(1);
			vm.pushlvalue( table.luaMaxN() );
			return 1;
		}
			
		/* table.remove (table [, pos])
		 * 
		 * Removes from table the element at position pos, shifting down other elements to close the space, if necessary. 
		 * Returns the value of the removed element. The default value for pos is n, where n is the length of the table, 
		 * so that a call table.remove(t) removes the last element of table t.
		 */ 
		case REMOVE: {
			LTable table = vm.checktable(1);
			int pos = vm.optint(2,0);
			LValue v = table.luaRemovePos( pos );
			vm.pushlvalue( v );
			return v.isNil()? 0: 1;
		}
			
		/* table.sort (table [, comp])
		 * 
		 * Sorts table elements in a given order, in-place, from table[1] to table[n], where n is the length of the table. 
		 * If comp is given, then it must be a function that receives two table elements, and returns true when the first 
		 * is less than the second (so that not comp(a[i+1],a[i]) will be true after the sort). If comp is not given, 
		 * then the standard Lua operator &lt; is used instead.
		 *
		 * The sort algorithm is not stable; that is, elements considered equal by the given order may have their relative positions changed by the sort.
		 */ 
		case SORT: { 
			LTable table = vm.checktable(1);
			LValue compare = (vm.isnoneornil(2)? (LValue) LNil.NIL: (LValue) vm.checkfunction(2));
			table.luaSort( vm, compare );
			return 0;
		}

		default:
			LuaState.vmerror( "bad table id" );
			return 0;
		}
	}
}
