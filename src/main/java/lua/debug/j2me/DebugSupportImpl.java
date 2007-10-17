package lua.debug.j2me;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

import lua.debug.DebugSupport;
import lua.debug.DebugUtils;
import lua.debug.event.DebugEvent;

public class DebugSupportImpl extends DebugSupport {
	protected ServerSocketConnection requestServerConnection;
	protected SocketConnection requestSocketConnection;

	protected ServerSocketConnection eventServerConnection;
	protected SocketConnection eventSocketConnection;
	
	public DebugSupportImpl(int requestPort, 
            				int eventPort) {
		super(requestPort, eventPort);
	}
	
    /* (non-Javadoc)
	 * @see lua.debug.j2se.DebugSupport#start()
	 */
    public synchronized void start() throws IOException {
    	System.out.println("Starting the sockets....");
    	// Set up the request socket and request input + output streams
    	this.requestServerConnection 
    		= (ServerSocketConnection)Connector.open("socket://:" + this.requestPort);
    	this.requestSocketConnection = 
    		(SocketConnection) requestServerConnection.acceptAndOpen();
    	requestSocketConnection.setSocketOption(SocketConnection.DELAY, 0);
    	requestSocketConnection.setSocketOption(SocketConnection.LINGER, 0);
    	requestSocketConnection.setSocketOption(SocketConnection.KEEPALIVE, 1);
    	requestSocketConnection.setSocketOption(SocketConnection.RCVBUF, 1024);
    	requestSocketConnection.setSocketOption(SocketConnection.SNDBUF, 1024);
		this.requestReader = requestSocketConnection.openDataInputStream();
		this.requestWriter = requestSocketConnection.openDataOutputStream();

		// Set up the event socket and event output stream
		this.eventServerConnection 
			= (ServerSocketConnection)Connector.open("socket://:" + this.eventPort);
		this.eventSocketConnection 
			= (SocketConnection) eventServerConnection.acceptAndOpen();
		eventSocketConnection.setSocketOption(SocketConnection.DELAY, 0);
		eventSocketConnection.setSocketOption(SocketConnection.LINGER, 0);
		eventSocketConnection.setSocketOption(SocketConnection.KEEPALIVE, 1);
		eventSocketConnection.setSocketOption(SocketConnection.RCVBUF, 1024);
		eventSocketConnection.setSocketOption(SocketConnection.SNDBUF, 1024);
        this.eventWriter = eventSocketConnection.openDataOutputStream();;                 

        System.out.println("Lua debug server is started on ports: " + requestPort + ", " + eventPort);
        super.start();
    }

    protected void releaseServer() {
    	super.releaseServer();
    	
        if (requestSocketConnection != null) {
            try {
            	requestSocketConnection.close();
            } catch (IOException e) {}
        }
        
        if (requestServerConnection != null) {
            try {
            	requestServerConnection.close();
            } catch (IOException e) {}
        }
        
        if (eventSocketConnection != null) {
            try {
            	eventSocketConnection.close();
            } catch (IOException e) {}
        }
        
        if (eventServerConnection != null){
            try {
            	eventServerConnection.close();
            } catch (IOException e) {}
        }
    }
    
    protected void handleRequest() {        
        synchronized (requestSocketConnection) {
        	super.handleRequest();
        }
    }
    
    protected void sendEvent(DebugEvent event) {
        synchronized (eventSocketConnection) {
        	super.sendEvent(event);
        }
    }
}
