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
package org.luaj.vm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.luaj.lib.BaseLib;
import org.luaj.lib.CoroutineLib;
import org.luaj.lib.MathLib;
import org.luaj.lib.PackageLib;
import org.luaj.lib.StringLib;
import org.luaj.lib.TableLib;

/**
 * <hr>
 * <h3><a name="LuaState"><code>LuaState</code></a></h3>
 * 
 * <pre>
 * typedef struct LuaState;
 * </pre>
 * 
 * <p>
 * Opaque structure that keeps the whole state of a Lua interpreter. The Lua
 * library is fully reentrant: it has no global variables. All information about
 * a state is kept in this structure.
 * 
 * 
 * <p>
 * Here we list all functions and types from the C&nbsp;API in alphabetical
 * order. Each function has an indicator like this: <span class="apii">[-o, +p,
 * <em>x</em>]</span>
 * 
 * <p>
 * The first field, <code>o</code>, is how many elements the function pops
 * from the stack. The second field, <code>p</code>, is how many elements the
 * function pushes onto the stack. (Any function always pushes its results after
 * popping its arguments.) A field in the form <code>x|y</code> means the
 * function may push (or pop) <code>x</code> or <code>y</code> elements,
 * depending on the situation; an interrogation mark '<code>?</code>' means
 * that we cannot know how many elements the function pops/pushes by looking
 * only at its arguments (e.g., they may depend on what is on the stack). The
 * third field, <code>x</code>, tells whether the function may throw errors: '<code>-</code>'
 * means the function never throws any error; '<code>m</code>' means the
 * function may throw an error only due to not enough memory; '<code>e</code>'
 * means the function may throw other kinds of errors; '<code>v</code>'
 * means the function may throw an error on purpose.
 * 
 * 
 */
public class LuaState extends Lua {
    /* thread status; 0 is OK */
    public static final int LUA_YIELD  = 1;
    public static final int LUA_ERRRUN = 2;
    public static final int LUA_ERRSYNTAX  = 3;
    public static final int LUA_ERRMEM = 4;
    public static final int LUA_ERRERR = 5;

    private static final int LUA_MINSTACK = 20;
    private static final int LUA_MINCALLS = 10;    
    private static final int MAXTAGLOOP	= 100;

    public int base = 0;
    public int top = 0;
    protected int nresults = -1;
    public LValue[] stack = new LValue[LUA_MINSTACK];
    public int cc = -1;
    public CallInfo[] calls = new CallInfo[LUA_MINCALLS];
    protected Stack upvals = new Stack();
	protected LFunction panic;
    
	static LuaState mainState;
    public LTable _G;

    // main debug hook, overridden by DebugStackState
    protected void debugHooks(int pc) {
    }
    protected void debugAssert(boolean b) {
    }
    
    
    // ------------------- constructors ---------------------
	/**
	 * Creates a new, independent LuaState instance. <span class="apii">[-0, +0,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns <code>NULL</code> if cannot create the state (due to lack of
	 * memory). The argument <code>f</code> is the allocator function; Lua
	 * does all memory allocation for this state through this function. The
	 * second argument, <code>ud</code>, is an opaque pointer that Lua simply
	 * passes to the allocator in every call.
	 */
	protected LuaState() {
		_G = new LTable();
		mainState = this;
	}

	/** 
	 * Create a LuaState with a specific global environment. Used by LThread.
	 * 
	 * @param globals the LTable to use as globals for this LuaState  
	 */
	LuaState(LTable globals) {
		_G = globals;
	}
	
	/**
	 * Performs the initialization.
	 */
	public void init() {}
	
	/**
	 * Perform any shutdown/clean up tasks if needed
	 */
	public void shutdown() {}
	
	/**
	 * Install the standard set of libraries used by most implementations:
	 * BaseLib, CoroutineLib, MathLib, PackageLib, TableLib, StringLib
	 */
	public void installStandardLibs() {
        PackageLib.install(_G);
		BaseLib.install(_G);
        CoroutineLib.install(_G);
        MathLib.install(_G);
        TableLib.install(_G);
        StringLib.install(_G);	
	}
    
    // ================ interfaces for performing calls
    
	/**
	 * Create a call frame for a call that has been set up on
	 * the stack. The first value on the stack must be a Closure,
	 * and subsequent values are arguments to the closure.
	 */ 
    public void prepStackCall() {
        LClosure c = (LClosure) stack[base];
        int resultbase = base;
        // Expand the stack if necessary
        checkstack( c.p.maxstacksize );
        if ( c.p.is_vararg == 0 ) {
            base += 1;
            luaV_adjusttop( base+c.p.numparams );
        } else {
            /* vararg function */
            int npar = c.p.numparams;
            int narg = Math.max(0, top - base - 1);
            int nfix = Math.min(narg, npar);
            int nvar = Math.max(0, narg-nfix);
            
            
            // must copy args into position, add number parameter
            stack[top] = LInteger.valueOf(nvar);
            System.arraycopy(stack, base+1, stack, top+1, nfix);
            base = top + 1;
            top = base + nfix;
            luaV_adjusttop( base + npar );
            
            // add 'arg' compatibility variable
            if ( (c.p.is_vararg & VARARG_NEEDSARG) != 0 ) {
            	LTable arg = new LTable();
            	for ( int i=1,j=base-nvar-1; i<=nvar; i++, j++ )
            		arg.put(i, stack[j]);
            	arg.put("n", nvar);
            	pushlvalue( arg );
            }
        }
        final int newcc = cc + 1;
        if ( newcc >= calls.length ) {
            CallInfo[] newcalls = new CallInfo[ calls.length * 2 ];
            System.arraycopy( calls, 0, newcalls, 0, cc+1 );
            calls = newcalls;
        }
        calls[newcc] = new CallInfo(c, base, top, resultbase, nresults);
        cc = newcc;
        
        stackClear( top, base + c.p.maxstacksize );        
    }
    
	/**
	 * Execute bytecodes until the current call completes
	 * or the vm yields.
	 */
    public void execute() {
        for ( int cb=cc; cc>=cb; )
            exec();
    }
    
	/**
	 * Put the closure on the stack with arguments, 
	 * then perform the call.   Leave return values 
	 * on the stack for later querying. 
	 * 
	 * @param c
	 * @param values
	 */
    public void doCall( LClosure c, LValue[] args ) {
    	settop(0);
        pushlvalue( c );
        for ( int i=0, n=(args!=null? args.length: 0); i<n; i++ )
            pushlvalue( args[i] );
        prepStackCall();
        execute();
        base = (cc>=0? calls[cc].base: 0);
    }

	/** 
	 * Invoke a LFunction being called via prepStackCall()
	 * @param javaFunction
	 */
	public void invokeJavaFunction(LFunction javaFunction) {
		int resultbase = base;
		int resultsneeded = nresults;
		++base;
		int nactual = javaFunction.invoke(this);
		debugAssert(nactual>=0);
		debugAssert(top-nactual>=base);
		System.arraycopy(stack, top-nactual, stack, base=resultbase, nactual);
		settop( nactual );
		if ( resultsneeded >= 0 )
			settop( resultsneeded );
	}
	
    // ================== error processing =================
    
