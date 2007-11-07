package org.luaj.debug;

import org.luaj.debug.SerializationHelper;
import org.luaj.debug.event.DebugEventType;
import org.luaj.debug.request.DebugRequestType;

import junit.framework.TestCase;

public class EnumTypeTest extends TestCase {
	public void testDebugRequestTypeSerialization() {
		try {
			DebugRequestType type = DebugRequestType.lineBreakpointClear;
			byte[] data = SerializationHelper.serialize(type);
			DebugRequestType typeOut 
				= (DebugRequestType) SerializationHelper.deserialize(data);
			assertEquals(type, typeOut);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testDebugEventTypeSerialization() {
		try {
			DebugEventType type = DebugEventType.error;
			byte[] data = SerializationHelper.serialize(type);
			DebugEventType typeOut = (DebugEventType) SerializationHelper.deserialize(data);
			assertNotNull(typeOut);
			assertEquals(type, typeOut);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
