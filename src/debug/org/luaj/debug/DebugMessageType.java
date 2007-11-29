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

import java.io.DataInputStream;
import java.io.IOException;


public class DebugMessageType extends EnumType {
    // requests
    public static final DebugMessageType start = new DebugMessageType("start", 0);
    public static final DebugMessageType resume = new DebugMessageType("resume", 1);
    public static final DebugMessageType suspend = new DebugMessageType("suspend", 2);
    public static final DebugMessageType exit = new DebugMessageType("exit", 3); 
    public static final DebugMessageType lineBreakpointSet = new DebugMessageType("lineBreakpointSet", 4); 
    public static final DebugMessageType lineBreakpointClear = new DebugMessageType("lineBreakpointClear", 5);
    public static final DebugMessageType watchpointSet = new DebugMessageType("watchpointSet", 6);
    public static final DebugMessageType watchpointClear = new DebugMessageType("watchpointClear", 7);
    public static final DebugMessageType callgraph = new DebugMessageType("callgraph", 8);
    public static final DebugMessageType stack = new DebugMessageType("stack", 9);
    public static final DebugMessageType stepInto = new DebugMessageType("stepInto", 10);
    public static final DebugMessageType stepOver = new DebugMessageType("stepOver", 11);
    public static final DebugMessageType stepReturn = new DebugMessageType("stepReturn", 12);
    public static final DebugMessageType global = new DebugMessageType("global", 13);
    public static final DebugMessageType disconnect = new DebugMessageType("disconnect", 14);
    public static final DebugMessageType reset = new DebugMessageType("reset", 15);
    public static final DebugMessageType session = new DebugMessageType("session", 16);
    
    // events
    public static final DebugMessageType started = new DebugMessageType("started", 17);
    public static final DebugMessageType suspendedByClient = new DebugMessageType("suspendedByClient", 18);
    public static final DebugMessageType suspendedOnBreakpoint = new DebugMessageType("suspendedOnBreakpoint", 19);
    public static final DebugMessageType suspendedOnWatchpoint = new DebugMessageType("suspendedOnWatchpoint", 20);
    public static final DebugMessageType suspendedOnStepping = new DebugMessageType("suspendedOnStepping", 21);
    public static final DebugMessageType suspendedOnError = new DebugMessageType("suspendedOnError", 22);
    public static final DebugMessageType resumedByClient = new DebugMessageType("resumedByClient", 23);
    public static final DebugMessageType resumedOnSteppingInto = new DebugMessageType("resumedOnSteppingInto", 24);
    public static final DebugMessageType resumedOnSteppingOver = new DebugMessageType("resumedOnSteppingOver", 25);
    public static final DebugMessageType resumedOnSteppingReturn = new DebugMessageType("resumedOnSteppingReturn", 26);
    public static final DebugMessageType resumedOnSteppingEnd = new DebugMessageType("resumedOnSteppingEnd", 27);    
    public static final DebugMessageType resumedOnError = new DebugMessageType("resumedOnError", 28);
    public static final DebugMessageType error = new DebugMessageType("error", 29);
    public static final DebugMessageType terminated = new DebugMessageType("terminated", 30);
    public static final DebugMessageType clientRequestCallgraphReply = new DebugMessageType("clientRequestCallgraphReply", 31);
    public static final DebugMessageType clientRequestStackReply = new DebugMessageType("clientRequestStackReply", 32);
    public static final DebugMessageType clientRequestGlobalReply = new DebugMessageType("clientRequestGlobalReply", 33);
    public static final DebugMessageType disconnected = new DebugMessageType("disconnected", 34);
    public static final DebugMessageType sessionId = new DebugMessageType("sessionId", 35);    
    public static final DebugMessageType debugServiceDown = new DebugMessageType("debugServiceDown", 36);
    
    protected static DebugMessageType[] ENUMS = new DebugMessageType[] {
        start,
        resume,
        suspend,
        exit,
        lineBreakpointSet,
        lineBreakpointClear,
        watchpointSet,
        watchpointClear,
        callgraph,
        stack,
        stepInto,
        stepOver,
        stepReturn,
        global,
        disconnect,
        reset,
        session,
    	started,
    	suspendedByClient,
    	suspendedOnBreakpoint,
    	suspendedOnWatchpoint,
    	suspendedOnStepping,
    	suspendedOnError,
    	resumedByClient,
    	resumedOnSteppingInto,
    	resumedOnSteppingOver,
    	resumedOnSteppingReturn,
    	resumedOnSteppingEnd,
    	resumedOnError,
    	error,
    	terminated,
    	clientRequestCallgraphReply,
    	clientRequestStackReply,
    	clientRequestGlobalReply,
    	disconnected,
    	sessionId,
        debugServiceDown
    };
	
    protected DebugMessageType(String name, int ordinal) {
    	super(name, ordinal);
    }

    public static DebugMessageType deserialize(DataInputStream in) 
    throws IOException {
        int ordinal = in.readInt();
        if (ordinal < 0 || ordinal >= ENUMS.length) {
            throw new RuntimeException(
                    "DebugMessageType: ordinal is out of the range.");
        }
        return ENUMS[ordinal];
    }
}
