package org.luaj.debug.j2me;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

import org.luaj.debug.DebugSupport;
import org.luaj.debug.event.DebugEvent;

public class DebugSupportImpl extends DebugSupport {
    protected ServerSocketConnection serverConnection;
    protected SocketConnection clientDebugConnection;
    protected SocketConnection eventSocketConnection;

    public DebugSupportImpl(int debugPort) {
        super(debugPort);
    }

    /*
     * (non-Javadoc)
     * 
     * @see lua.debug.j2se.DebugSupport#start()
     */
    public synchronized void start() throws IOException {
        // Set up the request socket and request input + event output streams
        this.serverConnection = (ServerSocketConnection) Connector
                .open("socket://:" + this.debugPort);
        this.clientDebugConnection = (SocketConnection) serverConnection
                .acceptAndOpen();
        clientDebugConnection.setSocketOption(SocketConnection.DELAY, 0);
        clientDebugConnection.setSocketOption(SocketConnection.LINGER, 0);
        clientDebugConnection.setSocketOption(SocketConnection.KEEPALIVE, 1);
        clientDebugConnection.setSocketOption(SocketConnection.RCVBUF, 1024);
        clientDebugConnection.setSocketOption(SocketConnection.SNDBUF, 1024);
        this.requestReader = clientDebugConnection.openDataInputStream();
        this.eventWriter = clientDebugConnection.openDataOutputStream();

        System.out.println("Lua debug server is started on ports: " + debugPort);
        super.start();
    }

    protected void dispose() {
        super.dispose();

        if (this.clientDebugConnection != null) {
            try {
                clientDebugConnection.close();
            } catch (IOException e) {
            }
        }

        if (this.serverConnection != null) {
            try {
                serverConnection.close();
            } catch (IOException e) {
            }
        }
    }

    public Object getClientConnection() {
        return clientDebugConnection;
    }

    protected void sendEvent(DebugEvent event) {
        synchronized (eventSocketConnection) {
            super.sendEvent(event);
        }
    }
}
