/*******************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.luaj.vm2.TypeTest.MyData;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.ZeroArgFunction;

public class LuaOperationsTest extends TestCase {
	
	private final int sampleint = 77;
	private final long samplelong = 123400000000L;
	private final double sampledouble = 55.25;
	private final String samplestringstring = "abcdef";
	private final String samplestringint = String.valueOf(sampleint);
	private final String samplestringlong = String.valueOf(samplelong);
	private final String samplestringdouble = String.valueOf(sampledouble);
	private final Object sampleobject = new Object();
	private final MyData sampledata = new MyData();
	
	private final LuaValue somenil       = LuaValue.NIL;
	private final LuaValue sometrue      = LuaValue.TRUE;
	private final LuaValue somefalse     = LuaValue.FALSE;
	private final LuaValue zero          = LuaValue.ZERO;
	private final LuaValue intint        = LuaValue.valueOf(sampleint);
	private final LuaValue longdouble    = LuaValue.valueOf(samplelong);
	private final LuaValue doubledouble  = LuaValue.valueOf(sampledouble);
	private final LuaValue stringstring  = LuaValue.valueOf(samplestringstring);
	private final LuaValue stringint     = LuaValue.valueOf(samplestringint);
	private final LuaValue stringlong    = LuaValue.valueOf(samplestringlong);
	private final LuaValue stringdouble  = LuaValue.valueOf(samplestringdouble);
	private final LuaTable    table         = LuaValue.listOf( new LuaValue[] { LuaValue.valueOf("aaa"), LuaValue.valueOf("bbb") } );
	private final LuaValue    somefunc      = new ZeroArgFunction(table) { public LuaValue call() { return NONE;}};
	private final LuaThread   thread        = new LuaThread(somefunc,table);
	private final Prototype   proto         = new Prototype();
	private final LuaClosure  someclosure   = new LuaClosure(proto,table);
	private final LuaUserdata userdataobj   = LuaValue.userdataOf(sampleobject);
	private final LuaUserdata userdatacls   = LuaValue.userdataOf(sampledata);
	
	private void throwsLuaError(String methodName, Object obj) {
		try {
			LuaValue.class.getMethod(methodName).invoke(obj);
			fail("failed to throw LuaError as required");
		} catch (InvocationTargetException e) {
			if ( ! (e.getTargetException() instanceof LuaError) )
				fail("not a LuaError: "+e.getTargetException());
			return; // pass
		} catch ( Exception e ) {
			fail( "bad exception: "+e );
		}
	}
	
	private void throwsLuaError(String methodName, Object obj, Object arg) {
		try {
			LuaValue.class.getMethod(methodName,LuaValue.class).invoke(obj,arg);
			fail("failed to throw LuaError as required");
		} catch (InvocationTargetException e) {
			if ( ! (e.getTargetException() instanceof LuaError) )
				fail("not a LuaError: "+e.getTargetException());
			return; // pass
		} catch ( Exception e ) {
			fail( "bad exception: "+e );
		}
	}
	
	public void testLen() {
		throwsLuaError( "len", somenil );
		throwsLuaError( "len", sometrue );
		throwsLuaError( "len", somefalse );
		throwsLuaError( "len", zero );
		throwsLuaError( "len", intint );
		throwsLuaError( "len", longdouble );
		throwsLuaError( "len", doubledouble );
		assertEquals( LuaInteger.valueOf(samplestringstring.length()), stringstring.len() );
		assertEquals( LuaInteger.valueOf(samplestringint.length()), stringint.len() );
		assertEquals( LuaInteger.valueOf(samplestringlong.length()), stringlong.len() );
		assertEquals( LuaInteger.valueOf(samplestringdouble.length()), stringdouble.len() );
		assertEquals( LuaInteger.valueOf(2), table.len() );
		throwsLuaError( "len", somefunc );
		throwsLuaError( "len", thread );
		throwsLuaError( "len", someclosure );
		throwsLuaError( "len", userdataobj );
		throwsLuaError( "len", userdatacls );
	}
	
	public void testLength() {
		throwsLuaError( "length", somenil );
		throwsLuaError( "length", sometrue );
		throwsLuaError( "length", somefalse );
		throwsLuaError( "length", zero );
		throwsLuaError( "length", intint );
		throwsLuaError( "length", longdouble );
		throwsLuaError( "length", doubledouble );
		assertEquals( samplestringstring.length(), stringstring.length() );
		assertEquals( samplestringint.length(), stringint.length() );
		assertEquals( samplestringlong.length(), stringlong.length() );
		assertEquals( samplestringdouble.length(), stringdouble.length() );
		assertEquals( 2, table.length() );
		throwsLuaError( "length", somefunc );
		throwsLuaError( "length", thread );
		throwsLuaError( "length", someclosure );
		throwsLuaError( "length", userdataobj );
		throwsLuaError( "length", userdatacls );
	}
	
	public void testGetfenv() {
		throwsLuaError( "getfenv", somenil );
		throwsLuaError( "getfenv", sometrue );
		throwsLuaError( "getfenv", somefalse );
		throwsLuaError( "getfenv", zero );
		throwsLuaError( "getfenv", intint );
		throwsLuaError( "getfenv", longdouble );
		throwsLuaError( "getfenv", doubledouble );
		throwsLuaError( "getfenv", stringstring );
		throwsLuaError( "getfenv", stringint );
		throwsLuaError( "getfenv", stringlong );
		throwsLuaError( "getfenv", stringdouble );
		throwsLuaError( "getfenv", table );
		assertTrue( table == thread.getfenv() );
		assertTrue( table == someclosure.getfenv() );
		assertTrue( table == somefunc.getfenv() );
		throwsLuaError( "getfenv", userdataobj );
		throwsLuaError( "getfenv", userdatacls );
	}
	
	public void testSetfenv() {
		LuaTable table2 = LuaValue.listOf( new LuaValue[] { 
				LuaValue.valueOf("ccc"), 
				LuaValue.valueOf("ddd") } );
		throwsLuaError( "setfenv", somenil, table2 );
		throwsLuaError( "setfenv", sometrue, table2 );
		throwsLuaError( "setfenv", somefalse, table2 );
		throwsLuaError( "setfenv", zero, table2 );
		throwsLuaError( "setfenv", intint, table2 );
		throwsLuaError( "setfenv", longdouble, table2 );
		throwsLuaError( "setfenv", doubledouble, table2 );
		throwsLuaError( "setfenv", stringstring, table2 );
		throwsLuaError( "setfenv", stringint, table2 );
		throwsLuaError( "setfenv", stringlong, table2 );
		throwsLuaError( "setfenv", stringdouble, table2 );
		throwsLuaError( "setfenv", table, table2 );
		thread.setfenv(table2);
		assertTrue( table2 == thread.getfenv() );
		assertTrue( table == someclosure.getfenv() );
		assertTrue( table == somefunc.getfenv() );
		someclosure.setfenv(table2);
		assertTrue( table2 == someclosure.getfenv() );
		assertTrue( table == somefunc.getfenv() );
		somefunc.setfenv(table2);
		assertTrue( table2 == somefunc.getfenv() );
		throwsLuaError( "setfenv", userdataobj, table2 );
		throwsLuaError( "setfenv", userdatacls, table2 );
	}

	public Prototype createPrototype( String script, String name ) {
		try {
			LuaTable _G = org.luaj.vm2.lib.JsePlatform.standardGlobals();
			InputStream is = new ByteArrayInputStream(script.getBytes("UTF-8"));
			return LuaC.compile(is, name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.toString());
			return null;
		}		
	}

	public void testFunctionClosureThreadEnv() {

		// set up suitable environments for execution
		LuaValue aaa = LuaValue.valueOf("aaa");
		LuaValue eee = LuaValue.valueOf("eee");
		LuaTable _G = org.luaj.vm2.lib.JsePlatform.standardGlobals();
		LuaTable newenv = LuaValue.tableOf( new LuaValue[] { 
				LuaValue.valueOf("a"), LuaValue.valueOf("aaa"), 
				LuaValue.valueOf("b"), LuaValue.valueOf("bbb"), } );
		LuaTable mt = LuaValue.tableOf( new LuaValue[] { LuaValue.INDEX, _G } );
		newenv.setmetatable(mt);
		_G.set("a", aaa);
		newenv.set("a", eee);

		// function tests
		{
			LuaFunction f = new ZeroArgFunction(_G) { public LuaValue call() { return env.get("a");}};
			assertEquals( aaa, f.call() );
			f.setfenv(newenv);
			assertEquals( newenv, f.getfenv() );
			assertEquals( eee, f.call() );
		}
		
		// closure tests
		{
			Prototype p = createPrototype( "return a\n", "closuretester" );
			LuaClosure c = new LuaClosure(p, _G);
			assertEquals( aaa, c.call() );
			c.setfenv(newenv);
			assertEquals( newenv, c.getfenv() );
			assertEquals( eee, c.call() );
		}

		// thread tests, functions created in threads inherit the thread's environment initially
		// those closures created not in any other function get the thread's enviroment
		Prototype p2 = createPrototype( "return loadstring('return a')", "threadtester" );
		{
			LuaThread t = new LuaThread(new LuaClosure(p2,_G), _G);
			Varargs v = t.resume(LuaValue.NONE);
			assertEquals(LuaValue.TRUE, v.arg(1) );
			LuaValue f = v.arg(2);
			assertEquals( LuaValue.TFUNCTION, f.type() );
			assertEquals( aaa, f.call() );
			assertEquals( _G, f.getfenv() );
		}
		{
			// change the thread environment after creation!
			LuaThread t = new LuaThread(new LuaClosure(p2,_G), _G);
			t.setfenv(newenv);
			Varargs v = t.resume(LuaValue.NONE);
			assertEquals(LuaValue.TRUE, v.arg(1) );
			LuaValue f = v.arg(2);
			assertEquals( LuaValue.TFUNCTION, f.type() );
			assertEquals( eee, f.call() );
			assertEquals( newenv, f.getfenv() );
		}
		{
			// let the closure have a different environment from the thread
			Prototype p3 = createPrototype( "return function() return a end", "envtester" );
			LuaThread t = new LuaThread(new LuaClosure(p3,newenv), _G);
			Varargs v = t.resume(LuaValue.NONE);
			assertEquals(LuaValue.TRUE, v.arg(1) );
			LuaValue f = v.arg(2);
			assertEquals( LuaValue.TFUNCTION, f.type() );
			assertEquals( eee, f.call() );
			assertEquals( newenv, f.getfenv() );
		}
	}
}
