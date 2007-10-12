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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;

import lua.GlobalState;
import lua.StackState;
import lua.addon.luacompat.LuaCompat;
import lua.addon.luajava.LuaJava;
import lua.io.Closure;
import lua.io.LoadState;
import lua.io.Proto;
import lua.value.LString;
import lua.value.LValue;

/**
 * StandardLuaJVM executes a lua program in normal run mode or debug mode. 
 * 
 * @author:  Shu Lei
 * @version: 1.0
 */
public class StandardLuaJVM implements DebugRequestListener {
    protected boolean isDebugMode = false;
    protected DebugSupport debugSupport;
    protected int requestPort;
    protected int eventPort;
    protected String script;
    protected String[] scriptArgs;
    protected StackState state;
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
        System.out.println("\t java StandardLuaJVM [-debug <requestPort> <eventPort>] <script> [<script arguments>]");
        System.out.println("where [] denotes an optional argument and <> denotes a placeholder.");
    }
    
    void parse(String[] args) throws ParseException {
    	if (args == null || args.length < 1) {
    		throw new ParseException("Invalid command line arguments.");
    	}

    	if ("-debug".equals(args[0])) {
    		if (args.length < 4) {
    			throw new ParseException("Invalid command line arguments.");
    		}
    		
    		this.isDebugMode = true;
    		try {
	    		this.requestPort = Integer.parseInt(args[1]);
	            if (this.requestPort <= 0) {
	                throw new ParseException("Invalid request port: it must be greater than zero.");
	            }
	
	            this.eventPort = Integer.parseInt(args[2]);
	            if (this.eventPort <= 0) {
	                throw new ParseException("Invalid event port: it must be greater than zero.");
	            }
	    	} catch(NumberFormatException e) {
	            throw new ParseException("Invalid port number: " + e.getMessage());
	        } 
	    	
            if (this.requestPort == this.eventPort) {
                throw new ParseException("Invalid ports: request port and event port must be different");
            }
            
            int tempArgsCount = args.length - 3;
            String[] tempArgs = new String[tempArgsCount];
            System.arraycopy(args, 3, tempArgs, 0, tempArgsCount);	            
            parseScriptArgs(tempArgs);
    	} else {
    		parseScriptArgs(args);
    	}
    }

	private void parseScriptArgs(String[] args)
			throws lua.debug.StandardLuaJVM.ParseException {
		if (args == null || args.length < 1) {
			throw new ParseException("script is missing.");
		}
		
		try {
		    this.script = URLDecoder.decode(args[0], "UTF-8");
		    DebugUtils.println("Lua script to run: " + this.script);
		    int scriptArgsLength = args.length - 1;
		    if (scriptArgsLength > 0) {
		        this.scriptArgs = new String[scriptArgsLength];
		        for (int i = 1; i < args.length; i++) {
		            this.scriptArgs[i - 1] = URLDecoder.decode(args[i], "UTF-8");
		        }            	
		    }
		} catch (UnsupportedEncodingException e) {
		    throw new ParseException("Malformed program argument strings: " + e.getMessage());
		}
	}
    // end of command line parsing utilities
	
    boolean isDebug() {
        return this.isDebugMode;
    }
    
    int getRequestPort() {
        return this.requestPort;
    }
    
    int getEventPort() {
        return this.eventPort;
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

    public void run() throws IOException {
        if (isDebug()) {
            doDebug();
        } else {
            doRun();
        }
    }
    
    protected void init() {
    	//Reset the _G table
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
        String[] scriptArgs = getScriptArgs();
        int numOfScriptArgs = (scriptArgs == null) ? 0 : scriptArgs.length;
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
        DebugUtils.println("setting up LuaJava and debug stack state...");        
        init();

        // load the Lua file
        DebugUtils.println("loading Lua script '" + getScript() + "'");
        InputStream is = new FileInputStream(new File(getScript()));
        Proto p = LoadState.undump(state, is, getScript());

        // set up debug support if the file is successfully loaded
        DebugUtils.println("start debugging...");
        this.debugSupport = new DebugSupport(this, getRequestPort(), getEventPort());
        DebugUtils.println("created client request socket connection...");
        debugSupport.start();

        // new lua debug state 
        state = new DebugStackState();
        getDebugState().addDebugEventListener(debugSupport);
        getDebugState().suspend();
        
        // create closure and execute
        final Closure c = new Closure(state, p);
        
        new Thread(new Runnable() {
            public void run() {        
            	String[] args = getScriptArgs();
                int numOfScriptArgs = (args != null ? args.length : 0);
                LValue[] vargs = new LValue[numOfScriptArgs];
                for (int i = 0; i < numOfScriptArgs; i++) { 
                    vargs[i] = new LString(args[i]);
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
        DebugRequestType requestType = request.getType();
        if (DebugRequestType.suspend == requestType) {
        	DebugResponse status = getDebugState().handleRequest(request);                
            DebugEvent event = new DebugEvent(DebugEventType.suspendedByClient);
            debugSupport.fireEvent(event);                
            return status;
        } else if (DebugRequestType.resume == requestType) {
        	DebugResponse status = getDebugState().handleRequest(request);                
        	DebugEvent event = new DebugEvent(DebugEventType.resumedByClient);
            debugSupport.fireEvent(event);                
            return status;
        } else if (DebugRequestType.exit == requestType) {
            stop();
            return DebugResponseSimple.SUCCESS;
        } else {
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
        } catch (IOException e) {
        	System.out.println("Error: " + e.getMessage());
        	e.printStackTrace();
        }
    }
}
