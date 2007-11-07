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
import java.io.DataOutputStream;
import java.io.IOException;

public class DebugEventBreakpoint extends DebugEvent {
    protected String source;
    protected int lineNumber;

    public DebugEventBreakpoint(String source, int lineNumber) {
        super(DebugEventType.suspendedOnBreakpoint);
        if (source == null) {
        	throw new IllegalArgumentException("argument source cannot be null");
        }
        
        if (lineNumber <= 0) {
        	throw new IllegalArgumentException("argument lineNumber must be positive integer");
        }
        
        this.source = source;
        this.lineNumber = lineNumber;
    }
    
    public String getSource() {
        return this.source;        
    }
    
    public int getLineNumber() {
        return this.lineNumber;
    }

    /* (non-Javadoc)
     * @see lua.debug.DebugEvent#toString()
     */
    public String toString() {
        return super.toString() + " source:" + getSource() + " line:" + getLineNumber();
    }
    
    public static void serialize(DataOutputStream out, DebugEventBreakpoint object) 
    throws IOException {
    	out.writeUTF(object.getSource());
    	out.writeInt(object.getLineNumber());
	}

	public static DebugEvent deserialize(DataInputStream in) throws IOException {
		String source = in.readUTF();
		int lineNo = in.readInt();
		
		return new DebugEventBreakpoint(source, lineNo);
	}
}
