/*******************************************************************************
* Copyright (c) 2007-2011 LuaJ. All rights reserved.
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
public class LuaThread extends LuaValue {
	
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
		"dead",
		"error" };
	
	private int status = STATUS_SUSPENDED;
	
	private JavaThread thread;
	private LuaValue env;
	private LuaValue func;

	/** Field to hold state of error condition during debug hook function calls. */
	public LuaValue err;
	
	public static final int        MAX_CALLSTACK = 256;
	public final LuaFunction[]     callstack     = new LuaFunction[MAX_CALLSTACK];
	public int                     calls         = 0;
	
	private static final LuaThread main_thread = new LuaThread();
	
	// state of running thread including call stack
	private static LuaThread       running_thread    = main_thread;

	/** Interval to check for LuaThread dereferencing.  */
	public static int GC_INTERVAL = 30000;

	/** Thread-local used by DebugLib to store debugging state.  */
	public Object debugState;

	/** Private constructor for main thread only */
	private LuaThread() {
		status = STATUS_RUNNING;
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
		return r == main_thread;
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
	
	/** Yield the current thread with arguments 
	 * 
	 * @param args The arguments to send as return values to {@link #resume(Varargs)}
	 * @return {@link Varargs} provided as arguments to {@link #resume(Varargs)}
	 */
	public static Varargs yield(Varargs args) {
		JavaThread t = running_thread.thread;
		if ( t == null )
			error("cannot yield main thread");
		return t.yield(args);
	}
	
	/** Start or resume this thread 
	 * 
	 * @param args The arguments to send as return values to {@link #yield(Varargs)}
	 * @return {@link Varargs} provided as arguments to {@link #yield(Varargs)}
	 */
	public Varargs resume(Varargs args) {
		if ( status != STATUS_SUSPENDED )
			return varargsOf(FALSE, valueOf("cannot resume "+STATUS_NAMES[status]+" coroutine"));
		if ( thread == null )
			thread = new JavaThread(this,func);
		return thread.resume(this,args);
	}

	/**
	 * Private helper class which contains the java stack used by this coroutine, 
	 * and which detects when the LuaThread has been collected and completes. 
	 */
	private static final class JavaThread extends Thread {
		private final WeakReference ref;
		private final LuaValue func;
		private Varargs args;
		private boolean started;
		private static int count;
		private JavaThread(LuaThread lua_thread,LuaValue func) {
			this.ref = new WeakReference(lua_thread);
			this.func = func;
			this.setDaemon(true);
			this.setName("LuaThread-"+(++count));
		}
		public void run() {
			synchronized ( this ) {
				try {
					this.args = func.invoke(this.args);
					setStatus( STATUS_DEAD );
				} catch ( Throwable t ) {
					String msg = t.getMessage();
					this.args = valueOf(msg!=null? msg: t.toString());
					setStatus( STATUS_ERROR );
				} finally {
					this.notify();
				}
			}
		}
		
		private Varargs yield(Varargs args) {
			synchronized ( this ) {
				if ( getStatus() != STATUS_RUNNING )
					error(this+" not running");
				setStatus( STATUS_SUSPENDED );
				this.args = args;
				this.notify();
				try {
					while ( getStatus() == STATUS_SUSPENDED )
						this.wait(GC_INTERVAL);
					if ( null == this.ref.get()  )
						stop();
					setStatus( STATUS_RUNNING );
					return this.args;
				} catch ( InterruptedException e ) {
					setStatus( STATUS_DEAD );
					error( "thread interrupted" );
					return NONE;
				}
			}
		}
	
		private void setStatus(int status) {
			LuaThread lt = (LuaThread) ref.get();
			if ( lt != null )
				lt.status = status;
		}
		
		private int getStatus() {
			LuaThread lt = (LuaThread) ref.get();
			return lt != null? lt.status: STATUS_DEAD;
		}

		private Varargs resume(LuaThread lua_thread, Varargs args) {
	
 			synchronized ( this ) {
				
				// set prior thread to normal status while we are running
				LuaThread prior = running_thread;
				try {
					// set our status to running
					prior.status = STATUS_NORMAL;
					running_thread = lua_thread;
					running_thread.status = STATUS_RUNNING;
					
					// copy args in
					this.args = args;
					
					// start thread if not started alread
					if ( ! this.started ) {
						this.started = true;
						this.start();
					}
					
					// wait for thread to yield or finish
					this.notify();
					this.wait();
					
					// copy return values from yielding stack state
					if ( lua_thread.status == STATUS_ERROR ) {
						lua_thread.status = STATUS_DEAD;
						return varargsOf(FALSE, this.args);
					} else {
						return varargsOf(TRUE, this.args);
					}
		
				} catch ( Throwable t ) {
					lua_thread.status = STATUS_DEAD;
					try {
						return varargsOf(FALSE, valueOf("thread: "+t));
					} finally {
						this.notify();
					}
					
				} finally {
					// previous thread is now running again
					running_thread = prior;
					running_thread.status = STATUS_RUNNING;
				}
			}
		}		
	}
}
