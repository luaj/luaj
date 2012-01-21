/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.luaj.vm2.LoadState.LuaCompiler;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.DebugLib;

/**
 * Extension of {@link LuaFunction} which executes lua bytecode. 
 * <p>
 * A {@link LuaClosure} is a combination of a {@link Prototype} 
 * and a {@link LuaValue} to use as an environment for execution. 
 * <p>
 * There are three main ways {@link LuaClosure} instances are created:
 * <ul> 
 * <li>Construct an instance using {@link #LuaClosure(Prototype, LuaValue)}</li>
 * <li>Construct it indirectly by loading a chunk via {@link LuaCompiler#load(java.io.InputStream, String, LuaValue)}
 * <li>Execute the lua bytecode {@link Lua#OP_CLOSURE} as part of bytecode processing
 * </ul>
 * <p>
 * To construct it directly, the {@link Prototype} is typically created via a compiler such as {@link LuaC}:
 * <pre> {@code
 * InputStream is = new ByteArrayInputStream("print('hello,world').getBytes());
 * Prototype p = LuaC.instance.compile(is, "script");
 * LuaValue _G = JsePlatform.standardGlobals()
 * LuaClosure f = new LuaClosure(p, _G);
 * }</pre> 
 * <p>
 * To construct it indirectly, the {@link LuaC} compiler may be used, 
 * which implements the {@link LuaCompiler} interface: 
 * <pre> {@code
 * LuaFunction f = LuaC.instance.load(is, "script", _G);
 * }</pre>
 * <p>
 * Typically, a closure that has just been loaded needs to be initialized by executing it, 
 * and its return value can be saved if needed:
 * <pre> {@code
 * LuaValue r = f.call();
 * _G.set( "mypkg", r ) 
 * }</pre>
 * <p> 
 * In the preceding, the loaded value is typed as {@link LuaFunction} 
 * to allow for the possibility of other compilers such as {@link LuaJC}
 * producing {@link LuaFunction} directly without 
 * creating a {@link Prototype} or {@link LuaClosure}.
 * <p> 
 * Since a {@link LuaClosure} is a {@link LuaFunction} which is a {@link LuaValue}, 
 * all the value operations can be used directly such as:
 * <ul>
 * <li>{@link LuaValue#setfenv(LuaValue)}</li>
 * <li>{@link LuaValue#call()}</li>
 * <li>{@link LuaValue#call(LuaValue)}</li>
 * <li>{@link LuaValue#invoke()}</li>
 * <li>{@link LuaValue#invoke(Varargs)}</li>
 * <li>{@link LuaValue#method(String)}</li>
 * <li>{@link LuaValue#method(String,LuaValue)}</li>
 * <li>{@link LuaValue#invokemethod(String)}</li>
 * <li>{@link LuaValue#invokemethod(String,Varargs)}</li>
 * <li> ...</li> 
 * </ul>
 * @see LuaValue
 * @see LuaFunction
 * @see LuaValue#isclosure()
 * @see LuaValue#checkclosure()
 * @see LuaValue#optclosure(LuaClosure)
 * @see LoadState
 * @see LoadState#compiler
 */
public class LuaClosure extends LuaFunction {
	private static final UpValue[] NOUPVALUES = new UpValue[0];
	
	public final Prototype p;
	public final UpValue[] upValues;
	
	LuaClosure() {
		p = null;
		upValues = null;
	}
	/** Supply the initial environment */
	public LuaClosure(Prototype p, LuaValue env) {
		super( env );
		this.p = p;
		this.upValues = p.nups>0? new UpValue[p.nups]: NOUPVALUES;
	}
	
	protected LuaClosure(int nupvalues, LuaValue env) {
		super( env );
		this.p = null;
		this.upValues = nupvalues>0? new UpValue[nupvalues]: NOUPVALUES;
	}
	
	public boolean isclosure() {
		return true;
	}
	
	public LuaClosure optclosure(LuaClosure defval) {
		return this;
	}

	public LuaClosure checkclosure() {
		return this;
	}
	
	public LuaValue getmetatable() { 
		return s_metatable; 
	}
	
	public final LuaValue call() {
		LuaValue[] stack = new LuaValue[p.maxstacksize];
		System.arraycopy(NILS, 0, stack, 0, p.maxstacksize);
		return execute(stack,NONE).arg1();
	}

