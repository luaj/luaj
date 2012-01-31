package org.luaj.vm2.lib.jse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Permission;

import junit.framework.TestCase;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuajavaAccessibleMembersTest extends TestCase {
		
	private LuaTable _G;

    protected void setUp() throws Exception {
        super.setUp();
        _G = JsePlatform.standardGlobals();
     }

    private String invokeScript(String script) {
    	try {
        	InputStream is = new ByteArrayInputStream( script.getBytes("UTF8") );
			LuaFunction c = LuaC.instance.load( is, "script", _G );
			return c.call().tojstring();
    	} catch ( Exception e ) {
    		fail("exception: "+e );
    		return "failed";
    	}
    }
    
	public void testAccessFromPrivateClassImplementedMethod() {
		assertEquals("privateImpl-aaa-interface_method(bar)", invokeScript(
			"b = luajava.newInstance('"+TestClass.class.getName()+"');" +
			"a = b:create_PrivateImpl('aaa');" +
			"return a:interface_method('bar');"));
	}

	public void testAccessFromPrivateClassPublicMethod() {
		assertEquals("privateImpl-aaa-public_method", invokeScript(
			"b = luajava.newInstance('"+TestClass.class.getName()+"');" +
			"a = b:create_PrivateImpl('aaa');" +
			"return a:public_method();"));
	}

	public void testAccessFromPrivateClassGetPublicField() {
		assertEquals("aaa", invokeScript(
			"b = luajava.newInstance('"+TestClass.class.getName()+"');" +
			"a = b:create_PrivateImpl('aaa');" +
			"return a.public_field;"));
	}

	public void testAccessFromPrivateClassSetPublicField() {
		assertEquals("foo", invokeScript(
			"b = luajava.newInstance('"+TestClass.class.getName()+"');" +
			"a = b:create_PrivateImpl('aaa');" +
			"a.public_field = 'foo';" +
			"return a.public_field;"));
	}

	public void testAccessFromPrivateClassPublicConcstructor() {
		assertEquals("privateImpl-constructor", invokeScript(
			"b = luajava.newInstance('"+TestClass.class.getName()+"');" +
			"c = b:get_PrivateImplClass();" +
			"return luajava.new(c);"));
	}
}
