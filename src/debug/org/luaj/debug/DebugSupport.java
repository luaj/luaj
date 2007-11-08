package org.luaj.debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.luaj.debug.event.DebugEvent;
import org.luaj.debug.event.DebugEventListener;
import org.luaj.debug.request.DebugRequest;
import org.luaj.debug.request.DebugRequestListener;
import org.luaj.debug.response.DebugResponse;

/**
 *  DebugSupport provides the network communication support for the debugger and
 *  debugee. 
 */
public class DebugSupport implements DebugRequestListener, DebugEventListener {
    protected static final int UNKNOWN = 0;
    protected static final int RUNNING = 1;
    protected static final int STOPPED = 2;

    protected DebugLuaState vm;
    protected int requestPort;
    protected int eventPort;
    protected Thread requestWatcherThread;
    protected int state = UNKNOWN;
    protected DataInputStream requestReader;
    protected DataOutputStream requestWriter;
    protected DataOutputStream eventWriter;

    public DebugSupport(int requestPort, int eventPort) {
        if (requestPort == -1) {
            throw new IllegalArgumentException("requestPort is invalid");
        }

        if (eventPort == -1) {
            throw new IllegalArgumentException("eventPort is invalid");
        }

        this.requestPort = requestPort;
        this.eventPort = eventPort;
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

        if (requestWriter != null) {
            try {
                requestWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
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
                    "DebugStackState is not set. Please call setDebugStackState first.");
        }

        this.requestWatcherThread = new Thread(new Runnable() {
            public void run() {
                loopForRequests();
                cleanup();
            }
        });
        this.requestWatcherThread.start();
        this.state = RUNNING;

        System.out.println("LuaJ debug server is started on ports: "
                + requestPort + ", " + eventPort);
    }

    protected synchronized int getState() {
        return this.state;
    }

    public synchronized void stop() {
        if (DebugUtils.IS_DEBUG)
            DebugUtils.println("stopping the debug support...");
        this.state = STOPPED;
    }

    protected void loopForRequests() {
        try {
            while (getState() != STOPPED) {
                int size = requestReader.readInt();
                byte[] data = new byte[size];
                requestReader.readFully(data);
                DebugRequest request = (DebugRequest) SerializationHelper
                        .deserialize(data);
                if (DebugUtils.IS_DEBUG)
                    DebugUtils.println("SERVER receives request: "
                            + request.toString());

                DebugResponse response = handleRequest(request);
                data = SerializationHelper.serialize(response);
                requestWriter.writeInt(data.length);
                requestWriter.write(data);
                requestWriter.flush();
                if (DebugUtils.IS_DEBUG)
                    DebugUtils.println("SERVER sends response: " + response);
            }
        } catch (EOFException e) {
            // expected during shutdown
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
    public DebugResponse handleRequest(DebugRequest request) {
        if (DebugUtils.IS_DEBUG) {
            DebugUtils.println("handling request: " + request.toString());
        }

        DebugResponse response = vm.handleRequest(request);
        return response;
    }
}