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
package org.luaj.jit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.luaj.compiler.LuaC;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LValue;
import org.luaj.vm.LoadState;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaState;
import org.luaj.vm.LoadState.LuaCompiler;

public class LuaJit extends Lua implements LuaCompiler {

	private static LuaC luac;
	
	public static void install() {
		luac = new LuaC();
		LoadState.compiler = new LuaJit();
	}
	
	private static int filenum = 0;
	
	private static synchronized String filename() {
		return "LuaJit"+(filenum++);
	}
	
	public LPrototype compile(int firstByte, InputStream stream, String name) throws IOException {
		return jitCompile( luac.compile(firstByte, stream, name) );
	}
	
	public static LPrototype jitCompile( LPrototype p ) {
        try {
			final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null) {
				System.err.println("no java compiler");
				return p;
			}

			// write the file
			String name = filename();
			new File("jit").mkdirs();
			File source = new File("jit/"+name+JavaFileObject.Kind.SOURCE.extension);
			PrintStream ps = new PrintStream(new FileOutputStream(source));
			writeSource(ps, name, p);
			ps.close();

			// set up output location 
			Iterable<? extends File> dest = Arrays.asList(new File[] { new File("bin") });
			StandardJavaFileManager fm = compiler.getStandardFileManager( null, null, null);
			fm.setLocation(StandardLocation.CLASS_OUTPUT, dest);
			
			// compile the file
			Iterable<? extends JavaFileObject> compilationUnits = fm.getJavaFileObjects(source);
			CompilationTask task = compiler.getTask(null, fm, null, null, null, compilationUnits);
			boolean success = task.call();

			// instantiate, config and return
			if (success) {
				// compile sub-prototypes
				if ( p.p != null ) {
					for ( int i=0, n=p.p.length; i<n; i++ ) {
						if ( ! (p.p[i] instanceof JitPrototype) ) 
							p.p[i] = jitCompile( p.p[i] );
					}
				}
				
				// create JitPrototype instance
				Class clazz = Class.forName(name);
				Object instance = clazz.newInstance();
				JitPrototype jp = (JitPrototype) instance;
				jp.setLuaPrototype(p);
				return jp;
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return p;
        
	}
	
    private static String RKBC_jit(int bc) {
        return LuaState.ISK(bc) ? 
                "k"+LuaState.INDEXK(bc):
                "s"+bc;
    }
    
    private static String GETARG_RKB_jit(int i) {
        return RKBC_jit(GETARG_B(i));
    }

    private static String GETARG_RKC_jit(int i) {
        return RKBC_jit(GETARG_C(i));
    }

    private static Set<Integer> TESTS = new HashSet<Integer>();
    static {
    	TESTS.add( OP_TEST );
    	TESTS.add( OP_EQ );
    	TESTS.add( OP_LT );
    	TESTS.add( OP_LE );
    	TESTS.add( OP_TESTSET );
    }
    
    private static boolean istest(int instr) {
    	int opcode = Lua.GET_OPCODE(instr);
    	return TESTS.contains(opcode);
    }
    
    private static boolean isjump(int instr) {
    	if ( OP_JMP != Lua.GET_OPCODE(instr) )
    		return false;
		return true;
    }
    
    private static boolean isbackwardjump(int instr) {
		return isjump(instr) && (LuaState.GETARG_sBx(instr) < 0);
    }
    
    private static boolean isforwardjump(int instr) {
		return isjump(instr) && (LuaState.GETARG_sBx(instr) > 0);
    }
    
    private static boolean isloopbottom(int instr) {
		return isbackwardjump(instr) || OP_FORLOOP == Lua.GET_OPCODE(instr);
    }
    
    private static String append( String s, String t ) {
    	return (s==null? t: t==null? s: s+t);
    }
    
    private static void assertTrue(boolean b) {
    	if ( ! b )
    		throw new RuntimeException("assert failed"); 
    }

    private static String[] extractControlFlow( int[] code ) {
    	int n = code.length;
    	String[] s = new String[n];
    	
    	for ( int pc=0; pc<n; pc++ ) {
    		int i = code[pc];

    		// any backward jump is a loop bottom
    		if ( isbackwardjump(i) ) {
				int jmp = LuaState.GETARG_sBx(i);
				s[pc+jmp+1] = append( s[pc+jmp+1], "while (true) { if(false)break; /* WHILETOP */ " );
    			s[pc] = append( "} /* LOOPBOT */ ", s[pc] );
    			int i2 = code[pc-1];
				if ( istest(i2) ) {
        			s[pc] = append( "break; /* UNTIL */", s[pc] );
				}

				// TFORLOOP test is at top in Java, not bottom
				else if ( Lua.GET_OPCODE(i2) == OP_TFORLOOP ) {
    				int a = LuaState.GETARG_A(i2);
    				int c = LuaState.GETARG_C(i2);
    				assertTrue(c==2);
    				s[pc+jmp+1] = append( s[pc+jmp+1], 
    					"\n\t\tvm.stack[base+"+(a+3)+"] = s"+(a+0)+";"+ // iterator
    					"\n\t\tvm.stack[base+"+(a+4)+"] = s"+(a+1)+";"+ // table
    					"\n\t\tvm.stack[base+"+(a+5)+"] = s"+(a+2)+";"+ // key
    					"\n\t\tvm.top = base+"+(a+6)+";"+
    					"\n\t\tvm.call(2,2);"+
    					"\n\t\ts"+(a+3)+" = vm.stack[base+"+(a+3)+"];"+ // next key
    					"\n\t\ts"+(a+4)+" = vm.stack[base+"+(a+4)+"];"+ // next value
    					"\n\t\tif ( s"+(a+3)+".isNil() )"+
    					"\n\t\t\tbreak;"+
    					"\n\t\ts"+(a+2)+" = s"+(a+3)+";" ); // save next key        				
    			}
   			} 
    		
    		else if ( isforwardjump(i) ) {
    			
				// forward jump to loop bottom is a break
				int jmp = LuaState.GETARG_sBx(i);
   				if ( isloopbottom(code[pc+jmp]) ) {
        			s[pc] = append( s[pc], "if(true)break;" );
				} 
				
				// forward jump preceded by test is "if" block
				else if ( istest(code[pc-1]) ) {
        			s[pc] = append( s[pc], "{ /* IF */ " );
    				s[pc+jmp+1] = append( "} /* ENDIF */ ", s[pc+jmp+1]  );

    				// end of block preceded by forward jump is else clause
    				if ( isforwardjump(code[pc+jmp]) ) {
        				int jmp2 = LuaState.GETARG_sBx(code[pc+jmp]);
        				
    					// unless that jump is a break!
           				if ( ! isloopbottom(code[pc+jmp+jmp2]) ) {
	        				s[pc+jmp+1] = append( s[pc+jmp+1], "else { /* ELSE */ "  );
	        				s[pc+jmp+jmp2+1] = append( "}  /* ENDELSE */ ", s[pc+jmp+jmp2+1] );
           				}
    				}
				}				
			}
    	}
    	
		
        // find local variables, jump points
        return s;
    }
    
	private static void writeSource( PrintStream ps, String name, LPrototype p ) {
		
		int i, a, b, c, o;
        String bs, cs;

        int[] code = p.code;
        LValue[] k = p.k;
        String[] controlflow = extractControlFlow(code);
        
        // class header
		ps.print( 
				"import org.luaj.vm.*;\n"+
				"import org.luaj.jit.*;\n"+
				"import java.io.*;\n"+
				"\n"+
				"public class "+name+" extends JitPrototype {\n" );
		
		// static constants
		int nk = k.length;
		if ( nk > 0 ) {
			ps.print( "\tprivate LValue k0" ) ;
			for (int ik=1; ik<nk; ik++ )
				ps.print( ",k"+ik ) ;
			ps.println( ";" ) ;
			ps.println("	protected void setLuaPrototype(LPrototype lp) {\n" );
			ps.println("		super.setLuaPrototype(lp);\n" );
			ps.println( "		final LValue[] k = p.k;" ) ;
			for (int ik=0; ik<nk; ik++ ) 
				ps.println( "		k"+ik+" = k["+ik+"];" ) ;
			ps.println("	}" ) ;
		}
		
		// jit call
		ps.println( "\tpublic void jitCall(LuaState vm, LTable env, JitClosure jcl) {" );
		
		// parameters
		int ns = p.maxstacksize;
		int is = 0;
		if ( ! p.is_vararg ) {
			ps.println( "\t\tvm.checkstack("+(p.maxstacksize+1)+");" );
			ps.println( "\t\tvm.settop("+(p.numparams+1)+");");
			ps.println( "\t\tint base = vm.base + 1;" );
			for (; is<p.numparams; is++ ) 
				ps.println( "\t\tLValue s"+is+" = vm.stack[base+"+is+"];" );
		}
		for (; is<ns; is++ ) 
			ps.println( "\t\tLValue s"+is+" = LNil.NIL;" );
        ps.println("\t\tLClosure newcl;");
		ps.println("\t\tByteArrayOutputStream baos;");
		ps.println("\t\tLTable t;");
		ps.println();

		// save var args
		if ( p.is_vararg ) {
			ps.println( "\t\tint ncopy, ntotal;" );
			ps.println( "\t\tint nvarargs = vm.top - vm.base - 1;" );
			ps.println( "\t\tint base = vm.base + 1 + nvarargs;" );
		}
		ps.println();
		

        // loop until a return instruction is processed, 
        // or the vm yields
		for ( int pc=0; pc<code.length; pc++ ) {

			// print the instruction 
			ps.print( "\n\t\t// "); 
            Print.printOpCode(ps, p, pc);
            ps.println();
            if ( controlflow[pc] != null )
            	ps.println( "\t\t"+controlflow[pc] );
			
            // get instruction
            i = code[pc];

            // get opcode and first arg
        	o = (i >> POS_OP) & MAX_OP;
    		a = (i >> POS_A) & MAXARG_A;
        	
            switch (o) {
            default:
            	ps.println( "\t\tunimplemented();");
            	break;
            case LuaState.OP_MOVE: {
                b = LuaState.GETARG_B(i);
                ps.println( "\t\ts"+a+" = s"+b+";" );
                continue;
            }
            case LuaState.OP_LOADK: {
                b = LuaState.GETARG_Bx(i);
                ps.println( "\t\ts"+a+" = k"+b+";" );
                continue;
            }
            case LuaState.OP_LOADBOOL: {
//                b = LuaState.GETARG_B(i);
//                c = LuaState.GETARG_C(i);
//                this.stack[base + a] = (b != 0 ? LBoolean.TRUE : LBoolean.FALSE);
//                if (c != 0)
//                    ci.pc++; // skip next instruction (if C)
//                continue;
				b = LuaState.GETARG_B(i);
				c = LuaState.GETARG_C(i);
                ps.println( "\t\ts"+a+" = LBoolean."+(b!=0? "TRUE": "FALSE")+";" );
                if ( c != 0 )
                	throw new java.lang.UnsupportedOperationException("can't jit compile LOADBOOL with c != 0");
                break;
            }
            case LuaState.OP_LOADNIL: {
				b = LuaState.GETARG_B(i);
        		ps.print("\t\t");
            	for ( int j=a; j<=b; j++ )
            		ps.print("s"+j+"=");
        		ps.println("LNil.NIL;");
				break;
            }
            case LuaState.OP_GETUPVAL: {
				//b = LuaState.GETARG_B(i);
				//this.stack[base + a] = cl.upVals[b].getValue();
				//continue;
				b = LuaState.GETARG_B(i);
				ps.println("\t\t\ts"+a+" = jcl.upVals["+b+"].getValue();");
				break;
            }
            case LuaState.OP_GETGLOBAL: {
                b = LuaState.GETARG_Bx(i);
                ps.println("\t\ts"+a+" = vm.luaV_gettable(env, k"+b+");");
                break;
            }
            case LuaState.OP_GETTABLE: {
				b = GETARG_B(i);
				cs = GETARG_RKC_jit(i);
                ps.println("\t\ts"+a+" = vm.luaV_gettable(s"+b+", "+cs+");");
                break;
            }
            case LuaState.OP_SETGLOBAL: {
                b = LuaState.GETARG_Bx(i);
                ps.println("\t\tvm.luaV_settable(env, k"+b+", s"+a+");");
                break;
            }
            case LuaState.OP_SETUPVAL: {
				b = LuaState.GETARG_B(i);
				ps.println("\t\t\tjcl.upVals["+b+"].setValue(s"+a+");");
            	break;
            }
            case LuaState.OP_SETTABLE: {
				bs = GETARG_RKB_jit(i);
				cs = GETARG_RKC_jit(i);
				ps.println("\t\tvm.luaV_settable(s"+a+", "+bs+", "+cs+");");
				break;
            }
            case LuaState.OP_NEWTABLE: {
				b = GETARG_B(i);
				c = GETARG_C(i);
				ps.println("\t\ts"+a+" = new LTable("+b+","+c+");");
				break;
            }
            case LuaState.OP_SELF: {
				bs = GETARG_RKB_jit(i);
				cs = GETARG_RKC_jit(i);
                ps.println("\t\ts"+a+" = vm.luaV_gettable((s"+(a+1)+"="+bs+"), "+cs+");");
				break;
            }
            case LuaState.OP_ADD:
            case LuaState.OP_SUB:
            case LuaState.OP_MUL:
            case LuaState.OP_DIV:
            case LuaState.OP_MOD:
            case LuaState.OP_POW: {
				bs = GETARG_RKB_jit(i);
				cs = GETARG_RKC_jit(i);
				ps.println("\t\ts"+a+" = "+cs+".luaBinOpUnknown("+o+","+bs+");");
            	break;
            }
            case LuaState.OP_UNM: {
				bs = GETARG_RKB_jit(i);
				ps.println("\t\ts"+a+" = "+bs+".luaUnaryMinus();");
            }
            case LuaState.OP_NOT: {
				bs = GETARG_RKB_jit(i);
				ps.println("\t\ts"+a+" = ("+bs+".toJavaBoolean()? LBoolean.TRUE: LBoolean.FALSE);");
				break;
            }
            case LuaState.OP_LEN: {
				bs = GETARG_RKB_jit(i);
				ps.println("\t\ts"+a+" = LInteger.valueOf("+bs+".luaLength());");
            }
            case LuaState.OP_CONCAT: {
				b = LuaState.GETARG_B(i);
				c = LuaState.GETARG_C(i);
				ps.println("\t\tbaos = new ByteArrayOutputStream();");
				for (int j = b; j <= c; j++)
				    ps.println("\t\ts"+j+".luaConcatTo( baos );");
			    ps.println("\t\ts"+a+" = new LString( baos.toByteArray() );");
				ps.println("\t\tbaos = null;");
            	break;
            }
            case LuaState.OP_JMP: {
            	break;
            }
            case LuaState.OP_EQ:
            case LuaState.OP_LT:
            case LuaState.OP_LE: {
				bs = GETARG_RKB_jit(i);
				cs = GETARG_RKC_jit(i);
				ps.println( "\t\tif ( "+(a==0?"!":"")+" "+cs+".luaBinCmpUnknown("+o+", "+bs+") )" );
            	break;
            }
            case LuaState.OP_TEST: {
				c = LuaState.GETARG_C(i);
				ps.println( "\t\tif ( "+(c!=0?"!":"")+" s"+a+".toJavaBoolean() )" );
            	break;
            }
            case LuaState.OP_TESTSET: {
//                rkb = GETARG_RKB(k, i);
//                c = LuaState.GETARG_C(i);
//                if (rkb.toJavaBoolean() != (c != 0))
//                    ci.pc++;
//                else
//                    this.stack[base + a] = rkb;
//                continue;
				bs = GETARG_RKB_jit(i);
				c = LuaState.GETARG_C(i);
				ps.println( "\t\tif ( "+(c!=0?"!":"")+" "+bs+".toJavaBoolean() )" );
				ps.println( "\t\t\ts"+a+" = "+bs+";" );
				ps.println( "\t\telse" );
                break;
            }
            case LuaState.OP_CALL: {               
				//
				//// ra is base of new call frame
				//this.base += a;
				//
				//// number of args
				//b = LuaState.GETARG_B(i);
				//if (b != 0) // else use previous instruction set top
				//    top = base + b;
				//
				//// number of return values we need
				//c = LuaState.GETARG_C(i);
				//
				//// make or set up the call
				//this.nresults = c - 1;
				//if (this.stack[base].luaStackCall(this))
				//    return;
				//
				//// adjustTop only for case when call was completed
				//// and number of args > 0. If call completed but
				//// c == 0, leave top to point to end of results
				//if (c > 0)
				//    adjustTop(base + c - 1);
				//
				//// restore base
				//base = ci.base;
				//
				//continue;

				b = LuaState.GETARG_B(i);
				c = LuaState.GETARG_C(i);
				
            	// copy call to vm stack
				ps.println( "\t\tvm.stack[base+"+a+"] = s"+a+";" );
				
				// number of args
				if (b > 0) { // else use previous instruction set top
					for ( int j=1; j<b; j++ )
						ps.println( "\t\tvm.stack[base+"+(a+j)+"] = s"+(a+j)+";" );
					ps.println( "\t\tvm.top = base+"+(a+b)+";" );
					ps.println("\t\tvm.call("+(b-1)+","+(c-1)+");");
				} else {
					ps.println("\t\tvm.call(vm.top-base+"+(a-1)+","+(c-1)+");");
				}
				
				// copy results to local vars
				if ( c > 0 )
					for ( int j=0; j<c-1; j++ )
						ps.println( "\t\ts"+(a+j)+" = vm.stack[base+"+(a+j)+"];" );
            	
            	break;
            }
            /*
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
            */
            case LuaState.OP_RETURN: {
				//// number of return vals to return
				//b = LuaState.GETARG_B(i) - 1; 
				//if (b == -1)
				//    b = top - (base + a);
				//
				//// close open upvals
				//closeUpVals( base ); 
				//
				//// number to copy down
				//System.arraycopy(stack, base + a, stack, ci.resultbase, b);
				//top = ci.resultbase + b;
				//
				//// adjust results to what caller expected
				//if (ci.nresults >= 0)
				//    adjustTop(ci.resultbase + ci.nresults);
				//
				//// pop the call stack
				//--cc;
				//
				//// force a reload of the calling context
				//return;
				// number of return vals to return
            	if ( Lua.GET_OPCODE(code[pc-1]) == Lua.OP_RETURN )
            		break;

            	if ( p.is_vararg )
        			ps.println( "\t\tbase -= nvarargs;" );
            	else 
        			ps.println( "\t\tbase -= 1;" );

            	b = LuaState.GETARG_B(i); 
				if (b > 0) {
					for ( int j=1; j<b; j++ )
						ps.println( "\t\tvm.stack[base+"+(j-1)+"] = s"+(a+j-1)+";" );
					ps.println( "\t\tvm.top = base+"+(b-1)+";" );
				}
				ps.println( "\t\treturn;" );
				break;
            }
            case LuaState.OP_FORPREP: {
            	// do the test at the top, not the bottom of the loop
            	b = LuaState.GETARG_sBx(i);
				
            	// set up the loop variables
            	String init = "s"+(a);
            	String limit = "s"+(a+1);
            	String step = "s"+(a+2);
            	String idx = "s"+(a+3);
            	String back = "back"+pc;
            	ps.println( "\t\tboolean "+back+"="+step+".luaBinCmpInteger(Lua.OP_LT,0);");
            	ps.println( "\t\tfor ( "+idx+"="+init+";\n" +
            			"\t\t\t"+back+"? "+idx+".luaBinCmpUnknown(Lua.OP_LE, "+limit+"): "+limit+".luaBinCmpUnknown(Lua.OP_LE, "+idx+");\n" +
            			"\t\t\t"+idx+"="+idx+".luaBinOpUnknown(Lua.OP_ADD,"+step+") )\n" +
            			"\t\t{ /* FORLOOP */");
				break;
            }
            case LuaState.OP_FORLOOP: {
            	ps.println( "\t\t} /* LOOPBOT */");
            	break;
            }
            case LuaState.OP_TFORLOOP: {
				break;
            }
            case LuaState.OP_SETLIST: {
				b = LuaState.GETARG_B(i);
				c = LuaState.GETARG_C(i);
				if (c == 0)
				    c = code[++pc];
				int offset = (c-1) * LFIELDS_PER_FLUSH;
				if ( b == 0 ) {
					ps.println("\t\tt = (LTable) s"+(a)+";");
					ps.println("\t\tfor ( int j=0, nj=vm.top-base-"+(a+1)+"; j<nj; j++ )");
					ps.println("\t\t\tt.put("+offset+"+j,vm.stack[base+"+a+"+j]);");
				} else {
					ps.println("\t\tt = (LTable) s"+(a)+";");
					for (int j=1; j<=b; j++)
						ps.println("\t\tt.put("+(offset+j)+",s"+(a+j)+");");
				}
            	break;
            }
            /*
            case LuaState.OP_CLOSE: {
                closeUpVals( base + a ); // close upvals higher in the stack than position a
                continue;
            }
            */
            case LuaState.OP_CLOSURE: {
				//b = LuaState.GETARG_Bx(i);
				//proto = cl.p.p[b];
				//newClosure = proto.newClosure(cl.env);
				//for (int j = 0; j < newClosure.upVals.length; j++, ci.pc++) {
				//    i = code[ci.pc];
				//    o = LuaState.GET_OPCODE(i);
				//    b = LuaState.GETARG_B(i);
				//    if (o == LuaState.OP_GETUPVAL) {
				//        newClosure.upVals[j] = cl.upVals[b];
				//    } else if (o == LuaState.OP_MOVE) {
				//        newClosure.upVals[j] = findUpVal( base + b );
				//    } else {
				//        throw new java.lang.IllegalArgumentException(
				//                "bad opcode: " + o);
				//    }
				//}
				//this.stack[base + a] = newClosure;
				//continue;
				b = LuaState.GETARG_Bx(i);
				ps.println("\t\ts"+a+" = newcl = p.p["+b+"].newClosure(env);");
				for (int j = 0, nj=p.p[b].nups; j < nj; j++) {
				    i = code[++pc];
				    o = LuaState.GET_OPCODE(i);
				    b = LuaState.GETARG_B(i);
				    if (o == LuaState.OP_GETUPVAL) {
				        ps.println("\t\tnewcl.upVals["+j+"] = newcl.upVals["+b+"];");
				    } else if (o == LuaState.OP_MOVE) {
				        ps.println("\t\tnewcl.upVals["+j+"] = vm.findUpVal(base+"+b+");");
				    } else {
				        throw new java.lang.IllegalArgumentException("bad opcode: " + o);
				    }
				}
				break;
            }
            case LuaState.OP_VARARG: {
				//// figure out how many args to copy
				//b = LuaState.GETARG_B(i) - 1;
				//nvarargs = this.stack[base - 1];
				//n = nvarargs.toJavaInt();
				//if (b == LuaState.LUA_MULTRET) {
				//    b = n; // use entire varargs supplied
				//}
				//
				//// copy args up to call stack area
				//checkstack(a+b);
				//for (int j = 0; j < b; j++)
				//    this.stack[base + a + j] = (j < n ? this.stack[base
				//            - n + j - 1]
				//            : LNil.NIL);
				//top = base + a + b;
				//continue;
				b = LuaState.GETARG_B(i) - 1;
				if ( b == LuaState.LUA_MULTRET ) {
					ps.println( "\t\tncopy = ntotal = nvarargs;" );
				} else {
					ps.println( "\t\tncopy = Math.min(ntotal="+b+",nvarargs);" );
				}
				ps.println( "\t\tSystem.arraycopy(vm.stack,base-nvarargs,vm.stack,base+"+a+",ncopy);" );
				ps.println( "\t\tfor (int j = ncopy; j < ntotal; j++)" );
				ps.println( "\t\t\tvm.stack[base+j] = LNil.NIL;" );
				ps.println( "\t\tvm.top = base+ntotal+"+(a)+";" );
            	break;
            }            
            }
		}
        ps.print( 	
			"	}\n"+
			"}\n" );
    }
}
