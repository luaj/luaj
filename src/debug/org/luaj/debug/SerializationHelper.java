package org.luaj.debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.luaj.debug.event.DebugEventBreakpoint;
import org.luaj.debug.event.DebugEventError;
import org.luaj.debug.request.DebugRequestDisconnect;
import org.luaj.debug.request.DebugRequestLineBreakpointToggle;
import org.luaj.debug.request.DebugRequestStack;
import org.luaj.debug.response.DebugResponseCallgraph;
import org.luaj.debug.response.DebugResponseSession;
import org.luaj.debug.response.DebugResponseStack;
import org.luaj.debug.response.DebugResponseVariables;

public class SerializationHelper {

    public static byte[] serialize(Serializable object) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);

        serialize(object, dout);

        byte[] data = bout.toByteArray();

        bout.close();
        dout.close();

        return data;
    }

    public static Serializable deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(bin);

        Serializable object = deserialize(din);

        bin.close();
        din.close();

        return object;
    }

    static final int SERIAL_TYPE_NullableString                         = 0;
    static final int SERIAL_TYPE_TableVariable                          = 1;
    static final int SERIAL_TYPE_Variable                               = 2;
    static final int SERIAL_TYPE_StackFrame                             = 3;
    static final int SERIAL_TYPE_DebugMessageType                       = 4;
    static final int SERIAL_TYPE_DebugMessage                           = 5;
    static final int SERIAL_TYPE_DebugRequestStack                      = 6;
    static final int SERIAL_TYPE_DebugRequestLineBreakpointToggle       = 7;
    static final int SERIAL_TYPE_DebugRequestDisconnect                 = 8;
    static final int SERIAL_TYPE_DebugEventBreakpoint                   = 9;
    static final int SERIAL_TYPE_DebugEventError                        = 10;
    static final int SERIAL_TYPE_DebugResponseCallgraph                 = 11;
    static final int SERIAL_TYPE_DebugResponseVariables                 = 12;
    static final int SERIAL_TYPE_DebugResponseStack                     = 13;   
    static final int SERIAL_TYPE_DebugResponseSession                   = 14;

    public static void serialize(Serializable object, DataOutputStream dout)
            throws IOException {
        if (object instanceof NullableString) {
            dout.writeInt(SERIAL_TYPE_NullableString);
            NullableString.serialize(dout, (NullableString) object);
        } else if (object instanceof TableVariable) {
            dout.writeInt(SERIAL_TYPE_TableVariable);
            TableVariable.serialize(dout, (TableVariable) object);
        } else if (object instanceof Variable) {
            dout.writeInt(SERIAL_TYPE_Variable);
            Variable.serialize(dout, (Variable) object);
        } else if (object instanceof StackFrame) {
            dout.writeInt(SERIAL_TYPE_StackFrame);
            StackFrame.serialize(dout, (StackFrame) object);
        } else if (object instanceof DebugMessageType) {
            dout.writeInt(SERIAL_TYPE_DebugMessageType);
            DebugMessageType.serialize(dout, (DebugMessageType) object);
        } else if (object instanceof DebugRequestStack) {
            dout.writeInt(SERIAL_TYPE_DebugRequestStack);
            DebugRequestStack.serialize(dout, (DebugRequestStack) object);
        } else if (object instanceof DebugRequestLineBreakpointToggle) {
            dout.writeInt(SERIAL_TYPE_DebugRequestLineBreakpointToggle);
            DebugRequestLineBreakpointToggle.serialize(dout,
                    (DebugRequestLineBreakpointToggle) object);
        } else if (object instanceof DebugRequestDisconnect) {
            dout.writeInt(SERIAL_TYPE_DebugRequestDisconnect);
            DebugRequestDisconnect.serialize(dout, (DebugRequestDisconnect) object);
        } else if (object instanceof DebugEventBreakpoint) {
            dout.writeInt(SERIAL_TYPE_DebugEventBreakpoint);
            DebugEventBreakpoint.serialize(dout, (DebugEventBreakpoint) object);
        } else if (object instanceof DebugEventError) {
            dout.writeInt(SERIAL_TYPE_DebugEventError);
            DebugEventError.serialize(dout, (DebugEventError) object);
        } else if (object instanceof DebugResponseStack) {
            dout.writeInt(SERIAL_TYPE_DebugResponseStack);
            DebugResponseStack.serialize(dout, (DebugResponseStack) object);
        } else if (object instanceof DebugResponseVariables) {
            dout.writeInt(SERIAL_TYPE_DebugResponseVariables);
            DebugResponseVariables.serialize(dout, (DebugResponseVariables) object);
        } else if (object instanceof DebugResponseCallgraph) {
            dout.writeInt(SERIAL_TYPE_DebugResponseCallgraph);
            DebugResponseCallgraph.serialize(dout,
                    (DebugResponseCallgraph) object);
        } else if (object instanceof DebugResponseSession) {
            dout.writeInt(SERIAL_TYPE_DebugResponseSession);
            DebugResponseSession.serialize(dout, (DebugResponseSession) object);
        } else if (object instanceof DebugMessage) {
            dout.writeInt(SERIAL_TYPE_DebugMessage);
            DebugMessage.serialize(dout, (DebugMessage) object);
        } else {
            // catch the errors: forgot to implement
            // serialization/deserialization
            throw new RuntimeException(
                    "serialization operation is not supported");
        }
    }

    public static Serializable deserialize(DataInputStream din)
            throws IOException {
        Serializable object = null;
        int type = din.readInt();
        switch (type) {
        case SERIAL_TYPE_NullableString:
            object = NullableString.deserialize(din);
            break;
        case SERIAL_TYPE_TableVariable:
            object = TableVariable.deserialize(din);
            break;
        case SERIAL_TYPE_Variable:
            object = Variable.deserialize(din);
            break;
        case SERIAL_TYPE_StackFrame:
            object = StackFrame.deserialize(din);
            break;
        case SERIAL_TYPE_DebugMessageType:
            object = DebugMessageType.deserialize(din);
            break;            
        case SERIAL_TYPE_DebugRequestStack:
            object = DebugRequestStack.deserialize(din);
            break;
        case SERIAL_TYPE_DebugRequestLineBreakpointToggle:
            object = DebugRequestLineBreakpointToggle.deserialize(din);
            break;
        case SERIAL_TYPE_DebugRequestDisconnect:
            object = DebugRequestDisconnect.deserialize(din);
            break;
        case SERIAL_TYPE_DebugEventBreakpoint:
            object = DebugEventBreakpoint.deserialize(din);
            break;
        case SERIAL_TYPE_DebugEventError:
            object = DebugEventError.deserialize(din);
            break;
        case SERIAL_TYPE_DebugResponseCallgraph:
            object = DebugResponseCallgraph.deserialize(din);
            break;
        case SERIAL_TYPE_DebugResponseStack:
            object = DebugResponseStack.deserialize(din);
            break;
        case SERIAL_TYPE_DebugResponseVariables:
            object = DebugResponseVariables.deserialize(din);
            break;
        case SERIAL_TYPE_DebugResponseSession:
            object = DebugResponseSession.deserialize(din);
            break;
        case SERIAL_TYPE_DebugMessage:
            object = DebugMessage.deserialize(din);
            break;
        default:
            throw new RuntimeException(
                    "deserialization operation is not supported: " + type);
        }

        return object;
    }
}
