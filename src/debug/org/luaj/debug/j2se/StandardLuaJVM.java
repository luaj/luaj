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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.luaj.compiler.LuaC;
import org.luaj.debug.DebugLuaState;
import org.luaj.debug.net.DebugSupport;
import org.luaj.debug.net.j2se.DebugSupportImpl;
import org.luaj.lib.j2se.LuajavaLib;
import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LString;
import org.luaj.vm.LValue;
import org.luaj.vm.LoadState;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;

/**
 * StandardLuaJVM executes a lua program in normal run mode or debug mode. 
 * 
 * @author:  Shu Lei
 * @version: 1.0
 */
public class StandardLuaJVM {
    private static final String CMD_LINE_DEBUG_OPTION_SUSPEND_ON_START = "suspendOnStart=";
    private static final String CMD_LINE_DEBUG_OPTION_PORT = "port=";
    protected boolean isDebugMode = false;
    protected int debugPort = -1;;
    protected boolean bSuspendOnStart = false;
    protected String script;
    protected String[] scriptArgs;
    protected LuaState state;
    protected boolean isReady = false;
    protected boolean isTerminated = false;

    // command line parsing utilities
    class ParseException extends Exception {
        private static final long serialVersionUID = -3134938136698856577L;

        public ParseException(String message) {
            super(message);
        }
    }

    protected void printUsage() {
        System.out.println("Usage:");
        System.out.println("\t java StandardLuaJVM [-Dport=<port>[,suspendedOnStart=<true|false>]] <script> [<script arguments>]");
        System.out.println("where [] denotes an optional argument and <> denotes a placeholder.");
    }

    void parse(String[] args) throws ParseException {
        if (args == null || args.length < 1) {
            throw new ParseException("Invalid command line arguments.");
        }

        if (args[0] != null && args[0].startsWith("-D")) {
            if (args.length < 2) {
                throw new ParseException("Invalid command line arguments.");
            }

            this.isDebugMode = true;
            String debugOptions = args[0];
            debugOptions = debugOptions.substring(2); // remove '-D'
            String[] options = debugOptions.split(",");
            for (int i = 0; options != null && i < options.length; i++) {
                if (options[i].startsWith(CMD_LINE_DEBUG_OPTION_PORT)) {
                    String portString = options[i].substring(CMD_LINE_DEBUG_OPTION_PORT.length());
                    try {
                        this.debugPort = Integer.parseInt(portString);
                        if (this.debugPort <= 0) {
                            throw new ParseException(
                                    "Invalid debug port: it must be greater than zero.");
                        }
                    } catch (NumberFormatException e) {
                        throw new ParseException("Invalid debug port: " + e.getMessage());
                    }                    
                } else if (options[i].startsWith(CMD_LINE_DEBUG_OPTION_SUSPEND_ON_START)) {
                    String suspendOnStartStr 
                        = options[i].substring(CMD_LINE_DEBUG_OPTION_SUSPEND_ON_START.length());
                    if (!suspendOnStartStr.equalsIgnoreCase("true") && 
                        !suspendOnStartStr.equalsIgnoreCase("false")) {
                        throw new ParseException("invalid debug flag: suspendOnStart");
                    }
                    this.bSuspendOnStart = Boolean.valueOf(suspendOnStartStr).booleanValue();                    
                } else {
                    throw new ParseException("Invalid command line argument: " + debugOptions);
                }
            }
           
            if (this.debugPort == -1) {
                throw new ParseException("Invalid command line: debug port is missing");
            }
            
            int tempArgsCount = args.length - 1;
            String[] tempArgs = new String[tempArgsCount];
            System.arraycopy(args, 1, tempArgs, 0, tempArgsCount);
            parseScriptArgs(tempArgs);
        } else {
            parseScriptArgs(args);
        }
    }

    private void parseScriptArgs(String[] args)
            throws org.luaj.debug.j2se.StandardLuaJVM.ParseException {
        if (args == null || args.length < 1) {
            throw new ParseException("script is missing.");
        }

        this.script = args[0];
        int scriptArgsLength = args.length - 1;
        if (scriptArgsLength > 0) {
            this.scriptArgs = new String[scriptArgsLength];
            for (int i = 1; i < args.length; i++) {
                this.scriptArgs[i - 1] = args[i];
            }
        }
    }

    // end of command line parsing utilities

    boolean isDebug() {
        return this.isDebugMode;
    }

    int getDebugPort() {
        return this.debugPort;
    }
    
    boolean getSuspendOnStart() {
        return this.bSuspendOnStart;
    }

    String getScript() {
        return this.script;
    }

    boolean hasScriptArgs() {
        return (this.scriptArgs != null && this.scriptArgs.length > 0);
    }

    String[] getScriptArgs() {
        return this.scriptArgs;
    }

    public void run() {
        try {
            if (isDebug()) {
                doDebug();
            } else {
                doRun();
            }
        } catch (LuaErrorException e) { 
            System.err.println("Error: " + e.getMessage());
            System.err.print(state.getStackTrace());
            System.err.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void init(LuaState state) {

        // add standard bindings
        state.installStandardLibs();

        // add LuaJava bindings
        LuajavaLib.install(state._G);  
        
        // add the compiler
        LuaC.install();
    }

    protected void doRun() throws IOException {

        // new lua state 
        state = new LuaState();
        init(state);

        // convert args to lua
        String[] scriptArgs = getScriptArgs();
        int numOfScriptArgs = (scriptArgs == null) ? 0 : scriptArgs.length;
        LValue[] vargs = new LValue[numOfScriptArgs];
        for (int i = 0; i < numOfScriptArgs; i++) {
            vargs[i] = new LString(getScriptArgs()[i]);
        }

        // load the Lua file
        InputStream is = new FileInputStream(new File(getScript()));
        LPrototype p = LoadState.undump(state, is, getScript());

        // create closure and execute
        LClosure c = new LClosure(state, p);        
        state.doCall(c, vargs);
    }

    protected void doDebug() throws IOException {
        // new lua debug state 
        state = new DebugLuaState();
        init(state);

        // load the Lua file
        InputStream is = new FileInputStream(new File(getScript()));
        LPrototype p = LoadState.undump(state, is, getScript());

        // set up debug support if the file is successfully loaded
        DebugSupport debugSupport = new DebugSupportImpl(getDebugPort());
        getDebugState().setSuspendAtStart(getSuspendOnStart());
        getDebugState().setDebugSupport(debugSupport);

        // create closure and execute
        final LClosure c = new LClosure(p, state._G);
        String[] args = getScriptArgs();
        int numOfScriptArgs = (args != null ? args.length : 0);
        LValue[] vargs = new LValue[numOfScriptArgs];
        for (int i = 0; i < numOfScriptArgs; i++) {
            vargs[i] = new LString(args[i]);
        }
        try {
            getDebugState().doCall(c, vargs);
        } finally {
            getDebugState().stop();
        }
    }

    private DebugLuaState getDebugState() {
        return (DebugLuaState) state;
    }

    /**
     * Parses the command line arguments and executes/debugs the lua program.
     * @param args -- command line arguments: 
     *  [-debug <requestPort> <eventPort>] <script> <script arguments separated by a whitespace>
     */
    public static void main(String[] args) {
        StandardLuaJVM vm = new StandardLuaJVM();

        try {
            vm.parse(args);
            vm.run();
        } catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            vm.printUsage();
            return;
        }
    }
}