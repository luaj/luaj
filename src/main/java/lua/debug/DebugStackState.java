package lua.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import lua.CallInfo;
import lua.Print;
import lua.StackState;
import lua.io.LocVars;
import lua.io.Proto;

public class DebugStackState extends StackState implements DebugRequestListener {

	public Map<Integer,Boolean> breakpoints = new HashMap<Integer,Boolean>();
	private boolean exiting = false;
	private boolean suspended = false;
	private boolean stepping = false;
	private int lastline = -1;
	
	public DebugStackState() {
	}
	
	private String getFileLine(int cindex) {
		String func = "?";
		String line = "?";
		String source = "?";
		if ( cindex >= 0 ) {
			CallInfo call = this.calls[cindex];
			Proto p = call.closure.p;
			if ( p != null && p.source != null )
				source = p.source.toJavaString();
			if ( p.lineinfo != null && p.lineinfo.length > call.pc-1 )
				line = String.valueOf( p.lineinfo[call.pc-1] );
			// TODO: reverse lookup on function name ????
			func = call.closure.luaAsString().toJavaString();
		}
		return source+":"+line+"("+func+")";
	}
	
	private void printLuaTrace(String message) {
		System.out.println( "Lua error: "+message );
		for ( int cindex=cc-1; cindex>=0; cindex-- )
			System.out.println( "\tcalled by "+getFileLine( cindex ) );
	}
	
	// intercept exceptions and fill in line numbers
	public void exec() {
		try {
			super.exec();
		} catch ( RuntimeException t ) {
			String message = getFileLine(cc)+": "+t.getMessage();
			t.printStackTrace();			
			printLuaTrace(message);
			System.out.flush();
			throw new RuntimeException( message, t );
		}
	}
	
	
	// debug hooks
	public void debugHooks( int pc ) {
		if ( exiting )
			throw new java.lang.RuntimeException("exiting");

		synchronized ( this ) {
			
			// anytime the line doesn't change we keep going
			int[] li = calls[cc].closure.p.lineinfo;
			int line = (li!=null && li.length>pc? li[pc]: -1);
			if ( lastline == line )
				return;

			// save line in case next op is a step
			lastline = line;
			if ( stepping )
				stepping = false;

			// check for a break point if we aren't suspended already
			if ( ! suspended ) {
				if ( breakpoints.containsKey(line) )
					suspended = true;
				else
					return;
			}
			
			// wait for a state change
			while ( suspended && (!exiting) && (!stepping) ) {
				try {
					this.wait();
				} catch ( InterruptedException ie ) {
					ie.printStackTrace();
				}
			}
		}
	}
	
	// ------------------ commands coming from the debugger -------------------
    
    public enum RequestType {
        suspend,
        resume,
        exit, 
        set, 
        clear, 
        callgraph, 
        stack,
        step,
        variable,
    }
    
	public String handleRequest(String request) {
    	StringTokenizer st = new StringTokenizer( request );
    	String req = st.nextToken();
    	RequestType rt = RequestType.valueOf(req);
    	switch ( rt ) {
    	case suspend: suspend(); return "true";
    	case resume: resume(); return "true";
    	case exit: exit(); return "true";
    	case set: set( Integer.parseInt(st.nextToken()) ); return "true";
    	case clear: clear( Integer.parseInt(st.nextToken()) ); return "true";
    	case callgraph: return callgraph();
    	case stack: return stack();
    	case step: step(); return "true";
    	case variable: 
    		String N = st.nextToken();
    		String M = st.nextToken();
    		return variable( Integer.parseInt(N), Integer.parseInt(M) );
    	}
    	throw new java.lang.IllegalArgumentException( "unkown request type: "+req );
	}

    /**
     * suspend the execution
     */
	public void suspend() {
		synchronized ( this ) {
			suspended = true;
			stepping = false;
			lastline = -1;
			this.notify();
		}
	}

	/** 
	 * resume the execution
	 */
	public void resume() {
		synchronized ( this ) {
			suspended = false;
			this.notify();
		}
	}
	
    /** 
     * terminate the execution
     */
	public void exit() {
		synchronized ( this ) {
			exiting = true;
			this.notify();
		}
	}
	
    /**
     * set breakpoint at line N
     * @param N the line to set the breakpoint at
     */
	public void set( int N ) {
		synchronized ( this ) {
			breakpoints.put( N, Boolean.TRUE );
		}
	}
	
    /**
     * clear breakpoint at line N
     */
	public void clear( int N ) {
		synchronized ( this ) {
			breakpoints.remove( N );
		}
	}
	
    /** 
     * return the current call graph (i.e. stack frames from
     * old to new, include information about file, method, etc.)
     */
	public String callgraph() {
		int n = cc;
		if ( n < 0 || n >= calls.length )
			return "";
		StringBuffer sb = new StringBuffer();
		for ( int i=0; i<=n; i++ ) {
			CallInfo ci = calls[i];
			// TODO: fill this out with proper format, names, etc.
			sb.append( String.valueOf(ci.closure.p) );
			sb.append( "\n" );
		}
		return sb.toString();
	}

	/**
	 *  return the content of the current stack frame,
     *  listing the (variable, value) pairs
     */
	public String stack() {
		CallInfo ci;
		if ( cc < 0 || cc >= calls.length || (ci=calls[cc]) == null )
			return "<out of scope>";
		LocVars[] lv = ci.closure.p.locvars;
		int n = (lv != null? lv.length: 0);
		StringBuffer sb = new StringBuffer();
		for ( int i=0; i<n; i++ ) {
			// TODO: figure out format 
			sb.append( "(" + lv[i].varname + "," + super.stack[ci.base+i] + ")\n" );
		}
		return sb.toString();
	}
	
	
    /**
     * single step forward (go to next statement)
     */
	public void step() {
		synchronized ( this ) {
			stepping = true;
			this.notify();
		}
	}
	
    /**
     * return the value of variable M from the stack frame N
	 *  (stack frames are indexed from 0)
	 */
	public String variable( int N, int M ) {
		CallInfo ci;
		if ( M < 0 || M >= calls.length || (ci=calls[M]) == null )
			return "<out of scope>";
		return String.valueOf( super.stack[ci.base] );
	}
}
