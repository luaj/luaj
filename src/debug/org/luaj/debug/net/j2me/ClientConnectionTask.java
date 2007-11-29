package org.luaj.debug.net.j2me;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import org.luaj.debug.DebugMessage;
import org.luaj.debug.DebugMessageType;
import org.luaj.debug.SerializationHelper;
import org.luaj.debug.event.DebugEventListener;
import org.luaj.debug.response.DebugResponseSession;

public class ClientConnectionTask implements Runnable, DebugEventListener {
    private static final boolean TRACE = true; //(null != System.getProperty("TRACE"));
    
    protected String host;
    protected int port;
    DebugSupportImpl debugSupport;
    protected boolean bDisconnected = false;
    
    protected SocketConnection connection;
    protected DataInputStream inStream;
    protected DataOutputStream outStream;
    
    public ClientConnectionTask(String host, int port, DebugSupportImpl debugSupport) {
        this.host = host;
        this.port = port;
        this.debugSupport = debugSupport;
    }
    
    public synchronized void disconnect () {
        this.bDisconnected = true;
        if (this.inStream != null) {
            try {
                inStream.close();
            } catch (IOException e) {}
        }
    }
    
    public boolean isDisconnected() {
        return this.bDisconnected;
    }
    
    public void run() {
        try {
            if ( TRACE )
                System.out.println("LuaJ debug server connecting to " + host + ":" + port);
            
            // create connection
            this.connection = (SocketConnection) 
                Connector.open("socket://" + host + ":" + port);
            this.inStream = connection.openDataInputStream();
            this.outStream = connection.openDataOutputStream();
    
            if ( TRACE )
                System.out.println("LuaJ debug server connected to " + host + ":" + port);
        
            // loop for incoming requests
            while ( !isDisconnected() ) {
                byte[] data = null;
                int size = inStream.readInt();
                data = new byte[size];
                inStream.readFully(data);
                                    
                DebugMessage request = (DebugMessage) SerializationHelper
                        .deserialize(data);
                if ( TRACE )
                    System.out.println("SERVER receives request: " + request.toString());
                
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
            
            debugSupport.disconnect(1);
        } finally {
            dispose();
        }
    }

    protected void dispose() {
        if (this.inStream != null) {
            try {
                inStream.close();
                inStream = null;
            } catch (IOException e) {}
        }
        
        if (this.outStream != null) {
            try {
                outStream.close();
                outStream = null;
            } catch (IOException e) {}
        }
        
        if (this.connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (IOException e) {}
        }
    }

    public void handleRequest(DebugMessage request) {
        if ( TRACE )
            System.out.println("SERVER handling request: " + request.toString());

        if (request.getType() == DebugMessageType.session) {
            notifyDebugEvent(new DebugResponseSession(1));
        } else {
            debugSupport.handleRequest(request);
        }
    }
    
    public void notifyDebugEvent(DebugMessage event) {
        if (outStream == null) return;
        
        if ( TRACE )
            System.out.println("SERVER sending event: " + event.toString());

        try {
            byte[] data = SerializationHelper.serialize(event);
            outStream.writeInt(data.length);
            outStream.write(data);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }         
    }
}
