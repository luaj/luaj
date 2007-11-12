package org.luaj.debug;

import java.io.IOException;

import junit.framework.TestCase;

import org.luaj.debug.event.DebugEventType;
import org.luaj.debug.response.DebugResponseCallgraph;
import org.luaj.debug.response.DebugResponseStack;
import org.luaj.debug.response.DebugResponseVariables;
import org.luaj.vm.Lua;

public class DebugResponseTest extends TestCase {

    public void testDebugResponseStackSerialization() {
        try {
            doTestDebugResponseStackSerialization(0, null);

            doTestDebugResponseStackSerialization(1, new Variable[0]);

            Variable[] variables = new Variable[5];
            variables[0] = new Variable(0, "variable1", Lua.LUA_TSTRING,
                    "value1");
            variables[1] = new Variable(1, "variable2", Lua.LUA_TNIL, "nil");
            variables[2] = new Variable(2, "variable3", Lua.LUA_TBOOLEAN,
                    "false");

            TableVariable childTable = new TableVariable(0, "child",
                    Lua.LUA_TTABLE, new String[0], new Object[0]);
            String[] keys = new String[] { "key1", "key2" };
            Object[] values = new Object[] { "value1", childTable };
            variables[3] = new TableVariable(2, "variable4", Lua.LUA_TTABLE,
                    keys, values);

            variables[4] = new Variable(2, "variable3", Lua.LUA_TNUMBER, "10");
            doTestDebugResponseStackSerialization(2, variables);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void doTestDebugResponseStackSerialization(int index, Variable[] variables)
            throws IOException {
        DebugResponseStack stackIn = new DebugResponseStack(index, variables);
        byte[] data = SerializationHelper.serialize(stackIn);
        DebugResponseStack stackOut = (DebugResponseStack) SerializationHelper
                .deserialize(data);
        Variable[] variablesIn = stackIn.getVariables();
        Variable[] variablesOut = stackOut.getVariables();
        assertNotNull(variablesIn);
        assertNotNull(variablesOut);
        assertEquals(stackIn.getIndex(), stackOut.getIndex());
        assertEquals(variablesIn.length, variablesOut.length);
        for (int i = 0; i < variablesIn.length; i++) {
            assertEquals(variablesIn[i], variablesOut[i]);
        }
    }

    public void testDebugResponseCallgraphSerialization() {
        try {
            doTestDebugResponseCallgraphSerialization(null);
            doTestDebugResponseCallgraphSerialization(new StackFrame[0]);

            StackFrame[] frames = new StackFrame[1];
            frames[0] = new StackFrame(100, "test.lua");
            doTestDebugResponseCallgraphSerialization(frames);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void doTestDebugResponseCallgraphSerialization(StackFrame[] frames)
            throws IOException {
        DebugResponseCallgraph responseIn = new DebugResponseCallgraph(frames);
        byte[] data = SerializationHelper.serialize(responseIn);
        DebugResponseCallgraph responseOut = (DebugResponseCallgraph) SerializationHelper
                .deserialize(data);
        assertNotNull(responseOut);
        StackFrame[] inFrames = responseIn.getCallgraph();
        StackFrame[] outFrames = responseOut.getCallgraph();
        assertNotNull(outFrames);
        assertEquals(inFrames.length, outFrames.length);
        for (int i = 0; i < inFrames.length; i++) {
            assertEquals(inFrames[i], outFrames[i]);
        }
    }
}
