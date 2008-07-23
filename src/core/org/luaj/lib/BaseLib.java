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
import org.luaj.vm.LuaErrorException;
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
	
	public boolean luaStackCall(LuaState vm) {
		switch ( id ) {
		case PRINT: {
			int n = vm.gettop();
			vm.getglobal("tostring");			
			for ( int i=2; i<=n; i++ ) {
				vm.pushvalue(-1);
				vm.pushvalue(i);
				vm.call(1, 1);
				if ( vm.type(-1) != Lua.LUA_TSTRING )
					vm.error( "'tostring' must return a string to 'print'" );				
				if ( i > 2 )
					STDOUT.print( "\t" );
				STDOUT.print( vm.tostring(-1) );
				vm.poplvalue();
			}
			STDOUT.println();
			vm.resettop();
			break;
		}
		case IPAIRS: {			
			LTable t = vm.checktable(2);
			vm.resettop();
			vm.pushjavafunction( inext );
			vm.pushlvalue( t );
			vm.pushinteger( 0 );
			break;
		}
		case PAIRS: {
			LTable t = vm.checktable(2);
			vm.resettop();
			vm.pushjavafunction( next );
			vm.pushlvalue( t );
			vm.pushnil();
			break;
		}
		case INEXT: {
			  int i = vm.checkint(3) + 1;
			  LTable t = vm.checktable(2);
			  LValue v = t.get(i);
			  vm.resettop();
			  if ( !v.isNil() ) {
				  vm.pushinteger(i);
				  vm.pushlvalue(v);
			  }
			  break;
		}
		case NEXT: {
			LTable t = vm.checktable(2);
			LValue k = vm.topointer(3);
			vm.resettop();
			t.next(vm,k,false);
			break;
		}
		case GETMETATABLE: {
			vm.checkany(2);
			if ( ! vm.getmetatable(2) ) {
				vm.resettop();
				vm.pushnil();
			} else {
				vm.replace(1);
				vm.settop(1);
				vm.getfield(-1,LValue.TM_METATABLE);
				if ( vm.isnil(-1) )
					vm.pop(1);
				else
					vm.remove(1);
			}
			break;
		}
		case SETMETATABLE: {
			LTable t = vm.checktable(2);
			LValue v = vm.checkany(3);
			vm.argcheck(v.isTable() || v.isNil(), 3, "table or nil expected");
			t.luaSetMetatable(v);
			vm.resettop();
			vm.pushlvalue(t);
			break;
		}		
		case TYPE: {
			LValue v = vm.checkany(2);
			vm.resettop();
			vm.pushlstring( v.luaGetTypeName() );
			break;
		}
		case PCALL: {
			vm.checkany(2);
			int n = vm.gettop();
			int s = vm.pcall( n-2, Lua.LUA_MULTRET, 0 );
			if ( s == 0 ) { // success, results are on stack above the pcall
				vm.remove( 1 );
				vm.pushboolean( true );
				vm.insert( 1 );
			} else { // error, error message is on the stack
				vm.pushboolean( false );
				vm.insert( 1 );
			}
			break;
		}
		case XPCALL: {
			LValue errfun = vm.checkany(3);
			vm.settop(2);
			int s = vm.pcall( 0, Lua.LUA_MULTRET, 0 );
			if ( s == 0 ) { // success, results are on stack above the xpcall
				vm.pushboolean( true );
				vm.replace( 1 );
			} else { // error, error message is on the stack
				vm.pushlvalue( errfun );
				vm.insert( 1 );
				s = vm.pcall( vm.gettop()-1, 1, 0 );
				if ( s == 0 ) {
					vm.pushboolean( false );
					vm.insert( 1 );
				} else { // error in error handler
					vm.resettop();
					vm.pushboolean(false);
					vm.pushstring("error in error handling");					
				}
			}
			break;
		}
		case ERROR: {
			vm.error(vm.optstring(2,null), vm.optint(3,1));
			break;
		}
		case ASSERT: {
			if ( ! vm.toboolean(2) )
				vm.error( vm.optstring(3,"assertion failed!") );
			vm.remove(1);
			break;
		}
		
		case LOADFILE:
			loadfile(vm, vm.optstring(2,null));
			break;
			
		case TONUMBER: {
			int base = vm.optint(3, 10);
			if (base == 10) {  /* standard conversion */
				vm.checkany(2);
				LValue v = vm.tolnumber(2);
				vm.resettop();
				if ( ! v.isNil() )
					vm.pushlvalue(v);
			} else {
				if ( base < 2 || base > 36 )
					vm.typerror(3, "base out of range");				
				LString s = vm.checklstring(2);
				vm.resettop();
				vm.pushlvalue( s.luaToNumber(base) );
			}
			break;
		}
		case RAWEQUAL: {
			LValue a = vm.checkany(2);
			LValue b = vm.checkany(3);
			vm.resettop();
			vm.pushboolean(a == b);
			break;
		}	
		case RAWGET: {
			LTable t = vm.checktable(2);
			LValue k = vm.checkany(3);
			vm.resettop();
			vm.pushlvalue( t.get( k ) );
			break;
		}
		case RAWSET: {
			LTable t = vm.checktable(2);
			LValue k = vm.checkany(3);
			LValue v = vm.checkany(4);
			t.put( k, v );
			vm.resettop();
			vm.pushlvalue(t);
			break;
		}
		case GETFENV: {
			LValue f = getfunc(vm, true);
			vm.resettop();
			vm.pushlvalue(f.luaGetEnv(vm._G));
			break;
		}
		case SETFENV: {
			LTable t = vm.checktable(3);
			LValue f = getfunc(vm, false);
			if ( vm.isnumber(2) && vm.tointeger(2) == 0 ) {
				vm._G = t;
			} else if ( (!(f instanceof LClosure)) || ! f.luaSetEnv(t) ) {
				vm.error( "'setfenv' cannot change environment of given object" );
			}
			vm.resettop();
			vm.pushlvalue(f);
			break;
		}
		case SELECT: {
			vm.checkany(2);
			int n = vm.gettop();			
			if ( vm.isnumber(2) ) {
				int index = vm.tolnumber(2).toJavaInt();
				if ( index < 0 )
					index += n-1;
				if ( index <= 0 )
					vm.typerror( 2, "index out of range" );
				if ( index >= n )
					vm.resettop();
				else {
					for ( int i=0; i<=index; i++ )
						vm.remove(1);
				}					
			} else if ( vm.checkstring(2).equals( "#" ) ) {
				vm.resettop();
				vm.pushnumber( n - 2 );
			} else {
				vm.typerror(2,"expected number or '#'");
			}
			break;
		}
		case COLLECTGARBAGE: {
			String s = vm.checkstring(2);
			int result = 0;
			vm.resettop();
			if ( "collect".equals(s) )
				System.gc();
			else if ( "count".equals(s) ) {
				Runtime rt = Runtime.getRuntime();
				long used = rt.totalMemory() - rt.freeMemory();
				result = (int) (used >> 10);
			} else {
				vm.typerror(2,"gc op");
			}
			vm.pushnumber(result);
			break;
		}
		case DOFILE:
			dofile(vm);
			break;
		case LOADSTRING:
			loadstring(vm, vm.checklstring(2), vm.optstring(3,"(string)"));
			break;
		case LOAD:
			load(vm);
			break;
		case TOSTRING: {
			LValue v = vm.checkany(2);
			vm.resettop();
			vm.pushlvalue( v.luaAsString() );			
			break;
		}
		case UNPACK: {
			LTable list = vm.checktable(2);
			int n = vm.gettop();
			int i = vm.optint(3,1);
			int j;
			if ( n >= 4 ) {
				j = vm.checkint(4);
			} else {
				j = list.luaLength();
			}
			vm.resettop();
			vm.checkstack(j+1-i);
			for ( int k=i; k<=j; k++ )
				vm.pushlvalue(list.get(k));
			break;
		}
		default:
			LuaState.vmerror( "bad base id" );
		}
		return false;
	}
	
	private static LValue getfunc (LuaState vm, boolean opt) {
		if ( vm.isfunction(2) )
			return vm.tojavafunction(2);
		else {
			int level = opt? vm.optint(2, 1): vm.checkint(2);
		    vm.argcheck(level >= 0, 2, "level must be non-negative");
		    vm.argcheck(level-1 <= vm.cc, 2, "invalid level");
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
		String filename = vm.optstring(2,null);
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
				if ( 0 != vm.pcall(0, 1, 0) ) {
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
