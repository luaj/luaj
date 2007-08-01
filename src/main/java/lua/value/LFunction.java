package lua.value;

import lua.VM;


public class LFunction extends LValue {

	public static final LString TYPE_NAME = new LString("function");
	
	public String luaAsString() {
		return "function: "+hashCode();
	}

	public void luaSetTable(VM vm, LValue table, LValue key, LValue val) {
		vm.push( this );
		vm.push( table );
		vm.push( key );
		vm.push( val );
		vm.setExpectedResultCount( 0 );
		if ( this.luaStackCall( vm ) )
			vm.execute();
		else
			vm.adjustResults();
	}

	public void luaGetTable(VM vm, LValue table, LValue key) {
		vm.push( this );
		vm.push( table );
		vm.push( key );
		vm.setExpectedResultCount( 1 );
		if ( this.luaStackCall( vm ) )
			vm.execute();
		else
			vm.adjustResults();
	}
	
	public LString luaGetType() {
		return TYPE_NAME;
	}
	
}