	/**
	 * Calls a function. <span class="apii">[-(nargs + 1), +nresults, <em>e</em>]</span>
	 * 
	 * 
	 * <p>
	 * To call a function you must use the following protocol: first, the
	 * function to be called is pushed onto the stack; then, the arguments to
	 * the function are pushed in direct order; that is, the first argument is
	 * pushed first. Finally you call <a href="#lua_call"><code>lua_call</code></a>;
	 * <code>nargs</code> is the number of arguments that you pushed onto the
	 * stack. All arguments and the function value are popped from the stack
	 * when the function is called. The function results are pushed onto the
	 * stack when the function returns. The number of results is adjusted to
	 * <code>nresults</code>, unless <code>nresults</code> is <a
	 * name="pdf-LUA_MULTRET"><code>LUA_MULTRET</code></a>. In this case,
	 * <em>all</em> results from the function are pushed. Lua takes care that
	 * the returned values fit into the stack space. The function results are
	 * pushed onto the stack in direct order (the first result is pushed first),
	 * so that after the call the last result is on the top of the stack.
	 * 
	 * 
	 * <p>
	 * Any error inside the called function is propagated upwards (with a
	 * <code>longjmp</code>).
	 * 
	 * 
	 * <p>
	 * The following example shows how the host program may do the equivalent to
	 * this Lua code:
	 * 
	 * <pre>
	 * a = f(&quot;how&quot;, t.x, 14)
	 * </pre>
	 * 
	 * <p>
	 * Here it is in&nbsp;C:
	 * 
	 * <pre>
	 * lua_getfield(L, LUA_GLOBALSINDEX, &quot;f&quot;); // function to be called 
	 * lua_pushstring(L, &quot;how&quot;); // 1st argument 
	 * lua_getfield(L, LUA_GLOBALSINDEX, &quot;t&quot;); // table to be indexed 
	 * lua_getfield(L, -1, &quot;x&quot;); // push result of t.x (2nd arg) 
	 * lua_remove(L, -2); // remove 't' from the stack 
	 * lua_pushinteger(L, 14); // 3rd argument 
	 * lua_call(L, 3, 1); // call 'f' with 3 arguments and 1 result 
	 * lua_setfield(L, LUA_GLOBALSINDEX, &quot;a&quot;); // set global 'a' 
	 * </pre>
	 * 
	 * <p>
	 * Note that the code above is "balanced": at its end, the stack is back to
	 * its original configuration. This is considered good programming practice.
	 */
    public void call( int nargs, int nreturns ) {
        // save stack state
        int oldbase = base;
        try {
	
	        // rb is base of new call frame
	        int rb = this.base = top - 1 - nargs;
	
	        // make or set up the call
	        this.nresults = nreturns;
	        if (this.stack[base].luaStackCall(this)) {
	            // call was set up on the stack, 
	            // we still have to execute it
	            execute();
	        }
	        
	        // adjustTop only for case when call was completed
	        // and number of args > 0.  If call completed but 
	        // c == 0, leave top to point to end of results
	        if (nreturns >= 0)
	        	luaV_adjusttop(rb + nreturns);
	        
        } finally {
        	this.base = oldbase;
        }
    }

	/**
	 * Calls a function in protected mode. <span class="apii">[-(nargs + 1),
	 * +(nresults|1), <em>-</em>]</span>
	 * 
	 * 
	 * <p>
	 * Both <code>nargs</code> and <code>nresults</code> have the same
	 * meaning as in <a href="#lua_call"><code>lua_call</code></a>. If there
	 * are no errors during the call, <a href="#lua_pcall"><code>lua_pcall</code></a>
	 * behaves exactly like <a href="#lua_call"><code>lua_call</code></a>.
	 * However, if there is any error, <a href="#lua_pcall"><code>lua_pcall</code></a>
	 * catches it, pushes a single value on the stack (the error message), and
	 * returns an error code. Like <a href="#lua_call"><code>lua_call</code></a>,
	 * <a href="#lua_pcall"><code>lua_pcall</code></a> always removes the
	 * function and its arguments from the stack.
	 * 
	 * 
	 * <p>
	 * If <code>errfunc</code> is 0, then the error message returned on the
	 * stack is exactly the original error message. Otherwise,
	 * <code>errfunc</code> is the stack index of an
	 * <em>error handler function</em>. (In the current implementation, this
	 * index cannot be a pseudo-index.) In case of runtime errors, this function
	 * will be called with the error message and its return value will be the
	 * message returned on the stack by <a href="#lua_pcall"><code>lua_pcall</code></a>.
	 * 
	 * 
	 * <p>
	 * Typically, the error handler function is used to add more debug
	 * information to the error message, such as a stack traceback. Such
	 * information cannot be gathered after the return of <a href="#lua_pcall"><code>lua_pcall</code></a>,
	 * since by then the stack has unwound.
	 * 
	 * 
	 * <p>
	 * The <a href="#lua_pcall"><code>lua_pcall</code></a> function returns
	 * 0 in case of success or one of the following error codes (defined in
	 * <code>lua.h</code>):
	 * 
	 * <ul>
	 * 
	 * <li><b><a name="pdf-LUA_ERRRUN"><code>LUA_ERRRUN</code></a>:</b> a
	 * runtime error. </li>
	 * 
	 * <li><b><a name="pdf-LUA_ERRMEM"><code>LUA_ERRMEM</code></a>:</b>
	 * memory allocation error. For such errors, Lua does not call the error
	 * handler function. </li>
	 * 
	 * <li><b><a name="pdf-LUA_ERRERR"><code>LUA_ERRERR</code></a>:</b>
	 * error while running the error handler function. </li>
	 * 
	 * </ul>
	 */
    public int pcall( int nargs, int nreturns, int errfunc ) {
        // save stack state
        int oldtop = top;
        int oldbase = base;
        int oldcc = cc;
        try {
            // rb is base of new call frame
            int rb = this.base = top - 1 - nargs;

            // make or set up the call
            this.nresults = nreturns;
            if (this.stack[base].luaStackCall(this)) {
                // call was set up on the stack, 
                // we still have to execute it
                execute();
            }
            
            // adjustTop only for case when call was completed
            // and number of args > 0.  If call completed but 
            // c == 0, leave top to point to end of results
            if (nreturns >= 0)
            	luaV_adjusttop(rb + nreturns);
            
            // restore base
            this.base = oldbase;
            
            return 0;
        } catch ( Throwable t ) {
            this.base = oldbase;
            this.cc = oldcc;
            closeUpVals(oldtop);  /* close eventual pending closures */
            String s = t.getMessage();
            resettop();
            if ( s != null )
            	pushstring( s );
            else
            	pushnil();
            return (t instanceof OutOfMemoryError? LUA_ERRMEM: LUA_ERRRUN);
        }
    }

	/**
	 * Loads a Lua chunk. <span class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 * <p>
	 * If there are no errors, <a href="#lua_load"><code>lua_load</code></a>
	 * pushes the compiled chunk as a Lua function on top of the stack.
	 * Otherwise, it pushes an error message. The return values of <a
	 * href="#lua_load"><code>lua_load</code></a> are:
	 * 
	 * <ul>
	 * 
	 * <li><b>0:</b> no errors;</li>
	 * 
	 * <li><b><a name="pdf-LUA_ERRSYNTAX"><code>LUA_ERRSYNTAX</code></a>:</b>
	 * syntax error during pre-compilation;</li>
	 * 
	 * <li><b><a href="#pdf-LUA_ERRMEM"><code>LUA_ERRMEM</code></a>:</b>
	 * memory allocation error.</li>
	 * 
	 * </ul>
	 * 
	 * <p>
	 * This function only loads a chunk; it does not run it.
	 * 
	 * 
	 * <p>
	 * <a href="#lua_load"><code>lua_load</code></a> automatically detects
	 * whether the chunk is text or binary, and loads it accordingly (see
	 * program <code>luac</code>).
	 * 
	 * 
	 * <p>
	 * The <a href="#lua_load"><code>lua_load</code></a> function uses a
	 * user-supplied <code>reader</code> function to read the chunk (see <a
	 * href="#lua_Reader"><code>lua_Reader</code></a>). The
	 * <code>data</code> argument is an opaque value passed to the reader
	 * function.
	 * 
	 * 
	 * <p>
	 * The <code>chunkname</code> argument gives a name to the chunk, which is
	 * used for error messages and in debug information (see <a
	 * href="#3.8">&sect;3.8</a>).
	 */
    public int load( InputStream is, String chunkname ) {
        try {
            LPrototype p = LoadState.undump(this, is, chunkname );
            pushlvalue( p.newClosure( _G ) );
            return 0;
        } catch ( Throwable t ) {
            pushstring( t.getMessage() ); 
            return (t instanceof OutOfMemoryError? LUA_ERRMEM: LUA_ERRSYNTAX);
        }
    }
    
    // ================ execute instructions
    private LValue RKBC(LValue[] k, int bc) {
        return LuaState.ISK(bc) ? 
                k[LuaState.INDEXK(bc)]:
                stack[base + bc];
    }

