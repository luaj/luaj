/**
 * 
 */
package org.luaj.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.luaj.vm.CallInfo;
import org.luaj.vm.LClosure;
import org.luaj.vm.LFunction;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;



public class BaseLib extends LFunction {

	public static InputStream STDIN = null;
	public static PrintStream STDOUT = System.out;
	
	private static final String[] NAMES = { 
		"base",
		"print", 
		"pairs", 
		"getmetatable", 
		"setmetatable", 
		"type", 
		"pcall", 
		"ipairs", 
		"error",
		"assert",
		"loadfile",
		"tonumber",
		"rawequal",
		"rawget",
		"rawset",
		"getfenv",
		"setfenv",
		"select",
		"collectgarbage",
		"dofile",
		"loadstring",
		"load",
		"tostring",
		"unpack",
		"xpcall",
		"next",
		"_inext", // not public
		};

	private static final int INSTALL        = 0;
	private static final int PRINT          = 1;
	private static final int PAIRS          = 2;
	private static final int GETMETATABLE   = 3;
	private static final int SETMETATABLE   = 4;
	private static final int TYPE           = 5;
	private static final int PCALL          = 6;
	private static final int IPAIRS         = 7;
	private static final int ERROR          = 8;
	private static final int ASSERT         = 9;
	private static final int LOADFILE       = 10;
	private static final int TONUMBER       = 11;
	private static final int RAWEQUAL       = 12;
	private static final int RAWGET         = 13;
	private static final int RAWSET         = 14;
	private static final int GETFENV        = 15;
	private static final int SETFENV        = 16;
	private static final int SELECT         = 17;
	private static final int COLLECTGARBAGE = 18;
	private static final int DOFILE         = 19;
	private static final int LOADSTRING     = 20;
	private static final int LOAD           = 21;
	private static final int TOSTRING       = 22;
	private static final int UNPACK         = 23;
	private static final int XPCALL         = 24;
	private static final int NEXT           = 25;
	private static final int INEXT          = 26;

	private static LFunction next;
	private static LFunction inext;
	
	public static void install(LTable globals) {
		for ( int i=1, n=NAMES.length; i<n; i++ )
			globals.put( NAMES[i], new BaseLib(i) );
		next = new BaseLib(NEXT);
		inext = new BaseLib(INEXT);
		globals.put("_G", globals);
		globals.put("_VERSION", new LString(Lua._VERSION));
	}

	private int id;
	
	private BaseLib( int id ) {			
		this.id = id;
	}

	public String toString() {
		return NAMES[id]+"()";
	}
	
	private static void setResult( LuaState vm, LValue value ) {
		vm.resettop();
		vm.pushlvalue( value );
	}
	
	private static void setErrorResult( LuaState vm, String message ) {
		vm.resettop();
		vm.pushnil();
		vm.pushstring( message );
	}
	
