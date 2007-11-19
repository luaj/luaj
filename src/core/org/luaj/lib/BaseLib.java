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
	private static PrintStream stdout = System.out;	
	
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
	
	public static void install(LTable globals) {
		for ( int i=1; i<NAMES.length; i++ )
			globals.put( NAMES[i], new BaseLib(i) );
		globals.put("_VERSION", new LString("Lua 5.1"));
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

	private void checkargexists(LuaState vm, int index, int type) {
		if ( vm.gettop() < index )
			vm.error("bad argument #"+(index-1)+" to '?' ("+
					(type == Lua.LUA_TVALUE? 
						"value expected)":
						Lua.TYPE_NAMES[type]+" expected, got no value)") );
	}

	private void checkargtype(LuaState vm, int index, int type) {		
		checkargexists( vm, index, type );
		int t = vm.type(index);
		if ( t != type ) {
			if ( type == Lua.LUA_TNUMBER && t == Lua.LUA_TSTRING && vm.isnumber(index) )
				return;
			vm.error("bad argument #"+(index-1)+" to '?' ("+
					Lua.TYPE_NAMES[type]+" expected, got "+vm.typename(index)+")");
		}
	}

	public boolean luaStackCall(LuaState vm) {
		switch ( id ) {
		case PRINT: {
			int n = vm.gettop();
			for ( int i=2; i<=n; i++ ) {
				if ( i > 2 )
					stdout.print( "\t" );
				stdout.print( vm.tostring(i) );
			}
			stdout.println();
			vm.resettop();
			break;
		}
		case PAIRS:
		case IPAIRS: {
			checkargtype(vm,2,Lua.LUA_TTABLE);
			LTable v = vm.totable(2);
			LValue r = v.luaPairs(id==PAIRS);
			vm.resettop();
			vm.pushlvalue( r );
			break;
		}
		case GETMETATABLE: {
			checkargexists(vm,2,Lua.LUA_TVALUE);
			if ( 0 == vm.getmetatable(2) ) {
				vm.resettop();
				vm.pushnil();
			} else {
				vm.insert(1);
				vm.settop(1);
			}
			break;
		}
		case SETMETATABLE: {
			checkargtype(vm,2,Lua.LUA_TTABLE);
			vm.setmetatable(2);
			vm.remove(1);
			break;
		}		
		case TYPE: {
			// TODO: generalize, compute location insofar as possible
			if ( vm.gettop() < 2 ) 
				vm.error("bad argument #1 to '?' (value expected)");
			LValue v = vm.topointer(2);
			vm.resettop();
			vm.pushlstring( v.luaGetTypeName() );
			break;
		}
		case PCALL: {
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
			vm.error(vm.tostring(2), vm.gettop()>=3? vm.tointeger(3): 1);
			break;
		}
		case ASSERT: {
			if ( ! vm.toboolean(2) )
				vm.error( vm.gettop()>2? vm.tostring(3): "assertion failed!", 0 );
			vm.remove(1);
			break;
		}
		
		case LOADFILE:
			loadfile(vm, vm.tostring(2));
			break;
			
		case TONUMBER: {
			checkargexists(vm,2,Lua.LUA_TVALUE);
			switch ( vm.type(2) ) {
			case Lua.LUA_TNUMBER:
				break;
			case Lua.LUA_TSTRING:
				LString s = vm.tolstring(2);
				int base = 10;
				if ( vm.isnumber(3) ) {
					base = vm.tointeger(3);
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
			checkargtype(vm,2,Lua.LUA_TTABLE);
			checkargexists(vm,3,Lua.LUA_TVALUE);
			LTable t = vm.totable(2);
			LValue k = vm.topointer(3);
			vm.resettop();
			vm.pushlvalue( t.get( k ) );
		}	break;
		case RAWSET: {
			checkargtype(vm,2,Lua.LUA_TTABLE);
			checkargexists(vm,3,Lua.LUA_TVALUE);
			checkargexists(vm,4,Lua.LUA_TVALUE);
			LTable t = vm.totable(2);
			LValue k = vm.topointer(3);
			LValue v = vm.topointer(4);
			t.put( k, v );
			vm.resettop();
			vm.pushlvalue(t);
		}	break;
		case GETFENV: {
			if ( vm.gettop() <= 1 ) {
				vm.pushlvalue(vm._G);
			} else {
				if ( ! vm.isfunction(2) ) {
					int i = (vm.isnil(2)? 1: vm.tointeger(2));
					vm.pushlvalue( vm.getStackFrame(i).closure );
				}
				vm.getfenv(-1);
			}
			vm.insert(1);
			vm.settop(1);
			break;
		}
		case SETFENV: {
			LTable t = vm.totable(-1);
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
			checkargexists(vm,2,Lua.LUA_TNUMBER);
			int n = vm.gettop();
			if ( vm.isnumber(2) ) {
				int index = vm.tointeger(2);
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
			} else if ( vm.tostring(2).equals( "#" ) ) {
				vm.resettop();
				vm.pushnumber( n - 2 );
			} else {
				vm.error( "bad argument #1 to '?' (number expected, got "+vm.typename(2)+")" );
			}
			break;
		}
		case COLLECTGARBAGE: {
			String s = vm.tostring(2);
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
			load(vm, vm.topointer(2), vm.tostring(3));
			break;
		case TOSTRING: {
			checkargexists(vm,2,Lua.LUA_TVALUE);
			LValue v = vm.topointer(2);
			vm.resettop();
			vm.pushlvalue( v.luaAsString() );			
			break;
		}
		case UNPACK: {
			checkargtype(vm,2,Lua.LUA_TTABLE);
			LTable list = vm.totable(2);
			int n = vm.gettop();
			int i=1,j;
			if ( n >= 3 ) {
				checkargtype(vm,3,Lua.LUA_TNUMBER);
				i = vm.tointeger(3);
			}
			if ( n >= 4 ) {
				checkargtype(vm,4,Lua.LUA_TNUMBER);
				j = vm.tointeger(4);
			} else {
				j = list.luaLength();
			}
			vm.resettop();
			vm.checkstack(j+1-i);
			for ( int k=i; k<=j; k++ )
				vm.pushlvalue(list.get(k));
			break;
		}
		case NEXT: {
			checkargtype(vm,2,Lua.LUA_TTABLE);
			LTable t = vm.totable(2);
			LValue v = vm.topointer(3);
			vm.resettop();
			t.next(vm,v);
			break;
		}
		default:
			luaUnsupportedOperation();
		}
		return false;
	}
	
	public static void redirectOutput( OutputStream newStdOut ) {
		stdout = new PrintStream( newStdOut );
	}
	
	public static void restoreStandardOutput() {
		stdout = System.out;
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

	// return true if laoded, false if error put onto the stack
	private static boolean loadis(LuaState vm, InputStream is, String chunkname ) {
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
		String filename = vm.tostring(2);
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
	private boolean load(LuaState vm, LValue chunkPartLoader, String chunkname) {
		if ( ! (chunkPartLoader instanceof LClosure) ) {
			vm.error("not a closure: "+chunkPartLoader);
		}
		
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
				if ( v == LNil.NIL )
					break;
				LString s = v.luaAsString();
				s.write(baos, 0, s.length());
			}

			// load the chunk
			return loadis( vm, 
					new ByteArrayInputStream( baos.toByteArray() ), 
					("".equals(chunkname)? "=(load)": chunkname) );
			
		} catch (IOException ioe) {
			setErrorResult(vm, ioe.getMessage());
			return false;
		} finally {
			closeSafely( baos );
		}
	}
}
