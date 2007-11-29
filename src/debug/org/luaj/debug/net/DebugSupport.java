package org.luaj.debug.net;

import java.io.IOException;

import org.luaj.debug.DebugLuaState;
import org.luaj.debug.DebugMessage;
import org.luaj.debug.event.DebugEventListener;
import org.luaj.debug.request.DebugRequestListener;

/**
 *  DebugSupport provides the network communication support between the luaj-vm 
 *  and debug clients. 
 */
public abstract class DebugSupport implements DebugRequestListener, DebugEventListener {
    
    public abstract void start() throws IOException;
    
    public abstract void stop();

    /**
     * Disconnect all connected clients.
     */
    public abstract void disconnect();
    
    /**
     * Disconnect the client with the given id.
     * @param id -- client id
     */
    public abstract void disconnect(int id);

    protected DebugLuaState vm;
    public void setDebugStackState(DebugLuaState vm) {
        this.vm = vm;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.luaj.debug.request.DebugRequestListener#handleRequest(org.luaj.debug.request.DebugRequest)
     */
    public void handleRequest(DebugMessage request) {
        vm.handleRequest(request);
    }
    
    protected static final int UNKNOWN = 0;
    protected static final int RUNNING = 1;
    protected static final int STOPPED = 2;
    
    protected int state = UNKNOWN;
    protected synchronized void setState(int state) {
        this.state = state;
    }
    
    protected synchronized boolean isRunning() {
        return this.state == RUNNING;
    }
}