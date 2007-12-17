package org.luaj.debug;

import junit.framework.TestCase;

public class EnumTypeTest extends TestCase {
    public void testDebugRequestTypeSerialization() {
        try {
            DebugMessageType type = DebugMessageType.lineBreakpointClear;
            byte[] data = SerializationHelper.serialize(type);
            DebugMessageType typeOut = (DebugMessageType) SerializationHelper
                    .deserialize(data);
            assertEquals(type, typeOut);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testDebugEventTypeSerialization() {
        try {
            DebugMessageType type = DebugMessageType.suspendedOnError;
            byte[] data = SerializationHelper.serialize(type);
            DebugMessageType typeOut = (DebugMessageType) SerializationHelper
                    .deserialize(data);
            assertNotNull(typeOut);
            assertEquals(type, typeOut);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
