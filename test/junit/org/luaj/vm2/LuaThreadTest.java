/*******************************************************************************
 * Copyright (c) 2011 Luaj.org. All rights reserved.
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

import junit.framework.TestCase;

import org.luaj.vm2.lib.OneArgFunction;

public class LuaThreadTest extends TestCase {

	public void testMainThread() {
		assertEquals( true, LuaThread.isMainThread( LuaThread.getRunning()) );
		assertEquals( "running", LuaThread.getRunning().getStatus() );
		assertEquals( LuaValue.FALSE, LuaThread.getRunning().resume(LuaValue.NONE).arg1() );
		try {
			LuaThread.yield(LuaThread.yield(LuaValue.NONE));
			fail("did not throw lua error as expected");
		} catch ( LuaError le ) {			
		}
	}
	
	public void testLuaThreadIsCollected() throws InterruptedException {		System.out.println("testLuaThread - starting");
		int originalInterval = LuaThread.GC_INTERVAL;
		try {
			LuaThread.GC_INTERVAL = 75;
			TestRig rig = new TestRig();
			assertEquals( "resumed 1 times, arg=test-arg", rig.resumeOnce() );
			assertEquals( true, rig.isThreadReferenced() );
			assertEquals( true, rig.isFunctionReferenced() );
			assertEquals( true, rig.isArgReferenced() );
			collectGarbage();
			assertEquals( "resumed 2 times, arg=test-arg", rig.resumeOnce() );
			assertEquals( true, rig.isThreadReferenced() );
			assertEquals( true, rig.isFunctionReferenced() );
			assertEquals( true, rig.isArgReferenced() );
			Thread.sleep( 200 );
			collectGarbage();
			assertEquals( "resumed 3 times, arg=test-arg", rig.resumeOnce() );
			assertEquals( true, rig.isThreadReferenced() );
			assertEquals( true, rig.isFunctionReferenced() );
			assertEquals( true, rig.isArgReferenced() );
	
			// check that references are collected 
			// some time after lua thread is de-referenced
			rig.weakenReference();
			Thread.sleep( 200 );
			collectGarbage();
			assertEquals( false, rig.isThreadReferenced() );
			assertEquals( false, rig.isFunctionReferenced() );
			assertEquals( false, rig.isArgReferenced() );
		} finally {
			LuaThread.GC_INTERVAL = originalInterval;
			
		}
	}

	static class TestRig {
		LuaThread luaThread;
		final WeakReference luaRef;
		final WeakReference funcRef;
		final WeakReference argRef;
		TestRig() {
			LuaValue a = new LuaUserdata( "test-arg" );
			LuaValue f = new TestFunction();
			luaThread = new LuaThread( f, new LuaTable() );
			luaRef = new WeakReference( luaThread );
			funcRef = new WeakReference( f );
			argRef = new WeakReference( a );
		}
		public String resumeOnce() {
			LuaThread t = (LuaThread) luaRef.get();
			LuaValue a = (LuaValue) argRef.get();
			return t==null? "no ref to lua thread": 
				   a==null? "no ref to arg": 
				   t.resume(a).arg(2).toString();
		}
		public void weakenReference() {
			luaThread = null;
		}
		public Object isThreadReferenced() {
			return null != luaRef.get();
		}
		public Object isFunctionReferenced() {
			return null != funcRef.get();
		}
		public Object isArgReferenced() {
			return null != argRef.get();
		}	
	}
	
	static class TestFunction extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			for ( int count=1; true; count++ ) {
				LuaValue r = LuaValue.valueOf("resumed "+count+" times, arg="+arg);
				Varargs v = LuaThread.yield( r );
				arg = v.arg1();
			}
		}
	}
	
	static void collectGarbage() {
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		try {
			Thread.sleep(20);
			rt.gc();
			Thread.sleep(20);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		rt.gc();
	}
}
