package lua.debug;

import junit.framework.TestCase;
import lua.debug.event.DebugEventType;
import lua.debug.request.DebugRequestType;

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
