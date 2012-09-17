/*******************************************************************************
 * Copyright (c) 2012 Luaj.org. All rights reserved.
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
package org.luaj.vm2;

import junit.framework.TestCase;

/**
 * Tests of basic unary and binary operators on main value types.
 */
public class VarargsTest extends TestCase {

	static LuaValue A = LuaValue.valueOf("a");
	static LuaValue B = LuaValue.valueOf("b");
	static LuaValue C = LuaValue.valueOf("c");
	static LuaValue D = LuaValue.valueOf("d");
	static LuaValue E = LuaValue.valueOf("e");
	static LuaValue F = LuaValue.valueOf("f");
	static LuaValue G = LuaValue.valueOf("g");
	static LuaValue H = LuaValue.valueOf("h");
	static LuaValue Z = LuaValue.valueOf("z");
	static LuaValue NIL = LuaValue.NIL;
	static Varargs A_G = LuaValue.varargsOf(new LuaValue[] { A, B, C, D, E, F, G });
	static Varargs B_E = LuaValue.varargsOf(new LuaValue[] { B, C, D, E });
	static Varargs C_G = LuaValue.varargsOf(new LuaValue[] { C, D, E, F, G });
	static Varargs C_E = LuaValue.varargsOf(new LuaValue[] { C, D, E });
	static Varargs DE = LuaValue.varargsOf(new LuaValue[] { D, E });
	static Varargs E_G = LuaValue.varargsOf(new LuaValue[] { E, F, G });
	static Varargs FG = LuaValue.varargsOf(new LuaValue[] { F, G });
	static LuaValue[] Z_H_array = {Z, A, B, C, D, E, F, G, H };
	static Varargs A_G_alt = new Varargs.ArrayPartVarargs(Z_H_array, 1, 7);
	static Varargs B_E_alt = new Varargs.ArrayPartVarargs(Z_H_array, 2, 4);
	static Varargs C_G_alt = new Varargs.ArrayPartVarargs(Z_H_array, 3, 5);
	static Varargs C_E_alt = new Varargs.ArrayPartVarargs(Z_H_array, 3, 3);
	static Varargs C_E_alt2 = LuaValue.varargsOf(C, D, E);
	static Varargs DE_alt = new Varargs.PairVarargs(D,E);
	static Varargs DE_alt2 = LuaValue.varargsOf(D,E);
	static Varargs E_G_alt = new Varargs.ArrayPartVarargs(Z_H_array, 5, 3);
	static Varargs FG_alt = new Varargs.PairVarargs(F, G);
	static Varargs NONE = LuaValue.NONE;

	static void expectEquals(Varargs x, Varargs y) {
		assertEquals(x.narg(), y.narg());
		assertEquals(x.arg1(), y.arg1());
		assertEquals(x.arg(0), y.arg(0));
		assertEquals(x.arg(-1), y.arg(-1));
		assertEquals(x.arg(2), y.arg(2));
		assertEquals(x.arg(3), y.arg(3));		
		for (int i = 4; i < x.narg() + 2; ++i)
			assertEquals(x.arg(i), y.arg(i));
	}
	
	public void testSanity() {
		expectEquals(A_G, A_G);
		expectEquals(A_G_alt, A_G_alt);
		expectEquals(A_G, A_G_alt);
		expectEquals(B_E, B_E_alt);
		expectEquals(C_G, C_G_alt);
		expectEquals(C_E, C_E_alt);
		expectEquals(C_E, C_E_alt2);
		expectEquals(DE, DE_alt);
		expectEquals(DE, DE_alt2);
		expectEquals(E_G, E_G_alt);
		expectEquals(FG, FG_alt);
		expectEquals(FG_alt, FG_alt);
		expectEquals(A, A);
		expectEquals(NONE, NONE);
		expectEquals(NIL, NIL);
	}

	public void testNegativeIndices() {
		expectNegSubargsError(A_G);
		expectNegSubargsError(A_G_alt);
		expectNegSubargsError(B_E);
		expectNegSubargsError(B_E_alt);
		expectNegSubargsError(C_G);
		expectNegSubargsError(C_G_alt);
		expectNegSubargsError(C_E);
		expectNegSubargsError(C_E_alt);
		expectNegSubargsError(C_E_alt2);
		expectNegSubargsError(DE);
		expectNegSubargsError(DE_alt);
		expectNegSubargsError(DE_alt2);
		expectNegSubargsError(E_G);
		expectNegSubargsError(FG);
		expectNegSubargsError(A);
		expectNegSubargsError(NONE);
		expectNegSubargsError(NIL);
	}
	
