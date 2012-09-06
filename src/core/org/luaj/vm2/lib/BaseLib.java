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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.luaj.vm2.LoadState;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/** 
 * Subclass of {@link LibFunction} which implements the lua basic library functions. 
 * <p>
 * This contains all library functions listed as "basic functions" in the lua documentation for JME. 
 * The functions dofile and loadfile use the 
 * {@link #FINDER} instance to find resource files.
 * Since JME has no file system by default, {@link BaseLib} implements 
 * {@link ResourceFinder} using {@link Class#getResource(String)}, 
 * which is the closest equivalent on JME.     
 * The default loader chain in {@link PackageLib} will use these as well.
 * <p>  
 * To use basic library functions that include a {@link ResourceFinder} based on 
 * directory lookup, use {@link JseBaseLib} instead. 
 * <p>
 * Typically, this library is included as part of a call to either 
 * {@link JmePlatform#standardGlobals()}
 * <p>
 * To instantiate and use it directly, 
 * link it into your globals table via {@link LuaValue#load(LuaValue)} using code such as:
 * <pre> {@code
 * LuaTable _G = new LuaTable();
 * LuaThread.setGlobals(_G);
 * _G.load(new BaseLib());
 * _G.get("print").call(LuaValue.valueOf("hello, world"));
 * } </pre>
 * Doing so will ensure the library is properly initialized 
 * and loaded into the globals table. 
 * <p>
 * This is a direct port of the corresponding library in C.
 * @see JseBaseLib
 * @see ResourceFinder
 * @see #FINDER
 * @see LibFunction
 * @see JsePlatform
 * @see JmePlatform
 * @see <a href="http://www.lua.org/manual/5.1/manual.html#5.1">http://www.lua.org/manual/5.1/manual.html#5.1</a>
 */
public class BaseLib extends OneArgFunction implements ResourceFinder {
	
	public static BaseLib instance;
	
	public InputStream STDIN  = null;
	public PrintStream STDOUT = System.out;
	public PrintStream STDERR = System.err;

	/** 
	 * Singleton file opener for this Java ClassLoader realm.
	 * 
	 * Unless set or changed elsewhere, will be set by the BaseLib that is created.
	 */
	public static ResourceFinder FINDER;
	
	private LuaValue next;
	private LuaValue inext;
	
	private static final String[] LIB1_KEYS = {
		"getmetatable", // ( object ) -> table 
		"rawlen", // (v) -> value
		"tostring", // (e) -> value
		"type",  // (v) -> value
	};
	private static final String[] LIB2_KEYS = {
		"collectgarbage", // ( opt [,arg] ) -> value
		"error", // ( message [,level] ) -> ERR
		"rawequal", // (v1, v2) -> boolean
		"rawget", // (table, index) -> value
	};
	private static final String[] LIBV_KEYS = {
		"assert", // ( v [,message] ) -> v, message | ERR
		"dofile", // ( filename ) -> result1, ...
		"load", // ( ld [, source [, mode [, env]]] ) -> chunk | nil, msg
		"loadfile", // ( [filename [, mode [, env]]] ) -> chunk | nil, msg
		"pcall", // (f, arg1, ...) -> status, result1, ...
		"xpcall", // (f, err) -> result1, ...
		"print", // (...) -> void
		"select", // (f, ...) -> value1, ...
		"rawset", // (table, index, value) -> table
		"setmetatable", // (table, metatable) -> table
		"tonumber", // (e [,base]) -> value
		"pairs", // "pairs" (t) -> iter-func, t, nil
		"ipairs", // "ipairs", // (t) -> iter-func, t, 0
		"next", // "next"  ( table, [index] ) -> next-index, next-value
		"__inext", // "inext" ( table, [int-index] ) -> next-index, next-value
	};
	
	/**
	 * Construct a base libarary instance.
	 */
	public BaseLib() {
		instance = this;
	}
	
	public LuaValue call(LuaValue arg) {
		env.set( "_G", env );
		env.set( "_VERSION", Lua._VERSION );
		bind( env, BaseLib1.class, LIB1_KEYS );
		bind( env, BaseLib2.class, LIB2_KEYS );
		bind( env, BaseLibV.class, LIBV_KEYS ); 
		
		// remember next, and inext for use in pairs and ipairs
		next = env.get("next");
		inext = env.get("__inext");
		
		// inject base lib int vararg instances
		for ( int i=0; i<LIBV_KEYS.length; i++ ) 
			((BaseLibV) env.get(LIBV_KEYS[i])).baselib = this;
		
		// set the default resource finder if not set already
		if ( FINDER == null )
			FINDER = this;
		return env;
	}

	/** ResourceFinder implementation 
	 * 
	 * Tries to open the file as a resource, which can work for . 
	 */
	public InputStream findResource(String filename) {
		Class c = getClass();
		return c.getResourceAsStream(filename.startsWith("/")? filename: "/"+filename);
	}

