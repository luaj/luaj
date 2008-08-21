package org.luaj;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.luaj");
		
		// debug tests
		TestSuite vm = new TestSuite("VM");
		vm.addTestSuite(org.luaj.vm.CompatibiltyTest.class);
		vm.addTestSuite(org.luaj.vm.ErrorMessageTest.class);
		vm.addTestSuite(org.luaj.vm.LuaStateTest.class);
		vm.addTestSuite(org.luaj.vm.LoadStateTest.class);
		vm.addTestSuite(org.luaj.vm.LStringTest.class);
		vm.addTestSuite(org.luaj.vm.MathLibTest.class);
		vm.addTestSuite(org.luaj.vm.LTableTest.class);
		vm.addTestSuite(org.luaj.vm.LWeakTableTest.class);
		suite.addTest(vm);
		
		// compiler tests
		TestSuite compiler = new TestSuite("Compiler");
		compiler.addTestSuite(org.luaj.compiler.SimpleTests.class);
		compiler.addTestSuite(org.luaj.compiler.RegressionTests.class);
		compiler.addTestSuite(org.luaj.compiler.CompilerUnitTests.class);
		compiler.addTestSuite(org.luaj.compiler.DumpLoadEndianIntTest.class);
		suite.addTest(compiler);
		
		return suite;
	}

}
