/*******************************************************************************
 * Copyright (c) 2013 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm2.script;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

public class ScriptEngineTests extends TestSuite  {

	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Script Engine Tests");
		suite.addTest( new TestSuite( LookupEngineTestCase.class,   "Lookup Engine" ) );
		suite.addTest( new TestSuite( SimpleBindingsTest.class,   "Simple Bindings" ) );
		suite.addTest( new TestSuite( DefaultBindingsTest.class,   "Default Bindings" ) );
		suite.addTest( new TestSuite( LuaJCBindingsTest.class,   "LuaJC Bindings" ) );
		suite.addTest( new TestSuite( UserContextTest.class,   "User Context" ) );
		return suite;
	}
	
	public static class LookupEngineTestCase extends TestCase {	
		public void testGetEngineByExtension() {
	        ScriptEngine e = new ScriptEngineManager().getEngineByExtension(".lua");
	        assertNotNull(e);
	        assertEquals(LuaScriptEngine.class, e.getClass());
		}
		public void testGetEngineByName() {
	        ScriptEngine e = new ScriptEngineManager().getEngineByName("luaj");
	        assertNotNull(e);
	        assertEquals(LuaScriptEngine.class, e.getClass());
		}
		public void testGetEngineByMimeType() {
	        ScriptEngine e = new ScriptEngineManager().getEngineByMimeType("text/lua");
	        assertNotNull(e);
	        assertEquals(LuaScriptEngine.class, e.getClass());
		}
		public void testFactoryMetadata() {
	        ScriptEngine e = new ScriptEngineManager().getEngineByName("luaj");
	        ScriptEngineFactory f = e.getFactory();
	        assertEquals("Luaj", f.getEngineName());
	        assertEquals("Luaj 0.0", f.getEngineVersion());
	        assertEquals("lua", f.getLanguageName());
	        assertEquals("5.2", f.getLanguageVersion());
		}
	}
	
	public static class SimpleBindingsTest extends EngineTestCase {
		protected Bindings createBindings() {
			return new SimpleBindings();
		}
		public void testCompiledFunctionIsClosure() throws ScriptException {
            CompiledScript cs = ((Compilable)e).compile("return 'foo'");
            assertTrue(((LuaScriptEngine.LuajCompiledScript)cs).function.isclosure());
		}
	}
	
	public static class DefaultBindingsTest extends EngineTestCase {
		protected Bindings createBindings() {
			return e.createBindings();
		}
	}

	public static class LuaJCBindingsTest extends EngineTestCase {
		static {
			System.setProperty("org.luaj.luajc", "true");
		}
		protected Bindings createBindings() {
			return new SimpleBindings();
		}
		public void setUp() {
			super.setUp();
		}
		public void testCompiledFunctionIsNotClosure() throws ScriptException {
            CompiledScript cs = ((Compilable)e).compile("return 'foo'");
            assertFalse(((LuaScriptEngine.LuajCompiledScript)cs).function.isclosure());
		}
	}

	abstract public static class EngineTestCase extends TestCase {	
		protected ScriptEngine e;
		protected Bindings b;
		abstract protected Bindings createBindings();
		public void setUp() {
	       	this.e = new ScriptEngineManager().getEngineByName("luaj");
			this.b = createBindings();
		}
		public void testSqrtIntResult() throws ScriptException {
            e.put("x", 25);
            e.eval("y = math.sqrt(x)");
            Object y = e.get("y");
            assertEquals(5, y);
		}
		public void testOneArgFunction() throws ScriptException {
            e.put("x", 25);
            e.eval("y = math.sqrt(x)");
            Object y = e.get("y");
            assertEquals(5, y);
            e.put("f", new OneArgFunction() {
 				public LuaValue call(LuaValue arg) {
 					return LuaValue.valueOf(arg.toString()+"123");
 				}
             });
            Object r = e.eval("return f('abc')");
            assertEquals("abc123", r);
		}
		public void testCompiledScript() throws ScriptException {
            CompiledScript cs = ((Compilable)e).compile("y = math.sqrt(x); return y");
            b.put("x", 144);
            assertEquals(12, cs.eval(b));
		}
		public void testBuggyLuaScript() {
	        try {
	        	e.eval("\n\nbuggy lua code\n\n");
	        } catch ( ScriptException se ) {
	        	assertEquals("eval threw javax.script.ScriptException: [string \"script\"]:3: syntax error", se.getMessage());
	        	return;
	        }
	        fail("buggy script did not throw ScriptException as expected.");
		}
		public void testScriptRedirection() throws ScriptException {
            Reader input = new CharArrayReader("abcdefg\nhijk".toCharArray());
            CharArrayWriter output = new CharArrayWriter();
            CharArrayWriter errors = new CharArrayWriter();
            String script = 
            		"print(\"string written using 'print'\")\n" +
            		"io.write(\"string written using 'io.write()'\\n\")\n" +
            		"io.stdout:write(\"string written using 'io.stdout:write()'\\n\")\n" +
            		"io.stderr:write(\"string written using 'io.stderr:write()'\\n\")\n" +
            		"io.write([[string read using 'io.stdin:read(\"*l\")':]]..io.stdin:read(\"*l\")..\"\\n\")\n";

            // Evaluate script with redirection set
            e.getContext().setReader(input);
            e.getContext().setWriter(output);
            e.getContext().setErrorWriter(errors);
            e.eval(script);
            final String expectedOutput = "string written using 'print'\n"+
            		"string written using 'io.write()'\n"+
            		"string written using 'io.stdout:write()'\n"+
            		"string read using 'io.stdin:read(\"*l\")':abcdefg\n";
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
		public void testBindingJavaInt() throws ScriptException {
	        CompiledScript cs = ((Compilable)e).compile("y = x; return 'x '..type(x)..' '..tostring(x)\n");
	        b.put("x", 111);
	        assertEquals("x number 111", cs.eval(b));
	        assertEquals(111, b.get("y"));
		}
		public void testBindingJavaDouble() throws ScriptException {
	        CompiledScript cs = ((Compilable)e).compile("y = x; return 'x '..type(x)..' '..tostring(x)\n");
	        b.put("x", 125.125);
	        assertEquals("x number 125.125", cs.eval(b));
	        assertEquals(125.125, b.get("y"));
		}
		public void testBindingJavaString() throws ScriptException {
	        CompiledScript cs = ((Compilable)e).compile("y = x; return 'x '..type(x)..' '..tostring(x)\n");
	        b.put("x", "foo");
	        assertEquals("x string foo", cs.eval(b));
	        assertEquals("foo", b.get("y"));
		}
		public void testBindingJavaObject() throws ScriptException {
	        CompiledScript cs = ((Compilable)e).compile("y = x; return 'x '..type(x)..' '..tostring(x)\n");
	        b.put("x", new SomeUserClass());
	        assertEquals("x userdata some-user-value", cs.eval(b));
	        assertEquals(SomeUserClass.class, b.get("y").getClass());
		}
		public void testBindingJavaArray() throws ScriptException {
	        CompiledScript cs = ((Compilable)e).compile("y = x; return 'x '..type(x)..' '..#x..' '..x[1]..' '..x[2]\n");
	        b.put("x", new int[] { 777, 888 });
	        assertEquals("x userdata 2 777 888", cs.eval(b));
	        assertEquals(int[].class, b.get("y").getClass());
		}
		public void testBindingLuaFunction() throws ScriptException {
	        CompiledScript cs = ((Compilable)e).compile("y = function(x) return 678 + x end; return 'foo'");
	        assertEquals("foo", cs.eval(b).toString());
	        assertTrue(b.get("y") instanceof LuaFunction);
	        assertEquals(LuaValue.valueOf(801), ((LuaFunction) b.get("y")).call(LuaValue.valueOf(123)));
		}
	    public void testUserClasses() throws ScriptException {
	        CompiledScript cs = ((Compilable)e).compile(
	        		"x = x or luajava.newInstance('java.lang.String', 'test')\n" +
	        		"return 'x ' ..  type(x) .. ' ' .. tostring(x)\n");
	        assertEquals("x string test", cs.eval(b));
	        b.put("x", new SomeUserClass());
	        assertEquals("x userdata some-user-value", cs.eval(b));
	    }	
		public void testReturnMultipleValues() throws ScriptException {
	        CompiledScript cs = ((Compilable)e).compile("return 'foo', 'bar'\n");
	        Object o = cs.eval();
	        assertEquals(Object[].class, o.getClass());
	        Object[] array = (Object[]) o;
	        assertEquals(2, array.length);
	        assertEquals("foo", array[0]);
	        assertEquals("bar", array[1]);
		}
	}

	public static class SomeUserClass {
    	public String toString() {
    		return "some-user-value";
    	}
    }

	public static class UserContextTest extends TestCase {	
		protected ScriptEngine e;
		protected Bindings b;
		protected ScriptContext c;
		public void setUp() {
	       	this.e = new ScriptEngineManager().getEngineByName("luaj");
			this.c = new LuajContext();
			this.b = c.getBindings(ScriptContext.ENGINE_SCOPE);
		}
		public void testUncompiledScript() throws ScriptException {
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
		public void testCompiledScript() throws ScriptException {
            CompiledScript cs = ((Compilable)e).compile("z = math.sqrt(x); return z");
            
            b.put("x", 144);
            assertEquals(12, cs.eval(b));
            assertEquals(12, b.get("z"));

            b.put("x", 25);
            assertEquals(5, cs.eval(c));
            assertEquals(5, b.get("z"));
		}
	}
}