    private LValue GETARG_RKB(LValue[] k, int i) {
        return RKBC(k, GETARG_B(i));
    }

    private LValue GETARG_RKC(LValue[] k, int i) {
        return RKBC(k, GETARG_C(i));
    }

	private final void stackClear(int startIndex, int endIndex) {
		for (; startIndex < endIndex; startIndex++) {
			stack[startIndex] = LNil.NIL;
		}
	}    

    /** execute instructions up to a yield, return, or call */
    public void exec() {
        if ( cc < 0 )
            return;

        int i, a, b, c, o, n, cb;
        LValue rkb, rkc, nvarargs, key, val;
        LValue i0, step, idx, limit, init, table;
        boolean back, body;
        LPrototype proto;
        LClosure newClosure;

        // reload values from the current call frame 
        // into local variables
        CallInfo ci = calls[cc];
        LClosure cl = ci.closure;
        LPrototype p = cl.p;
        int[] code = p.code;
        LValue[] k = p.k;
        
        this.base = ci.base;
        
        // loop until a return instruction is processed, 
        // or the vm yields
        while (true) {
            debugAssert( ci == calls[cc] );
    
            // sync up top
            ci.top = top;
        	
            // allow debug hooks a chance to operate
            debugHooks( ci.pc );
            
            // advance program counter
            i = code[ci.pc++];
            
            // get opcode and first arg
        	o = (i >> POS_OP) & MAX_OP;
    		a = (i >> POS_A) & MAXARG_A;
        	
            switch (o) {
            case LuaState.OP_MOVE: {
                b = LuaState.GETARG_B(i);
                this.stack[base + a] = this.stack[base + b];
                continue;
            }
            case LuaState.OP_LOADK: {
                b = LuaState.GETARG_Bx(i);
                this.stack[base + a] = k[b];
                continue;
            }
            case LuaState.OP_LOADBOOL: {
                b = LuaState.GETARG_B(i);
                c = LuaState.GETARG_C(i);
                this.stack[base + a] = (b != 0 ? LBoolean.TRUE : LBoolean.FALSE);
                if (c != 0)
                    ci.pc++; /* skip next instruction (if C) */
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
            case LuaState.OP_GETGLOBAL: {
                b = LuaState.GETARG_Bx(i);
                key = k[b];
                table = cl.env;
                val = this.luaV_gettable(table, key);
                this.stack[base + a] = val;
                continue;
            }
            case LuaState.OP_GETTABLE: {
                b = LuaState.GETARG_B(i);
                key = GETARG_RKC(k, i);
                table = this.stack[base + b];
                val = this.luaV_gettable(table, key);
                this.stack[base + a] = val;
                continue;
            }
            case LuaState.OP_SETGLOBAL: {
                b = LuaState.GETARG_Bx(i);
                key = k[b];
                val = this.stack[base + a];
                table = cl.env;
                this.luaV_settable(table, key, val);
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
                this.luaV_settable(table, key, val);
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
                val = this.luaV_gettable(rkb, rkc);
                this.stack[base + a] = val;
                this.stack[base + a + 1] = rkb;
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
                    luaV_settop_fillabove( base + b );
                
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
                	luaV_adjusttop(base + c - 1);
                
                // restore base
                base = ci.base;
                
                continue;
            }
            
            case LuaState.OP_TAILCALL: {
                closeUpVals(base);

                // copy down the frame before calling!

                // number of args (including the function)
                b = LuaState.GETARG_B(i);
                if (b != 0) // else use previous instruction set top
                    luaV_settop_fillabove( base + a + b );
                else
                    b = top - (base + a);

                // copy call + all args, discard current frame
                System.arraycopy(stack, base + a, stack, ci.resultbase, b);
                this.base = ci.resultbase;
                luaV_settop_fillabove( base + b );
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
                if (ci.nresults >= 0)
                	luaV_adjusttop(base + ci.nresults);

                // force restore of base, etc.
                return;
            }

            case LuaState.OP_RETURN: {
                closeUpVals( base ); 

                // number of return vals to return
                b = LuaState.GETARG_B(i) - 1; 
                if (b >= 0) // else use previous instruction set top
                    luaV_settop_fillabove( base + a + b );
                else
                    b = top - (base + a);

                // number to copy down
                System.arraycopy(stack, base + a, stack, ci.resultbase, b);
                debugAssert( ci.resultbase + b <= top );
                luaV_settop_fillabove( ci.resultbase + b );

                // adjust results to what caller expected
                if (ci.nresults >= 0)
                	luaV_adjusttop(ci.resultbase + ci.nresults);

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
                    ci.pc += LuaState.GETARG_sBx(i);
                }
                continue;
            }
            case LuaState.OP_FORPREP: {
                init = this.stack[base + a].luaToNumber();
                limit = this.stack[base + a + 1].luaToNumber();
                step = this.stack[base + a + 2].luaToNumber();
                if ( init.isNil() ) error("'for' initial value must be a number");
                if ( limit.isNil() ) error("'for' limit must be a number");
                if ( step.isNil() ) error("'for' step must be a number");
                this.stack[base + a] = step.luaBinOpUnknown(Lua.OP_SUB, init);
                this.stack[base + a + 1] = limit;
                this.stack[base + a + 2] = step;
                b = LuaState.GETARG_sBx(i);
                ci.pc += b;
                continue;
            }
            case LuaState.OP_TFORLOOP: {
            	cb = base + a + 3; /* call base */
            	base = cb;
                System.arraycopy(this.stack, cb-3, this.stack, cb, 3);
                luaV_settop_fillabove( cb + 3 );
                
                // call the iterator
                c = LuaState.GETARG_C(i);
                this.nresults = c;
                if (this.stack[cb].luaStackCall(this))
                    execute();
                base = ci.base;
                luaV_adjusttop( cb + c );
                
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
                tbl.arrayPresize( offset + b );
                for (int j=1; j<=b; j++) {
                    tbl.put(offset+j, stack[listBase + j]);
                }
                continue;
            }
            case LuaState.OP_CLOSE: {
                closeUpVals( base + a ); // close upvals higher in the stack than position a
                continue;
            }
            case LuaState.OP_CLOSURE: {
                b = LuaState.GETARG_Bx(i);
                proto = cl.p.p[b];
                newClosure = proto.newClosure(cl.env);
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
                luaV_settop_fillabove( base + a + b );
                continue;
            }            
            }
        }   
    }
    
    public UpVal findUpVal( int target ) {
        UpVal up;
        int i;
        for ( i = this.upvals.size() - 1; i >= 0; --i ) {
            up = (UpVal) this.upvals.elementAt( i );
            if ( up.state == this && up.position == target ) {
                return up;
            } else if ( up.position < target ) {
                break;
            }
        }
        
        up = new UpVal( this, target );
        this.upvals.insertElementAt( up, i + 1 );
        return up;
    }
    
    public void closeUpVals( int limit ) {
        while ( !upvals.empty() && ( (UpVal) this.upvals.lastElement() ).close( limit ) ) {
            this.upvals.pop();
        }
    }

    public CallInfo getStackFrame(int callStackDepth) {
        return calls[cc-callStackDepth];
    }
    
    private void indexError(LValue nontable) {
		error( "attempt to index ? (a "+nontable.luaGetTypeName()+" value)", 1 );
	}
    
    public static LValue luaV_getmetafield(LValue t, LString tag) {
    	LTable mt = t.luaGetMetatable();
    	if ( mt == null )
    		return null;
    	LValue h = mt.get(tag);
    	return h.isNil()? null: h;
    }

    /** Get a key from a table using full metatable processing */
    public LValue luaV_gettable(LValue table, LValue key) {
    	LValue h=LNil.NIL,t=table;
    	for ( int loop=0; loop<MAXTAGLOOP; loop++ ) {
    		if ( t.isTable() ) {
    			LValue v = ((LTable) t).get(key);
    			if ( !v.isNil() ) {
    				return v;
    			}
    			h = luaV_getmetafield(t, LTable.TM_INDEX);
    			if ( h == null ) {
    				return v;
    			}
    		} else {
    			h = luaV_getmetafield(t, LTable.TM_INDEX);
    			if ( h == null ) {
    				indexError(t);
    			}
    		}
       	    if (h.isFunction()) {
       	    	return ((LFunction)h).__index(this, table, key);
       	    }
       	    t = h;
    	}
      	error("loop in gettable");
      	return LNil.NIL;
   	}    
    
