/*******************************************************************************
* Copyright (c) 2010 Luaj.org. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;


public class PackageLib extends OneArgFunction {

	public static String DEFAULT_LUA_PATH = "?.lua";

	public InputStream    STDIN   = null;
	public PrintStream    STDOUT  = System.out;
	public LuaTable       LOADED;
	public LuaTable       PACKAGE;

	/** Most recent instance of PackageLib */
	public static PackageLib instance;
	
	/** Loader that loads from preload table if found there */
	public LuaValue preload_loader;
	
	/** Loader that loads as a lua script using the LUA_PATH */
	public LuaValue lua_loader;
	
	/** Loader that loads as a Java class.  Class must have public constructor and be a LuaValue */
	public LuaValue java_loader;
	
	private static final LuaString _M         = valueOf("_M");
	private static final LuaString _NAME      = valueOf("_NAME");	
	private static final LuaString _PACKAGE   = valueOf("_PACKAGE");	
	private static final LuaString _DOT       = valueOf(".");
	private static final LuaString _LOADERS   = valueOf("loaders");
	private static final LuaString _LOADED    = valueOf("loaded");
	private static final LuaString _LOADLIB   = valueOf("loadlib");
	private static final LuaString _PRELOAD   = valueOf("preload");
	private static final LuaString _PATH      = valueOf("path");
	private static final LuaString _SEEALL    = valueOf("seeall");
	private static final LuaString _SENTINEL  = valueOf("\u0001");
	
	private static final int OP_MODULE         = 0;
	private static final int OP_REQUIRE        = 1;
	private static final int OP_LOADLIB        = 2;
	private static final int OP_SEEALL         = 3;
	private static final int OP_PRELOAD_LOADER = 4;
	private static final int OP_LUA_LOADER     = 5;
	private static final int OP_JAVA_LOADER    = 6;
	
	public PackageLib() {
		instance = this;
	}

	public LuaValue call(LuaValue arg) {
		env.set("require", new PkgLib1(env,"require",OP_REQUIRE,this));
		env.set("module",  new PkgLibV(env,"module",OP_MODULE,this));
		env.set( "package", PACKAGE=tableOf( new LuaValue[] {
				_LOADED,  LOADED=tableOf(),
				_PRELOAD, tableOf(),
				_PATH,    valueOf(DEFAULT_LUA_PATH),
				_LOADLIB, new PkgLibV(env,"loadlib",OP_LOADLIB,this),
				_SEEALL,  new PkgLib1(env,"seeall",OP_SEEALL,this),
				_LOADERS, listOf(new LuaValue[] {
						preload_loader = new PkgLibV(env,"preload_loader", OP_PRELOAD_LOADER,this),
						lua_loader     = new PkgLibV(env,"lua_loader", OP_LUA_LOADER,this),
						java_loader    = new PkgLibV(env,"java_loader", OP_JAVA_LOADER,this),
				}) }) );
		return env;
	}

	public static final class PkgLib1 extends OneArgFunction {
		PackageLib lib;
		public PkgLib1(LuaValue env,String name, int opcode, PackageLib lib) {
			this.env = env;
			this.name = name;
			this.opcode = opcode;
			this.lib = lib;
		}
		public LuaValue call(LuaValue arg) {
			switch ( opcode ) {
			case OP_REQUIRE: 
				return lib.require(arg);
			case OP_SEEALL: { 
				LuaTable t = arg.checktable();
				LuaValue m = t.getmetatable();
				if ( m == null )
					t.setmetatable(m=tableOf());
				m.set( INDEX, LuaThread.getGlobals() );
				return NONE;
			}
			}
			return NIL;
		}
	}

	public static final class PkgLibV extends VarArgFunction {
		PackageLib lib;
		public PkgLibV(LuaValue env,String name, int opcode, PackageLib lib) {
			this.env = env;
			this.name = name;
			this.opcode = opcode;
			this.lib = lib;
		}
		public Varargs invoke(Varargs args) {
			switch ( opcode ) {
			case OP_MODULE: 
				return lib.module(args);
			case OP_LOADLIB: 
				return loadlib(args);
			case OP_PRELOAD_LOADER: {
				return lib.loader_preload(args);
			}
			case OP_LUA_LOADER: {
				return lib.loader_Lua(args);
			}
			case OP_JAVA_LOADER: {
				return lib.loader_Java(args);
			}
			}
			return NONE;
		}
	}
	
	/** Allow packages to mark themselves as loaded */
	public void setIsLoaded(String name, LuaTable value) {
		LOADED.set(name, value);
	}

	public void setLuaPath( String newLuaPath ) {
		PACKAGE.set( _PATH, valueOf(newLuaPath) );
	}
	
	public String tojstring() {
		return "package";
	}
	
	
	// ======================== Module, Package loading =============================
	/**
	 * module (name [, ...])
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
	public Varargs module(Varargs args) {
		LuaString modname = args.checkstring(1);
		int n = args.narg();
		LuaValue value = LOADED.get(modname);
		LuaValue module;
		if ( ! value.istable() ) { /* not found? */
			
		    /* try global variable (and create one if it does not exist) */
			LuaValue globals = LuaThread.getGlobals();
			module = findtable( globals, modname );
			if ( module == null )
				error( "name conflict for module '"+modname+"'" );
			LOADED.set(modname, module);
		} else {
			module = (LuaTable) value;
		}
		
		
		/* check whether table already has a _NAME field */
		LuaValue name = module.get(_NAME);
		if ( name.isnil() ) {
			modinit( module, modname );
		}
		
		// set the environment of the current function
		LuaFunction f = LuaThread.getCallstackFunction(1);
		if ( f == null )
			error(1, "no calling function");
		f.setfenv(module);
		
		// apply the functions
		for ( int i=2; i<=n; i++ )
			args.arg(i).call( module );
		
		// returns no results
		return NONE;
	}

	/**
	 * 
	 * @param table the table at which to start the search
	 * @param fname the name to look up or create, such as "abc.def.ghi"
	 * @return the table for that name, possible a new one, or null if a non-table has that name already. 
	 */
	private static final LuaValue findtable(LuaValue table, LuaString fname) {
		int b, e=(-1);
		do {
			e = fname.indexOf(_DOT, b=e+1 );
			if ( e < 0 )
				e = fname.m_length;
			LuaString key = fname.substring(b, e);
			LuaValue val = table.rawget(key);
			if ( val.isnil() ) { /* no such field? */
				LuaTable field = new LuaTable(); /* new table for field */
				table.set(key, field);
				table = field;
			} else if ( ! val.istable() ) {  /* field has a non-table value? */
				return null;
			} else {
				table = val;
			}
		} while ( e < fname.m_length );
		return table;
	}

	private static final void modinit(LuaValue module, LuaString modname) {
		/* module._M = module */
		module.set(_M, module);
		int e = modname.lastIndexOf(_DOT);
		module.set(_NAME, modname );
		module.set(_PACKAGE, (e<0? EMPTYSTRING: modname.substring(0,e+1)) );
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
	public LuaValue require( LuaValue arg ) {
		LuaString name = arg.checkstring();
		LuaValue loaded = LOADED.get(name);
		if ( loaded.toboolean() ) {
			if ( loaded == _SENTINEL )
				error("loop or previous error loading module '"+name+"'");
			return loaded;
		}

		/* else must load it; iterate over available loaders */
		LuaTable tbl = PACKAGE.get(_LOADERS).checktable();
		StringBuffer sb = new StringBuffer();
		LuaValue chunk = null;
		for ( int i=1; true; i++ ) {
			LuaValue loader = tbl.get(i);
			if ( loader.isnil() ) {
				error( "module '"+name+"' not found: "+name+"\n"+sb );				
		    }
						
		    /* call loader with module name as argument */
			chunk = loader.call(name);
			if ( chunk.isfunction() )
				break;
			if ( chunk.isstring() )
				sb.append( chunk.tojstring() );
		}

		// load the module using the loader
		LOADED.set(name, _SENTINEL);
		LuaValue result = chunk.call(name);
		if ( ! result.isnil() )
			LOADED.set( name, result );
		else if ( (result = LOADED.get(name)) == _SENTINEL ) 
			LOADED.set( name, result = LuaValue.TRUE );
		return result;
	}

	public static Varargs loadlib( Varargs args ) {
		return varargsOf(NIL, valueOf("dynamic libraries not enabled"), valueOf("absent"));
	}

	LuaValue loader_preload( Varargs args ) {
		LuaString name = args.checkstring(1);
		LuaValue preload = PACKAGE.get(_PRELOAD).checktable();
		LuaValue val = preload.get(name);
		return val.isnil()? 
			valueOf("\n\tno field package.preload['"+name+"']"):
			val;
	}

	LuaValue loader_Lua( Varargs args ) {
		String name = args.checkjstring(1);
		InputStream is = null;
		
		
		// get package path
		LuaValue pp = PACKAGE.get(_PATH);
		if ( ! pp.isstring() ) 
			return valueOf("package.path is not a string");
		String path = pp.tojstring();
		
		// check the path elements
		int e = -1;
		int n = path.length();
		StringBuffer sb = null;
		name = name.replace('.','/');
		while ( e < n ) {
			
			// find next template
			int b = e+1;
			e = path.indexOf(';',b);
			if ( e < 0 )
				e = path.length();
			String template = path.substring(b,e);

			// create filename
			int q = template.indexOf('?');
			String filename = template;
			if ( q >= 0 ) {
				filename = template.substring(0,q) + name + template.substring(q+1);
			}
			
			// try loading the file
			Varargs v; 
			try {
				v = BaseLib.loadFile(filename);
			} catch ( IOException ioe ) {
				v = varargsOf(NIL, valueOf(ioe.getMessage()));
				
			}
			if ( v.arg1().isfunction() )
				return v.arg1();
			
			// report error
			if ( sb == null )
				sb = new StringBuffer();
			sb.append( "\n\t'"+filename+"': "+v.arg(2) );
		}
		return valueOf(sb.toString());
	}
	
	LuaValue loader_Java( Varargs args ) {
		String name = args.checkjstring(1);
		String classname = toClassname( name );
		Class c = null;
		LuaValue v = null;
		try {
			c = Class.forName(classname);
			v = (LuaValue) c.newInstance();
			v.setfenv(env);
			return v;
		} catch ( ClassNotFoundException  cnfe ) {
			return valueOf("\n\tno class '"+classname+"'" );
		} catch ( Exception e ) {
			return valueOf("\n\tjava load failed on '"+classname+"', "+e );
		}
	}
	
	/** Convert lua filename to valid class name */
	public static final String toClassname( String filename ) {
		int n=filename.length();
		int j=n;
		if ( filename.endsWith(".lua") )
			j -= 4;
		for ( int k=0; k<j; k++ ) {
			char c = filename.charAt(k);
			if ( (!isClassnamePart(c)) || (c=='/') || (c=='\\') ) {
				StringBuffer sb = new StringBuffer(j);
				for ( int i=0; i<j; i++ ) {
					c = filename.charAt(i);
					sb.append( 
							 (isClassnamePart(c))? c:
							 ((c=='/') || (c=='\\'))? '.': '_' ); 
				}
				return sb.toString();
			}
		}
		return n==j? filename: filename.substring(0,j);
	}
	
	private static final boolean isClassnamePart(char c) {
		if ( (c>='a'&&c<='z') || (c>='A'&&c<='Z') || (c>='0'&&c<='9') )
			return true;
		switch ( c ) {
		case '.':
		case '$':
		case '_':
			return true;
		default:
			return false;
		}
	}	
}
