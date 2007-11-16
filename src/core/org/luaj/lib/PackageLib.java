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

import org.luaj.vm.LBoolean;
import org.luaj.vm.LFunction;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;


public class PackageLib extends LFunction {

	public static InputStream STDIN = null;
	public static PrintStream STDOUT = System.out;
	public static LTable      LOADED = new LTable();
	
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
		case SEEALL: 
			seeall(vm);
			break;
		default:
			luaUnsupportedOperation();
		}
		return false;
	}
	
	
	// ======================== Module, Package loading =============================
	
	public static void module( LuaState vm ) {		
		vm.error( "module not implemented" );
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
	
	public static void seeall( LuaState vm ) {
		vm.error( "seeall not implemented" );
	}
}
