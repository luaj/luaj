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
package lua.debug.j2se;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lua.debug.DebugUtils;
import lua.debug.SerializationHelper;
import lua.debug.event.DebugEvent;
import lua.debug.event.DebugEventListener;
import lua.debug.request.DebugRequest;
import lua.debug.request.DebugRequestListener;
import lua.debug.response.DebugResponse;

public class DebugSupport implements DebugEventListener {
	protected static final int UNKNOWN = 0;
	protected static final int RUNNING = 1;
	protected static final int STOPPED = 2;
	
    protected DebugRequestListener listener;
    protected int requestPort;
    protected int eventPort;
    protected Thread requestWatcherThread;
    protected int state = UNKNOWN;
    
    protected ServerSocket requestSocket;
    protected Socket clientRequestSocket;
    protected DataInputStream requestReader;
    protected DataOutputStream requestWriter;
    
    protected ServerSocket eventSocket;
    protected Socket clientEventSocket;
    protected DataOutputStream eventWriter;
        
    public DebugSupport(DebugRequestListener listener, 
                       int requestPort, 
                       int eventPort) {
        this.listener = listener;
        this.requestPort = requestPort;
        this.eventPort = eventPort;
    }
        
    protected void releaseServer() {
        DebugUtils.println("shutting down the debug server...");
        if (requestReader != null) {
            try {
                requestReader.close();
            } catch (IOException e) {}
        }
        
        if (requestWriter != null) {
            try {
                requestWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if (clientRequestSocket != null) {
            try {
                clientRequestSocket.close();
            } catch (IOException e) {}
        }
        
        if (requestSocket != null) {
            try {
                requestSocket.close();
            } catch (IOException e) {}
        }
        
        if (eventWriter != null) {
            try {
                eventWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if (clientEventSocket != null) {
            try {
                clientEventSocket.close();
            } catch (IOException e) {}
        }
        
        if (eventSocket != null){
            try {
                eventSocket.close();
            } catch (IOException e) {}
        }
    }
    
    public synchronized void start() throws IOException {
        this.requestSocket = new ServerSocket(requestPort);
        this.clientRequestSocket = requestSocket.accept();
        this.requestReader 
            = new DataInputStream(clientRequestSocket.getInputStream());
        this.requestWriter 
            = new DataOutputStream(clientRequestSocket.getOutputStream());
        
        this.eventSocket = new ServerSocket(eventPort);
        this.clientEventSocket = eventSocket.accept();
        this.eventWriter 
            = new DataOutputStream(clientEventSocket.getOutputStream());                 

        this.requestWatcherThread = new Thread(new Runnable() {
            public void run() {
                if (getState() != STOPPED) {
                    handleRequest();
                } else {
                    releaseServer();
                }
            }
        });
        this.requestWatcherThread.start();
        this.state = RUNNING;
    }
    
    public synchronized int getState() {
        return this.state;
    }
    
    public synchronized void stop() {
        this.state = STOPPED;
    }
    
    public void handleRequest() {        
        synchronized (clientRequestSocket) {
            try {
                while (getState() != STOPPED) {
                	int size = requestReader.readInt();
                	byte[] data = new byte[size];
                	requestReader.readFully(data);                	
                    DebugRequest request 
                        = (DebugRequest) SerializationHelper.deserialize(data);
                    DebugUtils.println("SERVER receives request: " + request.toString());
                    
                    DebugResponse response = listener.handleRequest(request);
                    data = SerializationHelper.serialize(response);
                    requestWriter.writeInt(data.length);
                    requestWriter.write(data);
                    requestWriter.flush();
                    DebugUtils.println("SERVER sends response: " + response);
                }
                
                if (getState() == STOPPED) {
                    cleanup();
                }
            } catch (EOFException e) {
                cleanup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    private void cleanup() {
        DebugUtils.println("SERVER terminated...");
        releaseServer();
        System.exit(0);
    }

    /**
     * This method provides the second communication channel with the debugging
     * client. The server can send events via this channel to notify the client
     * about debug events (see below) asynchonously.
     * 
     * The following events can be fired:
     * 1. started    -- the vm is started and ready to receive debugging requests
     *                  (guaranteed to be the first event sent)
     * 2. terminated -- the vm is terminated (guaranteed to be the last event sent)
     * 3. suspended client|step|breakpoint N
     *               -- the vm is suspended by client, due to a stepping request or
     *                  the breakpoint at line N is hit 
     * 4. resumed client|step
     *               -- the vm resumes execution by client or step
     *              
     * @param event
     */
    public void fireEvent(DebugEvent event) {
        DebugUtils.println("SERVER sending event: " + event.toString());
        synchronized (eventSocket) {
            try {
            	byte[] data = SerializationHelper.serialize(event);
                eventWriter.writeInt(data.length);
                eventWriter.write(data);
                eventWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }         
        }
    }

    /* (non-Javadoc)
     * @see lua.debug.DebugEventListener#notifyDebugEvent(lua.debug.DebugEvent)
     */
    public void notifyDebugEvent(DebugEvent event) {
        fireEvent(event);        
    }    
}
