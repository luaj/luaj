/*******************************************************************************
* Copyright (c) 2008 LuaJ. All rights reserved.
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
package org.luaj.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptEngineSample {
    
    public static void main(String [] args) {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine e = sem.getEngineByExtension(".lua");
        ScriptEngineFactory f = e.getFactory();
        String engineName = f.getEngineName();
        String engineVersion = f.getEngineVersion();
        String langName = f.getLanguageName();
        String langVersion = f.getLanguageVersion();
        System.out.println(engineName + " " +
                engineVersion + " " +
                langName + " " +
                langVersion);
        String statement = f.getOutputStatement("\"hello, world\"");
        System.out.println(statement);
        try {
            e.eval(statement);
            
            e.put("x", 25);
            e.eval("y = math.sqrt(x)");
            System.out.println( "y="+e.get("y") );

            e.put("x", 2);
            e.eval("y = math.sqrt(x)");
            System.out.println( "y="+e.get("y") );
            
            e.eval("\n\nbogus example\n\n");
        } catch (ScriptException ex) {
            ex.printStackTrace();
        }
    }

}
