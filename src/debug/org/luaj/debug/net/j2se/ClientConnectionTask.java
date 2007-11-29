package org.luaj.debug.net.j2se;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import org.luaj.debug.DebugMessage;
import org.luaj.debug.DebugMessageType;
import org.luaj.debug.SerializationHelper;
import org.luaj.debug.event.DebugEventListener;
import org.luaj.debug.response.DebugResponseSession;

public class ClientConnectionTask implements Runnable, DebugEventListener {
    private static final boolean TRACE = (null != System.getProperty("TRACE"));
    protected static int counter = 1;
    
    protected int sessionId;
    protected Socket clientSocket;
    protected boolean bDisconnected = false;
    protected DataInputStream requestReader;
    protected DataOutputStream eventWriter;
    protected DebugSupportImpl debugSupport;
    protected boolean isDisposed = false;
    
    public ClientConnectionTask(DebugSupportImpl debugSupport, Socket socket) 
    throws IOException {
        this.debugSupport = debugSupport;       
        this.sessionId = getNextId();
        this.clientSocket = socket;
        this.requestReader 
            = new DataInputStream(clientSocket.getInputStream());
        this.eventWriter 
            = new DataOutputStream(clientSocket.getOutputStream()); 
    }
    
    protected synchronized int getNextId() {
        // when the number of ClientConnectionTask created approaches 
        // Integer.MAX_VALUE, reset to 1. We don't expect we have 
        // Integer.MAX_VALUE concurrent debug clients ever.
        if (counter == Integer.MAX_VALUE) {
            counter = 1;
        }
        
        return counter++;
    }
    
    public void disconnect () {
        this.bDisconnected = true;
    }
    
    public boolean isDisconnected() {
        return this.bDisconnected;
    }

    public int getSessionId() {
        return this.sessionId;
    }
    
    public void run() {
        try {
            // loop for incoming requests
            while (!isDisconnected()) {
                byte[] data = null;
                int size = requestReader.readInt();
                data = new byte[size];
                requestReader.readFully(data);
                                    
                DebugMessage request = (DebugMessage) SerializationHelper
                        .deserialize(data);                
                if (TRACE) {
                    System.out.println("SERVER receives request: " + request.toString());
                }
                
                handleRequest(request);
            }
        } catch (EOFException e) {
            // client has terminated the connection
            // it's time to exit.
        } catch (IOException e) {
            e.printStackTrace();
            
            // if the connected debugging client aborted abnormally,
            // discard the current connection.
            handleRequest(new DebugMessage(DebugMessageType.reset));
            
            debugSupport.disconnect(getSessionId());
        } finally {
            dispose();
        }
    }
    
    public void handleRequest(DebugMessage request) {
        if (TRACE) {
            System.out.println("SERVER handling request: " + request.toString());
        }

        if (request.getType() == DebugMessageType.session) {
            notifyDebugEvent(new DebugResponseSession(getSessionId()));
        } else {
            debugSupport.handleRequest(request);
        }
    }
    
    public void notifyDebugEvent(DebugMessage event) {
        if (TRACE)
            System.out.println("SERVER sending event: " + event.toString());

        try {
            byte[] data = SerializationHelper.serialize(event);
            eventWriter.writeInt(data.length);
            eventWriter.write(data);
            eventWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    public void dispose() {
        if ( this.isDisposed ) return;
        
        this.isDisposed = true;
        debugSupport.decrementClientCount();
        
        if (this.requestReader != null) {
            try {
                requestReader.close();
                requestReader = null;
            } catch (IOException e) {}
        }
        
        if (this.eventWriter != null) {
            try {
                eventWriter.close();
                eventWriter = null;
            } catch (IOException e) {}
        }
        
        if (this.clientSocket != null) {
            try {
                clientSocket.close();
                clientSocket = null;
            } catch (IOException e) {}
        }
    }
}
