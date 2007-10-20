package lua.value;

import lua.Lua;
import lua.StackState;
import lua.VM;
import lua.io.Closure;

public class LThread extends LValue {

	private static final int STATUS_SUSPENDED     = 1;
	private static final int STATUS_NORMAL        = 2;
	private static final int STATUS_ACTIVE        = 3;
	private static final int STATUS_DEAD          = 4;
	private static final String[] NAMES = { 
		"suspended", 
		"normal", 
		"active", 
		"dead" };
	
	private int status = STATUS_SUSPENDED;
	
	private StackState threadVm;
	
	
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

	/** This needs to leave any values returned by yield in the corouting 
	 * on the calling vm stack
	 * @param vm
	 * @param prior
	 */
	public void resumeFrom(VM vm, LThread prior) {

		if ( status == STATUS_DEAD ) {
			vm.settop(0);
			vm.pushboolean(false);
			vm.pushstring("cannot resume dead coroutine");
			return;
		}
		
		// set prior thread to normal status while we are running
		if ( prior != null  )
			prior.status = STATUS_NORMAL;
		
		try {	
			// copy args in
			if ( threadVm.cc < 0 ) {
				vm.xmove(threadVm, vm.gettop() - 2);
				threadVm.prepStackCall();
			} else {
				threadVm.settop(0);
				vm.xmove(threadVm, vm.gettop() - 2);
			}

			// run this vm until it yields
			status = STATUS_ACTIVE;
			while ( threadVm.cc >= 0 && status == STATUS_ACTIVE )
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
			if ( threadVm.cc < 0 )
				status = STATUS_DEAD;
			
			// reset prior thread status
			if ( prior != null  )
				prior.status = STATUS_ACTIVE;
		}
		
	}

	public boolean yield() {
		if ( status == STATUS_ACTIVE )
			status = STATUS_SUSPENDED;
		return true;
	}
}
