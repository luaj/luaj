package org.luaj.debug.response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.luaj.debug.event.DebugEvent;
import org.luaj.debug.event.DebugEventType;

public class DebugResponseSession extends DebugEvent {
    protected int sessionId;
    
    public DebugResponseSession(int id) {
        super(DebugEventType.session);
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
    
    public static DebugEvent deserialize(DataInputStream in) throws IOException {
        int id = in.readInt();
        return new DebugResponseSession(id);
    }
}
