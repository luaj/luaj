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

public class DebugResponseCallgraph extends DebugResponseSimple {
    private static final long serialVersionUID = -7761865402188853413L;
    protected StackFrame[] stackFrames;
    
    public DebugResponseCallgraph(StackFrame[] callgraph) {
        super(true);
        this.stackFrames = callgraph;
    }
    
    public StackFrame[] getCallgraph() {
        return this.stackFrames;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; stackFrames != null && i < stackFrames.length; i++) {
           StackFrame frame = stackFrames[i];
           buffer.append(frame.toString());
           buffer.append("\n");
        }
        return buffer.toString();
    }
}
