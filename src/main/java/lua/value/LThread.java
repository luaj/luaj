package lua.value;

import lua.Lua;
import lua.StackState;
import lua.VM;
import lua.io.Closure;

public class LThread extends LValue {

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
	
	/** This needs to leave any values returned by yield in the coroutine 
	 * on the calling vm stack
	 * @param vm
	 * @param nargs 
	 */
	public void resumeFrom(VM vm, int nargs) {

		if ( status == STATUS_DEAD ) {
			vm.error("cannot resume dead coroutine");
//			vm.settop(0);
//			vm.pushboolean(false);
//			vm.pushstring("cannot resume dead coroutine");
			return;
		}
		
		// set prior thread to normal status while t
		LThread prior = running;
		try {
			// set our status to running
			running = this;
			if ( prior != null  )
				prior.status = STATUS_NORMAL;
			status = STATUS_RUNNING;
			
			// copy args in
			if ( threadVm.cc < 0 ) {
				vm.xmove(threadVm, nargs);
				threadVm.prepStackCall();
			} else {
				threadVm.settop(0);
				vm.xmove(threadVm, nargs);
			}

			// run this vm until it yields
			while ( threadVm.cc >= 0 && status == STATUS_RUNNING )
				threadVm.exec();
			
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
			
		} finally {
			// previous thread is now running
			running = prior;
			if ( running != null  )
				running.status = STATUS_RUNNING;

			// check if thread is actually dead
			if ( threadVm.cc < 0 )
				status = STATUS_DEAD;
		
		}
		
	}

	public boolean yield() {
		if ( status == STATUS_RUNNING )
			status = STATUS_SUSPENDED;
		return true;
	}
}
