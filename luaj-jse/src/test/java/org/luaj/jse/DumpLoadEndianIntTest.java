package org.luaj.jse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.DumpState;
import org.luaj.vm2.lib.jse.JsePlatform;

class DumpLoadEndianIntTest {
	private static final String SAVECHUNKS = "SAVECHUNKS";

	private static final boolean SHOULDPASS  = true;
	private static final boolean SHOULDFAIL  = false;
	private static final String  mixedscript = "return tostring(1234)..'-#!-'..tostring(23.75)";
	private static final String  intscript   = "return tostring(1234)..'-#!-'..tostring(23)";
	private static final String  withdoubles = "1234-#!-23.75";
	private static final String  withints    = "1234-#!-23";

	private Globals globals;

	@BeforeEach
	protected void setUp() throws Exception {
		globals = JsePlatform.standardGlobals();
		DumpState.ALLOW_INTEGER_CASTING = false;
	}

	@Test
	void testBigDoubleCompile() {
		doTest(false, DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES, false, mixedscript, withdoubles, withdoubles,
			SHOULDPASS);
		doTest(false, DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES, true, mixedscript, withdoubles, withdoubles,
			SHOULDPASS);
	}

	@Test
	void testLittleDoubleCompile() {
		doTest(true, DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES, false, mixedscript, withdoubles, withdoubles,
			SHOULDPASS);
		doTest(true, DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES, true, mixedscript, withdoubles, withdoubles,
			SHOULDPASS);
	}

	@Test
	void testBigIntCompile() {
		DumpState.ALLOW_INTEGER_CASTING = true;
		doTest(false, DumpState.NUMBER_FORMAT_INTS_ONLY, false, mixedscript, withdoubles, withints, SHOULDPASS);
		doTest(false, DumpState.NUMBER_FORMAT_INTS_ONLY, true, mixedscript, withdoubles, withints, SHOULDPASS);
		DumpState.ALLOW_INTEGER_CASTING = false;
		doTest(false, DumpState.NUMBER_FORMAT_INTS_ONLY, false, mixedscript, withdoubles, withints, SHOULDFAIL);
		doTest(false, DumpState.NUMBER_FORMAT_INTS_ONLY, true, mixedscript, withdoubles, withints, SHOULDFAIL);
		doTest(false, DumpState.NUMBER_FORMAT_INTS_ONLY, false, intscript, withints, withints, SHOULDPASS);
		doTest(false, DumpState.NUMBER_FORMAT_INTS_ONLY, true, intscript, withints, withints, SHOULDPASS);
	}

	@Test
	void testLittleIntCompile() {
		DumpState.ALLOW_INTEGER_CASTING = true;
		doTest(true, DumpState.NUMBER_FORMAT_INTS_ONLY, false, mixedscript, withdoubles, withints, SHOULDPASS);
		doTest(true, DumpState.NUMBER_FORMAT_INTS_ONLY, true, mixedscript, withdoubles, withints, SHOULDPASS);
		DumpState.ALLOW_INTEGER_CASTING = false;
		doTest(true, DumpState.NUMBER_FORMAT_INTS_ONLY, false, mixedscript, withdoubles, withints, SHOULDFAIL);
		doTest(true, DumpState.NUMBER_FORMAT_INTS_ONLY, true, mixedscript, withdoubles, withints, SHOULDFAIL);
		doTest(true, DumpState.NUMBER_FORMAT_INTS_ONLY, false, intscript, withints, withints, SHOULDPASS);
		doTest(true, DumpState.NUMBER_FORMAT_INTS_ONLY, true, intscript, withints, withints, SHOULDPASS);
	}

	@Test
	void testBigNumpatchCompile() {
		doTest(false, DumpState.NUMBER_FORMAT_NUM_PATCH_INT32, false, mixedscript, withdoubles, withdoubles,
			SHOULDPASS);
		doTest(false, DumpState.NUMBER_FORMAT_NUM_PATCH_INT32, true, mixedscript, withdoubles, withdoubles, SHOULDPASS);
	}

	@Test
	void testLittleNumpatchCompile() {
		doTest(true, DumpState.NUMBER_FORMAT_NUM_PATCH_INT32, false, mixedscript, withdoubles, withdoubles, SHOULDPASS);
		doTest(true, DumpState.NUMBER_FORMAT_NUM_PATCH_INT32, true, mixedscript, withdoubles, withdoubles, SHOULDPASS);
	}

	private void doTest(boolean littleEndian, int numberFormat, boolean stripDebug, String script,
		String expectedPriorDump, String expectedPostDump, boolean shouldPass) {
		try {

			// compile into prototype
			Reader reader = new StringReader(script);
			Prototype p = globals.compilePrototype(reader, "script");

			// double check script result before dumping
			LuaFunction f = new LuaClosure(p, globals);
			LuaValue r = f.call();
			String actual = r.tojstring();
			assertEquals(expectedPriorDump, actual);

			// dump into bytes
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				DumpState.dump(p, baos, stripDebug, numberFormat, littleEndian);
				if (!shouldPass)
					fail("dump should not have succeeded");
			} catch (Exception e) {
				if (shouldPass)
					fail("dump threw " + e);
				else
					return;
			}
			byte[] dumped = baos.toByteArray();

			// load again using compiler
			InputStream is = new ByteArrayInputStream(dumped);
			f = globals.load(is, "dumped", "b", globals).checkfunction();
			r = f.call();
			actual = r.tojstring();
			assertEquals(expectedPostDump, actual);

			// write test chunk
			if (System.getProperty(SAVECHUNKS) != null && script.equals(mixedscript)) {
				new File("build").mkdirs();
				String filename = "build/test-" + (littleEndian? "little-": "big-")
					+ (numberFormat == DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES? "double-"
						: numberFormat == DumpState.NUMBER_FORMAT_INTS_ONLY? "int-"
							: numberFormat == DumpState.NUMBER_FORMAT_NUM_PATCH_INT32? "numpatch4-": "???-")
					+ (stripDebug? "nodebug-": "debug-") + "bin.lua";
				FileOutputStream fos = new FileOutputStream(filename);
				fos.write(dumped);
				fos.close();
			}

		} catch (IOException e) {
			fail(e.toString());
		}
	}
}
