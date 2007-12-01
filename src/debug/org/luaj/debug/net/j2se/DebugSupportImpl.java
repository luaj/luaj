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
package org.luaj.debug.net.j2se;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.luaj.debug.DebugMessage;
import org.luaj.debug.net.DebugNetSupportBase;

/**
 * J2SE version of DebugNetSupportBase. The luaj-vm opens a port accepting the debug
 * client connections. The current implementation allows the vm to accept one 
 * and only one debug client connection at any time.
 */
public class DebugSupportImpl extends DebugNetSupportBase {	

    protected int numClientConnectionsAllowed;
    protected int numClientConnections = 0;
    protected ServerSocket serverSocket;
    protected int debugPort;
    protected ClientConnectionTask clientConnectionTask;
    
    /**
     * Creates an instance of DebugNetSupportBase at the given port
     * @param debugPort
     * @throws IOException
     */
    public DebugSupportImpl(int debugPort) throws IOException {
        this(debugPort, 1);
        this.serverSocket 
            = new ServerSocket(debugPort, this.numClientConnectionsAllowed);
    }
    
    /**
     * Creates a debugging service on the given port. The service will support
     * <code>numClientConnections</code> debug client connections.
     * @param port Debug port
     * @param connections
     * @throws IOException
     */
    protected DebugSupportImpl(int port, int connections) throws IOException {
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port value out of range: " + port);
        }
        
        if (connections <= 0) {
            throw new IllegalArgumentException("numClientConnections value must be greater than zero");   
        }
        
        this.debugPort = port;
        this.numClientConnectionsAllowed = connections;
    }
        
    public void start() throws IOException {
        if (this.vm == null) {
            throw new IllegalStateException(
                    "DebugLuaState is not set. Please call setDebugStackState first.");
        }
        
        setState(RUNNING);
        System.out.println("LuaJ debug server is listening on port: " + debugPort);
    
        new Thread(new Runnable() {
            public void run() {
                while (isRunning()) {
                    try {
                        acceptClientConnection();
                    } catch (IOException e) {}
                }
            }
        }).start();
    }

    public synchronized void incrementClientCount() {
        this.numClientConnections++;
    }
    
    public synchronized void decrementClientCount() {
        this.numClientConnections--;
    }
    
    public synchronized int getClientCount() {
        return this.numClientConnections;
    }
        
    public synchronized void stop() {
        setState(STOPPED);
        if (clientConnectionTask != null) {
            disconnect(clientConnectionTask.getSessionId());
        }
        dispose();
    }

    /*
     * (non-Javadoc)
     * @see org.luaj.debug.event.DebugEventListener#notifyDebugEvent(org.luaj.debug.DebugMessage)
     */
    public void notifyDebugEvent(DebugMessage event) {
        if (clientConnectionTask != null) {
            clientConnectionTask.notifyDebugEvent(event);
        }
    }
    
    public synchronized void disconnect() {
        disconnect(clientConnectionTask.getSessionId());
    }
    
    public synchronized void disconnect(int id) {
        if (clientConnectionTask.getSessionId() == id) {
            clientConnectionTask.disconnect();
            clientConnectionTask = null;
        } else {
            throw new RuntimeException("Internal Error: mismatching sesion Id");
        }
    }        
    
    public void acceptClientConnection() throws IOException {
        try {
            Socket clientSocket = serverSocket.accept();
            int count = getClientCount();
            if (count == numClientConnectionsAllowed) {
                clientSocket.close();
            } else {
                synchronized(this) {
                    incrementClientCount();
                    this.clientConnectionTask = new ClientConnectionTask(this, clientSocket);
                    new Thread(clientConnectionTask).start();
                }
            }
        } catch (IOException e) {
            dispose();
        }
    }
         
    protected synchronized void dispose() {
        if (this.clientConnectionTask != null) {
            clientConnectionTask.dispose();
            clientConnectionTask = null;
        }
        
        if (this.serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {}
        }        
    }
}
