/**
 * 
 */
package lua;

import java.io.OutputStream;
import java.io.PrintStream;

import lua.value.LTable;
import lua.value.LValue;

final class Builtin extends JavaFunction {

	static void addBuiltins(LTable table) {
		for ( int i=0; i<NAMES.length; i++ )
			table.put( NAMES[i], new Builtin(i) );
	}

	private static final String[] NAMES = { 
		"print", 
		"pairs", 
		"getmetatable", 
		"setmetatable", 
		"type", 
		"pcall", 
		"ipairs", 
		};
	private static final int PRINT = 0;
	private static final int PAIRS = 1;
	private static final int GETMETATABLE = 2;
	private static final int SETMETATABLE = 3;
	private static final int TYPE = 4;
	private static final int PCALL = 5;
	private static final int IPAIRS = 6;
	
	private static PrintStream stdout = System.out;
	
	private int id;
	private Builtin( int id ) {			
		this.id = id;
	}

	public String toString() {
		return "builtin."+NAMES[id];
	}
	
	// perform a lua call
	/**
	 * Invoke a builtin
	 */
	public int invoke(VM vm) {
		switch ( id ) {
		case PRINT: {
			int n = vm.gettop();
			for ( int i=1; i<=n; i++ ) {
				if ( i > 1 )
					stdout.print( "\t" );
				stdout.print( vm.topointer(i).toJavaString() );
			}
			stdout.println();
			return 0;
		}
		case PAIRS:
		case IPAIRS: {
			LValue v = vm.topointer(1);
			LValue r = v.luaPairs(id==PAIRS);
			vm.pushlvalue( r );
			return 1;
		}
		case GETMETATABLE:
			return vm.getmetatable(1);
		case SETMETATABLE:
			vm.setmetatable(1);
			return 1;
		case TYPE: {
			LValue v = vm.topointer(1);
			vm.pushlstring( v.luaGetTypeName() );
			return 1;
		}
		case PCALL: {
			int n = vm.gettop();
			int s = vm.pcall( n-1, Lua.LUA_MULTRET, 0 );
			if ( s == 0 ) { // success
				vm.pushboolean( true );
				vm.insert( 1 );
				return vm.gettop();
			} else { // error, error message is on the stack
				vm.pushboolean( false );
				vm.insert( -2 );
				return 2;
			}
		}
		default:
			luaUnsupportedOperation();
			return 0;
		}
	}
	
	static void redirectOutput( OutputStream newStdOut ) {
		stdout = new PrintStream( newStdOut );
	}
	
	static void restoreStandardOutput() {
		stdout = System.out;
	}

}