	public int invoke(LuaState vm) {
		switch ( id ) {
		case PRINT: {
			int n = vm.gettop();
			vm.getglobal("tostring");			
			for ( int i=1; i<=n; i++ ) {
				vm.pushvalue(-1);
				vm.pushvalue(i);
				vm.call(1, 1);
				if ( vm.type(-1) != Lua.LUA_TSTRING )
					vm.error( "'tostring' must return a string to 'print'" );				
				if ( i > 1 )
					STDOUT.print( "\t" );
				STDOUT.print( vm.tostring(-1) );
				vm.poplvalue();
			}
			STDOUT.println();
			return 0;
		}
		case IPAIRS: {			
			LTable t = vm.checktable(1);
			vm.pushfunction( inext );
			vm.pushlvalue( t );
			vm.pushinteger( 0 );
			return 3;
		}
		case PAIRS: {
			LTable t = vm.checktable(1);
			vm.pushfunction( next );
			vm.pushlvalue( t );
			vm.pushnil();
			return 3;
		}
		case INEXT: {
			  int i = vm.checkint(2) + 1;
			  LTable t = vm.checktable(1);
			  LValue v = t.get(i);
			  if ( !v.isNil() ) {
				  vm.pushinteger(i);
				  vm.pushlvalue(v);
				  return 2;
			  }
			  return 0;
		}
		case NEXT: {
			LTable t = vm.checktable(1);
			LValue k = vm.topointer(2);
			vm.resettop();
			t.next(vm,k,false);
			return -1;
		}
		case GETMETATABLE: {
			vm.checkany(1);
			if ( ! vm.getmetatable(1) ) {
				vm.pushnil();
				return 1;
			} else {
				vm.getfield(-1,LValue.TM_METATABLE);
				if ( vm.isnil(-1) )
					vm.pop(1);
			}
			return 1;
		}
		case SETMETATABLE: {
			LTable t = vm.checktable(1);
			LValue v = vm.checkany(2);
			vm.argcheck(v.isTable() || v.isNil(), 2, "table or nil expected");
			t = t.luaSetMetatable(v);
			vm.pushlvalue(t);
			return 1;
		}		
		case TYPE: {
			LValue v = vm.checkany(1);
			vm.pushlstring( v.luaGetTypeName() );
			return 1;
		}
		case PCALL: {
			vm.checkany(1);
			int n = vm.gettop();
			int s = vm.pcall( n-1, Lua.LUA_MULTRET );
			if ( s == 0 ) { // success, results are on stack
				vm.pushboolean( true );
				vm.insert( 1 );
				return -1;
			} else { // error, error message is on the stack
				vm.pushboolean( false );
				vm.insert( -2 );
				return 2;
			}
		}
		case XPCALL: {
			LValue errfun = vm.checkany(2);
			vm.settop(1);
			int s = vm.xpcall( 0, Lua.LUA_MULTRET, errfun );
			if ( s == 0 ) { // success, results are on stack
				vm.pushboolean( true );
				vm.insert( 1 );
				return -1;
			} else { // error, error message is on the stack
				vm.pushboolean( false );
				vm.insert( 1 );
				return 2;
			}
		}
		case ERROR: {
			vm.error(vm.optstring(1,null), vm.optint(2,-1));
			return 0;
		}
		case ASSERT: {
			if ( ! vm.toboolean(1) )
				vm.error( vm.optstring(2,"assertion failed!") );
			return -1;
		}
		
		case LOADFILE:
			loadfile(vm, vm.optstring(1,null));
			return -1;
			
		case TONUMBER: {
			int base = vm.optint(2, 10);
			if (base == 10) {  /* standard conversion */
				vm.checkany(1);
				LValue v = vm.tolnumber(1);
				vm.pushlvalue(v);
				return 1;
			} else {
				if ( base < 2 || base > 36 )
					vm.argerror(2, "base out of range");				
				LString s = vm.checklstring(1);
				vm.pushlvalue( s.luaToNumber(base) );
				return 1;
			}
		}
		case RAWEQUAL: {
			LValue a = vm.checkany(1);
			LValue b = vm.checkany(2);
			vm.pushboolean(a == b);
			return 1;
		}	
		case RAWGET: {
			LTable t = vm.checktable(1);
			LValue k = vm.checkany(2);
			vm.pushlvalue( t.get( k ) );
			return 1;
		}
		case RAWSET: {
			LTable t = vm.checktable(1);
			LValue k = vm.checkany(2);
			LValue v = vm.checkany(3);
			t.put( k, v );
			vm.pushlvalue(t);
			return 1;
		}
		case GETFENV: {
			LValue f = getfunc(vm, true);
			vm.pushlvalue(f.luaGetEnv(vm._G));
			return 1;
		}
		case SETFENV: {
			LTable t = vm.checktable(2);
			if ( vm.isnumber(1) && vm.tointeger(1) == 0 ) {
				vm._G = t;
			} else { 
				LValue f = getfunc(vm, false);
				if ( (!(f instanceof LClosure)) || ! f.luaSetEnv(t) )
					vm.error( "'setfenv' cannot change environment of given object" );
				vm.pushlvalue(f);
				return 1;
			}
		}
		case SELECT: {
			vm.checkany(1);
			int n = vm.gettop();			
			if ( vm.isnumber(1) ) {
				int index = vm.tolnumber(1).toJavaInt();
				if ( index < 0 )
					index += n;
				if ( index <= 0 )
					vm.argerror( 1, "index out of range" );
				if ( index >= n )
					return 0;
				else {
					return n-index;
				}					
			} else if ( vm.checkstring(1).equals( "#" ) ) {
				vm.pushnumber( n - 1 );
				return 1;
			} else {
				vm.typerror(1,"expected number or '#'");
				return 0;
			}
		}
		case COLLECTGARBAGE: {
			String s = vm.optstring(1, "collect");
			int result = 0;
			if ( "collect".equals(s) )
				System.gc();
			else if ( "count".equals(s) ) {
				Runtime rt = Runtime.getRuntime();
				long used = rt.totalMemory() - rt.freeMemory();
				result = (int) (used >> 10);
			} else {
				vm.argerror(2,"gc op");
			}
			vm.pushnumber(result);
			return 1;
		}
		case DOFILE:
			dofile(vm);
			return -1;
		case LOADSTRING:
			loadstring(vm, vm.checklstring(1), vm.optstring(2,"(string)"));
			return -1;
		case LOAD:
			load(vm);
			return -1;
		case TOSTRING: {
			LValue v = vm.checkany(1);
			vm.pushlvalue( v.luaAsString() );			
			return 1;
		}
		case UNPACK: {
			LTable list = vm.checktable(1);
			int n = vm.gettop();
			int i = vm.optint(2,1);
			int j;
			if ( n >= 3 ) {
				j = vm.checkint(3);
			} else {
				j = list.luaLength();
			}
			vm.resettop();
			vm.checkstack(j+1-i);
			for ( int k=i; k<=j; k++ )
				vm.pushlvalue(list.get(k));
			return -1;
		}
		default:
			LuaState.vmerror( "bad base id" );
			return 0;
		}
	}
	
