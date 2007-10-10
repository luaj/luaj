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

import java.io.Serializable;

import lua.Lua;

public class Variable implements Serializable {
    private static final long serialVersionUID = 8194091816623233934L;
    
    protected int    index;
    protected String name;
    protected String value;
    protected int type;
    
    public Variable(int index, String name, int type, String value) {
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
}
