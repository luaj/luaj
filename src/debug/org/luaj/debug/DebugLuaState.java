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
package org.luaj.debug;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.luaj.compiler.LexState;
import org.luaj.debug.event.DebugEventBreakpoint;
import org.luaj.debug.event.DebugEventError;
import org.luaj.debug.net.DebugSupport;
import org.luaj.debug.request.DebugRequestDisconnect;
import org.luaj.debug.request.DebugRequestLineBreakpointToggle;
import org.luaj.debug.request.DebugRequestListener;
import org.luaj.debug.request.DebugRequestStack;
import org.luaj.debug.response.DebugResponseCallgraph;
import org.luaj.debug.response.DebugResponseStack;
import org.luaj.debug.response.DebugResponseVariables;
import org.luaj.vm.CallInfo;
import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.LocVars;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;


public class DebugLuaState extends LuaState implements DebugRequestListener {
    private static final boolean TRACE = (null != System.getProperty("TRACE"));

    // stepping constants and stepping state
    protected static final int STEP_NONE = 0;
    protected static final int STEP_OVER = 1;
    protected static final int STEP_INTO = 2;
    protected static final int STEP_RETURN = 3;
    protected int stepping = STEP_NONE;
    protected boolean shouldPauseForStepping = false;
    protected int steppingFrame = -1;
    
    protected Hashtable breakpoints = new Hashtable();
    protected boolean exiting = false;
    protected boolean suspended = false;
    protected boolean bSuspendOnStart = false;
    protected int lastline = -1;
    protected String lastSource;
    protected DebugSupport debugSupport;
    protected LuaErrorException lastError;

    public DebugLuaState() {}
    
    public void setDebugSupport(DebugSupport debugSupport) 
    throws IOException {
        if (debugSupport == null) {
            throw new IllegalArgumentException("DebugSupport cannot be null");
        }

        this.debugSupport = debugSupport;
        debugSupport.setDebugStackState(this);
        debugSupport.start();
    }
    
    public void setSuspendAtStart(boolean bSuspendAtStart) {
        this.bSuspendOnStart = bSuspendAtStart;
    }
    
    protected void debugAssert(boolean b) {
        if (!b)
            error("assert failure");
    }

     // use line numbers by default
    public void error(String message) {
        error(message, 1);
    }

    // intercept exceptions and fill in line numbers
    public void exec() {
        try {
            super.exec();
        } catch (AbortException e) {
            // ignored. Client aborts the debugging session.
        } catch (LuaErrorException e) {
            // give debug client a chance to see the error
            if (e != lastError) {
                lastError = e;
                debugErrorHook(e);                
            }
            throw e;
        }
    }

    private void debugErrorHook(Exception e) {
        if (debugSupport != null) {
            String msg = getFileLine(cc) + ": " + e.getMessage();
            String trace = getStackTrace();
            debugSupport.notifyDebugEvent(new DebugEventError(msg, trace));            
            synchronized (this) {
                suspend();
                while (suspended) {
                    try {
                        wait();                      
                    } catch (InterruptedException ex) {}
                }                    
            }
        }
    }

