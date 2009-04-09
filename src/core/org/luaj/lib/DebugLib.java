/*******************************************************************************
* Copyright (c) 2009 LuaJ. All rights reserved.
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


import org.luaj.vm.CallInfo;
import org.luaj.vm.LBoolean;
import org.luaj.vm.LClosure;
import org.luaj.vm.LFunction;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNil;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;
import org.luaj.vm.UpVal;

public class DebugLib extends LFunction {

	private static final String[] NAMES = {
		"debuglib",
		"debug",
		"getfenv",
		"gethook",
		"getinfo",
		"getlocal",
		"getmetatable",
		"getregistry",
		"getupvalue",
		"setfenv",
		"sethook",
		"setlocal",
		"setmetatable",
		"setupvalue",
		"traceback",
	};
	
	private static final int INSTALL        = 0;
	private static final int DEBUG        	= 1;
	private static final int GETFENV        = 2;
	private static final int GETHOOK        = 3;
	private static final int GETINFO        = 4;
	private static final int GETLOCAL       = 5;
	private static final int GETMETATABLE 	= 6;
	private static final int GETREGISTRY    = 7;
	private static final int GETUPVALUE    	= 8;
	private static final int SETFENV        = 9;
	private static final int SETHOOK        = 10;
	private static final int SETLOCAL 		= 11;
	private static final int SETMETATABLE   = 12;
	private static final int SETUPVALUE    	= 13;
	private static final int TRACEBACK    	= 14;

	/* maximum stack for a Lua function */
	private static final int MAXSTACK = 250;
	
	private static final LString LUA       = new LString("Lua");  
	private static final LString JAVA      = new LString("Java");  
	private static final LString JAVASRC   = new LString("[Java]");  
	private static final LString QMARK     = new LString("?");  
	private static final LString GLOBAL    = new LString("global");  
	private static final LString LOCAL     = new LString("local");  
	private static final LString METHOD    = new LString("method");  
	private static final LString UPVALUE   = new LString("upvalue");  
	private static final LString FIELD     = new LString("field");
	private static final LString NOSTRING  = new LString("");  
	
	public static void install( LuaState vm ) {
		LTable debug = new LTable();
		for (int i = 1; i < NAMES.length; i++)
			debug.put(NAMES[i], new DebugLib(i));
		vm._G.put("debug", debug);
		PackageLib.setIsLoaded("debug", debug);
	}

	private final int id;
	
	public DebugLib() {
		this.id = INSTALL;
	}
	
	private DebugLib( int id ) {
		this.id = id;
	}
	
	public String toString() {
		return NAMES[id]+"()";
	}
	
	public int invoke( LuaState vm ) {
		switch ( id ) {
		case INSTALL:
			install(vm);
			return 0;
		case DEBUG: 
			return debug(vm);
		case GETFENV:
			return getfenv(vm);
		case GETHOOK: 
			return gethook(vm);
		case GETINFO: 
			return getinfo(vm);
		case GETLOCAL:
			return getlocal(vm);
		case GETMETATABLE:
			return getmetatable(vm);
		case GETREGISTRY:
			return getregistry(vm);
		case GETUPVALUE:
			return getupvalue(vm);
		case SETFENV:
			return setfenv(vm);
		case SETHOOK:
			return sethook(vm);
		case SETLOCAL:
			return setlocal(vm);
		case SETMETATABLE:
			return setmetatable(vm);
		case SETUPVALUE:
			return setupvalue(vm);
		case TRACEBACK:
			return traceback(vm);
		default:
			LuaState.vmerror( "bad package id" );
			return 0;
		}
	}

	// j2se subclass may wish to override and provide actual console here. 
	// j2me platform has not System.in to provide console.
	protected int debug(LuaState vm) {
		return 0;
	}
	
	protected int gethook(LuaState vm) {
		LuaState threadVm = vm;
		if ( vm.gettop() >= 2 )
			threadVm = vm.checkthread(1).vm;
		vm.pushlvalue(threadVm.gethook());
		vm.pushinteger(threadVm.gethookmask());
		vm.pushinteger(threadVm.gethookcount());
		return 3;
	}

	protected LuaState optthreadvm(LuaState vm, int index) {
		if ( ! vm.isthread(index) )
			return vm;
		LuaState threadVm = vm.checkthread(index).vm;
		vm.remove(index);
		return threadVm;
	}
	
	protected int sethook(LuaState vm) {
		LuaState threadVm = optthreadvm(vm, 1);
		LFunction func = vm.isnoneornil(1)? null: vm.checkfunction(1);
		String str    =  vm.optstring(2,"");
		int count      = vm.optint(3,0);
		int mask       = 0;
		for ( int i=0; i<str.length(); i++ )
			switch ( str.charAt(i) ) {
				case 'c': mask |= LuaState.LUA_MASKCALL; break;
				case 'l': mask |= LuaState.LUA_MASKLINE; break;
				case 'r': mask |= LuaState.LUA_MASKRET; break;
			}
		threadVm.sethook(func, mask, count);
		return 0;
	}

	protected int getfenv(LuaState vm) {
		LValue object = vm.topointer(1);
		LValue env = object.luaGetEnv(null);
		vm.pushlvalue(env!=null? env: LNil.NIL);
		return 1;
	}

	protected int setfenv(LuaState vm) {
		LValue object = vm.topointer(1);
		LTable table = vm.checktable(2);
		object.luaSetEnv(table);
		vm.settop(1);
		return 1;
	}
	
	protected int getinfo(LuaState vm) {
		LuaState threadVm = optthreadvm(vm, 1);
		String what = vm.optstring(2, "nSluf");
		
		// find the stack info
		StackInfo si;
		if ( vm.isnumber(1) ) {
			int level = vm.tointeger(1);
			si = getstackinfo(threadVm, level, 1)[0];
			if ( si == null ) {
				return 0;
			}
		} else {			
			LFunction func = vm.checkfunction(1);
			si = findstackinfo(threadVm, func);
		}

		// look up info
		LTable info = new LTable();
		vm.pushlvalue(info);
		LClosure c = si.closure();
		for (int i = 0, n = what.length(); i < n; i++) {
			switch (what.charAt(i)) {
				case 'S': {
					if ( c != null ) {
						LPrototype p = c.p;
						info.put("what", LUA);
						info.put("source", p.source);
						info.put("short_src", new LString(p.sourceshort()));
						info.put("linedefined", p.linedefined);
						info.put("lastlinedefined", p.lastlinedefined);
					} else {
						LString name = (si.func!=null? 
								new LString("[Java] "+si.func.toString()):
								JAVASRC);
						info.put("what", JAVA);
						info.put("source", name);
						info.put("short_src", name);
						info.put("linedefined", -1);
						info.put("lastlinedefined", -1);
					}
					break;
				}
				case 'l': {
					int line = si.currentline();
					info.put( "currentline", line );
					break;
				}
				case 'u': {
					info.put("nups", (c!=null? c.p.nups: 0));
					break;
				}
				case 'n': {
					LString[] kind = si.getfunckind();
					info.put("name", kind!=null? kind[0]: QMARK);
					info.put("namewhat", kind!=null? kind[1]: NOSTRING);
					break;
				}
				case 'f': {
					info.put( "func", si.func );
					break;
				}
				case 'L': {
					LTable lines = new LTable();
					info.put("activelines", lines);
					if ( si.luainfo != null ) {
						int line = si.luainfo.currentline();
						if ( line >= 0 )
							lines.put(1, LInteger.valueOf(line));
					}
					break;
				}
			}
		}
		return 1;
	}
	
	protected int getlocal(LuaState vm) {
		LuaState threadVm = optthreadvm(vm, 1);
		int level = vm.checkint(1);
		int local = vm.checkint(2);
		StackInfo si = getstackinfo(threadVm, level, 1)[0];
		CallInfo ci = (si!=null? si.luainfo: null);
		LPrototype p = (ci!=null? ci.closure.p: null);
		LString name = (p!=null? p.getlocalname(local, ci.currentpc()): null);
		if ( name != null ) {
			LValue value = threadVm.stack[ci.base+(local-1)];
			vm.pushlvalue( name );
			vm.pushlvalue( value );
			return 2;
		} else {
			vm.pushnil();
			return 1;
		}
	}

	protected int setlocal(LuaState vm) {
		LuaState threadVm = optthreadvm(vm, 1);
		int level = vm.checkint(1);
		int local = vm.checkint(2);
		LValue value = vm.topointer(3);
		StackInfo si = getstackinfo(threadVm, level, 1)[0];
		CallInfo ci = (si!=null? si.luainfo: null);
		LPrototype p = (ci!=null? ci.closure.p: null);
		LString name = (p!=null? p.getlocalname(local, ci.currentpc()): null);
		if ( name != null ) {
			threadVm.stack[ci.base+(local-1)] = value;
			vm.pushlvalue(name);
		} else {
			vm.pushnil();
		}
		return 1;
	}

	protected int getmetatable(LuaState vm) {
		LValue object = vm.topointer(1);
		LValue mt = object.luaGetMetatable();
		if ( mt != null )
			vm.pushlvalue( object.luaGetMetatable() );
		else
			vm.pushnil();
		return 1;
	}

	protected int setmetatable(LuaState vm) {
		LValue object = vm.topointer(1);
		try {
			if ( ! vm.isnoneornil(2) )
				object.luaSetMetatable(vm.checktable(3));
			else
				object.luaSetMetatable(null);
			vm.pushboolean(true);
			return 1;
		} catch ( LuaErrorException e ) {
			vm.pushboolean(false);
			vm.pushstring(e.toString());
			return 2;
		}
	}

	protected int getregistry(LuaState vm) {
		vm.pushlvalue( new LTable() );
		return 1;
	}

	private static LString findupvalue(LClosure c, int up) {
		if ( c.upVals != null && up > 0 && up <= c.upVals.length ) {
			if ( c.p.upvalues != null && up <= c.p.upvalues.length )
				return c.p.upvalues[up-1];
			else
				return new LString( "."+up+"" );
		}
		return null;
	}

	protected int getupvalue(LuaState vm) {
		LFunction func = vm.checkfunction(1);
		int up = vm.checkint(2);
		vm.resettop();
		if ( func.isClosure() ) {
			LClosure c = (LClosure) func;
			LString name = findupvalue(c, up);
			if ( name != null ) {
				vm.pushlstring(name);
				vm.pushlvalue(c.upVals[up-1].getValue());
				return 2;
			}
		}
		vm.pushnil();
		return 1;
	}

	protected int setupvalue(LuaState vm) {
		LFunction func = vm.checkfunction(1);
		int up = vm.checkint(2);
		LValue value = vm.topointer(3);
		vm.resettop();
		if ( func instanceof LClosure ) {
			LClosure c = (LClosure) func;
			LString name = findupvalue(c, up);
			if ( name != null ) {
				c.upVals[up-1].setValue(value);
				vm.pushlstring(name);
				return 1;
			}
		}
		vm.pushnil();
		return 1;
	}

	protected int traceback(LuaState vm) {
		LuaState threadVm = optthreadvm(vm, 1);
		String message = "stack traceback:\n";
		int level = vm.optint(2,1);
		if ( ! vm.isnoneornil(1) )
			message = vm.checkstring(1)+"\n";
		String tb = DebugLib.traceback(threadVm, level);
		vm.pushstring(message+tb);
		return 1;
	}
	
	// =================== public utilities ====================

	/** 
	 * @param callinfo the CallInfo to inspect
	 * @param up the 1-based index of the local   
	 * @return { name, value } or null if not found.
	 */ 
	public static LValue[] getlocal(LuaState vm, CallInfo ci, int local) {
		LPrototype p = ci.closure.p;
		LString name = p.getlocalname(local, ci.currentpc());
		if ( name != null ) {
			LValue value = vm.stack[ci.base+(local-1)];
			return new LValue[] { name, value };
		} 
		return null;
	}
	
	/** 
	 * @param c the LClosure to inspect
	 * @param up the 1-based index of the upvalue  
	 * @return { name, value, isclosed } or null if not found.
	 */ 
	public static LValue[] getupvalue(LClosure c, int up) {
		LString name = findupvalue(c, up);
		if ( name != null ) {
			UpVal u = c.upVals[up-1];
			LValue value = u.getValue();
			boolean isclosed = u.isClosed();
			return new LValue[] { name, value, LBoolean.valueOf(isclosed) };
		}
		return null;
	}
	
	/** 
	 * Get a traceback as a string for an arbitrary LuaState 
	 */
	public static String traceback(LuaState vm, int level) {
		StackInfo[] s = getstackinfo(vm, level, 10);
		StringBuffer sb = new StringBuffer();
		for ( int i=0; i<s.length; i++ ) {
			StackInfo si = s[i];
			if ( si != null ) {
				sb.append( "\n\t" );
				sb.append( si.sourceline() );
				sb.append( ": in " );
				sb.append( si.tracename() );
			}
		}
		return sb.toString();
	}
	
	// =======================================================
	
	private static void lua_assert(boolean x) {
		if (!x) throw new RuntimeException("lua_assert failed");
	}	


	private static class StackInfo {
		private LuaState vm;
		private CallInfo caller; // or null if first item on stack
		private int stackpos; // offset into stack
		private CallInfo luainfo; // or null if a java function 
		private LValue func; // or null if a lua call
		public StackInfo(LuaState vm, CallInfo caller, int stackpos, CallInfo luainfo, LFunction func) {
			this.vm = vm;
			this.caller = caller;
			this.stackpos = stackpos;
			this.luainfo = luainfo;
			this.func = func!=null? func: luainfo!=null? luainfo.closure: null;
		}
		public LClosure closure() {
			return luainfo!=null? luainfo.closure:
				func!=null&&func.isClosure()? (LClosure)func:
				null;
		}
		public String sourceline() {
			if ( luainfo != null ) {
				String s = luainfo.closure.p.source.toJavaString();
				int line = currentline();
				return (s.startsWith("@")||s.startsWith("=")? s.substring(1): s) + ":" + line;
			} else {
				return "[Java]";
			}
		}
		public LString[] getfunckind() {
			return (caller!=null && stackpos>=0? getobjname(vm, caller, stackpos): null);
		}
		public int currentline() {
			return luainfo!=null? luainfo.currentline(): -1;
		}
		public String tracename() {
			if ( caller == null )
				return "main chunk";
			if ( func != null )
				return func.toString();
			LString[] kind = getfunckind();
			if ( kind == null )
				return "function ?";
			return "function "+kind[0].toJavaString();
		}
	}

	
	/** 
	 * @param level first level to report
	 * @return array StackInfo with countlevels items, some may be null!
	 */
	private static StackInfo[] getstackinfo(LuaState vm, int level, int countlevels) {
		StackInfo[] si = new StackInfo[countlevels];
		int i = 0;
		LClosure prevclosure = null;
        for (int j=vm.cc; j>=0; --j) {
			
			CallInfo ci = vm.calls[j];
			LFunction f = ci.currentfunc(vm);

			// java, or tailcall? 
			if ( f != null && (! f.isClosure() || f!=prevclosure) ) {
				if ( (level--) <= 0 ) {
					si[i++] = new StackInfo( vm, ci, ci.currentfunca(vm), null, f);
					if ( i >= countlevels )
						return si;
				}
			}
					
			// add the lua closure
			if ( (level--) <= 0 ) {
				if (j>0 && vm.calls[j-1].currentfunc(vm) == ci.closure) {
					CallInfo caller = vm.calls[j-1];
					int callera = caller.currentfunca(vm);
					si[i++] = new StackInfo( vm, caller, callera, ci, ci.closure);
				} else {
					si[i++] = new StackInfo( vm, null, -1, ci, ci.closure);
				}
				if ( i >= countlevels )
					return si;
			}
			prevclosure = ci.closure;
		}
		
		return si;
	}
	
	// look up a function in the stack, if it exists
	private static StackInfo findstackinfo(LuaState vm, LFunction func) {
	    for (int j=vm.cc; j>=0; --j) {
			CallInfo ci = vm.calls[j];
			int instr = ci.closure.p.code[ci.currentpc()];
			if ( Lua.GET_OPCODE(instr) == Lua.OP_CALL ) {
				int a = Lua.GETARG_A(instr);
				if ( func == vm.stack[ci.base + a] )
					return new StackInfo(vm, ci, a, null, func);
				if ( func == ci.closure )
					return new StackInfo(vm, (j>0? vm.calls[j-1]: null), 0, ci, null);
			}
	    }
		return new StackInfo(vm, null, -1, null, func);
	}

	// return LString[] { name, namewhat } if found, null if not
	private static LString[] getobjname(LuaState L, CallInfo ci, int stackpos) {
		LString name;
		if (ci.isLua()) { /* a Lua function? */
			LPrototype p = ci.closure.p;
			int pc = (ci.pc > 0 ? ci.pc - 1 : 0); // currentpc(L, ci);
			int i;// Instruction i;
			name = p.getlocalname(stackpos + 1, pc);
			if (name != null) /* is a local? */
				return new LString[] { name, LOCAL };
			i = symbexec(p, pc, stackpos); /* try symbolic execution */
			lua_assert(pc != -1);
			switch (Lua.GET_OPCODE(i)) {
			case Lua.OP_GETGLOBAL: {
				int g = Lua.GETARG_Bx(i); /* global index */
				// lua_assert(p.k[g].isString());
				return new LString[] { p.k[g].luaAsString(), GLOBAL };
			}
			case Lua.OP_MOVE: {
				int a = Lua.GETARG_A(i);
				int b = Lua.GETARG_B(i); /* move from `b' to `a' */
				if (b < a)
					return getobjname(L, ci, b); /* get name for `b' */
				break;
			}
			case Lua.OP_GETTABLE: {
				int k = Lua.GETARG_C(i); /* key index */
				name = kname(p, k);
				return new LString[] { name, FIELD };
			}
			case Lua.OP_GETUPVAL: {
				int u = Lua.GETARG_B(i); /* upvalue index */
				name = u < p.upvalues.length ? p.upvalues[u] : QMARK;
				return new LString[] { name, UPVALUE };
			}
			case Lua.OP_SELF: {
				int k = Lua.GETARG_C(i); /* key index */
				name = kname(p, k);
				return new LString[] { name, METHOD };
			}
			default:
				break;
			}
		}
		return null; /* no useful name found */
	}

	private static LString kname(LPrototype p, int c) {
		if (Lua.ISK(c) && p.k[Lua.INDEXK(c)].isString())
			return p.k[Lua.INDEXK(c)].luaAsString();
		else
			return QMARK;
	}

	private static boolean checkreg(LPrototype pt,int reg)	{
		return (reg < pt.maxstacksize);
	}

	private static boolean precheck(LPrototype pt) {
		if (!(pt.maxstacksize <= MAXSTACK)) return false;
		lua_assert(pt.numparams + (pt.is_vararg & Lua.VARARG_HASARG) <= pt.maxstacksize);
		lua_assert((pt.is_vararg & Lua.VARARG_NEEDSARG) == 0
				|| (pt.is_vararg & Lua.VARARG_HASARG) != 0);
		if (!(pt.upvalues.length <= pt.nups)) return false;
		if (!(pt.lineinfo.length == pt.code.length || pt.lineinfo.length == 0)) return false;
		if (!(Lua.GET_OPCODE(pt.code[pt.code.length - 1]) == Lua.OP_RETURN)) return false;
		return true;
	}

	private static boolean checkopenop(LPrototype pt,int pc) {
		int i = pt.code[(pc)+1];
		switch (Lua.GET_OPCODE(i)) {
		case Lua.OP_CALL:
		case Lua.OP_TAILCALL:
		case Lua.OP_RETURN:
		case Lua.OP_SETLIST: {
			if (!(Lua.GETARG_B(i) == 0)) return false;
			return true;
		}
		default:
			return false; /* invalid instruction after an open call */
		}
	}
	
	//static int checkArgMode (LPrototype pt, int r, enum OpArgMask mode) {
	private static boolean checkArgMode (LPrototype pt, int r, int mode) {
		switch (mode) {
			case Lua.OpArgN: if (!(r == 0)) return false; break;
			case Lua.OpArgU: break;
			case Lua.OpArgR: checkreg(pt, r); break;
			case Lua.OpArgK:
				if (!(Lua.ISK(r) ? Lua.INDEXK(r) < pt.k.length : r < pt.maxstacksize)) return false;
				break;
		}
		return true;
	}


	// return last instruction, or 0 if error
	private static int symbexec(LPrototype pt, int lastpc, int reg) {
		int pc;
		int last; /* stores position of last instruction that changed `reg' */
		last = pt.code.length - 1; /*
									 * points to final return (a `neutral'
									 * instruction)
									 */
		if (!(precheck(pt))) return 0;
		for (pc = 0; pc < lastpc; pc++) {
			int i = pt.code[pc];
			int op = Lua.GET_OPCODE(i);
			int a = Lua.GETARG_A(i);
			int b = 0;
			int c = 0;
			if (!(op < Lua.NUM_OPCODES)) return 0;
			if (!checkreg(pt, a)) return 0;
			switch (Lua.getOpMode(op)) {
			case Lua.iABC: {
				b = Lua.GETARG_B(i);
				c = Lua.GETARG_C(i);
				if (!(checkArgMode(pt, b, Lua.getBMode(op)))) return 0;
				if (!(checkArgMode(pt, c, Lua.getCMode(op)))) return 0;
				break;
			}
			case Lua.iABx: {
				b = Lua.GETARG_Bx(i);
				if (Lua.getBMode(op) == Lua.OpArgK)
					if (!(b < pt.k.length)) return 0;
				break;
			}
			case Lua.iAsBx: {
				b = Lua.GETARG_sBx(i);
				if (Lua.getBMode(op) == Lua.OpArgR) {
					int dest = pc + 1 + b;
					if (!(0 <= dest && dest < pt.code.length)) return 0;
					if (dest > 0) {
						/* cannot jump to a setlist count */
						int d = pt.code[dest - 1];
						if ((Lua.GET_OPCODE(d) == Lua.OP_SETLIST && Lua.GETARG_C(d) == 0)) return 0;
					}
				}
				break;
			}
			}
			if (Lua.testAMode(op)) {
				if (a == reg)
					last = pc; /* change register `a' */
			}
			if (Lua.testTMode(op)) {
				if (!(pc + 2 < pt.code.length)) return 0; /* check skip */
				if (!(Lua.GET_OPCODE(pt.code[pc + 1]) == Lua.OP_JMP)) return 0;
			}
			switch (op) {
			case Lua.OP_LOADBOOL: {
				if (!(c == 0 || pc + 2 < pt.code.length)) return 0; /* check its jump */
				break;
			}
			case Lua.OP_LOADNIL: {
				if (a <= reg && reg <= b)
					last = pc; /* set registers from `a' to `b' */
				break;
			}
			case Lua.OP_GETUPVAL:
			case Lua.OP_SETUPVAL: {
				if (!(b < pt.nups)) return 0;
				break;
			}
			case Lua.OP_GETGLOBAL:
			case Lua.OP_SETGLOBAL: {
				if (!(pt.k[b].isString())) return 0;
				break;
			}
			case Lua.OP_SELF: {
				if (!checkreg(pt, a + 1)) return 0;
				if (reg == a + 1)
					last = pc;
				break;
			}
			case Lua.OP_CONCAT: {
				if (!(b < c)) return 0; /* at least two operands */
				break;
			}
			case Lua.OP_TFORLOOP: {
				if (!(c >= 1)) return 0; /* at least one result (control variable) */
				if (!checkreg(pt, a + 2 + c)) return 0; /* space for results */
				if (reg >= a + 2)
					last = pc; /* affect all regs above its base */
				break;
			}
			case Lua.OP_FORLOOP:
			case Lua.OP_FORPREP:
				if (!checkreg(pt, a + 3)) return 0;
				/* go through */
			case Lua.OP_JMP: {
				int dest = pc + 1 + b;
				/* not full check and jump is forward and do not skip `lastpc'? */
				if (reg != Lua.NO_REG && pc < dest && dest <= lastpc)
					pc += b; /* do the jump */
				break;
			}
			case Lua.OP_CALL:
			case Lua.OP_TAILCALL: {
				if (b != 0) {
					if (!checkreg(pt, a + b - 1)) return 0;
				}
				c--; /* c = num. returns */
				if (c == Lua.LUA_MULTRET) {
					if (!(checkopenop(pt, pc))) return 0;
				} else if (c != 0)
					if (!checkreg(pt, a + c - 1)) return 0;
				if (reg >= a)
					last = pc; /* affect all registers above base */
				break;
			}
			case Lua.OP_RETURN: {
				b--; /* b = num. returns */
				if (b > 0)
					if (!checkreg(pt, a + b - 1)) return 0;
				break;
			}
			case Lua.OP_SETLIST: {
				if (b > 0)
					if (!checkreg(pt, a + b)) return 0;
				if (c == 0)
					pc++;
				break;
			}
			case Lua.OP_CLOSURE: {
				int nup, j;
				if (!(b < pt.p.length)) return 0;
				nup = pt.p[b].nups;
				if (!(pc + nup < pt.code.length)) return 0;
				for (j = 1; j <= nup; j++) {
					int op1 = Lua.GET_OPCODE(pt.code[pc + j]);
					if (!(op1 == Lua.OP_GETUPVAL || op1 == Lua.OP_MOVE)) return 0;
				}
				if (reg != Lua.NO_REG) /* tracing? */
					pc += nup; /* do not 'execute' these pseudo-instructions */
				break;
			}
			case Lua.OP_VARARG: {
				if (!((pt.is_vararg & Lua.VARARG_ISVARARG) != 0
						&& (pt.is_vararg & Lua.VARARG_NEEDSARG) == 0)) return 0;
				b--;
				if (b == Lua.LUA_MULTRET)
					if (!(checkopenop(pt, pc))) return 0;
				if (!checkreg(pt, a + b - 1)) return 0;
				break;
			}
			default:
				break;
			}
		}
		return pt.code[last];
	}
	
}
