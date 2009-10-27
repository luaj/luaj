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

import java.util.Vector;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import junit.framework.TestCase;

/**
 * Tests for tables used as lists. 
 */
public class TableArrayTest extends TestCase {
	
	protected LuaTable new_Table() {
		return new LuaTable();
	}
	
	protected LuaTable new_Table(int n,int m) {
		return new LuaTable(n,m);
	}
	
	public void testInOrderIntegerKeyInsertion() {
		LuaTable t = new_Table();
		
		for ( int i = 1; i <= 32; ++i ) {
			t.set( i, LuaString.valueOf( "Test Value! "+i ) );
		}

		// Ensure all keys are still there.
		for ( int i = 1; i <= 32; ++i ) {
			assertEquals( "Test Value! " + i, t.get( i ).toString() );
		}
		
		// Ensure capacities make sense
		assertEquals( 0, t.hashCapacity() );
		
		assertTrue( t.arrayCapacity() >= 32 );
		assertTrue( t.arrayCapacity() <= 64 );
		
	}
	
	public void testResize() {
		LuaTable t = new_Table();
		
		// NOTE: This order of insertion is important.
		t.set(3, LuaInteger.valueOf(3));
		t.set(1, LuaInteger.valueOf(1));
		t.set(5, LuaInteger.valueOf(5));
		t.set(4, LuaInteger.valueOf(4));
		t.set(6, LuaInteger.valueOf(6));
		t.set(2, LuaInteger.valueOf(2));
		
		for ( int i = 1; i < 6; ++i ) {
			assertEquals(LuaInteger.valueOf(i), t.get(i));
		}
		
		assertTrue( t.arrayCapacity() >= 0 && t.arrayCapacity() <= 2 );
		assertTrue( t.hashCapacity() >= 4 );
	}
	
	public void testOutOfOrderIntegerKeyInsertion() {
		LuaTable t = new_Table();
		
		for ( int i = 32; i > 0; --i ) {
			t.set( i, LuaString.valueOf( "Test Value! "+i ) );
		}

		// Ensure all keys are still there.
		for ( int i = 1; i <= 32; ++i ) {
			assertEquals( "Test Value! "+i, t.get( i ).toString() );
		}
		
		// Ensure capacities make sense
		assertTrue( t.arrayCapacity() >= 0 );
		assertTrue( t.arrayCapacity() <= 6 );
		
		assertTrue( t.hashCapacity() >= 16 );
		assertTrue( t.hashCapacity() <= 64 );
		
	}
	
	public void testStringAndIntegerKeys() {
		LuaTable t = new_Table();
		
		for ( int i = 0; i < 10; ++i ) {
			LuaString str = LuaString.valueOf( String.valueOf( i ) );
			t.set( i, str );
			t.set( str, LuaInteger.valueOf( i ) );
		}
		
		assertTrue( t.arrayCapacity() >= 9 ); // 1, 2, ..., 9
		assertTrue( t.arrayCapacity() <= 18 );
		assertTrue( t.hashCapacity() >= 11 ); // 0, "0", "1", ..., "9"
		assertTrue( t.hashCapacity() <= 33 );
		
		LuaValue[] keys = t.keys();
		
		int intKeys = 0;
		int stringKeys = 0;
		
		assertEquals( 20, keys.length );
		for ( int i = 0; i < keys.length; ++i ) {
			LuaValue k = keys[i];
			
			if ( k instanceof LuaInteger ) {
				final int ik = k.toint();
				assertTrue( ik >= 0 && ik < 10 );
				final int mask = 1 << ik;
				assertTrue( ( intKeys & mask ) == 0 );
				intKeys |= mask;
			} else if ( k instanceof LuaString ) {
				final int ik = Integer.parseInt( k.toString() );
				assertEquals( String.valueOf( ik ), k.toString() );
				assertTrue( ik >= 0 && ik < 10 );
				final int mask = 1 << ik;
				assertTrue( "Key \""+ik+"\" found more than once", ( stringKeys & mask ) == 0 );
				stringKeys |= mask;
			} else {
				fail( "Unexpected type of key found" );
			}
		}
		
		assertEquals( 0x03FF, intKeys );
		assertEquals( 0x03FF, stringKeys );
	}
	
	public void testBadInitialCapacity() {
		LuaTable t = new_Table(0, 1);
		
		t.set( "test", LuaString.valueOf("foo") );
		t.set( "explode", LuaString.valueOf("explode") );
		assertEquals( 2, t.keyCount() );
	}
	
	public void testRemove0() {
		LuaTable t = new_Table(2, 0);
		
		t.set( 1, LuaString.valueOf("foo") );
		t.set( 2, LuaString.valueOf("bah") );
		assertNotSame(LuaValue.NIL, t.get(1));
		assertNotSame(LuaValue.NIL, t.get(2));
		assertEquals(LuaValue.NIL, t.get(3));
		
		t.set( 1, LuaValue.NIL );
		t.set( 2, LuaValue.NIL );
		t.set( 3, LuaValue.NIL );
		assertEquals(LuaValue.NIL, t.get(1));
		assertEquals(LuaValue.NIL, t.get(2));
		assertEquals(LuaValue.NIL, t.get(3));
	}
	
