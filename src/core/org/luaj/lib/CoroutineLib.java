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
package org.luaj.lib;

import org.luaj.vm.LClosure;
import org.luaj.vm.LFunction;
import org.luaj.vm.LTable;
import org.luaj.vm.LThread;
import org.luaj.vm.LuaState;

public class CoroutineLib extends LFunction {

	private static final String[] NAMES = {
		"install",
		"create",
		"resume",
		"running",
		"status",
		"wrap",
		"yield",
		"wrapped"
	};
	
	private static final int INSTALL = 0;
	private static final int CREATE  = 1;
	private static final int RESUME  = 2;
	private static final int RUNNING = 3;
	private static final int STATUS  = 4;
	private static final int WRAP    = 5;
	private static final int YIELD   = 6;
	private static final int WRAPPED = 7;
	
	public static void install( LTable globals ) {
		LTable lib = new LTable(0,6);
		for ( int i=1; i<=YIELD; i++ )
			lib.put(NAMES[i], new CoroutineLib(i));
		globals.put("coroutine",lib);
		PackageLib.setIsLoaded("coroutine", lib);
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
	
	public String toString() {
		return NAMES[id]+"()";
	}
	
	private CoroutineLib( int id, LThread thread ) {
		this.id = id;
		this.thread = thread;
	}
	
	public boolean luaStackCall( LuaState vm ) {
		switch ( id ) {
			case INSTALL: {
				install(vm._G);
				break;
			}
			case CREATE: {
				LFunction c = vm.checkfunction(2);
				vm.pushlvalue( new LThread( c, c.luaGetEnv(vm._G) ) );
				break;
			}
			case RESUME: {
				LThread t = vm.checkthread(2);
				t.resumeFrom( vm, vm.gettop()-2 );
				return false;
			}
			case RUNNING: {
				LThread r = LThread.getRunning();
				if ( r != null ) {
					vm.pushlvalue( r );
				} else {
					vm.pushnil();					
				}
				break;
			}
			case STATUS: {
				vm.pushstring( vm.checkthread(2).getStatus() );
				break;
			}
			case WRAP: {
				LFunction c = vm.checkfunction(2);
				vm.pushlvalue( new CoroutineLib(WRAPPED,new LThread(c, c.luaGetEnv(vm._G))) );
				break;
			}
			case YIELD: {
				LThread r = LThread.getRunning();
				if ( r == null )
					vm.error("main thread can't yield");
				else {
					return r.yield();
				}
			}
			case WRAPPED: {
				LThread t = this.thread;
				t.resumeFrom( vm, vm.gettop()-1 );
				if ( vm.toboolean(1) )
					vm.remove(1);
				else
					vm.error( vm.tostring(2) );
				return false;
			}
		}
		vm.insert(1);
		vm.settop(1);
		return false;
	}
	
	
}
