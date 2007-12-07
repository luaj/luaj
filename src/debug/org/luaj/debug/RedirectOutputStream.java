package org.luaj.debug;

import java.io.IOException;
import java.io.OutputStream;

import org.luaj.debug.event.DebugEventListener;
import org.luaj.debug.event.DebugEventOutputRedirect;

public class RedirectOutputStream extends OutputStream {
    
    protected DebugEventListener listener;
    protected int count;
    protected byte[] buffer;

    public RedirectOutputStream(DebugEventListener listener) {
        this(listener, 1024);
    }
    
    public RedirectOutputStream(DebugEventListener listener, int count) {
        this.listener = listener;
        this.count = 0;
        this.buffer = new byte[count];
    }

    public void close() throws IOException {
        flushBuffer();
    }

    public synchronized void flush() throws IOException {
        flushBuffer();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        flushBuffer();
    }

    public void write(byte[] b) throws IOException {
        super.write(b);
        flushBuffer();
    }

    public synchronized void write(int b) throws IOException {
        if (count >= buffer.length) {
            flushBuffer();
        }
        buffer[count++] = (byte)b;
    }
    
    protected synchronized void flushBuffer(){
        if (count > 0) {
            String msg = new String(buffer, 0, this.count);
            listener.notifyDebugEvent(new DebugEventOutputRedirect(msg));
            count = 0;
        }
    }
}
