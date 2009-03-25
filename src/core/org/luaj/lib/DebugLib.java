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
import org.luaj.vm.LClosure;
import org.luaj.vm.LFunction;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.LocVars;
import org.luaj.vm.LuaState;

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
	
	public boolean luaStackCall( LuaState vm ) {
		switch ( id ) {
		case INSTALL:
			install(vm);
			break;
		case DEBUG: 
			debug(vm);
			break;
		case GETFENV:
			getfenv(vm);
			break;
		case GETHOOK: 
			gethook(vm);
			break;
		case GETINFO: 
			getinfo(vm);
			break;
		case GETLOCAL:
			getlocal(vm);
			break;
		case GETMETATABLE:
			getmetatable(vm);
			break;
		case GETREGISTRY:
			getregistry(vm);
			break;
		case GETUPVALUE:
			getupvalue(vm);
			break;
		case SETFENV:
			setfenv(vm);
			break;
		case SETHOOK:
			sethook(vm);
			break;
		case SETLOCAL:
			setlocal(vm);
			break;
		case SETMETATABLE:
			setmetatable(vm);
			break;
		case SETUPVALUE:
			setupvalue(vm);
			break;
		case TRACEBACK:
			traceback(vm);
			break;
		default:
			LuaState.vmerror( "bad package id" );
		}
		return false;
	}

	private void debug(LuaState vm) {
		// TODO: interactive console impl
		vm.resettop();
	}
	
	private void gethook(LuaState vm) {
		LuaState threadVm = vm;
		if ( vm.gettop() >= 2 )
			threadVm = vm.checkthread(2).vm;
		vm.resettop();
		vm.pushlvalue(threadVm.gethook());
		vm.pushinteger(threadVm.gethookmask());
		vm.pushinteger(threadVm.gethookcount());
	}

	private void sethook(LuaState vm) {
		LuaState threadVm = vm;
		if ( vm.gettop() >= 4 ) {
			threadVm = vm.checkthread(2).vm;
			vm.remove(2);
		}
		LFunction func = vm.checkfunction(2);
		LString str    = vm.checklstring(3);
		int count      = vm.optint(4,0);
		int mask       = 0;
		for ( int i=0; i<str.m_length; i++ )
			switch ( str.m_bytes[str.m_offset+i] ) {
				case 'c': mask |= LuaState.LUA_MASKCALL; break;
				case 'l': mask |= LuaState.LUA_MASKLINE; break;
				case 'r': mask |= LuaState.LUA_MASKRET; break;
			}
		threadVm.sethook(func, mask, count);
		vm.resettop();
	}

	private void getfenv(LuaState vm) {
		LValue object = vm.topointer(2);
		LValue env = object.luaGetEnv(null);
		vm.resettop();
		vm.pushlvalue(env!=null? env: LNil.NIL);
	}

	private void setfenv(LuaState vm) {
		LValue object = vm.topointer(2);
		LTable table = vm.checktable(3);
		object.luaSetEnv(table);
		vm.settop(1);
	}

	private void getinfo(LuaState vm) {
		LuaState threadVm = vm;
		CallInfo ci = null;
		LFunction func = null;
		LClosure closure = null;
		String what = "";
		if ( vm.gettop() >= 4 ) {
			threadVm = vm.checkthread(2).vm;
			vm.remove(2);			
		}
		if ( vm.gettop() >= 3 ) {
			what = vm.tostring(3);
		}
		if ( vm.isnumber(2) ) {
			ci = this.getcallinfo(vm, threadVm, vm.tointeger(2));
			closure = ci.closure;
		} else {
			func = vm.checkfunction(2);
			if ( func instanceof LClosure )
				closure = (LClosure) func;
		}
		vm.resettop();
		LTable info = new LTable();
		vm.pushlvalue(info);
		for (int i = 0, n = what.length(); i < n; i++) {
			switch (what.charAt(i)) {
				case 'S': {
					info.put("source", (closure!=null? closure.p.source: new LString("@?")));
					info.put("short_src", (closure!=null? closure.p.source.substring(1, closure.p.source.m_length-1): new LString("?")));
					info.put("linedefined", (closure!=null? closure.p.linedefined: 0));
					info.put("lastlinedefined", (closure!=null? closure.p.lastlinedefined: 0));
					info.put("what", new LString(what));
					break;
				}
				case 'l': {
					info.put( "currentline", (ci!=null? ci.pc: 0) );
					break;
				}
				case 'u': {
					info.put("nups", (closure!=null? closure.p.nups: 0));
					info.put("what", new LString(what));
					break;
				}
				case 'n': {
					// TODO: name
					info.put("name", new LString("?"));
					info.put("namewhat", new LString("?"));
					break;
				}
				case 'f': {
					if ( func != null )
						info.put( "func", func );
					break;
				}
				case 'L': {
					LTable lines = new LTable();
					info.put("activelines", lines);
					if ( closure != null )
						for ( int j=0, k=1; j<closure.p.lineinfo.length; j++, k++ )
							lines.put(k, LInteger.valueOf(closure.p.lineinfo[j]));
					break;
				}
			}
		}
	}

	private void getlocal(LuaState vm) {
		LuaState threadVm = vm;
		if ( vm.gettop() >= 4 ) {
			threadVm = vm.checkthread(2).vm;
			vm.remove(2);
		}
		int level = vm.checkint(2);
		int local = vm.checkint(3);
		CallInfo ci = getcallinfo(vm, threadVm, level);
		LValue value = LNil.NIL;
		LValue name = LNil.NIL;
		LocVars[] vars = ci.closure.p.locvars;
		if ( local > 0 && local <= ci.top-ci.base ) {
			value = threadVm.stack[ ci.base + local - 1 ];
			if ( vars != null && local > 0 && local <= vars.length )
				name = vars[local-1].varname;
		}
		vm.resettop();
		vm.pushlvalue( name );
		vm.pushlvalue( value );
	}

	private void setlocal(LuaState vm) {
		LuaState threadVm = vm;
		if ( vm.gettop() >= 5 ) {
			threadVm = vm.checkthread(2).vm;
			vm.remove(2);
		}
		int level = vm.checkint(2);
		int local = vm.checkint(3);
		LValue value = vm.topointer(4);
		CallInfo ci = getcallinfo(vm, threadVm, level);
		LValue name = LNil.NIL;
		LocVars[] vars = ci.closure.p.locvars;
		if ( local > 0 && local <= ci.top-ci.base ) {
			threadVm.stack[ ci.base + local - 1 ] = value;
			if ( vars != null && local > 0 && local <= vars.length )
				name = vars[local-1].varname;
		}
		vm.resettop();
		vm.pushlvalue( name );
	}

	private CallInfo getcallinfo(LuaState vm, LuaState threadVm, int level) {
		--level ; // level 0 is the debug function itself
		if ( level < 0 || level > threadVm.cc )
			vm.error("level out of range");
		int cc = threadVm.cc-level;
		return threadVm.calls[cc];
	}

	private void getmetatable(LuaState vm) {
		LValue object = vm.topointer(2);
		vm.resettop();
		vm.pushlvalue( object.luaGetMetatable() );
	}

	private void setmetatable(LuaState vm) {
		LValue object = vm.topointer(2);
		LValue table  = vm.totable(3);
		object.luaSetMetatable(table);
		vm.resettop();
		vm.pushlvalue( object );
	}

	private void getregistry(LuaState vm) {
		vm.resettop();
		vm.pushlvalue( new LTable() );
	}

	private void getupvalue(LuaState vm) {
		LFunction func = vm.checkfunction(2);
		int up = vm.checkint(3);
		vm.resettop();
		if ( func instanceof LClosure ) {
			LClosure c = (LClosure) func;
			if ( c.upVals != null && up > 0 && up < c.upVals.length ) {
				vm.pushlvalue(c.upVals[up].getValue());
				return;
			}
		}
		vm.pushnil();
	}

	private void setupvalue(LuaState vm) {
		LFunction func = vm.checkfunction(2);
		int up = vm.checkint(3);
		LValue value = vm.topointer(4);
		vm.resettop();
		if ( func instanceof LClosure ) {
			LClosure c = (LClosure) func;
			if ( c.upVals != null && up > 0 && up < c.upVals.length ) {
				c.upVals[up].setValue(value);
				if ( c.p.upvalues != null && up < c.p.upvalues.length )
					vm.pushlvalue( c.p.upvalues[up] );
				else
					vm.pushstring( "."+up+"" );
				return;
			}
		}
		vm.pushnil();
	}

	private void traceback(LuaState vm) {
		LuaState threadVm = vm;
		int level = 1;
		String message = "";
		StringBuffer sb = new StringBuffer();
		if ( vm.gettop() >= 4 ) {
			threadVm = vm.checkthread(2).vm;
			vm.remove(2);			
		}
		if ( vm.gettop() >= 3 )
			level = vm.optint(3,1);
		if ( vm.gettop() >= 2 )
			message = vm.tostring(2)+"\n";
		message += threadVm.getStackTrace(level); 
		vm.resettop();
		vm.pushstring(sb.toString());
	}
}
