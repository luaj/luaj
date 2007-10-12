package lua.debug;

import java.io.IOException;

import junit.framework.TestCase;
import lua.Lua;
import lua.value.LBoolean;
import lua.value.LDouble;
import lua.value.LInteger;
import lua.value.LNil;
import lua.value.LString;
import lua.value.LTable;

public class TableVariableTest extends TestCase {
	public void testCreate() {
		LTable table = new LTable();
		table.put("key1", new LString("value1"));
		table.put("key2", new LNil());
		table.put("key3", LBoolean.TRUE);
		table.put("key4", LInteger.valueOf(10));
		table.put("key5", new LDouble(0.5));
		LTable childTable = new LTable();
		childTable.put("childKey1", new LString("childValue1"));		
		table.put("key6", childTable);
		
		TableVariable tableVariable = new TableVariable(0, "tableVar", Lua.LUA_TTABLE, table);
		String[] keys = tableVariable.getKeys();
		assertNotNull(keys);
		assertEquals(keys.length, 6);
		for (int i = 0; i < 6; i++) {
			assertTrue(keys[i].startsWith("key"));
		}
		Object[] values = tableVariable.getValues();
		assertNotNull(values);
		assertEquals(values.length, 6);
		for (int i = 0; i < 6; i++) {
			if (values[i] instanceof String) {
				assertTrue(values[i].equals("value1") ||
						   values[i].equals("nil") ||
						   values[i].equals("true") ||
						   values[i].equals("10") ||
						   values[i].equals("0.5"));
			} else if (values[i] instanceof TableVariable) {
				TableVariable child = (TableVariable) values[i];
				
				String[] childKeys = child.getKeys();
				assertNotNull(childKeys);
				assertEquals(childKeys.length, 1);
				assertEquals(childKeys[0], "childKey1");
				
				Object[] childValues = child.getValues();
				assertNotNull(childValues);
				assertEquals(childValues.length, 1);
				assertEquals(childValues[0], "childValue1");
			} else {
				fail("bad value type");
			}
		}
	}
	
	public void testSerialization() {
		String[] keys = null;
		Object[] values = null;
		try {
			doTestSerialization(0, "name", Lua.LUA_TTABLE, keys, values);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		keys = new String[0];
		values = new String[0];
		try {
			doTestSerialization(0, "name", Lua.LUA_TTABLE, keys, values);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		keys = new String[] {"key1", "key2"};
		values = new String[] {"value1", "value2"};
		try {
			doTestSerialization(0, "name", Lua.LUA_TTABLE, keys, values);
		} catch (IOException e) {
			fail(e.getMessage());
		}

		TableVariable grandchild = new TableVariable(1, "child", Lua.LUA_TTABLE, keys, values);
		keys = new String[] {"grandchild1", "grandchild2", "grandchild3"};
		values = new Object[] {"value1", "value2", grandchild};
		TableVariable child = new TableVariable(1, "child", Lua.LUA_TTABLE, keys, values);
		keys = new String[] {"child1", "child2"};
		values = new Object[] {"value1", child};
		try {
			doTestSerialization(0, "name", Lua.LUA_TTABLE, keys, values);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	protected void doTestSerialization(int index, String name, int type, 
			                           String[] keys, Object[] values) 
	throws IOException {
		TableVariable in = new TableVariable(index, name, type, keys, values);
		byte[] data = SerializationHelper.serialize(in);
		TableVariable out = (TableVariable) SerializationHelper.deserialize(data);
		
		assertTableVariable(in, out);
	}

	private void assertTableVariable(TableVariable in, TableVariable out) {
		assertEquals(in.getIndex(), out.getIndex());
		assertEquals(in.getType(), out.getType());
		assertEquals(in.getName(), out.getName());
		
		String[] inKeys = in.getKeys();
		String[] outKeys = out.getKeys();
		assertEquals(inKeys == null ? 0 : inKeys.length, outKeys == null ? 0 : outKeys.length);
		for (int i = 0; inKeys != null && i < inKeys.length; i++) {
			assertEquals(inKeys[i], outKeys[i]);
		}
		
		Object[] inValues = in.getValues();
		Object[] outValues = out.getValues();
		assertEquals(inValues == null ? 0 : inValues.length, outValues == null ? 0 : outValues.length);
		for (int i = 0; inValues != null && i < inValues.length; i++) {
			if (inValues[i] instanceof String && outValues[i] instanceof String) {
				assertEquals(inValues[i], outValues[i]);
			} else if (inValues[i] instanceof TableVariable && outValues[i] instanceof TableVariable) {
				assertTableVariable((TableVariable)inValues[i], (TableVariable)outValues[i]);
			} else {
				fail("bad serialization");
			}
		}
	}
}
