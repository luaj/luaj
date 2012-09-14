/*******************************************************************************
* Copyright (c) 2009-2011 Luaj.org. All rights reserved.
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

import java.lang.ref.WeakReference;

import org.luaj.vm2.Globals;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaNil;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Print;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.Varargs;

/** 
 * Subclass of {@link LibFunction} which implements the lua standard {@code debug} 
 * library. 
 * <p> 
 * The debug library in luaj tries to emulate the behavior of the corresponding C-based lua library.
 * To do this, it must maintain a separate stack of calls to {@link LuaClosure} and {@link LibFunction} 
 * instances.  
 * Especially when lua-to-java bytecode compiling is being used
 * via a {@link LuaCompiler} such as {@link LuaJC}, 
 * this cannot be done in all cases.  
 * <p> 
 * Typically, this library is included as part of a call to either 
 * {@link JsePlatform#debugGlobals()} or {@link JmePlatform#debugGlobals()}
 * <p>
 * To instantiate and use it directly, 
 * link it into your globals table via {@link LuaValue#load(LuaValue)} using code such as:
 * <pre> {@code
 * LuaTable _G = new LuaTable();
 * _G.load(new DebugLib());
 * } </pre>
 * Doing so will ensure the library is properly initialized 
 * and loaded into the globals table. 
 * <p>
 * @see LibFunction
 * @see JsePlatform
 * @see JmePlatform
 * @see <a href="http://www.lua.org/manual/5.1/manual.html#5.9">http://www.lua.org/manual/5.1/manual.html#5.9</a>
 */
public class DebugLib extends OneArgFunction {
	public static final boolean CALLS = (null != System.getProperty("CALLS"));
	public static final boolean TRACE = (null != System.getProperty("TRACE"));

	// leave this unset to allow obfuscators to 
	// remove it in production builds
	public static boolean DEBUG_ENABLED;
	
	private static final LuaString LUA        = valueOf("Lua");  
	private static final LuaString JAVA       = valueOf("Java");  
	private static final LuaString QMARK      = valueOf("?");  
	private static final LuaString GLOBAL     = valueOf("global");  
	private static final LuaString LOCAL      = valueOf("local");  
	private static final LuaString METHOD     = valueOf("method");  
	private static final LuaString UPVALUE    = valueOf("upvalue");  
	private static final LuaString FIELD      = valueOf("field");
	private static final LuaString CALL       = valueOf("call");  
	private static final LuaString LINE       = valueOf("line");  
	private static final LuaString COUNT      = valueOf("count");  
	private static final LuaString RETURN     = valueOf("return");  
	private static final LuaString TAILRETURN = valueOf("tail return");
	private static final LuaString CONSTANT   = valueOf("constant");  
	
	private static final LuaString FUNC            = valueOf("func");  
	private static final LuaString NUPS            = valueOf("nups");  
	private static final LuaString NAME            = valueOf("name");  
	private static final LuaString NAMEWHAT        = valueOf("namewhat");  
	private static final LuaString WHAT            = valueOf("what");  
	private static final LuaString SOURCE          = valueOf("source");  
	private static final LuaString SHORT_SRC       = valueOf("short_src");  
	private static final LuaString LINEDEFINED     = valueOf("linedefined");  
	private static final LuaString LASTLINEDEFINED = valueOf("lastlinedefined");  
	private static final LuaString CURRENTLINE     = valueOf("currentline");  
	private static final LuaString ACTIVELINES     = valueOf("activelines");  

	Globals globals;
	
	public LuaTable call(LuaValue env) {
		globals = env.checkglobals();
		globals.debuglib = this;
		DEBUG_ENABLED = true;
		LuaTable debug = new LuaTable();
		env.set("debug", new debug());
		env.set("gethook", new gethook());
		env.set("sethook", new sethook());
		env.get("package").get("loaded").set("debug", debug);
		return debug;
	}

	// debug.debug()
	static final class debug extends ZeroArgFunction { 
		public LuaValue call() {
			return NONE;
		}
	}

