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

import lua.value.LTable;
import lua.value.LValue;
import lua.value.Type;

public class TableVariable extends Variable {
    
    private static final long serialVersionUID = 1194778378382802700L;
    protected String[] keys;
    protected Object[] values;
    
    public TableVariable(int index, String name, Type type, LTable table) {
        super(index, name, type, null);
        
        int size = table.size();
        DebugUtils.println("table size:" + size);
        this.keys = new String[size];  
        this.values = new Object[size];
        LValue[] keyValues = table.getKeys();        
        for (int i = 0; i < size; i++) {
            this.keys[i] = keyValues[i].toString();            
            LValue value = table.get(keyValues[i]);
            if (value instanceof LTable) {
                this.values[i] = new TableVariable(i, "<table>", Type.table, (LTable)value);
            } else {
                this.values[i] = value.toString();
            }
            DebugUtils.println("["+ keys[i] + "," + values[i].toString() + "]");
        }
    }
    
    public String[] getKeys() {
        return this.keys;
    }
    
    public Object[] getValues() {
        return this.values;
    }
}
