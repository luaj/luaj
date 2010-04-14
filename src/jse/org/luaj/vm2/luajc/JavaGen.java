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
 * TODO: 
 *   propogate constants
 *   loader can find inner classes
 */
public class JavaGen {

	public String classname;
	public byte[] bytecode;
	public JavaGen[] inners;
	
	public JavaGen( Prototype p, String classname, String filename ) {
		this.classname = classname;
		
		// build this class
		JavaBuilder builder = new JavaBuilder(p, classname, filename);
		scanInstructions(p, classname, builder);
		this.bytecode = builder.completeClass();
		
		// build sub-prototypes
		int n = p.p.length;
		inners = new JavaGen[n];
		for ( int i=0; i<n; i++ )
			inners[i] = new JavaGen(p.p[i], closureName(classname,i), filename);
	}

	private String closureName(String classname, int subprotoindex) {
		return classname+"$"+subprotoindex;
	}
	
	private void scanInstructions(Prototype p, String classname, JavaBuilder builder) {
		int vresultbase = -1;
		
		for ( int pc=0, n=p.code.length; pc<n; pc++ ) {
			int ins = p.code[pc];
			int o = Lua.GET_OPCODE(ins);
			int a = Lua.GETARG_A(ins);
			int b = Lua.GETARG_B(ins);
			int bx = Lua.GETARG_Bx(ins);
			int sbx = Lua.GETARG_sBx(ins);
			int c = Lua.GETARG_C(ins);

			switch ( o ) {	
			case Lua.OP_GETUPVAL: /*	A B	R(A):= UpValue[B]				*/
				builder.loadUpvalue( b );
				builder.storeLocal( pc, a );
                break;
                
			case Lua.OP_SETUPVAL: /*	A B	UpValue[B]:= R(A)				*/
				builder.storeUpvalue( pc, b, a );
                break;
                
			case Lua.OP_NEWTABLE: /*	A B C	R(A):= {} (size = B,C)				*/
				builder.newTable( b, c );
				builder.storeLocal( pc, a );
                break;
                
			case Lua.OP_MOVE:/*	A B	R(A):= R(B)					*/
				builder.loadLocal( pc, b );
				builder.storeLocal( pc, a );
				break;
				
			case Lua.OP_UNM: /*	A B	R(A):= -R(B)					*/
			case Lua.OP_NOT: /*	A B	R(A):= not R(B)				*/
			case Lua.OP_LEN: /*	A B	R(A):= length of R(B)				*/
				builder.loadLocal( pc, b );
				builder.unaryop( o );
				builder.storeLocal( pc, a );
				break;
				
			case Lua.OP_LOADK:/*	A Bx	R(A):= Kst(Bx)					*/
				builder.loadConstant( p.k[bx] );
				builder.storeLocal( pc, a );
				break;
				
			case Lua.OP_GETGLOBAL: /*	A Bx	R(A):= Gbl[Kst(Bx)]				*/
				builder.loadEnv();
				builder.loadConstant( p.k[bx] );
				builder.getTable();
				builder.storeLocal( pc, a );
				break;
				
			case Lua.OP_SETGLOBAL: /*	A Bx	Gbl[Kst(Bx)]:= R(A)				*/
				builder.loadEnv();
				builder.loadConstant( p.k[bx] );
				builder.loadLocal( pc, a );
				builder.setTable();
				break;

			case Lua.OP_LOADNIL: /*	A B	R(A):= ...:= R(B):= nil			*/
				builder.loadNil();
				for ( ; a<=b; a++ ) {
					if ( a < b ) 
						builder.dup();
					builder.storeLocal( pc, a );
				}
				break;
				
			case Lua.OP_GETTABLE: /*	A B C	R(A):= R(B)[RK(C)]				*/
				builder.loadLocal( pc, b );
				loadLocalOrConstant( p, builder, pc, c );
				builder.getTable();
				builder.storeLocal( pc, a );
				break;
				
			case Lua.OP_SETTABLE: /*	A B C	R(A)[RK(B)]:= RK(C)				*/
				builder.loadLocal( pc, a );
				loadLocalOrConstant( p, builder, pc, b );
				loadLocalOrConstant( p, builder, pc, c );
				builder.setTable();
				break;
				
			case Lua.OP_ADD: /*	A B C	R(A):= RK(B) + RK(C)				*/
			case Lua.OP_SUB: /*	A B C	R(A):= RK(B) - RK(C)				*/
			case Lua.OP_MUL: /*	A B C	R(A):= RK(B) * RK(C)				*/
			case Lua.OP_DIV: /*	A B C	R(A):= RK(B) / RK(C)				*/
			case Lua.OP_MOD: /*	A B C	R(A):= RK(B) % RK(C)				*/
			case Lua.OP_POW: /*	A B C	R(A):= RK(B) ^ RK(C)				*/
				loadLocalOrConstant( p, builder, pc, b );
				loadLocalOrConstant( p, builder, pc, c );
				builder.binaryop( o );
				builder.storeLocal( pc, a );
				break;
				
			case Lua.OP_SELF: /*	A B C	R(A+1):= R(B): R(A):= R(B)[RK(C)]		*/
				builder.loadLocal(pc,b);
				builder.dup();
				builder.storeLocal(pc, a+1);
				loadLocalOrConstant( p, builder, pc, c );
				builder.getTable();
				builder.storeLocal(pc, a);
				break;
				
			case Lua.OP_CONCAT: /*	A B C	R(A):= R(B).. ... ..R(C)			*/
				builder.newBuffer();
				while ( b<=c ) {
					builder.dup();
					builder.loadLocal(pc, b++);
					builder.appendBuffer();
				}
				builder.tostring();
				builder.storeLocal(pc, a);
				break;
				
			case Lua.OP_LOADBOOL:/*	A B C	R(A):= (Bool)B: if (C) pc++			*/
				builder.loadBoolean( b!=0 );
				builder.storeLocal( pc, a );
				//if ( c != 0 ) branchdest[index+2] = true;
				break;
				
			case Lua.OP_JMP: /*	sBx	pc+=sBx					*/
			{
				int pc1 = pc+1+sbx;
				ins = p.code[pc1];
				if ( Lua.GET_OPCODE(ins) == Lua.OP_TFORLOOP )
					builder.createUpvalues(pc, Lua.GETARG_A(ins)+3, Lua.GETARG_C(ins));
				builder.addBranch(pc, JavaBuilder.BRANCH_GOTO, pc+1+sbx);
				break;
			}
				
			case Lua.OP_EQ: /*	A B C	if ((RK(B) == RK(C)) ~= A) then pc++		*/
			case Lua.OP_LT: /*	A B C	if ((RK(B) <  RK(C)) ~= A) then pc++  		*/
			case Lua.OP_LE: /*	A B C	if ((RK(B) <= RK(C)) ~= A) then pc++  		*/
				loadLocalOrConstant( p, builder, pc, b );
				loadLocalOrConstant( p, builder, pc, c );
				builder.compareop(o);
				builder.addBranch(pc, (a!=0? JavaBuilder.BRANCH_IFEQ: JavaBuilder.BRANCH_IFNE), pc+2);
				break;

			case Lua.OP_TEST: /*	A C	if not (R(A) <=> C) then pc++			*/ 
				builder.loadLocal( pc, a );
				builder.toBoolean();
				builder.addBranch(pc, (c!=0? JavaBuilder.BRANCH_IFEQ: JavaBuilder.BRANCH_IFNE), pc+2);
				break;
				
			case Lua.OP_TESTSET: /*	A B C	if (R(B) <=> C) then R(A):= R(B) else pc++	*/
				builder.loadLocal( pc, b );
				builder.toBoolean();
				builder.addBranch(pc, (c!=0? JavaBuilder.BRANCH_IFEQ: JavaBuilder.BRANCH_IFNE), pc+2);
				builder.loadLocal( pc, b );
				builder.storeLocal( pc, a );
				break;
				
			case Lua.OP_CALL: /*	A B C	R(A), ... ,R(A+C-2):= R(A)(R(A+1), ... ,R(A+B-1)) */

				// load function
				builder.loadLocal(pc, a);
				
				// load args
				int narg = b - 1;
				switch ( narg ) {
				case 0: case 1: case 2: case 3:
					for ( int i=1; i<b; i++ )
						builder.loadLocal(pc, a+i);
					break;
				default: // fixed arg count > 3
					builder.newVarargs( pc, a+1, b-1 );
					narg = -1;
					break;
				case -1: // prev vararg result
					loadVarargResults( builder, pc, a+1, vresultbase );
					narg = -1;
					break;
				}
				
				// call or invoke
				boolean useinvoke = narg<0 || c<1 || c>2;
				if ( useinvoke )
					builder.invoke(narg);
				else
					builder.call(narg);
				
				// handle results
				switch ( c ) {
				case 1: 
					builder.pop(); 
					break;
				case 2:
					if ( useinvoke ) 
						builder.arg( 1 );
					builder.storeLocal(pc, a);
					break;
				default: // fixed result count - unpack args
					for ( int i=1; i<c; i++ ) {
						if ( i+1 < c )
							builder.dup();
						builder.arg( i );
						builder.storeLocal(pc, a+i-1);
					}
					break;
				case 0: // vararg result
					vresultbase = a;
					builder.storeVarresult();
					break;
				}
				break;
				
			case Lua.OP_TAILCALL: /*	A B C	return R(A)(R(A+1), ... ,R(A+B-1))		*/
				
				// load function
				builder.loadLocal(pc, a);
				
				// load args
				switch ( b ) {
				case 1: 
					builder.loadNone();
					break;
				case 2: 
					builder.loadLocal(pc, a+1);
					break;
				default: // fixed arg count > 1
					builder.newVarargs( pc, a+1, b-1 );
					break;
				case 0: // prev vararg result
					loadVarargResults( builder, pc, a+1, vresultbase );
					break;
				}
				builder.newTailcallVarargs();
				builder.areturn();
				break;
				
			case Lua.OP_RETURN: /*	A B	return R(A), ... ,R(A+B-2)	(see note)	*/
				if ( c == 1 ) {
					builder.loadNone();
				} else {
					switch ( b ) {
					case 0: loadVarargResults( builder, pc, a, vresultbase ); break;
					case 1: builder.loadNone(); break;
					case 2: builder.loadLocal(pc, a); break;
					default: builder.newVarargs(pc, a, b-1); break;
					}
				}
				builder.areturn(); 
				break;
				
			case Lua.OP_FORPREP: /*	A sBx	R(A)-=R(A+2): pc+=sBx				*/
				builder.loadLocal(pc, a);
				builder.loadLocal(pc, a+2);
				builder.binaryop( Lua.OP_SUB );
				builder.storeLocal(pc, a);
				builder.addBranch(pc, JavaBuilder.BRANCH_GOTO, pc+1+sbx);
				break;
				
			case Lua.OP_FORLOOP: /*	A sBx	R(A)+=R(A+2): if R(A) <?= R(A+1) then { pc+=sBx: R(A+3)=R(A) }*/
				builder.loadLocal(pc, a);
				builder.loadLocal(pc, a+2);
				builder.binaryop( Lua.OP_ADD );
				builder.dup();
				builder.dup();
				builder.storeLocal(pc, a);
				builder.storeLocal(pc, a+3);
				builder.loadLocal(pc, a+1); // limit
				builder.loadLocal(pc, a+2); // step
				builder.testForLoop();
				builder.addBranch(pc, JavaBuilder.BRANCH_IFNE, pc+1+sbx);
				break;
			
			case Lua.OP_TFORLOOP: /*
								 * A C R(A+3), ... ,R(A+2+C):= R(A)(R(A+1),
								 * R(A+2)): if R(A+3) ~= nil then R(A+2)=R(A+3)
								 * else pc++
								 */
				builder.loadLocal(pc, a);
				builder.loadLocal(pc, a+1);
				builder.loadLocal(pc, a+2);
				builder.invoke(2);
				for ( int i=0; i<c; i++ ) {
					if ( i+1 < c )
						builder.dup();
					builder.arg( i+1 );
					builder.storeLocal(pc, a+3+i);
				}
				
				builder.loadLocal(pc, a+3);
				builder.isNil();
				builder.addBranch(pc, JavaBuilder.BRANCH_IFNE, pc+2);
				builder.loadLocal(pc, a+3);
				builder.storeLocal(pc, a+2);
				break;
				
			case Lua.OP_SETLIST: /*	A B C	R(A)[(C-1)*FPF+i]:= R(A+i), 1 <= i <= B	*/
				int index0 = (c-1)*Lua.LFIELDS_PER_FLUSH + 1;
				builder.loadLocal( pc, a );
				if ( b == 0 ) {
					int nstack = vresultbase - (a+1);
					if ( nstack > 0 ) {
						builder.setlistStack( pc, a+1, index0, nstack );
						index0 += nstack;
					}
					builder.setlistVarargs( index0, vresultbase );
				} else {
					builder.setlistStack( pc, a+1, index0, b );
					builder.pop();
				}
				break;
				
			case Lua.OP_CLOSE: /*	A 	close all variables in the stack up to (>=) R(A)*/
				break;
				
			case Lua.OP_CLOSURE: /*	A Bx	R(A):= closure(KPROTO[Bx], R(A), ... ,R(A+n))	*/
			{
				Prototype newp = p.p[bx];
				String protoname = closureName(classname, bx);
				int nup = newp.nups;
				builder.closureCreate( protoname );
				if ( nup > 0 )
					builder.dup();
				builder.storeLocal( pc, a );
				if ( nup > 0 ) {
					for ( int up=0; up<nup; ++up ) {
						if ( up+1 < nup )
							builder.dup();
						ins = p.code[++pc];
						b = Lua.GETARG_B(ins);
						if ( (ins&4) != 0 ) {
							builder.closureInitUpvalueFromUpvalue( protoname, up, b );
						} else {
							builder.closureInitUpvalueFromLocal( protoname, up, pc, b );
						}
					}
				}
				break;
			}				
			case Lua.OP_VARARG: /*	A B	R(A), R(A+1), ..., R(A+B-1) = vararg		*/
				if ( b == 0 ) {
					builder.loadVarargs();
					builder.storeVarresult();
					vresultbase = a;
				} else {
					for ( int i=1; i<b; ++a, ++i ) { 
						builder.loadVarargs( i );
						builder.storeLocal(pc, a);
					}
				}
				break;				
			}
			
			// let builder process branch instructions
			builder.onEndOfLuaInstruction( pc );
		}
	}

	private void loadVarargResults(JavaBuilder builder, int pc, int a, int vresultbase) {
		if ( vresultbase <= a ) {
			builder.loadVarresult();
			builder.subargs( a+1-vresultbase );
		} else if ( vresultbase == a ) {
			builder.loadVarresult();
		} else {
			builder.newVarargsVarresult(pc, a, vresultbase-a);
		}
	}

	private void loadLocalOrConstant(Prototype p, JavaBuilder builder, int pc, int borc) {
		if ( borc<=0xff )
			builder.loadLocal( pc, borc );
		else
			builder.loadConstant( p.k[borc&0xff] );
	}
}