	public void testRemove1() {
		LuaTable t = new_Table(0, 1);
		assertEquals( 0, t.keyCount() );
		
		t.set( "test", LuaString.valueOf("foo") );
		assertEquals( 1, t.keyCount() );
		t.set( "explode", LuaValue.NIL );
		assertEquals( 1, t.keyCount() );
		t.set( 42, LuaValue.NIL );
		assertEquals( 1, t.keyCount() );
		t.set( new_Table(), LuaValue.NIL );
		assertEquals( 1, t.keyCount() );
		t.set( "test", LuaValue.NIL );
		assertEquals( 0, t.keyCount() );
		
		t.set( 10, LuaInteger.valueOf( 5 ) );
		t.set( 10, LuaValue.NIL );
		assertEquals( 0, t.keyCount() );
	}
	
	public void testRemove2() {
		LuaTable t = new_Table(0, 1);
		
		t.set( "test", LuaString.valueOf("foo") );
		t.set( "string", LuaInteger.valueOf( 10 ) );
		assertEquals( 2, t.keyCount() );
		
		t.set( "string", LuaValue.NIL );
		t.set( "three", LuaDouble.valueOf( 3.14 ) );
		assertEquals( 2, t.keyCount() );
		
		t.set( "test", LuaValue.NIL );
		assertEquals( 1, t.keyCount() );
		
		t.set( 10, LuaInteger.valueOf( 5 ) );
		assertEquals( 2, t.keyCount() );
		
		t.set( 10, LuaValue.NIL );
		assertEquals( 1, t.keyCount() );
		
		t.set( "three", LuaValue.NIL );
		assertEquals( 0, t.keyCount() );
	}

	public void testInOrderlen() {
		LuaTable t = new_Table();
		
		for ( int i = 1; i <= 32; ++i ) {
			LuaValue v = LuaString.valueOf( "Test Value! "+i );
			t.set( i, v );
			assertEquals( i, t.length() );
			assertEquals( i, t.maxn() );
		}
	}

	public void testOutOfOrderlen() {
		LuaTable t = new_Table();

		for ( int j=8; j<32; j+=8 ) {
			for ( int i = j; i > 0; --i ) {
				t.set( i, LuaString.valueOf( "Test Value! "+i ) );
			}
			assertEquals( j, t.length() );
			assertEquals( j, t.maxn() );
		}
	}
	
	public void testStringKeyslen() {
		LuaTable t = new_Table();
		
		for ( int i = 1; i <= 32; ++i ) {
			t.set( "str-"+i, LuaString.valueOf( "String Key Test Value! "+i ) );
			assertEquals( 0, t.length() );
			assertEquals( 0, t.maxn() );
		}
	}

	public void testMixedKeyslen() {
		LuaTable t = new_Table();
		
		for ( int i = 1; i <= 32; ++i ) {
			t.set( "str-"+i, LuaString.valueOf( "String Key Test Value! "+i ) );
			t.set( i, LuaString.valueOf( "Int Key Test Value! "+i ) );
			assertEquals( i, t.length() );
			assertEquals( i, t.maxn() );
		}
	}

	private static final void compareLists(LuaTable t,Vector v) {
		int n = v.size();
		assertEquals(v.size(),t.length());
		for ( int j=0; j<n; j++ ) {
			Object vj = v.elementAt(j);
			Object tj = t.get(j+1).toString();
			vj = ((LuaString)vj).toString();
			assertEquals(vj,tj);
		}
	}
	
	public void testInsertBeginningOfList() {
		LuaTable t = new_Table();
		Vector v = new Vector();
		
		for ( int i = 1; i <= 32; ++i ) {
			LuaString test = LuaString.valueOf("Test Value! "+i);
			t.insert(1, test);
			v.insertElementAt(test, 0);						
			compareLists(t,v);
		}
	}

	public void testInsertEndOfList() {
		LuaTable t = new_Table();
		Vector v = new Vector();
		
		for ( int i = 1; i <= 32; ++i ) {
			LuaString test = LuaString.valueOf("Test Value! "+i);
			t.insert(0, test);
			v.insertElementAt(test, v.size());						
			compareLists(t,v);
		}
	}

	public void testInsertMiddleOfList() {
		LuaTable t = new_Table();
		Vector v = new Vector();
		
		for ( int i = 1; i <= 32; ++i ) {
			LuaString test = LuaString.valueOf("Test Value! "+i);
			int m = i / 2;
			t.insert(m+1, test);
			v.insertElementAt(test, m);
			compareLists(t,v);
		}
	}
	
	private static final void prefillLists(LuaTable t,Vector v) {
		for ( int i = 1; i <= 32; ++i ) {
			LuaString test = LuaString.valueOf("Test Value! "+i);
			t.insert(0, test);
			v.insertElementAt(test, v.size());
		}
	}
	
	public void testRemoveBeginningOfList() {
		LuaTable t = new_Table();
		Vector v = new Vector();
		prefillLists(t,v);
		for ( int i = 1; i <= 32; ++i ) {
			t.remove(1);
			v.removeElementAt(0);
			compareLists(t,v);
		}
	}
	
	public void testRemoveEndOfList() {
		LuaTable t = new_Table();
		Vector v = new Vector();
		prefillLists(t,v);
		for ( int i = 1; i <= 32; ++i ) {
			t.remove(0);
			v.removeElementAt(v.size()-1);
			compareLists(t,v);
		}
	}

	public void testRemoveMiddleOfList() {
		LuaTable t = new_Table();
		Vector v = new Vector();
		prefillLists(t,v);
		for ( int i = 1; i <= 32; ++i ) {
			int m = v.size() / 2;
			t.remove(m+1);
			v.removeElementAt(m);
			compareLists(t,v);
		}
	}
	
}
