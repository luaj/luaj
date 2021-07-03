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

import java.lang.ref.WeakReference;

abstract public class WeakTableTest extends TableTest {

	public static class MyData {
		public final int value;

		public MyData(int value) {
			this.value = value;
		}

		public int hashCode() {
			return value;
		}

		public boolean equals(Object o) {
			return (o instanceof MyData) && ((MyData) o).value == value;
		}

		public String toString() {
			return "mydata-" + value;
		}
	}

	static void collectGarbage() {
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		try {
			Thread.sleep(20);
			rt.gc();
			Thread.sleep(20);
		} catch (Exception e) {
			e.printStackTrace();
		}
		rt.gc();
	}

	public static class WeakValueTableTest extends WeakTableTest {
		protected LuaTable new_Table() { return WeakTable.make(false, true); }

		protected LuaTable new_Table(int n, int m) { return WeakTable.make(false, true); }

		public void testWeakValuesTable() {
			LuaTable t = new_Table();

			Object obj = new Object();
			LuaTable tableValue = new LuaTable();
			LuaString stringValue = LuaString.valueOf("this is a test");
			LuaTable tableValue2 = new LuaTable();

			t.set("table", tableValue);
			t.set("userdata", LuaValue.userdataOf(obj, null));
			t.set("string", stringValue);
			t.set("string2", LuaValue.valueOf("another string"));
			t.set(1, tableValue2);
			assertTrue("table must have at least 4 elements", t.getHashLength() >= 4);
			assertTrue("array part must have 1 element", t.getArrayLength() >= 1);

			// check that table can be used to get elements 
			assertEquals(tableValue, t.get("table"));
			assertEquals(stringValue, t.get("string"));
			assertEquals(obj, t.get("userdata").checkuserdata());
			assertEquals(tableValue2, t.get(1));

			// nothing should be collected, since we have strong references here
			collectGarbage();

			// check that elements are still there 
			assertEquals(tableValue, t.get("table"));
			assertEquals(stringValue, t.get("string"));
			assertEquals(obj, t.get("userdata").checkuserdata());
			assertEquals(tableValue2, t.get(1));

			// drop our strong references
			obj = null;
			tableValue = null;
			tableValue2 = null;
			stringValue = null;

			// Garbage collection should cause weak entries to be dropped.
			collectGarbage();

			// check that they are dropped
			assertEquals(LuaValue.NIL, t.get("table"));
			assertEquals(LuaValue.NIL, t.get("userdata"));
			assertEquals(LuaValue.NIL, t.get(1));
			assertFalse("strings should not be in weak references", t.get("string").isnil());
		}
	}

	public static class WeakKeyTableTest extends WeakTableTest {
		protected LuaTable new_Table() { return WeakTable.make(true, false); }

		protected LuaTable new_Table(int n, int m) { return WeakTable.make(true, false); }

		public void testWeakKeysTable() {
			LuaTable t = WeakTable.make(true, false);

			LuaValue key = LuaValue.userdataOf(new MyData(111));
			LuaValue val = LuaValue.userdataOf(new MyData(222));

			// set up the table
			t.set(key, val);
			assertEquals(val, t.get(key));
			System.gc();
			assertEquals(val, t.get(key));

			// drop key and value references, replace them with new ones
			WeakReference origkey = new WeakReference(key);
			WeakReference origval = new WeakReference(val);
			key = LuaValue.userdataOf(new MyData(111));
			val = LuaValue.userdataOf(new MyData(222));

			// new key and value should be interchangeable (feature of this test class)
			assertEquals(key, origkey.get());
			assertEquals(val, origval.get());
			assertEquals(val, t.get(key));
			assertEquals(val, t.get((LuaValue) origkey.get()));
			assertEquals(origval.get(), t.get(key));

			// value should not be reachable after gc
			collectGarbage();
			assertEquals(null, origkey.get());
			assertEquals(LuaValue.NIL, t.get(key));
			collectGarbage();
			assertEquals(null, origval.get());
		}

