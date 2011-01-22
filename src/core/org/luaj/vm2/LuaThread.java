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
 * Subclass of {@link LuaValue} that implements 
 * a lua coroutine thread using Java Threads.
 * <p>
 * A LuaThread is typically created in response to a scripted call to 
 * {@code coroutine.create()}
 * <p>
 * The threads must be initialized with the globals, so that 
 * the global environment may be passed along according to rules of lua. 
 * This is done via a call to {@link #setGlobals(LuaValue)} 
 * at some point during globals initialization.
 * See {@link BaseLib} for additional documentation and example code.  
 * <p> 
 * The utility classes {@link JsePlatform} and {@link JmePlatform} 
 * see to it that this initialization is done properly.  
 * For this reason it is highly recommended to use one of these classes
 * when initializing globals. 
 * <p>
 * The behavior of coroutine threads matches closely the behavior 
 * of C coroutine library.  However, because of the use of Java threads 
 * to manage call state, it is possible to yield from anywhere in luaj. 
 * On the other hand, if a {@link LuaThread} is created, then yields 
 * without ever entering a completed state, then the garbage collector 
 * may not be able to determine that the thread needs to be collected, 
 * and this could cause a memory and resource leak.  It is recommended
 * that all coroutines that are created are resumed until they are in 
 * a completed state. 
 *   
 * @see LuaValue
 * @see JsePlatform
 * @see JmePlatform
 * @see CoroutineLib
 */
public class LuaThread extends LuaValue implements Runnable {
	
	public static LuaValue s_metatable;
	
	private static final int STATUS_SUSPENDED     = 0;
	private static final int STATUS_RUNNING       = 1;
	private static final int STATUS_NORMAL        = 2;
	private static final int STATUS_DEAD          = 3;
	private static final int STATUS_ERROR         = 4;
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
	
	
	public static final int        MAX_CALLSTACK = 256;
	public final LuaFunction[]     callstack     = new LuaFunction[MAX_CALLSTACK];
	public int                     calls         = 0;

	private static final LuaThread mainthread = new LuaThread();
	
	// state of running thread including call stack
	private static LuaThread       running_thread    = mainthread;

	// thread-local used by DebugLib to store debugging state
	public Object debugState;

	
	LuaThread() {		
	}
	
	/** 
	 * Create a LuaThread around a function and environment
	 * @param func The function to execute
	 * @param env The environment to apply to the thread
	 */
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

	/**
	 * Get the currently running thread. 
	 * @return {@link LuaThread} that is currenly running
	 */
	public static LuaThread getRunning() {
		return running_thread;
	}
	
	/**
	 * Test if this is the main thread 
	 * @return true if this is the main thread
	 */
	public static boolean isMainThread(LuaThread r) {		
		return r == mainthread;
	}
	
	/** 
	 * Set the globals of the current thread.
	 * <p>
	 * This must be done once before any other code executes.
	 * @param globals The global variables for the main ghread. 
	 */
	public static void setGlobals(LuaValue globals) {
		running_thread.env = globals;
	}
	
	/** Get the current thread's environment 
	 * @return {@link LuaValue} containing the global variables of the current thread.
	 */
	public static LuaValue getGlobals() {
		LuaValue e = running_thread.env;
		return e!=null? e: LuaValue.error("LuaThread.setGlobals() not initialized");
	}

	/**
	 * Callback used at the beginning of a call
	 * @param function Function being called
	 * @see DebugLib
	 */
	public static final void onCall(LuaFunction function) {
		running_thread.callstack[running_thread.calls++] = function;
		if (DebugLib.DEBUG_ENABLED) 
			DebugLib.debugOnCall(running_thread, running_thread.calls, function);
	}
	
	/**
	 * Callback used at the end of a call
	 * @see DebugLib
	 */
	public static final void onReturn() {
		running_thread.callstack[--running_thread.calls] = null;
		if (DebugLib.DEBUG_ENABLED) 
			DebugLib.debugOnReturn(running_thread, running_thread.calls);
	}

	/**
	 * Get number of calls in stack
	 * @return number of calls in current call stack
	 * @see DebugLib
	 */
	public static int getCallstackDepth() {
		return running_thread.calls;
	}

	/**
	 * Get the function called as a specific location on the stack.
	 * @param level 1 for the function calling this one, 2 for the next one.
	 * @return LuaFunction on the call stack, or null if outside of range of active stack
	 */
	public static final LuaFunction getCallstackFunction(int level) {
		return level>0 && level<=running_thread.calls? 
			running_thread.callstack[running_thread.calls-level]:
			null;
	}

	public void run() {
		synchronized ( this ) {
			try {
				this.args = func.invoke(this.args);
				status = STATUS_DEAD;
			} catch ( Throwable t ) {
				String msg = t.getMessage();
				this.args = valueOf(msg!=null? msg: t.toString());
				status = STATUS_ERROR;
			} finally {
				this.notify();
			}
		}
	}
	
	/** Yield this thread with arguments 
	 * 
	 * @param args The arguments to send as return values to {@link #resume(Varargs)}
	 * @return {@link Varargs} provided as arguments to {@link #resume(Varargs)}
	 */
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

	/** Start or resume this thread 
	 * 
	 * @param args The arguments to send as return values to {@link #yield(Varargs)}
	 * @return {@link Varargs} provided as arguments to {@link #yield(Varargs)}
	 */
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
				if ( status == STATUS_ERROR ) {
					status = STATUS_DEAD;
					return varargsOf(FALSE, this.args);
				} else {
					return varargsOf(TRUE, this.args);
				}
	
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
