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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <code>DebugServer</code> manages the communications between LuaJ VM and 
 * the debugging client.
 * 
 * @author:  Shu Lei
 * @version: <version>
 */
public class DebugServer {
    public enum State {
        UNKNOWN,
        RUNNING,
        STOPPED
    }
    
    protected DebugRequestListener listener;
    protected int requestPort;
    protected int eventPort;
    protected Thread requestWatcherThread;
    protected State state = State.UNKNOWN;
    
    protected ServerSocket requestSocket;
    protected Socket clientRequestSocket;
    protected BufferedReader requestReader;
    protected PrintWriter requestWriter;
    
    protected ServerSocket eventSocket;
    protected Socket clientEventSocket;
    protected PrintWriter eventWriter;
        
    public DebugServer(DebugRequestListener listener, 
                       int requestPort, 
                       int eventPort) {
        this.listener = listener;
        this.requestPort = requestPort;
        this.eventPort = eventPort;
    }
        
    protected void destroy() {
        if (requestReader != null) {
            try {
                requestReader.close();
            } catch (IOException e) {}
        }
        
        if (requestWriter != null) {
            requestWriter.close();
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
            eventWriter.close();
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
        this.requestReader = new BufferedReader(
                new InputStreamReader(clientRequestSocket.getInputStream()));
        this.requestWriter = new PrintWriter(clientRequestSocket.getOutputStream());
        
        this.eventSocket = new ServerSocket(eventPort);
        this.clientEventSocket = eventSocket.accept();
        this.eventWriter = new PrintWriter(clientEventSocket.getOutputStream());                 

        this.requestWatcherThread = new Thread(new Runnable() {
            public void run() {
                if (getState() != State.STOPPED) {
                    handleRequest();
                } else {
                    destroy();
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
            String request = null;
            try {
                while (getState() != State.STOPPED &&
                       (request = requestReader.readLine()) != null) {
                    System.out.println("SERVER receives request: " + request);
                    String response = listener.handleRequest(request);
                    requestWriter.write(response);
                    requestWriter.flush();
                }
                
                if (getState() == State.STOPPED) {
                    destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    public void fireEvent(String event) {
        synchronized (eventSocket) {
            eventWriter.println(event);
            eventWriter.flush();            
        }
    }    
}
