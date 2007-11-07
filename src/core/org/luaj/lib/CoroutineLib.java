package org.luaj.lib;

import org.luaj.vm.LClosure;
import org.luaj.vm.LFunction;
import org.luaj.vm.LTable;
import org.luaj.vm.LThread;
import org.luaj.vm.LuaState;

public class CoroutineLib extends LFunction {

	private static final String[] NAMES = {
		"loadlib",
		"create",
		"resume",
		"running",
		"status",
		"wrap",
		"yield",
		"wrapped"
	};
	
	public static void install( LTable globals ) {
		LTable lib = new LTable(0,6);
		for ( int i=1; i<=6; i++ )
			lib.put(NAMES[i], new CoroutineLib(i));
		globals.put("coroutine",lib);
	}
	
	private final int id;
	private final LThread thread;
	
	public CoroutineLib() {
		this.id = 0;
		this.thread = null;
	}
	
	private CoroutineLib( int id ) {
		this.id = id;
		this.thread = null;
	}
	
	public String toJavaString() {
		return "coroutine."+NAMES[id];
	}
	
	private CoroutineLib( int id, LThread thread ) {
		this.id = id;
		this.thread = thread;
	}
	
	public boolean luaStackCall( LuaState vm ) {
		switch ( id ) {
			case 0: { // load lib
				install(vm._G);
				vm.pushnil();
				break;
			}
			case 1: { // create
				LClosure c = (LClosure) vm.topointer(2);
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
				LClosure c = (LClosure) vm.topointer(2);
				vm.pushlvalue( new CoroutineLib(7,new LThread(c)) );
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
