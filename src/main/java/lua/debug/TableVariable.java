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
import java.util.Vector;

import lua.Lua;
import lua.value.LTable;
import lua.value.LValue;

public class TableVariable extends Variable {   
    protected String[] keys;
    protected Object[] values;
    
    public TableVariable(int index, String name, int type, LTable table) {
        super(index, name, type, null);
        
        int size = table.size();
        DebugUtils.println("table size:" + size);
        Vector keyList = new Vector();  
        Vector valueList = new Vector();
        LValue[] keyValues = table.getKeys();        
        for (int i = 0; i < size; i++) {
        	
            LValue value = table.get(keyValues[i]);
            if (value == table) {
            	continue;
            }
            
            keyList.addElement(keyValues[i].toString());
            if (value instanceof LTable) {
            	DebugUtils.println("table: value[" + i + "]=" + value.toString());
            	valueList.addElement(new TableVariable(i, "element[" + keyValues[i].toString() + "]", Lua.LUA_TTABLE, (LTable)value));
            } else {
                valueList.addElement(value.toString());
            }
            DebugUtils.println("["+ keyValues[i].toString() + "," + value.toString() + "]");            	
        }
        
        this.keys = new String[keyList.size()];
        for (int i = 0; i < keyList.size(); i++) {
        	this.keys[i] = (String)keyList.elementAt(i);
        }

        this.values = new Object[valueList.size()];
        for (int i = 0; i < valueList.size(); i++) {
        	this.values[i] = valueList.elementAt(i);
        }

        if (this.keys.length != this.values.length) {
        	throw new RuntimeException("Internal Error: key.length must equal to values.length");
        }
    }
    
    public TableVariable(int index, String name, int type, String[] keys, Object[] values) {
    	super(index, name, type, null);
    	this.keys = keys;
    	this.values = values;
    }
    
    public String[] getKeys() {
        return this.keys == null ? new String[0] : this.keys;
    }
    
    public Object[] getValues() {
        return this.values == null ? new Object[0] : this.values;
    }
    
    public static void serialize(DataOutputStream out, TableVariable variable) throws IOException {
    	out.writeInt(variable.getIndex());
    	out.writeUTF(variable.getName());
    	out.writeInt(variable.getType());
    	
    	String[] keys = variable.getKeys();
    	out.writeInt(keys.length);
    	for (int i = 0; keys != null && i < keys.length; i++) {
    		SerializationHelper.serialize(new NullableString(keys[i]), out);
    	}
    	
    	Object[] values = variable.getValues();
    	for (int i = 0; values != null && i < values.length; i++) {
    		if (values[i] instanceof String) {
    			SerializationHelper.serialize(new NullableString((String)values[i]), out);
    		} else if (values[i] instanceof TableVariable) {
    			SerializationHelper.serialize((TableVariable)values[i], out);
    		} else {
    			throw new RuntimeException("Internal Error: values array should only contain String and TableVariable");
    		}
    	}
    }

    public static Variable deserialize(DataInputStream in) throws IOException {
    	int index = in.readInt();
    	String name = in.readUTF();
    	int type = in.readInt();
    	
    	String[] keys = null;
    	Object[] values = null;
    	int count = in.readInt();
    	keys = new String[count];
    	for (int i = 0; i < count; i++) {
    		keys[i] = ((NullableString) SerializationHelper.deserialize(in)).getRawString();
    	}
    	
    	values = new Object[count];
    	for (int i = 0; i < count; i++) {
    		int serialType = in.readInt();
    		if (serialType == SerializationHelper.SERIAL_TYPE_NullableString) {
    			values[i] = NullableString.deserialize(in).getRawString();
    		} else if (serialType == SerializationHelper.SERIAL_TYPE_TableVariable) {
    			values[i] = TableVariable.deserialize(in);
    		}
    	}
    	
    	TableVariable variable = new TableVariable(index, name, type, keys, values);
    	return variable;
    }   
}
