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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.luaj.vm2.Lua;
import org.luaj.vm2.Print;
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

	private static final byte BIT_ASSIGN     = 0x01;  // assignment is made to this slot at this pc
	private static final byte BIT_REFER      = 0x02;  // reference is made to this slot at this pc
	private static final byte BIT_UP_ASSIGN  = 0x04;  // upvalue assignment
	private static final byte BIT_UP_REFER   = 0x08;  // upvalue reference
	private static final byte BIT_UP_CREATE  = 0x10;  // upvalue storage must be created here
	private static final byte BIT_INVALID    = 0x20;  // slot becomes invlaid at this pc
	private static final byte BIT_NIL        = 0x40;  // slot initialized to nil at this point
	
	final Prototype p;
	final int n,m;
	public final byte[][] slots;
	public final boolean[] branchdest;

	public boolean isUpvalueCreate(int pc, int slot) {
		return (slots[pc+1][slot] & (BIT_UP_CREATE)) != 0;
	}
	
	public boolean isUpvalueAssign(int pc, int slot) {
		return (slots[pc+1][slot] & (BIT_UP_ASSIGN | BIT_UP_CREATE)) != 0;
	}

	public boolean isUpvalueRefer(int pc, int slot) {
		return (slots[pc+1][slot] & (BIT_UP_REFER)) != 0;
	}

	public boolean isInitialValueUsed(int slot) {
		return (slots[0][slot] & (BIT_INVALID)) == 0;
	}
	
	public Slots(Prototype prototype) {
		p = prototype;
		n = p.code.length;
		m = p.maxstacksize;
		slots = new byte[n+1][m];
		branchdest = new boolean[n+1];
		markassignments();
		while ( propogatebranches() ) 
			;
		markuninitialized();
		markupvalues();
		markforloopupvalues();
	}

	private void markassignments() {
		// mark initial assignments and references
		int j=0;
		for ( ; j<p.numparams; j++ )
			slots[0][j] = BIT_ASSIGN;
		for ( ; j<m; j++ )
			slots[0][j] = BIT_NIL;

		for ( int pc=0; pc<n; pc++ ) {
			int index = pc+1;
			byte[] s = slots[index];
			
			int ins = p.code[pc];
			int a = Lua.GETARG_A(ins);
			int b = Lua.GETARG_B(ins);
			int bx = Lua.GETARG_Bx(ins);
			int sbx = Lua.GETARG_sBx(ins);
			int c = Lua.GETARG_C(ins);

			switch ( Lua.GET_OPCODE(ins) ) {			
			case Lua.OP_GETUPVAL: /*	A B	R(A):= UpValue[B]				*/
			case Lua.OP_NEWTABLE: /*	A B C	R(A):= {} (size = B,C)				*/
			case Lua.OP_LOADK:/*	A Bx	R(A):= Kst(Bx)					*/
			case Lua.OP_GETGLOBAL: /*	A Bx	R(A):= Gbl[Kst(Bx)]				*/
				s[a] |= BIT_ASSIGN;
                break;
                
			case Lua.OP_SETUPVAL: /*	A B	UpValue[B]:= R(A)				*/
			case Lua.OP_SETGLOBAL: /*	A Bx	Gbl[Kst(Bx)]:= R(A)				*/
				s[a] |= BIT_REFER;
                break;
                
			case Lua.OP_MOVE:/*	A B	R(A):= R(B)					*/
			case Lua.OP_UNM: /*	A B	R(A):= -R(B)					*/
			case Lua.OP_NOT: /*	A B	R(A):= not R(B)				*/
			case Lua.OP_LEN: /*	A B	R(A):= length of R(B)				*/
				s[a] |= BIT_ASSIGN;
				s[b] |= BIT_REFER;
				break;
				
			case Lua.OP_LOADNIL: /*	A B	R(A):= ...:= R(B):= nil			*/
				while ( a<=b )
					s[a++] |= BIT_ASSIGN;
				break;
				
			case Lua.OP_GETTABLE: /*	A B C	R(A):= R(B)[RK(C)]				*/
				s[a] |= BIT_ASSIGN;
				s[b] |= BIT_REFER;
				if (c<=0xff) s[c] |= BIT_REFER;
				break;
				
			case Lua.OP_SETTABLE: /*	A B C	R(A)[RK(B)]:= RK(C)				*/
				s[a] |= BIT_REFER;
				if (b<=0xff) s[b] |= BIT_REFER;
				if (c<=0xff) s[c] |= BIT_REFER;
				break;
				
			case Lua.OP_ADD: /*	A B C	R(A):= RK(B) + RK(C)				*/
			case Lua.OP_SUB: /*	A B C	R(A):= RK(B) - RK(C)				*/
			case Lua.OP_MUL: /*	A B C	R(A):= RK(B) * RK(C)				*/
			case Lua.OP_DIV: /*	A B C	R(A):= RK(B) / RK(C)				*/
			case Lua.OP_MOD: /*	A B C	R(A):= RK(B) % RK(C)				*/
			case Lua.OP_POW: /*	A B C	R(A):= RK(B) ^ RK(C)				*/
				s[a] |= BIT_ASSIGN;
				if (b<=0xff) s[b] |= BIT_REFER;
				if (c<=0xff) s[c] |= BIT_REFER;
				break;
				
			case Lua.OP_SELF: /*	A B C	R(A+1):= R(B): R(A):= R(B)[RK(C)]		*/
				s[a] |= BIT_ASSIGN;
				s[a+1] |= BIT_ASSIGN;
				s[b] |= BIT_REFER;
				if (c<=0xff) s[c] |= BIT_REFER;
				break;
				
			case Lua.OP_CONCAT: /*	A B C	R(A):= R(B).. ... ..R(C)			*/
				s[a] |= BIT_ASSIGN;
				while ( b<=c )
					s[b++] |= BIT_REFER;
				break;
				             
			case Lua.OP_LOADBOOL:/*	A B C	R(A):= (Bool)B: if (C) pc++			*/
				s[a] |= BIT_ASSIGN;
				//if ( c!=0 ) branchdest[index+2] = true;
                break;
                
			case Lua.OP_JMP: /*	sBx	pc+=sBx					*/
				if ( sbx < 0 )
					branchdest[index+1+sbx] = true;
				ins = p.code[index+0+sbx]; 
				if ( Lua.GET_OPCODE(ins) == Lua.OP_TFORLOOP ) {
					a = Lua.GETARG_A(ins);
					c = Lua.GETARG_C(ins);
					for ( int i=1; i<=c; i++ )
						s[a+2+i] |= BIT_INVALID;
				}
				break;
				
			case Lua.OP_EQ: /*	A B C	if ((RK(B) == RK(C)) ~= A) then pc++		*/
			case Lua.OP_LT: /*	A B C	if ((RK(B) <  RK(C)) ~= A) then pc++  		*/
			case Lua.OP_LE: /*	A B C	if ((RK(B) <= RK(C)) ~= A) then pc++  		*/
				if (bx<=0xff) s[bx] |= BIT_REFER;
				if (c<=0xff) s[c] |= BIT_REFER;
				//branchdest[index+2] = true;
				break;

			case Lua.OP_TEST: /*	A C	if not (R(A) <=> C) then pc++			*/ 
				s[a] |= BIT_REFER;
				//branchdest[index+2] = true;
				break;
				
			case Lua.OP_TESTSET: /*	A B C	if (R(B) <=> C) then R(A):= R(B) else pc++	*/
				s[a] |= BIT_REFER;
				s[b] |= BIT_REFER;
				//branchdest[index+2] = true;
				break;
				
			case Lua.OP_CALL: /*	A B C	R(A), ... ,R(A+C-2):= R(A)(R(A+1), ... ,R(A+B-1)) */
				for ( int i=0; i<b; i++ )
					s[a+i] |= BIT_REFER;
				for ( int i=0; i<c-1; i++, a++ )
					s[a] |= BIT_ASSIGN;
				for ( ; a<m; a++ )
					s[a] |= BIT_INVALID;
				break;
				
			case Lua.OP_TAILCALL: /*	A B C	return R(A)(R(A+1), ... ,R(A+B-1))		*/
				for ( int i=1; i<b; i++, a++ )
					s[a] |= BIT_REFER;
				for ( ; a<m; a++ )
					s[a] |= BIT_INVALID;
				break;
				
			case Lua.OP_RETURN: /*	A B	return R(A), ... ,R(A+B-2)	(see note)	*/
				while ( a < b-1 )
					s[a++] |= BIT_REFER; 
				break;
				
			case Lua.OP_FORPREP: /*	A sBx	R(A)-=R(A+2): pc+=sBx				*/
				s[a] |= BIT_REFER | BIT_ASSIGN; 
				s[a+2] |= BIT_REFER; 
				branchdest[index+1+sbx] = true;
				break;
				
			case Lua.OP_FORLOOP: /*	A sBx	R(A)+=R(A+2): if R(A) <?= R(A+1) then { pc+=sBx: R(A+3)=R(A) }*/
				s[a] |= BIT_REFER | BIT_ASSIGN; 
				s[a+1] |= BIT_REFER; 
				s[a+2] |= BIT_REFER; 
				s[a+3] |= BIT_ASSIGN; 
				branchdest[index+1+sbx] = true;
				break;
			
			case Lua.OP_TFORLOOP: /*
								 * A C R(A+3), ... ,R(A+2+C):= R(A)(R(A+1),
								 * R(A+2)): if R(A+3) ~= nil then R(A+2)=R(A+3)
								 * else pc++
								 */
				s[a] |= BIT_REFER; 
				s[a+1] |= BIT_REFER; 
				s[a+2] |= BIT_REFER | BIT_ASSIGN; 
				for ( int aa=a+3; aa<a+3+c; aa++ )
					s[aa] |= BIT_ASSIGN;
				for ( int aa=a+3+c; aa<m; aa++ )
					s[aa] |= BIT_INVALID;
				branchdest[index+2] = true;
				break;
				
			case Lua.OP_SETLIST: /*	A B C	R(A)[(C-1)*FPF+i]:= R(A+i), 1 <= i <= B	*/
				s[a] |= BIT_REFER;
				for ( int i=1; i<=b; i++ )
					s[a+i] |= BIT_REFER;
				break;
				
			case Lua.OP_CLOSE: /*	A 	close all variables in the stack up to (>=) R(A)*/
				while ( a<m )
					s[a++] |= BIT_INVALID;
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
						s[b] |= BIT_REFER | BIT_UP_REFER;
					}
				}
				s[a] |= ((s[a] & BIT_UP_REFER) != 0 )?
						(BIT_ASSIGN | BIT_UP_ASSIGN):
						BIT_ASSIGN;
				break;
			}				
			case Lua.OP_VARARG: /*	A B	R(A), R(A+1), ..., R(A+B-1) = vararg		*/
				if ( b == 0 ) {
					while ( a<m )
						s[a++] |= BIT_INVALID;
				} else {
					for ( int i=1; i<b; ++a, ++i )
						s[a] |= BIT_ASSIGN;
				}
				break;				
			}
		}
	}
		
	private boolean propogatebranches() {
		boolean hadchanges = false;
		for ( int pc=0; pc<n; pc++ ) {
			int index = pc+1;
			int ins = p.code[pc];
			switch ( Lua.GET_OPCODE(ins) ) {			
				case Lua.OP_LOADBOOL:
				case Lua.OP_EQ:
				case Lua.OP_LT:
				case Lua.OP_LE:
				case Lua.OP_TEST: 
				case Lua.OP_TESTSET:
				case Lua.OP_TFORLOOP: 
					hadchanges |= propogatebranch( index, index+2 );
					break;
				case Lua.OP_JMP:
				case Lua.OP_FORPREP:
				case Lua.OP_FORLOOP:
					hadchanges |= propogatebranch( index, index+1+Lua.GETARG_sBx(ins) );
					break;
			} 
		}
		return hadchanges;
	}

	private boolean propogatebranch(int src, int dest) {
		boolean hadchanges = false;
		byte[] s = slots[src];
		byte[] d = slots[dest];
		for ( int j=0; j<m; j++ ) {
			byte bits = (byte) (s[j] & (BIT_ASSIGN | BIT_INVALID));
			hadchanges |= ((d[j] & bits) & (BIT_ASSIGN | BIT_INVALID)) != bits;
			d[j] |= bits;
		}
		return hadchanges;
	}

	private void markuninitialized() {
		for ( int j=p.numparams; j<m; j++ )
			if ( ! isreferrededtofirst(j) )
				slots[0][j] |= BIT_INVALID;
	}
	
	private boolean isreferrededtofirst(int j) {
		for ( int i=1; i<=n; i++ ) {
			if ( (slots[i][j] & (BIT_REFER | BIT_UP_REFER)) != 0 )
				return true;
			if ( (slots[i][j] & (BIT_ASSIGN | BIT_UP_ASSIGN | BIT_UP_CREATE | BIT_INVALID)) != 0 )
				return false;
		}
		return false;
	}

	private void markupvalues( ) {
		for ( int pc=0; pc<n; ++pc ) {
			if ( Lua.GET_OPCODE(p.code[pc]) == Lua.OP_CLOSURE ) {
				int index = pc+1;
				byte[] s = slots[index];
				for ( int j=0; j<m; j++ )
					if ( (s[j] & BIT_REFER) != 0 ) {
						promoteUpvalueBefore( index, j );
						if ( pc<n-1 ) 
							promoteUpvalueAfter( index+1, j );
					}
			}
		}
	}
	
	private void markforloopupvalues( ) {
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
						for ( int j=1; j<=c; j++ ) {
							checkPromoteLoopUpvalue( pc0+1, pc1+1, a+2+j );
						}
					}
				}
			}
		}
	}
	
	private void checkPromoteLoopUpvalue(int index0, int index1, int slot) {
		for ( int index=index0; index<=index1; ++index ) {
			if ( (slots[index][slot] & BIT_UP_CREATE) != 0 ) {
				for ( int i=index0+1; i<index1; ++i ) {
					promoteUpvalue(slots[i], slot);
					slots[i][slot] &= (~BIT_UP_CREATE);
				}
				slots[index1][slot] |= BIT_UP_CREATE;
				return;
			}
		}
	}

	private void promoteUpvalueBefore(int index, int j) {
		int undef  = prevUndefined(index,j);
//		int branch = firstBranchAfter(undef,index,j);
//		int assign = lastAssignBefore(branch,undef,j);
		int assign = firstAssignAfter(undef,index,j);
		slots[assign][j] |= BIT_UP_CREATE;
		while ( index>assign)
			promoteUpvalue( slots[index--], j );
	}

	private void promoteUpvalueAfter(int index, int j) {
		int end = nextUndefined(index,j);
		int access = lastAccessBefore(end,index,j);
		while ( index<=access )
			promoteUpvalue( slots[index++], j );
	}

	private void promoteUpvalue(byte[] s, int slot) {
		if ( (s[slot] & BIT_REFER) != 0 )
			s[slot] |= BIT_UP_REFER;
		if ( (s[slot] & BIT_ASSIGN) != 0 )
			s[slot] |= BIT_UP_ASSIGN;
	}

	private int prevUndefined(int index, int j) {
		for ( ; index>=0; --index )
			if ( ((slots[index][j] & BIT_INVALID) != 0) )
				return index;
		return index;
	}

	private int firstBranchAfter(int index, int limit, int j) {
		for ( ; ++index<limit; )
			if ( index>0 && this.branchdest[index-1] )
				return index;
		return index;
	}

	private int lastAssignBefore(int index, int limit, int j) {
		for ( int i=index; i>limit; --i )
			if ( (slots[i][j] & (BIT_ASSIGN | BIT_NIL)) != 0 )
				return i;
		return index;
	}

	private int firstAssignAfter(int index, int limit, int j) {
		for ( int i=index; ++i<limit; )
			if ( (slots[i][j] & (BIT_ASSIGN | BIT_NIL)) != 0 )
				return i;
		return limit;
	}

	private int nextUndefined(int index, int j) {
		while ( index<slots.length && ((slots[index][j] & BIT_INVALID) == 0) )
			++index;
		return index;
	}

	private int lastAccessBefore(int index, int limit, int j) {
		for ( --index; index>limit; --index )
			if ( (slots[index][j] & (BIT_ASSIGN|BIT_REFER)) != 0 )
				return index;
		return index;
	}

	// ------------- pretty-print slot info --------------
	
	String[] toStrings() {
		int n = slots.length;
		int m = slots[0].length;
		String[] strs = new String[n];
		byte[] b = new byte[m+1];
		for ( int i=0; i<n; i++ ) {
			for ( int j=0; j<=m; j++ )
				b[j] = ' ';
			if ( branchdest[i] )
				b[0] = 'D';
			byte[] si = slots[i];
			for ( int j=0; j<m; j++ ) {
				byte s = si[j];
				b[1+j] = (byte) (
					((s & BIT_UP_CREATE) != 0)? 'C':
					((s & BIT_UP_ASSIGN) != 0)? 
							(((s & BIT_UP_REFER) != 0)? 'B': 'A'):
					((s & BIT_UP_REFER) != 0)? 'R':
					((s & BIT_ASSIGN) != 0)? 
						(((s & BIT_REFER) != 0)? 'b': 'a'):
					((s & BIT_REFER) != 0)? 'r':
					((s & BIT_INVALID) != 0)? 'x':
					((s & BIT_NIL) != 0)? 'n': ' ' );
			}
			strs[i] = new String(b);
		}
		return strs;
	}
	
	public String toString() {
		String[] s = toStrings();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream( baos );
		for ( int i=0; i<s.length; i++ ) {
			if ( i>0 ) ps.append( '\n' );
			ps.append( s[i] );
			if ( p != null && i>0 && i<=p.code.length )
				Print.printOpCode(ps, p, i-1);
		}
		ps.close();
		return baos.toString();
	}
}
