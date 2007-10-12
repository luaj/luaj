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
package lua.debug.request;

import lua.debug.EnumType;

public class DebugRequestWatchpointToggle extends DebugRequest {    
    public static class AccessType extends EnumType {
		private static final long serialVersionUID = 3523086189648091587L;
		
		public static final AccessType Ignore = new AccessType("Ignore", 0);
        public static final AccessType Read = new AccessType("Read", 1);
        public static final AccessType Modify = new AccessType("Modify", 2);
        public static final AccessType ReadAndModify = new AccessType("ReadAndModify", 3);
        
        protected AccessType(String name, int ordinal) {
        	super(name, ordinal);
        }
    };
    
    protected String functionName;
    protected String variableName;
    
    public DebugRequestWatchpointToggle(String functionName, 
                                        String variableName, 
                                        AccessType accessType) {        
        super(accessType == AccessType.Ignore ? 
              DebugRequestType.watchpointClear : 
              DebugRequestType.watchpointSet);
        this.functionName = functionName;
        this.variableName = variableName;
    }

    public String getFunctionName() {
        return this.functionName;        
    }
    
    public String getVariableName() {
        return this.variableName;
    }
    
    /* (non-Javadoc)
     * @see lua.debug.DebugRequest#toString()
     */
    public String toString() {        
        return super.toString() + " functionName:" + getFunctionName() + " variableName:" + getVariableName();
    }
    
    // TODO: add the serialization stuff
}
