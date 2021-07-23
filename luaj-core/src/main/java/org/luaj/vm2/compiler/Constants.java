/*******************************************************************************
* Copyright (c) 2015 Luaj.org. All rights reserved.
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
package org.luaj.vm2.compiler;

import org.luaj.vm2.LocVars;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.Upvaldesc;

/**
 * Constants used by the LuaC compiler and related classes.
 *
 * @see LuaC
 * @see FuncState
 */
public class Constants extends Lua {

	/** Maximum stack size of a luaj vm interpreter instance. */
	public static final int MAXSTACK = 250;

	static final int LUAI_MAXUPVAL = 0xff;
	static final int LUAI_MAXVARS  = 200;
	static final int NO_REG        = MAXARG_A;

	/* OpMode - basic instruction format */
	static final int iABC = 0, iABx = 1, iAsBx = 2;

	/* OpArgMask */
	static final int OpArgN = 0, /* argument is not used */
		OpArgU = 1, /* argument is used */
		OpArgR = 2, /* argument is a register or a jump offset */
		OpArgK = 3; /* argument is a constant or register/constant */

	protected static void _assert(boolean b) {
		if (!b)
			throw new LuaError("compiler assert failed");
	}

	static void SET_OPCODE(InstructionPtr i, int o) {
		i.set(i.get() & MASK_NOT_OP | o<<POS_OP & MASK_OP);
	}

	static void SETARG_A(int[] code, int index, int u) {
		code[index] = code[index] & MASK_NOT_A | u<<POS_A & MASK_A;
	}

	static void SETARG_A(InstructionPtr i, int u) {
		i.set(i.get() & MASK_NOT_A | u<<POS_A & MASK_A);
	}

	static void SETARG_B(InstructionPtr i, int u) {
		i.set(i.get() & MASK_NOT_B | u<<POS_B & MASK_B);
	}

	static void SETARG_C(InstructionPtr i, int u) {
		i.set(i.get() & MASK_NOT_C | u<<POS_C & MASK_C);
	}

	static void SETARG_Bx(InstructionPtr i, int u) {
		i.set(i.get() & MASK_NOT_Bx | u<<POS_Bx & MASK_Bx);
	}

	static void SETARG_sBx(InstructionPtr i, int u) {
		SETARG_Bx(i, u+MAXARG_sBx);
	}

	static int CREATE_ABC(int o, int a, int b, int c) {
		return o<<POS_OP & MASK_OP | a<<POS_A & MASK_A | b<<POS_B & MASK_B | c<<POS_C & MASK_C;
	}

	static int CREATE_ABx(int o, int a, int bc) {
		return o<<POS_OP & MASK_OP | a<<POS_A & MASK_A | bc<<POS_Bx & MASK_Bx;
	}

	static int CREATE_Ax(int o, int a) {
		return o<<POS_OP & MASK_OP | a<<POS_Ax & MASK_Ax;
	}

	// vector reallocation

	static LuaValue[] realloc(LuaValue[] v, int n) {
		LuaValue[] a = new LuaValue[n];
		if (v != null)
			System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
		return a;
	}

	static Prototype[] realloc(Prototype[] v, int n) {
		Prototype[] a = new Prototype[n];
		if (v != null)
			System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
		return a;
	}

	static LuaString[] realloc(LuaString[] v, int n) {
		LuaString[] a = new LuaString[n];
		if (v != null)
			System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
		return a;
	}

	static LocVars[] realloc(LocVars[] v, int n) {
		LocVars[] a = new LocVars[n];
		if (v != null)
			System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
		return a;
	}

	static Upvaldesc[] realloc(Upvaldesc[] v, int n) {
		Upvaldesc[] a = new Upvaldesc[n];
		if (v != null)
			System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
		return a;
	}

	static LexState.Vardesc[] realloc(LexState.Vardesc[] v, int n) {
		LexState.Vardesc[] a = new LexState.Vardesc[n];
		if (v != null)
			System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
		return a;
	}

	static LexState.Labeldesc[] grow(LexState.Labeldesc[] v, int min_n) {
		return v == null? new LexState.Labeldesc[2]: v.length < min_n? realloc(v, v.length*2): v;
	}

	static LexState.Labeldesc[] realloc(LexState.Labeldesc[] v, int n) {
		LexState.Labeldesc[] a = new LexState.Labeldesc[n];
		if (v != null)
			System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
		return a;
	}

	static int[] realloc(int[] v, int n) {
		int[] a = new int[n];
		if (v != null)
			System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
		return a;
	}

	static byte[] realloc(byte[] v, int n) {
		byte[] a = new byte[n];
		if (v != null)
			System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
		return a;
	}

	static char[] realloc(char[] v, int n) {
		char[] a = new char[n];
		if (v != null)
			System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
		return a;
	}

	protected Constants() {}
}
