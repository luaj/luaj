package org.luaj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Framework to add regression tests as problem areas are found.
 * 
 * To add a new regression test: 1) run "unpack.sh" in the project root 2) add a
 * new "lua" file in the "regressions" subdirectory 3) run "repack.sh" in the
 * project root 4) add a line to the source file naming the new test
 * 
 * After adding a test, check in the zip file rather than the individual
 * regression test files.
 * 
 * @author jrosebor
 */
class RegressionsTest extends CompilingTestCase {

	@BeforeEach
	@Override
	protected void setUp() {
		setBaseDir("regressions");
		super.setUp();
	}

	@Test
	void testModulo() { doTest("modulo"); }

	@Test
	void testConstruct() { doTest("construct"); }

	@Test
	void testBigAttrs() { doTest("bigattr"); }

	@Test
	void testControlChars() { doTest("controlchars"); }

	@Test
	void testComparators() { doTest("comparators"); }

	@Test
	void testMathRandomseed() { doTest("mathrandomseed"); }

	@Test
	void testVarargs() { doTest("varargs"); }
}