	private static LValue getfunc (LuaState vm, boolean opt) {
		if ( vm.isfunction(1) )
			return vm.tofunction(1);
		else {
			int level = opt? vm.optint(1, 1): vm.checkint(1);
		    vm.argcheck(level >= 0, 1, "level must be non-negative");
		    vm.argcheck(level-1 <= vm.cc, 1, "invalid level");
		    CallInfo ci = vm.getStackFrame(level-1);
		    if ( ci == null || ci.closure == null )
		    	return LNil.NIL;
		    return ci.closure;
		}
	}

	
	public static void redirectOutput( OutputStream newStdOut ) {
		STDOUT = new PrintStream( newStdOut );
	}
	
	public static void restoreStandardOutput() {
		STDOUT = System.out;
	}

	// closes the input stream, provided its not null or System.in
	private static void closeSafely(InputStream is) {
		try {
			if ( is != null && is != STDIN )
				is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// closes the output stream, provided its not null or STDOUT 
	private static void closeSafely(OutputStream os) {
		try {
			if ( os != null && os != STDOUT )
				os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// return true if loaded, false if error put onto the stack
	/** Load a chunk from an input stream, leaving it on the stack if successful,
	 * then close the input stream if it is not STDIN
	 * 
	 * @param vm LuaState to load into
	 * @param is input stream to load from 
	 * @return true if loaded and only item on stack, false if nil+error put onto stack
	 */
	static boolean loadis(LuaState vm, InputStream is, String chunkname ) {
		try {
			vm.resettop();
			if ( 0 != vm.load(is, chunkname) ) {
				setErrorResult( vm, "cannot load "+chunkname+": "+vm.topointer(-1) );
				return false;
			} else {
				return true;
			}
		} finally {
			closeSafely( is );
		}
	}
	

	/** Load a file into a chunk given a filename, leaving it on the stack if successful
	 * 
	 * @param vm LuaState to load into
	 * @param fileName file to load, or null to use STDIN
	 * @return true if loaded and only item on stack, false if nil+error put onto stack
	 */
	private static boolean loadfile( LuaState vm, String fileName ) {
		InputStream is;
		
		String script;
		if ( fileName != null ) {
			script = fileName;
			is = Platform.getInstance().openFile(fileName);
			if ( is == null ) {
				setErrorResult( vm, "cannot open "+fileName+": No such file or directory" );
				return false;
			}
		} else {
			is = STDIN != null? STDIN: new ByteArrayInputStream(new byte[0]);
			script = "-";			
		}
		
		// use vm to load the script
		return loadis( vm, is, script );
	}
	
	// if load succeeds, return 0 for success, 1 for error (as per lua spec)
	private void dofile( LuaState vm ) {
		String filename = vm.optstring(1,null);
		if ( loadfile( vm, filename ) ) {
			vm.call(0, 0);
		} else {
			vm.error( vm.tostring(-1) );
		}
	}

	// return true if loaded, false if error put onto stack
	private boolean loadstring(LuaState vm,  LString string, String chunkname) {
		return loadis( vm, string.toInputStream(), chunkname );
	}

	// return true if loaded, false if error put onto stack
	private boolean load(LuaState vm) {
		LFunction chunkPartLoader = vm.checkfunction(2);
		String chunkname = vm.optstring(3,"=(load)");
		
		// load all the parts
		LClosure c = (LClosure) chunkPartLoader;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while ( true ) {
				setResult(vm,c);
				if ( 0 != vm.pcall(0, 1) ) {
					setErrorResult(vm, vm.tostring(2));
					return false;
				}
				LValue v = vm.topointer(2);
				if ( v.isNil() )
					break;
				LString s = v.luaAsString();
				s.write(baos, 0, s.length());
			}

			// load the chunk
			return loadis( vm, new ByteArrayInputStream( baos.toByteArray() ), chunkname );
			
		} catch (IOException ioe) {
			setErrorResult(vm, ioe.getMessage());
			return false;
		} finally {
			closeSafely( baos );
		}
	}
}
