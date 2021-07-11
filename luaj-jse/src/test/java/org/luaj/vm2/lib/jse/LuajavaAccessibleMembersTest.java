package org.luaj.vm2.lib.jse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

class LuajavaAccessibleMembersTest {

	private Globals globals;

	@BeforeEach
	protected void setUp() throws Exception {
		globals = JsePlatform.standardGlobals();
	}

	private String invokeScript(String script) {
		try {
			LuaValue c = globals.load(script, "script");
			return c.call().tojstring();
		} catch (Exception e) {
			fail("exception: " + e);
			return "failed";
		}
	}

	@Test
	void testAccessFromPrivateClassImplementedMethod() {
		assertEquals("privateImpl-aaa-interface_method(bar)",
			invokeScript("b = luajava.newInstance('" + TestClass.class.getName() + "');"
				+ "a = b:create_PrivateImpl('aaa');" + "return a:interface_method('bar');"));
	}

	@Test
	void testAccessFromPrivateClassPublicMethod() {
		assertEquals("privateImpl-aaa-public_method", invokeScript("b = luajava.newInstance('"
			+ TestClass.class.getName() + "');" + "a = b:create_PrivateImpl('aaa');" + "return a:public_method();"));
	}

	@Test
	void testAccessFromPrivateClassGetPublicField() {
		assertEquals("aaa", invokeScript("b = luajava.newInstance('" + TestClass.class.getName() + "');"
			+ "a = b:create_PrivateImpl('aaa');" + "return a.public_field;"));
	}

	@Test
	void testAccessFromPrivateClassSetPublicField() {
		assertEquals("foo", invokeScript("b = luajava.newInstance('" + TestClass.class.getName() + "');"
			+ "a = b:create_PrivateImpl('aaa');" + "a.public_field = 'foo';" + "return a.public_field;"));
	}

	@Test
	void testAccessFromPrivateClassPublicConstructor() {
		assertEquals("privateImpl-constructor", invokeScript("b = luajava.newInstance('" + TestClass.class.getName()
			+ "');" + "c = b:get_PrivateImplClass();" + "return luajava.new(c);"));
	}

	@Test
	void testAccessPublicEnum() {
		assertEquals("class org.luaj.vm2.lib.jse.TestClass$SomeEnum",
			invokeScript("b = luajava.newInstance('" + TestClass.class.getName() + "');" + "return b.SomeEnum"));
	}
}