    /** Get a key from a table using full metatable processing */
    public void luaV_settable(LValue table, LValue key, LValue val) {
        if ( key.isNil() )
        	this.error("table index is nil");
    	LValue h=LNil.NIL,t=table;
    	for ( int loop=0; loop<MAXTAGLOOP; loop++ ) {
    		if ( t.isTable() ) {
    			LTable lt = (LTable) t;
    			if ( lt.containsKey(key) ) {
    				lt.put(key, val);
    				return;
    			}
    			h = luaV_getmetafield(t, LTable.TM_NEWINDEX);
    			if ( h == null ) {
    				lt.put(key, val);
    				return;
    			}
    		} else {
    			h = luaV_getmetafield(t, LTable.TM_NEWINDEX);
    			if ( h == null ) {
    				indexError(t);
    			}
    		}
       	    if (h.isFunction()) {
       	    	((LFunction)h).__newindex(this, table, key, val);
       	    	return;
       	    }
       	    t = h;
    	}
      	error("loop in settable");
   	}    

    /** Move top, and fill in both directions */
    private void luaV_adjusttop(int newTop) {
        while (top < newTop)
            this.stack[top++] = LNil.NIL;
        while (top > newTop)
            this.stack[--top] = LNil.NIL;
    }

    /** Move top down, filling from above */
    private void luaV_settop_fillabove(int newTop) {
        while (top > newTop)
            this.stack[--top] = LNil.NIL;
        top = newTop;
    }
	
    
    //===============================================================
    //              Lua Java API
    //===============================================================
    
    /**
     * Returns the current program counter for the given call frame.
     * @param ci -- A call frame
     * @return the current program counter for the given call frame.
     */
    protected int getCurrentPc(CallInfo ci) {
        int pc = ci.pc;
        return pc > 0 ? pc - 1 : 0;
    }

    protected String getSourceFileName(LString source) {
        String sourceStr = LoadState.getSourceName(source.toJavaString());
        return getSourceFileName(sourceStr);
    }

    protected String getSourceFileName(String sourceStr) {
        if (!LoadState.SOURCE_BINARY_STRING.equals(sourceStr)) {
            sourceStr = sourceStr.replace('\\', '/');
        }

        int index = sourceStr.lastIndexOf('/');
        if (index != -1) {
            sourceStr = sourceStr.substring(index + 1);
        }

        return sourceStr;
    }
    
    /** 
     * Get the file line number info for a particular call frame.
     * @param cindex index into call stack
     * @return
     */
    protected String getFileLine(int cindex) {
        String source = "?";
        String line = "";
        if (cindex >= 0 && cindex <= cc) {
            CallInfo call = this.calls[cindex];
            LPrototype p = call.closure.p;
            if (p != null && p.source != null)
                source = getSourceFileName(p.source);
            int pc = getCurrentPc(call);
            if (p.lineinfo != null && p.lineinfo.length > pc)
                line = ":" + String.valueOf(p.lineinfo[pc]);
        }
        return source + line;
    }
    
    public String getStackTrace() {
        StringBuffer buffer = new StringBuffer("Stack Trace:\n");
        for (int i = cc; i >= 0; i--) {
            buffer.append("   ");
            buffer.append(getFileLine(i));
            buffer.append("\n");
        }
        return buffer.toString();
    }
    
    /**
	 * Raises an error.   The message is pushed onto the stack and used as the error message.  
	 * It also adds at the beginning of the message the file name and the line number where 
	 * the error occurred, if this information is available.
	 * 
	 * In the java implementation this throws a LuaErrorException 
	 * after filling line number information first when level > 0.
	 */
    public void error(String message, int level) {
        throw new LuaErrorException( this, message, level );
    }
    
	/**
	 * Raises an error with the default level.
	 */
    public void error(String message) {
    	throw new LuaErrorException( this, message, 1 );
    }

	/**
	 * 
	 * Ensures that there are at least <code>extra</code> free stack slots in
	 * the stack. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * It returns false if it cannot grow the stack to that size. This function
	 * never shrinks the stack; if the stack is already larger than the new
	 * size, it is left unchanged.
	 * 
	 */
	public void checkstack(int extra) {
		if ( top + extra >= stack.length ) {
			int n = Math.max( top + extra + LUA_MINSTACK, stack.length * 2 );
			LValue[] s = new LValue[n];
			System.arraycopy(stack, 0, s, 0, stack.length);
			stack = s;			
		}
	}
	
	/**
	 * Dereference a tables field. <span class="apii">[-0, +1, <em>e</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the value <code>t[k]</code>, where
	 * <code>t</code> is the value at the given valid index. As in Lua, this
	 * function may trigger a metamethod for the "index" event (see <a
	 * href="#2.8">&sect;2.8</a>).
	 * 
	 */
	public void getfield(int index, LString k) {
		pushlvalue( this.luaV_gettable(topointer(index), k) );
	}
	
	/**
	 * Look up a global value. <span class="apii">[-0, +1, <em>e</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the value of the global <code>name</code>. It is
	 * defined as a macro:
	 * 
	 * <pre>
	 * 	 #define lua_getglobal(L,s)  lua_getfield(L, LUA_GLOBALSINDEX, s)
	 * 	
	 * </pre>
	 */
	public void getglobal(String s) {
		pushlvalue( this.luaV_gettable(_G, new LString(s)) );
	}
	
	/**
	 * Get a value's metatable. <span class="apii">[-0, +(0|1), <em>-</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the metatable of the value at the given acceptable
	 * index. If the index is not valid, or if the value does not have a
	 * metatable, the function returns false and pushes nothing on the stack.
	 * 
	 * @return true if the metatable was pushed onto the stack, false otherwise
	 */
	public boolean getmetatable(int index) {
		LTable mt = topointer(index).luaGetMetatable();
		if ( mt != null ) {
			pushlvalue( mt );
			return true;			
		}
		return false;
	}
	
	/**
	 * Insert the top item somewhere in the stack. <span class="apii">[-1, +1,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Moves the top element into the given valid index, shifting up the
	 * elements above this index to open space. Cannot be called with a
	 * pseudo-index, because a pseudo-index is not an actual stack position.
	 * 
	 */
	public void insert(int index) {
		int ai = index2adr(index);
		LValue v = stack[top-1];
		System.arraycopy(stack, ai, stack, ai+1, top-ai-1);
		stack[ai] = v;
	}
	
	/**
	 * Test if a value is boolean. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index has type boolean,
	 * and 0&nbsp;otherwise.
	 * 
	 */
	public boolean isboolean(int index) {
		return type(index) == Lua.LUA_TBOOLEAN;
	}
	
	/**
	 * Test if a value is a function. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns true if the value at the given acceptable index is a function
	 * (either C or Lua), and false&nbsp;otherwise.
	 * 
	 */
	public boolean isfunction(int index) {
		return type(index) == Lua.LUA_TFUNCTION;
	}

	/**
	 * Test if a value is nil <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is <b>nil</b>, and
	 * 0&nbsp;otherwise.
	 */
	public boolean isnil(int index) {
		return topointer(index).isNil();
	}

	/**
	 * Test if a value is nil or not valid <span class="apii">[-0, +0,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the the given acceptable index is not valid (that is, it
	 * refers to an element outside the current stack) or if the value at this
	 * index is <b>nil</b>, and 0&nbsp;otherwise.
	 */
	public boolean isnoneornil(int index) {
		Object v = topointer(index);
		return v == null || v == LNil.NIL;
	}

	/**
	 * Test if a value is a number <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a number or a
	 * string convertible to a number, and 0&nbsp;otherwise.
	 */
	public boolean isnumber(int index) {
		return ! tolnumber(index).isNil();
	}

