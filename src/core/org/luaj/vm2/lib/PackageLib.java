/*******************************************************************************
* Copyright (c) 2010-2011 Luaj.org. All rights reserved.
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

import java.io.InputStream;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/** 
 * Subclass of {@link LibFunction} which implements the lua standard package and module 
 * library functions. 
 * 
 * <p>
 * Typically, this library is included as part of a call to either 
 * {@link JsePlatform#standardGlobals()} or {@link JmePlatform#standardGlobals()}
 * <p>
 * To instantiate and use it directly, 
 * link it into your globals table via {@link LuaValue#load(LuaValue)} using code such as:
 * <pre> {@code
 * LuaTable _G = new LuaTable();
 * LuaThread.setGlobals(_G);
 * _G.load(new BaseLib());
 * _G.load(new PackageLib());
 * System.out.println( _G.get("require").call(LuaValue.valueOf("hyperbolic")) );
 * } </pre>
 * In practice, the first 4 lines of the above are minimal requirements to get 
 * and initialize a globals table capable of basic reqire, print, and other functions, 
 * so it is much more convenient to use the {@link JsePlatform} and {@link JmePlatform} 
 * utility classes instead.  
 * <p>
 * This has been implemented to match as closely as possible the behavior in the corresponding library in C.
 * However, the default filesystem search semantics are different and delegated to the bas library 
 * as outlined in the {@link BaseLib} and {@link JseBaseLib} documetnation. 
 * @see LibFunction
 * @see BaseLib
 * @see JseBaseLib
 * @see JsePlatform
 * @see JmePlatform
 * @see <a href="http://www.lua.org/manual/5.1/manual.html#5.3">http://www.lua.org/manual/5.1/manual.html#5.3</a>
 */
public class PackageLib extends OneArgFunction {

	public static String DEFAULT_LUA_PATH = "?.lua";

	Globals globals;
	
	public LuaTable       LOADED;
	public LuaTable       PACKAGE;

	/** Loader that loads from preload table if found there */
	public LuaValue preload_searcher;
	
	/** Loader that loads as a lua script using the LUA_PATH */
	public LuaValue lua_searcher;
	
	/** Loader that loads as a Java class.  Class must have public constructor and be a LuaValue */
	public LuaValue java_searcher;
	
	private static final LuaString _LOADED     = valueOf("loaded");
	private static final LuaString _LOADLIB    = valueOf("loadlib");
	private static final LuaString _PRELOAD    = valueOf("preload");
	private static final LuaString _PATH       = valueOf("path");
	private static final LuaString _SEARCHERS  = valueOf("searchers");
	private static final LuaString _SEARCHPATH = valueOf("searchpath");
	private static final LuaString _SENTINEL   = valueOf("\u0001");
	
	private static final int OP_REQUIRE          = 0;
	private static final int OP_LOADLIB          = 1;
	private static final int OP_SEARCHPATH       = 2;
	private static final int OP_PRELOAD_SEARCHER = 3;
	private static final int OP_LUA_SEARCHER     = 4;
	private static final int OP_JAVA_SEARCHER    = 5;

	private static final String FILE_SEP = System.getProperty("file.separator");

	public PackageLib() {}

	public LuaValue call(LuaValue env) {
		globals = env.checkglobals();
		env.set("require", new PkgLib1("require",OP_REQUIRE,this));
		env.set( "package", PACKAGE=tableOf( new LuaValue[] {
				_LOADED,  LOADED=tableOf(),
				_PRELOAD, tableOf(),
				_PATH,    valueOf(DEFAULT_LUA_PATH),
				_LOADLIB, new PkgLibV("loadlib",OP_LOADLIB,this),
				_SEARCHPATH,  new PkgLibV("searchpath",OP_SEARCHPATH,this),
				_SEARCHERS, listOf(new LuaValue[] {
						preload_searcher = new PkgLibV("preload_searcher",OP_PRELOAD_SEARCHER, this),
						lua_searcher     = new PkgLibV("lua_searcher",OP_LUA_SEARCHER, this),
						java_searcher    = new PkgLibV("java_searcher",OP_JAVA_SEARCHER, this),
				}) }) );
		LOADED.set("package", PACKAGE);
		return env;
	}

