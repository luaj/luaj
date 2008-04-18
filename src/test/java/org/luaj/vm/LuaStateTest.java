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
}
