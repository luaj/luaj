package lua.debug;

import java.io.IOException;

import junit.framework.TestCase;

public class DebugEventTest extends TestCase {
	public void testDebugEventSerialization() {
		try {
			DebugEvent event = new DebugEvent(DebugEventType.started);
			byte[] data = SerializationHelper.serialize(event);
			DebugEvent eventOut = (DebugEvent)SerializationHelper.deserialize(data);
			assertNotNull(eventOut);
			assertEquals(event.getType(), eventOut.getType());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	public void testDebugEventBreakpointSerialization() {
		try {
			DebugEventBreakpoint event = new DebugEventBreakpoint("test.lua", 100);
			byte[] data = SerializationHelper.serialize(event);
			DebugEventBreakpoint eventOut 
				= (DebugEventBreakpoint) SerializationHelper.deserialize(data);
			assertNotNull(eventOut);
			assertEquals(event.getSource(), eventOut.getSource());
			assertEquals(event.getLineNumber(), eventOut.getLineNumber());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
}
