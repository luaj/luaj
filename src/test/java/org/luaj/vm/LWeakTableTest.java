package org.luaj.vm;

import java.util.Random;

import junit.framework.TestCase;

public class LWeakTableTest extends TestCase  {

	Random random = new Random(0);
	Runtime rt = Runtime.getRuntime();
	
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
	
}
