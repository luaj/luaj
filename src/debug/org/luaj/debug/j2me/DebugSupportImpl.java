package org.luaj.debug.j2me;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

import org.luaj.debug.DebugSupport;

public class DebugSupportImpl extends DebugSupport {
    protected ServerSocketConnection serverConnection;
    protected SocketConnection clientDebugConnection;
    protected SocketConnection eventSocketConnection;

    public DebugSupportImpl(int debugPort) throws IOException {
        super(debugPort);
        this.serverConnection 
            = (ServerSocketConnection) Connector.open(
                    "socket://:" + this.debugPort, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see lua.debug.j2se.DebugSupport#start()
     */
    public void acceptClientConnection() throws IOException {
        // Set up the request socket and request input + event output streams
        this.clientDebugConnection 
            = (SocketConnection) serverConnection.acceptAndOpen();
        clientDebugConnection.setSocketOption(SocketConnection.DELAY, 0);
        clientDebugConnection.setSocketOption(SocketConnection.LINGER, 0);
        clientDebugConnection.setSocketOption(SocketConnection.KEEPALIVE, 1);
        clientDebugConnection.setSocketOption(SocketConnection.RCVBUF, 1024);
        clientDebugConnection.setSocketOption(SocketConnection.SNDBUF, 1024);
        this.requestReader = clientDebugConnection.openDataInputStream();
        this.eventWriter = clientDebugConnection.openDataOutputStream();

        System.out.println("Lua debug server is started on ports: " + debugPort);
    }

    protected void releaseClientConnection() {
        super.dispose();

        if (this.clientDebugConnection != null) {
            try {
                clientDebugConnection.close();
                clientDebugConnection = null;
            } catch (IOException e) {
            }
        }        
    }
    
    protected void dispose() {
        releaseClientConnection();
        
        if (this.serverConnection != null) {
            try {
                serverConnection.close();
                serverConnection = null;
            } catch (IOException e) {
            }
        }
    }
}
