package org.luaj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.luaj.vm2.Print;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.DumpState;

abstract class CompilingTestCase extends ResourcesTestCase {

	protected void doTest(String name) {
		try {
			// compile in memory
			Prototype p = globals.loadPrototype(inputStreamOfLua(name), "@" + name + ".lua", "bt");
			String actual = protoToString(p);

			// load expected value from jar
			Prototype e = globals.loadPrototype(inputStreamOfBytecode(name), name, "b");
			String expected = protoToString(e);

			// compare results
			assertEquals(expected, actual);

			// dump into memory
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DumpState.dump(p, baos, false);
			ByteArrayInputStream dumped = new ByteArrayInputStream(baos.toByteArray());

			// re-undump
			Prototype p2 = globals.loadPrototype(dumped, name, "b");
			String actual2 = protoToString(p2);

			// compare again
			assertEquals(actual, actual2);

		} catch (Exception e) {
			fail(e.toString());
		}
	}

	private String protoToString(Prototype p) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		Print.ps = ps;
		Print.printFunction(p, true);
		return baos.toString();
	}
}
