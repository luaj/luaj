package lua;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Stack;

import lua.io.Closure;
import lua.io.LoadState;
import lua.io.LocVars;
import lua.io.Proto;
import lua.io.UpVal;
import lua.value.LBoolean;
import lua.value.LDouble;
import lua.value.LInteger;
import lua.value.LNil;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LUserData;
import lua.value.LValue;


public class StackState extends Lua implements VM {
    private static final boolean TRACE = (null != System.getProperty("TRACE"));

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
    protected LValue[] stack = new LValue[LUA_MINSTACK];
    public int cc = -1;
    protected CallInfo[] calls = new CallInfo[LUA_MINCALLS];
    protected Stack upvals = new Stack();
	protected JavaFunction panic;
    
    public LTable _G = GlobalState.getGlobalsTable();

    // main debug hook, overridden by DebugStackState
    protected void debugHooks(int pc) {
    }
    protected void debugPcallError(Throwable t) {       
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
	public StackState() {		
	}
    
    // ================ interfaces for performing calls
    
    public void push( int value ) {
        stack[top++] = LInteger.valueOf(value);
    }

    public void push( String value ) {
        stack[top++] = new LString(value);
    }

    public void push( double value ) {
        stack[top++] = new LDouble(value);
    }

    public void push( boolean value ) {
        stack[top++] = value? LBoolean.TRUE: LBoolean.FALSE;
    }

    public void push( LValue value ) {
        stack[top++] = value;
    }
    
    public void prepStackCall() {
        Closure c = (Closure) stack[base];
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
        calls[newcc] = new CallInfo(c, base, resultbase, nresults);
        cc = newcc;
    }
    
    public void execute() {
        for ( int cb=cc; cc>=cb; )
            exec();
    }
    
    public void doCall( Closure c, LValue[] args ) {
        setResult( c );
        for ( int i=0, n=(args!=null? args.length: 0); i<n; i++ )
            push( args[i] );
        prepStackCall();
        execute();
        base = (cc>=0? calls[cc].base: 0);
    }

    public void setExpectedResultCount( int nresults ) {
        this.nresults = nresults;
    }
    
    public int getExpectedResultCount() {
        return this.nresults;
    }

	public void invokeJavaFunction(JavaFunction javaFunction) {
		int resultbase = base;
		int resultsneeded = nresults;
		++base;
		int nactual = javaFunction.invoke(this);
		debugAssert(nactual>=0);
		debugAssert(top-nactual>=base);
		base = resultbase;
		System.arraycopy(stack, top-nactual, stack, base, nactual);
		settop( nactual );
		if ( resultsneeded >= 0 )
			settop( resultsneeded );
	}
	
    // ================== error processing =================
    
    // return 0 for success, non-zero for error condition
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

    // return 0 for success, non-zero for error condition
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
            debugPcallError( t );
            this.base = oldbase;
            this.cc = oldcc;
            close(oldtop);  /* close eventual pending closures */
            String s = t.getMessage();
            setResult( s!=null? s: t.toString() ); 
            return (t instanceof OutOfMemoryError? LUA_ERRMEM: LUA_ERRRUN);
        }
    }

    // loader
    public int load( InputStream is, String chunkname ) {
        try {
            Proto p = LoadState.undump(this, is, chunkname );
            push( new Closure( this, p ) );
            return 0;
        } catch ( Throwable t ) {
            setResult( t.getMessage() ); 
            return (t instanceof OutOfMemoryError? LUA_ERRMEM: LUA_ERRSYNTAX);
        }
    }
    
    // ================ interfaces for being called
    
    public int getArgCount() {
        return top - (base + 1);
    }
    
    public LValue getArg(int index) {
        int off = (base + 1) + index;
        return off < top? stack[off]: LNil.NIL;
    }
    
    public int getArgAsInt( int index ) {
        return getArg(index).toJavaInt();
    }

    public double getArgAsDouble( int index ) {
        return getArg(index).toJavaDouble();
    }   
    
    public boolean getArgAsBoolean( int index ) {
        return getArg(index).toJavaBoolean();
    }

    public LString getArgAsLuaString( int index ) {
        return getArg(index).luaAsString();
    }

    public String getArgAsString( int index ) {
        return getArgAsLuaString(index).toJavaString();
    }   

    public void setResult() {
        top = base;
    }
    
    public void setResult( LValue value ) {
        setResult();
        push( value );
    }
    
    public void setResult( String value ) {
        setResult();
        push( value );
    }
    
    public void setErrorResult(LValue value, String message) {
        setResult();
        push( value );
        push( message );
    }
    
    public void adjustResults() {
        if ( nresults != LUA_MULTRET ) {
            adjustTop( base + nresults );
        }
        // Copy base back from value in call frame
        base = (cc >= 0) ? calls[cc].base : 0;
    }
    
    // ================ execute instructions
    private LValue RKBC(LValue[] k, int bc) {
        return StackState.ISK(bc) ? 
                k[StackState.INDEXK(bc)]:
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
    
    /** execute instructions up to a yield, return, or call */
    public void exec() {
        if ( cc < 0 )
            return;

        int i, a, b, c, o, n, cb;
        LValue rkb, rkc, nvarargs, key, val;
        LValue i0, step, idx, limit, init, table;
        boolean back, body;
        Proto proto;
        Closure newClosure;

        // reload values from the current call frame 
        // into local variables
        CallInfo ci = calls[cc];
        Closure cl = ci.closure;
        Proto p = cl.p;
        int[] code = p.code;
        LValue[] k = p.k;
        
        this.base = ci.base;
        
        // loop until a return instruction is processed, 
        // or the vm yields
        while (true) {
            debugAssert( ci == calls[cc] );
    
            // sync up top
            ci.top = top;
        	
            if (TRACE) {
                Print.printState(this, base, top, base+p.maxstacksize, cl, ci.pc);
                for (int j = 0; j <= cc; j++) {
                    System.out.println("calls[" + j + "]: " + calls[j].base + "," + calls[j].top);
                    LocVars[] localVars = calls[j].closure.p.locvars;
                    for (int t = 0; t < localVars.length; t++)
                        System.out.println("localVars[" + t + "]: " + localVars[t].varname);
                }
            }
            
            // allow debug hooks a chance to operate
            debugHooks( ci.pc );
            
            // advance program counter
            i = code[ci.pc++];
            
            // get first opcode arg
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
                this.stack[base + a] = (b != 0 ? LBoolean.TRUE : LBoolean.FALSE);
                if (c != 0)
                    ci.pc++; /* skip next instruction (if C) */
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
                top = base + a;
                table.luaGetTable(this, table, key);
                continue;
            }
            case StackState.OP_GETTABLE: {
                b = StackState.GETARG_B(i);
                key = GETARG_RKC(k, i);
                table = this.stack[base + b];
                top = base + a;
                table.luaGetTable(this, table, key);
                continue;
            }
            case StackState.OP_SETGLOBAL: {
                b = StackState.GETARG_Bx(i);
                key = k[b];
                val = this.stack[base + a];
                table = cl.env;
                table.luaSetTable(this, table, key, val);
                continue;
            }
            case StackState.OP_SETUPVAL: {
                b = StackState.GETARG_B(i);
                cl.upVals[b].setValue( this.stack[base + a] );
                continue;
            }
            case StackState.OP_SETTABLE: {
                key = GETARG_RKB(k, i);
                val = GETARG_RKC(k, i);
                table = this.stack[base + a];
                table.luaSetTable(this, table, key, val);
                continue;
            }
            case StackState.OP_NEWTABLE: {
                b = StackState.GETARG_B(i);
                c = StackState.GETARG_C(i);
                this.stack[base + a] = new LTable(b, c);
                continue;
            }
            case StackState.OP_SELF: {
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
            case StackState.OP_ADD:
            case StackState.OP_SUB:
            case StackState.OP_MUL:
            case StackState.OP_DIV:
            case StackState.OP_MOD:
            case StackState.OP_POW: {
                o = StackState.GET_OPCODE(i);
                rkb = GETARG_RKB(k, i);
                rkc = GETARG_RKC(k, i);
                this.stack[base + a] = rkc.luaBinOpUnknown(o, rkb);
                continue;
            }
            case StackState.OP_UNM: {
                rkb = GETARG_RKB(k, i);
                this.stack[base + a] = rkb.luaUnaryMinus();
                continue;
            }
            case StackState.OP_NOT: {
                rkb = GETARG_RKB(k, i);
                this.stack[base + a] = (!rkb.toJavaBoolean() ? LBoolean.TRUE
                        : LBoolean.FALSE);
                continue;
            }
            case StackState.OP_LEN: {
                rkb = GETARG_RKB(k, i);
                this.stack[base + a] = LInteger.valueOf( rkb.luaLength() );
                continue;
            }
            case StackState.OP_CONCAT: {
                b = StackState.GETARG_B(i);
                c = StackState.GETARG_C(i);
                int numValues = c - b + 1;
                LString[] strings = new LString[numValues];
                
                for (int j = b, l = 0; j <= c; j++, l++) {
                    LString s = this.stack[base + j].luaAsString();
                    strings[l] = s;
                }
                this.stack[base + a] = LString.concat( strings );
                continue;
            }
            case StackState.OP_JMP: {
                ci.pc += StackState.GETARG_sBx(i);
                continue;
            }
            case StackState.OP_EQ:
            case StackState.OP_LT:
            case StackState.OP_LE: {
                o = StackState.GET_OPCODE(i);
                rkb = GETARG_RKB(k, i);
                rkc = GETARG_RKC(k, i);
                boolean test = rkc.luaBinCmpUnknown(o, rkb);
                if (test == (a == 0))
                    ci.pc++;
                continue;
            }
            case StackState.OP_TEST: {
                c = StackState.GETARG_C(i);
                if (this.stack[base + a].toJavaBoolean() != (c != 0))
                    ci.pc++;
                continue;
            }
            case StackState.OP_TESTSET: {
                rkb = GETARG_RKB(k, i);
                c = StackState.GETARG_C(i);
                if (rkb.toJavaBoolean() != (c != 0))
                    ci.pc++;
                else
                    this.stack[base + a] = rkb;
                continue;
            }
            case StackState.OP_CALL: {               
                
                // ra is base of new call frame
                this.base += a;
                
                // number of args
                b = StackState.GETARG_B(i);
                if (b != 0) // else use previous instruction set top
                    top = base + b;
                
                // number of return values we need
                c = StackState.GETARG_C(i);

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
            
            case StackState.OP_TAILCALL: {
                close(base);

                // copy down the frame before calling!

                // number of args (including the function)
                b = StackState.GETARG_B(i);
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
                } catch (RuntimeException e) {
                    // in case of error, we need to restore cc so that
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

            case StackState.OP_RETURN: {
                // number of return vals to return
                b = StackState.GETARG_B(i) - 1; 
                if (b == -1)
                    b = top - (base + a);

                // close open upvals
                close( base ); 

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
                    ci.pc += StackState.GETARG_sBx(i);
                }
                continue;
            }
            case StackState.OP_FORPREP: {
                init = this.stack[base + a];
                step = this.stack[base + a + 2];
                this.stack[base + a] = step.luaBinOpUnknown(Lua.OP_SUB,
                        init);
                b = StackState.GETARG_sBx(i);
                ci.pc += b;
                continue;
            }
            case StackState.OP_TFORLOOP: {
                cb = a + 3; /* call base */
                System.arraycopy(this.stack, base + a, this.stack,
                        base + cb, 3);
                base += cb;
                try {
                    top = base + 3; /* func. + 2 args (state and index) */
                    
                    // call the iterator
                    c = StackState.GETARG_C(i);
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
            case StackState.OP_SETLIST: {
                b = StackState.GETARG_B(i);
                c = StackState.GETARG_C(i);
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
            case StackState.OP_CLOSE: {
                close( a ); // close upvals higher in the stack than position a
                continue;
            }
            case StackState.OP_CLOSURE: {
                b = StackState.GETARG_Bx(i);
                proto = cl.p.p[b];
                newClosure = new Closure(this, proto);
                for (int j = 0; j < newClosure.upVals.length; j++, ci.pc++) {
                    i = code[ci.pc];
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
                n = nvarargs.toJavaInt();
                if (b == StackState.LUA_MULTRET) {
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
            if ( up.stack == this.stack && up.position == target ) {
                return up;
            } else if ( up.position < target ) {
                break;
            }
        }
        
        up = new UpVal( upValName, this.stack, target );
        this.upvals.insertElementAt( up, i + 1 );
        return up;
    }
    
    private void close( int limit ) {
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
		throw new java.lang.RuntimeException("AbstractStack: not yet implemented");
	}
	
	public JavaFunction atpanic(JavaFunction panicf) {
		JavaFunction f = panic;
		panic = panicf;
		return f;
	}
	
    public void error(String message, int level) {
        error( message );
    }
    
    public void error(String message) {
        throw new RuntimeException( message );
    }
    
	public void error() {
		String message = tostring(-1);
		pop(1);
		error( message );
	}
	
	public void dump() {
		notImplemented();
	}
	
	public void pushclosure(JavaFunction fn, int n) {
		notImplemented();
	}

	public int javapcall(JavaFunction func, Object ud) {
		this.pushjavafunction(func);
		this.pushlightuserdata(ud);
		return this.pcall(1, 0, 0);
	}

	public String pushfstring(String fmt, Object[] args) {
		notImplemented();
		return null;
	}
	public void pushvfstring(String format, Object[] args) {
		notImplemented();
	}

	public void rawequal(int index1, int index2) {
		notImplemented();
	}

	public void getfenv(int index) {
		notImplemented();
	}
	
	public int setfenv(int index) {
		notImplemented();
		return 0;
	}

	
	public void checkstack(int extra) {
		if ( top + extra > stack.length ) {
			int n = Math.max( top + extra + LUA_MINSTACK, stack.length * 2 );
			LValue[] s = new LValue[n];
			System.arraycopy(stack, 0, s, 0, stack.length);
			stack = s;			
		}
	}
	
	public void close() {
		stack = new LValue[20];
		base = top = 0;
	}
	
	public void concat(int n) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for ( int i=-n; i<0; i++ ) {
			LString ls = tolstring(i);
			baos.write(ls.m_bytes, ls.m_offset, ls.m_length);
		}
		pop(n);
		pushlvalue( new LString(baos.toByteArray()));
	}
	
	public void createtable(int narr, int nrec) {
		stack[top++] = new LTable(narr, nrec);
	}
	
	public boolean equal(int index1, int index2) {
		return topointer(index2).luaBinOpUnknown(Lua.OP_EQ,
				topointer(index1)).toJavaBoolean();
	}
	
	public void gc(int what, int data) {
		notImplemented();
	}
	
	public void getfield(int index, String k) {
		LTable t = totable(index);
		// TODO: what if this triggers metatable ops
		// pushlvalue( t.luaGetTable(this, t, new LString(k)) );
		t.luaGetTable(this, t, new LString(k));
	}
	
	public void getglobal(String s) {
		LTable t = this._G;
		// TODO: what if this triggers metatable ops
		// pushlvalue( t.luaGetTable(this, t, new LString(s)));
		t.luaGetTable(this, t, new LString(s));
	}
	
	public int getmetatable(int index) {
		LTable mt = topointer(index).luaGetMetatable();
		if ( mt != null ) {
			pushlvalue( mt );
			return 1;			
		}
		return 0;
	}
	
	public void gettable(int index) {
		LValue t = totable(index);
		LValue k = poplvalue();
		// todo: what if this triggers metatable ops
		// pushlvalue( t.luaGetTable(this, t, k) );
		t.luaGetTable(this, t, k);
	}
	
	public void insert(int index) {
		int ai = index2adr(index);
		LValue v = stack[top-1];
		System.arraycopy(stack, ai, stack, ai+1, top-ai-1);
		stack[ai] = v;
	}
	
	public boolean isboolean(int index) {
		return type(index) == Lua.LUA_TBOOLEAN;
	}
	
	public boolean isfunction(int index) {
		return type(index) == Lua.LUA_TFUNCTION;
	}

	public boolean isjavafunction(int index) {
		return topointer(index) instanceof JavaFunction;
	}

	public boolean islightuserdata(int index) {
		return false;
	}

	public boolean isnil(int index) {
		return topointer(index) == LNil.NIL;
	}

	public boolean isnone(int index) {
		return topointer(index) == null;
	}

	public boolean isnoneornil(int index) {
		Object v = topointer(index);
		return v == null || v == LNil.NIL;
	}

	public boolean isnumber(int index) {
		return type(index) == Lua.LUA_TNUMBER;
	}

	public boolean isstring(int index) {
		return type(index) == Lua.LUA_TSTRING;
	}

	public boolean istable(int index) {
		return type(index) == Lua.LUA_TTABLE;
	}

	public boolean isthread(int index) {
		return type(index) == Lua.LUA_TTHREAD;
	}

	public boolean isuserdata(int index) {
		return type(index) == Lua.LUA_TUSERDATA;
	}

	public boolean lessthan(int index1, int index2) {
		return topointer(index2).luaBinOpUnknown(Lua.OP_LT,
				topointer(index1)).toJavaBoolean();
	}

	public void newtable() {
		stack[top++] = new LTable();
	}

	public void newthread() {
		notImplemented();
	}

	public void newuserdata(Object o) {
		stack[top++] = new LUserData(o);
	}

	public int next(int index) {
		notImplemented();
		return 0;
	}

	public int objlen(int index) {
		return tostring(index).length();
	}

	public void pop(int n) {
		for ( int i=0; i<n; i++ )
			stack[--top] = null;
	}
	private LValue poplvalue() {
		LValue p = stack[--top];
		stack[top] = null;
		return p;
		
	}
	
	public void pushlvalue(LValue value) {
		if ( value == null )
			throw new java.lang.IllegalArgumentException("stack values cannot be null");
		stack[top++] = value;
	}

	public void pushboolean(boolean b) {
		pushlvalue( LBoolean.valueOf(b) );
	}


	public void pushinteger(int n) {
		pushlvalue( LInteger.valueOf(n) );
	}

	public void pushjavafunction(JavaFunction f) {
		pushlvalue( f );
	}

	public void pushlightuserdata(Object p) {
		notImplemented();
	}


	public void pushlstring(LString s) {
		pushlvalue(s);
	}
	
	public void pushlstring(byte[] bytes, int offset, int length) {
		pushlvalue(new LString(bytes, offset, length));
	}

	public void pushlstring(byte[] byteArray) {
		pushlstring(byteArray, 0, byteArray.length);
	}
	
	public void pushnil() {
		pushlvalue(LNil.NIL);
	}

	public void pushnumber(double d) {
		pushlvalue(new LDouble(d));
	}

	public void pushstring(String s) {
		if ( s == null )
			pushnil();
		else
			pushlstring( LString.valueOf(s) );
	}
	
	public void pushthread() {
		notImplemented();
	}

	public void pushvalue(int index) {
		pushlvalue(topointer(index));
	}

	public void rawget(int index) {
		pushlvalue( totable(index).get(poplvalue()) );
	}

	public void rawgeti(int index, int n) {
		pushlvalue( totable(index).get(n) );
	}

	public void rawset(int index) {
		LTable t = totable(index);
		LValue v = poplvalue();
		LValue k = poplvalue();
		t.put(k,v);
	}

	public void rawseti(int index, int n) {
		LTable t = totable(index);
		LValue v = poplvalue();
		t.put(n,v);
	}

	public void register(String name, JavaFunction f) {
		pushjavafunction(f);
		setglobal(name);
	}

	public void remove(int index) {
		int ai = index2adr(index);
		System.arraycopy(stack, ai+1, stack, ai, top-ai-1);
		--top;
	}

	public void replace(int index) {
		int ai = index2adr(index);
		stack[ai] = poplvalue();
	}

	public void resume(int narg) {
		notImplemented();
	}

	public void setfield(int index, String k) {
		LTable t = totable(index);
		LValue v = poplvalue();
		t.luaSetTable(this, t, new LString(k), v);
	}

	public void setglobal(String name) {
		LTable g = this._G;
		LValue v = poplvalue();
		g.luaSetTable(this, g, new LString(name), v);
	}

	public void setmetatable(int index) {
		LTable t = totable(index);
		LValue v = poplvalue();
		t.luaSetMetatable(v);
	}

	public void settable(int index) {
		LTable t = totable(index);
		LValue v = poplvalue();
		LValue k = poplvalue();
		t.luaSetTable(this, t, k, v);
	}

	public void status() {
		notImplemented();
	}

	public StackState tothread(int index) {
		notImplemented();
		return null;
	}

	public boolean toboolean(int index) {
		return topointer(index).toJavaBoolean();
	}

	public int tointeger(int index) {
		return topointer(index).toJavaInt();
	}

	public JavaFunction tojavafunction(int index) {
		return (JavaFunction) topointer(index);
	}

	public LString tolstring(int index) {
		return topointer(index).luaAsString();
	}

	public double tonumber(int index) {
		return topointer(index).toJavaDouble();
	}

	public int gettop() {
		return top - base;
	}
	
	public void settop(int nt) {
		int ant = nt>=0? base+nt: top+nt;
		if ( ant < base )
			throw new IllegalArgumentException("index out of bounds: "+ant );
		while ( top < ant )
			stack[top++] = LNil.NIL;
		while ( top > ant )
			stack[--top] = null;
	}

	private int index2adr(int index) {
		// TODO: upvalues? globals? environment?   
		int ai = index>0? base+index-1: top+index;
		if ( ai < base )
			throw new IllegalArgumentException("index out of bounds: "+ai );
		return ai;
	}
	
	public LValue topointer(int index) {
		int ai = index2adr(index);
		if ( ai >= top )
			return LNil.NIL;
		return stack[ai];
	}

	public String tostring(int index) {
		return topointer(index).toJavaString();
	}
	
	public LTable totable(int index) {
		return (LTable) topointer(index);
	}

	public Object touserdata(int index) {
		LValue v = topointer(index);
		if ( v.luaGetType() != Lua.LUA_TUSERDATA )
			return null;
		return ((LUserData)v).m_instance;
	}

	public int type(int index) {
		return topointer(index).luaGetType();
	}

	public String typename(int index) {
		return topointer(index).luaGetTypeName().toJavaString();
	}

	public void xmove(VM to, int n) {
		if ( n > 0 ) {
			to.checkstack(n);
			StackState ss = (StackState)to;
			System.arraycopy(stack, top-n, ss.stack, ss.top, n);
			ss.top += n;
		}
	}

	public void yield(int nresults) {
		notImplemented();
	}
	
	// ============================= conversion to and from Java boxed types ====================

	public void pushboolean(Boolean b) {
		if ( b == null )
			pushnil();
		else
			pushboolean( b.booleanValue() );
	}
	
	public void pushinteger(Byte b) {
		if ( b == null )
			pushnil();
		else
			pushinteger( b.byteValue() );
	}
	
	public void pushinteger(Character c) {
		if ( c == null )
			pushnil();
		else
			pushinteger( c.charValue() );
	}
	
	public void pushnumber(Double d) {
		if ( d == null )
			pushnil();
		else
			pushnumber( d.doubleValue() );
	}
	
	public void pushnumber(Float f) {
		if ( f == null )
			pushnil();
		else
			pushnumber( f.doubleValue() );
	}
	
	public void pushinteger(Integer i) {
		if ( i == null )
			pushnil();
		else
			pushinteger( i.intValue() );
	}
	
	public void pushinteger(Short s) {
		if ( s == null )
			pushnil();
		else
			pushinteger( s.shortValue() );
	}
	
	public void pushnumber(Long l) {
		if ( l == null )
			pushnil();
		else
			pushnumber( l.doubleValue() );
	}
	
	public void pushuserdata( Object o ) {
		if ( o == null )
			pushnil();
		else
			newuserdata( o );
	}

	public Boolean toboxedboolean(int index) {
		return topointer(index).toJavaBoxedBoolean();
	}
	
	public Byte toboxedbyte(int index) {
		return topointer(index).toJavaBoxedByte();
	}
	
	public Double toboxeddouble(int index) {
		return topointer(index).toJavaBoxedDouble();
	}
	
	public Float toboxedfloat(int index) {
		return topointer(index).toJavaBoxedFloat();
	}
	
	public Integer toboxedinteger(int index) {
		return topointer(index).toJavaBoxedInteger();
	}
	
	public Long toboxedlong(int index) {
		return topointer(index).toJavaBoxedLong();
	}
	
}
