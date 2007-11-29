package org.luaj.debug.request;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.luaj.debug.DebugMessage;
import org.luaj.debug.DebugMessageType;

public class DebugRequestDisconnect extends DebugMessage {
    protected int sessionId;
    
    public DebugRequestDisconnect(int connectionId) {
        super(DebugMessageType.disconnect);
        this.sessionId = connectionId;
    }
    
    public int getSessionId() {
        return this.sessionId;
    }
    
    public String toString() {
        return super.toString() + " sessionId:" + getSessionId();
    }

    public static void serialize(DataOutputStream out, DebugRequestDisconnect request)
            throws IOException {
        out.writeInt(request.getSessionId());
    }

    public static DebugMessage deserialize(DataInputStream in)
            throws IOException {
        int id = in.readInt();

        return new DebugRequestDisconnect(id);
    }    
}
