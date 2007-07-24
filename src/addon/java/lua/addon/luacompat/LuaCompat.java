package lua.addon.luacompat;

import java.io.IOException;
import java.io.InputStream;

import lua.CallInfo;
import lua.GlobalState;
import lua.StackState;
import lua.VM;
import lua.io.Closure;
import lua.io.LoadState;
import lua.io.Proto;
import lua.value.LFunction;
import lua.value.LNil;
import lua.value.LNumber;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LValue;

public class LuaCompat extends LFunction {

	public static void install() {
		LTable globals = GlobalState.getGlobalsTable();
		for ( int i = 0; i < NAMES.length; ++i ) {
			globals.put( NAMES[i], new LuaCompat( i ) );
		}
	}
	
	public static final String[] NAMES = {
		"assert",
		"collectgarbage",
		"loadfile",
		"tonumber",
		"rawget",
		"setfenv"
	};
	
	private static final int ASSERT = 0;
	private static final int COLLECTGARBAGE = 1;
	private static final int LOADFILE = 2;
	private static final int TONUMBER = 3;
	private static final int RAWGET = 4;
	private static final int SETFENV = 5;
	
	private final int id;
	private LuaCompat( int id ) {
		this.id = id;
	}
	
	public void luaStackCall( VM vm ) {
		switch ( id ) {
		case ASSERT: {
			if ( !vm.getArgAsBoolean(0) ) {
				String message;
				if ( vm.getArgCount() > 1 ) {
					message = vm.getArgAsString(1);
				} else {
					message = "assertion failed!";
				}
				throw new RuntimeException(message);
			}
			vm.setResult();
		}	break;
		case COLLECTGARBAGE:
			System.gc();
			vm.setResult();
			break;
		case LOADFILE:
			vm.setResult( loadfile(vm, vm.getArg(0)) );
			break;
		case TONUMBER:
			vm.setResult( toNumber( vm ) );
			break;
		case RAWGET: {
			LValue t = vm.getArg(0);;
			LValue k = vm.getArg(1);
			LValue result = LNil.NIL;
			if ( t instanceof LTable ) {
				LValue v = (LValue) ( (LTable) t ).m_hash.get( k );
				if ( v != null ) {
					result = v;
				}
			}
			vm.setResult( result );
		}	break;
		case SETFENV:
			setfenv( (StackState) vm );
			break;
		default:
			luaUnsupportedOperation();
		}
	}
	
	private LValue toNumber( VM vm ) {
		LValue input = vm.getArg(0);
		if ( input instanceof LNumber ) {
			return input;
		} else if ( input instanceof LString ) {
			int base = 10;
			if ( vm.getArgCount()>1 ) {
				base = vm.getArgAsInt(1);
			}
			return ( (LString) input ).luaToNumber( base );
		}
		return LNil.NIL;
	}
	
	private void setfenv( StackState state ) {
		LValue f = state.getArg(0);
		LValue newenv = state.getArg(1);

		Closure c = null;
		
		// Lua reference manual says that first argument, f, can be a "Lua
		// function" or an integer. Lots of things extend LFunction, but only
		// instances of Closure are "Lua functions".
		if ( f instanceof Closure ) {
			c = (Closure) f;
		} else {
			int callStackDepth = f.luaAsInt();
			if ( callStackDepth > 0 ) {
				CallInfo frame = state.getStackFrame( callStackDepth );
				if ( frame != null ) {
					c = frame.closure;
				}
			} else {
				// This is supposed to set the environment of the current
				// "thread". But, we have not implemented coroutines yet.
				throw new RuntimeException( "not implemented" );
			}
		}
		
		if ( c != null ) {
			if ( newenv instanceof LTable ) {
				c.env = (LTable) newenv;
			}
			state.setResult( c );
			return;
		}
		
		state.setResult();
		return;
	}
	
	private LValue loadfile( VM vm, LValue fileName ) {
		InputStream is;
		
		String script;
		if ( fileName != null ) {
			script = fileName.luaAsString();
			is = getClass().getResourceAsStream( "/"+script );
		} else {
			is = System.in;
			script = "-";
		}
		
		if ( is != null ) {
			try {
				Proto p = LoadState.undump(vm, is, script);
				return new Closure( (StackState) vm, p);
			} catch (IOException e) {
			} finally {
				if ( is != System.in ) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}
		
		return LNil.NIL;
	}
}
