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

import java.io.InputStream;
import java.io.PrintStream;

import org.luaj.vm.CallInfo;
import org.luaj.vm.LBoolean;
import org.luaj.vm.LFunction;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaState;


public class PackageLib extends LFunction {

	public static InputStream STDIN = null;
	public static PrintStream STDOUT = System.out;
	public static LTable      LOADED = new LTable();

	private static final LString _M = new LString("_M");
	private static final LString _NAME = new LString("_NAME");	
	private static final LString _PACKAGE = new LString("_PACKAGE");	
	private static final LString _DOT = new LString(".");
	private static final LString _EMPTY = new LString("");
	private static final LString __INDEX = new LString("__index");
	
	private static final String[] NAMES = {
		"package",
		"module",
		"require",
		"loadlib",
		"seeall",
	};
	
	private static final int INSTALL        = 0;
	private static final int MODULE         = 1;
	private static final int REQUIRE        = 2;
	private static final int LOADLIB        = 3;
	private static final int SEEALL         = 4;
	
	
	public static void install( LTable globals ) {
		for ( int i=1; i<LOADLIB; i++ )
			globals.put( NAMES[i], new PackageLib(i) );
		LTable pckg = new LTable();
		for ( int i=LOADLIB; i<NAMES.length; i++ )
			pckg.put( NAMES[i], new PackageLib(i) );
		globals.put( "package", pckg );
		pckg.put( "loaded", LOADED );	
	}
	
	private final int id;

	private PackageLib( int id ) {
		this.id = id;
	}
	
	public String toString() {
		return NAMES[id]+"()";
	}
	
	public boolean luaStackCall( LuaState vm ) {
		switch ( id ) {
		case INSTALL:
			install(vm._G);
			break;
		case MODULE: 
			module(vm);
			break;
		case REQUIRE: 
			require(vm);
			break;
		case LOADLIB: 
			loadlib(vm);
			break;
		case SEEALL: { 
			if ( ! vm.istable(2) )
				vm.error( "table expected, got "+vm.typename(2) );
			LTable t = vm.totable(2);
			LTable m = t.luaGetMetatable();
			if ( m == null )
				t.luaSetMetatable(m = new LTable());
			m.put(__INDEX, vm._G);
			vm.resettop();
			break;
		}
		default:
			luaUnsupportedOperation();
		}
		return false;
	}
	
	
	// ======================== Module, Package loading =============================
	/**
	 * module (name [, ···])
	 * 
	 * Creates a module. If there is a table in package.loaded[name], this table
	 * is the module. Otherwise, if there is a global table t with the given
	 * name, this table is the module. Otherwise creates a new table t and sets
	 * it as the value of the global name and the value of package.loaded[name].
	 * This function also initializes t._NAME with the given name, t._M with the
	 * module (t itself), and t._PACKAGE with the package name (the full module
	 * name minus last component; see below). Finally, module sets t as the new
	 * environment of the current function and the new value of
	 * package.loaded[name], so that require returns t.
	 * 
	 * If name is a compound name (that is, one with components separated by
	 * dots), module creates (or reuses, if they already exist) tables for each
	 * component. For instance, if name is a.b.c, then module stores the module
	 * table in field c of field b of global a.
	 * 
	 * This function may receive optional options after the module name, where
	 * each option is a function to be applied over the module.
	 */
	public static void module(LuaState vm) {
		LString modname = vm.tolstring(2);
		int n = vm.gettop();
		LValue value = LOADED.get(modname);
		LTable module;
		if ( value.luaGetType() != Lua.LUA_TTABLE ) { /* not found? */
			
		    /* try global variable (and create one if it does not exist) */
			module = findtable( vm._G, modname );
			if ( module == null )
				vm.error( "name conflict for module '"+modname+"'", 2 );
			LOADED.luaSetTable(vm, LOADED, modname, module);
		} else {
			module = (LTable) value;
		}
		
		
		/* check whether table already has a _NAME field */
		module.luaGetTable(vm, module, _NAME);
		if ( vm.isnil(-1) ) {
			modinit( vm, module, modname );
		}
		
		// set the environment of the current function
		CallInfo ci = vm.getStackFrame(0);
		ci.closure.env = module;
		
		// apply the functions
		for ( int i=3; i<=n; i++ ) {
			vm.pushvalue( i );   /* get option (a function) */
			vm.pushlvalue( module );  /* module */
			vm.call( 1, 0 );
		}
		
		// returns no results
		vm.resettop();
	}

