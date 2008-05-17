package org.luaj.jit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.luaj.compiler.LuaC;
import org.luaj.debug.Print;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

public class LuaJit extends Lua {
	
	public static void main(String[] args) throws IOException {
		
	    Platform.setInstance(new J2sePlatform());
		LuaC.install();
		
		String program = "print 'starting'\nfor i=1,10 do\n\tprint 'hello, world'\nend";
		InputStream is = new ByteArrayInputStream(program.getBytes());
		LPrototype p = LuaC.compile(is, "program");
		test( p );
		LPrototype q = LuaJit.compile( p );
		test( q );
//		JitPrototype jp = new JitPrototypeOne();
//		jp.setLuaPrototype(p);
//		test( jp );
	}
	
	private static void test(LPrototype p) {
		try {
			LuaState vm = Platform.newLuaState();
			LClosure c = p.newClosure(vm._G);
			vm.pushlvalue(c);
			vm.call(0, 0);
		} catch ( Throwable e ) {
			e.printStackTrace();
		}
	}

	public static LPrototype compile( LPrototype p ) {
        int i, a, b, c, o, n, cb;
        LValue rkb, rkc, nvarargs, key, val;
        LValue i0, step, idx, limit, init, table;
        boolean back, body;

        PrintWriter pw = new PrintWriter( System.out );
        String name = "JitPrototypeOne";
		pw.print( 
				"package org.luaj.jit;\n"+
				"import org.luaj.vm.LTable;\n"+
				"import org.luaj.vm.LuaState;\n"+
				"public class "+name+" extends JitPrototype {\n"+
				"	public void jitCall(LuaState vm, LTable env) {\n"+
				"" );
		
        int[] code = p.code;
        LValue[] k = p.k;
        
        // loop until a return instruction is processed, 
        // or the vm yields
		for ( int pc=0; pc<code.length; pc++ ) {

			// print the instruction 
			pw.print( "\n\t\t// "); 
			pw.flush();
            Print.printOpCode(p, pc);
            pw.println();
			
            // get instruction
            i = code[pc];
            
            // get opcode and first arg
        	o = (i >> POS_OP) & MAX_OP;
    		a = (i >> POS_A) & MAXARG_A;
        	
            switch (o) {
            case LuaState.OP_MOVE: {
                b = LuaState.GETARG_B(i);
                // this.stack[base + a] = this.stack[base + b];
                pw.println( "\t\tvm.stack[base+"+a+"] = vm.stack[base+"+b+"];" );
                continue;
            }
            case LuaState.OP_LOADK: {
                b = LuaState.GETARG_Bx(i);
                // this.stack[base + a] = k[b];
                pw.println( "\t\tvm.stack[base+"+a+"] = p.k["+b+"];" );
                continue;
            }
            /*
            case LuaState.OP_LOADBOOL: {
                b = LuaState.GETARG_B(i);
                c = LuaState.GETARG_C(i);
                this.stack[base + a] = (b != 0 ? LBoolean.TRUE : LBoolean.FALSE);
                if (c != 0)
                    ci.pc++; // skip next instruction (if C)
                continue;
            }
            case LuaState.OP_LOADNIL: {
                b = LuaState.GETARG_B(i);
                do {
                    this.stack[base + b] = LNil.NIL;
                } while ((--b) >= a);
                continue;
            }
            case LuaState.OP_GETUPVAL: {
                b = LuaState.GETARG_B(i);
                this.stack[base + a] = cl.upVals[b].getValue();
                continue;
            }
            */
            case LuaState.OP_GETGLOBAL: {
                b = LuaState.GETARG_Bx(i);
                // key = k[b];
                // table = cl.env;
                // top = base + a;
                // table.luaGetTable(this, table, key);
                // pw.println("\t\tvm.top = base+"+a+";");
                pw.println("\t\tvm.settop("+a+");");
                pw.println("\t\tenv.luaGetTable(vm, env, p.k["+b+"]);");
                continue;
            }
            /*
            case LuaState.OP_GETTABLE: {
                b = LuaState.GETARG_B(i);
                key = GETARG_RKC(k, i);
                table = this.stack[base + b];
                top = base + a;
                table.luaGetTable(this, table, key);
                continue;
            }
            case LuaState.OP_SETGLOBAL: {
                b = LuaState.GETARG_Bx(i);
                key = k[b];
                val = this.stack[base + a];
                table = cl.env;
                table.luaSetTable(this, table, key, val);
                continue;
            }
            case LuaState.OP_SETUPVAL: {
                b = LuaState.GETARG_B(i);
                cl.upVals[b].setValue( this.stack[base + a] );
                continue;
            }
            case LuaState.OP_SETTABLE: {
                key = GETARG_RKB(k, i);
                val = GETARG_RKC(k, i);
                table = this.stack[base + a];
                table.luaSetTable(this, table, key, val);
                continue;
            }
            case LuaState.OP_NEWTABLE: {
                b = LuaState.GETARG_B(i);
                c = LuaState.GETARG_C(i);
                this.stack[base + a] = new LTable(b, c);
                continue;
            }
            case LuaState.OP_SELF: {
                rkb = GETARG_RKB(k, i);
                rkc = GETARG_RKC(k, i);
                top = base + a;
                rkb.luaGetTable(this, rkb, rkc);
                this.stack[base + a + 1] = rkb;
                // StkId rb = RB(i);
                // setobjs2s(L, ra+1, rb);
                // Protect(luaV_gettable(L, rb, RKC(i), ra));
                continue;
            }
            case LuaState.OP_ADD:
            case LuaState.OP_SUB:
            case LuaState.OP_MUL:
            case LuaState.OP_DIV:
            case LuaState.OP_MOD:
            case LuaState.OP_POW: {
                rkb = GETARG_RKB(k, i);
                rkc = GETARG_RKC(k, i);
                this.stack[base + a] = rkc.luaBinOpUnknown(o, rkb);
                continue;
            }
            case LuaState.OP_UNM: {
                rkb = GETARG_RKB(k, i);
                this.stack[base + a] = rkb.luaUnaryMinus();
                continue;
            }
            case LuaState.OP_NOT: {
                rkb = GETARG_RKB(k, i);
                this.stack[base + a] = (!rkb.toJavaBoolean() ? LBoolean.TRUE
                        : LBoolean.FALSE);
                continue;
            }
            case LuaState.OP_LEN: {
                rkb = GETARG_RKB(k, i);
                this.stack[base + a] = LInteger.valueOf( rkb.luaLength() );
                continue;
            }
            case LuaState.OP_CONCAT: {
                b = LuaState.GETARG_B(i);
                c = LuaState.GETARG_C(i);
                int numValues = c - b + 1;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (int j = b, l = 0; j <= c; j++, l++) {
                    this.stack[base + j].luaConcatTo( baos );
                }
                this.stack[base + a] = new LString( baos.toByteArray() );
                continue;
            }
            case LuaState.OP_JMP: {
                ci.pc += LuaState.GETARG_sBx(i);
                continue;
            }
            case LuaState.OP_EQ:
            case LuaState.OP_LT:
            case LuaState.OP_LE: {
                rkb = GETARG_RKB(k, i);
                rkc = GETARG_RKC(k, i);
                boolean test = rkc.luaBinCmpUnknown(o, rkb);
                if (test == (a == 0))
                    ci.pc++;
                continue;
            }
            case LuaState.OP_TEST: {
                c = LuaState.GETARG_C(i);
                if (this.stack[base + a].toJavaBoolean() != (c != 0))
                    ci.pc++;
                continue;
            }
            case LuaState.OP_TESTSET: {
                rkb = GETARG_RKB(k, i);
                c = LuaState.GETARG_C(i);
                if (rkb.toJavaBoolean() != (c != 0))
                    ci.pc++;
                else
                    this.stack[base + a] = rkb;
                continue;
            }
            case LuaState.OP_CALL: {               
                
                // ra is base of new call frame
                this.base += a;
                
                // number of args
                b = LuaState.GETARG_B(i);
                if (b != 0) // else use previous instruction set top
                    top = base + b;
                
                // number of return values we need
                c = LuaState.GETARG_C(i);

                // make or set up the call
                this.nresults = c - 1;
                if (this.stack[base].luaStackCall(this))
                    return;
                
                // adjustTop only for case when call was completed
                // and number of args > 0. If call completed but
                // c == 0, leave top to point to end of results
                if (c > 0)
                    adjustTop(base + c - 1);
                
                // restore base
                base = ci.base;
                
                continue;
            }
            
            case LuaState.OP_TAILCALL: {
                closeUpVals(base);

                // copy down the frame before calling!

                // number of args (including the function)
                b = LuaState.GETARG_B(i);
                if (b == 0)
                    b = top - (base + a);

                // copy call + all args, discard current frame
                System.arraycopy(stack, base + a, stack, ci.resultbase, b);
                this.base = ci.resultbase;
                this.top = base + b;
                this.nresults = ci.nresults;
                --cc;
                
                // make or set up the call
                try {
                    if (this.stack[base].luaStackCall(this)) {
                        return;
                    }
                } catch (LuaErrorException e) {
                    // in case of lua error, we need to restore cc so that
                    // the debug can get the correct location where the error
                    // occured.
                    cc++;
                    throw e;
                }
                
                // adjustTop only for case when call was completed
                // and number of args > 0. If call completed but
                // c == 0, leave top to point to end of results
                if (this.nresults >= 0)
                    adjustTop(base + nresults);

                // force restore of base, etc.
                return;
            }

            case LuaState.OP_RETURN: {
                // number of return vals to return
                b = LuaState.GETARG_B(i) - 1; 
                if (b == -1)
                    b = top - (base + a);

                // close open upvals
                closeUpVals( base ); 

                // number to copy down
                System.arraycopy(stack, base + a, stack, ci.resultbase, b);
                top = ci.resultbase + b;

                // adjust results to what caller expected
                if (ci.nresults >= 0)
                    adjustTop(ci.resultbase + ci.nresults);

                // pop the call stack
                --cc;
                
                // force a reload of the calling context
                return;
            }
            case LuaState.OP_FORLOOP: {
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
                    top = base + a + 3 + 1;
                    ci.pc += LuaState.GETARG_sBx(i);
                }
                continue;
            }
            case LuaState.OP_FORPREP: {
                init = this.stack[base + a];
                step = this.stack[base + a + 2];
                this.stack[base + a] = step.luaBinOpUnknown(Lua.OP_SUB, init);
                b = LuaState.GETARG_sBx(i);
                ci.pc += b;
                continue;
            }
            case LuaState.OP_TFORLOOP: {
            	cb = base + a + 3; // call base 
            	base = cb;
            	adjustTop( cb + 3 );
                System.arraycopy(this.stack, cb-3, this.stack, cb, 3);
                
                // call the iterator
                c = LuaState.GETARG_C(i);
                this.nresults = c;
                if (this.stack[cb].luaStackCall(this))
                    execute();
                base = ci.base;
                adjustTop( cb + c );
                
                // test for continuation
                if (!this.stack[cb].isNil() ) { // continue?
                    this.stack[cb-1] = this.stack[cb]; // save control variable
                } else {
                    ci.pc++; // skip over jump
                }
                continue;
            }
            case LuaState.OP_SETLIST: {
                b = LuaState.GETARG_B(i);
                c = LuaState.GETARG_C(i);
                int listBase = base + a;
                if (b == 0) {
                    b = top - listBase - 1;
                }
                if (c == 0) {
                    c = code[ci.pc++];
                }
                int offset = (c-1) * LFIELDS_PER_FLUSH;
                LTable tbl = (LTable) this.stack[base + a];
                for (int j=1; j<=b; j++) {
                    tbl.put(offset+j, stack[listBase + j]);
                }
                top = base + a - 1;
                continue;
            }
            case LuaState.OP_CLOSE: {
                closeUpVals( base + a ); // close upvals higher in the stack than position a
                continue;
            }
            case LuaState.OP_CLOSURE: {
                b = LuaState.GETARG_Bx(i);
                proto = cl.p.p[b];
                newClosure = new LClosure(proto, cl.env);
                for (int j = 0; j < newClosure.upVals.length; j++, ci.pc++) {
                    i = code[ci.pc];
                    o = LuaState.GET_OPCODE(i);
                    b = LuaState.GETARG_B(i);
                    if (o == LuaState.OP_GETUPVAL) {
                        newClosure.upVals[j] = cl.upVals[b];
                    } else if (o == LuaState.OP_MOVE) {
                        newClosure.upVals[j] = findUpVal( base + b );
                    } else {
                        throw new java.lang.IllegalArgumentException(
                                "bad opcode: " + o);
                    }
                }
                this.stack[base + a] = newClosure;
                continue;
            }
            case LuaState.OP_VARARG: {
                // figure out how many args to copy
                b = LuaState.GETARG_B(i) - 1;
                nvarargs = this.stack[base - 1];
                n = nvarargs.toJavaInt();
                if (b == LuaState.LUA_MULTRET) {
                    b = n; // use entire varargs supplied
                }

                // copy args up to call stack area
                checkstack(a+b);
                for (int j = 0; j < b; j++)
                    this.stack[base + a + j] = (j < n ? this.stack[base
                            - n + j - 1]
                            : LNil.NIL);
                top = base + a + b;
                continue;
            }            
            */
            }
		}
        pw.print( "\n"+	
        	"		return false;\n"+
			"	}\n"+
			"}\n" );
        pw.flush();
		return p;
		
    }
	
}
