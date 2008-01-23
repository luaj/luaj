package org.luaj.vm;

import org.luaj.vm.LDouble;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;

import junit.framework.TestCase;

public class LTableTest extends TestCase {
	
	public void testInOrderIntegerKeyInsertion() {
		LTable t = new LTable();
		
		for ( int i = 1; i <= 32; ++i ) {
			t.put( i, new LString( "Test Value! "+i ) );
		}
		
		// Ensure capacities make sense
		assertEquals( 0, t.getHashCapacity() );
		
		assertTrue( t.getArrayCapacity() >= 32 );
		assertTrue( t.getArrayCapacity() <= 64 );
		
		// Ensure all keys are still there.
		for ( int i = 1; i <= 32; ++i ) {
			assertEquals( "Test Value! " + i, t.get( i ).luaAsString().toJavaString() );
		}
	}
	
	public void testResize() {
		LTable t = new LTable();
		
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
		
		assertEquals( 0, t.getHashCapacity() );
		assertTrue( t.getArrayCapacity() >= 6 );
	}
	
	public void testOutOfOrderIntegerKeyInsertion() {
		LTable t = new LTable();
		
		for ( int i = 32; i > 0; --i ) {
			t.put( i, new LString( "Test Value! "+i ) );
		}
		
		// Ensure capacities make sense
		assertEquals( 0, t.getHashCapacity() );
		
		assertTrue( t.getArrayCapacity() >= 32 );
		assertTrue( t.getArrayCapacity() <= 64 );
		
		// Ensure all keys are still there.
		for ( int i = 1; i <= 32; ++i ) {
			assertEquals( "Test Value! " + i, t.get( i ).luaAsString() );
		}
	}
	
	public void testStringAndIntegerKeys() {
		LTable t = new LTable();
		
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
		LTable t = new LTable(0, 1);
		
		t.put( "test", new LString("foo") );
		t.put( "explode", new LString("explode") );
		assertEquals( 2, t.size() );
	}
	
	public void testRemove0() {
		LTable t = new LTable(2, 0);
		
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
		LTable t = new LTable(0, 1);
		
		t.put( "test", new LString("foo") );
		t.put( "explode", LNil.NIL );
		t.put( 42, LNil.NIL );
		t.put( new LTable(), LNil.NIL );
		t.put( "test", LNil.NIL );
		assertEquals( 0, t.size() );
		
		t.put( 10, LInteger.valueOf( 5 ) );
		t.put( 10, LNil.NIL );
		assertEquals( 0, t.size() );
	}
	
	public void testRemove2() {
		LTable t = new LTable(0, 1);
		
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

}