	/** 
	 * Convert a value to an LNumber<span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns an LNumber if the value at the given acceptable index is a number or a
	 * string convertible to a number, and LNil.NIL&nbsp;otherwise.
	 */
	public LValue tolnumber(int index) {
		return topointer(index).luaToNumber();
	}
	
	/**
	 * Test if a value is a string <span class="apii">[-0, +0, <em>m</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a string or a
	 * number (which is always convertible to a string), and 0&nbsp;otherwise.
	 */
	public boolean isstring(int index) {
		return topointer(index).isString();
	}

	/**
	 * Test if a value is a table <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a table, and
	 * 0&nbsp;otherwise.
	 */
	public boolean istable(int index) {
		return topointer(index).isTable();
	}

	/**
	 * Test if a value is a thread <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a thread, and
	 * 0&nbsp;otherwise.
	 */
	public boolean isthread(int index) {
		return type(index) == Lua.LUA_TTHREAD;
	}

	/**
	 * Test if a value is a userdata <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a userdata
	 * (either full or light), and 0&nbsp;otherwise.
	 */
	public boolean isuserdata(int index) {
		return type(index) == Lua.LUA_TUSERDATA;
	}

	/**
	 * Pops <code>n</code> elements from the stack. <span class="apii">[-n,
	 * +0, <em>-</em>]</span>
	 */
	public void pop(int n) {
		for ( int i=0; i<n; i++ )
			stack[--top] = LNil.NIL;
	}
	
	public LValue poplvalue() {
		LValue p = stack[--top];
		stack[top] = LNil.NIL;
		return p;
		
	}
	
	/** 
	 * Push an LValue onto the stack. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 */
	public void pushlvalue(LValue value) {
		if ( value == null )
			throw new java.lang.IllegalArgumentException("stack values cannot be null");
		try {
			stack[top] = value;
		} catch ( java.lang.ArrayIndexOutOfBoundsException aiobe ) {
			checkstack( LUA_MINSTACK );
			stack[top] = value;
		} finally {
			++top;
		}
	}

	/**
	 * Pushes a boolean value with value <code>b</code> onto the stack. <span
	 * class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 */
	public void pushboolean(boolean b) {
		pushlvalue( LBoolean.valueOf(b) );
	}

	/**
	 * Pushes a number with value <code>n</code> onto the stack. <span
	 * class="apii">[-0, +1, <em>-</em>]</span>
	 */
	public void pushinteger(int n) {
		pushlvalue( LInteger.valueOf(n) );
	}

	/**
	 * Pushes a function onto the stack. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * This function receives an LFunction and pushes onto the
	 * stack a Lua value of type <code>function</code> that, when called,
	 * invokes the corresponding function.
	 * 
	 * 
	 * <p>
	 * Any function to be registered in Lua must follow the correct protocol to
	 * receive its parameters and return its results 
	 * @see LFunction
	 */
	public void pushfunction(LFunction f) {
		pushlvalue( f );
	}


	/** 
	 * Push an LString onto the stack. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 */
	public void pushlstring(LString s) {
		pushlvalue(s);
	}
	
	/**
	 * Push string bytes onto the stack as a string. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 * 
	 * Pushes the string pointed to by <code>s</code> with size
	 * <code>len</code> onto the stack. Lua makes (or reuses) an internal copy
	 * of the given string, so the memory at <code>s</code> can be freed or
	 * reused immediately after the function returns. The string can contain
	 * embedded zeros.
	 */
	public void pushlstring(byte[] bytes, int offset, int length) {
		pushlvalue(new LString(bytes, offset, length));
	}

	/**
	 * Push string bytes onto the stack as a string. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 * 
	 * Pushes the bytes in byteArray onto the stack as a lua string. 
	 */
	public void pushlstring(byte[] byteArray) {
		pushlstring(byteArray, 0, byteArray.length);
	}
	
	/**
	 * Pushes a nil value onto the stack. <span class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 */
	public void pushnil() {
		pushlvalue(LNil.NIL);
	}

	/**
	 * Pushes a number with value <code>d</code> onto the stack. <span
	 * class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 */
	public void pushnumber(double d) {
		pushlvalue(new LDouble(d));
	}

	/**
	 * Push a String onto the stack. <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * Pushes the String <code>s</code> onto the stack. Lua makes (or reuses)
	 * an internal copy of the given string, so the memory at <code>s</code>
	 * can be freed or reused immediately after the function returns. The string
	 * cannot contain embedded zeros; it is assumed to end at the first zero.
	 */
	public void pushstring(String s) {
		if ( s == null )
			pushnil();
		else
			pushlstring( LString.valueOf(s) );
	}

	/**
	 * Push a value from the stack onto the stack. <span class="apii">[-0, +1,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Pushes a copy of the element at the given valid index onto the stack.
	 */
	public void pushvalue(int index) {
		pushlvalue(topointer(index));
	}

	/**
	 * Do a integer-key table get without metadata calls. <span
	 * class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the value <code>t[n]</code>, where
	 * <code>t</code> is the value at the given valid index. The access is
	 * raw; that is, it does not invoke metamethods.
	 * @deprecated should get the table and do a raw get instead
	 */
	public void rawgeti(int index, int n) {
		pushlvalue( totable(index).get(n) );
	}

	/**
	 * Remove an element from the stack. <span class="apii">[-1, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Removes the element at the given valid index, shifting down the elements
	 * above this index to fill the gap. Cannot be called with a pseudo-index,
	 * because a pseudo-index is not an actual stack position.
	 */
	public void remove(int index) {
		int ai = index2adr(index);
		System.arraycopy(stack, ai+1, stack, ai, top-ai-1);
		poplvalue();
	}

	/**
	 * Replace an element on the stack. <span class="apii">[-1, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Moves the top element into the given position (and pops it), without
	 * shifting any element (therefore replacing the value at the given
	 * position).
	 */
	public void replace(int index) {
		int ai = index2adr(index);
		stack[ai] = poplvalue();
	}

	/**
	 * Set the value of a table field. <span class="apii">[-1, +0, <em>e</em>]</span>
	 * 
	 * <p>
	 * Does the equivalent to <code>t[k] = v</code>, where <code>t</code>
	 * is the value at the given valid index and <code>v</code> is the value
	 * at the top of the stack.
	 * 
	 * 
	 * <p>
	 * This function pops the value from the stack. As in Lua, this function may
	 * trigger a metamethod for the "newindex" event (see <a
	 * href="#2.8">&sect;2.8</a>).
	 */
	public void setfield(int index, LString k) {
		LTable t = totable(index);
		this.luaV_settable(t, k, poplvalue());
	}

	/**
	 * Set the value of a global variable. <span class="apii">[-1, +0,
	 * <em>e</em>]</span>
	 * 
	 * <p>
	 * Pops a value from the stack and sets it as the new value of global
	 * <code>name</code>. It is defined as a macro:
	 * 
	 * <pre>
	 * 	 #define lua_setglobal(L,s)   lua_setfield(L, LUA_GLOBALSINDEX, s)
	 * 	
	 * </pre>
	 */
	public void setglobal(String name) {
		this.luaV_settable(_G, new LString(name), poplvalue());
	}

	/**
	 * Get a value as a boolean. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the Lua value at the given acceptable index to a C&nbsp;boolean
	 * value (0&nbsp;or&nbsp;1). Like all tests in Lua, <a
	 * href="#lua_toboolean"><code>lua_toboolean</code></a> returns 1 for
	 * any Lua value different from <b>false</b> and <b>nil</b>; otherwise it
	 * returns 0. It also returns 0 when called with a non-valid index. (If you
	 * want to accept only actual boolean values, use <a href="#lua_isboolean"><code>lua_isboolean</code></a>
	 * to test the value's type.)
	 * 
	 */
	public boolean toboolean(int index) {
		return topointer(index).toJavaBoolean();
	}

	/**
	 * Get a value as an int. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the Lua value at the given acceptable index to the signed
	 * integral type <a href="#lua_Integer"><code>lua_Integer</code></a>.
	 * The Lua value must be a number or a string convertible to a number (see
	 * <a href="#2.2.1">&sect;2.2.1</a>); otherwise, <a href="#lua_tointeger"><code>lua_tointeger</code></a>
	 * returns&nbsp;0.
	 * 
	 * 
	 * <p>
	 * If the number is not an integer, it is truncated in some non-specified
	 * way.
	 */
	public int tointeger(int index) {
		LValue v = tolnumber(index);
		return v.isNil()? 0: v.toJavaInt();
	}

