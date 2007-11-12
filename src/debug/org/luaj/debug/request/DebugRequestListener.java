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
package org.luaj.debug.request;


public interface DebugRequestListener {
    
    /**
     * Debugging client can send the following requests to the server:
     * suspend   -- suspend the execution and listen for debug requests
     * resume    -- resume the execution
     * exit      -- terminate the execution
     * set N     -- set breakpoint at line N
     * clear N   -- clear breakpoint at line N
     * callgraph -- return the current call graph (i.e. stack frames from 
     *              old to new, include information about file, method, etc.)
     * stack     -- return the content of the current stack frame, 
     *              listing the (variable, value) pairs
     * step      -- single step forward (go to next statement)                         
     */ 
    public void handleRequest(DebugRequest request);
}
