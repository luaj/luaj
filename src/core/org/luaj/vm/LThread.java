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
package org.luaj.vm;



/** 
 * Implementation of lua coroutines using Java Threads
 */
public class LThread extends LValue implements Runnable {
	
	private static final boolean USE_JAVA_THREADS = true;

	private static final int STATUS_SUSPENDED     = 0;
	private static final int STATUS_RUNNING       = 1;
	private static final int STATUS_NORMAL        = 2;
	private static final int STATUS_DEAD          = 3;
	private static final String[] NAMES = { 
		"suspended", 
		"running", 
		"normal", 
		"dead" };
	
	private int status = STATUS_SUSPENDED;
	
	LuaState threadVm;
	private Thread thread;
	
	static LThread running;
	public LThread(LFunction c, LTable env) {
		threadVm = new LuaState(env);
		threadVm.pushlvalue(c);
	}

	public int luaGetType() {
		return Lua.LUA_TTHREAD;
	}
	
	public String toJavaString() {
		return "thread: "+hashCode();
	}

	// Set the environment if a thread, or closure, and return 1, otherwise return 0
	public boolean luaSetEnv(LTable t) {
		threadVm._G = t;
		return true;
	}

	public String getStatus() {
		return NAMES[status];
	}

	public static LThread getRunning() {
		return running;
	}

	public void run() {
		synchronized ( this ) {
			try {
				threadVm.execute();
			} finally {
				status = STATUS_DEAD;
				this.notify();
			}
		}
	}
	
	public boolean yield() {
		synchronized ( this ) {
			if ( status != STATUS_RUNNING )
				threadVm.error(this+" not running");
			status = STATUS_SUSPENDED;
			if ( USE_JAVA_THREADS ) {
				this.notify();
				try {
					this.wait();
					status = STATUS_RUNNING;
				} catch ( InterruptedException e ) {
					status = STATUS_DEAD;
					threadVm.error(this+" "+e);
				}
			}
			return false;
		}
	}
	
	// This needs to leave any values returned by yield in the coroutine 
	// on the calling vm stack
	// @param vm
	// @param nargs 
	//
	public void resumeFrom(LuaState vm, int nargs) {

		synchronized ( this ) {
 			if ( status == STATUS_DEAD ) {
				vm.resettop();
				vm.pushboolean(false);
				vm.pushstring("cannot resume dead coroutine");
				return;
			}
			
			// set prior thread to normal status while we are running
			LThread prior = running;
			try {
				// set our status to running
				if ( prior != null  )
					prior.status = STATUS_NORMAL;
				running = this;
				status = STATUS_RUNNING;
				
				// copy args in
				if (threadVm.cc < 0) {
					vm.xmove(threadVm, nargs);
					threadVm.prepStackCall();
				} else {
					threadVm.resettop();
					vm.xmove(threadVm, nargs);
				}

				// execute in the other thread
				if ( USE_JAVA_THREADS ) {
					// start the thread
					if ( thread == null ) { 
						thread = new Thread(this);
						thread.start();
					}
					
					// run this vm until it yields
					this.notify();
					this.wait();
				} else {
					// run this vm until it yields
					while (threadVm.cc >= 0 && status == STATUS_RUNNING)
						threadVm.exec();
				}
				
				// copy return values from yielding stack state
				vm.resettop();
				if ( threadVm.cc >= 0 ) { 
					vm.pushboolean(status != STATUS_DEAD);
					threadVm.xmove(vm, threadVm.gettop() - 1);
				} else {
					vm.pushboolean(true);
					threadVm.base = 0;
					threadVm.xmove(vm, threadVm.gettop());
				}
	
			} catch ( Throwable t ) {
				status = STATUS_DEAD;
				vm.resettop();
				vm.pushboolean(false);
				vm.pushstring("thread: "+t);
				if ( USE_JAVA_THREADS ) {
					this.notify();
				}
				
			} finally {
				// previous thread is now running again
				running = prior;
			}
		}
		
	}
}
