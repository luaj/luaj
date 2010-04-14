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
package org.luaj.vm2.luajc;

import org.luaj.vm2.Lua;
import org.luaj.vm2.Prototype;

/** 
 * Analyze slot usage to find:
 *   - which assignments and references are to upvalue 'u'
 *   - which slots must be initialized with the implied "nil"
 *   - which assignment locations need to create upvalue storage 'U'
 *   
 * Eventually add:
 *   - subexpression sequences that can remain in primitive types
 *   - assignments of constant values to upvalues that are never modified
 */
public class Slots {

	private static final byte ASSIGN            = 'a'; // assignment to a slot position
	private static final byte REFER             = 'r'; // reference to a slot position
	private static final byte REFER_ASSIGN      = 'b'; // i.e. "both"
	private static final byte UPVAL_CREATE      = 'U'; // where upvalue must be alloced
	private static final byte UPVAL_USE         = 'u'; // continuation of existing upvalue
	private static final byte UPVAL_USE_ASSIGN  = 'c'; // on create closure only
	private static final byte UPVAL_USE_CREATE  = 'C'; // on create closure only, create new upvalue
	private static final byte INVALID           = 'x'; // after call, etc
	private static final byte INITIAL_NIL       = 'n'; // above parameters at initial call
	
	final int n,m;
	public final byte[][] slots;
	public final boolean[] branchdest;

	public boolean isUpvalueCreate(int pc, int slot) {
		switch (slots[pc+1][slot]) {
		case UPVAL_CREATE:
		case UPVAL_USE_CREATE:
			return true;
		}
		return false;
	}
	
	public boolean isUpvalueAssign(int pc, int slot) {
		switch (slots[pc+1][slot]) {
		case UPVAL_CREATE:
		case UPVAL_USE_CREATE:
		case UPVAL_USE_ASSIGN:
			return true;
		}
		return false;
	}
	
	public boolean isUpvalueRefer(int pc, int slot) {
		switch (slots[pc+1][slot]) {
		case UPVAL_USE:
		case UPVAL_USE_ASSIGN:
			return true;
		}
		return false;
	}

	public boolean isInitialValueUsed(int slot) {
		return slots[0][slot] != INVALID;
	}
	
	public Slots(Prototype p) {
		n = p.code.length;
		m = p.maxstacksize;
		slots = new byte[n+1][m];
		branchdest = new boolean[n+1];
		markassignments( p );
		markuninitialized( p );
		markupvalues( p );
		markforloopupvalues( p );
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for ( int i=0; i<slots.length; i++ ) {
			if ( i > 0 ) sb.append( "\n" );
			byte[] s = slots[i];
			for ( int j=s.length; --j>=0; ) {
				if ( s[j] == 0 )
					s[j] = ' ';
			}
			sb.append( i>0 && branchdest[i]? "D": " " );
			sb.append( new String(s) );
		}
		return sb.toString();
	}
	
