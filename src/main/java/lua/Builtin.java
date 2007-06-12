/**
 * 
 */
package lua;

import lua.value.LFunction;
import lua.value.LNil;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LValue;

final class Builtin extends LFunction {

	static void addBuiltins(LTable table) {
		for ( int i=0; i<NAMES.length; i++ )
			table.luaSetTable( new LString( NAMES[i] ), new Builtin(i) );
	}

	private static final int PRINT = 0;
	private static final int PAIRS = 1;
	
	private static final String[] NAMES = { "print", "pairs" };
	
	private int id;
	private Builtin( int id ) {			
		this.id = id;
	}

	public String toString() {
		return "Builtin('"+NAMES[id]+"')";
	}
	
	// perform a lua call
	public void luaStackCall(StackState state, int base) {
		int returnValues = 0;
		switch ( id ) {
		case PRINT:
			for ( int i=base+1, n=state.top; i<n; i++ ) {
				System.out.print( String.valueOf(state.stack[i]) );
				System.out.print( "\t" );
			}
			System.out.println();
			break;
		case PAIRS:
			state.adjustTop(base+2);
			LValue value = state.stack[base+1].luaPairs();
			state.stack[base] = value;
			returnValues = 1;
			break;
		default:
			luaUnsupportedOperation();
		}
		state.adjustTop(base-1+returnValues);
	}

}