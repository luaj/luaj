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
		this.luaStackCall(vm);
	}

	public void luaGetTable(VM vm, LValue table, LValue key) {
		vm.push( this );
		vm.push( table );
		vm.push( key );
		this.luaStackCall(vm);
	}
	
	public LString luaGetType() {
		return TYPE_NAME;
	}
	
}
