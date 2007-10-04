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
package lua.value;

import java.io.Serializable;

public class Type implements Serializable, Comparable {   
    private static final long serialVersionUID = 877640303374122782L;
    
    public static Type bool = new Type("boolean");
    public static Type function = new Type("function");
    public static Type nil = new Type("nil");
    public static Type number = new Type("number");
    public static Type string = new Type("string");
    public static Type table = new Type("table");
    public static Type thread = new Type("thread");
    public static Type userdata = new Type("userdata");
    protected static Type[] VALUES = new Type[] {
        bool,
        function,
        nil,
        number,
        string,
        table,
        thread,
        userdata
    };
    protected static int ORDINAL = 0;
    
    private String name;
    private LString lname;
    private int ordinal;
    Type(String name) {
        this.name = name;
        this.lname = new LString(name);
        this.ordinal = ORDINAL++;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    public String toString() {
        return name;
    }
    
    public static Type valueOf(String strValue) {
        Type[] values = Type.VALUES;
        for ( int i=0; i<values.length; i++ ) {
        	Type value = values[i];
            if (value.toString().equals(strValue)) {
                return value;
            }
        }
        
        throw new IllegalArgumentException("String '" + strValue + "' cannot be converted to enum Type");
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        return this.ordinal - ((Type)o).ordinal;
    }

	public LString toLString() {
		return lname;
	}
}
