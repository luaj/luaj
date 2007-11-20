package org.luaj.debug.request;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DebugRequestDisconnect extends DebugRequest {
    protected int sessionId;
    
    public DebugRequestDisconnect(int connectionId) {
        super(DebugRequestType.disconnect);
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

    public static DebugRequest deserialize(DataInputStream in)
            throws IOException {
        int id = in.readInt();

        return new DebugRequestDisconnect(id);
    }    
}
