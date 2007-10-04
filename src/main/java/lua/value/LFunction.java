package lua.value;

import lua.Lua;
import lua.VM;


public class LFunction extends LValue {

	public LString luaAsString() {
		return new LString( "function: "+hashCode() );
	}

	public void luaSetTable(VM vm, LValue table, LValue key, LValue val) {
		vm.push( this );
		vm.push( table );
		vm.push( key );
		vm.push( val );
		vm.lua_call( 3, 0 );
	}

	public void luaGetTable(VM vm, LValue table, LValue key) {
		vm.push( this );
		vm.push( table );
		vm.push( key );
		vm.lua_call( 2, 1 );
	}
	
	public int luaGetType() {
		return Lua.LUA_TFUNCTION;
	}
	
}
