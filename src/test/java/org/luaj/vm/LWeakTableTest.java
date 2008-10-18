package org.luaj.vm;

import java.util.Random;

import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LValue;

public class LWeakTableTest extends LTableTest {
	
	protected LTable new_LTable() {
		return new LWeakTable();
	}
	
	protected LTable new_LTable(int n,int m) {
		return new LWeakTable(n,m);
	}

	Random random = new Random(0);
	Runtime rt = Runtime.getRuntime();
	
	protected void setUp() throws Exception {
		super.setUp();
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		Thread.sleep(20);
		rt.gc();
		Thread.sleep(20);
	}

	private void runTest(int n,int i0,int i1,int di) {
		System.out.println("------- testing "+n+" keys up to "+i1+" bytes each ("+(n*i1)+" bytes total)");
		LTable t = new LWeakTable();
		for ( int i=0; i<n; i++ )
			t.put(i, new LString(new byte[1]));
		for ( int i=i0; i<=i1; i+=di ) {
			int hits = 0;
			for ( int j=0; j<100; j++ ) {
				int k = random.nextInt(n);
				LValue v = t.get(k);
				if ( v != LNil.NIL )
					hits++;
				t.put(i, new LString(new byte[i]));
			}
			long total = rt.totalMemory() / 1000;
			long free = rt.freeMemory() / 1000; 
			long used = (rt.totalMemory() - rt.freeMemory()) / 1000; 
			System.out.println("keys="+n+" bytes="+i+" mem u(f,t)="+used+"("+free+"/"+total+") hits="+hits+"/100");
		}
	}
	
	public void testWeakTable5000() {
		runTest(100,0,5000,500);
	}
	
	public void testWeakTable10000() {
		runTest(100,0,10000,1000);
	}

	public void testWeakTable100() {
		runTest(100,0,100,10);
	}
	
	public void testWeakTable1000() {
		runTest(100,0,1000,100);
	}
	
	public void testWeakTable2000() {
		runTest(100,0,2000,200);
	}
	
	public void testWeakTableRehashToEmpty() {
		LTable t = new_LTable();
		
		Object obj = new Object();
		LTable tableValue = new LTable();
		LString stringValue = LString.valueOf("this is a test");
		
		t.put("table", tableValue);
		t.put("userdata", new LUserData(obj, null));
		t.put("string", stringValue);
		t.put("string2", LString.valueOf("another string"));
		assertTrue("table must have at least 4 elements", t.hashKeys.length > 4);
		
		System.gc();
		
		assertTrue("table must have at least 4 elements", t.hashKeys.length > 4);
		assertEquals(tableValue, t.get(LString.valueOf("table")));
		assertEquals(stringValue, t.get(LString.valueOf("string")));
		assertEquals(obj, t.get(LString.valueOf("userdata")).toJavaInstance());
		
		obj = null;
		tableValue = null;
		stringValue = null;
		
		// Garbage collection should cause weak entries to be dropped.
		System.gc();
		
		// Add a new key to cause a rehash - note that this causes the test to
		// be dependent on the load factor, which is an implementation detail.
		t.put("newkey1", 1);
		
		// Removing the new key should leave the table empty since first set of values fell out
		t.put("newkey1", LNil.NIL);
		
		assertTrue("weak table must be empty", t.hashKeys.length == 0);
	}
}
