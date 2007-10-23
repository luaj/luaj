package lua.value;

import lua.Lua;
import lua.StackState;
import lua.VM;
import lua.io.Closure;

/** 
 * Implementation of lua coroutines using Java Threads
 */
public class LThread extends LValue implements Runnable {

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
	
	private StackState threadVm;
	private Thread thread;
	
	private static LThread running;
	
	
	public LThread(Closure c) {
		// TODO: inherit globals!
		threadVm = new StackState();
		threadVm.pushlvalue(new Closure(c.p, threadVm._G));
	}

	public int luaGetType() {
		return Lua.LUA_TTHREAD;
	}
	
	public String toJavaString() {
		return "thread: "+hashCode();
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
				throw new RuntimeException(this+" not running");
			status = STATUS_SUSPENDED;
			this.notify();
			try {
				this.wait();
				status = STATUS_RUNNING;
			} catch ( InterruptedException e ) {
				status = STATUS_DEAD;
				throw new RuntimeException(this+" "+e);
			}
			return false;
		}
	}
	
	/** This needs to leave any values returned by yield in the coroutine 
	 * on the calling vm stack
	 * @param vm
	 * @param nargs 
	 */
	public void resumeFrom(VM vm, int nargs) {

		synchronized ( this ) {
 			if ( status == STATUS_DEAD ) {
				vm.settop(0);
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
				if ( thread == null ) {
					vm.xmove(threadVm, nargs);
					threadVm.prepStackCall();
					thread = new Thread(this);
					thread.start();
				} else {
					threadVm.settop(0);
					vm.xmove(threadVm, nargs);
				}
	
				// run this vm until it yields
				this.notify();
				this.wait();
				
				// copy return values from yielding stack state
				vm.settop(0);
				vm.pushboolean(true);
				if ( threadVm.cc >= 0 ) { 
					threadVm.xmove(vm, threadVm.gettop() - 1);
				} else {
					threadVm.base = 0;
					threadVm.xmove(vm, threadVm.gettop());
				}
	
			} catch ( Throwable t ) {
				status = STATUS_DEAD;
				vm.settop(0);
				vm.pushboolean(false);
				vm.pushstring("thread: "+t);
				this.notify();
				
			} finally {
				// previous thread is now running again
				running = prior;
			}
		}
		
	}

}
