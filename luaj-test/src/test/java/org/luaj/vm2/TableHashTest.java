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
package org.luaj.vm2;

import junit.framework.TestCase;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * Tests for tables used as lists.
 */
public class TableHashTest extends TestCase {

	protected LuaTable new_Table() {
		return new LuaTable();
	}

	protected LuaTable new_Table(int n, int m) {
		return new LuaTable(n, m);
	}

	public void testSetRemove() {
		LuaTable t = new_Table();

		assertEquals(0, t.getHashLength());
		assertEquals(0, t.length());
		assertEquals(0, t.keyCount());

		String[] keys = { "abc", "def", "ghi", "jkl", "mno", "pqr", "stu", "wxy", "z01", "cd", "ef", "g", "hi", "jk",
				"lm", "no", "pq", "rs", };
		int[] capacities = { 0, 2, 2, 4, 4, 8, 8, 8, 8, 16, 16, 16, 16, 16, 16, 16, 16, 32, 32, 32 };
		for (int i = 0; i < keys.length; ++i) {
			assertEquals(capacities[i], t.getHashLength());
			String si = "Test Value! " + i;
			t.set(keys[i], si);
			assertEquals(0, t.length());
			assertEquals(i+1, t.keyCount());
		}
		assertEquals(capacities[keys.length], t.getHashLength());
		for (int i = 0; i < keys.length; ++i) {
			LuaValue vi = LuaString.valueOf("Test Value! " + i);
			assertEquals(vi, t.get(keys[i]));
			assertEquals(vi, t.get(LuaString.valueOf(keys[i])));
			assertEquals(vi, t.rawget(keys[i]));
			assertEquals(vi, t.rawget(keys[i]));
		}

		// replace with new values
		for (int i = 0; i < keys.length; ++i) {
			t.set(keys[i], LuaString.valueOf("Replacement Value! " + i));
			assertEquals(0, t.length());
			assertEquals(keys.length, t.keyCount());
			assertEquals(capacities[keys.length], t.getHashLength());
		}
		for (int i = 0; i < keys.length; ++i) {
			LuaValue vi = LuaString.valueOf("Replacement Value! " + i);
			assertEquals(vi, t.get(keys[i]));
		}

		// remove
		for (int i = 0; i < keys.length; ++i) {
			t.set(keys[i], LuaValue.NIL);
			assertEquals(0, t.length());
			assertEquals(keys.length-i-1, t.keyCount());
			if (i < keys.length-1)
				assertEquals(capacities[keys.length], t.getHashLength());
			else
				assertTrue(0 <= t.getHashLength());
		}
		for (int i = 0; i < keys.length; ++i) {
			assertEquals(LuaValue.NIL, t.get(keys[i]));
		}
	}

	public void testIndexMetatag() {
		LuaTable t = new_Table();
		LuaTable mt = new_Table();
		LuaTable fb = new_Table();

		// set basic values
		t.set("ppp", "abc");
		t.set(123, "def");
		mt.set(LuaValue.INDEX, fb);
		fb.set("qqq", "ghi");
		fb.set(456, "jkl");

		// check before setting metatable
		assertEquals("abc", t.get("ppp").tojstring());
		assertEquals("def", t.get(123).tojstring());
		assertEquals("nil", t.get("qqq").tojstring());
		assertEquals("nil", t.get(456).tojstring());
		assertEquals("nil", fb.get("ppp").tojstring());
		assertEquals("nil", fb.get(123).tojstring());
		assertEquals("ghi", fb.get("qqq").tojstring());
		assertEquals("jkl", fb.get(456).tojstring());
		assertEquals("nil", mt.get("ppp").tojstring());
		assertEquals("nil", mt.get(123).tojstring());
		assertEquals("nil", mt.get("qqq").tojstring());
		assertEquals("nil", mt.get(456).tojstring());

		// check before setting metatable
		t.setmetatable(mt);
		assertEquals(mt, t.getmetatable());
		assertEquals("abc", t.get("ppp").tojstring());
		assertEquals("def", t.get(123).tojstring());
		assertEquals("ghi", t.get("qqq").tojstring());
		assertEquals("jkl", t.get(456).tojstring());
		assertEquals("nil", fb.get("ppp").tojstring());
		assertEquals("nil", fb.get(123).tojstring());
		assertEquals("ghi", fb.get("qqq").tojstring());
		assertEquals("jkl", fb.get(456).tojstring());
		assertEquals("nil", mt.get("ppp").tojstring());
		assertEquals("nil", mt.get(123).tojstring());
		assertEquals("nil", mt.get("qqq").tojstring());
		assertEquals("nil", mt.get(456).tojstring());

		// set metatable to metatable without values
		t.setmetatable(fb);
		assertEquals("abc", t.get("ppp").tojstring());
		assertEquals("def", t.get(123).tojstring());
		assertEquals("nil", t.get("qqq").tojstring());
		assertEquals("nil", t.get(456).tojstring());

		// set metatable to null
		t.setmetatable(null);
		assertEquals("abc", t.get("ppp").tojstring());
		assertEquals("def", t.get(123).tojstring());
		assertEquals("nil", t.get("qqq").tojstring());
		assertEquals("nil", t.get(456).tojstring());
	}

