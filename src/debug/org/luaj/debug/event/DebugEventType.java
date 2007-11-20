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
package org.luaj.debug.event;

import java.io.DataInputStream;
import java.io.IOException;

import org.luaj.debug.EnumType;



public class DebugEventType extends EnumType {
    public static DebugEventType started = new DebugEventType("started", 0);
    public static DebugEventType suspendedByClient = new DebugEventType("suspendedByClient", 1);
    public static DebugEventType suspendedOnBreakpoint = new DebugEventType("suspendedOnBreakpoint", 2);
    public static DebugEventType suspendedOnWatchpoint = new DebugEventType("suspendedOnWatchpoint", 3);
    public static DebugEventType suspendedOnStepping = new DebugEventType("suspendedOnStepping", 4);
    public static DebugEventType suspendedOnError = new DebugEventType("suspendedOnError", 5);
    public static DebugEventType resumedByClient = new DebugEventType("resumedByClient", 6);
    public static DebugEventType resumedOnSteppingInto = new DebugEventType("resumedOnSteppingInto", 7);
    public static DebugEventType resumedOnSteppingOver = new DebugEventType("resumedOnSteppingOver", 8);
    public static DebugEventType resumedOnSteppingReturn = new DebugEventType("resumedOnSteppingReturn", 9);
    public static DebugEventType resumedOnSteppingEnd = new DebugEventType("resumedOnSteppingEnd", 10);    
    public static DebugEventType resumedOnError = new DebugEventType("resumedOnError", 11);
    public static DebugEventType error = new DebugEventType("error", 12);
    public static DebugEventType terminated = new DebugEventType("terminated", 13);
    public static DebugEventType clientRequestCallgraphReply 
        = new DebugEventType("clientRequestCallgraphReply", 14);
    public static DebugEventType clientRequestStackReply 
        = new DebugEventType("clientRequestStackReply", 15);
    public static DebugEventType clientRequestGlobalReply 
        = new DebugEventType("clientRequestGlobalReply", 16);
    public static DebugEventType disconnected 
        = new DebugEventType("disconnected", 17);
    public static DebugEventType session 
        = new DebugEventType("session", 17);
    
    protected static DebugEventType[] ENUMS = new DebugEventType[] {
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
    	session
    };
	
    protected DebugEventType(String name, int ordinal) {
    	super(name, ordinal);
    }

    public static DebugEventType deserialize(DataInputStream in) 
    throws IOException {
		int ordinal = in.readInt();
		if (ordinal < 0 || ordinal >= ENUMS.length) {
			throw new RuntimeException("DebugEventType: ordinal is out of the range.");
		}
		return ENUMS[ordinal];
    }
}
