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
package lua.debug;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import lua.CallInfo;
import lua.Lua;
import lua.StackState;
import lua.addon.compile.LexState;
import lua.debug.event.DebugEvent;
import lua.debug.event.DebugEventBreakpoint;
import lua.debug.event.DebugEventType;
import lua.debug.request.DebugRequest;
import lua.debug.request.DebugRequestLineBreakpointToggle;
import lua.debug.request.DebugRequestListener;
import lua.debug.request.DebugRequestStack;
import lua.debug.request.DebugRequestType;
import lua.debug.response.DebugResponse;
import lua.debug.response.DebugResponseCallgraph;
import lua.debug.response.DebugResponseSimple;
import lua.debug.response.DebugResponseStack;
import lua.io.Closure;
import lua.io.LocVars;
import lua.io.Proto;
import lua.value.LTable;
import lua.value.LValue;

public class DebugStackState extends StackState implements DebugRequestListener {

	private static final boolean DEBUG = false;
    
	protected Hashtable breakpoints = new Hashtable();
    protected boolean exiting = false;
    protected boolean suspended = false;
    protected boolean stepping = false;
    protected int lastline = -1;
    protected DebugSupport debugSupport = null;

	public DebugStackState() {}
	
	public void setDebugSupport(DebugSupport debugSupport) throws IOException {
		if (debugSupport == null) {
			throw new IllegalArgumentException("DebugSupport cannot be null");
		}
		
		this.debugSupport = debugSupport;
		debugSupport.setDebugStackState(this);
		debugSupport.start();
	}
	
