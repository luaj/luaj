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
import java.io.DataOutputStream;
import java.io.IOException;

import lua.debug.SerializationHelper;

public class DebugRequestLineBreakpointToggle extends DebugRequest {
    protected String source;
    protected int lineNumber;
    
    public DebugRequestLineBreakpointToggle(DebugRequestType type, String source, int lineNumber) {
        super(type);
        if (lineNumber < 0) {
            throw new IllegalArgumentException("lineNumber must be equal to greater than zero");
        }
        this.source = source;
        this.lineNumber = lineNumber;
    }
    
    public int getLineNumber() {
        return this.lineNumber;
    }

    public String getSource() {
        return this.source;
    }
    
    /* (non-Javadoc)
     * @see lua.debug.DebugRequest#toString()
     */
    public String toString() {
        return super.toString() + " Source:" + getSource() + " lineNumber:" + getLineNumber();
    }
        
	public static void serialize(DataOutputStream out, DebugRequestLineBreakpointToggle request) 
	throws IOException {
		SerializationHelper.serialize(request.getType(), out);
		out.writeUTF(request.getSource());
		out.writeInt(request.getLineNumber());
	}
	
	public static DebugRequest deserialize(DataInputStream in) throws IOException {
		DebugRequestType type = (DebugRequestType)SerializationHelper.deserialize(in);
		String source = in.readUTF();
		int lineNo = in.readInt();
		
		return new DebugRequestLineBreakpointToggle(type, source, lineNo);
	}
}
