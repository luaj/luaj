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
package lua;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;

import lua.addon.luacompat.LuaCompat;
import lua.addon.luajava.LuaJava;
import lua.debug.DebugEvent;
import lua.debug.DebugEventType;
import lua.debug.DebugRequest;
import lua.debug.DebugRequestListener;
import lua.debug.DebugResponse;
import lua.debug.DebugStackState;
import lua.debug.DebugSupport;
import lua.debug.DebugUtils;
import lua.io.Closure;
import lua.io.LoadState;
import lua.io.Proto;
import lua.value.LString;
import lua.value.LValue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * LuaJVM executes a lua program in normal run mode or debug mode. 
 * 
 * @author:  Shu Lei
 * @version: 1.0
 */
public class LuaJVM implements DebugRequestListener {        
    protected Options options = new Options();
    protected boolean isDebugMode = false;
    protected DebugSupport debugSupport;
    protected int requestPort;
    protected int eventPort;
    protected String script;
    protected String[] scriptArgs;
    protected StackState state;
    protected boolean isReady = false;
    protected boolean isTerminated = false;
    
    @SuppressWarnings("static-access")
    public LuaJVM() {
        options.addOption(OptionBuilder.withArgName("requestPort eventPort").
                                        hasArgs(2).
                                        isRequired(false).                                            
                                        withValueSeparator(' ').
                                        withDescription("run LuaJ VM in debug mode").
                                        create("debug"));
        options.addOption(OptionBuilder.withArgName("LuaJProgram").
                                        withDescription("lua program to be executed").                    
                                        isRequired().                    
                                        hasArgs().
                                        withValueSeparator(' ').                                            
                                        create("file"));            
    }
    
