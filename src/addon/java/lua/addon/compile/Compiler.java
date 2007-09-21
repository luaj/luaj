package lua.addon.compile;

import java.io.Reader;
import java.util.Hashtable;

import lua.io.Proto;
import lua.value.LString;

public class Compiler {

	public int nCcalls;
	
	public static Proto compile( Reader reader, String name ) {
		Compiler compiler = new Compiler();
		return compiler.luaY_parser(reader, name);
	}

			
	Proto luaY_parser(Reader z, String name) {
		LexState lexstate = new LexState(this, z);
		FuncState funcstate = new FuncState();
		// lexstate.buff = buff;
		lexstate.setinput( this, z, new LString(name) );
		lexstate.open_func(funcstate);
		/* main func. is always vararg */
		funcstate.varargflags = LuaC.VARARG_ISVARARG;
		funcstate.f.is_vararg = true;
		funcstate.f.source = new LString("@"+name);
		lexstate.next(); /* read first token */
		lexstate.chunk();
		lexstate.check(LexState.TK_EOS);
		lexstate.close_func();
		LuaC._assert (funcstate.prev == null);
		LuaC._assert (funcstate.f.nups == 0);
		LuaC._assert (lexstate.fs == null);
		return funcstate.f;
	}
	
	
	// these string utilities were originally 
	// part of the Lua C State object, so we have 
	// left them here for now until we deterimine 
	// what exact purpose they serve.
	
	// table of all strings -> TString mapping. 
	// this includes reserved words that must be identified 
	// during lexical analysis for the compiler to work at all.
	// TODO: consider moving this to LexState and simplifying 
	// all the various "newstring()" like functions
	Hashtable strings = new Hashtable();
	
	public LString newTString(String s) {
		LString t = (LString) strings.get(s);
		if ( t == null )
			strings.put( s, t = new LString(s) );
		return t;
	}

	public String pushfstring(String string) {
		return string;
	}

	public LString newlstr(char[] chars, int offset, int len) {
		return newTString( new String(chars,offset,len) );
	}
}
