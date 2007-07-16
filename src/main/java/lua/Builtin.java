/**
 * 
 */
package lua;

import java.io.OutputStream;
import java.io.PrintStream;

import lua.value.LFunction;
import lua.value.LTable;
import lua.value.LValue;

final class Builtin extends LFunction {

	static void addBuiltins(LTable table) {
		for ( int i=0; i<NAMES.length; i++ )
			table.put( NAMES[i], new Builtin(i) );
	}

	private static final int PRINT = 0;
	private static final int PAIRS = 1;
	private static final int GETMETATABLE = 2;
	private static final int SETMETATABLE = 3;
	private static final int TYPE = 4;
	
	private static final String[] NAMES = { "print", "pairs", "getmetatable", "setmetatable", "type" };
	
	private static PrintStream stdout = System.out;
	
	private int id;
	private Builtin( int id ) {			
		this.id = id;
	}

	public String toString() {
		return "builtin."+NAMES[id];
	}
	
	// perform a lua call
	public void luaStackCall(CallFrame call, int base, int top, int nresults) {
		switch ( id ) {
		case PRINT:
			if ( base+1<top )
				stdout.print( call.stack[base+1].luaAsString() );
			for ( int i=base+2; i<top; i++ ) {
				stdout.print( "\t" );
				stdout.print( call.stack[i].luaAsString() );
			}
			stdout.println();
			call.top = base;
			break;
		case PAIRS:
			LValue value = call.stack[base+1].luaPairs();
			call.stack[base] = value;
			call.top = base+1;
			break;
		case GETMETATABLE:
			call.stack[base] = call.stack[base+1].luaGetMetatable();
			call.top = base+1;
			break;
		case SETMETATABLE:
			call.stack[base+1].luaSetMetatable(call.stack[base+2]);
			call.stack[base] = call.stack[base+1];
			call.top = base+1;
			break;
		case TYPE:
			call.stack[base] = call.stack[base+1].luaGetType();
			call.top = base+1;
			break;
		default:
			luaUnsupportedOperation();
		}
		if (nresults >= 0)
			call.adjustTop(base + nresults);
	}
	
	static void redirectOutput( OutputStream newStdOut ) {
		stdout = new PrintStream( newStdOut );
	}
	
	static void restoreStandardOutput() {
		stdout = System.out;
	}

}
