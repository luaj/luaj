package org.luaj.vm;

import java.io.IOException;

import junit.framework.TestCase;

import org.luaj.TestPlatform;

public class LuaStateTest extends TestCase {
	LuaState vm;

	protected void setUp() throws Exception {
        Platform.setInstance(new TestPlatform());
		vm = Platform.newLuaState();		
	}
	
	public void testPushnilReplaceSettop() throws IOException {
		vm.pushnil();
		vm.replace(1);
		vm.settop(1);
		assertEquals( 1, vm.gettop() );
		assertEquals( LNil.NIL, vm.topointer(1) );
		assertEquals( LNil.NIL, vm.topointer(-1) );
	}
	
	public void testFuncCall() throws IOException {
		vm.pushstring("bogus");
		vm.pushfunction(new SomeFunc( "arg" ));
		vm.pushstring("arg");
		vm.call(1, 1);
		assertEquals( 2, vm.gettop() );
		assertEquals( "bogus", vm.tostring(1) );
		assertEquals( LNil.NIL, vm.topointer(2) );
		assertEquals( LNil.NIL, vm.topointer(-1) );
	}
		
	public void testFuncCall2() throws IOException {
		vm.pushstring("bogus");
		vm.pushfunction(new SomeFunc( "nil" ));
		vm.call(0, 1);
		assertEquals( 2, vm.gettop() );
		assertEquals( "bogus", vm.tostring(1) );
		assertEquals( LNil.NIL, vm.topointer(2) );
		assertEquals( LNil.NIL, vm.topointer(-1) );
	}
	
	private static final class SomeFunc extends LFunction {
		private String expected;
		public SomeFunc(String expected) {
			this.expected = expected;
		}
		public boolean luaStackCall(LuaState vm) {
			String arg = vm.tostring(2);
			assertEquals(expected, arg);
			vm.pushnil();
			vm.replace(1);
			vm.settop(1);
			return false;
		}
		
	}
}
