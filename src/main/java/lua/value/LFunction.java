package lua.value;

import lua.Lua;
import lua.VM;


public class LFunction extends LValue {

	public String toJavaString() {
		return "function: "+hashCode();
	}
	
	public void luaSetTable(VM vm, LValue table, LValue key, LValue val) {
		vm.pushlvalue( this );
		vm.pushlvalue( table );
		vm.pushlvalue( key );
		vm.pushlvalue( val );
		vm.call( 3, 0 );
	}

	public void luaGetTable(VM vm, LValue table, LValue key) {
		vm.pushlvalue( this );
		vm.pushlvalue( table );
		vm.pushlvalue( key );
		vm.call( 2, 1 );
	}
	
	public int luaGetType() {
		return Lua.LUA_TFUNCTION;
	}
	
}
