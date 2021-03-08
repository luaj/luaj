package org.luaj.vm2.compiler;



public class CompilerUnitTests extends AbstractUnitTests {

    public CompilerUnitTests() {
        super("test/lua", "luaj3.0-tests.zip", "lua5.2.1-tests");
    }

    public void testAll()        { doTest("all.lua"); }
	public void testApi()        { doTest("api.lua"); }
	public void testAttrib()     { doTest("attrib.lua"); }
	public void testBig()        { doTest("big.lua"); }
	public void testBitwise()    { doTest("bitwise.lua"); }
	public void testCalls()      { doTest("calls.lua"); }
	public void testChecktable() { doTest("checktable.lua"); }
	public void testClosure()    { doTest("closure.lua"); }
	public void testCode()       { doTest("code.lua"); }
	public void testConstruct()  { doTest("constructs.lua"); }
	public void testCoroutine()  { doTest("coroutine.lua"); }
	public void testDb()         { doTest("db.lua"); }
	public void testErrors()     { doTest("errors.lua"); }
	public void testEvents()     { doTest("events.lua"); }
	public void testFiles()      { doTest("files.lua"); }
	public void testGc()         { doTest("gc.lua"); }
	public void testGoto()       { doTest("goto.lua"); }
	public void testLiterals()   { doTest("literals.lua"); }
	public void testLocals()     { doTest("locals.lua"); }
	public void testMain()       { doTest("main.lua"); }
	public void testMath()       { doTest("math.lua"); }
	public void testNextvar()    { doTest("nextvar.lua"); }
	public void testPm()         { doTest("pm.lua"); }
	public void testSort()       { doTest("sort.lua"); }
	public void testStrings()    { doTest("strings.lua"); }
	public void testVararg()     { doTest("vararg.lua"); }
	public void testVerybig()    { doTest("verybig.lua"); }
}
