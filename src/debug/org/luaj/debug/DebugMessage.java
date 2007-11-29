package org.luaj.debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DebugMessage implements Serializable {
    protected DebugMessageType type;

    public DebugMessage(DebugMessageType type) {
        this.type = type;
    }

    public DebugMessageType getType() {
        return type;
    }

    public void setType(DebugMessageType type) {
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return type.toString();
    }

    public static void serialize(DataOutputStream out, DebugMessage object)
            throws IOException {
        SerializationHelper.serialize(object.getType(), out);
    }

    public static DebugMessage deserialize(DataInputStream in) throws IOException {
        DebugMessageType type = (DebugMessageType) SerializationHelper
                .deserialize(in);
        return new DebugMessage(type);
    }
}
