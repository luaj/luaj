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

import org.luaj.vm.LClosure;
import org.luaj.vm.LFunction;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNil;
import org.luaj.vm.LNumber;
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
	private static final int RAWGET         = 12;
	private static final int RAWSET         = 13;
	private static final int GETFENV        = 14;
	private static final int SETFENV        = 15;
	private static final int SELECT         = 16;
	private static final int COLLECTGARBAGE = 17;
	private static final int DOFILE         = 18;
	private static final int LOADSTRING     = 19;
	private static final int LOAD           = 20;
	private static final int TOSTRING       = 21;
	private static final int UNPACK         = 22;
	private static final int NEXT           = 23;
	private static final int INEXT          = 24;

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
			for ( int i=2; i<=n; i++ ) {
				if ( i > 2 )
					STDOUT.print( "\t" );
				STDOUT.print( vm.tostring(i) );
			}
			STDOUT.println();
			vm.resettop();
			break;
		}
		case IPAIRS:
		case PAIRS: {			
			LTable t = vm.checktable(2);
			vm.resettop();
			vm.pushjavafunction( (id==IPAIRS)? inext: next );
			vm.pushlvalue( t );
			vm.pushnil();
			break;
		}
		case INEXT:
		case NEXT: {
			LTable t = vm.checktable(2);
			LValue v = vm.topointer(3);
			vm.resettop();
			t.next(vm,v,(id==INEXT));
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
			vm.checktable(2);
			vm.setmetatable(2);
			vm.remove(1);
			vm.settop(1);
			break;
		}		
		case TYPE: {
			vm.checkany(2);
			LValue v = vm.topointer(2);
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
		case ERROR: {
			vm.error(vm.checkstring(2), vm.optint(3,1));
			break;
		}
		case ASSERT: {
			if ( ! vm.toboolean(2) )
				vm.error( vm.optstring(3,"assertion failed!") );
			vm.resettop();
			break;
		}
		
		case LOADFILE:
			loadfile(vm, vm.optstring(2,""));
			break;
			
		case TONUMBER: {
			vm.checkany(2);
			switch ( vm.type(2) ) {
			case Lua.LUA_TNUMBER:
				break;
			case Lua.LUA_TSTRING:
				LString s = vm.tolstring(2);
				int base = 10;
				if ( vm.isnumber(3) ) {
					base = vm.tolnumber(3).toJavaInt();
					if ( base < 2 || base > 36 )
						vm.error("bad argument #2 to '?' (base out of range)");
				}
				vm.pushlvalue( s.luaToNumber(base) );
				break;
			default:
				vm.pushnil();
				break;
			}
			vm.insert(1);
			vm.settop(1);
			break;
		}
		case RAWGET: {
			vm.checkany(3);
			LTable t = vm.checktable(2);
			LValue k = vm.topointer(3);
			vm.resettop();
			vm.pushlvalue( t.get( k ) );
		}	break;
		case RAWSET: {
			vm.checkany(3);
			vm.checkany(4);
			LTable t = vm.checktable(2);
			LValue k = vm.topointer(3);
			LValue v = vm.topointer(4);
			t.put( k, v );
			vm.resettop();
			vm.pushlvalue(t);
		}	break;
		case GETFENV: {
			if ( vm.isfunction(2) ) {
				vm.getfenv(-1);
			} else {
				int i = vm.optint(2,1);
				if ( i <= 0 )
					vm.pushlvalue(vm._G);
				else if ( i-1 <= vm.cc )
					vm.pushlvalue( vm.getStackFrame(i-1).closure.env );
				else
					vm.pushnil();
			}
			vm.insert(1);
			vm.settop(1);
			break;
		}
		case SETFENV: {
			LTable t = vm.checktable(-1);
			LFunction f = vm.checkfunction(2);
			if ( vm.setfenv(2) != 0 ) {
				vm.remove(1);
				break;
			}
			int i = vm.tointeger(2);
			if ( i == 0 ) {
				vm._G = t;
				vm.resettop();
			} else {
				LClosure c = vm.getStackFrame(i-1).closure;
				c.luaSetEnv(t);
				vm.resettop();
				vm.pushlvalue(c);
			}
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
					vm.error( "bad argument #1 to '?' (index out of range)" );
				if ( index >= n )
					vm.resettop();
				else {
					for ( int i=0; i<=index; i++ )
						vm.remove(1);
				}					
			} else if ( vm.checkstring(2).equals( "#" ) ) {
				vm.resettop();
				vm.pushnumber( n - 2 );
			}
			break;
		}
		case COLLECTGARBAGE: {
			String s = vm.checkstring(2);
			int result = 0;
			vm.resettop();
			if ( "collect".equals(s) )
				System.gc();
			else {
				Runtime rt = Runtime.getRuntime();
				long used = rt.totalMemory() - rt.freeMemory();
				result = (int) (used >> 10);
			}
			vm.pushnumber(result);
			break;
		}
		case DOFILE:
			dofile(vm);
			break;
		case LOADSTRING:
			loadstring(vm, vm.topointer(2), vm.tostring(3));
			break;
		case LOAD:
			load(vm);
			break;
		case TOSTRING: {
			vm.checkany(2);
			LValue v = vm.topointer(2);
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
	

	// return true if loaded, false if error put onto stack
	public static boolean loadfile( LuaState vm, String fileName ) {
		InputStream is;
		
		String script;
		if ( ! "".equals(fileName) ) {
			script = fileName;
			is = Platform.getInstance().openFile(fileName);
			if ( is == null ) {
				setErrorResult( vm, "cannot open "+fileName+": No such file or directory" );
				return false;
			}
		} else {
			is = STDIN;
			script = "-";
		}
		
		// use vm to load the script
		return loadis( vm, is, script );
	}
	
	// if load succeeds, return 0 for success, 1 for error (as per lua spec)
	private void dofile( LuaState vm ) {
		String filename = vm.checkstring(2);
		if ( loadfile( vm, filename ) ) {
			int s = vm.pcall(1, 0, 0);
			setResult( vm, LInteger.valueOf( s!=0? 1: 0 ) );
		} else {
			vm.error("cannot open "+filename);
		}
	}

	// return true if loaded, false if error put onto stack
	private boolean loadstring(LuaState vm,  LValue string, String chunkname) {
		return loadis( vm, 
				string.luaAsString().toInputStream(), 
				("".equals(chunkname)? "(string)": chunkname) );
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
