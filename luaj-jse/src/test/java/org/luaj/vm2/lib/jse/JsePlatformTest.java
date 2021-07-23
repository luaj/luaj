package org.luaj.vm2.lib.jse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

class JsePlatformTest {
	@Test
	void testLuaMainPassesArguments() {
		Globals globals = JsePlatform.standardGlobals();
		LuaValue chunk = globals.load("return #arg, arg.n, arg[2], arg[1]");
		Varargs results = JsePlatform.luaMain(chunk, new String[] { "aaa", "bbb" });
		assertEquals(results.narg(), 4);
		assertEquals(results.arg(1), LuaValue.valueOf(2));
		assertEquals(results.arg(2), LuaValue.valueOf(2));
		assertEquals(results.arg(3), LuaValue.valueOf("bbb"));
		assertEquals(results.arg(4), LuaValue.valueOf("aaa"));
	}
}
