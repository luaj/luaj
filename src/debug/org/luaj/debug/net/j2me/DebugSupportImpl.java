package org.luaj.debug.net.j2me;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

import org.luaj.debug.DebugLuaState;
import org.luaj.debug.event.DebugEvent;
import org.luaj.debug.net.DebugSupport;
import org.luaj.debug.request.DebugRequest;

public class DebugSupportImpl implements DebugSupport {
    protected ServerSocketConnection serverConnection;

    public DebugSupportImpl(int port) throws IOException {
        this.serverConnection 
        = (ServerSocketConnection) Connector.open(
                "socket://:" + port);
    }
    
    public synchronized void acceptClientConnection() 
    throws IOException {        
        // Set up the request socket and request input + event output streams
        SocketConnection clientDebugConnection 
            = (SocketConnection) serverConnection.acceptAndOpen();
        clientDebugConnection.setSocketOption(SocketConnection.DELAY, 0);
        clientDebugConnection.setSocketOption(SocketConnection.LINGER, 0);
        clientDebugConnection.setSocketOption(SocketConnection.KEEPALIVE, 1);
        clientDebugConnection.setSocketOption(SocketConnection.RCVBUF, 1024);
        clientDebugConnection.setSocketOption(SocketConnection.SNDBUF, 1024);
        //TODO....
    }
    
    protected void dispose() {
        if (this.serverConnection != null) {
            try {
                serverConnection.close();
                serverConnection = null;
            } catch (IOException e) {
            }
        }
    }

    public void disconnect(int id) {
        // TODO Auto-generated method stub
        
    }

    public void setDebugStackState(DebugLuaState vm) {
        // TODO Auto-generated method stub
        
    }

    public void start() throws IOException {
        // TODO Auto-generated method stub
        
    }

    public void stop() {
        // TODO Auto-generated method stub
        
    }

    public void handleRequest(DebugRequest request) {
        // TODO Auto-generated method stub
        
    }

    public void notifyDebugEvent(DebugEvent event) {
        // TODO Auto-generated method stub
        
    }
}