		public void testNext() {
			LuaTable t = WeakTable.make(true, true);

			LuaValue key = LuaValue.userdataOf(new MyData(111));
			LuaValue val = LuaValue.userdataOf(new MyData(222));
			LuaValue key2 = LuaValue.userdataOf(new MyData(333));
			LuaValue val2 = LuaValue.userdataOf(new MyData(444));
			LuaValue key3 = LuaValue.userdataOf(new MyData(555));
			LuaValue val3 = LuaValue.userdataOf(new MyData(666));

			// set up the table
			t.set(key, val);
			t.set(key2, val2);
			t.set(key3, val3);

			// forget one of the keys
			key2 = null;
			val2 = null;
			collectGarbage();

			// table should have 2 entries
			int size = 0;
			for (LuaValue k = t.next(LuaValue.NIL).arg1(); !k.isnil(); k = t.next(k).arg1()) {
				size++;
			}
			assertEquals(2, size);
		}
	}

	public static class WeakKeyValueTableTest extends WeakTableTest {
		protected LuaTable new_Table() { return WeakTable.make(true, true); }

		protected LuaTable new_Table(int n, int m) { return WeakTable.make(true, true); }

		public void testWeakKeysValuesTable() {
			LuaTable t = WeakTable.make(true, true);

			LuaValue key = LuaValue.userdataOf(new MyData(111));
			LuaValue val = LuaValue.userdataOf(new MyData(222));
			LuaValue key2 = LuaValue.userdataOf(new MyData(333));
			LuaValue val2 = LuaValue.userdataOf(new MyData(444));
			LuaValue key3 = LuaValue.userdataOf(new MyData(555));
			LuaValue val3 = LuaValue.userdataOf(new MyData(666));

			// set up the table
			t.set(key, val);
			t.set(key2, val2);
			t.set(key3, val3);
			assertEquals(val, t.get(key));
			assertEquals(val2, t.get(key2));
			assertEquals(val3, t.get(key3));
			System.gc();
			assertEquals(val, t.get(key));
			assertEquals(val2, t.get(key2));
			assertEquals(val3, t.get(key3));

			// drop key and value references, replace them with new ones
			WeakReference origkey = new WeakReference(key);
			WeakReference origval = new WeakReference(val);
			WeakReference origkey2 = new WeakReference(key2);
			WeakReference origval2 = new WeakReference(val2);
			WeakReference origkey3 = new WeakReference(key3);
			WeakReference origval3 = new WeakReference(val3);
			key = LuaValue.userdataOf(new MyData(111));
			val = LuaValue.userdataOf(new MyData(222));
			key2 = LuaValue.userdataOf(new MyData(333));
			// don't drop val2, or key3
			val3 = LuaValue.userdataOf(new MyData(666));

			// no values should be reachable after gc
			collectGarbage();
			assertEquals(null, origkey.get());
			assertEquals(null, origval.get());
			assertEquals(null, origkey2.get());
			assertEquals(null, origval3.get());
			assertEquals(LuaValue.NIL, t.get(key));
			assertEquals(LuaValue.NIL, t.get(key2));
			assertEquals(LuaValue.NIL, t.get(key3));

			// all originals should be gone after gc, then access
			val2 = null;
			key3 = null;
			collectGarbage();
			assertEquals(null, origval2.get());
			assertEquals(null, origkey3.get());
		}

		public void testReplace() {
			LuaTable t = WeakTable.make(true, true);

			LuaValue key = LuaValue.userdataOf(new MyData(111));
			LuaValue val = LuaValue.userdataOf(new MyData(222));
			LuaValue key2 = LuaValue.userdataOf(new MyData(333));
			LuaValue val2 = LuaValue.userdataOf(new MyData(444));
			LuaValue key3 = LuaValue.userdataOf(new MyData(555));
			LuaValue val3 = LuaValue.userdataOf(new MyData(666));

			// set up the table
			t.set(key, val);
			t.set(key2, val2);
			t.set(key3, val3);

			LuaValue val4 = LuaValue.userdataOf(new MyData(777));
			t.set(key2, val4);

			// table should have 3 entries
			int size = 0;
			for (LuaValue k = t.next(LuaValue.NIL).arg1(); !k.isnil() && size < 1000; k = t.next(k).arg1()) {
				size++;
			}
			assertEquals(3, size);
		}
	}
}
