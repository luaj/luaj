package org.luaj.debug;

import java.io.IOException;

import org.luaj.debug.SerializationHelper;
import org.luaj.debug.Variable;
import org.luaj.vm.Lua;

import junit.framework.TestCase;

public class VariableTest extends TestCase {
	public void testSerialization() {
		doTestSerialization(0, "varName", Lua.LUA_TSTRING, null);
		doTestSerialization(0, "varName", Lua.LUA_TSTRING, "varValue");
	}
	
	protected void doTestSerialization(int index, String varName, int type, String varValue) {
		Variable varIn = new Variable(index, varName, type, varValue);
		Variable varOut = null;
		try {
			byte[] data = SerializationHelper.serialize(varIn);
			varOut = (Variable) SerializationHelper.deserialize(data);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		assertNotNull(varOut);
		assertEquals(varIn, varOut);
	}
}
