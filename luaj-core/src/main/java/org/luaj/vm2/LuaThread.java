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

/**
 * Subclass of {@link LuaValue} that implements a lua coroutine thread using
 * Java Threads.
 * <p>
 * A LuaThread is typically created in response to a scripted call to
 * {@code coroutine.create()}
 * <p>
 * The threads must be initialized with the globals, so that the global
 * environment may be passed along according to rules of lua. This is done via
 * the constructor arguments {@link #LuaThread(Globals)} or
 * {@link #LuaThread(Globals, LuaValue)}.
 * <p>
 * The utility classes {@link org.luaj.vm2.lib.jse.JsePlatform} and
 * {@link org.luaj.vm2.lib.jme.JmePlatform} see to it that this {@link Globals}
 * are initialized properly.
 * <p>
 * The behavior of coroutine threads matches closely the behavior of C coroutine
 * library. However, because of the use of Java threads to manage call state, it
 * is possible to yield from anywhere in luaj.
 * <p>
 * Each Java thread wakes up at regular intervals and checks a weak reference to
 * determine if it can ever be resumed. If not, it throws {@link OrphanedThread}
 * which is an {@link java.lang.Error}. Applications should not catch
 * {@link OrphanedThread}, because it can break the thread safety of luaj. The
 * value controlling the polling interval is
 * {@link #thread_orphan_check_interval} and may be set by the user.
 * <p>
 * There are two main ways to abandon a coroutine. The first is to call
 * {@code yield()} from lua, or equivalently {@link Globals#yield(Varargs)}, and
 * arrange to have it never resumed possibly by values passed to yield. The
 * second is to throw {@link OrphanedThread}, which should put the thread in a
 * dead state. In either case all references to the thread must be dropped, and
 * the garbage collector must run for the thread to be garbage collected.
 *
 *
 * @see LuaValue
 * @see org.luaj.vm2.lib.jse.JsePlatform
 * @see org.luaj.vm2.lib.jme.JmePlatform
 * @see org.luaj.vm2.lib.CoroutineLib
 */
public class LuaThread extends LuaValue {

	/** Shared metatable for lua threads. */
	public static LuaValue s_metatable;

	/** The current number of coroutines. Should not be set. */
	public static int coroutine_count = 0;

	/**
	 * Polling interval, in milliseconds, which each thread uses while waiting
	 * to return from a yielded state to check if the lua threads is no longer
	 * referenced and therefore should be garbage collected. A short polling
	 * interval for many threads will consume server resources. Orphaned threads
	 * cannot be detected and collected unless garbage collection is run. This
	 * can be changed by Java startup code if desired.
	 */
	public static long thread_orphan_check_interval = 5000;

	public static final int      STATUS_INITIAL   = 0;
	public static final int      STATUS_SUSPENDED = 1;
	public static final int      STATUS_RUNNING   = 2;
	public static final int      STATUS_NORMAL    = 3;
	public static final int      STATUS_DEAD      = 4;
	public static final String[] STATUS_NAMES     = { "suspended", "suspended", "running", "normal", "dead", };

	public final State state;

	public static final int MAX_CALLSTACK = 256;

	/**
	 * Thread-local used by DebugLib to store debugging state. This is an opaque
	 * value that should not be modified by applications.
	 */
	public Object callstack;

	public final Globals globals;

	/** Error message handler for this thread, if any. */
	public LuaValue errorfunc;

	/** Private constructor for main thread only */
	public LuaThread(Globals globals) {
		state = new State(globals, this, null);
		state.status = STATUS_RUNNING;
		this.globals = globals;
	}

	/**
	 * Create a LuaThread around a function and environment
	 *
	 * @param func The function to execute
	 */
	public LuaThread(Globals globals, LuaValue func) {
		LuaValue.assert_(func != null, "function cannot be null");
		state = new State(globals, this, func);
		this.globals = globals;
	}

	@Override
	public int type() {
		return LuaValue.TTHREAD;
	}

	@Override
	public String typename() {
		return "thread";
	}

	@Override
	public boolean isthread() {
		return true;
	}

	@Override
	public LuaThread optthread(LuaThread defval) {
		return this;
	}

	@Override
	public LuaThread checkthread() {
		return this;
	}

	@Override
	public LuaValue getmetatable() {
		return s_metatable;
	}

	public String getStatus() { return STATUS_NAMES[state.status]; }

	public boolean isMainThread() { return this.state.function == null; }

	public Varargs resume(Varargs args) {
		final LuaThread.State s = this.state;
		if (s.status > LuaThread.STATUS_SUSPENDED)
			return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf(
				"cannot resume " + (s.status == LuaThread.STATUS_DEAD? "dead": "non-suspended") + " coroutine"));
		return s.lua_resume(this, args);
	}

	public static class State implements Runnable {
		private final Globals globals;
		final WeakReference   lua_thread;
		public final LuaValue function;
		Varargs               args   = LuaValue.NONE;
		Varargs               result = LuaValue.NONE;
		String                error  = null;

		/** Hook function control state used by debug lib. */
		public LuaValue hookfunc;

		public boolean hookline;
		public boolean hookcall;
		public boolean hookrtrn;
		public int     hookcount;
		public boolean inhook;
		public int     lastline;
		public int     bytecodes;

		public int status = LuaThread.STATUS_INITIAL;

		State(Globals globals, LuaThread lua_thread, LuaValue function) {
			this.globals = globals;
			this.lua_thread = new WeakReference(lua_thread);
			this.function = function;
		}

		@Override
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

		public synchronized Varargs lua_resume(LuaThread new_thread, Varargs args) {
			LuaThread previous_thread = globals.running;
			try {
				globals.running = new_thread;
				this.args = args;
				if (this.status == STATUS_INITIAL) {
					this.status = STATUS_RUNNING;
					new Thread(this, "Coroutine-" + (++coroutine_count)).start();
				} else {
					this.notify();
				}
				if (previous_thread != null)
					previous_thread.state.status = STATUS_NORMAL;
				this.status = STATUS_RUNNING;
				this.wait();
				return this.error != null? LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf(this.error))
					: LuaValue.varargsOf(LuaValue.TRUE, this.result);
			} catch (InterruptedException ie) {
				throw new OrphanedThread();
			} finally {
				this.args = LuaValue.NONE;
				this.result = LuaValue.NONE;
				this.error = null;
				globals.running = previous_thread;
				if (previous_thread != null)
					globals.running.state.status = STATUS_RUNNING;
			}
		}

		public synchronized Varargs lua_yield(Varargs args) {
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
				} while ( this.status == STATUS_SUSPENDED );
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

}
