/**
 * 
 */
package lua;

import lua.io.Closure;
import lua.io.Proto;
import lua.io.UpVal;
import lua.value.LBoolean;
import lua.value.LInteger;
import lua.value.LNil;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LValue;

public class CallFrame {
	private final static boolean DEBUG = false;
	
	public final StackState state;
	public final LValue[] stack;
	public int base;
	public int top;
	public final Closure cl;
	public final Proto p;
	private final LValue[] k;
	private final int nresults;
	private int pc = 0;
	boolean done = false;

	CallFrame(StackState state, Closure c, int base, int nargs, int nresults) {
		this.state = state;
		this.stack = state.stack;
		this.cl = c;
		this.p = c.p;
		this.k = p.k;
		this.base = base;
		this.nresults = nresults;
		int nparams = p.numparams;
		int nvalues = (p.is_vararg && nargs > nparams ? nargs : nparams);
		for (int i = nargs; i < nvalues; i++)
			this.state.stack[base + i] = LNil.NIL;
		this.top = base + nvalues;
		this.state.calls[++this.state.cc] = this;
		this.state.avail = base + p.maxstacksize;
	}

	private LValue RKBC(int bc) {
		return StackState.ISK(bc) ? k[StackState.INDEXK(bc)]
				: this.state.stack[base + bc];
	}

	private LValue GETARG_RKB(int i) {
		return RKBC(StackState.GETARG_B(i));
	}

	private LValue GETARG_RKC(int i) {
		return RKBC(StackState.GETARG_C(i));
	}

	public void adjustTop(int newTop) {
		while (top < newTop)
			this.stack[top++] = LNil.NIL;
		top = newTop;
	}

