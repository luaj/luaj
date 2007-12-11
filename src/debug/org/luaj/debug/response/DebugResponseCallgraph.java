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
package org.luaj.debug.response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.luaj.debug.DebugMessage;
import org.luaj.debug.DebugMessageType;
import org.luaj.debug.StackFrame;

public class DebugResponseCallgraph extends DebugMessage {
    protected StackFrame[] stackFrames;

    public DebugResponseCallgraph(StackFrame[] callgraph) {
        super(DebugMessageType.clientRequestCallgraphReply);
        if (callgraph == null) {
            this.stackFrames = new StackFrame[0];
        } else {
            this.stackFrames = callgraph;
        }
    }

    public StackFrame[] getCallgraph() {
        return this.stackFrames;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("callgraph: ");
        for (int i = 0; i < stackFrames.length; i++) {
            StackFrame frame = stackFrames[i];
            buffer.append(frame.toString());
            buffer.append("\n");
        }
        return buffer.toString();
    }

    public static void serialize(DataOutputStream out,
            DebugResponseCallgraph response) throws IOException {
        StackFrame[] stackFrames = response.getCallgraph();
        out.writeInt(stackFrames == null ? 0 : stackFrames.length);
        for (int i = 0; stackFrames != null && i < stackFrames.length; i++) {
            StackFrame.serialize(out, stackFrames[i]);
        }
    }

    public static DebugMessage deserialize(DataInputStream in) throws IOException {
        int count = in.readInt();
        StackFrame[] stackFrames = new StackFrame[count];
        for (int i = 0; i < count; i++) {
            stackFrames[i] = StackFrame.deserialize(in);
        }

        return new DebugResponseCallgraph(stackFrames);
    }
}
