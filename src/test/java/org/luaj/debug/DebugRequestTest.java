package org.luaj.debug;

import java.io.IOException;

import org.luaj.debug.SerializationHelper;
import org.luaj.debug.request.DebugRequest;
import org.luaj.debug.request.DebugRequestLineBreakpointToggle;
import org.luaj.debug.request.DebugRequestStack;
import org.luaj.debug.request.DebugRequestType;

import junit.framework.TestCase;

public class DebugRequestTest extends TestCase {
	public void testDebugRequestSerialization() {
		try {
			DebugRequest request = new DebugRequest(DebugRequestType.resume);
			byte[] data = SerializationHelper.serialize(request);
			DebugRequest requestOut 
				= (DebugRequest)SerializationHelper.deserialize(data);
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
			DebugRequestStack requestOut 
				= (DebugRequestStack) SerializationHelper.deserialize(data);
			assertNotNull(requestOut);
			assertEquals(requestOut.getIndex(), 1);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	public void testDebugRequestLineBreakpointToggleSerialization() {
		try 
		{
			doTestDebugRequestLineBreakpointToggleSerialization(DebugRequestType.lineBreakpointSet, "test.lua", 100);
			doTestDebugRequestLineBreakpointToggleSerialization(DebugRequestType.lineBreakpointClear, "test.lua", 50);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	private void doTestDebugRequestLineBreakpointToggleSerialization(
			DebugRequestType type, String source, int lineNo) throws IOException {
		DebugRequestLineBreakpointToggle request 
			= new DebugRequestLineBreakpointToggle(DebugRequestType.lineBreakpointSet, "test.lua", 100);
		byte[] data = SerializationHelper.serialize(request);
		DebugRequestLineBreakpointToggle requestOut = 
			(DebugRequestLineBreakpointToggle) SerializationHelper.deserialize(data);
		assertNotNull(requestOut);
		assertEquals(request.getType(), requestOut.getType());
		assertEquals(request.getSource(), requestOut.getSource());
		assertEquals(request.getLineNumber(), requestOut.getLineNumber());
	}
}
