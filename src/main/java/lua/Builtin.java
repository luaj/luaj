/**
 * 
 */
package lua;

import lua.value.LFunction;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LValue;

final class Builtin extends LFunction {

	static void addBuiltins(LTable table) {
		for ( int i=0; i<NAMES.length; i++ )
			table.m_hash.put( new LString( NAMES[i] ), new Builtin(i) );
	}

	private static final int PRINT = 0;
	private static final int PAIRS = 1;
	private static final int GETMETATABLE = 2;
	private static final int SETMETATABLE = 3;
	
	private static final String[] NAMES = { "print", "pairs", "getmetatable", "setmetatable" };
	
	private int id;
	private Builtin( int id ) {			
		this.id = id;
	}

	public String toString() {
		return "builtin."+NAMES[id];
	}
	
	// perform a lua call
	public void luaStackCall(StackState state, int base, int top, int nresults) {
		switch ( id ) {
		case PRINT:
			for ( int i=base+1; i<top; i++ ) {
				System.out.print( String.valueOf(state.stack[i]) );
				System.out.print( "\t" );
			}
			System.out.println();
			state.top = base;
			break;
		case PAIRS:
			LValue value = state.stack[base+1].luaPairs();
			state.stack[base] = value;
			state.top = base+1;
			break;
		case GETMETATABLE:
			state.stack[base] = state.stack[base+1].luaGetMetatable();
			state.top = base+1;
			break;
		case SETMETATABLE:
			state.stack[base+1].luaSetMetatable(state.stack[base+2]);
			state.top = base;
			break;
		default:
			luaUnsupportedOperation();
		}
		if (nresults >= 0)
			state.adjustTop(base + nresults);
	}

}