    protected void debugAssert(boolean b) {
    	if ( ! b ) 
    		error( "assert failure" );
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
			if ( p.lineinfo != null && p.lineinfo.length > call.pc )
				line = String.valueOf( p.lineinfo[call.pc] );
			// TODO: reverse lookup on function name ????
			func = call.closure.toJavaString();
		}
		return source+":"+line+"("+func+")";
	}
	
	
	// override and fill in line number info 
	public void error(String message) {
		super.error( getFileLine(cc)+": "+message );
	}
	
	private void printLuaTrace() {
		System.out.println( "Lua location: "+getFileLine(cc) );
		for ( int cindex=cc-1; cindex>=0; cindex-- )
			System.out.println( "\tin "+getFileLine( cindex ) );
	}
	
	// intercept exceptions and fill in line numbers
	public void exec() {
		try {
			super.exec();
		} catch (AbortException e) {
            // ignored. Client aborts the debugging session.
        } catch ( Exception t ) {        
			t.printStackTrace();
			printLuaTrace();
			System.out.flush();
		}
	}
	
	
	// debug hooks
	public void debugHooks( int pc ) {
		if ( exiting ) {
			throw new AbortException("aborted by debug client");
		}
		
		if(DebugUtils.IS_DEBUG)
        	DebugUtils.println("entered debugHook...");
        
		synchronized ( this ) {
            
			// anytime the line doesn't change we keep going
			int line = getLineNumber(calls[cc]);
			if ( !stepping && lastline == line ) {
				return;
            }
	        if(DebugUtils.IS_DEBUG)
	        	DebugUtils.println("debugHook - executing line: " + line);

			// save line in case next op is a step
			lastline = line;
            
            if ( stepping ) {
                DebugUtils.println("suspended by stepping at pc=" + pc);
                //TODO: notifyDebugEventListeners(new DebugEventStepping());
                suspended = true;
            } else if ( !suspended ) {
                // check for a break point if we aren't suspended already
                Proto p = calls[cc].closure.p;
                String source = DebugUtils.getSourceFileName(p.source);                
                if ( breakpoints.containsKey(constructBreakpointKey(source, line))){
                	if(DebugUtils.IS_DEBUG) {
                		DebugUtils.println("hitting breakpoint " + constructBreakpointKey(source, line));
                	}
                	if (debugSupport != null) {
                		debugSupport.notifyDebugEvent(new DebugEventBreakpoint(source, line));
                	}
                    suspended = true;
                } else {
                    return;
                }                    
			}
			
			// wait for a state change
			while (suspended && !exiting ) {
				try {
					this.wait();
					if(DebugUtils.IS_DEBUG)
						DebugUtils.println("resuming execution...");
				} catch ( InterruptedException ie ) {
					ie.printStackTrace();
				}
			}
		}
	}

    /**
     * Get the current line number
     * @param pc program counter
     * @return the line number corresponding to the pc
     */
    private int getLineNumber(CallInfo ci) {
        int[] lineNumbers = ci.closure.p.lineinfo;
        int pc = ci.pc;
        int line = (lineNumbers != null && lineNumbers.length > pc ? lineNumbers[pc] : -1);
        return line;
    }
	
	// ------------------ commands coming from the debugger -------------------   
    
	public DebugResponse handleRequest(DebugRequest request) {
		if (this.debugSupport == null) {
			throw new IllegalStateException("DebugStackState is not equiped with DebugSupport.");
		}
		
        DebugUtils.println("DebugStackState is handling request: " + request.toString());
        DebugRequestType requestType = request.getType();   
        if (DebugRequestType.start == requestType) {
            DebugEvent event = new DebugEvent(DebugEventType.started);
            debugSupport.notifyDebugEvent(event);                
            return DebugResponseSimple.SUCCESS;
        } else if (DebugRequestType.exit == requestType) {
            stop();
            return DebugResponseSimple.SUCCESS;
        } else if (DebugRequestType.suspend == requestType) { 
            suspend();          
            DebugEvent event = new DebugEvent(DebugEventType.suspendedByClient);
            debugSupport.notifyDebugEvent(event);                
            return DebugResponseSimple.SUCCESS;
    	} else if (DebugRequestType.resume == requestType) { 
            resume();
        	DebugEvent event = new DebugEvent(DebugEventType.resumedByClient);
            debugSupport.notifyDebugEvent(event);
            return DebugResponseSimple.SUCCESS;
    	} else if (DebugRequestType.lineBreakpointSet == requestType) {
            DebugRequestLineBreakpointToggle setBreakpointRequest 
                = (DebugRequestLineBreakpointToggle)request;
            setBreakpoint(setBreakpointRequest.getSource(), setBreakpointRequest.getLineNumber()); 
            return DebugResponseSimple.SUCCESS;
    	} else if (DebugRequestType.lineBreakpointClear == requestType) {
            DebugRequestLineBreakpointToggle clearBreakpointRequest 
                = (DebugRequestLineBreakpointToggle)request;
            clearBreakpoint(clearBreakpointRequest.getSource(), clearBreakpointRequest.getLineNumber()); 
            return DebugResponseSimple.SUCCESS;
    	} else if (DebugRequestType.callgraph == requestType) { 
            return new DebugResponseCallgraph(getCallgraph());
    	} else if (DebugRequestType.stack == requestType) { 
            DebugRequestStack stackRequest = (DebugRequestStack) request;
            int index = stackRequest.getIndex();
            return new DebugResponseStack(getStack(index));
    	} else if (DebugRequestType.step == requestType) { 
            step(); 
            return DebugResponseSimple.SUCCESS;
        }
    	
    	throw new java.lang.IllegalArgumentException( "unkown request type: "+ request.getType());
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
            stepping = false;
			this.notify();
		}
	}
    
    public void stop() {
		if (this.debugSupport == null) {
			throw new IllegalStateException("DebugStackState is not equiped with DebugSupport.");
		}
		
        DebugEvent event = new DebugEvent(DebugEventType.terminated);
        debugSupport.notifyDebugEvent(event);

        new Timer().schedule(new TimerTask() {
        	public void run () {
        		debugSupport.stop();
        		debugSupport = null;            		
        	}
        }, 300);

        exit();
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
	public void setBreakpoint(String source, int lineNumber) {
        DebugUtils.println("adding breakpoint " + constructBreakpointKey(source, lineNumber));
		synchronized ( this ) {
			breakpoints.put(constructBreakpointKey(source, lineNumber), Boolean.TRUE );
		}
	}
	
    protected String constructBreakpointKey(String source, int lineNumber) {
        return source + ":" + lineNumber;
    }

    /**
     * clear breakpoint at line lineNumber of source source
     */
	public void clearBreakpoint(String source, int lineNumber) {
		if(DebugUtils.IS_DEBUG)
			DebugUtils.println("removing breakpoint " + constructBreakpointKey(source, lineNumber));
		synchronized ( this ) {
			breakpoints.remove(constructBreakpointKey(source, lineNumber));
		}
	}
	
    /** 
     * return the current call graph (i.e. stack frames from
     * old to new, include information about file, method, etc.)
     */
	public StackFrame[] getCallgraph() {
		int n = cc;
        
		if ( n < 0 || n >= calls.length )
			return new StackFrame[0];
        
		StackFrame[] frames = new StackFrame[n+1];        
		for ( int i = 0; i <= n; i++ ) {
			CallInfo ci = calls[i];
            frames[i] = new StackFrame(ci, getLineNumber(ci));
		}
		return frames;
	}

	public Variable[] getStack(int index) {
        if (index < 0 || index >= calls.length) {
            //TODO: this is an error, handle it differently
            return new Variable[0];
        }
        
        CallInfo callInfo = calls[index];
        if(DebugUtils.IS_DEBUG)
        	DebugUtils.println("Stack Frame: " + index + "[" + callInfo.base + "," + callInfo.top + "]");
        int top = callInfo.top < callInfo.base ? callInfo.base : callInfo.top;
        Proto prototype = callInfo.closure.p;
        LocVars[] localVariables = prototype.locvars;
        Vector variables = new Vector();
        int localVariableCount = 0;
        Hashtable variablesSeen = new Hashtable();
        for (int i = 0; localVariables != null && i < localVariables.length && i <= top; i++) {
            String varName = localVariables[i].varname.toString();
            if(DebugUtils.IS_DEBUG) {
            	DebugUtils.print("\tVariable: " + varName); 
            	DebugUtils.print("\tValue: " + stack[callInfo.base + i]);
            }
            if (!variablesSeen.contains(varName) &&
                !LexState.isReservedKeyword(varName)) {
                variablesSeen.put(varName, varName);
                LValue value = stack[callInfo.base + i];                
                if (value != null) {
                    int type = value.luaGetType();
                    DebugUtils.print("\tType: " + Lua.TYPE_NAMES[type]);
                    if (type == Lua.LUA_TTABLE) {
                        DebugUtils.println(" (selected)");
                        variables.addElement(
                                new TableVariable(localVariableCount++, 
                                             varName, 
                                             type, 
                                             (LTable) value));                        
                    } else if (type != Lua.LUA_TFUNCTION &&
                               type != LUA_TTHREAD) {
                        DebugUtils.println(" (selected)");
                        variables.addElement(
                                new Variable(localVariableCount++, 
                                             varName, 
                                             type, 
                                             value.toString()));
                    } else {
                        DebugUtils.println("");
                    }
                } else {
                    DebugUtils.println("");
                }
            } else {
                DebugUtils.println("");
            }
        }
        
        Variable[] result = new Variable[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
        	result[i] = (Variable) variables.elementAt(i);
        }
        return result;
	}
	
	
    /**
     * single step forward (go to next statement)
     */
	public void step() {
		synchronized ( this ) {
            suspended = false;
			stepping = true;
			this.notify();
		}
	}
}