	/**
	 * Get a value as a LFunction.
	 * <hr>
	 * <h3><a name="tofunction"><code>tofunction</code></a></h3>
	 * <p>
	 * <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <pre>
	 * LFunction tofunction (lua_State *L, int index);
	 * </pre>
	 * 
	 * <p>
	 * Converts a value at the given acceptable index to a C&nbsp;function. That
	 * value must be a function; otherwise, returns <code>NULL</code>.
	 */
	public LValue tofunction(int index) {
		LValue v = topointer(index);
		return v.isFunction()? v: LNil.NIL;
	}

	/**
	 * Gets the value of a string as byte array. <span class="apii">[-0, +0,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * Converts the Lua value at the given acceptable index to a C&nbsp;string.
	 * If <code>len</code> is not <code>NULL</code>, it also sets
	 * <code>*len</code> with the string length. The Lua value must be a
	 * string or a number; otherwise, the function returns <code>NULL</code>.
	 * If the value is a number, then <a href="#lua_tolstring"><code>lua_tolstring</code></a>
	 * also <em>changes the actual value in the stack to a string</em>. (This
	 * change confuses <a href="#lua_next"><code>lua_next</code></a> when <a
	 * href="#lua_tolstring"><code>lua_tolstring</code></a> is applied to
	 * keys during a table traversal.)
	 * 
	 * 
	 * <p>
	 * <a href="#lua_tolstring"><code>lua_tolstring</code></a> returns a
	 * fully aligned pointer to a string inside the Lua state. This string
	 * always has a zero ('<code>\0</code>') after its last character (as
	 * in&nbsp;C), but may contain other zeros in its body. Because Lua has
	 * garbage collection, there is no guarantee that the pointer returned by <a
	 * href="#lua_tolstring"><code>lua_tolstring</code></a> will be valid
	 * after the corresponding value is removed from the stack.
	 */
	public LString tolstring(int index) {
		return topointer(index).luaAsString();
	}

	/**
	 * Convert a value to a double. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the Lua value at the given acceptable index to the C&nbsp;type
	 * <a href="#lua_Number"><code>lua_Number</code></a> (see <a
	 * href="#lua_Number"><code>lua_Number</code></a>). The Lua value must
	 * be a number or a string convertible to a number (see <a
	 * href="#2.2.1">&sect;2.2.1</a>); otherwise, <a href="#lua_tonumber"><code>lua_tonumber</code></a>
	 * returns&nbsp;0.
	 * 
	 */
	public double tonumber(int index) {
		LValue v = tolnumber(index);
		return v.isNil()? 0: v.toJavaDouble();
	}

	/**
	 * Returns the index of the top element in the stack. <span
	 * class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Because indices start at&nbsp;1, this result is equal to the number of
	 * elements in the stack (and so 0&nbsp;means an empty stack).
	 */
	public int gettop() {
		return top - base;
	}
	
	/**
	 * Set the top of the stack. <span class="apii">[-?, +?, <em>-</em>]</span>
	 * 
	 * <p>
	 * Accepts any acceptable index, or&nbsp;0, and sets the stack top to this
	 * index. If the new top is larger than the old one, then the new elements
	 * are filled with <b>nil</b>. If <code>index</code> is&nbsp;0, then all
	 * stack elements are removed.
	 */
	public void settop(int nt) {
		int ant = nt>=0? base+nt: top+nt;
		if ( ant < base )
			throw new IllegalArgumentException("index out of bounds: "+ant );
		luaV_adjusttop(ant);
	}

	
	/** 
	 * Set the top to the base.  Equivalent to settop(0) 
	 */
	public void resettop() {
        luaV_settop_fillabove( base );
	}
	
	private int index2adr(int index) {
		// TODO: upvalues? globals? environment?   
		int ai = index>0? base+index-1: top+index;
		if ( ai < base )
			throw new IllegalArgumentException("index out of bounds: "+ai );
		return ai;
	}
	
	/**
	 * Get the raw Object at a stack location. <span class="apii">[-0, +0,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the value at the given acceptable index to a generic
	 * C&nbsp;pointer (<code>void*</code>). The value may be a userdata, a
	 * table, a thread, or a function; otherwise, <a href="#lua_topointer"><code>lua_topointer</code></a>
	 * returns <code>NULL</code>. Different objects will give different
	 * pointers. There is no way to convert the pointer back to its original
	 * value.
	 * 
	 * 
	 * <p>
	 * Typically this function is used only for debug information.
	 */
	public LValue topointer(int index) {
		int ai = index2adr(index);
		if ( ai >= top )
			return LNil.NIL;
		return stack[ai];
	}

	/**
	 * Get a stack value as a String. <span class="apii">[-0, +0, <em>m</em>]</span>
	 * 
	 * <p>
	 * Equivalent to <a href="#lua_tolstring"><code>lua_tolstring</code></a>
	 * with <code>len</code> equal to <code>NULL</code>.
	 */
	public String tostring(int index) {
		return topointer(index).toJavaString();
	}
	
	/**
	 * Get a value from the stack as a lua table. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the value at the given acceptable index to a Lua table
	 * This value must be a table otherwise, the function returns <code>NIL</code>.
	 */
	public LTable totable(int index) {
		return (LTable) topointer(index);
	}

	/**
	 * Get the Object from a userdata value. <span class="apii">[-0, +0,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * If the value at the given acceptable index is a full userdata, returns
	 * its block address. If the value is a light userdata, returns its pointer.
	 * Otherwise, returns <code>NULL</code>.
	 * 
	 */
	public Object touserdata(int index) {
		LValue v = topointer(index);
		if ( v.luaGetType() != Lua.LUA_TUSERDATA )
			return null;
		return ((LUserData)v).m_instance;
	}

	/**
	 * Get the type of a value. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns the type of the value in the given acceptable index, or
	 * <code>LUA_TNONE</code> for a non-valid index (that is, an index to an
	 * "empty" stack position). The types returned by <a href="#lua_type"><code>lua_type</code></a>
	 * are coded by the following constants defined in <code>lua.h</code>:
	 * <code>LUA_TNIL</code>, <code>LUA_TNUMBER</code>,
	 * <code>LUA_TBOOLEAN</code>, <code>LUA_TSTRING</code>,
	 * <code>LUA_TTABLE</code>, <code>LUA_TFUNCTION</code>,
	 * <code>LUA_TUSERDATA</code>, <code>LUA_TTHREAD</code>, and
	 * <code>LUA_TLIGHTUSERDATA</code>.
	 */
	public int type(int index) {
		return topointer(index).luaGetType();
	}

	/**
	 * Get the type name for a value. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns the name of the type encoded by the value <code>tp</code>,
	 * which must be one the values returned by <a href="#lua_type"><code>lua_type</code></a>.
	 */
	public String typename(int index) {
		return topointer(index).luaGetTypeName().toJavaString();
	}

	/**
	 * Exchange values between threads. <span class="apii">[-?, +?, <em>-</em>]</span>
	 * 
	 * <p>
	 * Exchange values between different threads of the <em>same</em> global
	 * state.
	 * 
	 * 
	 * <p>
	 * This function pops <code>n</code> values from the stack
	 * <code>from</code>, and pushes them onto the stack <code>to</code>.
	 */
	public void xmove(LuaState to, int n) {
		if ( n > 0 ) {
			to.checkstack(n);
			LuaState ss = (LuaState)to;
			ss.checkstack(n);
			System.arraycopy(stack, top-n, ss.stack, ss.top, n);
			ss.top += n;
		}
	}
	
	// ============================= conversion to and from Java boxed types ====================

	/**
	 * Push a Java Boolean value, or nil if the value is null. 
	 * @param b Boolean value to convert, or null to to nil.
	 */
	public void pushboolean(Boolean b) {
		if ( b == null )
			pushnil();
		else
			pushboolean( b.booleanValue() );
	}
	
