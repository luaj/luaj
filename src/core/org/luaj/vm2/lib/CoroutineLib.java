/*******************************************************************************
* Copyright (c) 2007-2011 LuaJ. All rights reserved.
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

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jme.JmePlatform;
import org.luaj.vm2.lib.jse.JsePlatform;

/** 
 * Subclass of {@link LibFunction} which implements the lua standard {@code coroutine} 
 * library. 
 * <p> 
 * The coroutine library in luaj has the same behavior as the
 * coroutine library in C, but is implemented using Java Threads to maintain 
 * the call state between invocations.  Therefore it can be yielded from anywhere, 
 * similar to the "Coco" yield-from-anywhere patch available for C-based lua.
 * However, coroutines that are yielded but never resumed to complete their execution
 * may not be collected by the garbage collector. 
 * <p> 
 * Typically, this library is included as part of a call to either 
 * {@link JsePlatform#standardGlobals()} or {@link JmePlatform#standardGlobals()}
 * <p>
 * To instantiate and use it directly, 
 * link it into your globals table via {@link LuaValue#load(LuaValue)} using code such as:
 * <pre> {@code
 * LuaTable _G = new LuaTable();
 * _G.load(new CoroutineLib());
 * } </pre>
 * Doing so will ensure the library is properly initialized 
 * and loaded into the globals table. 
 * <p>
 * @see LibFunction
 * @see JsePlatform
 * @see JmePlatform
 * @see <a href="http://www.lua.org/manual/5.1/manual.html#5.2">http://www.lua.org/manual/5.1/manual.html#5.2</a>
 */
public class CoroutineLib extends OneArgFunction {

	static long thread_orphan_check_interval = 30000;

	static int coroutine_count = 0;

	public LuaValue call(LuaValue env) {
		LuaTable coroutine = new LuaTable();
		coroutine.set("create", new create());
		coroutine.set("resume", new resume());
		coroutine.set("running", new running());
		coroutine.set("status", new status());
		coroutine.set("yield", new yield());
		coroutine.set("wrap", new wrap());
		env.set("coroutine", coroutine);
		env.get("package").get("loaded").set("coroutine", TRUE);
		return coroutine;
	}

	final class create extends LibFunction {
		public LuaValue call(LuaValue f) {
			return new LuaThread(f.checkfunction());
		}
	}

	final class resume extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			if (!(args.arg1() instanceof LuaThread)) argerror(1, "thread");
			final LuaThread t = (LuaThread) args.arg1();
			return resume( t, args.subargs(2) );
		}
	}

	final class running extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			final LuaThread r = LuaThread.getRunning();
			return varargsOf(r, valueOf(r.isMainThread()));
		}
	}

	static final class status extends LibFunction {
		public LuaValue call(LuaValue t) {
			LuaThread lt = t.checkthread();
			return valueOf( lt.getStatus() );
		}
	}
	
	final class yield extends VarArgFunction {
		public Varargs invoke(Varargs args) {
			return yield( args );
		}
	}

	final class wrap extends LibFunction {
		public LuaValue call(LuaValue f) {
			final LuaValue func = f.checkfunction();
			final LuaThread thread = new LuaThread(func);
			return new wrapper(thread);
		}
	}

	final class wrapper extends VarArgFunction {
		final LuaThread luathread;
		wrapper(LuaThread luathread) {
			this.luathread = luathread;
		}
		public Varargs invoke(Varargs args) {
			final Varargs result = resume(luathread, args);
			if ( result.arg1().toboolean() ) {
				return result.subargs(2);
			} else {
				return error( result.arg(2).tojstring() );
			}
		}
	}

	Varargs yield(Varargs args) {
		final LuaThread.State s = LuaThread.getRunning().state;
		if (s.function == null)
			throw new LuaError("cannot yield main thread");
		return s.lua_yield(args);
	}

	Varargs resume(LuaThread t, Varargs args) {
		final LuaThread.State s = t.state;
		if (s.status > LuaThread.STATUS_SUSPENDED)
			return LuaValue.varargsOf(LuaValue.FALSE, 
					LuaValue.valueOf("cannot resume "+(s.status==LuaThread.STATUS_DEAD? "dead": "non-suspended")+" coroutine"));
		return s.lua_resume(t, args);
	}
}
