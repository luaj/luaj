/*******************************************************************************
* Copyright (c) 2007 LuaJ. All rights reserved.
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

import org.luaj.vm2.lib.DebugLib;



/** 
 * Implementation of lua coroutines using Java Threads
 */
public class LuaThread extends LuaValue implements Runnable {
	
	public static LuaValue s_metatable;
	
	private static final int STATUS_SUSPENDED     = 0;
	private static final int STATUS_RUNNING       = 1;
	private static final int STATUS_NORMAL        = 2;
	private static final int STATUS_DEAD          = 3;
	private static final String[] STATUS_NAMES = { 
		"suspended", 
		"running", 
		"normal", 
		"dead" };
	
	private int status = STATUS_SUSPENDED;
	
	private Thread thread;
	private LuaValue env;
	private LuaValue func;
	private Varargs args;
	public LuaValue err;
	
	
	public static final int        MAX_CALLSTACK = 64;
	public final LuaFunction[]     callstack     = new LuaFunction[MAX_CALLSTACK];
	public int                     calls         = 0;

	private static final LuaThread mainthread = new LuaThread();
	
	// state of running thread including call stack
	private static LuaThread       running_thread    = mainthread;

	// thread-local used by DebugLib to store debugging state
	public Object debugState;

	
	LuaThread() {		
	}
	
	public LuaThread(LuaValue func, LuaValue env) {	
		this.env = env;
		this.func = func;
	}

	public int type() {
		return LuaValue.TTHREAD;
	}
	
	public String typename() {
		return "thread";
	}
	
	public boolean isthread() {
		return true;
	}
	
	public LuaThread optthread(LuaThread defval) {
		return this;
	}
	
	public LuaThread checkthread() {
		return this;
	}
	
	public LuaValue getmetatable() { 
		return s_metatable; 
	}
	
	public LuaValue getfenv() {
		return env;
	}
	
	public void setfenv(LuaValue env) {
		this.env = env;
	}

	public String getStatus() {
		return STATUS_NAMES[status];
	}

	public static LuaThread getRunning() {
		return running_thread;
	}
	
	public static boolean isMainThread(LuaThread r) {		
		return r == mainthread;
	}

	/** get environment of the running thread, or defval if not defined */ 
	public static LuaValue getRunningEnv(LuaValue defval) {
		return running_thread.env!=null? running_thread.env: defval;
	}
	
	public static final void onCall(LuaFunction function) {
		running_thread.callstack[running_thread.calls++] = function;
		if (DebugLib.DEBUG_ENABLED) 
			DebugLib.debugOnCall(running_thread, running_thread.calls, function);
	}
	
	public static final void onReturn() {
		running_thread.callstack[--running_thread.calls] = null;
		if (DebugLib.DEBUG_ENABLED) 
			DebugLib.debugOnReturn(running_thread, running_thread.calls);
	}

	public static int getCallstackDepth() {
		return running_thread.calls;
	}

	public static final LuaFunction getCallstackFunction(int level) {
		return level>0 || level<running_thread.calls? 
			running_thread.callstack[running_thread.calls-level-1]:
			null;
	}

	public void run() {
		synchronized ( this ) {
			try {
				this.args = func.invoke(this.args);
			} finally {
				status = STATUS_DEAD;
				this.notify();
			}
		}
	}
	
	public Varargs yield(Varargs args) {
		synchronized ( this ) {
			if ( status != STATUS_RUNNING )
				error(this+" not running");
			status = STATUS_SUSPENDED;
			this.args = args;
			this.notify();
			try {
				this.wait();
				status = STATUS_RUNNING;
				return this.args;
			} catch ( InterruptedException e ) {
				status = STATUS_DEAD;
				error( "thread interrupted" );
				return NONE;
			}
		}
	}

	/** Start or resume this thread */
	public Varargs resume(Varargs args) {

		synchronized ( this ) {
 			if ( status == STATUS_DEAD ) {
 				return varargsOf(FALSE, valueOf("cannot resume dead coroutine"));
			}
			
			// set prior thread to normal status while we are running
			LuaThread prior = running_thread;
			try {
				// set our status to running
				prior.status = STATUS_NORMAL;
				running_thread = this;
				this.status = STATUS_RUNNING;
				
				// copy args in
				this.args = args;

				// start the thread
				if ( thread == null ) { 
					thread = new Thread(this);
					thread.start();
				}
				
				// run this vm until it yields
				this.notify();
				this.wait();
				
				// copy return values from yielding stack state
				return varargsOf(LuaValue.TRUE, this.args);
	
			} catch ( Throwable t ) {
				status = STATUS_DEAD;
				try {
					return varargsOf(FALSE, valueOf("thread: "+t));
				} finally {
					this.notify();
				}
				
			} finally {
				// previous thread is now running again
				running_thread = prior;
				prior.status = STATUS_RUNNING;
			}
		}
		
	}

}
