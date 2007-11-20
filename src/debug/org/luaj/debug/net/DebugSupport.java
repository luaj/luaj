package org.luaj.debug.net;

import java.io.IOException;

import org.luaj.debug.DebugLuaState;
import org.luaj.debug.event.DebugEventListener;
import org.luaj.debug.request.DebugRequestListener;

/**
 *  DebugSupport provides the network communication support for the debugger and
 *  debug clients. 
 */
public interface DebugSupport extends DebugRequestListener, DebugEventListener {
    public void start() throws IOException;
    public void stop();
    public void setDebugStackState(DebugLuaState vm);
    public void disconnect(int id);
}