	public final LuaValue call(LuaValue arg) {
		LuaValue[] stack = new LuaValue[p.maxstacksize];
		System.arraycopy(NILS, 0, stack, 0, p.maxstacksize);
		switch ( p.numparams ) {
		default: stack[0]=arg; return execute(stack,NONE).arg1();
		case 0: return execute(stack,arg).arg1();
		}
	}
	
	public final LuaValue call(LuaValue arg1, LuaValue arg2) {
		LuaValue[] stack = new LuaValue[p.maxstacksize];
		System.arraycopy(NILS, 0, stack, 0, p.maxstacksize);
		switch ( p.numparams ) {
		default: stack[0]=arg1; stack[1]=arg2; return execute(stack,NONE).arg1();
		case 1: stack[0]=arg1; return execute(stack,arg2).arg1();
		case 0: return execute(stack,p.is_vararg!=0? varargsOf(arg1,arg2): NONE).arg1();
		}
	}

	public final LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
		LuaValue[] stack = new LuaValue[p.maxstacksize];
		System.arraycopy(NILS, 0, stack, 0, p.maxstacksize);
		switch ( p.numparams ) {
		default: stack[0]=arg1; stack[1]=arg2; stack[2]=arg3; return execute(stack,NONE).arg1();
		case 2: stack[0]=arg1; stack[1]=arg2; return execute(stack,arg3).arg1();
		case 1: stack[0]=arg1; return execute(stack,p.is_vararg!=0? varargsOf(arg2,arg3): NONE).arg1();
		case 0: return execute(stack,p.is_vararg!=0? varargsOf(arg1,arg2,arg3): NONE).arg1();
		}
	}

	public final Varargs invoke(Varargs varargs) {
		return onInvoke( varargs ).eval();
	}
	
	public Varargs onInvoke(Varargs varargs) {
		LuaValue[] stack = new LuaValue[p.maxstacksize];
		System.arraycopy(NILS, 0, stack, 0, p.maxstacksize);
		for ( int i=0; i<p.numparams; i++ )
			stack[i] = varargs.arg(i+1);		
		return execute(stack,p.is_vararg!=0? varargs.subargs(p.numparams+1): NONE);
	}
	
	
	protected Varargs execute( LuaValue[] stack, Varargs varargs ) {
		// loop through instructions
		int i,a,b,c,pc=0,top=0;
		LuaValue o;
		Varargs v = NONE;
		int[] code = p.code;
		LuaValue[] k = p.k;
		
		// upvalues are only possible when closures create closures
		UpValue[] openups = p.p.length>0? new UpValue[stack.length]: null;
		
		// create varargs "arg" table
		if ( p.is_vararg >= Lua.VARARG_NEEDSARG )
			stack[p.numparams] = new LuaTable(varargs);

		// debug wants args to this function
		if (DebugLib.DEBUG_ENABLED) 
			DebugLib.debugSetupCall(varargs, stack);

		// process instructions
		LuaThread.CallStack cs = LuaThread.onCall( this ); 
		try {
			while ( true ) {
				if (DebugLib.DEBUG_ENABLED) 
					DebugLib.debugBytecode(pc, v, top);
				
				// pull out instruction
				i = code[pc++];
				a = ((i>>6) & 0xff);
				
				// process the op code
				switch ( i & 0x3f ) {
				
				case Lua.OP_MOVE:/*	A B	R(A):= R(B)					*/
					stack[a] = stack[i>>>23];
					continue;
					
				case Lua.OP_LOADK:/*	A Bx	R(A):= Kst(Bx)					*/
					stack[a] = k[i>>>14];
					continue;
					
				case Lua.OP_LOADBOOL:/*	A B C	R(A):= (Bool)B: if (C) pc++			*/
	                stack[a] = (i>>>23!=0)? LuaValue.TRUE: LuaValue.FALSE;
	                if ((i&(0x1ff<<14)) != 0)
	                    pc++; /* skip next instruction (if C) */
	                continue;
	
				case Lua.OP_LOADNIL: /*	A B	R(A):= ...:= R(B):= nil			*/
					for ( b=i>>>23; a<=b; )
						stack[a++] = LuaValue.NIL;
					continue;
					
				case Lua.OP_GETUPVAL: /*	A B	R(A):= UpValue[B]				*/
	                stack[a] = upValues[i>>>23].getValue();
	                continue;
					
				case Lua.OP_GETGLOBAL: /*	A Bx	R(A):= Gbl[Kst(Bx)]				*/
	                stack[a] = env.get(k[i>>>14]);
					continue;
					
				case Lua.OP_GETTABLE: /*	A B C	R(A):= R(B)[RK(C)]				*/
	                stack[a] = stack[i>>>23].get((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]);
					continue;
					
				case Lua.OP_SETGLOBAL: /*	A Bx	Gbl[Kst(Bx)]:= R(A)				*/
	                env.set(k[i>>>14], stack[a]);
					continue;
					
				case Lua.OP_SETUPVAL: /*	A B	UpValue[B]:= R(A)				*/
					upValues[i>>>23].setValue(stack[a]);
					continue;
					
				case Lua.OP_SETTABLE: /*	A B C	R(A)[RK(B)]:= RK(C)				*/
					stack[a].set(((b=i>>>23)>0xff? k[b&0x0ff]: stack[b]), (c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]);
					continue;
					
				case Lua.OP_NEWTABLE: /*	A B C	R(A):= {} (size = B,C)				*/
					stack[a] = new LuaTable(i>>>23,(i>>14)&0x1ff);
					continue;
					
				case Lua.OP_SELF: /*	A B C	R(A+1):= R(B): R(A):= R(B)[RK(C)]		*/
					stack[a+1] = (o = stack[i>>>23]);
					stack[a] = o.get((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]);
					continue;
					
				case Lua.OP_ADD: /*	A B C	R(A):= RK(B) + RK(C)				*/
					stack[a] = ((b=i>>>23)>0xff? k[b&0x0ff]: stack[b]).add((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]);
					continue;
					
				case Lua.OP_SUB: /*	A B C	R(A):= RK(B) - RK(C)				*/
					stack[a] = ((b=i>>>23)>0xff? k[b&0x0ff]: stack[b]).sub((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]);
					continue;
					
				case Lua.OP_MUL: /*	A B C	R(A):= RK(B) * RK(C)				*/
					stack[a] = ((b=i>>>23)>0xff? k[b&0x0ff]: stack[b]).mul((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]);
					continue;
					
				case Lua.OP_DIV: /*	A B C	R(A):= RK(B) / RK(C)				*/
					stack[a] = ((b=i>>>23)>0xff? k[b&0x0ff]: stack[b]).div((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]);
					continue;
					
				case Lua.OP_MOD: /*	A B C	R(A):= RK(B) % RK(C)				*/
					stack[a] = ((b=i>>>23)>0xff? k[b&0x0ff]: stack[b]).mod((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]);
					continue;
					
				case Lua.OP_POW: /*	A B C	R(A):= RK(B) ^ RK(C)				*/
					stack[a] = ((b=i>>>23)>0xff? k[b&0x0ff]: stack[b]).pow((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]);
					continue;
					
				case Lua.OP_UNM: /*	A B	R(A):= -R(B)					*/
					stack[a] = stack[i>>>23].neg();
					continue;
					
				case Lua.OP_NOT: /*	A B	R(A):= not R(B)				*/
					stack[a] = stack[i>>>23].not();
					continue;
					
				case Lua.OP_LEN: /*	A B	R(A):= length of R(B)				*/
					stack[a] = stack[i>>>23].len();
					continue;
					
				case Lua.OP_CONCAT: /*	A B C	R(A):= R(B).. ... ..R(C)			*/
					b = i>>>23;
					c = (i>>14)&0x1ff;
					{
						if ( c > b+1 ) {
							Buffer sb = stack[c].buffer();
							while ( --c>=b ) 
								sb = stack[c].concat(sb);
							stack[a] = sb.value();
						} else {
							stack[a] = stack[c-1].concat(stack[c]);
						}
					}
					continue;
					
				case Lua.OP_JMP: /*	sBx	pc+=sBx					*/
					pc  += (i>>>14)-0x1ffff;
					continue;
					
				case Lua.OP_EQ: /*	A B C	if ((RK(B) == RK(C)) ~= A) then pc++		*/
					if ( ((b=i>>>23)>0xff? k[b&0x0ff]: stack[b]).eq_b((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]) != (a!=0) ) 
						++pc;
					continue;
					
				case Lua.OP_LT: /*	A B C	if ((RK(B) <  RK(C)) ~= A) then pc++  		*/
					if ( ((b=i>>>23)>0xff? k[b&0x0ff]: stack[b]).lt_b((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]) != (a!=0) ) 
						++pc;
					continue;
					
				case Lua.OP_LE: /*	A B C	if ((RK(B) <= RK(C)) ~= A) then pc++  		*/
					if ( ((b=i>>>23)>0xff? k[b&0x0ff]: stack[b]).lteq_b((c=(i>>14)&0x1ff)>0xff? k[c&0x0ff]: stack[c]) != (a!=0) ) 
						++pc;
					continue;
					
				case Lua.OP_TEST: /*	A C	if not (R(A) <=> C) then pc++			*/ 
					if ( stack[a].toboolean() != ((i&(0x1ff<<14))!=0) ) 
						++pc;
					continue;
					
				case Lua.OP_TESTSET: /*	A B C	if (R(B) <=> C) then R(A):= R(B) else pc++	*/
					/* note: doc appears to be reversed */
					if ( (o=stack[i>>>23]).toboolean() != ((i&(0x1ff<<14))!=0) ) 
						++pc;
					else
						stack[a] = o; // TODO: should be sBx? 
					continue;
					
				case Lua.OP_CALL: /*	A B C	R(A), ... ,R(A+C-2):= R(A)(R(A+1), ... ,R(A+B-1)) */
					switch ( i & (Lua.MASK_B | Lua.MASK_C) ) {
					case (1<<Lua.POS_B) | (0<<Lua.POS_C): v=stack[a].invoke(NONE); top=a+v.narg(); continue;
					case (2<<Lua.POS_B) | (0<<Lua.POS_C): v=stack[a].invoke(stack[a+1]); top=a+v.narg(); continue;
					case (1<<Lua.POS_B) | (1<<Lua.POS_C): stack[a].call(); continue;
					case (2<<Lua.POS_B) | (1<<Lua.POS_C): stack[a].call(stack[a+1]); continue;
					case (3<<Lua.POS_B) | (1<<Lua.POS_C): stack[a].call(stack[a+1],stack[a+2]); continue;
					case (4<<Lua.POS_B) | (1<<Lua.POS_C): stack[a].call(stack[a+1],stack[a+2],stack[a+3]); continue;
					case (1<<Lua.POS_B) | (2<<Lua.POS_C): stack[a] = stack[a].call(); continue;
					case (2<<Lua.POS_B) | (2<<Lua.POS_C): stack[a] = stack[a].call(stack[a+1]); continue;
					case (3<<Lua.POS_B) | (2<<Lua.POS_C): stack[a] = stack[a].call(stack[a+1],stack[a+2]); continue;
					case (4<<Lua.POS_B) | (2<<Lua.POS_C): stack[a] = stack[a].call(stack[a+1],stack[a+2],stack[a+3]); continue;
					default:
						b = i>>>23;
						c = (i>>14)&0x1ff;
						v = b>0? 
							varargsOf(stack,a+1,b-1): // exact arg count
							varargsOf(stack, a+1, top-v.narg()-(a+1), v); // from prev top 
						v = stack[a].invoke(v);
						if ( c > 0 ) {
							while ( --c > 0 )
								stack[a+c-1] = v.arg(c);
							v = NONE; // TODO: necessary?
						} else {
							top = a + v.narg();
						}
						continue;
					}
					
				case Lua.OP_TAILCALL: /*	A B C	return R(A)(R(A+1), ... ,R(A+B-1))		*/
					switch ( i & Lua.MASK_B ) {
					case (1<<Lua.POS_B): return new TailcallVarargs(stack[a], NONE);
					case (2<<Lua.POS_B): return new TailcallVarargs(stack[a], stack[a+1]);
					case (3<<Lua.POS_B): return new TailcallVarargs(stack[a], varargsOf(stack[a+1],stack[a+2]));
					case (4<<Lua.POS_B): return new TailcallVarargs(stack[a], varargsOf(stack[a+1],stack[a+2],stack[a+3]));
					default:
						b = i>>>23;
						v = b>0? 
							varargsOf(stack,a+1,b-1): // exact arg count
							varargsOf(stack, a+1, top-v.narg()-(a+1), v); // from prev top 
						return new TailcallVarargs( stack[a], v );
					}
					
				case Lua.OP_RETURN: /*	A B	return R(A), ... ,R(A+B-2)	(see note)	*/
					b = i>>>23;
					switch ( b ) {
					case 0: return varargsOf(stack, a, top-v.narg()-a, v); 
					case 1: return NONE;
					case 2: return stack[a]; 
					default:
						return varargsOf(stack, a, b-1);
					}
					
				case Lua.OP_FORLOOP: /*	A sBx	R(A)+=R(A+2): if R(A) <?= R(A+1) then { pc+=sBx: R(A+3)=R(A) }*/
					{
			            LuaValue limit = stack[a + 1];
						LuaValue step  = stack[a + 2];
						LuaValue idx   = step.add(stack[a]);
			            if (step.gt_b(0)? idx.lteq_b(limit): idx.gteq_b(limit)) {
		                    stack[a] = idx;
		                    stack[a + 3] = idx;
		                    pc += (i>>>14)-0x1ffff;
			            }
					}
					continue;
					
				case Lua.OP_FORPREP: /*	A sBx	R(A)-=R(A+2): pc+=sBx				*/
					{
						LuaValue init  = stack[a].checknumber("'for' initial value must be a number");
						LuaValue limit = stack[a + 1].checknumber("'for' limit must be a number");
						LuaValue step  = stack[a + 2].checknumber("'for' step must be a number");
						stack[a] = init.sub(step);
						stack[a + 1] = limit;
						stack[a + 2] = step;
						pc += (i>>>14)-0x1ffff;
					}
					continue;
					
				case Lua.OP_TFORLOOP: /*
									 * A C R(A+3), ... ,R(A+2+C):= R(A)(R(A+1),
									 * R(A+2)): if R(A+3) ~= nil then R(A+2)=R(A+3)
									 * else pc++
									 */
					// TODO: stack call on for loop body, such as:   stack[a].call(ci);
					v = stack[a].invoke(varargsOf(stack[a+1],stack[a+2]));
					if ( (o=v.arg1()).isnil() )
						++pc;
					else {
						stack[a+2] = stack[a+3] = o;
						for ( c=(i>>14)&0x1ff; c>1; --c )
							stack[a+2+c] = v.arg(c);
						v = NONE; // todo: necessary? 
					}
					continue;
					
				case Lua.OP_SETLIST: /*	A B C	R(A)[(C-1)*FPF+i]:= R(A+i), 1 <= i <= B	*/
					{
		                if ( (c=(i>>14)&0x1ff) == 0 )
		                    c = code[pc++];
		                int offset = (c-1) * Lua.LFIELDS_PER_FLUSH;
		                o = stack[a];
		                if ( (b=i>>>23) == 0 ) {
		                    b = top - a - 1;
		                    int m = b - v.narg(); 
		                	int j=1;
		                	for ( ;j<=m; j++ )
		                    	o.set(offset+j, stack[a + j]);
		                	for ( ;j<=b; j++ )
		                    	o.set(offset+j, v.arg(j-m));
		                } else {
		                    o.presize( offset + b );
		                    for (int j=1; j<=b; j++)
		                    	o.set(offset+j, stack[a + j]);
		                }
					}
					continue;
					
				case Lua.OP_CLOSE: /*	A 	close all variables in the stack up to (>=) R(A)*/
					for ( b=openups.length; --b>=a; )
						if ( openups[b]!=null ) {
							openups[b].close();
							openups[b] = null;
						}
					continue;
					
				case Lua.OP_CLOSURE: /*	A Bx	R(A):= closure(KPROTO[Bx], R(A), ... ,R(A+n))	*/
					{
						Prototype newp = p.p[i>>>14];
						LuaClosure newcl = new LuaClosure(newp, env);
						for ( int j=0, nup=newp.nups; j<nup; ++j ) {
							i = code[pc++];
							//b = B(i);
							b = i>>>23;
							newcl.upValues[j] = (i&4) != 0? 
									upValues[b]:
									openups[b]!=null? openups[b]: (openups[b]=new UpValue(stack,b));
						}
						stack[a] = newcl;
					}
					continue;
					
				case Lua.OP_VARARG: /*	A B	R(A), R(A+1), ..., R(A+B-1) = vararg		*/
					b = i>>>23;
					if ( b == 0 ) {
						top = a + (b = varargs.narg());
						v = varargs;
					} else { 
						for ( int j=1; j<b; ++j )
							stack[a+j-1] = varargs.arg(j);
					}
					continue;				
				}
			}
		} catch ( LuaError le ) {
			throw le;
		} catch ( Exception e ) {
			throw new LuaError(e);
		} finally {
			cs.onReturn();
			if ( openups != null )
				for ( int u=openups.length; --u>=0; )
					if ( openups[u] != null )
						openups[u].close();
		}
	}

	protected LuaValue getUpvalue(int i) {
		return upValues[i].getValue();
	}
	
	protected void setUpvalue(int i, LuaValue v) {
		upValues[i].setValue(v);
	}
}