	static final class PkgLib1 extends OneArgFunction {
		PackageLib lib;
		public PkgLib1(String name, int opcode, PackageLib lib) {
			this.name = name;
			this.opcode = opcode;
			this.lib = lib;
		}
		public LuaValue call(LuaValue arg) {
			switch ( opcode ) {
			case OP_REQUIRE: 
				return lib.require(arg);
			}
			return NIL;
		}
	}

	static final class PkgLibV extends VarArgFunction {
		PackageLib lib;
		public PkgLibV(String name,int opcode, PackageLib lib) {
			this.name = name;
			this.opcode = opcode;
			this.lib = lib;
		}
		public Varargs invoke(Varargs args) {
			switch ( opcode ) {
			case OP_LOADLIB: {
				return loadlib(args);
			}
			case OP_PRELOAD_SEARCHER: {
				return lib.searcher_preload(args);
			}
			case OP_LUA_SEARCHER: {
				return lib.searcher_Lua(args);
			}
			case OP_JAVA_SEARCHER: {
				return lib.searcher_Java(args);
			}
			case OP_SEARCHPATH: {
				String name = args.checkjstring(1);
				String path = args.checkjstring(2);
				String sep = args.optjstring(3, ".");
				String rep = args.optjstring(4, FILE_SEP);
				return lib.searchpath(name, path, sep, rep);
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
	
	
	// ======================== Package loading =============================

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
		LuaTable tbl = PACKAGE.get(_SEARCHERS).checktable();
		StringBuffer sb = new StringBuffer();
		LuaValue chunk = null;
		for ( int i=1; true; i++ ) {
			LuaValue loader = tbl.get(i);
			if ( loader.isnil() ) {
				error( "module '"+name+"' not found: "+name+sb );				
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
		args.checkstring(1);
		return varargsOf(NIL, valueOf("dynamic libraries not enabled"), valueOf("absent"));
	}

	LuaValue searcher_preload( Varargs args ) {
		LuaString name = args.checkstring(1);
		LuaValue preload = PACKAGE.get(_PRELOAD).checktable();
		LuaValue val = preload.get(name);
		return val.isnil()? 
			valueOf("\n\tno field package.preload['"+name+"']"):
			val;
	}

	Varargs searcher_Lua( Varargs args ) {
		LuaString name = args.checkstring(1);
		InputStream is = null;
				
		// get package path
		LuaValue path = PACKAGE.get(_PATH);
		if ( ! path.isstring() ) 
			return valueOf("package.path is not a string");

		// get the searchpath function. 	
		LuaValue searchpath = PACKAGE.get(_SEARCHPATH);
		Varargs v = searchpath.invoke(varargsOf(name, path));
		
		// Didd we get a result?
		if (!v.isstring(1))
			return v.arg(2).tostring();
		LuaString filename = v.arg1().strvalue();

		// Try to load the file.
		v = globals.loadFile(filename.tojstring()); 
		if ( v.arg1().isfunction() )
			return LuaValue.varargsOf(v.arg1(), filename);
		
		// report error
		return varargsOf(NIL, valueOf("'"+filename+"': "+v.arg(2).tojstring()));
	}

	public Varargs searchpath(String name, String path, String sep, String rep) {
		
		// check the path elements
		int e = -1;
		int n = path.length();
		StringBuffer sb = null;
		name = name.replace(sep.charAt(0), rep.charAt(0));
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
			
			// try opening the file
			InputStream is = globals.FINDER.findResource(filename);
			if (is != null) {
				try { is.close(); } catch ( java.io.IOException ioe ) {}
				return valueOf(filename);
			}
			
			// report error
			if ( sb == null )
				sb = new StringBuffer();
			sb.append( "\n\t"+filename );
		}
		return varargsOf(NIL, valueOf(sb.toString()));
	}
	
	LuaValue searcher_Java( Varargs args ) {
		String name = args.checkjstring(1);
		String classname = toClassname( name );
		Class c = null;
		LuaValue v = null;
		try {
			c = Class.forName(classname);
			v = (LuaValue) c.newInstance();
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
