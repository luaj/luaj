/*******************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
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
package org.luaj.jse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

class LuaPrototypeTest {

	private Prototype createPrototype(String script, String name) {
		try {
			Globals globals = JsePlatform.standardGlobals();
			Reader reader = new StringReader(script);
			return globals.compilePrototype(reader, name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.toString());
			return null;
		}
	}

	@Test
	void testFunctionClosureThreadEnv() {

		// set up suitable environments for execution
		LuaValue aaa = LuaValue.valueOf("aaa");
		LuaValue eee = LuaValue.valueOf("eee");
		final Globals globals = JsePlatform.standardGlobals();
		LuaTable newenv = LuaValue.tableOf(new LuaValue[] { LuaValue.valueOf("a"), LuaValue.valueOf("aaa"),
				LuaValue.valueOf("b"), LuaValue.valueOf("bbb"), });
		LuaTable mt = LuaValue.tableOf(new LuaValue[] { LuaValue.INDEX, globals });
		newenv.setmetatable(mt);
		globals.set("a", aaa);
		newenv.set("a", eee);

		// function tests
		{
			LuaFunction f = new ZeroArgFunction() {
				@Override
				public LuaValue call() { return globals.get("a"); }
			};
			assertEquals(aaa, f.call());
		}

		// closure tests
		{
			Prototype p = createPrototype("return a\n", "closuretester");
			LuaClosure c = new LuaClosure(p, globals);

			// Test that a clusure with a custom enviroment uses that environment.
			assertEquals(aaa, c.call());
			c = new LuaClosure(p, newenv);
			assertEquals(newenv, c.upValues[0].getValue());
			assertEquals(eee, c.call());
		}
	}
}
