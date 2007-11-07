package org.luaj.debug;

import java.io.IOException;

import org.luaj.debug.SerializationHelper;
import org.luaj.debug.StackFrame;

import junit.framework.TestCase;

public class StackFrameTest extends TestCase {
	public void testSerialization() {
		try {
			StackFrame stackIn = new StackFrame(10, "test.lua");
			byte[] data = SerializationHelper.serialize(stackIn);
			StackFrame stackOut = (StackFrame) SerializationHelper.deserialize(data);
			assertNotNull(stackOut);
			assertEquals(stackIn, stackOut);
	    } catch (IOException e) {
			fail(e.getMessage());
		}
	}
}
