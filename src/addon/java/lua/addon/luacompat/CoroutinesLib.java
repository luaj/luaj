package lua.addon.luacompat;

import lua.GlobalState;
import lua.VM;
import lua.io.Closure;
import lua.value.LFunction;
import lua.value.LTable;
import lua.value.LThread;


public class CoroutinesLib extends LFunction {

	public static void install() {
		LTable lib = new LTable(0,6);
		lib.put("create", new CoroutinesLib(1));
		lib.put("resume", new CoroutinesLib(2));
		lib.put("running", new CoroutinesLib(3));
		lib.put("status", new CoroutinesLib(4));
		lib.put("wrap", new CoroutinesLib(5));
		lib.put("yield", new CoroutinesLib(6));
		GlobalState.getGlobalsTable().put("coroutine",lib);
	}
	
	private final int id;
	private final LThread thread;
	
	public CoroutinesLib() {
		this.id = 0;
		this.thread = null;
	}
	
	private CoroutinesLib( int id ) {
		this.id = id;
		this.thread = null;
	}
	
	private CoroutinesLib( int id, LThread thread ) {
		this.id = id;
		this.thread = thread;
	}
	
	public boolean luaStackCall( VM vm ) {
		switch ( id ) {
			case 0: { // load lib
				install();
				vm.pushnil();
				break;
			}
			case 1: { // create
				Closure c = (Closure) vm.topointer(2);
				vm.pushlvalue( new LThread(c) );
				break;
			}
			case 2: {// resume
				LThread t = (LThread) vm.topointer(2);
				t.resumeFrom( vm, vm.gettop()-2 );
				return false;
			}
			case 3: { // running
				LThread r = LThread.getRunning();
				if ( r != null ) {
					vm.pushlvalue( r );
				} else {
					vm.pushnil();					
				}
				break;
			}
			case 4: { // status
				vm.pushstring( ((LThread) vm.topointer(2)).getStatus() );
				break;
			}
			case 5: { // wrap
				Closure c = (Closure) vm.topointer(2);
				vm.pushlvalue( new CoroutinesLib(7,new LThread(c)) );
				break;
			}
			case 6: { // yield
				LThread r = LThread.getRunning();
				if ( r == null )
					vm.error("main thread can't yield");
				else {
					return r.yield();
				}
			}
			case 7: { // wrapped resume
				LThread t = this.thread;
				t.resumeFrom( vm, vm.gettop()-1 );
				if ( vm.toboolean(1) )
					vm.remove(1);
				else
					vm.error( vm.tostring(2), 0 );
				return false;
			}
		}
		vm.insert(1);
		vm.settop(1);
		return false;
	}
	
	
}
