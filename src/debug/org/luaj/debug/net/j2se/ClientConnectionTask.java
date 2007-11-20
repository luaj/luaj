package org.luaj.debug.net.j2se;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import org.luaj.debug.SerializationHelper;
import org.luaj.debug.event.DebugEvent;
import org.luaj.debug.event.DebugEventListener;
import org.luaj.debug.request.DebugRequest;
import org.luaj.debug.request.DebugRequestType;
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
                                    
                DebugRequest request = (DebugRequest) SerializationHelper
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
            handleRequest(new DebugRequest(DebugRequestType.reset));            
        } finally {
            dispose();
        }
    }
    
    public void handleRequest(DebugRequest request) {
        if (TRACE) {
            System.out.println("SERVER handling request: " + request.toString());
        }

        if (request.getType() == DebugRequestType.session) {
            notifyDebugEvent(new DebugResponseSession(getSessionId()));
        } else {
            debugSupport.handleRequest(request);
        }
    }
    
    public void notifyDebugEvent(DebugEvent event) {
        if (TRACE)
            System.out.println("SERVER sending event: " + event.toString());
        
        if (event == null)
            System.out.println("notifyDebugEvent: event is null");       
        if (eventWriter == null)
            System.out.println("notifyDebugEvent: eventWriter is null");
        
        try {
            byte[] data = SerializationHelper.serialize(event);
            eventWriter.writeInt(data.length);
            eventWriter.write(data);
            eventWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    protected void dispose() {
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
