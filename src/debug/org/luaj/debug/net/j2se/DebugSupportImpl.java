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

import org.luaj.debug.DebugLuaState;
import org.luaj.debug.event.DebugEvent;
import org.luaj.debug.net.DebugSupport;
import org.luaj.debug.request.DebugRequest;


public class DebugSupportImpl implements DebugSupport {	
    protected static final int UNKNOWN = 0;
    protected static final int RUNNING = 1;
    protected static final int STOPPED = 2;
    
    protected int state = UNKNOWN;
    protected int numClientConnectionsAllowed;
    protected int numClientConnections = 0;
    protected DebugLuaState vm;
    protected ServerSocket serverSocket;
    protected int debugPort;
    protected ClientConnectionTask clientConnectionTask;
    
    /**
     * Creates an instance of DebugSupportImpl at the given port
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
    
    public void setDebugStackState(DebugLuaState vm) {
        this.vm = vm;
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
    
    protected synchronized void setState(int state) {
        this.state = state;
    }
    
    protected synchronized boolean isRunning() {
        return this.state == RUNNING;
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
     * 
     * @see org.luaj.debug.event.DebugEventListener#notifyDebugEvent(org.luaj.debug.event.DebugEvent)
     */
    public void notifyDebugEvent(DebugEvent event) {
        if (clientConnectionTask != null) {
            clientConnectionTask.notifyDebugEvent(event);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.luaj.debug.request.DebugRequestListener#handleRequest(org.luaj.debug.request.DebugRequest)
     */
    public void handleRequest(DebugRequest request) {
        vm.handleRequest(request);
    }
    
    public synchronized void disconnect(int id) {
        if (clientConnectionTask.getSessionId() == id) {
            clientConnectionTask.disconnect();
            clientConnectionTask = null;
        } else {
            throw new RuntimeException("Internal Error: mismatching sesion Id: " + id + " current task: " + clientConnectionTask.getSessionId());
        }
    }        
    
    public void acceptClientConnection() throws IOException {
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
    }
         
    protected synchronized void dispose() {
        this.clientConnectionTask = null;
        if (this.serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {}
        }        
    }
}
