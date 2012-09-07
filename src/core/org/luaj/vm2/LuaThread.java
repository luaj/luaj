/*******************************************************************************
* Copyright (c) 2007-2012 LuaJ. All rights reserved.
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
 * <p>
 * Each Java thread wakes up at regular intervals and checks a weak reference
 * to determine if it can ever be resumed.  If not, it throws 
 * {@link OrphanedThread} which is an {@link java.lang.Error}. 
 * Applications should not catch {@link OrphanedThread}, because it can break
 * the thread safety of luaj.
 *   
 * @see LuaValue
 * @see JsePlatform
 * @see JmePlatform
 * @see CoroutineLib
 */
public class LuaThread extends LuaValue {
	
	public static LuaValue s_metatable;

	public static int coroutine_count = 0;

	/** Interval at which to check for lua threads that are no longer referenced. 
	 * This can be changed by Java startup code if desired.
	 */
	static long thread_orphan_check_interval = 30000;
	
	private static final int STATUS_INITIAL       = 0;
	private static final int STATUS_SUSPENDED     = 1;
	private static final int STATUS_RUNNING       = 2;
	private static final int STATUS_NORMAL        = 3;
	private static final int STATUS_DEAD          = 4;
	private static final String[] STATUS_NAMES = { 
		"suspended", 
		"suspended", 
		"running", 
		"normal", 
		"dead",};
	
	private final State state;

	/** Field to hold state of error condition during debug hook function calls. */
	public LuaValue err;
	
	final CallStack callstack = new CallStack();
	
	public static final int        MAX_CALLSTACK = 256;
	
	private static final LuaThread main_thread = new LuaThread();
	
	// state of running thread including call stack
	private static LuaThread       running_thread    = main_thread;

	/** Interval to check for LuaThread dereferencing.  */
	public static int GC_INTERVAL = 30000;

	/** Thread-local used by DebugLib to store debugging state.  */
	public Object debugState;

	/** Private constructor for main thread only */
	private LuaThread() {
		state = new State(this, null);
		state.status = STATUS_RUNNING;
	}
	