	/**
	 * Push a Java Byte value, or nil if the value is null. 
	 * @param b Byte value to convert, or null to to nil.
	 */
	public void pushinteger(Byte b) {
		if ( b == null )
			pushnil();
		else
			pushinteger( b.byteValue() );
	}
	
	/**
	 * Push a Java Character value, or nil if the value is null. 
	 * @param c Character value to convert, or null to to nil.
	 */
	public void pushinteger(Character c) {
		if ( c == null )
			pushnil();
		else
			pushinteger( c.charValue() );
	}
	
	/**
	 * Push a Java Double as a double, or nil if the value is null. 
	 * @param d Double value to convert, or null to to nil.
	 */
	public void pushnumber(Double d) {
		if ( d == null )
			pushnil();
		else
			pushnumber( d.doubleValue() );
	}
	
	/**
	 * Push a Java Float value, or nil if the value is null. 
	 * @param f Float value to convert, or null to to nil.
	 */
	public void pushnumber(Float f) {
		if ( f == null )
			pushnil();
		else
			pushnumber( f.doubleValue() );
	}
	
	/**
	 * Push a Java Integer value, or nil if the value is null. 
	 * @param i Integer value to convert, or null to to nil.
	 */
	public void pushinteger(Integer i) {
		if ( i == null )
			pushnil();
		else
			pushinteger( i.intValue() );
	}
	
	/**
	 * Push a Java Short value, or nil if the value is null. 
	 * @param s Short value to convert, or null to to nil.
	 */
	public void pushinteger(Short s) {
		if ( s == null )
			pushnil();
		else
			pushinteger( s.shortValue() );
	}
	
	/**
	 * Push a Java Long value, or nil if the value is null. 
	 * @param l Long value to convert, or null to to nil.
	 */
	public void pushnumber(Long l) {
		if ( l == null )
			pushnil();
		else
			pushnumber( l.doubleValue() );
	}
	
	/**
	 * Push a Java Object as userdata, or nil if the value is null. 
	 * @param o Object value to push, or null to to nil.
	 */
	public void pushuserdata( Object o ) {
		if ( o == null )
			pushnil();
		else
			pushlvalue( new LUserData(o) );
	}


	/**
	 * Convert a value to a Java Boolean value, or null if the value is nil.
	 * @param index index of the parameter to convert. 
	 * @return Boolean value at the index, or null if the value was not a boolean.
	 */
	public Boolean toboxedboolean(int index) {
		return topointer(index).toJavaBoxedBoolean();
	}
	
	/**
	 * Convert a value to a Java Byte value, or null if the value is not a number.
	 * @param index index of the parameter to convert. 
	 * @return Byte value at the index, or null if the value was not a number.
	 */
	public Byte toboxedbyte(int index) {
		return topointer(index).toJavaBoxedByte();
	}
	
	/**
	 * Convert a value to a Java Double value, or null if the value is not a number.
	 * @param index index of the parameter to convert. 
	 * @return Double value at the index, or null if the value was not a number.
	 */
	public Double toboxeddouble(int index) {
		return topointer(index).toJavaBoxedDouble();
	}
	
	/**
	 * Convert a value to a Java Float value, or null if the value is not a number.
	 * @param index index of the parameter to convert. 
	 * @return Float value at the index, or null if the value was not a boolean.
	 */
	public Float toboxedfloat(int index) {
		return topointer(index).toJavaBoxedFloat();
	}
	
	/**
	 * Convert a value to a Java Integer value, or null if the value is not a number.
	 * @param index index of the parameter to convert. 
	 * @return Integer value at the index, or null if the value was not a number.
	 */
	public Integer toboxedinteger(int index) {
		return topointer(index).toJavaBoxedInteger();
	}
	
	/**
	 * Convert a value to a Java Long value, or null if the value is nil.
	 * @param index index of the parameter to convert. 
	 * @return Long value at the index, or null if the value was not a number.
	 */
	public Long toboxedlong(int index) {
		return topointer(index).toJavaBoxedLong();
	}

    // ================= Error Reporting Functions =================

	/**
	 * Report an error with an argument.
	 * 
	 * @param narg Stack index of the bad argument
	 * @param extramsg String to include in error message
	 */
    public void argerror(int narg, String extramsg) {
        error("bad argument #" + (narg - 1) + " (" + extramsg + ")");
    }

	/**
	 * Conditionally report an error with an argument.
	 * 
	 * @param cond boolean condition that generates an error when false
	 * @param narg Stack index of the bad argument
	 * @param extramsg String to include in error message
	 */
	public void argcheck(boolean cond, int narg, String extramsg) {
		if ( ! cond )
			argerror(narg,extramsg);		
	}
	
	/**
     * Report a type error.
     * 
     * @param narg Stack index of the bad argument
     * @param typename Name of the type that was expected, such as "string"
     */
    public void typerror(int narg, String typename) {
        argerror(narg, typename + " expected, got " + typename(narg));
    }

    /**
     * Report a type error.
     * 
     * @param narg Stack index of the bad argument
     * @param typenum Constant value specifying the type of argument that was expected (i.e. LUA_TSTRING).
     */
    public void typerror(int narg, int typenum) {
        typerror(narg, TYPE_NAMES[typenum]);
    }
    

	/**
	 * Checks whether the function has an argument of any type (including <b>nil</b>)
	 * at position <code>narg</code>.
	 * @param narg the argument number
	 * @return the value at the index
	 * @throws LuaErrorException if there is no argument at position narg
	 */
	public LValue checkany(int narg) {
		if ( gettop() < narg )
			argerror(narg, "value expected");
		return topointer(narg);
	}

	/**
	 * Checks whether the function argument <code>narg</code> is a function and
	 * returns this function.
	 * @see LFunction
	 * @param narg the argument number
	 * @throws LuaErrorException if the value is not a function
	 * @return LFunction value if the argument is a function
	 */
	public LFunction checkfunction(int narg) {
		return (LFunction) checktype(narg, Lua.LUA_TFUNCTION);
	}
	
	/**
	 * Checks whether the function argument <code>narg</code> is a thread and
	 * returns this thread.
	 * @see LThread
	 * @param narg the argument number
	 * @throws LuaErrorException if the value is not a thread
	 * @return LThread value if the argument is a thread
	 */
	public LThread checkthread(int narg) {
		return (LThread) checktype(narg, Lua.LUA_TTHREAD);
	}
	
	/**
	 * Checks whether the function argument <code>narg</code> is a number and
	 * returns this number cast to an <code>int</code>.
	 * @param narg the argument number
	 * @throws LuaErrorException if the number cannot be converted to an int
	 * @return int value if the argument is an int or can be converted to one
	 */
	public int checkint(int narg) {
		LValue v = tolnumber(narg);
		if ( v.isNil() )
			typerror(narg, Lua.LUA_TNUMBER);
		return v.toJavaInt();
	}

	/**
	 * Checks whether the function argument <code>narg</code> is a number and
	 * returns this number cast to a <code>LInteger</code></a>.
	 * @see LInteger
	 * @param narg the argument number
	 * @throws LuaErrorException if the value cannot be converted to an int
	 * @return LInteger value if the argument is an int or can be converted to one
	 */
	public LInteger checkinteger(int narg) {
		return LInteger.valueOf(checkint(narg));
	}
	
	/**
	 * Checks whether the function argument <code>narg</code> is a number and
	 * returns this number cast to a <code>long</code>.
	 * @param narg the argument number
	 * @throws LuaErrorException if the value cannot be converted to a long
	 * @return long value if the argument is a number or can be converted to long
	 */
	public long checklong(int narg) {
		return checknumber(narg).toJavaLong();
	}
	
	/**
	 * Checks whether the function argument <code>narg</code> is a number and
	 * returns this number cast to a <code>double</code>.
	 * @param narg the argument number
	 * @throws LuaErrorException if the value cannot be converted to a double
	 * @return long value if the argument is a number or can be converted to double
	 */
	public double checkdouble(int narg) {
		return checknumber(narg).toJavaDouble();
	}
	
