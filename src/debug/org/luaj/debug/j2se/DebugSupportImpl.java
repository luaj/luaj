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
package org.luaj.debug.j2se;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.luaj.debug.DebugSupport;
import org.luaj.debug.event.DebugEvent;


public class DebugSupportImpl extends DebugSupport {	
    protected ServerSocket serverSocket;
    protected Socket clientSocket;
        
    public DebugSupportImpl(int debugPort) {
    	super(debugPort);
    }        
    
    /* (non-Javadoc)
	 * @see lua.debug.j2se.DebugSupport#start()
	 */
    public synchronized void start() throws IOException {
        this.serverSocket = new ServerSocket(debugPort, 1);
        this.clientSocket = serverSocket.accept();
        this.requestReader 
            = new DataInputStream(clientSocket.getInputStream());
        this.eventWriter 
            = new DataOutputStream(clientSocket.getOutputStream());                 
        
        super.start();
    }
     
    protected void dispose() {
    	super.dispose();
    	
        if (this.clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {}
        }
        
        if (this.serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {}
        }        
    }
    
    public Object getClientConnection() {        
        return clientSocket;
    }

    /**
     * This method provides the second communication channel with the debugging
     * client. The server can send events via this channel to notify the client
     * about debug events (see below) asynchronously.
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
    protected void sendEvent(DebugEvent event) {
        synchronized (clientSocket) {
            super.sendEvent(event);
        }
    }
}
