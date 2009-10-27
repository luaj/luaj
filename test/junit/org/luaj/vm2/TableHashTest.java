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
	
	protected LuaTable new_Table(int n,int m) {
		return new LuaTable(n,m);
	}
	
	public void testSetRemove() {
		LuaTable t = new_Table();
		
		assertEquals( 0, t.hashCapacity() );
		assertEquals( 0, t.length() );
		assertEquals( 0, t.keyCount() );
		
		String[] keys = { "abc", "def", "ghi", "jkl", "mno", "pqr", "stu", "wxy", "z01",
				"cd", "ef", "g", "hi", "jk", "lm", "no", "pq", "rs", };		
		int[] capacities = { 0, 2, 4, 4, 7, 7, 7, 10, 10, 14, 14, 14, 14, 19, 19, 19, 19, 25, 25, 25 };
		for ( int i = 0; i < keys.length; ++i ) {
			assertEquals( capacities[i], t.hashCapacity() );
			String si = "Test Value! "+i;
			t.set( keys[i], si );
			assertEquals( 0, t.length() );
			assertEquals( i+1, t.keyCount() );
		}
		assertEquals( capacities[keys.length], t.hashCapacity() );
		for ( int i = 0; i < keys.length; ++i ) {
			LuaValue vi = LuaString.valueOf( "Test Value! "+i );
			assertEquals( vi, t.get( keys[i] ) );
			assertEquals( vi, t.get( LuaString.valueOf(keys[i]) ) );
			assertEquals( vi, t.rawget( keys[i] ) );
			assertEquals( vi, t.rawget( keys[i] ) );
		}

		// replace with new values
		for ( int i = 0; i < keys.length; ++i ) {
			t.set( keys[i], LuaString.valueOf( "Replacement Value! "+i ) );
			assertEquals( 0, t.length() );
			assertEquals( keys.length, t.keyCount() );
			assertEquals( capacities[keys.length], t.hashCapacity() );
		}
		for ( int i = 0; i < keys.length; ++i ) {
			LuaValue vi = LuaString.valueOf( "Replacement Value! "+i );
			assertEquals( vi, t.get( keys[i] ) );
		}

		// remove
		for ( int i = 0; i < keys.length; ++i ) {
			t.set( keys[i], LuaValue.NIL );
			assertEquals( 0, t.length() );
			assertEquals( keys.length-i-1, t.keyCount() );
			if ( i<keys.length-1 )
				assertEquals( capacities[keys.length], t.hashCapacity() );
			else
				assertTrue( 0<=t.hashCapacity()  );
		}
		for ( int i = 0; i < keys.length; ++i ) {
			assertEquals( LuaValue.NIL, t.get( keys[i] ) );			
		}
	}
	
	public void testIndexMetatag() {
		LuaTable t = new_Table();
		LuaTable mt = new_Table();
		LuaTable fb = new_Table();
		
		// set basic values
		t.set( "ppp", "abc" );
		t.set( 123, "def" );
		mt.set(LuaValue.INDEX, fb);
		fb.set( "qqq", "ghi" );
		fb.set( 456, "jkl" );
		
		// check before setting metatable
		assertEquals( "abc", t.get("ppp").toString() );
		assertEquals( "def", t.get(123).toString() );
		assertEquals( "nil", t.get("qqq").toString() );
		assertEquals( "nil", t.get(456).toString() );
		assertEquals( "nil", fb.get("ppp").toString() );
		assertEquals( "nil", fb.get(123).toString() );
		assertEquals( "ghi", fb.get("qqq").toString() );
		assertEquals( "jkl", fb.get(456).toString() );
		assertEquals( "nil", mt.get("ppp").toString() );
		assertEquals( "nil", mt.get(123).toString() );
		assertEquals( "nil", mt.get("qqq").toString() );
		assertEquals( "nil", mt.get(456).toString() );
		
		// check before setting metatable
		t.setmetatable(mt);
		assertEquals( mt, t.getmetatable() );
		assertEquals( "abc", t.get("ppp").toString() );
		assertEquals( "def", t.get(123).toString() );
		assertEquals( "ghi", t.get("qqq").toString() );
		assertEquals( "jkl", t.get(456).toString() );
		assertEquals( "nil", fb.get("ppp").toString() );
		assertEquals( "nil", fb.get(123).toString() );
		assertEquals( "ghi", fb.get("qqq").toString() );
		assertEquals( "jkl", fb.get(456).toString() );
		assertEquals( "nil", mt.get("ppp").toString() );
		assertEquals( "nil", mt.get(123).toString() );
		assertEquals( "nil", mt.get("qqq").toString() );
		assertEquals( "nil", mt.get(456).toString() );

		// set metatable to metatable without values
		t.setmetatable(fb);
		assertEquals( "abc", t.get("ppp").toString() );
		assertEquals( "def", t.get(123).toString() );
		assertEquals( "nil", t.get("qqq").toString() );
		assertEquals( "nil", t.get(456).toString() );
		
		// set metatable to null
		t.setmetatable(null);
		assertEquals( "abc", t.get("ppp").toString() );
		assertEquals( "def", t.get(123).toString() );
		assertEquals( "nil", t.get("qqq").toString() );
		assertEquals( "nil", t.get(456).toString() );
	}		

	public void testIndexFunction() {
		final LuaTable t = new_Table();
		final LuaTable mt = new_Table();		
		
		final TwoArgFunction fb = new TwoArgFunction() {
			public LuaValue call(LuaValue tbl, LuaValue key) {
				assertEquals(tbl, t);
				return valueOf("from mt: "+key);
			}
		};
		
		// set basic values
		t.set( "ppp", "abc" );
		t.set( 123, "def" );
		mt.set(LuaValue.INDEX, fb);
		
		// check before setting metatable
		assertEquals( "abc", t.get("ppp").toString() );
		assertEquals( "def", t.get(123).toString() );
		assertEquals( "nil", t.get("qqq").toString() );
		assertEquals( "nil", t.get(456).toString() );
		
		
		// check before setting metatable
		t.setmetatable(mt);
		assertEquals( mt, t.getmetatable() );
		assertEquals( "abc", t.get("ppp").toString() );
		assertEquals( "def", t.get(123).toString() );
		assertEquals( "from mt: qqq", t.get("qqq").toString() );
		assertEquals( "from mt: 456", t.get(456).toString() );
		
		// use raw set
		t.rawset("qqq", "alt-qqq");
		t.rawset(456, "alt-456");
		assertEquals( "abc", t.get("ppp").toString() );
		assertEquals( "def", t.get(123).toString() );
		assertEquals( "alt-qqq", t.get("qqq").toString() );
		assertEquals( "alt-456", t.get(456).toString() );

		// remove using raw set
		t.rawset("qqq", LuaValue.NIL);
		t.rawset(456, LuaValue.NIL);
		assertEquals( "abc", t.get("ppp").toString() );
		assertEquals( "def", t.get(123).toString() );
		assertEquals( "from mt: qqq", t.get("qqq").toString() );
		assertEquals( "from mt: 456", t.get(456).toString() );

		// set metatable to null
		t.setmetatable(null);
		assertEquals( "abc", t.get("ppp").toString() );
		assertEquals( "def", t.get(123).toString() );
		assertEquals( "nil", t.get("qqq").toString() );
		assertEquals( "nil", t.get(456).toString() );
	}
	
	public void testNext() {
		final LuaTable t = new_Table();
		assertEquals( LuaValue.NIL, t.next(LuaValue.NIL) );
		
		// insert array elements
		t.set( 1, "one" );
		assertEquals( LuaValue.valueOf(1),     t.next(LuaValue.NIL).arg(1) );
		assertEquals( LuaValue.valueOf("one"), t.next(LuaValue.NIL).arg(2) );
		assertEquals( LuaValue.NIL, t.next(LuaValue.ONE) );
		t.set( 2, "two" );
		assertEquals( LuaValue.valueOf(1),     t.next(LuaValue.NIL).arg(1) );
		assertEquals( LuaValue.valueOf("one"), t.next(LuaValue.NIL).arg(2) );
		assertEquals( LuaValue.valueOf(2),     t.next(LuaValue.ONE).arg(1) );
		assertEquals( LuaValue.valueOf("two"), t.next(LuaValue.ONE).arg(2) );
		assertEquals( LuaValue.NIL, t.next(LuaValue.valueOf(2)) );
		
		// insert hash elements
		t.set( "aa", "aaa" );
		assertEquals( LuaValue.valueOf(1),     t.next(LuaValue.NIL).arg(1) );
		assertEquals( LuaValue.valueOf("one"), t.next(LuaValue.NIL).arg(2) );
		assertEquals( LuaValue.valueOf(2),     t.next(LuaValue.ONE).arg(1) );
		assertEquals( LuaValue.valueOf("two"), t.next(LuaValue.ONE).arg(2) );
		assertEquals( LuaValue.valueOf("aa"),  t.next(LuaValue.valueOf(2)).arg(1) );
		assertEquals( LuaValue.valueOf("aaa"), t.next(LuaValue.valueOf(2)).arg(2) );
		assertEquals( LuaValue.NIL, t.next(LuaValue.valueOf("aa")) );
		t.set( "bb", "bbb" );
		assertEquals( LuaValue.valueOf(1),     t.next(LuaValue.NIL).arg(1) );
		assertEquals( LuaValue.valueOf("one"), t.next(LuaValue.NIL).arg(2) );
		assertEquals( LuaValue.valueOf(2),     t.next(LuaValue.ONE).arg(1) );
		assertEquals( LuaValue.valueOf("two"), t.next(LuaValue.ONE).arg(2) );
		assertEquals( LuaValue.valueOf("aa"),  t.next(LuaValue.valueOf(2)).arg(1) );
		assertEquals( LuaValue.valueOf("aaa"), t.next(LuaValue.valueOf(2)).arg(2) );
		assertEquals( LuaValue.valueOf("bb"),  t.next(LuaValue.valueOf("aa")).arg(1) );
		assertEquals( LuaValue.valueOf("bbb"), t.next(LuaValue.valueOf("aa")).arg(2) );
		assertEquals( LuaValue.NIL, t.next(LuaValue.valueOf("bb")) );
	}
}