	/**
	 * Checks whether the function argument <code>narg</code> is a number and
	 * returns this number.
	 * @see LNumber
	 * @param narg the argument number
	 * @throws LuaErrorException if the value cannot be converted to a number
	 * @return LNumber value if the argument is a number or can be converted to one
	 */
	public LNumber checknumber(int narg) {
		LValue v = topointer(narg).luaToNumber();
		if ( v.isNil() )
			typerror(narg, Lua.LUA_TNUMBER);
		return (LNumber) v;
	}
	
	/**
	 * Checks whether the function argument <code>narg</code> is a string and
	 * returns this string as a lua string.
	 * @see LString
	 * @param narg the argument number
	 * @throws LuaErrorException if the value cannot be converted to a string
	 * @return LString value if the argument is a string or can be converted to one
	 */
	public LString checklstring(int narg) {
		LValue v = topointer(narg);
		if ( ! v.isString() )
			typerror(narg, Lua.LUA_TSTRING);
		return v.luaAsString();
	}

	/**
	 * Checks whether the function argument <code>narg</code> is a string and
	 * returns this string as a Java String.
	 * @param narg the argument number
	 * @throws LuaErrorException if the value cannot be converted to a string
	 * @return String value if the argument is a string or can be converted to one
	 */
	public String checkstring(int narg) {
		LValue v = topointer(narg);
		if ( ! v.isString() )
			typerror(narg, Lua.LUA_TSTRING);
		return v.toJavaString();
	}

	/**
	 * Checks whether the function argument <code>narg</code> is a table and
	 * returns this table.
	 * @see LTable
	 * @param narg the argument number
	 * @throws LuaErrorException if the value is not a table
	 * @return LTable value if the argument is a table
	 */
	public LTable checktable(int narg) {
		return (LTable) checktype(narg, Lua.LUA_TTABLE);
	}
	
	/**
	 * Checks whether the function argument <code>narg</code> has type
	 * <code>t</code> and return it as an LValue.
	 * @param narg the argument number
	 * @param t the type number to check against
	 * @return the lua value
	 * @throws LuaErrorException if the value is not of type t
	 */
	public LValue checktype(int narg, int t) {
		LValue v = topointer(narg);
		if ( v.luaGetType() != t )
			typerror(narg, t);
		return v;
	}


    /**
     * Check that the type of userdata on the stack matches the required type,
     * and if so, return the Java Object the userdata value points to.
     * 
     * @param ud
     *            Stack index of the argument to check
     * @param expected
     *            Class that the userdata is expected to have an instance of.
     */
    public Object checkudata(int ud, Class expected) {
        Object p = touserdata(ud);
        if (expected.isInstance(p)) {
            return p;
        }
        typerror(ud, expected.getName());
        return null;
    }

	/**
	 * If the function argument <code>narg</code> is a number, returns this
	 * number cast to an <code>int</code>. If this argument is absent or is
	 * <b>nil</b>, returns <code>d</code>. Otherwise, raises an error.
	 * @param narg the argument number
	 * @param d the default value when the argument is nil or not supplied 
	 * @throws LuaErrorException if the value cannot be converted to an int
	 * @return int value if the argument is an int, or d
	 */
    public int optint(int narg, int d) { 
		LNumber n = optnumber(narg,null);
		return (n==null? d: n.toJavaInt());
    }
    
	/**
	 * If the function argument <code>narg</code> is a number, returns this
	 * number cast to a <a href="#lua_Integer"><code>lua_Integer</code></a>.
	 * If this argument is absent or is <b>nil</b>, returns <code>d</code>.
	 * Otherwise, raises an error.
	 * @param narg the argument number
	 * @param d the default value when the argument is nil or not supplied 
	 * @throws LuaErrorException if the value cannot be converted to an int
	 * @return int value if the argument is an int, or d
	 */
	public LInteger optinteger(int narg, int d) {
		return LInteger.valueOf(optint(narg,d));
	}
    
	/**
	 * If the function argument <code>narg</code> is a number, returns this
	 * number cast to a <code>long</code>. If this argument is absent or is
	 * <b>nil</b>, returns <code>d</code>. Otherwise, raises an error.
	 * @param narg the argument number
	 * @param d the default value when the argument is nil or not supplied 
	 * @throws LuaErrorException if the value cannot be converted to an number
	 * @return int value if the argument is an number, or d
	 */
	public long optlong(int narg, long d) {
		LNumber n = optnumber(narg,null);
		return (n==null? d: n.toJavaLong());
	}
	
	/**
	 * If the function argument <code>narg</code> is a number, returns this
	 * number. If this argument is absent or is <b>nil</b>, returns
	 * <code>d</code>. Otherwise, raises an error.
	 * @param narg the argument number
	 * @param d the default value when the argument is nil or not supplied 
	 * @throws LuaErrorException if the value cannot be converted to an number
	 * @return int value if the argument is an number, or d
	 */
	public LNumber optnumber(int narg, LNumber d) {
		LValue v = topointer(narg);
		if ( v.isNil() )
			return d;
		v = v.luaToNumber();
		if ( v.isNil() )
			typerror(narg, Lua.LUA_TNUMBER);
		return (LNumber) v;
		
	}
	
	/**
	 * If the function argument <code>narg</code> is a string, returns this
	 * string. If this argument is absent or is <b>nil</b>, returns
	 * <code>d</code>. Otherwise, raises an error.
	 */
	public LString optlstring(int narg, LString d) {
		LValue v = topointer(narg);
		if ( v.isNil() )
			return d;
		if ( ! v.isString() )
			typerror(narg, Lua.LUA_TSTRING);
		return v.luaAsString();
	}
	
	/**
	 * If the function argument <code>narg</code> is a string, returns this
	 * string. If this argument is absent or is <b>nil</b>, returns
	 * <code>d</code>. Otherwise, raises an error.
	 */
	public String optstring(int narg, String d) {
		LValue v = topointer(narg);
		if ( v.isNil() )
			return d;
		if ( ! v.isString() )
			typerror(narg, Lua.LUA_TSTRING);
		return v.toJavaString();
	}
    
	/**
	 * Method to indicate a vm internal error has occurred. Generally, this is
	 * not recoverable, so we convert to a lua error during production so that
	 * the code may recover.
	 */
	public static void vmerror(String description) {
		throw new LuaErrorException( "internal error: "+description );
	}
	
	/**  
	 * Call a function with no arguments and one return value 
	 * @param function
	 * @return
	 */
	public LValue call(LFunction function) {
        int oldtop = top;
        try {
        	top = base + this.calls[cc].closure.p.maxstacksize;
   	    	pushlvalue(function);
   	    	call(0,1);
   	    	return poplvalue();
        } finally {
        	top = oldtop;
        }
	}

	/**  
	 * Call a function with one argument and one return value 
	 * @param function
	 * @param arg0
	 * @return
	 */
	public LValue call(LFunction function, LValue arg0) {
        int oldtop = top;
        try {
        	top = base + this.calls[cc].closure.p.maxstacksize;
   	    	pushlvalue(function);
   	    	pushlvalue(arg0);
   	    	call(1,1);
   	    	return poplvalue();
        } finally {
        	top = oldtop;
        }
	}

	/**  
	 * Call a function with two arguments and one return value 
	 * @param function
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public LValue call(LFunction function, LValue arg0, LValue arg1) {
        int oldtop = top;
        try {
        	top = base + this.calls[cc].closure.p.maxstacksize;
   	    	pushlvalue(function);
   	    	pushlvalue(arg0);
   	    	pushlvalue(arg1);
   	    	call(2,1);
   	    	return poplvalue();
        } finally {
        	top = oldtop;
        }
	}

	/**  
	 * Call a function with three arguments and one return value 
	 * @param function
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	public LValue call(LFunction function, LValue arg0, LValue arg1, LValue arg2) {
        int oldtop = top;
        try {
        	top = base + this.calls[cc].closure.p.maxstacksize;
   	    	pushlvalue(function);
   	    	pushlvalue(arg0);
   	    	pushlvalue(arg1);
   	    	pushlvalue(arg2);
   	    	call(3,1);
   	    	return poplvalue();
        } finally {
        	top = oldtop;
        }
	}
}