	public void testIndexFunction() {
		final LuaTable t = new_Table();
		final LuaTable mt = new_Table();

		final TwoArgFunction fb = new TwoArgFunction() {
			public LuaValue call(LuaValue tbl, LuaValue key) {
				assertEquals(tbl, t);
				return valueOf("from mt: " + key);
			}
		};

		// set basic values
		t.set("ppp", "abc");
		t.set(123, "def");
		mt.set(LuaValue.INDEX, fb);

		// check before setting metatable
		assertEquals("abc", t.get("ppp").tojstring());
		assertEquals("def", t.get(123).tojstring());
		assertEquals("nil", t.get("qqq").tojstring());
		assertEquals("nil", t.get(456).tojstring());

		// check before setting metatable
		t.setmetatable(mt);
		assertEquals(mt, t.getmetatable());
		assertEquals("abc", t.get("ppp").tojstring());
		assertEquals("def", t.get(123).tojstring());
		assertEquals("from mt: qqq", t.get("qqq").tojstring());
		assertEquals("from mt: 456", t.get(456).tojstring());

		// use raw set
		t.rawset("qqq", "alt-qqq");
		t.rawset(456, "alt-456");
		assertEquals("abc", t.get("ppp").tojstring());
		assertEquals("def", t.get(123).tojstring());
		assertEquals("alt-qqq", t.get("qqq").tojstring());
		assertEquals("alt-456", t.get(456).tojstring());

		// remove using raw set
		t.rawset("qqq", LuaValue.NIL);
		t.rawset(456, LuaValue.NIL);
		assertEquals("abc", t.get("ppp").tojstring());
		assertEquals("def", t.get(123).tojstring());
		assertEquals("from mt: qqq", t.get("qqq").tojstring());
		assertEquals("from mt: 456", t.get(456).tojstring());

		// set metatable to null
		t.setmetatable(null);
		assertEquals("abc", t.get("ppp").tojstring());
		assertEquals("def", t.get(123).tojstring());
		assertEquals("nil", t.get("qqq").tojstring());
		assertEquals("nil", t.get(456).tojstring());
	}

	public void testNext() {
		final LuaTable t = new_Table();
		assertEquals(LuaValue.NIL, t.next(LuaValue.NIL));

		// insert array elements
		t.set(1, "one");
		assertEquals(LuaValue.valueOf(1), t.next(LuaValue.NIL).arg(1));
		assertEquals(LuaValue.valueOf("one"), t.next(LuaValue.NIL).arg(2));
		assertEquals(LuaValue.NIL, t.next(LuaValue.ONE));
		t.set(2, "two");
		assertEquals(LuaValue.valueOf(1), t.next(LuaValue.NIL).arg(1));
		assertEquals(LuaValue.valueOf("one"), t.next(LuaValue.NIL).arg(2));
		assertEquals(LuaValue.valueOf(2), t.next(LuaValue.ONE).arg(1));
		assertEquals(LuaValue.valueOf("two"), t.next(LuaValue.ONE).arg(2));
		assertEquals(LuaValue.NIL, t.next(LuaValue.valueOf(2)));

		// insert hash elements
		t.set("aa", "aaa");
		assertEquals(LuaValue.valueOf(1), t.next(LuaValue.NIL).arg(1));
		assertEquals(LuaValue.valueOf("one"), t.next(LuaValue.NIL).arg(2));
		assertEquals(LuaValue.valueOf(2), t.next(LuaValue.ONE).arg(1));
		assertEquals(LuaValue.valueOf("two"), t.next(LuaValue.ONE).arg(2));
		assertEquals(LuaValue.valueOf("aa"), t.next(LuaValue.valueOf(2)).arg(1));
		assertEquals(LuaValue.valueOf("aaa"), t.next(LuaValue.valueOf(2)).arg(2));
		assertEquals(LuaValue.NIL, t.next(LuaValue.valueOf("aa")));
		t.set("bb", "bbb");
		assertEquals(LuaValue.valueOf(1), t.next(LuaValue.NIL).arg(1));
		assertEquals(LuaValue.valueOf("one"), t.next(LuaValue.NIL).arg(2));
		assertEquals(LuaValue.valueOf(2), t.next(LuaValue.ONE).arg(1));
		assertEquals(LuaValue.valueOf("two"), t.next(LuaValue.ONE).arg(2));
		assertEquals(LuaValue.valueOf("aa"), t.next(LuaValue.valueOf(2)).arg(1));
		assertEquals(LuaValue.valueOf("aaa"), t.next(LuaValue.valueOf(2)).arg(2));
		assertEquals(LuaValue.valueOf("bb"), t.next(LuaValue.valueOf("aa")).arg(1));
		assertEquals(LuaValue.valueOf("bbb"), t.next(LuaValue.valueOf("aa")).arg(2));
		assertEquals(LuaValue.NIL, t.next(LuaValue.valueOf("bb")));
	}

