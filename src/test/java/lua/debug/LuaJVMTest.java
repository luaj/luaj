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

import java.net.URL;

import junit.framework.TestCase;

/**
 * Sanity test for StandardLuaJVM.
 */
public class LuaJVMTest extends TestCase {
    protected void doTestRun(String testName) {
        String[] args = new String[2];
        args[0] = "-file";        
        URL filePath = getClass().getResource("/"+ testName);
        if (filePath != null) {
            args[1] = filePath.getPath(); 
            try {
                StandardLuaJVM.main(args);
            } catch (Exception e) {
                e.printStackTrace();
                fail("Test " + testName + " failed due to " + e.getMessage());
            }            
        }        
    }
    
    public void testRun() {
        String[] tests = new String[] {
                "autoload",
                "boolean",
                "calls",
                "coercions",
                "compare",
                "math",
                "mathlib",
                "metatables",
                "select",
                "setlist",
                "swingapp",
                "test1",
                "test2",
                "test3",
                "test4",
                "test5",
                "test6",
                "test7",
                "type",
                "upvalues",
                //"strlib"
        };
        
        for (int i = 0; i < tests.length; i++) {
        	String test = tests[i];
            System.out.println("==> running test: " + test + ".lua");
            doTestRun(test + ".lua");            
            System.out.println("==> running test: " + test + ".luac");
            doTestRun(test + ".luac");
            System.out.println();
        }
    }
}
