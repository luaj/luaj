package lua.debug;

import junit.framework.TestCase;

public class EnumTypeTest extends TestCase {
	public void testDebugSupportStateSerialization() {
		try {
			DebugSupport.State stateIn = DebugSupport.State.RUNNING;
			byte[] data = SerializationHelper.serialize(stateIn);
			DebugSupport.State stateOut 
				= (DebugSupport.State) SerializationHelper.deserialize(data);
			assertEquals(stateIn, stateOut);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
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
