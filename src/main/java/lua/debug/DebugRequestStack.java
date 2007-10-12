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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DebugRequestStack extends DebugRequest implements Serializable {
    protected int index;
    
    public DebugRequestStack(int index) {
        super(DebugRequestType.stack);
        this.index = index;
    }
    
    public int getIndex() {
        return this.index;
    }

    /* (non-Javadoc)
     * @see lua.debug.DebugRequest#toString()
     */
    public String toString() {
        return super.toString() + " stack frame:" + getIndex();
    }
    
	public static void serialize(DataOutputStream out, DebugRequestStack request) 
	throws IOException {
		out.writeInt(request.getIndex());
	}
	
	public static DebugRequest deserialize(DataInputStream in) throws IOException {
		int index = in.readInt();
		
		return new DebugRequestStack(index);
	}
}