	static final class BaseLib1 extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			switch ( opcode ) {
			case 0: // "getmetatable", // ( object ) -> table 
			{
				LuaValue mt = arg.getmetatable();
				return mt!=null? mt.rawget(METATABLE).optvalue(mt): NIL;
			}
			case 1: // "rawlen", // (v) -> value
			{
				return valueOf(arg.rawlen());
			}
			case 2: // "tostring", // (e) -> value
			{
				LuaValue h = arg.metatag(TOSTRING);
				if ( ! h.isnil() ) 
					return h.call(arg);
				LuaValue v = arg.tostring();
				if ( ! v.isnil() ) 
					return v;
				return valueOf(arg.tojstring());
			}
			case 3: // "type",  // (v) -> value
			{
				return valueOf(arg.typename());
			}
			}
			return NIL;
		}
	}
	
	static final class BaseLib2 extends TwoArgFunction {
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			switch ( opcode ) {
			case 0: // "collectgarbage", // ( opt [,arg] ) -> value
				String s = arg1.checkjstring();
				if ( "collect".equals(s) ) {
					System.gc();
					return ZERO;
				} else if ( "count".equals(s) ) {
					Runtime rt = Runtime.getRuntime();
					long used = rt.totalMemory() - rt.freeMemory();
					return valueOf(used/1024.);
				} else if ( "step".equals(s) ) {
					System.gc();
					return LuaValue.TRUE;
				} else {
					this.argerror("gc op");
				}
				return NIL;
			case 1: // "error", // ( message [,level] ) -> ERR
				throw new LuaError( arg1.isnil()? null: arg1.tojstring(), arg2.optint(1) );
			case 2: // "rawequal", // (v1, v2) -> boolean
				return valueOf(arg1.raweq(arg2));
			case 3: // "rawget", // (table, index) -> value
				return arg1.rawget(arg2);
			}
			return NIL;
		}
	}

	static final class BaseLibV extends VarArgFunction {
		public BaseLib baselib;
		public Varargs invoke(Varargs args) {
			switch ( opcode ) {
			case 0: // "assert", // ( v [,message] ) -> v, message | ERR
				if ( !args.arg1().toboolean() ) 
					error( args.narg()>1? args.optjstring(2,"assertion failed!"): "assertion failed!" );
				return args;
			case 1: // "dofile", // ( filename ) -> result1, ...
			{
				args.argcheck(args.isstring(1) || args.isnil(1), 1, "filename must be string or nil");
				String filename = args.isstring(1)? args.tojstring(1): null;
				Varargs v = filename == null? 
						BaseLib.loadStream( baselib.STDIN, "=stdin", "bt", LuaThread.getGlobals() ):
						BaseLib.loadFile( args.checkjstring(1), "bt", LuaThread.getGlobals() );
				return v.isnil(1)? error(v.tojstring(2)): v.arg1().invoke();
			}
			case 2: // "load", // ( ld [, source [, mode [, env]]] ) -> chunk | nil, msg
			{
				LuaValue ld = args.arg1();
				args.argcheck(ld.isstring() || ld.isfunction(), 1, "ld must be string or function");
				String source = args.optjstring(2, ld.isstring()? ld.tojstring(): "=(load)");
				String mode = args.optjstring(3, "bt");
				LuaValue env = args.optvalue(4, LuaThread.getGlobals());
				return BaseLib.loadStream(ld.isstring()? ld.strvalue().toInputStream(): 
					new StringInputStream(ld.checkfunction()), source, mode, env);
			}
			case 3: // "loadfile", // ( [filename [, mode [, env]]] ) -> chunk | nil, msg
			{
				args.argcheck(args.isstring(1) || args.isnil(1), 1, "filename must be string or nil");
				String filename = args.isstring(1)? args.tojstring(1): null;
				String mode = args.optjstring(2, "bt");
				LuaValue env = args.optvalue(3, LuaThread.getGlobals());
				return filename == null? 
					BaseLib.loadStream( baselib.STDIN, "=stdin", mode, env ):
					BaseLib.loadFile( filename, mode, env );
			}
			case 4: // "pcall", // (f, arg1, ...) -> status, result1, ...
			{
				LuaValue func = args.checkvalue(1);
				LuaThread.CallStack cs = LuaThread.onCall(this);
				try {
					return pcall(func,args.subargs(2),null);
				} finally {
					cs.onReturn();
				}
			}
			case 5: // "xpcall", // (f, err) -> result1, ...				
			{
				LuaThread.CallStack cs = LuaThread.onCall(this);
				try {
					return pcall(args.arg1(),NONE,args.checkvalue(2));
				} finally {
					cs.onReturn();
				}
			}
			case 6: // "print", // (...) -> void
			{
				LuaValue tostring = LuaThread.getGlobals().get("tostring"); 
				for ( int i=1, n=args.narg(); i<=n; i++ ) {
					if ( i>1 ) baselib.STDOUT.write( '\t' );
					LuaString s = tostring.call( args.arg(i) ).strvalue();
					int z = s.indexOf((byte)0, 0);
					baselib.STDOUT.write( s.m_bytes, s.m_offset, z>=0? z: s.m_length );
				}
				baselib.STDOUT.println();
				return NONE;
			}
			case 7: // "select", // (f, ...) -> value1, ...
			{
				int n = args.narg()-1; 				
				if ( args.arg1().equals(valueOf("#")) )
					return valueOf(n);
				int i = args.checkint(1);
				if ( i == 0 || i < -n )
					argerror(1,"index out of range");
				return args.subargs(i<0? n+i+2: i+1);
			}
			case 8: { // "rawset", // (table, index, value) -> table
				LuaTable t = args.checktable(1);
				t.rawset(args.checknotnil(2), args.checkvalue(3));
				return t;
			}
			case 9: { // "setmetatable", // (table, metatable) -> table
				final LuaValue t = args.arg1();
				final LuaValue mt0 = t.getmetatable();
				if ( mt0!=null && !mt0.rawget(METATABLE).isnil() )
					error("cannot change a protected metatable");
				final LuaValue mt = args.checkvalue(2);
				return t.setmetatable(mt.isnil()? null: mt.checktable());
			}
			case 10: { // "tonumber", // (e [,base]) -> value
				LuaValue arg1 = args.checkvalue(1);
				final int base = args.optint(2,10);
				if (base == 10) {  /* standard conversion */
					return arg1.tonumber();
				} else {
					if ( base < 2 || base > 36 )
						argerror(2, "base out of range");
					return arg1.checkstring().tonumber(base);
				}
			}
			case 11: // "pairs" (t) -> iter-func, t, nil
				return varargsOf( baselib.next, args.checktable(1), NIL );
			case 12: // "ipairs", // (t) -> iter-func, t, 0
				return varargsOf( baselib.inext, args.checktable(1), ZERO );
			case 13: // "next"  ( table, [index] ) -> next-index, next-value
				return args.checktable(1).next(args.arg(2));
			case 14: // "inext" ( table, [int-index] ) -> next-index, next-value
				return args.checktable(1).inext(args.arg(2));
			}
			return NONE;
		}
	}

	public static Varargs pcall(LuaValue func, Varargs args, LuaValue errfunc) {
		LuaValue olderr = LuaThread.setErrorFunc(errfunc);
		try {
			Varargs result =  varargsOf(LuaValue.TRUE, func.invoke(args));
			LuaThread.setErrorFunc(olderr);
			return result;
		} catch ( LuaError le ) {
			LuaThread.setErrorFunc(olderr);
			String m = le.getMessage();
			return varargsOf(FALSE, m!=null? valueOf(m): NIL);
		} catch ( Exception e ) {
			LuaThread.setErrorFunc(olderr);
			String m = e.getMessage();
			return varargsOf(FALSE, valueOf(m!=null? m: e.toString()));
		}
	}
	
	/** 
	 * Load from a named file, returning the chunk or nil,error of can't load
	 * @param env 
	 * @param mode 
	 * @return Varargs containing chunk, or NIL,error-text on error
	 */
	public static Varargs loadFile(String filename, String mode, LuaValue env) {
		InputStream is = FINDER.findResource(filename);
		if ( is == null )
			return varargsOf(NIL, valueOf("cannot open "+filename+": No such file or directory"));
		try {
			return loadStream(is, "@"+filename, mode, env);
		} finally {
			try {
				is.close();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	public static Varargs loadStream(InputStream is, String chunkname, String mode, LuaValue env) {
		try {
			if ( is == null )
				return varargsOf(NIL, valueOf("not found: "+chunkname));
			return LoadState.load(is, chunkname, mode, env);
		} catch (Exception e) {
			return varargsOf(NIL, valueOf(e.getMessage()));
		}
	}
	
	
	private static class StringInputStream extends InputStream {
		final LuaValue func;
		byte[] bytes; 
		int offset, remaining = 0;
		StringInputStream(LuaValue func) {
			this.func = func;
		}
		public int read() throws IOException {
			if ( remaining <= 0 ) {
				LuaValue s = func.call();
				if ( s.isnil() )
					return -1;
				LuaString ls = s.strvalue();
				bytes = ls.m_bytes;
				offset = ls.m_offset;
				remaining = ls.m_length;
				if (remaining <= 0)
					return -1;
			}
			--remaining;
			return bytes[offset++];
		}
	}
}
