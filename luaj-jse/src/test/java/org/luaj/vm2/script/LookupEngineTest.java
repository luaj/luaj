package org.luaj.vm2.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.junit.jupiter.api.Test;

class LookupEngineTest {
	@Test
	void testGetEngineByExtension() {
		ScriptEngine e = new ScriptEngineManager().getEngineByExtension(".lua");
		assertNotNull(e);
		assertEquals(LuaScriptEngine.class, e.getClass());
	}

	@Test
	void testGetEngineByName() {
		ScriptEngine e = new ScriptEngineManager().getEngineByName("luaj");
		assertNotNull(e);
		assertEquals(LuaScriptEngine.class, e.getClass());
	}

	@Test
	void testGetEngineByMimeType() {
		ScriptEngine e = new ScriptEngineManager().getEngineByMimeType("text/lua");
		assertNotNull(e);
		assertEquals(LuaScriptEngine.class, e.getClass());
	}

	@Test
	void testFactoryMetadata() {
		ScriptEngine e = new ScriptEngineManager().getEngineByName("luaj");
		ScriptEngineFactory f = e.getFactory();
		assertEquals("Luaj", f.getEngineName());
		assertEquals("Luaj 0.0", f.getEngineVersion());
		assertEquals("lua", f.getLanguageName());
		assertEquals("5.2", f.getLanguageVersion());
	}
}