	public void exec() {
		int i, a, b, c, o, n, cb;
		LValue rkb, rkc, nvarargs, key, val;
		StringBuffer sb;
		LValue i0, step, idx, limit, init, table;
		boolean back, body;
		Proto proto;
		Closure newClosure;

		// reload the current calling context
		int[] code = p.code;
		while (true) {

			if (DEBUG)
				Print.printState(state, base, top, state.avail, cl, pc);

			i = code[pc++];

			// TODO: implement debug hooks
			// if ((L->hookmask & (LUA_MASKLINE | LUA_MASKCOUNT)) &&
			// (--L->hookcount == 0 || L->hookmask & LUA_MASKLINE)) {
			// traceexec(L, pc);
			// if (L->status == LUA_YIELD) { // did hook yield?
			// L->savedpc = pc - 1;
			// return;
			// }
			// base = L->base;
			// }

			a = StackState.GETARG_A(i);
			switch (StackState.GET_OPCODE(i)) {
			case StackState.OP_MOVE: {
				b = StackState.GETARG_B(i);
				this.stack[base + a] = this.stack[base + b];
				continue;
			}
			case StackState.OP_LOADK: {
				b = StackState.GETARG_Bx(i);
				this.stack[base + a] = k[b];
				continue;
			}
			case StackState.OP_LOADBOOL: {
				b = StackState.GETARG_B(i);
				c = StackState.GETARG_C(i);
				this.stack[base + a] = (b != 0 ? LBoolean.TRUE
						: LBoolean.FALSE);
				if (c != 0)
					pc++; /* skip next instruction (if C) */
				continue;
			}
			case StackState.OP_LOADNIL: {
				b = StackState.GETARG_B(i);
				do {
					this.stack[base + b] = LNil.NIL;
				} while ((--b) >= a);
				continue;
			}
			case StackState.OP_GETUPVAL: {
				b = StackState.GETARG_B(i);
				this.stack[base + a] = cl.upVals[b].getValue();
				continue;
			}
			case StackState.OP_GETGLOBAL: {
				b = StackState.GETARG_Bx(i);
				key = k[b];
				table = cl.env;
				table.luaGetTable(this, base + a, table, key);
				continue;
			}
			case StackState.OP_GETTABLE: {
				b = StackState.GETARG_B(i);
				key = GETARG_RKC(i);
				table = this.stack[base + b];
				table.luaGetTable(this, base + a, table, key);
				continue;
			}
			case StackState.OP_SETGLOBAL: {
				b = StackState.GETARG_Bx(i);
				key = k[b];
				val = this.stack[base + a];
				table = cl.env;
				table.luaSetTable(this, this.state.avail, table, key, val);
				continue;
			}
			case StackState.OP_SETUPVAL: {
				b = StackState.GETARG_B(i);
				cl.upVals[b].setValue( this.stack[base + a] );
				continue;
			}
			case StackState.OP_SETTABLE: {
				key = GETARG_RKB(i);
				val = GETARG_RKC(i);
				table = this.stack[base + a];
				table.luaSetTable(this, state.avail, table, key, val);
				continue;
			}
			case StackState.OP_NEWTABLE: {
				b = StackState.GETARG_B(i);
				c = StackState.GETARG_C(i);
				this.stack[base + a] = new LTable(b, c);
				continue;
			}
			case StackState.OP_SELF: {
				rkb = GETARG_RKB(i);
				rkc = GETARG_RKC(i);
				this.stack[base + a + 1] = rkb;
				rkb.luaGetTable(this, base + a, rkb, rkc);
				// StkId rb = RB(i);
				// setobjs2s(L, ra+1, rb);
				// Protect(luaV_gettable(L, rb, RKC(i), ra));
				continue;
			}
			case StackState.OP_ADD:
			case StackState.OP_SUB:
			case StackState.OP_MUL:
			case StackState.OP_DIV:
			case StackState.OP_MOD:
			case StackState.OP_POW: {
				o = StackState.GET_OPCODE(i);
				rkb = GETARG_RKB(i);
				rkc = GETARG_RKC(i);
				this.stack[base + a] = rkc.luaBinOpUnknown(o, rkb);
				continue;
			}
			case StackState.OP_UNM: {
				rkb = GETARG_RKB(i);
				this.stack[base + a] = rkb.luaUnaryMinus();
				continue;
			}
			case StackState.OP_NOT: {
				rkb = GETARG_RKB(i);
				this.stack[base + a] = (rkb.luaAsBoolean() ? LBoolean.TRUE
						: LBoolean.FALSE);
				continue;
			}
			case StackState.OP_LEN: {
				rkb = GETARG_RKB(i);
				this.stack[base + a] = rkb.luaLength();
				continue;
			}
			case StackState.OP_CONCAT: {
				b = StackState.GETARG_B(i);
				c = StackState.GETARG_C(i);
				sb = new StringBuffer();
				for (int j = b; j <= c; j++)
					sb.append(this.stack[base + j].luaAsString());
				this.stack[base + a] = new LString(sb.toString());
				continue;
			}
			case StackState.OP_JMP: {
				pc += StackState.GETARG_sBx(i);
				continue;
			}
			case StackState.OP_EQ:
			case StackState.OP_LT:
			case StackState.OP_LE: {
				o = StackState.GET_OPCODE(i);
				rkb = GETARG_RKB(i);
				rkc = GETARG_RKC(i);
				boolean test = rkc.luaBinCmpUnknown(o, rkb);
				if (test)
					pc++;
				continue;
			}
			case StackState.OP_TEST: {
				c = StackState.GETARG_C(i);
				if (this.stack[base + a].luaAsBoolean() != (c != 0))
					pc++;
				continue;
			}
			case StackState.OP_TESTSET: {
				rkb = GETARG_RKB(i);
				c = StackState.GETARG_C(i);
				if (rkb.luaAsBoolean() != (c != 0))
					pc++;
				else
					this.stack[base + a] = rkb;
				continue;
			}
			case StackState.OP_CALL: {
				/* ra is start of result location */
				b = StackState.GETARG_B(i); // number of stack spaces to reserve
											// for
				// closure plus args
				c = StackState.GETARG_C(i); // num results plus 1
				if (b != 0) // else use previous instruction set top
					top = base + a + b;

				// make or set up the call
				this.stack[base + a].luaStackCall(this, base + a, top, c - 1);

				// force re-entry into current call
				if (this.state.calls[this.state.cc] != this)
					return;

				// adjustTop only for case when call was completed
				if (c > 0)
					adjustTop(base + a + c - 1);

				continue;
			}
			case StackState.OP_TAILCALL: {
				b = StackState.GETARG_B(i); // number of stack spaces to reserve
											// for
				// closure plus args ??
				c = StackState.GETARG_C(i); // number of return args - must be
				// LUA_MULTRET
				if (b != 0) // else use previous instruction set top
					top = base + a + b;

				close( base ); // Close open upvals

				// make or set up the call
				this.stack[base + a].luaStackCall(this, base + a, top, c - 1);

				// adjustTop only for case when call was completed
				if (this.state.calls[this.state.cc] != this) {
					// was a vm call, or a Java call that re-entered the stack.
					// copy down the stack variables and substitute the stack
					// frame.
					CallFrame ci = this.state.calls[this.state.cc];
					n = ci.top - ci.base;
					System.arraycopy(this.stack, ci.base,
							this.stack, base, n);
					ci.base = base;
					ci.top = base + n;
					this.state.calls[this.state.cc - 1] = this.state.calls[this.state.cc];
					--this.state.cc;

					// force a reset of the calling context state
					return;
				}
				continue;
			}
			case StackState.OP_RETURN: {
				b = StackState.GETARG_B(i); // number of return vals
				if (b != 0) // else use previous top
					top = base + a + b - 1;
				close( base ); // close open upvals
				n = top - (base + a); // number to copy down
				System.arraycopy(this.stack, base + a, this.stack,
						base - 1, n);
				top = base - 1 + n;

				// adjust results to what caller expected
				if (nresults >= 0)
					adjustTop(base + nresults);

				// pop the call stack
				done = true;
				if ( --state.cc >= 0 ) {
					CallFrame call = state.calls[state.cc];
					call.top = top;
				}

				// force a reload of the calling context
				return;
			}
			case StackState.OP_FORLOOP: {
				i0 = this.stack[base + a];
				step = this.stack[base + a + 2];
				idx = step.luaBinOpUnknown(Lua.OP_ADD, i0);
				limit = this.stack[base + a + 1];
				back = step.luaBinCmpInteger(Lua.OP_LT, 0);
				body = (back ? idx.luaBinCmpUnknown(Lua.OP_LE, limit) : limit
						.luaBinCmpUnknown(Lua.OP_LE, idx));
				if (body) {
					this.stack[base + a] = idx;
					this.stack[base + a + 3] = idx;
					pc += StackState.GETARG_sBx(i);
				}
				continue;
			}
			case StackState.OP_FORPREP: {
				init = this.stack[base + a];
				step = this.stack[base + a + 2];
				this.stack[base + a] = step.luaBinOpUnknown(Lua.OP_SUB,
						init);
				b = StackState.GETARG_sBx(i);
				pc += b;
				continue;
			}
			case StackState.OP_TFORLOOP: {
				cb = a + 3; /* call base */
				System.arraycopy(this.stack, base + a, this.stack,
						base + cb, 3);
				top = base + cb + 3; /* func. + 2 args (state and index) */

				// call the iterator
				c = StackState.GETARG_C(i);
				this.stack[base + a].luaStackCall(this, base + cb, top, c - 1);

				// test for continuation
				if (this.stack[base + cb] != LNil.NIL) { // continue?
					this.stack[base + cb - 1] = this.stack[base
							+ cb]; // save control variable
				} else {
					pc++; // skip over jump
				}
				continue;
			}
			case StackState.OP_SETLIST: {
				b = StackState.GETARG_B(i);
				c = StackState.GETARG_C(i);
				int listBase = base + a;
				if (b == 0) {
					b = top - listBase - 1;
				}
				if (c == 0) {
					c = code[pc++];
				}
				table = this.stack[base + a];
				for (int index = 1; index <= b; index++) {
					val = this.stack[listBase + index];
					table.luaSetTable(this, this.state.avail, table,
							new LInteger(index), val);
				}
				top = base + a - 1;
				continue;
			}
			case StackState.OP_CLOSE: {
				close( a ); // close upvals higher in the stack than position a
				continue;
			}
			case StackState.OP_CLOSURE: {
				b = StackState.GETARG_Bx(i);
				proto = cl.p.p[b];
				newClosure = new Closure(this.state, proto);
				for (int j = 0; j < newClosure.upVals.length; j++, pc++) {
					i = code[pc];
					o = StackState.GET_OPCODE(i);
					b = StackState.GETARG_B(i);
					if (o == StackState.OP_GETUPVAL) {
						newClosure.upVals[j] = cl.upVals[b];
					} else if (o == StackState.OP_MOVE) {
						newClosure.upVals[j] = findUpVal( proto.upvalues[j], base + b );
					} else {
						throw new java.lang.IllegalArgumentException(
								"bad opcode: " + o);
					}
				}
				this.stack[base + a] = newClosure;
				continue;
			}
			case StackState.OP_VARARG: {
				// figure out how many args to copy
				b = StackState.GETARG_B(i) - 1;
				nvarargs = this.stack[base - 1];
				n = nvarargs.luaAsInt();
				if (b == StackState.LUA_MULTRET) {
					b = n; // use entire varargs supplied
				}

				// copy args up to call stack area
				for (int j = 0; j < b; j++)
					this.stack[base + a + j] = (j < n ? this.stack[base
							- n + j - 1]
							: LNil.NIL);
				top = base + a + b;
				continue;
			}
			}
		}
	}
	
	private UpVal findUpVal( LString upValName, int target ) {
		UpVal up;
		int i;
		for ( i = this.state.upvals.size() - 1; i >= 0; --i ) {
			up = (UpVal) this.state.upvals.elementAt( i );
			if ( up.stack == this.stack && up.position == target ) {
				return up;
			} else if ( up.position < target ) {
				break;
			}
		}
		
		up = new UpVal( upValName, this.stack, target );
		this.state.upvals.insertElementAt( up, i + 1 );
		return up;
	}
	
	private void close( int limit ) {
		while ( !state.upvals.empty() && ( (UpVal) this.state.upvals.lastElement() ).close( limit ) ) {
			this.state.upvals.pop();
		}
	}
	
	public void push(LValue value) {
		stack[top++] = value;
	}
}