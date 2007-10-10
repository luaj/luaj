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

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DebugSupport implements DebugEventListener {
    public static class State extends EnumType {
		private static final long serialVersionUID = 3657364516937093612L;
		
		public static final State UNKNOWN = new State("UNKNOWN", 0);
        public static final State RUNNING = new State("RUNNING", 1);
        public static final State STOPPED = new State("STOPPED", 2);
        
        public State(String name, int ordinal) {
        	super(name, ordinal);
        }
    }
    
    protected DebugRequestListener listener;
    protected int requestPort;
    protected int eventPort;
    protected Thread requestWatcherThread;
    protected State state = State.UNKNOWN;
    
    protected ServerSocket requestSocket;
    protected Socket clientRequestSocket;
    protected ObjectInputStream requestReader;
    protected ObjectOutputStream requestWriter;
    
    protected ServerSocket eventSocket;
    protected Socket clientEventSocket;
    protected ObjectOutputStream eventWriter;
        
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
            = new ObjectInputStream(clientRequestSocket.getInputStream());
        this.requestWriter 
            = new ObjectOutputStream(clientRequestSocket.getOutputStream());
        
        this.eventSocket = new ServerSocket(eventPort);
        this.clientEventSocket = eventSocket.accept();
        this.eventWriter 
            = new ObjectOutputStream(clientEventSocket.getOutputStream());                 

        this.requestWatcherThread = new Thread(new Runnable() {
            public void run() {
                if (getState() != State.STOPPED) {
                    handleRequest();
                } else {
                    releaseServer();
                }
            }
        });
        this.requestWatcherThread.start();
        this.state = State.RUNNING;
    }
    
    public synchronized State getState() {
        return this.state;
    }
    
    public synchronized void stop() {
        this.state = State.STOPPED;
    }
    
    public void handleRequest() {        
        synchronized (clientRequestSocket) {
            try {
                while (getState() != State.STOPPED) {
                    DebugRequest request 
                        = (DebugRequest) requestReader.readObject();
                    DebugUtils.println("SERVER receives request: " + request.toString());
                    DebugResponse response = listener.handleRequest(request);
                    requestWriter.writeObject(response);
                    requestWriter.flush();
                    DebugUtils.println("SERVER sends response: " + response);
                }
                
                if (getState() == State.STOPPED) {
                    cleanup();
                }
            } catch (EOFException e) {
                cleanup();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
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
                eventWriter.writeObject(event);
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