	// debug.gethook ([thread])
	final class gethook extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			LuaThread t = args.optthread(1, globals.running_thread); 
			return varargsOf(
					t.hookfunc,
					valueOf((t.hookcall?"c":"")+(t.hookline?"l":"")+(t.hookrtrn?"r":"")),
					valueOf(t.hookcount));
		}
	}

	//	debug.getinfo ([thread,] f [, what])
	final class getinfo extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			int a=1;
			LuaThread thread = args.isthread(a)? args.checkthread(a++): globals.running_thread; 
			LuaValue func    = args.optfunction(a++, null);
			String what       = args.optjstring(a++,"");
			return NONE;
		}
	}

	//	debug.getlocal ([thread,] f, local)
	final class getlocal extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			int a=1;
			LuaThread thread = args.isthread(a)? args.checkthread(a++): globals.running_thread; 
			LuaValue func    = args.optfunction(a++, null);
			LuaValue local   = args.checkvalue(a++);
			return thread.callstack.findCallFrame(func).getLocal(local.toint());
		}
	}

	//	debug.getmetatable (value)
	final class getmetatable extends LibFunction {
		public LuaValue call(LuaValue v) {
			LuaValue mt = v.getmetatable();
			return mt != null? mt: NIL;
		}
	}

	//	debug.getregistry ()
	final class getregistry extends ZeroArgFunction {
		public LuaValue call() {
			return globals;
		}
	}

	//	debug.getupvalue (f, up)
	static final class getupvalue extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			LuaValue func = args.checkfunction(1);
			int up = args.checkint(2);
			if ( func instanceof LuaClosure ) {
				LuaClosure c = (LuaClosure) func;
				LuaString name = findupvalue(c, up);
				if ( name != null ) {
					return varargsOf(name, c.upValues[up-1].getValue() );
				}
			}
			return NIL;
		}
	}

	//	debug.getuservalue (u)
	static final class getuservalue extends LibFunction { 
		public LuaValue call(LuaValue u) {
			return u.isuserdata()? u: NIL;
		}
	}
	
	
	// debug.sethook ([thread,] hook, mask [, count])
	final class sethook extends VarArgFunction { 
		public Varargs call(Varargs args) {
			int a=1;
			LuaThread thread = args.isthread(a)? args.checkthread(a++): globals.running_thread; 
			LuaValue func    = args.optfunction(a++, null);
			String str       = args.optjstring(a++,"");
			int count        = args.optint(a++,0);
			boolean call=false,line=false,rtrn=false;
			for ( int i=0; i<str.length(); i++ )
				switch ( str.charAt(i) ) {
					case 'c': call=true; break;
					case 'l': line=true; break;
					case 'r': rtrn=true; break;
				}
			thread.hookfunc = func;
			thread.hookcall = call;
			thread.hookline = line;
			thread.hookcount = count;
			thread.hookrtrn = rtrn;
			return NONE;
		}
	}

	//	debug.setlocal ([thread,] level, local, value)
	final class setlocal extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			int a=1;
			LuaThread thread = args.isthread(a)? args.checkthread(a++): globals.running_thread; 
			LuaValue func    = args.optfunction(a++, null);
			LuaValue local   = args.checkvalue(a++);
			LuaValue value   = args.checkvalue(a++);
			return thread.callstack.findCallFrame(func).setLocal(local.toint(), value);
		}
	}

	//	debug.setmetatable (value, table)
	final class setmetatable extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			LuaValue object = args.arg(1);
			try {
				LuaValue mt = args.opttable(2, null);
				switch ( object.type() ) {
					case TNIL:      LuaNil.s_metatable      = mt; break;
					case TNUMBER:   LuaNumber.s_metatable   = mt; break;
					case TBOOLEAN:  LuaBoolean.s_metatable  = mt; break;
					case TSTRING:   LuaString.s_metatable   = mt; break;
					case TFUNCTION: LuaFunction.s_metatable = mt; break;
					case TTHREAD:   LuaThread.s_metatable   = mt; break;
					default: object.setmetatable( mt );
				}
				return LuaValue.TRUE;
			} catch ( LuaError e ) {
				return varargsOf(FALSE, valueOf(e.toString()));
			}
		}
	}

	//	debug.setupvalue (f, up, value)
	final class setupvalue extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			LuaValue func = args.checkfunction(1);
			int up = args.checkint(2);
			LuaValue value = args.arg(3);
			if ( func instanceof LuaClosure ) {
				LuaClosure c = (LuaClosure) func;
				LuaString name = findupvalue(c, up);
				if ( name != null ) {
					c.upValues[up-1].setValue(value);
					return name;
				}
			}
			return NIL;
		}
	}

	//	debug.setuservalue (udata, value)
	final class setuservalue extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			Object o = args.checkuserdata(1);
			LuaValue v = args.checkvalue(2);
			LuaUserdata u = (LuaUserdata)args.arg1();
			u.m_instance = v.checkuserdata();
			u.m_metatable = v.getmetatable();
			return NONE;
		}
	}
	
	//	debug.traceback ([thread,] [message [, level]])
	final class traceback extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			int a=1;
			LuaThread thread = args.isthread(a)? args.checkthread(a++): globals.running_thread; 
			String message = args.optjstring(a++, null);
			int level = args.optint(a++,1);
			String tb = thread.callstack.traceback(level-1);
			return valueOf(message!=null? message+"\n"+tb: tb);
		}
	}
	
	//	debug.upvalueid (f, n)
	final class upvalueid extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			LuaValue func = args.checkfunction(1);
			int up = args.checkint(2);
			if ( func instanceof LuaClosure ) {
				LuaClosure c = (LuaClosure) func;
				if ( c.upValues != null && up > 0 && up <= c.upValues.length ) {
					return userdataOf(c.upValues[up-1].hashCode());
				}
			}
			return NIL;
		}
	}

	//	debug.upvaluejoin (f1, n1, f2, n2)
	final class upvaluejoin extends VarArgFunction { 
		public Varargs invoke(Varargs args) {
			LuaValue f1 = args.checkfunction(1);
			int n1 = args.checkint(2);
			LuaValue f2 = args.checkfunction(3);
			int n2 = args.checkint(4);
			return NONE;
		}
	}

	static LuaString findupvalue(LuaClosure c, int up) {
		if ( c.upValues != null && up > 0 && up <= c.upValues.length ) {
			if ( c.p.upvalues != null && up <= c.p.upvalues.length )
				return c.p.upvalues[up-1].name;
			else
				return LuaString.valueOf( "."+up );
		}
		return null;
	}

	
	// ------------------- library function implementations -----------------
	protected Varargs _getinfo(Varargs args, LuaValue level0func) {
		int a=1;
		LuaThread thread = args.isthread(a)? args.checkthread(a++): globals.running_thread; 
		LuaValue func = args.arg(a++);
		String what = args.optjstring(a++, "nSluf");
		LuaThread.CallStack callstack = thread.callstack;

		// find the stack info
		LuaThread.CallFrame frame;
		if ( func.isnumber() ) {
			frame = callstack.getCallFrame(func.toint());
		} else {
			frame = callstack.findCallFrame(func);
		}
		if (frame == null)
			return NIL;

		// start a table
		LuaTable info = new LuaTable();
		LuaClosure c = frame.f.isclosure()? (LuaClosure) frame.f: null;
		for (int i = 0, j = what.length(); i < j; i++) {
			switch (what.charAt(i)) {
				case 'S': {
					if ( c != null ) {
						Prototype p = c.p;
						info.set(WHAT, LUA);
						info.set(SOURCE, p.source);
						info.set(SHORT_SRC, valueOf(sourceshort(p)));
						info.set(LINEDEFINED, valueOf(p.linedefined));
						info.set(LASTLINEDEFINED, valueOf(p.lastlinedefined));
					} else {
						String shortName = frame.f.tojstring();
						LuaString name = LuaString.valueOf("[Java] "+shortName);
						info.set(WHAT, JAVA);
						info.set(SOURCE, name);
						info.set(SHORT_SRC, valueOf(shortName));
						info.set(LINEDEFINED, LuaValue.MINUSONE);
						info.set(LASTLINEDEFINED, LuaValue.MINUSONE);
					}
					break;
				}
				case 'l': {
					int line = frame.currentline();
					info.set( CURRENTLINE, valueOf(line) );
					break;
				}
				case 'u': {
					info.set(NUPS, valueOf(c!=null? c.p.upvalues.length: 0));
					break;
				}
				case 'n': {
					LuaString[] kind = frame.getfunckind();
					info.set(NAME, kind!=null? kind[0]: QMARK);
					info.set(NAMEWHAT, kind!=null? kind[1]: EMPTYSTRING);
					break;
				}
				case 'f': {
					info.set( FUNC, frame.f );
					break;
				}
				case 'L': {
					LuaTable lines = new LuaTable();
					info.set(ACTIVELINES, lines);
					int line = frame.currentline();
					if ( line >= 0 )
						lines.set(1, valueOf(line));
					break;
				}
			}
		}
		return info;
	}


	public static String sourceshort(Prototype p) {
		String name = p.source.tojstring();
        if ( name.startsWith("@") || name.startsWith("=") )
			name = name.substring(1);
		else if ( name.startsWith("\033") )
			name = "binary string";
        return name;
	}

	// =======================================================
	
	static void lua_assert(boolean x) {
		if (!x) throw new RuntimeException("lua_assert failed");
	}	

	// return StrValue[] { name, namewhat } if found, null if not
	public static LuaString[] getobjname(Prototype p, int lastpc, int reg) {
		int pc = lastpc; // currentpc(L, ci);
		LuaString name = p.getlocalname(reg + 1, pc);
		if (name != null) /* is a local? */
			return new LuaString[] { name, LOCAL };

		/* else try symbolic execution */
		pc = findsetreg(p, lastpc, reg);
		if (pc != -1) { /* could find instruction? */
			int i = p.code[pc];
			switch (Lua.GET_OPCODE(i)) {
			case Lua.OP_MOVE: {
				int a = Lua.GETARG_A(i);
				int b = Lua.GETARG_B(i); /* move from `b' to `a' */
				if (b < a)
					return getobjname(p, pc, b); /* get name for `b' */
				break;
			}
			case Lua.OP_GETTABUP:
			case Lua.OP_GETTABLE: {
				int k = Lua.GETARG_C(i); /* key index */
				int t = Lua.GETARG_Bx(i); /* table index */
		        LuaString vn = (Lua.GET_OPCODE(i) == Lua.OP_GETTABLE)  /* name of indexed variable */
	                    ? p.getlocalname(t + 1, pc)
	                    : (t < p.upvalues.length ? p.upvalues[t].name : QMARK);
				name = kname(p, k);
				return new LuaString[] { name, vn.eq_b(ENV)? GLOBAL: FIELD };
			}
			case Lua.OP_GETUPVAL: {
				int u = Lua.GETARG_B(i); /* upvalue index */
				name = u < p.upvalues.length ? p.upvalues[u].name : QMARK;
				return new LuaString[] { name, UPVALUE };
			}
		    case Lua.OP_LOADK:
		    case Lua.OP_LOADKX: {
		        int b = (Lua.GET_OPCODE(i) == Lua.OP_LOADK) ? Lua.GETARG_Bx(i)
		                                 : Lua.GETARG_Ax(p.code[pc + 1]);
		        if (p.k[b].isstring()) {
		          name = p.k[b].strvalue();
		          return new LuaString[] { name, CONSTANT };
		        }
		        break;
		    }
			case Lua.OP_SELF: {
				int k = Lua.GETARG_C(i); /* key index */
				name = kname(p, k);
				return new LuaString[] { name, METHOD };
			}
			default:
				break;
			}
		}
		return null; /* no useful name found */
	}

	static LuaString kname(Prototype p, int c) {
		if (Lua.ISK(c) && p.k[Lua.INDEXK(c)].isstring())
			return p.k[Lua.INDEXK(c)].strvalue();
		else
			return QMARK;
	}

	/*
	** try to find last instruction before 'lastpc' that modified register 'reg'
	*/
	static int findsetreg (Prototype p, int lastpc, int reg) {
	  int pc;
	  int setreg = -1;  /* keep last instruction that changed 'reg' */
	  for (pc = 0; pc < lastpc; pc++) {
	    int i = p.code[pc];
	    int op = Lua.GET_OPCODE(i);
	    int a = Lua.GETARG_A(i);
	    switch (op) {
	      case Lua.OP_LOADNIL: {
	        int b = Lua.GETARG_B(i);
	        if (a <= reg && reg <= a + b)  /* set registers from 'a' to 'a+b' */
	          setreg = pc;
	        break;
	      }
	      case Lua.OP_TFORCALL: {
	        if (reg >= a + 2) setreg = pc;  /* affect all regs above its base */
	        break;
	      }
	      case Lua.OP_CALL:
	      case Lua.OP_TAILCALL: {
	        if (reg >= a) setreg = pc;  /* affect all registers above base */
	        break;
	      }
	      case Lua.OP_JMP: {
	        int b = Lua.GETARG_sBx(i);
	        int dest = pc + 1 + b;
	        /* jump is forward and do not skip `lastpc'? */
	        if (pc < dest && dest <= lastpc)
	          pc += b;  /* do the jump */
	        break;
	      }
	      case Lua.OP_TEST: {
	        if (reg == a) setreg = pc;  /* jumped code can change 'a' */
	        break;
	      }
	      default:
	        if (Lua.testAMode(op) && reg == a)  /* any instruction that set A */
	          setreg = pc;
	        break;
	    }
	  }
	  return setreg;
	}


}
