package org.luaj.luajc;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class SampleMainChunk extends VarArgFunction {

	static final LuaValue $print = valueOf("print");
	static final LuaValue $foo   = valueOf("foo");

	LuaValue[] rw_ENV; // The environment when it is read-write
//	LuaValue ro_ENV;  // The environment when it is read-only in all sub-functions

	LuaValue[] rw_openup1; // upvalue that we create and modify in "slot" 1, passed to sub-function in initer.
	LuaValue[] rw_openup2; // array is instantiated on first set or before supply to closure, after that value is get, set.
	LuaValue[] rw_openup3; // closing these nulls them out, sub-functions still retain references to array & can use
	LuaValue   ro_openup4; // open upvalue that is read-only once it is supplied to an inner function.
	LuaValue   ro_openup5; // closing this also nulls it out.

	// Must have this in the main chunk so it can be loaded and instantiated on all platforms.
	public SampleMainChunk() {
	}

	public void initupvalue1(LuaValue[] v) {
		this.rw_ENV = v;
	}

	public Varargs invoke(Varargs args) {
		rw_ENV[0].get($print).call($foo);

		rw_ENV[0].set($print, new InnerFunction(rw_openup3, rw_openup1, ro_openup5));

		return null;
	}

	static class InnerFunction extends TwoArgFunction {
		static final LuaValue $print = valueOf("print"); // A constant, named for what it is.
		static final LuaValue $foo   = valueOf("foo");

		final LuaValue[] rw_upvalue1; // from enclosing function, corresponds to upvaldesc not instack.
		final LuaValue[] rw_upvalue2; // from enclosing function, corresponds to upvaldesc not instack.
		final LuaValue   ro_upvalue3; // from enclosing function, but read-only everywhere.

		LuaValue[] rw_openup1; // closing these nulls them out, sub-functions still retain references to array & can use
		LuaValue   ro_openup2; // open upvalue that is read-only once it is supplied to an inner function.

		InnerFunction(LuaValue[] rw_upvalue1, LuaValue[] rw_upvalue2, LuaValue ro_upvalue3) {
			this.rw_upvalue1 = rw_upvalue1;
			this.rw_upvalue2 = rw_upvalue2;
			this.ro_upvalue3 = ro_upvalue3;
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			return NIL;
		}

	}

}