    // debug hooks
    public void debugHooks(int pc) {
        if (TRACE) {
            CallInfo ci = calls[cc];
            LClosure cl = ci.closure;
            LPrototype p = cl.p;
            Print.printState(this, base, top, base+p.maxstacksize, cl, pc);
        }
        
        if (exiting) {
            throw new AbortException("aborted by debug client");
        }

/*
        if (TRACE) {
            System.out.println("entered debugHook on pc=" + pc + "...Line: " + getFileLine(cc));
            for (int j = 0; j <= cc; j++) {
                System.out.println("calls[" + j + "]: base=" + calls[j].base + ", top=" + calls[j].top + " , pc=" + calls[j].pc);
                dumpStack(j);                    
            }
        }
*/
        synchronized (this) {
            while (bSuspendOnStart) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            CallInfo currentCallInfo = calls[cc];
            LPrototype currentProto = currentCallInfo.closure.p;

            // if we are not stepping, we would keep going if the line doesn't
            // change
            int line = getLineNumber(currentCallInfo);
            String source = getSourceFileName(currentProto.source);
            if (!isStepping() && lastline == line && source.equals(lastSource)) {
                return;
            }

            if (TRACE)
                System.out.println("debugHook - executing line: " + line);
            
            int i = currentProto.code[pc];
            int opCode = LuaState.GET_OPCODE(i);
            if (isStepping() && opCode == LuaState.OP_RETURN && cc == 0) {
                cancelStepping();
            } else if (shouldPauseForStepping) {
                shouldPauseForStepping = false;
                suspendOnStepping();
            } else if (stepping == STEP_INTO) {
                if (lastline != line) {
                    suspendOnStepping();
                } else if (opCode == LuaState.OP_CALL) {
                    shouldPauseForStepping = true;
                }
            } else if (stepping == STEP_OVER) {
                if ((steppingFrame == cc && lastline != line)|| 
                    (steppingFrame > cc)) {
                    suspendOnStepping();
                }
            } else if (stepping == STEP_RETURN) {
                if ((opCode == LuaState.OP_RETURN && cc == this.steppingFrame) || 
                    (opCode == LuaState.OP_TAILCALL && cc == this.steppingFrame)) {
                    shouldPauseForStepping = true;
                }
            }

            // check for a break point if we aren't suspended already
            if (!suspended && lastline != line) {
                if (TRACE)
                    System.out.println("Source: " + currentProto.source);
                String fileName = getSourceFileName(source);
                String breakpointKey = constructBreakpointKey(fileName, line);
                if (breakpoints.containsKey(breakpointKey)) {
                    if (TRACE)
                        System.out.println("hitting breakpoint "
                                + constructBreakpointKey(fileName, line));
                    if (debugSupport != null) {
                        debugSupport.notifyDebugEvent(
                                new DebugEventBreakpoint(fileName, line));
                    }
                    suspended = true;
                }
            }

            // save line in case next op is a step
            lastline = line;
            lastSource = source;

            // wait for a state change
            while (suspended && !exiting) {
                try {
                    this.wait();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    private boolean isStepping() {
        return stepping != STEP_NONE;
    }

    private void cancelStepping() {
        if (debugSupport != null) {
            debugSupport.notifyDebugEvent(
                    new DebugMessage(DebugMessageType.resumedOnSteppingEnd));
        }
        stepping = STEP_NONE;
        steppingFrame = -1;
        shouldPauseForStepping = false;
    }

    private void suspendOnStepping() {
        if (debugSupport != null) {
            debugSupport.notifyDebugEvent(
                    new DebugMessage(DebugMessageType.suspendedOnStepping));
        }
        suspended = true;
        steppingFrame = -1;
    }

    /**
     * Get the current line number
     * 
     * @param pc program counter
     * @return the line number corresponding to the pc
     */
    private int getLineNumber(CallInfo ci) {
        int[] lineNumbers = ci.closure.p.lineinfo;
        int pc = getCurrentPc(ci);
        int line = (lineNumbers != null && lineNumbers.length > pc ? 
                    lineNumbers[pc] :
                    -1);
        return line;
    }

    // ------------------ commands coming from the debugger -------------------

    public void handleRequest(DebugMessage request) {
        if (this.debugSupport == null) {
            throw new IllegalStateException(
                    "DebugSupport must be defined.");
        }

        if (TRACE)
            System.out.println("DebugStackState is handling request: "
                    + request.toString());

        DebugMessageType requestType = request.getType();
        if (DebugMessageType.start == requestType) {
            DebugMessage event = new DebugMessage(DebugMessageType.started);
            debugSupport.notifyDebugEvent(event);
            cancelSuspendOnStart();
        } else if (DebugMessageType.exit == requestType) {
            stop();
        } else if (DebugMessageType.disconnect == requestType) {
            DebugRequestDisconnect disconnectRequest 
                = (DebugRequestDisconnect) request;
            int connectionId = disconnectRequest.getSessionId();
            disconnect(connectionId);
        } else if (DebugMessageType.debugServiceDown == requestType) {
            disconnectFromDebugService();
        } else if (DebugMessageType.reset == requestType) {
            reset();
        } else if (DebugMessageType.suspend == requestType) {
            suspend();
            DebugMessage event = new DebugMessage(DebugMessageType.suspendedByClient);
            debugSupport.notifyDebugEvent(event);
        } else if (DebugMessageType.resume == requestType) {
            resume();
            DebugMessage event = new DebugMessage(DebugMessageType.resumedByClient);
            debugSupport.notifyDebugEvent(event);
        } else if (DebugMessageType.lineBreakpointSet == requestType) {
            DebugRequestLineBreakpointToggle setBreakpointRequest 
                = (DebugRequestLineBreakpointToggle) request;
            setBreakpoint(setBreakpointRequest.getSource(),
                    setBreakpointRequest.getLineNumber());
        } else if (DebugMessageType.lineBreakpointClear == requestType) {
            DebugRequestLineBreakpointToggle clearBreakpointRequest 
                = (DebugRequestLineBreakpointToggle) request;
            clearBreakpoint(clearBreakpointRequest.getSource(),
                    clearBreakpointRequest.getLineNumber());
        } else if (DebugMessageType.callgraph == requestType) {
            DebugResponseCallgraph callgraph 
                = new DebugResponseCallgraph(getCallgraph());
            debugSupport.notifyDebugEvent(callgraph);
        } else if (DebugMessageType.stack == requestType) {
            DebugRequestStack stackRequest = (DebugRequestStack) request;
            int index = stackRequest.getIndex();
            DebugResponseStack stackState 
                = new DebugResponseStack(index, getStack(index));
            debugSupport.notifyDebugEvent(stackState);
        } else if (DebugMessageType.global == requestType) {            
            DebugResponseVariables globals 
                = new DebugResponseVariables(getGlobals(), DebugMessageType.clientRequestGlobalReply);
            debugSupport.notifyDebugEvent(globals);
        } else if (DebugMessageType.stepInto == requestType) {
            DebugMessage event = new DebugMessage(
                    DebugMessageType.resumedOnSteppingInto);
            debugSupport.notifyDebugEvent(event);
            stepInto();
        } else if (DebugMessageType.stepOver == requestType) {
            DebugMessage event = new DebugMessage(
                    DebugMessageType.resumedOnSteppingOver);
            debugSupport.notifyDebugEvent(event);
            stepOver();
        } else if (DebugMessageType.stepReturn == requestType) {
            DebugMessage event = new DebugMessage(
                    DebugMessageType.resumedOnSteppingReturn);
            debugSupport.notifyDebugEvent(event);
            stepReturn();
        } else {
            throw new java.lang.IllegalArgumentException("unkown request type: "
                    + request.getType());
        }
    }

    /**
     * suspend the execution
     */
    public void suspend() {
        synchronized (this) {
            suspended = true;
            stepping = STEP_NONE;
            lastline = -1;
            this.notify();
        }
    }

    /**
     * If the VM is suspended on start, this method resumes the execution.
     */
    protected void cancelSuspendOnStart() {
        synchronized (this) {
            if (bSuspendOnStart) {
                bSuspendOnStart = false;
                this.notify();
            }
        }
    }

    /**
     * resume the execution
     */
    public void resume() {
        synchronized (this) {
            this.suspended = false;
            this.stepping = STEP_NONE;
            this.shouldPauseForStepping = false;
            this.steppingFrame = -1;
            this.notify();
        }
    }

    /**
     * Stops the debugging communication with the debug client and terminate the
     * VM execution.
     */
    public void stop() {
        synchronized (this) {
            if (exiting) return;
            
            if (this.debugSupport == null) {
                throw new IllegalStateException(
                        "DebugSupport must be defined.");
            }

            DebugMessage event = new DebugMessage(DebugMessageType.terminated);
            debugSupport.notifyDebugEvent(event);
            exit();
            debugSupport.stop();
            debugSupport = null;            
        }
    }

    public void disconnect(int connectionId) {
        if (this.debugSupport == null) {
            throw new IllegalStateException(
                    "DebugSupport must be defined.");
        }
        
        reset();
        DebugMessage event = new DebugMessage(DebugMessageType.disconnected);
        debugSupport.notifyDebugEvent(event);
        debugSupport.disconnect(connectionId);                   
    }
    
    public void disconnectFromDebugService() {
        if (this.debugSupport == null) {
            throw new IllegalStateException(
                    "DebugSupport must be defined.");
        }
        
        reset();
        debugSupport.disconnect();                           
    }
    
    public void reset() {
        synchronized (this) {
            this.breakpoints.clear();
            if (this.suspended) {
                resume();                
            }
        }
    }
    
    /**
     * terminate the execution
     */
    public void exit() {
        synchronized (this) {
            exiting = true;
            this.notify();
        }
    }

    /**
     * set breakpoint at line N
     * 
     * @param N -- the line to set the breakpoint at
     */
    public void setBreakpoint(String source, int lineNumber) {
        String fileName = getSourceFileName(source);
        String breakpointKey = constructBreakpointKey(fileName, lineNumber);

        breakpoints.put(breakpointKey, Boolean.TRUE);
    }

    protected String constructBreakpointKey(String source, int lineNumber) {
        return source + ":" + lineNumber;
    }

    /**
     * clear breakpoint at line lineNumber of source source
     */
    public void clearBreakpoint(String source, int lineNumber) {
        String fileName = getSourceFileName(source);
        String breakpointKey = constructBreakpointKey(fileName, lineNumber);

        breakpoints.remove(breakpointKey);
    }

    /**
     * return the current call graph (i.e. stack frames from old to new, include
     * information about file, method, etc.)
     */
    public StackFrame[] getCallgraph() {
        int n = cc;

        if (n < 0 || n >= calls.length)
            return new StackFrame[0];

        StackFrame[] frames = new StackFrame[n + 1];
        for (int i = 0; i <= n; i++) {
            CallInfo ci = calls[i];
            String src = getSourceFileName(ci.closure.p.source);
            frames[i] = new StackFrame(src, getLineNumber(ci));
        }
        return frames;
    }

    /**
     * Returns the visible local variables on a stack frame.
     * @param index The stack frame index
     * @return the visible local variables on the given stack frame.
     */
    public Variable[] getStack(int index) {
        if (index < 0 || index >= calls.length) {
            throw new RuntimeException("invalid stack index");
        }

        Vector variables = new Vector();
        Hashtable variablesSeen = new Hashtable();
        LPrototype p = calls[index].closure.p;
        for (int i = index; i >= 0; i--) {
            if (i == index || isInScope(p, calls[i])) {
                addVariables(variables, variablesSeen, i);
            }
        }
        Variable[] result = new Variable[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            result[i] = (Variable) variables.elementAt(i);
        }

        return result;
    }
    
    /**
     * Check if the current LPrototype is lexically defined in the caller scope.
     * @param p -- current LPrototype
     * @param ci -- caller info
     * @return true if the current LPrototype is lexically defined in the 
     * caller scope; false, otherwise.
     */
    protected boolean isInScope(LPrototype p, CallInfo ci) {
        LPrototype[] enclosingProtos = ci.closure.p.p;
        boolean bFound = false;
        for (int i = 0; enclosingProtos!= null && i < enclosingProtos.length; i++) {
            if (enclosingProtos[i] == p) {
                bFound = true;
                break;
            }
        }
        
        return bFound;
    }
    
    /**
     * Returns the visible globals to the current VM.
     * @return the visible globals.
     */
    public Variable[] getGlobals() {
        Vector variables = new Vector();
        variables.addElement(
                new TableVariable(0, 
                             "*Globals*", 
                             Lua.LUA_TTABLE, 
                             (LTable) _G));
        
        Variable[] result = new Variable[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            result[i] = (Variable) variables.elementAt(i);
        }

        return result;
    }
    
    /**
     * Debugging Utility. Dumps the variables for a given call frame.
     * @param index Index of the call frame
     */
    private void dumpStack(int index) {
        if (index < 0 || index > cc) return;
        
        CallInfo callInfo = calls[index];
        LPrototype prototype = callInfo.closure.p;
        LocVars[] localVariables = prototype.locvars;
        System.out.println("Stack Frame: " + index + " [" + base + "," + top + "], # of localvars: " + localVariables.length + ", pc=" + callInfo.pc);
        
        int pc = getCurrentPc(callInfo);
        for (int i = 0; i < localVariables.length; i++) {
            if (!isActiveVariable(pc, localVariables[i])) {
                continue;
            } else {
                System.out.println("localvars["+i+"]=" + localVariables[i].varname.toJavaString());
            }
        }
        
        int base = callInfo.base;
        int top = callInfo.top < callInfo.base ? callInfo.base+1 : callInfo.top;
        for (int i = base; i < top; i++){
            System.out.println("stack[" + i + "]=" + stack[i]);
        }
    }
    
    /**
     * Returns the name of the Nth variable in scope of the call info. 
     * @param callInfo Call info
     * @param index Index of the variable
     * @return the name of the Nth variable in scope of the call info. If the 
     * variable for the given index is not found, null is returned.
     */
    private String getVariable(CallInfo callInfo, int index) {
        int count = -1;
        LocVars[] localVariables = callInfo.closure.p.locvars;
        int pc = getCurrentPc(callInfo);
        for (int i = 0; i < localVariables.length; i++) {
            if (!isActiveVariable(pc, localVariables[i])) {
                continue;
            } else {
                count++;
                if (count == index) {
                    return localVariables[i].varname.toJavaString();
                }
            }
        }
        
        return null;
    }

    /**
     * Check if a variable is in scope.
     * @param pc -- Current program counter.
     * @param localVariable -- A local variable.
     * @return true if the variable is active under the given program counter;
     * false, otherwise.
     */
    private boolean isActiveVariable(int pc, LocVars localVariable) {
        return pc >= localVariable.startpc && pc <= localVariable.endpc;
    }
    
    /**
     * Adds the active variables for the given call frame to the list of variables.
     * @param variables -- the list of active variables.
     * @param variablesSeen -- variables already seen so far
     * @param index -- index of the call frame
     */
    private void addVariables(Vector variables, Hashtable variablesSeen, int index) {
        CallInfo callInfo = calls[index];
        int base = callInfo.base;
        int top = callInfo.top < callInfo.base ? callInfo.base+1 : callInfo.top;

        if (TRACE) {
            dumpStack(index);
        }
        
        int selectedVariableCount = 0;
        for (int i = base; i < top; i++) {
            String varName = getVariable(callInfo, i-base);
            if (varName == null) {
                // we don't care about the temporary variables and constants 
                // on the stack
                continue;
            }
            
            if(TRACE) {
                System.out.print("\tVariable: " + varName); 
                System.out.print("\tValue: " + stack[i]);
            }
            if (!variablesSeen.contains(varName) &&
                !LexState.isReservedKeyword(varName)) {
                variablesSeen.put(varName, varName);
                LValue value = stack[i];              
                if (value != null) {
                    int type = value.luaGetType();
                    if (TRACE)
                        System.out.print("\tType: " + Lua.TYPE_NAMES[type]);
                    if (type == Lua.LUA_TTABLE) {
                        if (TRACE)
                            System.out.print(" (selected)");
                        variables.addElement(
                                new TableVariable(selectedVariableCount++, 
                                             varName, 
                                             type, 
                                             (LTable) value));                        
                    } else if (type == LUA_TTHREAD) {
                        // coroutines
                    } else if (type != LUA_TFUNCTION) {
                        if (TRACE)
                            System.out.print(" (selected)");
                        variables.addElement(
                                new Variable(selectedVariableCount++, 
                                             varName, 
                                             type, 
                                             value.toString()));
                    }
                }
            }
            
            if (TRACE)
                System.out.print("");            
        }
    }

    /**
     * step over to next line
     */
    public void stepOver() {
        synchronized (this) {
            suspended = false;
            stepping = STEP_OVER;
            steppingFrame = cc;
            this.notify();
        }
    }

    /**
     * step to the next statement
     */
    public void stepInto() {
        synchronized (this) {
            suspended = false;
            stepping = STEP_INTO;
            this.notify();
        }
    }

    /**
     * return from the current method call
     */
    public void stepReturn() {
        synchronized (this) {
            suspended = false;
            stepping = STEP_RETURN;
            steppingFrame = cc;
            this.notify();
        }
    }
}
