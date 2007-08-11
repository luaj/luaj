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
import java.util.StringTokenizer;

import lua.addon.luajava.LuaJava;
import lua.debug.DebugRequestListener;
import lua.debug.DebugServer;
import lua.debug.DebugStackState;
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
    protected DebugServer debugServer;
    protected int requestPort;
    protected int eventPort;
    protected String script;
    protected String[] scriptArgs;
    protected DebugStackState state;
    
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
                this.script = fileArgs[0];
                this.scriptArgs = new String[fileArgs.length - 1];
                for (int i = 1; i < fileArgs.length; i++) {
                    this.scriptArgs[i-1] = fileArgs[i];
                }
            }
        } catch(NumberFormatException e) {
            throw new ParseException("Invalid port number: " + e.getMessage());
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
            setupDebugHooks(getRequestPort(), getEventPort());
        }

        // TODO: VM hook for debugging
        
        // add LuaJava bindings
        LuaJava.install();        

        // new lua state 
        state = new DebugStackState();

        // convert args to lua
        int numOfScriptArgs = getScriptArgs().length;
        LValue[] vargs = new LValue[numOfScriptArgs];
        for (int i = 0; i < numOfScriptArgs; i++) { 
            vargs[i] = new LString(getScriptArgs()[i]);
        }
        
        // load the Lua file
        System.out.println("loading Lua script '" + getScript() + "'");
        InputStream is = new FileInputStream(new File(getScript()));
        Proto p = LoadState.undump(state, is, getScript());
        
        // create closure and execute
        Closure c = new Closure(state, p);
        state.doCall(c, vargs);
    }
    
    protected void setupDebugHooks(int requestPort, int eventPort) 
    throws IOException {
        this.debugServer = new DebugServer(this, requestPort, eventPort);
        this.debugServer.start();
    }
    
    /* (non-Javadoc)
     * @see lua.debug.DebugRequestListener#handleRequest(java.lang.String)
     */
    public String handleRequest(String request) {
    	return state.handleRequest( request );
    }
    
    public void stop() {
        if (this.debugServer != null) {
            this.debugServer.stop();
            this.debugServer = null;
        }
    }

    /**
     * Parses the command line arguments and executes/debugs the lua program.
     * @param args -- command line arguments: 
     *  [-debug requestPort eventPort] -file luaProgram args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        LuaJVM vm = new LuaJVM();

        try {
            vm.parse(args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println();
            vm.printUsage(); 
            return;
        }

        vm.run();
    }
}