	private void markassignments( Prototype p ) {
		// mark initial assignments and references
		int j=0;
		for ( ; j<p.numparams; j++ )
			slots[0][j] = ASSIGN;
		for ( ; j<m; j++ )
			slots[0][j] = INITIAL_NIL;

		for ( int index=1; index<=n; index++ ) {
			byte[] s = slots[index];
			
			int pc = index-1;
			int ins = p.code[pc];
			int a = Lua.GETARG_A(ins);
			int b = Lua.GETARG_B(ins);
			int bx = Lua.GETARG_Bx(ins);
			int sbx = Lua.GETARG_sBx(ins);
			int c = Lua.GETARG_C(ins);

			switch ( Lua.GET_OPCODE(ins) ) {			
			case Lua.OP_GETUPVAL: /*	A B	R(A):= UpValue[B]				*/
			case Lua.OP_SETUPVAL: /*	A B	UpValue[B]:= R(A)				*/
			case Lua.OP_NEWTABLE: /*	A B C	R(A):= {} (size = B,C)				*/
				s[a] = ASSIGN;
                break;
                
			case Lua.OP_MOVE:/*	A B	R(A):= R(B)					*/
			case Lua.OP_UNM: /*	A B	R(A):= -R(B)					*/
			case Lua.OP_NOT: /*	A B	R(A):= not R(B)				*/
			case Lua.OP_LEN: /*	A B	R(A):= length of R(B)				*/
				s[a] = ASSIGN;
				s[b] = REFER;
				break;
				
			case Lua.OP_LOADK:/*	A Bx	R(A):= Kst(Bx)					*/
			case Lua.OP_GETGLOBAL: /*	A Bx	R(A):= Gbl[Kst(Bx)]				*/
			case Lua.OP_SETGLOBAL: /*	A Bx	Gbl[Kst(Bx)]:= R(A)				*/
				s[a] = ASSIGN;
				break;

			case Lua.OP_LOADNIL: /*	A B	R(A):= ...:= R(B):= nil			*/
				while ( a<b )
					s[a++] = ASSIGN;
				break;
				
			case Lua.OP_GETTABLE: /*	A B C	R(A):= R(B)[RK(C)]				*/
				s[a] = ASSIGN;
				s[b] = REFER;
				if (c<=0xff) s[c] = REFER;
				break;
				
			case Lua.OP_SETTABLE: /*	A B C	R(A)[RK(B)]:= RK(C)				*/
			case Lua.OP_ADD: /*	A B C	R(A):= RK(B) + RK(C)				*/
			case Lua.OP_SUB: /*	A B C	R(A):= RK(B) - RK(C)				*/
			case Lua.OP_MUL: /*	A B C	R(A):= RK(B) * RK(C)				*/
			case Lua.OP_DIV: /*	A B C	R(A):= RK(B) / RK(C)				*/
			case Lua.OP_MOD: /*	A B C	R(A):= RK(B) % RK(C)				*/
			case Lua.OP_POW: /*	A B C	R(A):= RK(B) ^ RK(C)				*/
				s[a] = ASSIGN;
				if (bx<=0xff) s[bx] = REFER;
				if (c<=0xff) s[c] = REFER;
				break;
				
			case Lua.OP_SELF: /*	A B C	R(A+1):= R(B): R(A):= R(B)[RK(C)]		*/
				s[a] = ASSIGN;
				s[a+1] = ASSIGN;
				s[b] = REFER;
				if (c<=0xff) s[c] = REFER;
				break;
				
			case Lua.OP_CONCAT: /*	A B C	R(A):= R(B).. ... ..R(C)			*/
				s[a] = ASSIGN;
				while ( b<=c )
					s[b++] = REFER;
				break;
				             
			case Lua.OP_LOADBOOL:/*	A B C	R(A):= (Bool)B: if (C) pc++			*/
				s[a] = ASSIGN;
				if ( c!=0 ) branchdest[index+2] = true;
                break;
                
			case Lua.OP_JMP: /*	sBx	pc+=sBx					*/
				branchdest[index+1+sbx] = true;
				break;
				
			case Lua.OP_EQ: /*	A B C	if ((RK(B) == RK(C)) ~= A) then pc++		*/
			case Lua.OP_LT: /*	A B C	if ((RK(B) <  RK(C)) ~= A) then pc++  		*/
			case Lua.OP_LE: /*	A B C	if ((RK(B) <= RK(C)) ~= A) then pc++  		*/
				if (bx<=0xff) s[bx] = REFER;
				if (c<=0xff) s[c] = REFER;
				branchdest[index+2] = true;
				break;

			case Lua.OP_TEST: /*	A C	if not (R(A) <=> C) then pc++			*/ 
				s[a] = REFER;
				branchdest[index+2] = true;
				break;
				
			case Lua.OP_TESTSET: /*	A B C	if (R(B) <=> C) then R(A):= R(B) else pc++	*/
				s[a] = REFER;
				s[b] = REFER;
				branchdest[index+2] = true;
				break;
				
			case Lua.OP_CALL: /*	A B C	R(A), ... ,R(A+C-2):= R(A)(R(A+1), ... ,R(A+B-1)) */
				while ( a < c-1 || a < b )
					s[a++] = (byte) (a<c-1 && a<b? REFER_ASSIGN: a<c-1? ASSIGN: REFER);
				while ( a < m )
					s[a++] = INVALID;
				break;
				
			case Lua.OP_TAILCALL: /*	A B C	return R(A)(R(A+1), ... ,R(A+B-1))		*/
				while ( a < b )
					s[a++] = REFER;
				while ( a < m )
					s[a++] = INVALID;
				break;
				
			case Lua.OP_RETURN: /*	A B	return R(A), ... ,R(A+B-2)	(see note)	*/
				while ( a < b-1 )
					s[a++] = REFER; 
				break;
				
			case Lua.OP_FORPREP: /*	A sBx	R(A)-=R(A+2): pc+=sBx				*/
				s[a] = REFER_ASSIGN; 
				s[a+2] = REFER; 
				branchdest[index+1+sbx] = true;
				break;
				
			case Lua.OP_FORLOOP: /*	A sBx	R(A)+=R(A+2): if R(A) <?= R(A+1) then { pc+=sBx: R(A+3)=R(A) }*/
				s[a] = REFER_ASSIGN; 
				s[a+1] = REFER; 
				s[a+2] = REFER; 
				s[a+3] = ASSIGN; 
				branchdest[index+1+sbx] = true;
				break;
			
			case Lua.OP_TFORLOOP: /*
								 * A C R(A+3), ... ,R(A+2+C):= R(A)(R(A+1),
								 * R(A+2)): if R(A+3) ~= nil then R(A+2)=R(A+3)
								 * else pc++
								 */
				s[a] = REFER; 
				s[a+1] = REFER; 
				s[a+2] = REFER_ASSIGN; 
				for ( int aa=a+3; aa<a+3+c; aa++ )
					s[aa] = ASSIGN;
				for ( int aa=a+3+c; aa<m; aa++ )
					s[aa] = INVALID;
				branchdest[index+2] = true;
				break;
				
			case Lua.OP_SETLIST: /*	A B C	R(A)[(C-1)*FPF+i]:= R(A+i), 1 <= i <= B	*/
				s[a] = REFER;
				for ( int aa=1; aa<=b; aa++ )
					s[aa] = REFER;
				break;
				
			case Lua.OP_CLOSE: /*	A 	close all variables in the stack up to (>=) R(A)*/
				while ( a<m )
					s[a++] = INVALID;
				break;
				
			case Lua.OP_CLOSURE: /*	A Bx	R(A):= closure(KPROTO[Bx], R(A), ... ,R(A+n))	*/
			{
				Prototype newp = p.p[bx];
				for ( int up=0, nup=newp.nups; up<nup; ++up ) {
					ins = p.code[++pc];
					b = Lua.GETARG_B(ins);
					if ( (ins&4) != 0 ) {
						// up : ups[b]
					} else {
						s[b] = UPVAL_USE;
					}
				}
				s[a] = (byte) (s[a] == UPVAL_USE? UPVAL_USE_ASSIGN: ASSIGN);
				break;
			}				
			case Lua.OP_VARARG: /*	A B	R(A), R(A+1), ..., R(A+B-1) = vararg		*/
				while ( a<b )
					s[a++] = ASSIGN;
				break;				
			}
		}
	}
	