    protected void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java LuaJVM", options);            
    }
    
    protected void parse(String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("debug")) {
                this.isDebugMode = true;
                String[] ports = line.getOptionValues("debug");
                
                this.requestPort = Integer.parseInt(ports[0]);
                if (this.requestPort <= 0) {
                    throw new ParseException("Invalid request port: it must be greater than zero.");
                }
                
                this.eventPort = Integer.parseInt(ports[1]);
                if (this.eventPort <= 0) {
                    throw new ParseException("Invalid event port: it must be greater than zero.");
                }
                
                if (this.requestPort == this.eventPort) {
                    throw new ParseException("Invalid ports: request port and event port must be different");
                }
            }

            if (line.hasOption("file")) {
                String[] fileArgs = line.getOptionValues("file");
                this.script = URLDecoder.decode(fileArgs[0], "UTF-8");
                DebugUtils.println("Lua script to run: " + this.script);
                this.scriptArgs = new String[fileArgs.length - 1];
                for (int i = 1; i < fileArgs.length; i++) {
                    this.scriptArgs[i-1] = URLDecoder.decode(fileArgs[i], "UTF-8");
                }
            }
        } catch(NumberFormatException e) {
            throw new ParseException("Invalid port number: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new ParseException("Malformed program argument strings: " + e.getMessage());
        }
    }
    
    protected boolean isDebug() {
        return this.isDebugMode;
    }
    
    protected int getRequestPort() {
        return this.requestPort;
    }
    
    protected int getEventPort() {
        return this.eventPort;
    }
    
    protected String getScript() {
        return this.script;
    }
    
    protected boolean hasScriptArgs() {
        return (this.scriptArgs != null && this.scriptArgs.length > 0);
    }
    
    protected String[] getScriptArgs() {
        return this.scriptArgs;
    }

    public void run() throws IOException {
        if (isDebug()) {
            doDebug();
        } else {
            doRun();
        }
    }
    
    protected void init() {
        // reset global states
        GlobalState.resetGlobals();
        
        // add LuaJava bindings
        LuaJava.install();        

        // add LuaCompat bindings
        LuaCompat.install();        
    }
    
    public void doRun() throws IOException {
        init();
        
        // new lua state 
        state = new StackState();

        // convert args to lua
        int numOfScriptArgs = getScriptArgs().length;
        LValue[] vargs = new LValue[numOfScriptArgs];
        for (int i = 0; i < numOfScriptArgs; i++) { 
            vargs[i] = new LString(getScriptArgs()[i]);
        }
        
        // load the Lua file
        DebugUtils.println("loading Lua script '" + getScript() + "'");
        InputStream is = new FileInputStream(new File(getScript()));
        Proto p = LoadState.undump(state, is, getScript());
        
        // create closure and execute
        Closure c = new Closure(state, p);
        state.doCall(c, vargs);        
    }
    
    private void doDebug() throws IOException {
        DebugUtils.println("start debugging...");
        this.debugSupport = new DebugSupport(this, getRequestPort(), getEventPort());
        DebugUtils.println("created client request socket connection...");
        debugSupport.start();
        
        DebugUtils.println("setting up LuaJava and debug stack state...");
        
        init();
        
        // new lua state 
        state = new DebugStackState();
        getDebugState().addDebugEventListener(debugSupport);
        
        // load the Lua file
        DebugUtils.println("loading Lua script '" + getScript() + "'");
        InputStream is = new FileInputStream(new File(getScript()));
        Proto p = LoadState.undump(state, is, getScript());
        
        // create closure and execute
        final Closure c = new Closure(state, p);
        getDebugState().suspend();
        
        new Thread(new Runnable() {
            public void run() {                
                int numOfScriptArgs = getScriptArgs().length;
                LValue[] vargs = new LValue[numOfScriptArgs];
                for (int i = 0; i < numOfScriptArgs; i++) { 
                    vargs[i] = new LString(getScriptArgs()[i]);
                }
                
                getDebugState().doCall(c, vargs);
                stop();
            }
        }).start();
        
        debugSupport.fireEvent(new DebugEvent(DebugEventType.started));
    }   
    
    private DebugStackState getDebugState() {
        return (DebugStackState)state;
    }
    
    /* (non-Javadoc)
     * @see lua.debug.DebugRequestListener#handleRequest(java.lang.String)
     */
    public DebugResponse handleRequest(DebugRequest request) {
        if (!isDebug()) {
            throw new UnsupportedOperationException("Must be in debug mode to handle the debug requests");
        }
        
        DebugUtils.println("handling request: " + request.toString());
        switch (request.getType()) {
            case suspend:
                DebugResponse status = getDebugState().handleRequest(request);                
                DebugEvent event = new DebugEvent(DebugEventType.suspendedByClient);
                debugSupport.fireEvent(event);                
                return status;
            case resume:
                status = getDebugState().handleRequest(request);                
                event = new DebugEvent(DebugEventType.resumedByClient);
                debugSupport.fireEvent(event);                
                return status;
            case exit:
                stop();
            default:
                return getDebugState().handleRequest(request);
        }
    }
    
    protected void stop() {
        DebugUtils.println("exit LuaJ VM...");
        if (this.debugSupport != null) {
            DebugEvent event = new DebugEvent(DebugEventType.terminated);
            debugSupport.fireEvent(event);
            Timer timer = new Timer("DebugServerDeathThread");
            timer.schedule(new TimerTask() {
                public void run() {
                    debugSupport.stop();
                    debugSupport = null;                    
                }
            }, 500);
        }
        getDebugState().exit();
    }

    /**
     * Parses the command line arguments and executes/debugs the lua program.
     * @param args -- command line arguments: 
     *  [-debug requestPort eventPort] -file luaProgram args
     * @throws IOException
     */
    public static void main(String[] args) {
        LuaJVM vm = new LuaJVM();

        try {
            vm.parse(args);
        } catch (ParseException e) {
            DebugUtils.println(e.getMessage());
            vm.printUsage(); 
            return;
        }

        try {
            vm.run();
        } catch (IOException e) {
            //TODO: handle the error
            e.printStackTrace();
        }
    }
}
