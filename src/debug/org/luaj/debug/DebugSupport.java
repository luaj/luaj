package org.luaj.debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.luaj.debug.event.DebugEvent;
import org.luaj.debug.event.DebugEventListener;
import org.luaj.debug.request.DebugRequest;
import org.luaj.debug.request.DebugRequestListener;

/**
 *  DebugSupport provides the network communication support for the debugger and
 *  debugee. 
 */
public abstract class DebugSupport implements DebugRequestListener, DebugEventListener {
    protected static final int UNKNOWN = 0;
    protected static final int RUNNING = 1;
    protected static final int STOPPED = 2;

    protected DebugLuaState vm;
    protected int debugPort;
    protected Thread requestWatcherThread;
    protected int state = UNKNOWN;
    protected DataInputStream requestReader;
    protected DataOutputStream eventWriter;

    public DebugSupport(int debugPort) {
        if (debugPort == -1) {
            throw new IllegalArgumentException("requestPort is invalid");
        }
        this.debugPort = debugPort;
    }

    public void setDebugStackState(DebugLuaState vm) {
        this.vm = vm;
    }

    protected void dispose() {
        if (DebugUtils.IS_DEBUG)
            DebugUtils.println("releasing the networkig resources...");

        if (requestReader != null) {
            try {
                requestReader.close();
            } catch (IOException e) {
            }
        }

        if (eventWriter != null) {
            try {
                eventWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isStarted() {
        return (state == RUNNING || state == STOPPED);
    }

    public synchronized void start() throws IOException {
        if (this.vm == null) {
            throw new IllegalStateException(
                    "DebugLuaState is not set. Please call setDebugStackState first.");
        }

        this.requestWatcherThread = new Thread(new Runnable() {
            public void run() {
                loopForRequests();
                cleanup();
            }
        });
        this.requestWatcherThread.start();
        this.state = RUNNING;

        System.out.println("LuaJ debug server is listening on port: " + debugPort);
    }

    protected synchronized int getState() {
        return this.state;
    }

    public synchronized void stop() {
        if (DebugUtils.IS_DEBUG)
            DebugUtils.println("stopping the debug support...");
        this.state = STOPPED;
    }

    public abstract Object getClientConnection();
    
    protected void loopForRequests() {
        try {
            while (getState() != STOPPED) {
                byte[] data = null;
                int size = requestReader.readInt();
                data = new byte[size];
                requestReader.readFully(data);
                                    
                DebugRequest request = (DebugRequest) SerializationHelper
                        .deserialize(data);                
                if (DebugUtils.IS_DEBUG) {
                    DebugUtils.println("SERVER receives request: " + request.toString());
                }
                
                handleRequest(request);
            }
        } catch (EOFException e) {
            // expected. it may occur depending on the timing during the termination
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanup() {
        if (DebugUtils.IS_DEBUG)
            DebugUtils.println("SERVER terminated...");

        dispose();
        System.exit(0);
    }

    /**
     * This method provides the second communication channel with the debugging
     * client. The server can send events via this channel to notify the client
     * about debug events (see below) asynchronously.
     * 
     * The following events can be fired: 1. started -- the vm is started and
     * ready to receive debugging requests (guaranteed to be the first event
     * sent) 2. terminated -- the vm is terminated (guaranteed to be the last
     * event sent) 3. suspended client|step|breakpoint N -- the vm is suspended
     * by client, due to a stepping request or the breakpoint at line N is hit
     * 4. resumed client|step -- the vm resumes execution by client or step
     * 
     * @param event
     */
    protected void sendEvent(DebugEvent event) {
        if (DebugUtils.IS_DEBUG)
            DebugUtils.println("SERVER sending event: " + event.toString());

        try {
            byte[] data = SerializationHelper.serialize(event);
            eventWriter.writeInt(data.length);
            eventWriter.write(data);
            eventWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.luaj.debug.event.DebugEventListener#notifyDebugEvent(org.luaj.debug.event.DebugEvent)
     */
    public void notifyDebugEvent(DebugEvent event) {
        sendEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.luaj.debug.request.DebugRequestListener#handleRequest(org.luaj.debug.request.DebugRequest)
     */
    public void handleRequest(DebugRequest request) {
        if (DebugUtils.IS_DEBUG) {
            DebugUtils.println("SERVER handling request: " + request.toString());
        }

        vm.handleRequest(request);
    }
}