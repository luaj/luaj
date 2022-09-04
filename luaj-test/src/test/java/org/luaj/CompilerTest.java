package org.luaj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompilerTest extends CompilingTestCase {

	@BeforeEach
	@Override
	protected void setUp() {
		setBaseDir("lua5.2.1-tests");
		super.setUp();
	}

	@Test
	void testAll() { doTest("all"); }

	@Test
	void testApi() { doTest("api"); }

	@Test
	void testAttrib() { doTest("attrib"); }

	@Test
	void testBig() { doTest("big"); }

	@Test
	void testBitwise() { doTest("bitwise"); }

	@Test
	void testCalls() { doTest("calls"); }

	@Test
	void testChecktable() { doTest("checktable"); }

	@Test
	void testClosure() { doTest("closure"); }

	@Test
	void testCode() { doTest("code"); }

	@Test
	void testConstruct() { doTest("constructs"); }

	@Test
	void testCoroutine() { doTest("coroutine"); }

	@Test
	void testDb() { doTest("db"); }

	@Test
	void testErrors() { doTest("errors"); }

	@Test
	void testEvents() { doTest("events"); }

	@Test
	void testFiles() { doTest("files"); }

	@Test
	void testGc() { doTest("gc"); }

	@Test
	void testGoto() { doTest("goto"); }

	@Test
	void testLiterals() { doTest("literals"); }

	@Test
	void testLocals() { doTest("locals"); }

	@Test
	void testMain() { doTest("main"); }

	@Test
	void testMath() { doTest("math"); }

	@Test
	void testNextvar() { doTest("nextvar"); }

	@Test
	void testPm() { doTest("pm"); }

	@Test
	void testSort() { doTest("sort"); }

	@Test
	void testStrings() { doTest("strings"); }

	@Test
	void testVararg() { doTest("vararg"); }

	@Test
	void testVerybig() { doTest("verybig"); }
}
