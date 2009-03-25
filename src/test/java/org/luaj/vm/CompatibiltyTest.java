package org.luaj.vm;

import java.io.IOException;

/**
 * Compatibility tests for the Luaj VM
 * 
 * Results are compared for exact match with 
 * the installed C-based lua environment. 
 */
public class CompatibiltyTest extends ScriptDrivenTest {

	private static final String dir = "src/test/res";
	
	public CompatibiltyTest() {
		super(dir);
	}

	public void testTest1() throws IOException, InterruptedException {
		runTest("test1");
	}

	public void testTest2() throws IOException, InterruptedException {
		runTest("test2");
	}

	public void testTest3() throws IOException, InterruptedException {
		runTest("test3");
	}

	public void testTest4() throws IOException, InterruptedException {
		runTest("test4");
	}

	public void testTest5() throws IOException, InterruptedException {
		runTest("test5");
	}

	public void testTest6() throws IOException, InterruptedException {
		runTest("test6");
	}

	public void testTest7() throws IOException, InterruptedException {
		runTest("test7");
	}

	public void testTest8() throws IOException, InterruptedException {
		runTest("test8");
	}

	public void testAutoload() throws IOException, InterruptedException {
		runTest("autoload");
	}

	public void testBaseLib() throws IOException, InterruptedException {
		runTest("baselib");
	}

	public void testBoolean() throws IOException, InterruptedException {
		runTest("boolean");
	}

	public void testCalls() throws IOException, InterruptedException {
		runTest("calls");
	}

	public void testCoercions() throws IOException, InterruptedException {
		runTest("coercions");
	}

	public void testCoroutines() throws IOException, InterruptedException {
		runTest("coroutines");
	}

	public void testCompare() throws IOException, InterruptedException {
		runTest("compare");
	}

	public void testDebugLib() throws IOException, InterruptedException {
		runTest("debuglib");
	}

	public void testErrors() throws IOException, InterruptedException {
		runTest("errors");
	}

	public void testHugeTable() throws IOException, InterruptedException {
		runTest("hugetable");
	}

	public void testIoLib() throws IOException, InterruptedException {
		runTest("iolib");
	}

	public void testLoops() throws IOException, InterruptedException {
		runTest("loops");
	}

	public void testManyLocals() throws IOException, InterruptedException {
		runTest("manylocals");
	}

	public void testMathLib() throws IOException, InterruptedException {
		runTest("mathlib");
	}

	public void testMetatables() throws IOException, InterruptedException {
		runTest("metatables");
	}

	public void testModule() throws IOException, InterruptedException {
		runTest("module");
	}

	public void testNext() throws IOException, InterruptedException {
		runTest("next");
	}

	public void testPcalls() throws IOException, InterruptedException {
		runTest("pcalls");
	}

	public void testPrint() throws IOException, InterruptedException {
		runTest("print");
	}

	public void testRequire() throws IOException, InterruptedException {
		runTest("require");
	}

	public void testSelect() throws IOException, InterruptedException {
		runTest("select");
	}

	public void testSetfenv() throws IOException, InterruptedException {
		runTest("setfenv");
	}

	public void testSetlist() throws IOException, InterruptedException {
		runTest("setlist");
	}

	public void testSimpleMetatables() throws IOException, InterruptedException {
		runTest("simplemetatables");
	}

	public void testStack() throws IOException, InterruptedException {
		runTest("stack");
	}

	public void testStrLib() throws IOException, InterruptedException {
		runTest("strlib");
	}

	public void testSort() throws IOException, InterruptedException {
		runTest("sort");
	}

	public void testTable() throws IOException, InterruptedException {
		runTest("table");
	}

	public void testTailcall() throws IOException, InterruptedException {
		runTest("tailcall");
	}
	
	public void testType() throws IOException, InterruptedException {
		runTest("type");
	}

	public void testUpvalues() throws IOException, InterruptedException {
		runTest("upvalues");
	}

	public void testUpvalues2() throws IOException, InterruptedException {
		runTest("upvalues2");
	}

	public void testUpvalues3() throws IOException, InterruptedException {
		runTest("upvalues3");
	}

	public void testVarargs() throws IOException, InterruptedException {
		runTest("varargs");
	}

	public void testWeakTable() throws IOException, InterruptedException {
		runTest("weaktable");
	}
}