	public void testLoopWithRemoval() {
		final LuaTable t = new_Table();

		t.set(LuaValue.valueOf(1), LuaValue.valueOf("1"));
		t.set(LuaValue.valueOf(3), LuaValue.valueOf("3"));
		t.set(LuaValue.valueOf(8), LuaValue.valueOf("4"));
		t.set(LuaValue.valueOf(17), LuaValue.valueOf("5"));
		t.set(LuaValue.valueOf(26), LuaValue.valueOf("6"));
		t.set(LuaValue.valueOf(35), LuaValue.valueOf("7"));
		t.set(LuaValue.valueOf(42), LuaValue.valueOf("8"));
		t.set(LuaValue.valueOf(60), LuaValue.valueOf("10"));
		t.set(LuaValue.valueOf(63), LuaValue.valueOf("11"));

		Varargs entry = t.next(LuaValue.NIL);
		while ( !entry.isnil(1) ) {
			LuaValue k = entry.arg1();
			LuaValue v = entry.arg(2);
			if ((k.toint() & 1) == 0) {
				t.set(k, LuaValue.NIL);
			}
			entry = t.next(k);
		}

		int numEntries = 0;
		entry = t.next(LuaValue.NIL);
		while ( !entry.isnil(1) ) {
			LuaValue k = entry.arg1();
			// Only odd keys should remain
			assertTrue((k.toint() & 1) == 1);
			numEntries++;
			entry = t.next(k);
		}
		assertEquals(5, numEntries);
	}

	public void testLoopWithRemovalAndSet() {
		final LuaTable t = new_Table();

		t.set(LuaValue.valueOf(1), LuaValue.valueOf("1"));
		t.set(LuaValue.valueOf(3), LuaValue.valueOf("3"));
		t.set(LuaValue.valueOf(8), LuaValue.valueOf("4"));
		t.set(LuaValue.valueOf(17), LuaValue.valueOf("5"));
		t.set(LuaValue.valueOf(26), LuaValue.valueOf("6"));
		t.set(LuaValue.valueOf(35), LuaValue.valueOf("7"));
		t.set(LuaValue.valueOf(42), LuaValue.valueOf("8"));
		t.set(LuaValue.valueOf(60), LuaValue.valueOf("10"));
		t.set(LuaValue.valueOf(63), LuaValue.valueOf("11"));

		Varargs entry = t.next(LuaValue.NIL);
		Varargs entry2 = entry;
		while ( !entry.isnil(1) ) {
			LuaValue k = entry.arg1();
			LuaValue v = entry.arg(2);
			if ((k.toint() & 1) == 0) {
				t.set(k, LuaValue.NIL);
			} else {
				t.set(k, v.tonumber());
				entry2 = t.next(entry2.arg1());
			}
			entry = t.next(k);
		}

		int numEntries = 0;
		entry = t.next(LuaValue.NIL);
		while ( !entry.isnil(1) ) {
			LuaValue k = entry.arg1();
			// Only odd keys should remain
			assertTrue((k.toint() & 1) == 1);
			assertTrue(entry.arg(2).type() == LuaValue.TNUMBER);
			numEntries++;
			entry = t.next(k);
		}
		assertEquals(5, numEntries);
	}
}