	public void testVarargsSubargs() {
		expectEquals(A_G, A_G.subargs(1));
		expectEquals(A_G, A_G_alt.subargs(1));
		expectEquals(C_G, A_G.subargs(3));
		expectEquals(C_G, A_G_alt.subargs(3));
		expectEquals(C_G, A_G.subargs(3).subargs(1));
		expectEquals(C_G, A_G_alt.subargs(3).subargs(1));
		expectEquals(E_G, A_G.subargs(5));
		expectEquals(E_G, A_G_alt.subargs(5));
		expectEquals(E_G, A_G.subargs(5).subargs(1));
		expectEquals(E_G, A_G_alt.subargs(5).subargs(1));
		expectEquals(FG, A_G.subargs(6));
		expectEquals(FG, A_G_alt.subargs(6));
		expectEquals(FG, A_G.subargs(6).subargs(1));
		expectEquals(FG, A_G_alt.subargs(6).subargs(1));
		expectEquals(G, A_G.subargs(7));
		expectEquals(G, A_G_alt.subargs(7));
		expectEquals(G, A_G.subargs(7).subargs(1));
		expectEquals(G, A_G_alt.subargs(7).subargs(1));
		expectEquals(NONE, A_G.subargs(8));
		expectEquals(NONE, A_G_alt.subargs(8));
		expectEquals(NONE, A_G.subargs(8).subargs(1));
		expectEquals(NONE, A_G_alt.subargs(8).subargs(1));

		expectEquals(C_G, C_G.subargs(1));
		expectEquals(C_G, C_G_alt.subargs(1));
		expectEquals(E_G, C_G.subargs(3));
		expectEquals(E_G, C_G_alt.subargs(3));
		expectEquals(E_G, C_G.subargs(3).subargs(1));
		expectEquals(E_G, C_G_alt.subargs(3).subargs(1));
		expectEquals(FG, C_G.subargs(4));
		expectEquals(FG, C_G_alt.subargs(4));
		expectEquals(FG, C_G.subargs(4).subargs(1));
		expectEquals(FG, C_G_alt.subargs(4).subargs(1));
		expectEquals(G, C_G.subargs(5));
		expectEquals(G, C_G_alt.subargs(5));
		expectEquals(G, C_G.subargs(5).subargs(1));
		expectEquals(G, C_G_alt.subargs(5).subargs(1));
		expectEquals(NONE, C_G.subargs(6));
		expectEquals(NONE, C_G_alt.subargs(6));
		expectEquals(NONE, C_G.subargs(6).subargs(1));
		expectEquals(NONE, C_G_alt.subargs(6).subargs(1));

		expectEquals(E_G, E_G.subargs(1));
		expectEquals(E_G, E_G_alt.subargs(1));
		expectEquals(FG, E_G.subargs(2));
		expectEquals(FG, E_G_alt.subargs(2));
		expectEquals(FG, E_G.subargs(2).subargs(1));
		expectEquals(FG, E_G_alt.subargs(2).subargs(1));
		expectEquals(G, E_G.subargs(3));
		expectEquals(G, E_G_alt.subargs(3));
		expectEquals(G, E_G.subargs(3).subargs(1));
		expectEquals(G, E_G_alt.subargs(3).subargs(1));
		expectEquals(NONE, E_G.subargs(4));
		expectEquals(NONE, E_G_alt.subargs(4));
		expectEquals(NONE, E_G.subargs(4).subargs(1));
		expectEquals(NONE, E_G_alt.subargs(4).subargs(1));

		expectEquals(FG, FG.subargs(1));
		expectEquals(FG, FG_alt.subargs(1));
		expectEquals(G, FG.subargs(2));
		expectEquals(G, FG_alt.subargs(2));
		expectEquals(G, FG.subargs(2).subargs(1));
		expectEquals(G, FG_alt.subargs(2).subargs(1));
		expectEquals(NONE, FG.subargs(3));
		expectEquals(NONE, FG_alt.subargs(3));
		expectEquals(NONE, FG.subargs(3).subargs(1));
		expectEquals(NONE, FG_alt.subargs(3).subargs(1));

		expectEquals(NONE, NONE.subargs(1));
		expectEquals(NONE, NONE.subargs(2));
	}

	static void expectNegSubargsError(Varargs v) {
		String expected_msg = "bad argument #1: start must be > 0";
		try {
			v.subargs(0);
			fail("Failed to throw exception for index 0");
		} catch ( LuaError e ) {
			assertEquals(expected_msg, e.getMessage());
		}
		try {
			v.subargs(-1);
			fail("Failed to throw exception for index -1");
		} catch ( LuaError e ) {
			assertEquals(expected_msg, e.getMessage());
		}
	}	
}
