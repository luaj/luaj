package lua.addon.luacompat;

import java.io.IOException;
import java.io.InputStream;

import lua.CallInfo;
import lua.GlobalState;
import lua.Lua;
import lua.StackState;
import lua.VM;
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
		for ( int i = 0; i < GLOBAL_NAMES.length; ++i ) {
			globals.put( GLOBAL_NAMES[i], new LuaCompat( i ) );
		}
		
		LTable math = new LTable();
		for ( int i = 0; i < MATH_NAMES.length; ++i ) {
			math.put( MATH_NAMES[i], new LuaCompat( MATH_BASE + i ) );
		}
		
		// Some handy constants
		math.put( "huge", new LDouble( Double.MAX_VALUE ) );
		math.put( "pi", new LDouble( Math.PI ) );
		
		globals.put( "math", math );
		
		LTable string = LString.getMetatable();
		for ( int i = 0; i < STRING_NAMES.length; ++i ) {
			string.put( STRING_NAMES[i], new LuaCompat( STRING_BASE + i ) );
		}
		globals.put( "string", string );
	}
	
	public static final String[] GLOBAL_NAMES = {
		"assert",
		"loadfile",
		"tonumber",
		"rawget",
		"setfenv",
		"select",
		"collectgarbage",
	};
	
	public static final String[] MATH_NAMES = {
		"abs",
		"max",
		"min",
		"modf",
		"sin"
	};
	
	public static final String[] STRING_NAMES = {
		"rep",
		"sub",
	};
	
	private static final int ASSERT = 0;
	private static final int LOADFILE = 1;
	private static final int TONUMBER = 2;
	private static final int RAWGET = 3;
	private static final int SETFENV = 4;
	private static final int SELECT = 5;
	private static final int COLLECTGARBAGE = 6;
	
	private static final int MATH_BASE = 10;
	private static final int ABS = MATH_BASE + 0;
	private static final int MAX = MATH_BASE + 1;
	private static final int MIN = MATH_BASE + 2;
	private static final int MODF = MATH_BASE + 3;
	private static final int SIN = MATH_BASE + 4;
	
	private static final int STRING_BASE = 20;
	private static final int REP = STRING_BASE + 0;
	private static final int SUB = STRING_BASE + 1;
	
	private final int id;
	private LuaCompat( int id ) {
		this.id = id;
	}
	
	public boolean luaStackCall( VM vm ) {
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
				result = ( (LTable) t ).get( k );
			}
			vm.setResult( result );
		}	break;
		case SETFENV:
			setfenv( (StackState) vm );
			break;
		case SELECT:
			select( vm );
			break;
		case COLLECTGARBAGE:
			System.gc();
			vm.setResult();
			break;
		
		// Math functions
		case ABS:
			vm.setResult( abs( vm.getArg( 0 ) ) );
			break;
		case MAX:
			vm.setResult( max( vm.getArg( 0 ), vm.getArg( 1 ) ) );
			break;
		case MIN:
			vm.setResult( min( vm.getArg( 0 ), vm.getArg( 1 ) ) );
			break;
		case MODF:
			modf( vm );
			break;
		case SIN:
			vm.setResult( new LDouble( Math.sin( vm.getArgAsDouble( 0 ) ) ) );
			break;
		
		// String functions
		case REP: {
			String s = vm.getArgAsString( 0 );
			int n = vm.getArgAsInt( 1 );
			if ( n >= 0 ) {
				StringBuffer sb = new StringBuffer( s.length() * n );
				for ( int i = 0; i < n; ++i ) {
					sb.append( s );
				}
				vm.setResult( new LString( sb.toString() ) );
			} else {
				vm.setResult( LNil.NIL );
			}
		}	break;
		case SUB: {
			String s = vm.getArgAsString( 0 );
			final int len = s.length();
			
			int i = vm.getArgAsInt( 1 );
			if ( i < 0 ) {
				// start at -i characters from the end
				i = Math.max( len + i, 0 );
			} else if ( i > 0 ) {
				// start at character i - 1
				i = i - 1;
			}
			
			int j = ( vm.getArgCount() > 2 ) ? vm.getArgAsInt( 2 ): -1;
			if ( j < 0 ) {
				j = Math.max( i, len + j + 1 );
			} else {
				j = Math.min( Math.max( i, j ), len );
			}
			
			String result = s.substring( i, j );
			vm.setResult( new LString( result ) );
		}	break;
		
		default:
			luaUnsupportedOperation();
		}
		return false;
	}
	
	private void select( VM vm ) {
		LValue arg = vm.getArg( 0 );
		if ( arg instanceof LNumber ) {
			final int start;
			final int numResults;
			if ( ( start = arg.luaAsInt() ) > 0 &&
				 ( numResults = Math.max( vm.getArgCount() - start,
						 				  vm.getExpectedResultCount() ) ) > 0 ) {
				// since setResult trashes the arguments, we have to save them somewhere.
				LValue[] results = new LValue[numResults];
				for ( int i = 0; i < numResults; ++i ) {
					results[i] = vm.getArg( i+start );
				}
				vm.setResult();
				for ( int i = 0; i < numResults; ++i ) {
					vm.push( results[i] );
				}
				return;
			}
		} else if ( arg.luaAsString().equals( "#" ) ) {
			vm.setResult( new LInteger( vm.getArgCount() - 1 ) );
		}
		vm.setResult();
	}
	
	private LValue abs( final LValue v ) {
		LValue nv = v.luaUnaryMinus();
		return max( v, nv );
	}
	
	private LValue max( LValue lhs, LValue rhs ) {
		return rhs.luaBinCmpUnknown( Lua.OP_LT, lhs ) ? rhs: lhs;
	}
	
	private LValue min( LValue lhs, LValue rhs ) {
		return rhs.luaBinCmpUnknown( Lua.OP_LT, lhs ) ? lhs: rhs;
	}
	
	private void modf( VM vm ) {
		LValue arg = vm.getArg( 0 );
		double v = arg.luaAsDouble();
		double intPart = ( v > 0 ) ? Math.floor( v ) : Math.ceil( v );
		double fracPart = v - intPart;
		vm.setResult();
		vm.push( intPart );
		vm.push( fracPart );
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
