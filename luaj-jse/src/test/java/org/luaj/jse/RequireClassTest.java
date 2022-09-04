package org.luaj.jse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.jse.require.RequireSampleClassCastExcep;
import org.luaj.jse.require.RequireSampleLoadLuaError;
import org.luaj.jse.require.RequireSampleLoadRuntimeExcep;
import org.luaj.jse.require.RequireSampleSuccess;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

class RequireClassTest {

	private LuaTable globals;
	private LuaValue require;

	@BeforeEach
	public void setUp() {
		globals = JsePlatform.standardGlobals();
		require = globals.get("require");
	}

	@Test
	void testLoadClass() {
		LuaValue result = globals.load(new RequireSampleSuccess());
		assertEquals("require-sample-success-", result.tojstring());
	}

	@Test
	void testRequireClassSuccess() {
		LuaValue result = require.call(LuaValue.valueOf(RequireSampleSuccess.class.getName()));
		assertEquals("require-sample-success-" + RequireSampleSuccess.class.getName(), result.tojstring());
		result = require.call(LuaValue.valueOf(RequireSampleSuccess.class.getName()));
		assertEquals("require-sample-success-" + RequireSampleSuccess.class.getName(), result.tojstring());
	}

	@Test
	void testRequireClassLoadLuaError() {
		try {
			LuaValue result = require.call(LuaValue.valueOf(RequireSampleLoadLuaError.class.getName()));
			fail("incorrectly loaded class that threw lua error");
		} catch (LuaError le) {
			assertEquals("sample-load-lua-error", le.getMessage());
		}
		try {
			LuaValue result = require.call(LuaValue.valueOf(RequireSampleLoadLuaError.class.getName()));
			fail("incorrectly loaded class that threw lua error");
		} catch (LuaError le) {
			assertEquals("loop or previous error loading module '" + RequireSampleLoadLuaError.class.getName() + "'",
				le.getMessage());
		}
	}

	@Test
	void testRequireClassLoadRuntimeException() {
		try {
			LuaValue result = require.call(LuaValue.valueOf(RequireSampleLoadRuntimeExcep.class.getName()));
			fail("incorrectly loaded class that threw runtime exception");
		} catch (RuntimeException le) {
			assertEquals("sample-load-runtime-exception", le.getMessage());
		}
		try {
			LuaValue result = require.call(LuaValue.valueOf(RequireSampleLoadRuntimeExcep.class.getName()));
			fail("incorrectly loaded class that threw runtime exception");
		} catch (LuaError le) {
			assertEquals(
				"loop or previous error loading module '" + RequireSampleLoadRuntimeExcep.class.getName() + "'",
				le.getMessage());
		}
	}

	@Test
	void testRequireClassClassCastException() {
		try {
			LuaValue result = require.call(LuaValue.valueOf(RequireSampleClassCastExcep.class.getName()));
			fail("incorrectly loaded class that threw class cast exception");
		} catch (LuaError le) {
			String msg = le.getMessage();
			if (msg.indexOf("not found") < 0)
				fail("expected 'not found' message but got " + msg);
		}
		try {
			LuaValue result = require.call(LuaValue.valueOf(RequireSampleClassCastExcep.class.getName()));
			fail("incorrectly loaded class that threw class cast exception");
		} catch (LuaError le) {
			String msg = le.getMessage();
			if (msg.indexOf("not found") < 0)
				fail("expected 'not found' message but got " + msg);
		}
	}
}
