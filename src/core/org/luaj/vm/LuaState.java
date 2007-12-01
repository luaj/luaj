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

    public static final String PROPERTY_LUAJ_DEBUG = "Luaj-Debug";
    protected static final String DEBUG_CLASS_NAME = "org.luaj.debug.DebugLuaState";    
    
    /* thread status; 0 is OK */
    private static final int LUA_YIELD  = 1;
    private static final int LUA_ERRRUN = 2;
    private static final int LUA_ERRSYNTAX  = 3;
    private static final int LUA_ERRMEM = 4;
    private static final int LUA_ERRERR = 5;

    private static final int LUA_MINSTACK = 20;
    private static final int LUA_MINCALLS = 10;

    public int base = 0;
    protected int top = 0;
    protected int nresults = -1;
    public LValue[] stack = new LValue[LUA_MINSTACK];
    public int cc = -1;
    public CallInfo[] calls = new CallInfo[LUA_MINCALLS];
    protected Stack upvals = new Stack();
	protected LFunction panic;
    
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
	 * 
	 * @deprecated As of version 0.10, replaced by {@link #newState()}
	 */
	public LuaState() {
		_G = new LTable();
		_G.put("_G", _G);
	}

	public LuaState(LTable globals) {
		_G = globals;
	}

	/**
	 * Factory method to return an instance of LuaState. If debug property is
	 * present, it will create a DebugLuaState instance.
	 * @return
	 */
	public static LuaState newState() {
	    String isDebugStr 
	        = Platform.getInstance().getProperty(PROPERTY_LUAJ_DEBUG);
	    boolean isDebug = (isDebugStr != null && "true".equalsIgnoreCase(isDebugStr));

	    LuaState vm = null;
	    if ( isDebug ) {
                try {
                    vm = (LuaState) Class.forName( DEBUG_CLASS_NAME ).newInstance();
                } catch (Exception e) {
                    System.out.println("Warning: no debug support, " + e );
                }
	    }
	    
            if ( vm == null )
                vm = new LuaState();
            
            vm.init();
            
            return vm;
	}
	
	/**
	 * Performs the initialization.
	 */
	public void init() {}
	
	/**
	 * Install the standard set of libraries used by most implementations:
	 * BaseLib, CoroutineLib, MathLib, PackageLib, TableLib, StringLib
	 */
	public void installStandardLibs() {
		BaseLib.install(_G);
        CoroutineLib.install(_G);
        MathLib.install(_G);
        PackageLib.install(_G);
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
        if ( ! c.p.is_vararg ) {
            base += 1;
            adjustTop( base+c.p.numparams );
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
            adjustTop( base + npar );
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
            adjustTop(rb + nreturns);
        
        // restore base
        this.base = oldbase;
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
                adjustTop(rb + nreturns);
            
            // restore base
            this.base = oldbase;
            
            return 0;
        } catch ( Throwable t ) {
            this.base = oldbase;
            this.cc = oldcc;
            closeUpVals(oldtop);  /* close eventual pending closures */
            String s = t.getMessage();
            settop(0);
            pushstring( s!=null? s: t.toString() ); 
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
            pushlvalue( new LClosure( p, _G ) );
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

    private void adjustTop(int newTop) {
        while (top < newTop)
            this.stack[top++] = LNil.NIL;
        top = newTop;
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
            
            // get first opcode arg
            a = LuaState.GETARG_A(i);
            switch (LuaState.GET_OPCODE(i)) {
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
                top = base + a;
                table.luaGetTable(this, table, key);
                continue;
            }
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
                o = LuaState.GET_OPCODE(i);
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
                LString[] strings = new LString[numValues];
                
                for (int j = b, l = 0; j <= c; j++, l++) {
                    LString s = this.stack[base + j].luaAsString();
                    strings[l] = s;
                }
                this.stack[base + a] = LString.concat( strings );
                continue;
            }
            case LuaState.OP_JMP: {
                ci.pc += LuaState.GETARG_sBx(i);
                continue;
            }
            case LuaState.OP_EQ:
            case LuaState.OP_LT:
            case LuaState.OP_LE: {
                o = LuaState.GET_OPCODE(i);
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
                this.stack[base + a] = step.luaBinOpUnknown(Lua.OP_SUB,
                        init);
                b = LuaState.GETARG_sBx(i);
                ci.pc += b;
                continue;
            }
            case LuaState.OP_TFORLOOP: {
                cb = a + 3; /* call base */
                System.arraycopy(this.stack, base + a, this.stack,
                        base + cb, 3);
                base += cb;
                try {
                    top = base + 3; /* func. + 2 args (state and index) */
                    
                    // call the iterator
                    c = LuaState.GETARG_C(i);
                    if (this.stack[base].luaStackCall(this))
                        execute();
                    adjustTop( base + c - 1 );
                    
                    // test for continuation
                    if (this.stack[base] != LNil.NIL) { // continue?
                        this.stack[base - 1] = this.stack[base]; // save control variable
                    } else {
                        ci.pc++; // skip over jump
                    }
                } finally {
                    base -= cb;
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
                table = this.stack[base + a];
                for (int index = 1; index <= b; index++) {
                    val = this.stack[listBase + index];
                    table.luaSetTable(this, table, LInteger.valueOf(index),
                            val);
                }
                top = base + a - 1;
                continue;
            }
            case LuaState.OP_CLOSE: {
                closeUpVals( a ); // close upvals higher in the stack than position a
                continue;
            }
            case LuaState.OP_CLOSURE: {
                b = LuaState.GETARG_Bx(i);
                proto = cl.p.p[b];
                newClosure = new LClosure(proto, _G);
                for (int j = 0; j < newClosure.upVals.length; j++, ci.pc++) {
                    i = code[ci.pc];
                    o = LuaState.GET_OPCODE(i);
                    b = LuaState.GETARG_B(i);
                    if (o == LuaState.OP_GETUPVAL) {
                        newClosure.upVals[j] = cl.upVals[b];
                    } else if (o == LuaState.OP_MOVE) {
                        newClosure.upVals[j] = findUpVal( proto.upvalues[j], base + b );
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
            }
        }   
    }
    
    private UpVal findUpVal( LString upValName, int target ) {
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
        
        up = new UpVal( upValName, this, target );
        this.upvals.insertElementAt( up, i + 1 );
        return up;
    }
    
    private void closeUpVals( int limit ) {
        while ( !upvals.empty() && ( (UpVal) this.upvals.lastElement() ).close( limit ) ) {
            this.upvals.pop();
        }
    }

    public CallInfo getStackFrame(int callStackDepth) {
        return calls[cc-callStackDepth];
    }
    
    
    //===============================================================
    //              Lua Java API
    //===============================================================
    
	private void notImplemented() {
		throw new LuaErrorException("AbstractStack: not yet implemented");
	}
	
	/**
	 * Sets a new panic function and returns the old one. <span
	 * class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * 
	 * <p>
	 * If an error happens outside any protected environment, Lua calls a
	 * <em>panic function</em> and then calls <code>exit(EXIT_FAILURE)</code>,
	 * thus exiting the host application. Your panic function may avoid this
	 * exit by never returning (e.g., doing a long jump).
	 * 
	 * 
	 * <p>
	 * The panic function can access the error message at the top of the stack.
	 */
	public LFunction atpanic(LFunction panicf) {
		LFunction f = panic;
		panic = panicf;
		return f;
	}
	
    /**
     * Returns the current program counter for the given call frame.
     * @param ci -- A call frame
     * @return the current program counter for the given call frame.
     */
    protected int getCurrentPc(CallInfo ci) {
        int pc = (ci != calls[cc] ? ci.pc - 1 : ci.pc);
        return pc;
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
     * @param cindex
     * @return
     */
    protected String getFileLine(int cindex) {
        String source = "?";
        String line = "";
        if (cindex >= 0) {
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
    	if ( level > 1 )
    		message = getFileLine(cc + 2 - level) + ": " + message;
        throw new LuaErrorException( message );
    }
    
	/**
	 * Raises an error with the default level.   
	 * 
	 * In the java implementation this calls error(message,1)
	 */
    public void error(String message) {
    	error( message, 1 );
    }
    
	/**
	 * Generates a Lua error. <span class="apii">[-1, +0, <em>v</em>]</span>
	 * 
	 * <p>
	 * The error message (which can actually be a Lua value of any type) must be
	 * on the stack top. This function does a long jump, and therefore never
	 * returns. (see <a href="#luaL_error"><code>luaL_error</code></a>).
	 * 
	 */
	public void error() {
		String message = tostring(-1);
		// pop(1);
		error( message );
	}
	
	/**
	 * Dumps a function as a binary chunk. <span class="apii">[-0, +0,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * Receives a Lua function on the top of the stack and produces a binary
	 * chunk that, if loaded again, results in a function equivalent to the one
	 * dumped. As it produces parts of the chunk, <a href="#lua_dump"><code>lua_dump</code></a>
	 * calls function <code>writer</code> (see <a href="#lua_Writer"><code>lua_Writer</code></a>)
	 * with the given <code>data</code> to write them.
	 * <p>
	 * The value returned is the error code returned by the last call to the
	 * writer; 0&nbsp;means no errors.
	 * 
	 * 
	 * <p>
	 * This function does not pop the Lua function from the stack.
	 * 
	 */
	public void dump() {
		notImplemented();
	}
	

	/**
	 * Pushes a new C&nbsp;closure onto the stack. <span class="apii">[-n, +1,
	 * <em>m</em>]</span>
	 * 
	 * 
	 * <p>
	 * When a Java&nbsp;function is created, it is possible to associate some
	 * values with it, thus creating a C&nbsp;closure (see <a
	 * href="#3.4">&sect;3.4</a>); these values are then accessible to the
	 * function whenever it is called. To associate values with a
	 * C&nbsp;function, first these values should be pushed onto the stack (when
	 * there are multiple values, the first value is pushed first). Then <a
	 * href="#lua_pushcclosure"><code>lua_pushcclosure</code></a> is called
	 * to create and push the C&nbsp;function onto the stack, with the argument
	 * <code>n</code> telling how many values should be associated with the
	 * function. <a href="#lua_pushcclosure"><code>lua_pushcclosure</code></a>
	 * also pops these values from the stack.
	 */
	public void pushclosure(LFunction fn, int n) {
		notImplemented();
	}

	/**
	 * Calls the C&nbsp;function <code>func</code> in protected mode. <span
	 * class="apii">[-0, +(0|1), <em>-</em>]</span>
	 * 
	 * <p>
	 * <code>func</code> starts with only one element in its stack, a light
	 * userdata containing <code>ud</code>. In case of errors, <a
	 * href="#lua_cpcall"><code>lua_cpcall</code></a> returns the same error
	 * codes as <a href="#lua_pcall"><code>lua_pcall</code></a>, plus the
	 * error object on the top of the stack; otherwise, it returns zero, and
	 * does not change the stack. All values returned by <code>func</code> are
	 * discarded.
	 */
	public int javapcall(LFunction func, Object ud) {
		this.pushjavafunction(func);
		this.pushlightuserdata(ud);
		return this.pcall(1, 0, 0);
	}

	/**
	 * Format and push a string. <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack a formatted string and returns a pointer to this
	 * string. It is similar to the C&nbsp;function <code>sprintf</code>, but
	 * has some important differences:
	 * 
	 * <ul>
	 * 
	 * <li> You do not have to allocate space for the result: the result is a
	 * Lua string and Lua takes care of memory allocation (and deallocation,
	 * through garbage collection). </li>
	 * 
	 * <li> The conversion specifiers are quite restricted. There are no flags,
	 * widths, or precisions. The conversion specifiers can only be '<code>%%</code>'
	 * (inserts a '<code>%</code>' in the string), '<code>%s</code>'
	 * (inserts a zero-terminated string, with no size restrictions), '<code>%f</code>'
	 * (inserts a <a href="#lua_Number"><code>lua_Number</code></a>), '<code>%p</code>'
	 * (inserts a pointer as a hexadecimal numeral), '<code>%d</code>'
	 * (inserts an <code>int</code>), and '<code>%c</code>' (inserts an
	 * <code>int</code> as a character). </li>
	 * 
	 * </ul>
	 */
	public String pushfstring(String fmt, Object[] args) {
		notImplemented();
		return null;
	}
	
	/**
	 * Format and push a string. <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * Equivalent to <a href="#lua_pushfstring"><code>lua_pushfstring</code></a>,
	 * except that it receives a <code>va_list</code> instead of a variable
	 * number of arguments.
	 */
	public void pushvfstring(String format, Object[] args) {
		notImplemented();
	}

	/**
	 * Test if two objects are the same object. <span class="apii">[-0, +0,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the two values in acceptable indices <code>index1</code>
	 * and <code>index2</code> are primitively equal (that is, without calling
	 * metamethods). Otherwise returns&nbsp;0. Also returns&nbsp;0 if any of the
	 * indices are non valid.
	 */
	public void rawequal(int index1, int index2) {
		notImplemented();
	}

	/**
	 * Pushes a value's environment table. <span class="apii">[-0, +1,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the environment table of the value at the given
	 * index.
	 */
	public void getfenv(int index) {
		LValue f = topointer(index);
		pushlvalue( ((LClosure) f).env );
	}
	
	/**
	 * Set the environment for a value. <span class="apii">[-1, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Pops a table from the stack and sets it as the new environment for the
	 * value at the given index. If the value at the given index is neither a
	 * function nor a thread nor a userdata, <a href="#lua_setfenv"><code>lua_setfenv</code></a>
	 * returns 0. Otherwise it returns 1.
	 */
	public int setfenv(int index) {
		LTable t = totable(-1);
		LValue f = topointer(index);
		pop(1);
		return f.luaSetEnv(t);
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
		if ( top + extra > stack.length ) {
			int n = Math.max( top + extra + LUA_MINSTACK, stack.length * 2 );
			LValue[] s = new LValue[n];
			System.arraycopy(stack, 0, s, 0, stack.length);
			stack = s;			
		}
	}
	

	/**
	 * Closes the given Lua state. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Destroys all objects in the given Lua state (calling the corresponding
	 * garbage-collection metamethods, if any) and frees all dynamic memory used
	 * by this state. On several platforms, you may not need to call this
	 * function, because all resources are naturally released when the host
	 * program ends. On the other hand, long-running programs, such as a daemon
	 * or a web server, might need to release states as soon as they are not
	 * needed, to avoid growing too large.
	 */
	public void close() {
		stack = new LValue[20];
		base = top = 0;
	}
	
	/**
	 * Concatenates the <code>n</code> values at the top of the stack. <span
	 * class="apii">[-n, +1, <em>e</em>]</span>
	 * 
	 * <p>
	 * Concatenates the <code>n</code> values at the top of the stack, pops
	 * them, and leaves the result at the top. If <code>n</code>&nbsp;is&nbsp;1,
	 * the result is the single value on the stack (that is, the function does
	 * nothing); if <code>n</code> is 0, the result is the empty string.
	 * Concatenation is performed following the usual semantics of Lua (see <a
	 * href="#2.5.4">&sect;2.5.4</a>).
	 */
	public void concat(int n) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for ( int i=-n; i<0; i++ ) {
			LString ls = tolstring(i);
			baos.write(ls.m_bytes, ls.m_offset, ls.m_length);
		}
		pop(n);
		pushlvalue( new LString(baos.toByteArray()));
	}
	
	/**
	 * Creates a new empty table and pushes it onto the stack. <span
	 * class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * The new table has space pre-allocated for <code>narr</code> array
	 * elements and <code>nrec</code> non-array elements. This pre-allocation
	 * is useful when you know exactly how many elements the table will have.
	 * Otherwise you can use the function <a href="#lua_newtable"><code>lua_newtable</code></a>.
	 */
	public void createtable(int narr, int nrec) {
		stack[top++] = new LTable(narr, nrec);
	}
	
	/**
	 * Tests if two items on the stack are equal. <span class="apii">[-0, +0,
	 * <em>e</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the two values in acceptable indices <code>index1</code>
	 * and <code>index2</code> are equal, following the semantics of the Lua
	 * <code>==</code> operator (that is, may call metamethods). Otherwise
	 * returns&nbsp;0. Also returns&nbsp;0 if any of the indices is non valid.
	 * 
	 * 
	 */
	public boolean equal(int index1, int index2) {
		return topointer(index2).luaBinOpUnknown(Lua.OP_EQ,
				topointer(index1)).toJavaBoolean();
	}
	
	/**
	 * Controls the garbage collector. <span class="apii">[-0, +0, <em>e</em>]</span>
	 * 
	 * <p>
	 * This function performs several tasks, according to the value of the
	 * parameter <code>what</code>:
	 * 
	 * <ul>
	 * 
	 * <li><b><code>LUA_GCSTOP</code>:</b> stops the garbage collector.
	 * </li>
	 * 
	 * <li><b><code>LUA_GCRESTART</code>:</b> restarts the garbage
	 * collector. </li>
	 * 
	 * <li><b><code>LUA_GCCOLLECT</code>:</b> performs a full
	 * garbage-collection cycle. </li>
	 * 
	 * <li><b><code>LUA_GCCOUNT</code>:</b> returns the current amount of
	 * memory (in Kbytes) in use by Lua. </li>
	 * 
	 * <li><b><code>LUA_GCCOUNTB</code>:</b> returns the remainder of
	 * dividing the current amount of bytes of memory in use by Lua by 1024.
	 * </li>
	 * 
	 * <li><b><code>LUA_GCSTEP</code>:</b> performs an incremental step of
	 * garbage collection. The step "size" is controlled by <code>data</code>
	 * (larger values mean more steps) in a non-specified way. If you want to
	 * control the step size you must experimentally tune the value of
	 * <code>data</code>. The function returns 1 if the step finished a
	 * garbage-collection cycle. </li>
	 * 
	 * <li><b><code>LUA_GCSETPAUSE</code>:</b> sets <code>data</code>/100
	 * as the new value for the <em>pause</em> of the collector (see <a
	 * href="#2.10">&sect;2.10</a>). The function returns the previous value of
	 * the pause. </li>
	 * 
	 * <li><b><code>LUA_GCSETSTEPMUL</code>:</b> sets <code>data</code>/100
	 * as the new value for the <em>step multiplier</em> of the collector (see
	 * <a href="#2.10">&sect;2.10</a>). The function returns the previous value
	 * of the step multiplier. </li>
	 * 
	 * </ul>
	 */
	public void gc(int what, int data) {
		notImplemented();
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
	public void getfield(int index, String k) {
		LTable t = totable(index);
		// TODO: what if this triggers metatable ops
		// pushlvalue( t.luaGetTable(this, t, new LString(k)) );
		t.luaGetTable(this, t, new LString(k));
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
		LTable t = this._G;
		// TODO: what if this triggers metatable ops
		// pushlvalue( t.luaGetTable(this, t, new LString(s)));
		t.luaGetTable(this, t, new LString(s));
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
	 * Dereference a table's list element. <span class="apii">[-1, +1,
	 * <em>e</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the value <code>t[k]</code>, where
	 * <code>t</code> is the value at the given valid index and <code>k</code>
	 * is the value at the top of the stack.
	 * 
	 * <p>
	 * This function pops the key from the stack (putting the resulting value in
	 * its place). As in Lua, this function may trigger a metamethod for the
	 * "index" event (see <a href="#2.8">&sect;2.8</a>).
	 */
	public void gettable(int index) {
		LValue t = totable(index);
		LValue k = poplvalue();
		// todo: what if this triggers metatable ops
		// pushlvalue( t.luaGetTable(this, t, k) );
		t.luaGetTable(this, t, k);
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
	 * Test if a value is a JavaFunction. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a
	 * C&nbsp;function, and 0&nbsp;otherwise.
	 * 
	 */
	public boolean isjavafunction(int index) {
		return topointer(index) instanceof LFunction;
	}

	/**
	 * Test if a value is light user data <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a light userdata,
	 * and 0&nbsp;otherwise.
	 */
	public boolean islightuserdata(int index) {
		return false;
	}

	/**
	 * Test if a value is nil <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is <b>nil</b>, and
	 * 0&nbsp;otherwise.
	 */
	public boolean isnil(int index) {
		return topointer(index) == LNil.NIL;
	}

	/**
	 * Test if a value is not valid <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the the given acceptable index is not valid (that is, it
	 * refers to an element outside the current stack), and 0&nbsp;otherwise.
	 */
	public boolean isnone(int index) {
		return topointer(index) == null;
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
		return tolnumber(index) != LNil.NIL;
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
		return type(index) == Lua.LUA_TSTRING;
	}

	/**
	 * Test if a value is a table <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at the given acceptable index is a table, and
	 * 0&nbsp;otherwise.
	 */
	public boolean istable(int index) {
		return type(index) == Lua.LUA_TTABLE;
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
	 * Compare two values <span class="apii">[-0, +0, <em>e</em>]</span>
	 * 
	 * <p>
	 * Returns 1 if the value at acceptable index <code>index1</code> is
	 * smaller than the value at acceptable index <code>index2</code>,
	 * following the semantics of the Lua <code>&lt;</code> operator (that is,
	 * may call metamethods). Otherwise returns&nbsp;0. Also returns&nbsp;0 if
	 * any of the indices is non valid.
	 */
	public boolean lessthan(int index1, int index2) {
		return topointer(index2).luaBinOpUnknown(Lua.OP_LT,
				topointer(index1)).toJavaBoolean();
	}

	/**
	 * Create a table <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * Creates a new empty table and pushes it onto the stack. It is equivalent
	 * to <code>lua_createtable(L, 0, 0)</code>.
	 */
	public void newtable() {
		stack[top++] = new LTable();
	}

	/**
	 * Create a thread <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * Creates a new thread, pushes it on the stack, and returns a pointer to a
	 * <a href="#lua_State"><code>lua_State</code></a> that represents this
	 * new thread. The new state returned by this function shares with the
	 * original state all global objects (such as tables), but has an
	 * independent execution stack.
	 * 
	 * 
	 * <p>
	 * There is no explicit function to close or to destroy a thread. Threads
	 * are subject to garbage collection, like any Lua object.
	 */
	public void newthread() {
		notImplemented();
	}

	/**
	 * Create a userdata <span class="apii">[-0, +1, <em>m</em>]</span>
	 * 
	 * <p>
	 * This function allocates a new block of memory with the given size, pushes
	 * onto the stack a new full userdata with the block address, and returns
	 * this address.
	 * 
	 * 
	 * <p>
	 * Userdata represent C&nbsp;values in Lua. A <em>full userdata</em>
	 * represents a block of memory. It is an object (like a table): you must
	 * create it, it can have its own metatable, and you can detect when it is
	 * being collected. A full userdata is only equal to itself (under raw
	 * equality).
	 * 
	 * 
	 * <p>
	 * When Lua collects a full userdata with a <code>gc</code> metamethod,
	 * Lua calls the metamethod and marks the userdata as finalized. When this
	 * userdata is collected again then Lua frees its corresponding memory.
	 */
	public void newuserdata(Object o) {
		stack[top++] = new LUserData(o);
	}

	/**
	 * Traverse to the next table item. <span class="apii">[-1, +(2|0),
	 * <em>e</em>]</span>
	 * 
	 * <p>
	 * Pops a key from the stack, and pushes a key-value pair from the table at
	 * the given index (the "next" pair after the given key). If there are no
	 * more elements in the table, then <a href="#lua_next"><code>lua_next</code></a>
	 * returns 0 (and pushes nothing).
	 * 
	 * 
	 * <p>
	 * A typical traversal looks like this:
	 * 
	 * <pre>
	 * // table is in the stack at index 't' 
	 * lua_pushnil(L); // first key 
	 * while (lua_next(L, t) != 0) {
	 * 	// uses 'key' (at index -2) and 'value' (at index -1) 
	 * 	printf(&quot;%s - %s\n&quot;, lua_typename(L, lua_type(L, -2)), lua_typename(L,
	 * 			lua_type(L, -1)));
	 * 	// removes 'value'; keeps 'key' for next iteration 
	 * 	lua_pop(L, 1);
	 * }
	 * </pre>
	 * 
	 * <p>
	 * While traversing a table, do not call <a href="#lua_tolstring"><code>lua_tolstring</code></a>
	 * directly on a key, unless you know that the key is actually a string.
	 * Recall that <a href="#lua_tolstring"><code>lua_tolstring</code></a>
	 * <em>changes</em> the value at the given index; this confuses the next
	 * call to <a href="#lua_next"><code>lua_next</code></a>.
	 */
	public int next(int index) {
		notImplemented();
		return 0;
	}

	/**
	 * Get the length of an object <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Returns the "length" of the value at the given acceptable index: for
	 * strings, this is the string length; for tables, this is the result of the
	 * length operator ('<code>#</code>'); for userdata, this is the size of
	 * the block of memory allocated for the userdata; for other values, it
	 * is&nbsp;0.
	 */
	public int objlen(int index) {
		return tostring(index).length();
	}


	/**
	 * Pops <code>n</code> elements from the stack. <span class="apii">[-n,
	 * +0, <em>-</em>]</span>
	 */
	public void pop(int n) {
		for ( int i=0; i<n; i++ )
			stack[--top] = null;
	}
	private LValue poplvalue() {
		LValue p = stack[--top];
		stack[top] = null;
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
	 * Pushes a Java&nbsp;function onto the stack. <span class="apii">[-0, +1,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * This function receives a pointer to a C function and pushes onto the
	 * stack a Lua value of type <code>function</code> that, when called,
	 * invokes the corresponding C&nbsp;function.
	 * 
	 * 
	 * <p>
	 * Any function to be registered in Lua must follow the correct protocol to
	 * receive its parameters and return its results (see <a
	 * href="#lua_CFunction"><code>lua_CFunction</code></a>).
	 * 
	 */
	public void pushjavafunction(LFunction f) {
		pushlvalue( f );
	}

	/**
	 * Pushes a light userdata onto the stack. <span class="apii">[-0, +1,
	 * <em>-</em>]</span>
	 * 
	 * 
	 * <p>
	 * Userdata represent C&nbsp;values in Lua. A <em>light userdata</em>
	 * represents a pointer. It is a value (like a number): you do not create
	 * it, it has no individual metatable, and it is not collected (as it was
	 * never created). A light userdata is equal to "any" light userdata with
	 * the same C&nbsp;address.
	 */
	public void pushlightuserdata(Object p) {
		notImplemented();
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
	 * Push a thread onto the stack. <span class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 * Pushes the thread represented by <code>L</code> onto the stack. Returns
	 * 1 if this thread is the main thread of its state.
	 */
	public void pushthread() {
		notImplemented();
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
	 * Do a table get without metadata calls. <span class="apii">[-1, +1,
	 * <em>-</em>]</span>
	 * 
	 * <p>
	 * Similar to <a href="#lua_gettable"><code>lua_gettable</code></a>, but
	 * does a raw access (i.e., without metamethods).
	 */
	public void rawget(int index) {
		pushlvalue( totable(index).get(poplvalue()) );
	}

	/**
	 * Do a integer-key table get without metadata calls. <span
	 * class="apii">[-0, +1, <em>-</em>]</span>
	 * 
	 * <p>
	 * Pushes onto the stack the value <code>t[n]</code>, where
	 * <code>t</code> is the value at the given valid index. The access is
	 * raw; that is, it does not invoke metamethods.
	 */
	public void rawgeti(int index, int n) {
		pushlvalue( totable(index).get(n) );
	}

	/**
	 * Do a table set without metadata calls. <span class="apii">[-2, +0,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * Similar to <a href="#lua_settable"><code>lua_settable</code></a>, but
	 * does a raw assignment (i.e., without metamethods).
	 */
	public void rawset(int index) {
		LTable t = totable(index);
		LValue v = poplvalue();
		LValue k = poplvalue();
		t.put(k,v);
	}

	/**
	 * Do a integer-key table set without metadata calls. <span
	 * class="apii">[-1, +0, <em>m</em>]</span>
	 * 
	 * <p>
	 * Does the equivalent of <code>t[n] = v</code>, where <code>t</code>
	 * is the value at the given valid index and <code>v</code> is the value
	 * at the top of the stack.
	 * 
	 * 
	 * <p>
	 * This function pops the value from the stack. The assignment is raw; that
	 * is, it does not invoke metamethods.
	 */
	public void rawseti(int index, int n) {
		LTable t = totable(index);
		LValue v = poplvalue();
		t.put(n,v);
	}

	/**
	 * Register a LFunction with a specific name. <span class="apii">[-0, +0,
	 * <em>m</em>]</span>
	 * 
	 * <p>
	 * Sets the function <code>f</code> as the new value of global
	 * <code>name</code>. It is defined as a macro:
	 * 
	 * <pre>
	 * 	 #define lua_register(L,n,f) \
	 * 	 (lua_pushcfunction(L, f), lua_setglobal(L, n))
	 * 	
	 * </pre>
	 */
	public void register(String name, LFunction f) {
		pushjavafunction(f);
		setglobal(name);
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
		--top;
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
	 * Starts and resumes a coroutine in a given thread. <span class="apii">[-?,
	 * +?, <em>-</em>]</span>
	 * 
	 * 
	 * <p>
	 * To start a coroutine, you first create a new thread (see <a
	 * href="#lua_newthread"><code>lua_newthread</code></a>); then you push
	 * onto its stack the main function plus any arguments; then you call <a
	 * href="#lua_resume"><code>lua_resume</code></a>, with
	 * <code>narg</code> being the number of arguments. This call returns when
	 * the coroutine suspends or finishes its execution. When it returns, the
	 * stack contains all values passed to <a href="#lua_yield"><code>lua_yield</code></a>,
	 * or all values returned by the body function. <a href="#lua_resume"><code>lua_resume</code></a>
	 * returns <a href="#pdf-LUA_YIELD"><code>LUA_YIELD</code></a> if the
	 * coroutine yields, 0 if the coroutine finishes its execution without
	 * errors, or an error code in case of errors (see <a href="#lua_pcall"><code>lua_pcall</code></a>).
	 * In case of errors, the stack is not unwound, so you can use the debug API
	 * over it. The error message is on the top of the stack. To restart a
	 * coroutine, you put on its stack only the values to be passed as results
	 * from <code>yield</code>, and then call <a href="#lua_resume"><code>lua_resume</code></a>.
	 */
	public void resume(int narg) {
		notImplemented();
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
	public void setfield(int index, String k) {
		LTable t = totable(index);
		LValue v = poplvalue();
		t.luaSetTable(this, t, new LString(k), v);
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
		LTable g = this._G;
		LValue v = poplvalue();
		g.luaSetTable(this, g, new LString(name), v);
	}

	/**
	 * Set the metatable of a value. <span class="apii">[-1, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Pops a table from the stack and sets it as the new metatable for the
	 * value at the given acceptable index.
	 */
	public void setmetatable(int index) {
		LTable t = totable(index);
		LValue v = poplvalue();
		t.luaSetMetatable(v);
	}

	/**
	 * Set the value of a table for a key. <span class="apii">[-2, +0,
	 * <em>e</em>]</span>
	 * 
	 * <p>
	 * Does the equivalent to <code>t[k] = v</code>, where <code>t</code>
	 * is the value at the given valid index, <code>v</code> is the value at
	 * the top of the stack, and <code>k</code> is the value just below the
	 * top.
	 * 
	 * 
	 * <p>
	 * This function pops both the key and the value from the stack. As in Lua,
	 * this function may trigger a metamethod for the "newindex" event (see <a
	 * href="#2.8">&sect;2.8</a>).
	 */
	public void settable(int index) {
		LTable t = totable(index);
		LValue v = poplvalue();
		LValue k = poplvalue();
		t.luaSetTable(this, t, k, v);
	}

	/**
	 * Returns the status of the thread <code>L</code>. <span
	 * class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * 
	 * 
	 * <p>
	 * The status can be 0 for a normal thread, an error code if the thread
	 * finished its execution with an error, or <a name="pdf-LUA_YIELD"><code>LUA_YIELD</code></a>
	 * if the thread is suspended.
	 * 
	 */
	public void status() {
		notImplemented();
	}

	/**
	 * Get a thread value from the stack. <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <p>
	 * Converts the value at the given acceptable index to a Lua thread
	 * (represented as <code>lua_State*</code>). This value must be a thread;
	 * otherwise, the function returns <code>NULL</code>.
	 */
	public LuaState tothread(int index) {
		notImplemented();
		return null;
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
		return topointer(index).toJavaInt();
	}

	/**
	 * Get a value as a JavaFunction.
	 * <hr>
	 * <h3><a name="lua_tocfunction"><code>lua_tocfunction</code></a></h3>
	 * <p>
	 * <span class="apii">[-0, +0, <em>-</em>]</span>
	 * 
	 * <pre>
	 * lua_CFunction lua_tocfunction (lua_State *L, int index);
	 * </pre>
	 * 
	 * <p>
	 * Converts a value at the given acceptable index to a C&nbsp;function. That
	 * value must be a C&nbsp;function; otherwise, returns <code>NULL</code>.
	 */
	public LFunction tojavafunction(int index) {
		return (LFunction) topointer(index);
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
		return topointer(index).toJavaDouble();
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
		while ( top < ant )
			stack[top++] = LNil.NIL;
		while ( top > ant )
			stack[--top] = null;
	}

	/** 
	 * Set the top to the base.  Equivalent to settop(0) 
	 */
	public void resettop() {
		debugAssert( top >= base );
		while ( top > base )
			stack[--top] = null;
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
			System.arraycopy(stack, top-n, ss.stack, ss.top, n);
			ss.top += n;
		}
	}

	/**
	 * Yields a coroutine. <span class="apii">[-?, +?, <em>-</em>]</span>
	 * 
	 * 
	 * <p>
	 * This function should only be called as the return expression of a
	 * C&nbsp;function, as follows:
	 * 
	 * <pre>
	 * return lua_yield(L, nresults);
	 * </pre>
	 * 
	 * <p>
	 * When a C&nbsp;function calls <a href="#lua_yield"><code>lua_yield</code></a>
	 * in that way, the running coroutine suspends its execution, and the call
	 * to <a href="#lua_resume"><code>lua_resume</code></a> that started
	 * this coroutine returns. The parameter <code>nresults</code> is the
	 * number of values from the stack that are passed as results to <a
	 * href="#lua_resume"><code>lua_resume</code></a>.
	 * 
	 */
	public void yield(int nresults) {
		notImplemented();
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
			newuserdata( o );
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
	
}
