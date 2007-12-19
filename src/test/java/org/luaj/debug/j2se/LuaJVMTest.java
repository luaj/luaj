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
package org.luaj.debug.j2se;

import java.net.URL;
import java.util.Properties;

import junit.framework.TestCase;

import org.luaj.TestPlatform;
import org.luaj.debug.j2se.StandardLuaJVM.ParseException;
import org.luaj.vm.Platform;

/**
 * Sanity test for StandardLuaJVM.
 */
public class LuaJVMTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
        
        System.setProperty(Platform.PROPERTY_LUAJ_DEBUG_PORT, "1999");         
        Platform.setInstance(new TestPlatform());
    }

    public void testCommandLineParse() {
        // null arguments
        String[] args = null;
        StandardLuaJVM vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            fail("Bad parsing program. Should never reach this line.");
        } catch (ParseException e) {}

        // empty arguments
        args = new String[] {};
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            fail("Bad parsing program. Should never reach this line.");
        } catch (ParseException e) {}

        // incomplete arguments
        args = new String[] { "-D" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            fail("Bad parsing program. Should never reach this line.");
        } catch (ParseException e) {}

        args = new String[] { "-D1046" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            fail("Bad parsing program. Should never reach this line.");
        } catch (ParseException e) {}

        args = new String[] { "-DsuspendOnStart=true" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            fail("Bad parsing program. Should never reach this line.");
        } catch (ParseException e) {}

        // invalid debug option format
        args = new String[] { "-Dport=1044:suspendOnStart=true", "dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            assertFalse(1044 == vm.getDebugPort());
            assertFalse(true == vm.getSuspendOnStart());
            assertEquals("dummy.lua", vm.getScript());
        } catch (ParseException e) {
            //expected
        }

        args = new String[] { "-Dport=1044,suspendOnStart=xyz", "dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            assertTrue(1044 == vm.getDebugPort());
            assertFalse(true == vm.getSuspendOnStart());
            assertEquals("dummy.lua", vm.getScript());
        } catch (ParseException e) {
            //expected
        }
        
        // missing script name
        args = new String[] { "-Dport=1047,suspendOnStart=true"};
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            fail("Bad parsing program. Should never reach this line.");
        } catch (ParseException e) {}

        // lua script cannot be found
        args = new String[] { "-Dport=1046,suspendOnStart", "dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            vm.run();
            fail("Should never reach this line.");
        } catch (ParseException e) {} 

        // lua script cannot be found
        args = new String[] { "dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            vm.run();
        } catch (ParseException e) {
            fail("Should never reach this line.");
        }

        // valid command line
        args = new String[] { "-Dport=1044,suspendOnStart=true", "dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            assertEquals(1044, vm.getDebugPort());
            assertEquals(true, vm.getSuspendOnStart());
            assertEquals("dummy.lua", vm.getScript());
        } catch (ParseException e) {
            fail("Should never reach this line.");
        }

        args = new String[] { "-Dport=1044,suspendOnStart=True", "dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            assertEquals(1044, vm.getDebugPort());
            assertEquals(true, vm.getSuspendOnStart());
            assertEquals("dummy.lua", vm.getScript());
        } catch (ParseException e) {
            fail("Should never reach this line.");
        }

        args = new String[] { "-DsuspendOnStart=true,port=1044", "dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            assertEquals(1044, vm.getDebugPort());
            assertEquals(true, vm.getSuspendOnStart());
            assertEquals("dummy.lua", vm.getScript());
        } catch (ParseException e) {
            fail("Should never reach this line.");
        }
        
        args = new String[] { "-DsuspendOnStart=TRUE,port=1044", "dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            assertEquals(1044, vm.getDebugPort());
            assertEquals(true, vm.getSuspendOnStart());
            assertEquals("dummy.lua", vm.getScript());
        } catch (ParseException e) {
            fail("Should never reach this line.");
        }
        
        args = new String[] { "-Dport=1044", "dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            assertEquals(1044, vm.getDebugPort());
            assertEquals(false, vm.getSuspendOnStart());
            assertEquals("dummy.lua", vm.getScript());
        } catch (ParseException e) {
            fail("Should never reach this line.");
        }
        
        System.setProperty("LUA_PATH", "c:/work/CSI/prototypes/uidemo/?.lua");
        args = new String[] {"dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            assertEquals("c:/work/CSI/prototypes/uidemo/?.lua", vm.getLuaPath());
            assertEquals("dummy.lua", vm.getScript());
        } catch (ParseException e) {
            fail("Should never reach this line.");
        }
        
        System.setProperty("LUA_PATH", "c:/work/CSI/prototypes/uidemo/?.lua");
        args = new String[] { "-Dport=1044", "dummy.lua" };
        vm = new StandardLuaJVM();
        try {
            vm.parse(args);
            assertEquals(1044, vm.getDebugPort());
            assertEquals("c:/work/CSI/prototypes/uidemo/?.lua", vm.getLuaPath());
            assertEquals("dummy.lua", vm.getScript());
        } catch (ParseException e) {
            fail("Should never reach this line.");
        }
    }

    public void testRun() {
        Properties props = System.getProperties();
        props.remove(Platform.PROPERTY_LUAJ_DEBUG);
        props.remove(Platform.PROPERTY_LUAJ_DEBUG_HOST);
        props.remove(Platform.PROPERTY_LUAJ_DEBUG_PORT);
        props.remove(Platform.PROPERTY_LUAJ_DEBUG_SUSPEND_AT_START);        
        System.setProperties(props);
        
        String[] tests = new String[] { "autoload", "boolean", "calls",
                "coercions", "compare", "math", "mathlib", "metatables",
                "select", "setlist", "swingapp", "test1", "test2", "test3",
                "test4", "test5", "test6", "test7", "type", "upvalues",
                "strlib"
        };
        doRun(tests);
    }

    public void testDebugRun() {
        Properties props = System.getProperties();
        props.setProperty(Platform.PROPERTY_LUAJ_DEBUG, "true");
        props.setProperty(Platform.PROPERTY_LUAJ_DEBUG_PORT, "1999");        
        System.setProperties(props);

        String[] tests = new String[] { "boolean", "calls",
                "coercions", "compare", "math", "mathlib", "metatables",
                "select", "setlist", "swingapp", "test1", "test2", "test3",
                "test4", "test5", "test6", "test7", "type", "upvalues"
        };
        doRun(tests);
    }
    
    private void doRun(String[] tests) {
        for (int i = 0; i < tests.length; i++) {
            String test = tests[i];
            System.out.println("==> running test: " + test + ".lua");
            doTestRun(test + ".lua");
            System.out.println("==> running test: " + test + ".luac");
            doTestRun(test + ".luac");
            System.out.println();
        }
    }
    
    protected void doTestRun(String testName) {
        String[] args = new String[1];
        URL filePath = getClass().getResource("/" + testName);
        if (filePath != null) {
            args[0] = filePath.getPath();
            try {
                StandardLuaJVM.main(args);
            } catch (Exception e) {
                e.printStackTrace();
                fail("Test " + testName + " failed due to " + e.getMessage());
            }
        }
    }
}