	/**
	 * 
	 * @param table the table at which to start the search
	 * @param fname the name to look up or create, such as "abc.def.ghi"
	 * @return the table for that name, possible a new one, or null if a non-table has that name already. 
	 */
	private static LTable findtable(LTable table, LString fname) {
		int b, e=(-1);
		do {
			e = fname.indexOf(_DOT, b=e+1 );
			if ( e < 0 )
				e = fname.m_length;
			LString key = fname.substring(b, e);
			LValue val = table.get(key);
			if ( val == LNil.NIL ) { /* no such field? */
				LTable field = new LTable(); /* new table for field */
				table.put(key, field);
				table = field;
			} else if ( val.luaGetType() != Lua.LUA_TTABLE ) {  /* field has a non-table value? */
				return null;
			} else {
				table = (LTable) val;
			}
		} while ( e < fname.m_length );
		return table;
	}

	private static void modinit(LuaState vm, LTable module, LString modname) {
		/* module._M = module */
		module.luaSetTable(vm, module, _M, module);
		int e = modname.lastIndexOf(_DOT);
		module.luaSetTable(vm, module, _NAME, modname );
		module.luaSetTable(vm, module, _PACKAGE, (e<0? _EMPTY: modname.substring(0,e+1)) );
	}

	/** 
	 * require (modname)
	 * 
	 * Loads the given module. The function starts by looking into the package.loaded table to 
	 * determine whether modname is already loaded. If it is, then require returns the value 
	 * stored at package.loaded[modname]. Otherwise, it tries to find a loader for the module.
	 * 
	 * To find a loader, require is guided by the package.loaders array. By changing this array, 
	 * we can change how require looks for a module. The following explanation is based on the 
	 * default configuration for package.loaders.
	 *  
	 * First require queries package.preload[modname]. If it has a value, this value 
	 * (which should be a function) is the loader. Otherwise require searches for a Lua loader 
	 * using the path stored in package.path. If that also fails, it searches for a C loader 
	 * using the path stored in package.cpath. If that also fails, it tries an all-in-one loader 
	 * (see package.loaders).
	 * 
	 * Once a loader is found, require calls the loader with a single argument, modname. 
	 * If the loader returns any value, require assigns the returned value to package.loaded[modname]. 
	 * If the loader returns no value and has not assigned any value to package.loaded[modname], 
	 * then require assigns true to this entry. In any case, require returns the final value of 
	 * package.loaded[modname]. 
	 * 
	 * If there is any error loading or running the module, or if it cannot find any loader for 
	 * the module, then require signals an error.
	 */	
	public static void require( LuaState vm ) {
		LString modname = vm.tolstring(2);
		if ( LOADED.containsKey(modname) ) {
			vm.resettop();
			vm.pushlvalue( LOADED.get(modname) );
		}
		else {
			String s = modname.toJavaString();
			if ( ! BaseLib.loadfile(vm, s+".luac") && ! BaseLib.loadfile(vm, s+".lua") )
				vm.error( "not found: "+s );
			else if ( 0 == vm.pcall(0, 1, 0) ) {
				LValue result = vm.topointer( -1 ); 
				if ( result != LNil.NIL )
					LOADED.put(modname, result);
				else if ( ! LOADED.containsKey(modname) )
					LOADED.put(modname, result = LBoolean.TRUE);
				vm.resettop();
				vm.pushlvalue( result );
			}
		}
	}

	public static void loadlib( LuaState vm ) {
		vm.error( "loadlib not implemented" );
	}
}
