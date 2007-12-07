package org.luaj.debug.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.luaj.debug.DebugMessage;
import org.luaj.debug.DebugMessageType;

public class DebugEventOutputRedirect extends DebugMessage {
    protected String data;
    
    public DebugEventOutputRedirect(String data) {
        super(DebugMessageType.outputRedirect);
        this.data = data; 
    }
    
    public String getOutput() {
        return this.data;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return type.toString() + ": " + this.data;
    }

    public static void serialize(DataOutputStream out, DebugEventOutputRedirect event)
            throws IOException {
        out.writeUTF(event.getOutput());
    }

    public static DebugMessage deserialize(DataInputStream in) throws IOException {
        String data = in.readUTF();
        return new DebugEventOutputRedirect(data);
    }
}
