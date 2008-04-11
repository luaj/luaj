package org.luaj.vm;

import java.util.Vector;

import junit.framework.TestCase;

public class LTableTest extends TestCase {
	
	protected LTable new_LTable() {
		return new LTable();
	}
	
	protected LTable new_LTable(int n,int m) {
		return new LTable(n,m);
	}
	
	public void testInOrderIntegerKeyInsertion() {
		LTable t = new_LTable();
		
		for ( int i = 1; i <= 32; ++i ) {
			t.put( i, new LString( "Test Value! "+i ) );
		}

		// Ensure all keys are still there.
		for ( int i = 1; i <= 32; ++i ) {
			assertEquals( "Test Value! " + i, t.get( i ).toJavaString() );
		}
		
		// Ensure capacities make sense
		assertEquals( 0, t.getHashCapacity() );
		
		assertTrue( t.getArrayCapacity() >= 32 );
		assertTrue( t.getArrayCapacity() <= 64 );
		
	}
	
	public void testResize() {
		LTable t = new_LTable();
		
		// NOTE: This order of insertion is important.
		t.put(3, LInteger.valueOf(3));
		t.put(1, LInteger.valueOf(1));
		t.put(5, LInteger.valueOf(5));
		t.put(4, LInteger.valueOf(4));
		t.put(6, LInteger.valueOf(6));
		t.put(2, LInteger.valueOf(2));
		
		for ( int i = 1; i < 6; ++i ) {
			assertEquals(LInteger.valueOf(i), t.get(i));
		}
		
		assertTrue( t.getArrayCapacity() >= 0 && t.getArrayCapacity() <= 2 );
		assertTrue( t.getHashCapacity() >= 4 );
	}
	
	public void testOutOfOrderIntegerKeyInsertion() {
		LTable t = new_LTable();
		
		for ( int i = 32; i > 0; --i ) {
			t.put( i, new LString( "Test Value! "+i ) );
		}

		// Ensure all keys are still there.
		for ( int i = 1; i <= 32; ++i ) {
			assertEquals( "Test Value! "+i, t.get( i ).toJavaString() );
		}
		
		// Ensure capacities make sense
		assertTrue( t.getArrayCapacity() >= 0 );
		assertTrue( t.getArrayCapacity() <= 6 );
		
		assertTrue( t.getHashCapacity() >= 16 );
		assertTrue( t.getHashCapacity() <= 64 );
		
	}
	
