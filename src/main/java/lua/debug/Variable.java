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

import lua.Lua;

public class Variable implements Serializable {    
    protected int    index;
    protected String name;
    protected String value;
    protected int    type;
    
    public Variable(int index, String name, int type, String value) {
    	if (name == null) {
    		throw new IllegalArgumentException("argument name is null");
    	}
    	
    	if (type < Lua.LUA_TNIL || type > Lua.LUA_TTHREAD) {
    		throw new IllegalArgumentException("invalid LValue type: " + type);
    	}
    	
        this.index = index;
        this.name = name;
        this.type = type;
        this.value = value;
    }
    
    public String getName() {
        return this.name;
    }

    public int getType() {
        return this.type;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public int getIndex() {
        return this.index;
    }

    public String toString() {
        return "index: " + getIndex() + " name:" + getName() + " type: " + Lua.TYPE_NAMES[getType()] + " value:" + getValue();
    }
    
    public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + type;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Variable other = (Variable) obj;
		if (index != other.index)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public static void serialize(DataOutputStream out, Variable variable) 
	throws IOException {
    	out.writeInt(variable.getIndex());
    	out.writeUTF(variable.getName());
    	out.writeInt(variable.getType());
    	SerializationHelper.serialize(new NullableString(variable.getValue()), out);
    }
    
    public static Variable deserialize(DataInputStream in) throws IOException {
    	int index = in.readInt();
    	String name = in.readUTF();
    	int type = in.readInt();
    	NullableString value = (NullableString)SerializationHelper.deserialize(in);
  
    	Variable variable = new Variable(index, name, type, value.getRawString());
    	return variable;
    }   
}
