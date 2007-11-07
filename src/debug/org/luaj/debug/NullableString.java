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
import java.io.DataOutputStream;
import java.io.IOException;

public class NullableString implements Serializable {
	protected String string;
	
	public NullableString(String someString) {
		this.string = someString;
	}
    
	public String getNullableString() {
		return (this.string == null) ? "[NULL]" : this.string;
	}
	
	public String getRawString() {
		return (this.string.equals("[NULL]")) ? null : this.string;
	}
	
    public static void serialize(DataOutputStream out, NullableString string) 
    throws IOException {
    	out.writeUTF(string.getNullableString());
    }
    
    public static NullableString deserialize(DataInputStream in) 
    throws IOException {
    	String string = in.readUTF();
    	return new NullableString(string);
    }
}
