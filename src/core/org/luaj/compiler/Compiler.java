package org.luaj.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;

import org.luaj.vm.LString;
import org.luaj.vm.LPrototype;
import org.luaj.vm.Platform;


public class Compiler {

	private static final byte LUAC_BINARY_SIG = '\033';
		
	/** Try to compile into prototype.  
	 * If the supplied stream is a binary file (i.e. lua chunk)
	 * then consume one byte from the input stream, and return null.
	 * 
	 * Otherwise, try to compile the file, and return the Prototype
	 * on success, or throw RuntimeException on syntax error or I/O Exception
	 * 
	 * @param stream  InputStream to read from. 
	 * @param name Name of the chunk
	 * @return null if the first byte indicates it is a binary chunk, 
	 *   a LPrototype instance if it can be compiled, 
	 *   or an exception is thrown if there is an error.
	 * @throws IOException if an I/O exception occurs
	 * @throws RuntimeException if there is a syntax error.
	 */
	public static LPrototype compile(InputStream stream, String name) throws IOException {
		
		int c = stream.read();
		if ( c == LUAC_BINARY_SIG )
			return null;
		Reader r = Platform.getInstance().createReader( stream );
		Compiler compiler = new Compiler();
		return compiler.luaY_parser(c, r, name);
	}

	public int nCcalls;
			
	LPrototype luaY_parser(int firstByte, Reader z, String name) {
		LexState lexstate = new LexState(this, z);
		FuncState funcstate = new FuncState();
		// lexstate.buff = buff;
		lexstate.setinput( this, firstByte, z, new LString(name) );
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
