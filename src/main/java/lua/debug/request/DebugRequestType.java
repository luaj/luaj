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
package lua.debug.request;

import java.io.DataInputStream;
import java.io.IOException;

import lua.debug.EnumType;


public class DebugRequestType extends EnumType {
	public static final DebugRequestType suspend = new DebugRequestType("suspend", 0);
    public static final DebugRequestType resume = new DebugRequestType("resume", 1);
    public static final DebugRequestType exit = new DebugRequestType("exit", 2); 
    public static final DebugRequestType lineBreakpointSet = new DebugRequestType("lineBreakpointSet", 3); 
    public static final DebugRequestType lineBreakpointClear = new DebugRequestType("lineBreakpointClear", 4);
    public static final DebugRequestType watchpointSet = new DebugRequestType("watchpointSet", 5);
    public static final DebugRequestType watchpointClear = new DebugRequestType("watchpointClear", 6);
    public static final DebugRequestType callgraph = new DebugRequestType("callgraph", 7);
    public static final DebugRequestType stack = new DebugRequestType("stack", 8);
    public static final DebugRequestType step = new DebugRequestType("step", 9);
    public static final DebugRequestType start = new DebugRequestType("start", 10);
    
    protected static final DebugRequestType[] ENUMS = new DebugRequestType[] {
    	suspend,
    	resume,
    	exit,
    	lineBreakpointSet,
    	lineBreakpointClear,
    	watchpointSet,
    	watchpointClear,
    	callgraph,
    	stack,
    	step,
    	start
    };
    
    public DebugRequestType(String name, int ordinal) {
    	super(name, ordinal);
    }
    
    public static DebugRequestType deserialize(DataInputStream in) throws IOException {
		int ordinal = in.readInt();
		if (ordinal < 0 || ordinal >= ENUMS.length) {
			throw new RuntimeException("ordinal is out of the range.");
		}
		return ENUMS[ordinal];
    }
}