	private void markuninitialized(Prototype p) {
		for ( int j=p.numparams; j<m; j++ )
			if ( ! isreferrededtofirst(j) )
				slots[0][j] = INVALID;
	}
	
	private boolean isreferrededtofirst(int j) {
		for ( int i=1; i<=n; i++ ) {
			switch (slots[i][j]) {
			case REFER_ASSIGN:
			case REFER:
			case UPVAL_USE:
				return true;
			case ASSIGN:
			case INVALID:
				return false;
			}
		}
		return false;
	}

	private void markupvalues( Prototype p ) {
		for ( int pc=0; pc<n; ++pc ) {
			if ( Lua.GET_OPCODE(p.code[pc]) == Lua.OP_CLOSURE ) {
				int index = pc+1;
				byte[] s = slots[index];
				for ( int j=0; j<m; j++ )
					if ( s[j] == UPVAL_USE || s[j] == UPVAL_USE_ASSIGN ) {
						promoteUpvalueBefore( s, index, j );
						if ( pc<n-1 ) 
							promoteUpvalueAfter( s, index+1, j );
					}
			}
		}
	}
	
	private void markforloopupvalues( Prototype p ) {
		for ( int pc1=n; --pc1>=0; ) {
			int i = p.code[pc1];
			if ( Lua.GET_OPCODE(i) == Lua.OP_TFORLOOP ) {
				int a = Lua.GETARG_A(i);
				int c = Lua.GETARG_C(i);
				for ( int pc0=pc1; --pc0>=0; ) {
					i = p.code[pc0];
					int o = Lua.GET_OPCODE(i);
					int sbx = Lua.GETARG_sBx(i);
					if ( o == Lua.OP_JMP && (pc0 + 1 + sbx == pc1) ) {
						for ( int j=0; j<c; j++ ) {
							checkPromoteLoopUpvalue( pc0, pc1, a+3+j );
						}
					}
				}
			}
		}
		
	}
	
