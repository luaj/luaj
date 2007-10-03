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

import java.io.File;

import lua.io.LoadState;
import lua.value.LString;

public class DebugUtils {
    public static final boolean IS_DEBUG = false;
    
    public static void println(String message) {
        if (IS_DEBUG) {
            System.out.println(message);
        }
    }
    
    public static void print(String message) {
        if (IS_DEBUG) {
            System.out.print(message);
        }
    }

    public static String getSourceFileName(LString source) {
        String sourceStr = source.toJavaString();
        sourceStr = LoadState.getSourceName(sourceStr);
        if (!LoadState.SOURCE_BINARY_STRING.equals(sourceStr)) {
            File sourceFile = new File(sourceStr);            
            return sourceFile.getName();
        } else {
            return sourceStr;
        }
    }
}
