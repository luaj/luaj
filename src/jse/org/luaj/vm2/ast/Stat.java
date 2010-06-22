/*******************************************************************************
* Copyright (c) 2010 Luaj.org. All rights reserved.
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
package org.luaj.vm2.ast;

import java.util.List;

public class Stat {

	public static Stat block(Block b) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat whiledo(Exp e, Block b) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat repeatuntil(Block b, Exp e) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat breakstat() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat returnstat(List<Exp> el) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat assignment(Assign as) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat functioncall(PrimaryExp pe) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat localfunctiondef(String image, FuncBody fb) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat fornumeric(String image, Exp e, Exp e2, Exp e3, Block b) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat functiondef(FuncName fn, FuncBody fb) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat forgeneric(List<Name> nl, List<Exp> el, Block b) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat localassignment(List<Name> nl, List<Exp> el) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Stat ifthenelse(Exp e, Block b, List<Exp> el, List<Block> bl,
			Block b3) {
		// TODO Auto-generated method stub
		return null;
	}

}
