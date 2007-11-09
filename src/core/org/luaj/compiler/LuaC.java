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
package org.luaj.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;

import org.luaj.vm.LPrototype;
import org.luaj.vm.LString;
import org.luaj.vm.LValue;
import org.luaj.vm.LoadState;
import org.luaj.vm.LocVars;
import org.luaj.vm.Lua;
import org.luaj.vm.Platform;
import org.luaj.vm.LoadState.LuaCompiler;


/**
 * Compiler for Lua 
 */
public class LuaC extends Lua implements LuaCompiler {

	/** Install the compiler so that LoadState will first 
	 * try to use it when handed bytes that are 
	 * not already a compiled lua chunk.
	 */
	public static void install() {
		LoadState.compiler = new LuaC();
	}

	protected static void _assert(boolean b) {		
		if (!b) throw new RuntimeException("assert failed");
	}
	
	public static final int MAXSTACK = 250;
	static final int LUAI_MAXUPVALUES = 60;
	static final int LUAI_MAXVARS = 200;
	static final int LFIELDS_PER_FLUSH = 50;
	static final int NO_REG		 = MAXARG_A;
	
	/* masks for new-style vararg */
	static final int     VARARG_HASARG		= 1;
	static final int     VARARG_ISVARARG	= 2;
	static final int     VARARG_NEEDSARG	= 4;


	/* OpMode - basic instruction format */
	static final int 
		iABC = 0,
		iABx = 1,
		iAsBx = 2;

	/* OpArgMask */
	static final int 
	  OpArgN = 0,  /* argument is not used */
	  OpArgU = 1,  /* argument is used */
	  OpArgR = 2,  /* argument is a register or a jump offset */
	  OpArgK = 3;   /* argument is a constant or register/constant */


	static void SET_OPCODE(InstructionPtr i,int o) {
		i.set( ( i.get() & (MASK_NOT_OP)) | ((o << POS_OP) & MASK_OP) );
	}
	
	static void SETARG_A(InstructionPtr i,int u) {
		i.set( ( i.get() & (MASK_NOT_A)) | ((u << POS_A) & MASK_A) );
	}

	static void SETARG_B(InstructionPtr i,int u) {
		i.set( ( i.get() & (MASK_NOT_B)) | ((u << POS_B) & MASK_B) );
	}

	static void SETARG_C(InstructionPtr i,int u) {
		i.set( ( i.get() & (MASK_NOT_C)) | ((u << POS_C) & MASK_C) );
	}
	
	static void SETARG_Bx(InstructionPtr i,int u) {
		i.set( ( i.get() & (MASK_NOT_Bx)) | ((u << POS_Bx) & MASK_Bx) );
	}
	
	static void SETARG_sBx(InstructionPtr i,int u) {
		SETARG_Bx( i, u + MAXARG_sBx );
	}

	static int CREATE_ABC(int o, int a, int b, int c) {
		return ((o << POS_OP) & MASK_OP) |
				((a << POS_A) & MASK_A) |
				((b << POS_B) & MASK_B) |
				((c << POS_C) & MASK_C) ;
	}
	
	static int CREATE_ABx(int o, int a, int bc) {
		return ((o << POS_OP) & MASK_OP) |
				((a << POS_A) & MASK_A) |
				((bc << POS_Bx) & MASK_Bx) ;
 	}

	// vector reallocation
	
	static LValue[] realloc(LValue[] v, int n) {
		LValue[] a = new LValue[n];
		if ( v != null )
			System.arraycopy(v, 0, a, 0, Math.min(v.length,n));
		return a;
	}

	static LPrototype[] realloc(LPrototype[] v, int n) {
		LPrototype[] a = new LPrototype[n];
		if ( v != null )
			System.arraycopy(v, 0, a, 0, Math.min(v.length,n));
		return a;
	}

	static LString[] realloc(LString[] v, int n) {
		LString[] a = new LString[n];
		if ( v != null )
			System.arraycopy(v, 0, a, 0, Math.min(v.length,n));
		return a;
	}

	static LocVars[] realloc(LocVars[] v, int n) {
		LocVars[] a = new LocVars[n];
		if ( v != null )
			System.arraycopy(v, 0, a, 0, Math.min(v.length,n));
		return a;
	}

	static int[] realloc(int[] v, int n) {
		int[] a = new int[n];
		if ( v != null )
			System.arraycopy(v, 0, a, 0, Math.min(v.length,n));
		return a;
	}

	static char[] realloc(char[] v, int n) {
		char[] a = new char[n];
		if ( v != null )
			System.arraycopy(v, 0, a, 0, Math.min(v.length,n));
		return a;
	}

	public int nCcalls;
	Hashtable strings = new Hashtable();

	/** Utility method to invoke the compiler for an input stream 
	 */
	public static LPrototype compile(InputStream is, String string) throws IOException {
		return new LuaC().compile(is.read(), is, string);
	}

	/** Compile source bytes into a LPrototype.  
	 * 
	 * Try to compile the file, and return the Prototype on success, 
	 * or throw RuntimeException on syntax error or I/O Exception
	 * 
	 * @param firstByte the first byte from the InputStream.  
	 * This can be read by the client and tested to see if it is already a binary chunk.  
	 * @param stream  InputStream to read from. 
	 * @param name Name of the chunk
	 * @return null if the first byte indicates it is a binary chunk, 
	 *   a LPrototype instance if it can be compiled, 
	 *   or an exception is thrown if there is an error.
	 * @throws IOException if an I/O exception occurs
	 * @throws RuntimeException if there is a syntax error.
	 */
	public LPrototype compile(int firstByte, InputStream stream, String name) throws IOException {
		Reader r = Platform.getInstance().createReader( stream );
		LuaC compiler = new LuaC();
		return compiler.luaY_parser(firstByte, r, name);
	}

	/** Parse the input */
	private LPrototype luaY_parser(int firstByte, Reader z, String name) {
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

	public LString newlstr(char[] chars, int offset, int len) {
		return newTString( new String(chars,offset,len) );
	}

	public LString newTString(String s) {
		LString t = (LString) strings.get(s);
		if ( t == null )
			strings.put( s, t = new LString(s) );
		return t;
	}

	public String pushfstring(String string) {
		return string;
	}

}
