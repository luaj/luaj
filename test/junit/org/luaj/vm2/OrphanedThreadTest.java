/*******************************************************************************
 * Copyright (c) 2012 Luaj.org. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;

import junit.framework.TestCase;

import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;


public class OrphanedThreadTest extends TestCase {

	LuaThread luathread;
	WeakReference luathr_ref;
	LuaValue function;
	WeakReference func_ref;
	LuaValue env;
	
	protected void setUp() throws Exception {
		LuaThread.thread_orphan_check_interval = 5;
		env = JsePlatform.standardGlobals();
	}
	
	protected void tearDown() {
		LuaThread.thread_orphan_check_interval = 30000;
	}
	
	public void testCollectOrphanedNormalThread() throws Exception {
		function = new NormalFunction();
		doTest(LuaValue.TRUE, LuaValue.ZERO);
	}
	
	public void testCollectOrphanedEarlyCompletionThread() throws Exception {
		function = new EarlyCompletionFunction();
		doTest(LuaValue.TRUE, LuaValue.ZERO);
	}
	
	public void testCollectOrphanedAbnormalThread() throws Exception {
		function = new AbnormalFunction();
		doTest(LuaValue.FALSE, LuaValue.valueOf("abnormal condition"));
	}
	
	public void testCollectOrphanedClosureThread() throws Exception {
		String script =
			"print('in closure, arg is '..(...))\n" +
			"arg = coroutine.yield(1)\n" +
			"print('in closure.2, arg is '..arg)\n" +
			"arg = coroutine.yield(0)\n" +
			"print('leakage in closure.3, arg is '..arg)\n" +
			"return 'done'\n";
		LuaC.install();
		function = LoadState.load(new ByteArrayInputStream(script.getBytes()), "script", "bt", env);
		doTest(LuaValue.TRUE, LuaValue.ZERO);
	}
	
	public void testCollectOrphanedPcallClosureThread() throws Exception {
		String script =
			"f = function(x)\n" +
			"  print('in pcall-closure, arg is '..(x))\n" +
			"  arg = coroutine.yield(1)\n" +
			"  print('in pcall-closure.2, arg is '..arg)\n" +
			"  arg = coroutine.yield(0)\n" +
			"  print('leakage in pcall-closure.3, arg is '..arg)\n" +
			"  return 'done'\n" +
			"end\n" +
			"print( 'pcall-closre.result:', pcall( f, ... ) )\n";
		LuaC.install();
		function = LoadState.load(new ByteArrayInputStream(script.getBytes()), "script", "bt", env);
		doTest(LuaValue.TRUE, LuaValue.ZERO);
	}
	
	public void testCollectOrphanedLoadCloasureThread() throws Exception {
		String script =
			"t = { \"print \", \"'hello, \", \"world'\", }\n" +
			"i = 0\n" +
			"arg = ...\n" +
			"f = function()\n" +
			"	i = i + 1\n" +
			"   print('in load-closure, arg is', arg, 'next is', t[i])\n" +
			"   arg = coroutine.yield(1)\n" +
			"	return t[i]\n" +
		    "end\n" +
			"load(f)()\n";
		LuaC.install();
		function = LoadState.load(new ByteArrayInputStream(script.getBytes()), "script", "bt", env);
		doTest(LuaValue.TRUE, LuaValue.ONE);
	}

	private void doTest(LuaValue status2, LuaValue value2) throws Exception {
		luathread = new LuaThread(function, env);
		luathr_ref = new WeakReference(luathread);
		func_ref = new WeakReference(function);
		assertNotNull(luathr_ref.get());		
		
		// resume two times
		Varargs a = luathread.resume(LuaValue.valueOf("foo"));
		assertEquals(LuaValue.ONE, a.arg(2));
		assertEquals(LuaValue.TRUE, a.arg1());
		a = luathread.resume(LuaValue.valueOf("bar"));
		assertEquals(value2, a.arg(2));
		assertEquals(status2, a.arg1());
		
		// drop strong references
		luathread = null;
		function = null;
		
		// gc
		for (int i=0; i<100 && (luathr_ref.get() != null || func_ref.get() != null); i++) {
			Runtime.getRuntime().gc();
			Thread.sleep(5);
		}
		
		// check reference
		assertNull(luathr_ref.get());
		assertNull(func_ref.get());
	}
	
	
	static class NormalFunction extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			System.out.println("in normal.1, arg is "+arg);
			arg = LuaThread.yield(ONE).arg1();
			System.out.println("in normal.2, arg is "+arg);
			arg = LuaThread.yield(ZERO).arg1();
			System.out.println("in normal.3, arg is "+arg);
			return NONE;
		}		
	}
	
	static class EarlyCompletionFunction extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			System.out.println("in early.1, arg is "+arg);
			arg = LuaThread.yield(ONE).arg1();
			System.out.println("in early.2, arg is "+arg);
			return ZERO;
		}		
	}
	
	static class AbnormalFunction extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			System.out.println("in abnormal.1, arg is "+arg);
			arg = LuaThread.yield(ONE).arg1();
			System.out.println("in abnormal.2, arg is "+arg);
			error("abnormal condition");
			return ZERO;
		}		
	}
	
	static class ClosureFunction extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			System.out.println("in abnormal.1, arg is "+arg);
			arg = LuaThread.yield(ONE).arg1();
			System.out.println("in abnormal.2, arg is "+arg);
			error("abnormal condition");
			return ZERO;
		}		
	}
}
