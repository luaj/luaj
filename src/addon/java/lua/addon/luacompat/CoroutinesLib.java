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
	
	private int id = 0;
	
	private static LThread running;
	
	public CoroutinesLib() {
		this(0);
	}
	
	private CoroutinesLib( int id ) {
		this.id = id;
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
				LThread prior = running;
				try {
					// whatever is left on the stack by the resumeFrom() implementation 
					// becomes return values!
					running = t;
					t.resumeFrom( vm, prior );
					return false;
				} finally {
					running = prior;
				}
			}
			case 3: { // running
				if ( running != null ) {
					vm.pushlvalue( running );
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
				vm.error( "wrap() not supported" );
				return false;
			}
			case 6: { // yield
				if ( running == null )
					vm.error("main thread can't yield");
				else {
					return running.yield();
				}
			}
			case 7: { // wrapped resume
				vm.error( "wrap() not supported" );
				return false;
			}
		}
		vm.insert(1);
		vm.settop(1);
		return false;
	}
	
	
}
