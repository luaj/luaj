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
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.luaj.compiler.LexState;
import org.luaj.debug.event.DebugEvent;
import org.luaj.debug.event.DebugEventBreakpoint;
import org.luaj.debug.event.DebugEventError;
import org.luaj.debug.event.DebugEventType;
import org.luaj.debug.request.DebugRequest;
import org.luaj.debug.request.DebugRequestLineBreakpointToggle;
import org.luaj.debug.request.DebugRequestListener;
import org.luaj.debug.request.DebugRequestStack;
import org.luaj.debug.request.DebugRequestType;
import org.luaj.debug.response.DebugResponse;
import org.luaj.debug.response.DebugResponseCallgraph;
import org.luaj.debug.response.DebugResponseSimple;
import org.luaj.debug.response.DebugResponseVariables;
import org.luaj.vm.CallInfo;
import org.luaj.vm.LClosure;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.LocVars;
import org.luaj.vm.Lua;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LuaState;


public class DebugStackState extends LuaState implements DebugRequestListener {
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
    protected boolean bSuspendAtStart = false;
    protected int lastline = -1;
    protected String lastSource;
    protected DebugSupport debugSupport;

    public DebugStackState() {
    }
    
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
        this.bSuspendAtStart = bSuspendAtStart;
    }
    
    protected void debugAssert(boolean b) {
        if (!b)
            error("assert failure");
    }

    private String getFileLine(int cindex) {
        String func = "?";
        String line = "?";
        String source = "?";
        if (cindex >= 0) {
            CallInfo call = this.calls[cindex];
            LPrototype p = call.closure.p;
            if (p != null && p.source != null)
                source = p.source.toJavaString();
            int pc = getCurrentPc(call);
            if (p.lineinfo != null && p.lineinfo.length > pc)
                line = String.valueOf(p.lineinfo[pc]);
            // TODO: reverse lookup on function name ????
            func = call.closure.toJavaString();
        }
        return source + ":" + line + "(" + func + ")";
    }
    
    // override and fill in line number info
    public void error(String message, int level) {
        super.error(level <= 0 ? 
                    message : 
                    getFileLine(cc + 1 - level) + ": " + message);
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
        } catch (Exception e) {
            // let VM exceptions be processed if the debugger is not attached
            // the same as the base class to minimize differences
            // between the debug and non-debug behavior
            VMException lastException = new VMException(e);
            if (debugSupport != null) {
                debugSupport.notifyDebugEvent(new DebugEventError(e.getMessage()));
                suspend();
                synchronized (this) {
                    while (suspended) {
                        try {
                            wait();
                        } catch (InterruptedException e1) {}
                    }                    
                }
            }
            throw lastException;
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

        if (DebugUtils.IS_DEBUG) {
            DebugUtils.println("entered debugHook on pc=" + pc + "...Line: " + getFileLine(cc));
            for (int j = 0; j <= cc; j++) {
                DebugUtils.println("calls[" + j + "]: base=" + calls[j].base + ", top=" + calls[j].top + " ,pc=" + calls[j].pc);
                dumpStack(j);                    
            }
        }        
        
        synchronized (this) {
            while (bSuspendAtStart) {
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
            String source = DebugUtils.getSourceFileName(currentProto.source);
            if (!isStepping() && lastline == line && source.equals(lastSource)) {
                return;
            }

            if (DebugUtils.IS_DEBUG)
                DebugUtils.println("debugHook - executing line: " + line);

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
                if (DebugUtils.IS_DEBUG)
                    DebugUtils.println("Source: " + currentProto.source);
                String fileName = DebugUtils.getSourceFileName(source);
                String breakpointKey = constructBreakpointKey(fileName, line);
                if (breakpoints.containsKey(breakpointKey)) {
                    if (DebugUtils.IS_DEBUG)
                        DebugUtils.println("hitting breakpoint "
                                + constructBreakpointKey(fileName, line));

                    debugSupport.notifyDebugEvent(new DebugEventBreakpoint(
                            fileName, line));
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
                    if (DebugUtils.IS_DEBUG)
                        DebugUtils.println("resuming execution...");
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
        debugSupport.notifyDebugEvent(new DebugEvent(
                DebugEventType.resumedOnSteppingEnd));
        stepping = STEP_NONE;
        steppingFrame = -1;
        shouldPauseForStepping = false;
    }

    private void suspendOnStepping() {
        debugSupport.notifyDebugEvent(new DebugEvent(
                DebugEventType.suspendedOnStepping));
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

    private int getCurrentPc(CallInfo ci) {
        int pc = (ci != calls[cc] ? ci.pc - 1 : ci.pc);
        return pc;
    }

    // ------------------ commands coming from the debugger -------------------

    public DebugResponse handleRequest(DebugRequest request) {
        if (this.debugSupport == null) {
            throw new IllegalStateException(
                    "DebugStackState is not equiped with DebugSupport.");
        }

        if (DebugUtils.IS_DEBUG)
            DebugUtils.println("DebugStackState is handling request: "
                    + request.toString());

        DebugRequestType requestType = request.getType();
        if (DebugRequestType.start == requestType) {
            DebugEvent event = new DebugEvent(DebugEventType.started);
            debugSupport.notifyDebugEvent(event);
            setStarted();
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
            DebugRequestLineBreakpointToggle setBreakpointRequest = (DebugRequestLineBreakpointToggle) request;
            setBreakpoint(setBreakpointRequest.getSource(),
                    setBreakpointRequest.getLineNumber());
            return DebugResponseSimple.SUCCESS;
        } else if (DebugRequestType.lineBreakpointClear == requestType) {
            DebugRequestLineBreakpointToggle clearBreakpointRequest = (DebugRequestLineBreakpointToggle) request;
            clearBreakpoint(clearBreakpointRequest.getSource(),
                    clearBreakpointRequest.getLineNumber());
            return DebugResponseSimple.SUCCESS;
        } else if (DebugRequestType.callgraph == requestType) {
            return new DebugResponseCallgraph(getCallgraph());
        } else if (DebugRequestType.stack == requestType) {
            DebugRequestStack stackRequest = (DebugRequestStack) request;
            int index = stackRequest.getIndex();
            return new DebugResponseVariables(getStack(index));
        } else if (DebugRequestType.global == requestType) {            
            return new DebugResponseVariables(getGlobals());
        } else if (DebugRequestType.stepInto == requestType) {
            DebugEvent event = new DebugEvent(
                    DebugEventType.resumedOnSteppingInto);
            debugSupport.notifyDebugEvent(event);
            stepInto();
            return DebugResponseSimple.SUCCESS;
        } else if (DebugRequestType.stepOver == requestType) {
            DebugEvent event = new DebugEvent(
                    DebugEventType.resumedOnSteppingOver);
            debugSupport.notifyDebugEvent(event);
            stepOver();
            return DebugResponseSimple.SUCCESS;
        } else if (DebugRequestType.stepReturn == requestType) {
            DebugEvent event = new DebugEvent(
                    DebugEventType.resumedOnSteppingReturn);
            debugSupport.notifyDebugEvent(event);
            stepReturn();
            return DebugResponseSimple.SUCCESS;
        }

        throw new java.lang.IllegalArgumentException("unkown request type: "
                + request.getType());
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

    protected void setStarted() {
        synchronized (this) {
            bSuspendAtStart = false;
            this.notify();
        }
    }

    /**
     * resume the execution
     */
    public void resume() {
        synchronized (this) {
            suspended = false;
            stepping = STEP_NONE;
            this.notify();
        }
    }

    public void stop() {
        if (this.debugSupport == null) {
            throw new IllegalStateException(
                    "DebugStackState is not equiped with DebugSupport.");
        }

        DebugEvent event = new DebugEvent(DebugEventType.terminated);
        debugSupport.notifyDebugEvent(event);

        new Timer().schedule(new TimerTask() {
            public void run() {
                debugSupport.stop();
                debugSupport = null;
            }
        }, 500);

        exit();
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
     * @param N
     *                the line to set the breakpoint at
     */
    public void setBreakpoint(String source, int lineNumber) {
        String fileName = DebugUtils.getSourceFileName(source);
        String breakpointKey = constructBreakpointKey(fileName, lineNumber);

        if (DebugUtils.IS_DEBUG)
            DebugUtils.println("adding breakpoint " + breakpointKey);

        breakpoints.put(breakpointKey, Boolean.TRUE);
    }

    protected String constructBreakpointKey(String source, int lineNumber) {
        return source + ":" + lineNumber;
    }

    /**
     * clear breakpoint at line lineNumber of source source
     */
    public void clearBreakpoint(String source, int lineNumber) {
        String fileName = DebugUtils.getSourceFileName(source);
        String breakpointKey = constructBreakpointKey(fileName, lineNumber);

        if (DebugUtils.IS_DEBUG)
            DebugUtils.println("removing breakpoint " + breakpointKey);

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
            frames[i] = new StackFrame(ci, getLineNumber(ci));
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
    
    private void dumpStack(int index) {
        CallInfo callInfo = calls[index];
        LocVars[] localVariables = callInfo.closure.p.locvars;
        int pc = getCurrentPc(callInfo);
        for (int i = 0; i < localVariables.length; i++) {
            if (!isActiveVariable(pc, localVariables[i])) {
                continue;
            } else {
                DebugUtils.println("localvars["+i+"]=" + localVariables[i].varname.toJavaString());
            }
        }
    }
    
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

    private boolean isActiveVariable(int pc, LocVars localVariable) {
        return pc >= localVariable.startpc && pc <= localVariable.endpc;
    }
    
    private void addVariables(Vector variables, Hashtable variablesSeen, int index) {
        CallInfo callInfo = calls[index];
        LPrototype prototype = callInfo.closure.p;
        int base = callInfo.base;
        int top = callInfo.top < callInfo.base ? callInfo.base+1 : callInfo.top;

        if (DebugUtils.IS_DEBUG) {
            DebugUtils.println("Stack Frame: " + index + " [" + base + "," + top + "], # of localvars: " + prototype.locvars.length + ", pc=" + callInfo.pc);
            for (int i = 0; i < prototype.locvars.length; i++) {
                DebugUtils.println("localvars[" + i + "]: " + prototype.locvars[i].varname + "(" + prototype.locvars[i].startpc + "," + prototype.locvars[i].endpc + ")");
            }
            for (int i = base; i < top; i++){
                DebugUtils.println("stack[" + i + "]=" + stack[i]);
            }
        }
        
        int selectedVariableCount = 0;
        for (int i = base; i < top; i++) {
            String varName = getVariable(callInfo, i-base);
            if (varName == null) {
                // we don't care about the temporary variables and constants 
                // on the stack
                continue;
            }
            
            if(DebugUtils.IS_DEBUG) {
                DebugUtils.print("\tVariable: " + varName); 
                DebugUtils.print("\tValue: " + stack[i]);
            }
            if (!variablesSeen.contains(varName) &&
                !LexState.isReservedKeyword(varName)) {
                variablesSeen.put(varName, varName);
                LValue value = stack[i];              
                if (value != null) {
                    int type = value.luaGetType();
                    if (DebugUtils.IS_DEBUG)
                        DebugUtils.print("\tType: " + Lua.TYPE_NAMES[type]);
                    if (type == Lua.LUA_TTABLE) {
                        if (DebugUtils.IS_DEBUG)
                            DebugUtils.print(" (selected)");
                        variables.addElement(
                                new TableVariable(selectedVariableCount++, 
                                             varName, 
                                             type, 
                                             (LTable) value));                        
                    } else if (type == LUA_TTHREAD) {
                        // coroutines
                    } else if (type != LUA_TFUNCTION) {
                        if (DebugUtils.IS_DEBUG)
                            DebugUtils.print(" (selected)");
                        variables.addElement(
                                new Variable(selectedVariableCount++, 
                                             varName, 
                                             type, 
                                             value.toString()));
                    }
                }
            }
            
            if (DebugUtils.IS_DEBUG)
                DebugUtils.println("");            
        }
    }

    /**
     * step over to next line
     */
    public void stepOver() {
        synchronized (this) {
            if (DebugUtils.IS_DEBUG)
                DebugUtils.println("stepOver on cc=" + cc + "...");

            suspended = false;
            stepping = STEP_OVER;
            steppingFrame = cc;
            this.notify();
        }
    }

    /**
     * step a single statement
     */
    public void stepInto() {
        synchronized (this) {
            suspended = false;
            stepping = STEP_INTO;
            this.notify();
        }
    }

    /**
     * return from the method call
     */
    public void stepReturn() {
        synchronized (this) {
            if (DebugUtils.IS_DEBUG)
                DebugUtils.println("stepReturn on cc=" + cc + "...");

            suspended = false;
            stepping = STEP_RETURN;
            steppingFrame = cc;
            this.notify();
        }
    }
}