	public void testStringAndIntegerKeys() {
		LTable t = new_LTable();
		
		for ( int i = 0; i < 10; ++i ) {
			LString str = new LString( String.valueOf( i ) );
			t.put( i, str );
			t.put( str, LInteger.valueOf( i ) );
		}
		
		assertTrue( t.getArrayCapacity() >= 9 ); // 1, 2, ..., 9
		assertTrue( t.getArrayCapacity() <= 18 );
		assertTrue( t.getHashCapacity() >= 11 ); // 0, "0", "1", ..., "9"
		assertTrue( t.getHashCapacity() <= 33 );
		
		LValue[] keys = t.getKeys();
		
		int intKeys = 0;
		int stringKeys = 0;
		
		assertEquals( 20, keys.length );
		for ( int i = 0; i < keys.length; ++i ) {
			LValue k = keys[i];
			
			if ( k instanceof LInteger ) {
				final int ik = k.toJavaInt();
				assertTrue( ik >= 0 && ik < 10 );
				final int mask = 1 << ik;
				assertTrue( ( intKeys & mask ) == 0 );
				intKeys |= mask;
			} else if ( k instanceof LString ) {
				final int ik = Integer.parseInt( k.luaAsString().toJavaString() );
				assertEquals( String.valueOf( ik ), k.luaAsString().toJavaString() );
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
		LTable t = new_LTable(0, 1);
		
		t.put( "test", new LString("foo") );
		t.put( "explode", new LString("explode") );
		assertEquals( 2, t.size() );
	}
	
	public void testRemove0() {
		LTable t = new_LTable(2, 0);
		
		t.put( 1, new LString("foo") );
		t.put( 2, new LString("bah") );
		assertNotSame(LNil.NIL, t.get(1));
		assertNotSame(LNil.NIL, t.get(2));
		assertEquals(LNil.NIL, t.get(3));
		
		t.put( 1, LNil.NIL );
		t.put( 2, LNil.NIL );
		t.put( 3, LNil.NIL );
		assertEquals(LNil.NIL, t.get(1));
		assertEquals(LNil.NIL, t.get(2));
		assertEquals(LNil.NIL, t.get(3));
	}
	
	public void testRemove1() {
		LTable t = new_LTable(0, 1);
		
		t.put( "test", new LString("foo") );
		t.put( "explode", LNil.NIL );
		t.put( 42, LNil.NIL );
		t.put( new_LTable(), LNil.NIL );
		t.put( "test", LNil.NIL );
		assertEquals( 0, t.size() );
		
		t.put( 10, LInteger.valueOf( 5 ) );
		t.put( 10, LNil.NIL );
		assertEquals( 0, t.size() );
	}
	
	public void testRemove2() {
		LTable t = new_LTable(0, 1);
		
		t.put( "test", new LString("foo") );
		t.put( "string", LInteger.valueOf( 10 ) );
		assertEquals( 2, t.size() );
		
		t.put( "string", LNil.NIL );
		t.put( "three", new LDouble( 3.14 ) );
		assertEquals( 2, t.size() );
		
		t.put( "test", LNil.NIL );
		assertEquals( 1, t.size() );
		
		t.put( 10, LInteger.valueOf( 5 ) );
		assertEquals( 2, t.size() );
		
		t.put( 10, LNil.NIL );
		assertEquals( 1, t.size() );
		
		t.put( "three", LNil.NIL );
		assertEquals( 0, t.size() );
	}

	public void testInOrderLuaLength() {
		LTable t = new_LTable();
		
		for ( int i = 1; i <= 32; ++i ) {
			t.put( i, new LString( "Test Value! "+i ) );
			assertEquals( i, t.luaLength() );
			assertEquals( i, t.luaMaxN().toJavaInt() );
		}
	}

	public void testOutOfOrderLuaLength() {
		LTable t = new_LTable();

		for ( int j=8; j<32; j+=8 ) {
			for ( int i = j; i > 0; --i ) {
				t.put( i, new LString( "Test Value! "+i ) );
			}
			assertEquals( j, t.luaLength() );
			assertEquals( j, t.luaMaxN().toJavaInt() );
		}
	}
	
	public void testStringKeysLuaLength() {
		LTable t = new_LTable();
		
		for ( int i = 1; i <= 32; ++i ) {
			t.put( "str-"+i, new LString( "String Key Test Value! "+i ) );
			assertEquals( 0, t.luaLength() );
			assertEquals( 0, t.luaMaxN().toJavaInt() );
		}
	}

	public void testMixedKeysLuaLength() {
		LTable t = new_LTable();
		
		for ( int i = 1; i <= 32; ++i ) {
			t.put( "str-"+i, new LString( "String Key Test Value! "+i ) );
			t.put( i, new LString( "Int Key Test Value! "+i ) );
			assertEquals( i, t.luaLength() );
			assertEquals( i, t.luaMaxN().toJavaInt() );
		}
	}

	private static final void compareLists(LTable t,Vector v) {
		int n = v.size();
		assertEquals(v.size(),t.luaLength());
		for ( int j=0; j<n; j++ ) {
			Object vj = v.elementAt(j);
			Object tj = t.get(j+1).toJavaString();
			assertEquals(vj,tj);
		}
	}
	
	public void testInsertBeginningOfList() {
		LTable t = new_LTable();
		Vector v = new Vector();
		
		for ( int i = 1; i <= 32; ++i ) {
			String test = "Test Value! "+i;
			t.luaInsertPos(1, LString.valueOf(test));
			v.insertElementAt(test, 0);						
			compareLists(t,v);
		}
	}

	public void testInsertEndOfList() {
		LTable t = new_LTable();
		Vector v = new Vector();
		
		for ( int i = 1; i <= 32; ++i ) {
			String test = "Test Value! "+i;
			t.luaInsertPos(0, LString.valueOf(test));
			v.insertElementAt(test, v.size());						
			compareLists(t,v);
		}
	}

	public void testInsertMiddleOfList() {
		LTable t = new_LTable();
		Vector v = new Vector();
		
		for ( int i = 1; i <= 32; ++i ) {
			String test = "Test Value! "+i;
			int m = i / 2;
			t.luaInsertPos(m+1, LString.valueOf(test));
			v.insertElementAt(test, m);
			compareLists(t,v);
		}
	}
	
	private static final void prefillLists(LTable t,Vector v) {
		for ( int i = 1; i <= 32; ++i ) {
			String test = "Test Value! "+i;
			t.luaInsertPos(0, LString.valueOf(test));
			v.insertElementAt(test, v.size());
		}
	}
	
	public void testRemoveBeginningOfList() {
		LTable t = new_LTable();
		Vector v = new Vector();
		prefillLists(t,v);
		for ( int i = 1; i <= 32; ++i ) {
			t.luaRemovePos(1);
			v.removeElementAt(0);
			compareLists(t,v);
		}
	}
	
	public void testRemoveEndOfList() {
		LTable t = new_LTable();
		Vector v = new Vector();
		prefillLists(t,v);
		for ( int i = 1; i <= 32; ++i ) {
			t.luaRemovePos(0);
			v.removeElementAt(v.size()-1);
			compareLists(t,v);
		}
	}

	public void testRemoveMiddleOfList() {
		LTable t = new_LTable();
		Vector v = new Vector();
		prefillLists(t,v);
		for ( int i = 1; i <= 32; ++i ) {
			int m = v.size() / 2;
			t.luaRemovePos(m+1);
			v.removeElementAt(m);
			compareLists(t,v);
		}
	}
	
}