	private void checkPromoteLoopUpvalue(int pc0, int pc1, int slot) {
		for ( int index=pc0+1; index<=pc1; ++index ) {
			switch (slots[index][slot]) {
			case UPVAL_CREATE:
			case UPVAL_USE_CREATE:
			case UPVAL_USE_ASSIGN:
			case UPVAL_USE:
				int i = pc0;
				slots[++i][slot] = UPVAL_CREATE;
				while ( ++i<=pc1 )
					slots[i][slot] = UPVAL_USE;
				return;
			}
		}
	}

	private void promoteUpvalueBefore(byte[] s, int index, int j) {
		int begin  = prevUndefined(index,j);
		int assign = firstAssignAfter(begin,index,j);
		slots[assign][j] = slots[assign][j]==UPVAL_USE_ASSIGN? UPVAL_USE_CREATE: UPVAL_CREATE;
		while ( index>assign)
			slots[index--][j] = UPVAL_USE;
	}

	private void promoteUpvalueAfter(byte[] s, int index, int j) {
		int end = nextUndefined(index,j);
		int access = lastAccessBefore(end,index,j);
		while ( index<=access )
			slots[index++][j] = UPVAL_USE;
	}

	private int prevUndefined(int index, int j) {
		while ( index>0 && slots[index][j] != INVALID )
			--index;
		return index;
	}

	private int firstAssignAfter(int index, int limit, int j) {
		for ( ; index<limit; ++index ) {
			switch (slots[index][j]) {
			case ASSIGN:
			case REFER_ASSIGN:
				return index;
			case UPVAL_CREATE:
				throw new IllegalStateException("overlapping upvalues");
			}
		}
		return index;
	}

	private int nextUndefined(int index, int j) {
		while ( index+1<slots.length && slots[index+1][j] != INVALID )
			++index;
		return index;
	}

	private int lastAccessBefore(int index, int limit, int j) {
		for ( ; index>limit; --index ) {
			switch (slots[index][j]) {
			case ASSIGN:
			case REFER_ASSIGN:
			case REFER:
				return index;
			case UPVAL_CREATE:
			case UPVAL_USE:
				throw new IllegalStateException("overlapping upvalues");
			}
		}
		return index;
	}

}
