package org.luaj.debug.net.j2me;

import java.io.IOException;

import org.luaj.debug.DebugMessage;
import org.luaj.debug.net.DebugNetSupportBase;

/**
 * J2ME version of DebugNetSupportBase implementation. The vm will connect to a debug
 * service hosted on a remote machine and have the debug service relay the 
 * debugging messages between the vm and the debug client.
 */
public class DebugSupportImpl extends DebugNetSupportBase {
    protected String host;
    protected int port;
    ClientConnectionTask clientTask;
    Thread main;

    public DebugSupportImpl(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        setState(RUNNING);
        main = new Thread(new Runnable() {
            public void run() {
                while ( isRunning() ) {
                    synchronized (DebugSupportImpl.this) {
                        if ( clientTask == null ) {
                            clientTask = new ClientConnectionTask(host, port, DebugSupportImpl.this);
                            new Thread(clientTask).start();
                        }                        
                    }
                    
                    synchronized(main) {
                        try {
                            main.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }                        
                    }
                }
            }
        });
        main.start();
    }

    
    public void stop() {
        setState(STOPPED);
        disconnect(1);        
    }

    public void disconnect() {
        clientTask.disconnect();
        clientTask = null;
        synchronized(main) {
            main.notify();
        }        
    }
    
    public synchronized void disconnect(int id) {
        disconnect();
    }

    public synchronized void notifyDebugEvent(DebugMessage event) {
        if (clientTask != null) {
            clientTask.notifyDebugEvent(event);
        }        
    }
}
