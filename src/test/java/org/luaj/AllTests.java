package org.luaj;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.luaj");
		
		// compiler tests
		TestSuite compiler = new TestSuite("Compiler");
		compiler.addTestSuite(org.luaj.compiler.SimpleTests.class);
		compiler.addTestSuite(org.luaj.compiler.RegressionTests.class);
		compiler.addTestSuite(org.luaj.compiler.CompilerUnitTests.class);
		suite.addTest(compiler);
		
		// debug tests
		TestSuite debug = new TestSuite("Debug");
		debug.addTestSuite(org.luaj.debug.DebugEventTest.class);
		debug.addTestSuite(org.luaj.debug.DebugRequestTest.class);
		debug.addTestSuite(org.luaj.debug.DebugResponseTest.class);
		debug.addTestSuite(org.luaj.debug.DebugStackStateTest.class);
		debug.addTestSuite(org.luaj.debug.EnumTypeTest.class);
		debug.addTestSuite(org.luaj.debug.StackFrameTest.class);
		debug.addTestSuite(org.luaj.debug.TableVariableTest.class);
		debug.addTestSuite(org.luaj.debug.VariableTest.class);
		debug.addTestSuite(org.luaj.debug.j2se.LuaJVMTest.class);
		suite.addTest(debug);
		
		// debug tests
		TestSuite vm = new TestSuite("VM");
		vm.addTestSuite(org.luaj.vm.LoadStateTest.class);
		vm.addTestSuite(org.luaj.vm.LStringTest.class);
		vm.addTestSuite(org.luaj.vm.LTableTest.class);
		vm.addTestSuite(org.luaj.vm.LuaJTest.class);
		suite.addTest(vm);
		
		return suite;
	}

}
