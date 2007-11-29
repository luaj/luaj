package org.luaj.debug.response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.luaj.debug.DebugMessage;
import org.luaj.debug.DebugMessageType;

public class DebugResponseSession extends DebugMessage {
    protected int sessionId;
    
    public DebugResponseSession(int id) {
        super(DebugMessageType.session);
        this.sessionId = id;
    }
    
    public int getSessionId() {
        return this.sessionId;
    }
    
    public String toString() {
        return "SessionId: " + getSessionId();
    }
    
    public static void serialize(DataOutputStream out,
                                 DebugResponseSession response) 
    throws IOException {
        out.writeInt(response.getSessionId());
    }
    
    public static DebugMessage deserialize(DataInputStream in) throws IOException {
        int id = in.readInt();
        return new DebugResponseSession(id);
    }
}
