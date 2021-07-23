package org.luaj.vm2.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserContextTest {
	protected ScriptEngine  e;
	protected Bindings      b;
	protected ScriptContext c;

	@BeforeEach
	public void setUp() {
		this.e = new ScriptEngineManager().getEngineByName("luaj");
		this.c = new LuajContext();
		this.b = c.getBindings(ScriptContext.ENGINE_SCOPE);
	}

	@Test
	void testUncompiledScript() throws ScriptException {
		b.put("x", 144);
		assertEquals(12, e.eval("z = math.sqrt(x); return z", b));
		assertEquals(12, b.get("z"));
		assertEquals(null, e.getBindings(ScriptContext.ENGINE_SCOPE).get("z"));
		assertEquals(null, e.getBindings(ScriptContext.GLOBAL_SCOPE).get("z"));

		b.put("x", 25);
		assertEquals(5, e.eval("z = math.sqrt(x); return z", c));
		assertEquals(5, b.get("z"));
		assertEquals(null, e.getBindings(ScriptContext.ENGINE_SCOPE).get("z"));
		assertEquals(null, e.getBindings(ScriptContext.GLOBAL_SCOPE).get("z"));
	}

	@Test
	void testCompiledScript() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("z = math.sqrt(x); return z");

		b.put("x", 144);
		assertEquals(12, cs.eval(b));
		assertEquals(12, b.get("z"));

		b.put("x", 25);
		assertEquals(5, cs.eval(c));
		assertEquals(5, b.get("z"));
	}
}
