package org.luaj.debug.response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.luaj.debug.DebugMessage;
import org.luaj.debug.DebugMessageType;
import org.luaj.debug.SerializationHelper;
import org.luaj.debug.Variable;

public class DebugResponseStack extends DebugResponseVariables {
    protected int stackFrameIndex;
    
    public DebugResponseStack(int index, Variable[] variables) {
        super(variables, DebugMessageType.clientRequestStackReply);
        this.stackFrameIndex = index;
    }
    
    public int getIndex() {
        return this.stackFrameIndex;
    }
    
    public static void serialize(DataOutputStream out,
                                 DebugResponseStack response) 
    throws IOException {
        out.writeInt(response.getIndex());
        
        Variable[] variables = response.getVariables();
        out.writeInt(variables == null ? 0 : variables.length);
        for (int i = 0; i < variables.length; i++) {
            SerializationHelper.serialize(variables[i], out);
        }
    }

    public static DebugMessage deserialize(DataInputStream in) throws IOException {
        int index = in.readInt();
        
        int count = in.readInt();
        Variable[] variables = new Variable[count];
        for (int i = 0; i < count; i++) {
            variables[i] = (Variable) SerializationHelper.deserialize(in);
        }

        return new DebugResponseStack(index, variables);
    }
}
