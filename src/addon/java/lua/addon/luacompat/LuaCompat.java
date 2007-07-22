package lua.addon.luacompat;

import java.io.IOException;
import java.io.InputStream;

import lua.CallFrame;
import lua.GlobalState;
import lua.StackState;
import lua.io.Closure;
import lua.io.LoadState;
import lua.io.Proto;
import lua.value.LDouble;
import lua.value.LFunction;
import lua.value.LInteger;
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
	
	public void luaStackCall( CallFrame call, int base, int top, int nresults ) {
		switch ( id ) {
		case ASSERT: {
			LValue v = call.stack[base+1];
			if ( !v.luaAsBoolean() ) {
				String message;
				if ( top > base+2 ) {
					message = call.stack[base+2].luaAsString();
				} else {
					message = "assertion failed!";
				}
				throw new RuntimeException(message);
			}
			call.top = base;
		}	break;
		case COLLECTGARBAGE:
			System.gc();
			call.top = base;
			break;
		case LOADFILE:
			call.stack[base] = loadfile(call, ( top > base ) ? call.stack[base+1] : null);
			call.top = base+1;
			break;
		case TONUMBER:
			call.stack[base] = toNumber( call.stack, base+1, top );
			call.top = base+1;
			break;
		case RAWGET: {
			LValue t = call.stack[base+1];
			LValue k = call.stack[base+2];
			LValue result = LNil.NIL;
			if ( t instanceof LTable ) {
				LValue v = (LValue) ( (LTable) t ).m_hash.get( k );
				if ( v != null ) {
					result = v;
				}
			}
			call.stack[base] = result;
			call.top = base+1;
		}	break;
		case SETFENV:
			call.top = base + setfenv( call.stack, base, base+1, top, call.state );
			break;
		default:
			luaUnsupportedOperation();
		}
		if (nresults >= 0)
			call.adjustTop(base + nresults);
	}
	
	private LValue toNumber( LValue[] stack, int first, int top ) {
		LValue result = LNil.NIL;
		if ( first < top ) {
			LValue input = stack[first];
			if ( input instanceof LNumber ) {
				result = input;
			} else if ( input instanceof LString ) {
				int base = 10;
				if ( first+1 < top ) {
					base = stack[first+1].luaAsInt();
				}
				if ( base >= 2 && base <= 36 ) {
					String str = input.luaAsString().trim();
					try {
						result = new LInteger( Integer.parseInt( str, base ) );
					} catch ( NumberFormatException nfe ) {
						if ( base == 10 ) {
							try {
								result = new LDouble( Double.parseDouble( str ) );
							} catch ( NumberFormatException nfe2 ) {
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	private int setfenv( LValue[] stack, int result, int argbase, int arglimit, StackState state ) {
		LValue f = stack[argbase];
		LValue newenv = stack[argbase+1];

		Closure c = null;
		
		// Lua reference manual says that first argument, f, can be a "Lua
		// function" or an integer. Lots of things extend LFunction, but only
		// instances of Closure are "Lua functions".
		if ( f instanceof Closure ) {
			c = (Closure) f;
		} else {
			int callStackDepth = f.luaAsInt();
			if ( callStackDepth > 0 ) {
				CallFrame frame = state.getStackFrame( callStackDepth );
				if ( frame != null ) {
					c = frame.cl;
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
			stack[ result ] = c;
			return 1;
		}
		
		return 0;
	}
	
	private LValue loadfile( CallFrame call, LValue fileName ) {
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
				Proto p = LoadState.undump(call.state, is, script);
				return new Closure(call.state, p);
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
