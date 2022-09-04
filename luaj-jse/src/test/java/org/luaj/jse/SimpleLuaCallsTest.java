package org.luaj.jse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

class SimpleLuaCallsTest {

	private Globals globals;

	@BeforeEach
	protected void setUp() throws Exception {
		globals = JsePlatform.standardGlobals();
	}

	private void doTest(String script) {
		try {
			LuaValue c = globals.load(script, "script");
			c.call();
		} catch (Exception e) {
			fail("i/o exception: " + e);
		}
	}

	@Test
	void testTrivial() {
		String s = "print( 2 )\n";
		doTest(s);
	}

	@Test
	void testAlmostTrivial() {
		String s = "print( 2 )\n" + "print( 3 )\n";
		doTest(s);
	}

	@Test
	void testSimple() {
		String s = "print( 'hello, world' )\n" + "for i = 2,4 do\n" + "	print( 'i', i )\n" + "end\n";
		doTest(s);
	}

	@Test
	void testBreak() {
		String s = "a=1\n" + "while true do\n" + "  if a>10 then\n" + "     break\n" + "  end\n" + "  a=a+1\n"
			+ "  print( a )\n" + "end\n";
		doTest(s);
	}

	@Test
	void testShebang() {
		String s = "#!../lua\n" + "print( 2 )\n";
		doTest(s);
	}

	@Test
	void testInlineTable() {
		String s = "A = {g=10}\n" + "print( A )\n";
		doTest(s);
	}

	@Test
	void testEqualsAnd() {
		String s = "print( 1 == b and b )\n";
		doTest(s);
	}

	private static final int[]    samehash = { 0, 1, -1, 2, -2, 4, 8, 16, 32, Integer.MAX_VALUE, Integer.MIN_VALUE };
	private static final double[] diffhash = { .5, 1, 1.5, 1, .5, 1.5, 1.25, 2.5 };

	@Test
	void testDoubleHashCode() {
		for (int i = 0; i < samehash.length; i++) {
			LuaValue j = LuaInteger.valueOf(samehash[i]);
			LuaValue d = LuaDouble.valueOf(samehash[i]);
			int hj = j.hashCode();
			int hd = d.hashCode();
			assertEquals(hj, hd);
		}
		for (int i = 0; i < diffhash.length; i += 2) {
			LuaValue c = LuaValue.valueOf(diffhash[i+0]);
			LuaValue d = LuaValue.valueOf(diffhash[i+1]);
			int hc = c.hashCode();
			int hd = d.hashCode();
			assertTrue(hc != hd, "hash codes are same: " + hc);
		}
	}
}
