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
package org.luaj.vm2.lib;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class CoroutineLib extends VarArgFunction {
	
	private static final int INIT    = 0;
	private static final int CREATE  = 1;
	private static final int RESUME  = 2;
	private static final int RUNNING = 3;
	private static final int STATUS  = 4;
	private static final int YIELD   = 5;
	private static final int WRAP    = 6;
	private static final int WRAPPED = 7;
	
	public CoroutineLib() {
	}

	private LuaTable init() {
		LuaTable t = new LuaTable();
		bind(t, CoroutineLib.class, new  String[] {
			"create", "resume", "running", "status", "yield", "wrap" },
			CREATE);
		env.set("coroutine", t);
		PackageLib.instance.LOADED.set("coroutine", t);
		return t;
	}
	
	public Varargs invoke(Varargs args) {
		switch ( opcode ) {
			case INIT: {
				return init();
			}
			case CREATE: {
				final LuaValue func = args.checkfunction(1);
				return new LuaThread(func, LuaThread.getGlobals() );
			}
			case RESUME: {
				final LuaThread t = args.checkthread(1);
				return t.resume( args.subargs(2) );
			}
			case RUNNING: {
				final LuaThread r = LuaThread.getRunning();
				return LuaThread.isMainThread(r)? NIL: r;
			}
			case STATUS: {
				return valueOf( args.checkthread(1).getStatus() );
			}
			case YIELD: {
				final LuaThread r = LuaThread.getRunning();
				if ( LuaThread.isMainThread( r ) )
					error("main thread can't yield");
				return r.yield( args );
			}
			case WRAP: {
				final LuaValue func = args.checkfunction(1);
				final LuaThread thread = new LuaThread(func, func.getfenv());
				CoroutineLib cl = new CoroutineLib();
				cl.setfenv(thread);
				cl.name = "wrapped";
				cl.opcode = WRAPPED;
				return cl;
			}
			case WRAPPED: {
				final LuaThread t = (LuaThread) env;
				final Varargs result = t.resume( args );
				if ( result.arg1().toboolean() ) {
					return result.subargs(2);
				} else {
					error( result.arg(2).tojstring() );
				}
			}
			default:
				return NONE;
		}
	}
}
