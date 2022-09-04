package org.luaj.vm2.script;

import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.LuaValue;

class CompileNonClosureTest extends DefaultBindingsTestCase {
	@BeforeEach
	@Override
	protected void setUp() throws Exception {
		System.setProperty("org.luaj.luajc", "true");
		super.setUp();
	}

	@Test
	void testCompiledFunctionIsNotClosure() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("return 'foo'");
		LuaValue value = ((LuaScriptEngine.LuajCompiledScript) cs).function;
		assertFalse(value.isclosure());
	}
}
