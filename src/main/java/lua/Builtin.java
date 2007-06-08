/**
 * 
 */
package lua;

import lua.value.LFunction;
import lua.value.LString;
import lua.value.LTable;

final class Builtin extends LFunction {

	static void addBuiltins(LTable table) {
 		table.luaSetTable( new LString( "print" ), new Builtin(0) );
	}

	private static final int PRINT = 0;
	private int id;
	Builtin( int id ) {			
		this.id = id;
	}

	// perform a lua call
	public void luaStackCall(StackState state, int base, int nresults) {
		switch ( id ) {
		case PRINT:
			System.out.println( String.valueOf( state.stack[base+1] ) );
			return;
		default:
			luaUnsupportedOperation();
		}
	}

}