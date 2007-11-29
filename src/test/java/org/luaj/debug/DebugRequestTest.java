package org.luaj.debug;

import java.io.IOException;

import junit.framework.TestCase;

import org.luaj.debug.request.DebugRequestLineBreakpointToggle;
import org.luaj.debug.request.DebugRequestStack;

public class DebugRequestTest extends TestCase {
    public void testDebugRequestSerialization() {
        try {
            DebugMessage request = new DebugMessage(DebugMessageType.resume);
            byte[] data = SerializationHelper.serialize(request);
            DebugMessage requestOut = (DebugMessage) SerializationHelper
                    .deserialize(data);
            assertNotNull(requestOut);
            assertEquals(request.getType(), requestOut.getType());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testDebugRequestStackSerialization() {
        try {
            DebugRequestStack request = new DebugRequestStack(1);
            byte[] data = SerializationHelper.serialize(request);
            DebugRequestStack requestOut = (DebugRequestStack) SerializationHelper
                    .deserialize(data);
            assertNotNull(requestOut);
            assertEquals(requestOut.getIndex(), 1);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testDebugRequestLineBreakpointToggleSerialization() {
        try {
            doTestDebugRequestLineBreakpointToggleSerialization(
                    DebugMessageType.lineBreakpointSet, "test.lua", 100);
            doTestDebugRequestLineBreakpointToggleSerialization(
                    DebugMessageType.lineBreakpointClear, "test.lua", 50);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void doTestDebugRequestLineBreakpointToggleSerialization(
            DebugMessageType type, String source, int lineNo)
            throws IOException {
        DebugRequestLineBreakpointToggle request = new DebugRequestLineBreakpointToggle(
                DebugMessageType.lineBreakpointSet, "test.lua", 100);
        byte[] data = SerializationHelper.serialize(request);
        DebugRequestLineBreakpointToggle requestOut = (DebugRequestLineBreakpointToggle) SerializationHelper
                .deserialize(data);
        assertNotNull(requestOut);
        assertEquals(request.getType(), requestOut.getType());
        assertEquals(request.getSource(), requestOut.getSource());
        assertEquals(request.getLineNumber(), requestOut.getLineNumber());
    }
}
