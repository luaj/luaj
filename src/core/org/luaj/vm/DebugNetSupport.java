package org.luaj.vm;

import java.io.IOException;

public interface DebugNetSupport {

    /**
     * Starts the networking for debug support.
     * @throws IOException
     */
    public abstract void start() throws IOException;

    /**
     * Shuts down the networking for the debug support.
     */
    public abstract void stop();

    /**
     * Disconnect all connected clients.
     */
    public abstract void disconnect();
}