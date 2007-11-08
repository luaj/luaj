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

import org.luaj.debug.SerializationHelper;
import org.luaj.debug.Variable;

public class DebugResponseVariables implements DebugResponse {
    protected Variable[] variables;
    
    public DebugResponseVariables(Variable[] variables) {
        if (variables == null) {
            this.variables = new Variable[0];
        } else {
            this.variables = variables;                 
        }
    }
    
    public Variable[] getVariables() {
        return this.variables;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; variables != null && i < variables.length; i++) {
            buffer.append("\t" + variables[i].getName() + ":" + variables[i].getIndex() + "\n");
        }
        return buffer.toString();
    }
    
    public static void serialize(DataOutputStream out,
                                 DebugResponseVariables response) 
    throws IOException {
        Variable[] variables = response.getVariables();
        out.writeInt(variables == null ? 0 : variables.length);
        for (int i = 0; i < variables.length; i++) {
            SerializationHelper.serialize(variables[i], out);
        }
    }
        
    public static DebugResponseVariables deserialize(DataInputStream in)
    throws IOException {
        int count = in.readInt();
        Variable[] variables = new Variable[count];
        for (int i = 0; i < count; i++) {
            variables[i] = (Variable) SerializationHelper.deserialize(in);
        }
        return new DebugResponseVariables(variables);
    }
}
