package org.luaj.vm2.compiler;

/**
 * Framework to add regression tests as problem areas are found.
 * 
 * To add a new regression test:
 *  1) run "unpack.sh" in the project root
 *  2) add a new "lua" file in the "regressions" subdirectory
 *  3) run "repack.sh" in the project root
 *  4) add a line to the source file naming the new test
 * 
 * After adding a test, check in the zip file 
 * rather than the individual regression test files.
 * 
 * @author jrosebor
 */
public class RegressionTests extends AbstractUnitTests {
	
	public RegressionTests() {
		super( "regressions.zip", 
				"regressions" );
	}
	
	public void testModulo()			{ doTest("modulo.lua"); }
	public void testConstruct()			{ doTest("construct.lua"); }
	public void testBigAttrs()			{ doTest("bigattr.lua"); }
	public void testControlChars()		{ doTest("controlchars.lua"); }
	public void testComparators()		{ doTest("comparators.lua"); }
	public void testMathRandomseed()	{ doTest("mathrandomseed.lua"); }
	public void testVarargs()			{ doTest("varargs.lua"); }
}
