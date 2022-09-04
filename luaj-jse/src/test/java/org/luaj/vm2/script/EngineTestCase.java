package org.luaj.vm2.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

abstract class EngineTestCase {
	protected ScriptEngine e;
	protected Bindings     b;

	protected abstract Bindings createBindings();

	@BeforeEach
	protected void setUp() throws Exception {
		this.e = new ScriptEngineManager().getEngineByName("luaj");
		this.b = createBindings();
	}

	@Test
	void testSqrtIntResult() throws ScriptException {
		e.put("x", 25);
		e.eval("y = math.sqrt(x)");
		Object y = e.get("y");
		assertEquals(5, y);
	}

	@Test
	void testOneArgFunction() throws ScriptException {
		e.put("x", 25);
		e.eval("y = math.sqrt(x)");
		Object y = e.get("y");
		assertEquals(5, y);
		e.put("f", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				return LuaValue.valueOf(arg.toString() + "123");
			}
		});
		Object r = e.eval("return f('abc')");
		assertEquals("abc123", r);
	}

	@Test
	void testCompiledScript() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("y = math.sqrt(x); return y");
		b.put("x", 144);
		assertEquals(12, cs.eval(b));
	}

	@Test
	void testBuggyLuaScript() {
		try {
			e.eval("\n\nbuggy lua code\n\n");
		} catch (ScriptException se) {
			assertEquals("eval threw javax.script.ScriptException: [string \"script\"]:3: syntax error",
				se.getMessage());
			return;
		}
		fail("buggy script did not throw ScriptException as expected.");
	}

	@Test
	void testScriptRedirection() throws ScriptException {
		Reader input = new CharArrayReader("abcdefg\nhijk".toCharArray());
		CharArrayWriter output = new CharArrayWriter();
		CharArrayWriter errors = new CharArrayWriter();
		String script = "print(\"string written using 'print'\")\n"
			+ "io.write(\"string written using 'io.write()'\\n\")\n"
			+ "io.stdout:write(\"string written using 'io.stdout:write()'\\n\")\n"
			+ "io.stderr:write(\"string written using 'io.stderr:write()'\\n\")\n"
			+ "io.write([[string read using 'io.stdin:read(\"*l\")':]]..io.stdin:read(\"*l\")..\"\\n\")\n";

		// Evaluate script with redirection set
		e.getContext().setReader(input);
		e.getContext().setWriter(output);
		e.getContext().setErrorWriter(errors);
		e.eval(script);
		final String expectedOutput = "string written using 'print'\n" + "string written using 'io.write()'\n"
			+ "string written using 'io.stdout:write()'\n" + "string read using 'io.stdin:read(\"*l\")':abcdefg\n";
		assertEquals(expectedOutput, output.toString());
		final String expectedErrors = "string written using 'io.stderr:write()'\n";
		assertEquals(expectedErrors, errors.toString());

		// Evaluate script with redirection reset
		output.reset();
		errors.reset();
		// e.getContext().setReader(null); // This will block if using actual STDIN
		e.getContext().setWriter(null);
		e.getContext().setErrorWriter(null);
		e.eval(script);
		assertEquals("", output.toString());
		assertEquals("", errors.toString());
	}

	@Test
	void testBindingJavaInt() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("y = x; return 'x '..type(x)..' '..tostring(x)\n");
		b.put("x", 111);
		assertEquals("x number 111", cs.eval(b));
		assertEquals(111, b.get("y"));
	}

	@Test
	void testBindingJavaDouble() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("y = x; return 'x '..type(x)..' '..tostring(x)\n");
		b.put("x", 125.125);
		assertEquals("x number 125.125", cs.eval(b));
		assertEquals(125.125, b.get("y"));
	}

	@Test
	void testBindingJavaString() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("y = x; return 'x '..type(x)..' '..tostring(x)\n");
		b.put("x", "foo");
		assertEquals("x string foo", cs.eval(b));
		assertEquals("foo", b.get("y"));
	}

	@Test
	void testBindingJavaObject() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("y = x; return 'x '..type(x)..' '..tostring(x)\n");
		b.put("x", new SomeUserClass());
		assertEquals("x userdata some-user-value", cs.eval(b));
		assertEquals(SomeUserClass.class, b.get("y").getClass());
	}

	@Test
	void testBindingJavaArray() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("y = x; return 'x '..type(x)..' '..#x..' '..x[1]..' '..x[2]\n");
		b.put("x", new int[] { 777, 888 });
		assertEquals("x userdata 2 777 888", cs.eval(b));
		assertEquals(int[].class, b.get("y").getClass());
	}

	@Test
	void testBindingLuaFunction() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("y = function(x) return 678 + x end; return 'foo'");
		assertEquals("foo", cs.eval(b).toString());
		assertTrue(b.get("y") instanceof LuaFunction);
		assertEquals(LuaValue.valueOf(801), ((LuaFunction) b.get("y")).call(LuaValue.valueOf(123)));
	}

	@Test
	void testUserClasses() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("x = x or luajava.newInstance('java.lang.String', 'test')\n"
			+ "return 'x ' ..  type(x) .. ' ' .. tostring(x)\n");
		assertEquals("x string test", cs.eval(b));
		b.put("x", new SomeUserClass());
		assertEquals("x userdata some-user-value", cs.eval(b));
	}

	@Test
	void testReturnMultipleValues() throws ScriptException {
		CompiledScript cs = ((Compilable) e).compile("return 'foo', 'bar'\n");
		Object o = cs.eval();
		assertEquals(Object[].class, o.getClass());
		Object[] array = (Object[]) o;
		assertEquals(2, array.length);
		assertEquals("foo", array[0]);
		assertEquals("bar", array[1]);
	}

	private static class SomeUserClass {
		@Override
		public String toString() {
			return "some-user-value";
		}
	}
}