	/** 
	 * Create a LuaThread around a function and environment
	 * @param func The function to execute
	 */
	public LuaThread(LuaValue func) {	
		LuaValue.assert_(func != null, "function cannot be null");
		state = new State(this, func);
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
	
	public String getStatus() {
		return STATUS_NAMES[state.status];
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
	 * Callback used at the beginning of a call to prepare for possible getfenv/setfenv calls
	 * @param function Function being called
	 * @return CallStack which is used to signal the return or a tail-call recursion
	 * @see DebugLib
	 */
	public static final CallStack onCall(LuaFunction function) {
		CallStack cs = running_thread.callstack;
		cs.onCall(function);
		return cs;
	}

	/**
	 * Get the function called as a specific location on the stack.
	 * @param level 1 for the function calling this one, 2 for the next one.
	 * @return LuaFunction on the call stack, or null if outside of range of active stack
	 */
	public static final LuaFunction getCallstackFunction(int level) {
		return running_thread.callstack.getFunction(level);
	}

	/**
	 * Replace the error function of the currently running thread.
	 * @param errfunc the new error function to use.
	 * @return the previous error function.
	 */
	public static LuaValue setErrorFunc(LuaValue errfunc) {
		LuaValue prev = running_thread.err;
		running_thread.err = errfunc;
		return prev;
	}

	/** Yield the current thread with arguments 
	 * 
	 * @param args The arguments to send as return values to {@link #resume(Varargs)}
	 * @return {@link Varargs} provided as arguments to {@link #resume(Varargs)}
	 */
	public static Varargs yield(Varargs args) {
		State s = running_thread.state;
		if (s.function == null)
			throw new LuaError("cannot yield main thread");
		return s.lua_yield(args);
	}
	
	/** Start or resume this thread 
	 * 
	 * @param args The arguments to send as return values to {@link #yield(Varargs)}
	 * @return {@link Varargs} provided as arguments to {@link #yield(Varargs)}
	 */
	public Varargs resume(Varargs args) {
		if (this.state.status > STATUS_SUSPENDED)
			return LuaValue.varargsOf(LuaValue.FALSE, 
					LuaValue.valueOf("cannot resume "+LuaThread.STATUS_NAMES[this.state.status]+" coroutine"));
		return state.lua_resume(this, args);
	}

	static class State implements Runnable {
		final WeakReference lua_thread;
		final LuaValue function;
		Varargs args = LuaValue.NONE;
		Varargs result = LuaValue.NONE;
		String error = null;
		int status = LuaThread.STATUS_INITIAL;

		State(LuaThread lua_thread, LuaValue function) {
			this.lua_thread = new WeakReference(lua_thread);
			this.function = function;
		}
		
		public synchronized void run() {
			try {
				Varargs a = this.args;
				this.args = LuaValue.NONE;
				this.result = function.invoke(a);
			} catch (Throwable t) {
				this.error = t.getMessage();
			} finally {
				this.status = LuaThread.STATUS_DEAD;
				this.notify();
			}
		}

		synchronized Varargs lua_resume(LuaThread new_thread, Varargs args) {
			LuaThread previous_thread = LuaThread.running_thread;
			try {
				LuaThread.running_thread = new_thread;
				this.args = args;
				if (this.status == STATUS_INITIAL) {
					this.status = STATUS_RUNNING; 
					new Thread(this, "Coroutine-"+(++coroutine_count)).start();
				} else {
					this.notify();
				}
				previous_thread.state.status = STATUS_NORMAL;
				this.status = STATUS_RUNNING;
				this.wait();
				return (this.error != null? 
					LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf(this.error)):
					LuaValue.varargsOf(LuaValue.TRUE, this.result));
			} catch (InterruptedException ie) {
				throw new OrphanedThread();
			} finally {
				running_thread = previous_thread;
				running_thread.state.status =STATUS_RUNNING;
				this.args = LuaValue.NONE;
				this.result = LuaValue.NONE;
				this.error = null;
			}
		}

		synchronized Varargs lua_yield(Varargs args) {
			try {
				this.result = args;
				this.status = STATUS_SUSPENDED;
				this.notify();
				do {
					this.wait(thread_orphan_check_interval);
					if (this.lua_thread.get() == null) {
						this.status = STATUS_DEAD;
						throw new OrphanedThread();
					}
				} while (this.status == STATUS_SUSPENDED);
				return this.args;
			} catch (InterruptedException ie) {
				this.status = STATUS_DEAD;
				throw new OrphanedThread();
			} finally {
				this.args = LuaValue.NONE;
				this.result = LuaValue.NONE;
			}
		}
	}

	public static class CallStack {
		final LuaFunction[]     functions     = new LuaFunction[MAX_CALLSTACK];
		int                     calls         = 0;

		/**
		 * Method to indicate the start of a call
		 * @see DebugLib
		 */
		final void onCall(LuaFunction function) {
			functions[calls++] = function;
			if (DebugLib.DEBUG_ENABLED) 
				DebugLib.debugOnCall(running_thread, calls, function);
		}
		
		/**
		 * Method to signal the end of a call
		 * @see DebugLib
		 */
		public final void onReturn() {
			functions[--calls] = null;
			if (DebugLib.DEBUG_ENABLED) 
				DebugLib.debugOnReturn(running_thread, calls);
		}
		
		/**
		 * Get number of calls in stack
		 * @return number of calls in current call stack
		 * @see DebugLib
		 */
		public final int getCallstackDepth() {
			return calls;
		}

		/** 
		 * Get the function at a particular level of the stack.
		 * @param level # of levels back from the top of the stack.
		 * @return LuaFunction, or null if beyond the stack limits.
		 */
		LuaFunction getFunction(int level) {
			return level>0 && level<=calls? functions[calls-level]: null;
		}
	}
}
