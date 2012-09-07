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

import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

public class TypeTest extends TestCase {
	static {
		JsePlatform.debugGlobals();
	}
	
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
	private final LuaTable    table         = LuaValue.tableOf();
	private final LuaFunction somefunc      = new ZeroArgFunction() { public LuaValue call() { return NONE;}};
	private final LuaThread   thread        = new LuaThread(somefunc);
	private final LuaClosure  someclosure   = new LuaClosure(new Prototype());
	private final LuaUserdata userdataobj   = LuaValue.userdataOf(sampleobject);
	private final LuaUserdata userdatacls   = LuaValue.userdataOf(sampledata);
	
	public static final class MyData {
		public MyData() {			
		}
	}
	
	// ===================== type checks =======================
	
	public void testIsBoolean() {
		assertEquals( false, somenil.isboolean() );
		assertEquals( true, sometrue.isboolean() );
		assertEquals( true, somefalse.isboolean() );
		assertEquals( false, zero.isboolean() );
		assertEquals( false, intint.isboolean() );
		assertEquals( false, longdouble.isboolean() );
		assertEquals( false, doubledouble.isboolean() );
		assertEquals( false, stringstring.isboolean() );
		assertEquals( false, stringint.isboolean() );
		assertEquals( false, stringlong.isboolean() );
		assertEquals( false, stringdouble.isboolean() );
		assertEquals( false, thread.isboolean() );
		assertEquals( false, table.isboolean() );
		assertEquals( false, userdataobj.isboolean() );
		assertEquals( false, userdatacls.isboolean() );
		assertEquals( false, somefunc.isboolean() );
		assertEquals( false, someclosure.isboolean() );
	}
	
	public void testIsClosure() {
		assertEquals( false, somenil.isclosure() );
		assertEquals( false, sometrue.isclosure() );
		assertEquals( false, somefalse.isclosure() );
		assertEquals( false, zero.isclosure() );
		assertEquals( false, intint.isclosure() );
		assertEquals( false, longdouble.isclosure() );
		assertEquals( false, doubledouble.isclosure() );
		assertEquals( false, stringstring.isclosure() );
		assertEquals( false, stringint.isclosure() );
		assertEquals( false, stringlong.isclosure() );
		assertEquals( false, stringdouble.isclosure() );
		assertEquals( false, thread.isclosure() );
		assertEquals( false, table.isclosure() );
		assertEquals( false, userdataobj.isclosure() );
		assertEquals( false, userdatacls.isclosure() );
		assertEquals( false, somefunc.isclosure() );
		assertEquals( true, someclosure.isclosure() );
	}

	
	public void testIsFunction() {
		assertEquals( false, somenil.isfunction() );
		assertEquals( false, sometrue.isfunction() );
		assertEquals( false, somefalse.isfunction() );
		assertEquals( false, zero.isfunction() );
		assertEquals( false, intint.isfunction() );
		assertEquals( false, longdouble.isfunction() );
		assertEquals( false, doubledouble.isfunction() );
		assertEquals( false, stringstring.isfunction() );
		assertEquals( false, stringint.isfunction() );
		assertEquals( false, stringlong.isfunction() );
		assertEquals( false, stringdouble.isfunction() );
		assertEquals( false, thread.isfunction() );
		assertEquals( false, table.isfunction() );
		assertEquals( false, userdataobj.isfunction() );
		assertEquals( false, userdatacls.isfunction() );
		assertEquals( true, somefunc.isfunction() );
		assertEquals( true, someclosure.isfunction() );
	}

	
	public void testIsInt() {
		assertEquals( false, somenil.isint() );
		assertEquals( false, sometrue.isint() );
		assertEquals( false, somefalse.isint() );
		assertEquals( true, zero.isint() );
		assertEquals( true, intint.isint() );
		assertEquals( false, longdouble.isint() );
		assertEquals( false, doubledouble.isint() );
		assertEquals( false, stringstring.isint() );
		assertEquals( true, stringint.isint() );
		assertEquals( false, stringdouble.isint() );
		assertEquals( false, thread.isint() );
		assertEquals( false, table.isint() );
		assertEquals( false, userdataobj.isint() );
		assertEquals( false, userdatacls.isint() );
		assertEquals( false, somefunc.isint() );
		assertEquals( false, someclosure.isint() );
	}

	public void testIsIntType() {
		assertEquals( false, somenil.isinttype() );
		assertEquals( false, sometrue.isinttype() );
		assertEquals( false, somefalse.isinttype() );
		assertEquals( true, zero.isinttype() );
		assertEquals( true, intint.isinttype() );
		assertEquals( false, longdouble.isinttype() );
		assertEquals( false, doubledouble.isinttype() );
		assertEquals( false, stringstring.isinttype() );
		assertEquals( false, stringint.isinttype() );
		assertEquals( false, stringlong.isinttype() );
		assertEquals( false, stringdouble.isinttype() );
		assertEquals( false, thread.isinttype() );
		assertEquals( false, table.isinttype() );
		assertEquals( false, userdataobj.isinttype() );
		assertEquals( false, userdatacls.isinttype() );
		assertEquals( false, somefunc.isinttype() );
		assertEquals( false, someclosure.isinttype() );
	}

	public void testIsLong() {
		assertEquals( false, somenil.islong() );
		assertEquals( false, sometrue.islong() );
		assertEquals( false, somefalse.islong() );
		assertEquals( true, intint.isint() );
		assertEquals( true, longdouble.islong() );
		assertEquals( false, doubledouble.islong() );
		assertEquals( false, stringstring.islong() );
		assertEquals( true, stringint.islong() );
		assertEquals( true, stringlong.islong() );
		assertEquals( false, stringdouble.islong() );
		assertEquals( false, thread.islong() );
		assertEquals( false, table.islong() );
		assertEquals( false, userdataobj.islong() );
		assertEquals( false, userdatacls.islong() );
		assertEquals( false, somefunc.islong() );
		assertEquals( false, someclosure.islong() );
	}

	public void testIsNil() {
		assertEquals( true, somenil.isnil() );
		assertEquals( false, sometrue.isnil() );
		assertEquals( false, somefalse.isnil() );
		assertEquals( false, zero.isnil() );
		assertEquals( false, intint.isnil() );
		assertEquals( false, longdouble.isnil() );
		assertEquals( false, doubledouble.isnil() );
		assertEquals( false, stringstring.isnil() );
		assertEquals( false, stringint.isnil() );
		assertEquals( false, stringlong.isnil() );
		assertEquals( false, stringdouble.isnil() );
		assertEquals( false, thread.isnil() );
		assertEquals( false, table.isnil() );
		assertEquals( false, userdataobj.isnil() );
		assertEquals( false, userdatacls.isnil() );
		assertEquals( false, somefunc.isnil() );
		assertEquals( false, someclosure.isnil() );
	}

	public void testIsNumber() {
		assertEquals( false, somenil.isnumber() );
		assertEquals( false, sometrue.isnumber() );
		assertEquals( false, somefalse.isnumber() );
		assertEquals( true, zero.isnumber() );
		assertEquals( true, intint.isnumber() );
		assertEquals( true, longdouble.isnumber() );
		assertEquals( true, doubledouble.isnumber() );
		assertEquals( false, stringstring.isnumber() );
		assertEquals( true, stringint.isnumber() );
		assertEquals( true, stringlong.isnumber() );
		assertEquals( true, stringdouble.isnumber() );
		assertEquals( false, thread.isnumber() );
		assertEquals( false, table.isnumber() );
		assertEquals( false, userdataobj.isnumber() );
		assertEquals( false, userdatacls.isnumber() );
		assertEquals( false, somefunc.isnumber() );
		assertEquals( false, someclosure.isnumber() );
	}

	public void testIsString() {
		assertEquals( false, somenil.isstring() );
		assertEquals( false, sometrue.isstring() );
		assertEquals( false, somefalse.isstring() );
		assertEquals( true, zero.isstring() );
		assertEquals( true, longdouble.isstring() );
		assertEquals( true, doubledouble.isstring() );
		assertEquals( true, stringstring.isstring() );
		assertEquals( true, stringint.isstring() );
		assertEquals( true, stringlong.isstring() );
		assertEquals( true, stringdouble.isstring() );
		assertEquals( false, thread.isstring() );
		assertEquals( false, table.isstring() );
		assertEquals( false, userdataobj.isstring() );
		assertEquals( false, userdatacls.isstring() );
		assertEquals( false, somefunc.isstring() );
		assertEquals( false, someclosure.isstring() );
	}

	public void testIsThread() {
		assertEquals( false, somenil.isthread() );
		assertEquals( false, sometrue.isthread() );
		assertEquals( false, somefalse.isthread() );
		assertEquals( false, intint.isthread() );
		assertEquals( false, longdouble.isthread() );
		assertEquals( false, doubledouble.isthread() );
		assertEquals( false, stringstring.isthread() );
		assertEquals( false, stringint.isthread() );
		assertEquals( false, stringdouble.isthread() );
		assertEquals( true, thread.isthread() );
		assertEquals( false, table.isthread() );
		assertEquals( false, userdataobj.isthread() );
		assertEquals( false, userdatacls.isthread() );
		assertEquals( false, somefunc.isthread() );
		assertEquals( false, someclosure.isthread() );
	}

	public void testIsTable() {
		assertEquals( false, somenil.istable() );
		assertEquals( false, sometrue.istable() );
		assertEquals( false, somefalse.istable() );
		assertEquals( false, intint.istable() );
		assertEquals( false, longdouble.istable() );
		assertEquals( false, doubledouble.istable() );
		assertEquals( false, stringstring.istable() );
		assertEquals( false, stringint.istable() );
		assertEquals( false, stringdouble.istable() );
		assertEquals( false, thread.istable() );
		assertEquals( true, table.istable() );
		assertEquals( false, userdataobj.istable() );
		assertEquals( false, userdatacls.istable() );
		assertEquals( false, somefunc.istable() );
		assertEquals( false, someclosure.istable() );
	}

	public void testIsUserdata() {
		assertEquals( false, somenil.isuserdata() );
		assertEquals( false, sometrue.isuserdata() );
		assertEquals( false, somefalse.isuserdata() );
		assertEquals( false, intint.isuserdata() );
		assertEquals( false, longdouble.isuserdata() );
		assertEquals( false, doubledouble.isuserdata() );
		assertEquals( false, stringstring.isuserdata() );
		assertEquals( false, stringint.isuserdata() );
		assertEquals( false, stringdouble.isuserdata() );
		assertEquals( false, thread.isuserdata() );
		assertEquals( false, table.isuserdata() );
		assertEquals( true, userdataobj.isuserdata() );
		assertEquals( true, userdatacls.isuserdata() );
		assertEquals( false, somefunc.isuserdata() );
		assertEquals( false, someclosure.isuserdata() );
	}
	
	public void testIsUserdataObject() {
		assertEquals( false, somenil.isuserdata(Object.class) );
		assertEquals( false, sometrue.isuserdata(Object.class) );
		assertEquals( false, somefalse.isuserdata(Object.class) );
		assertEquals( false, longdouble.isuserdata(Object.class) );
		assertEquals( false, doubledouble.isuserdata(Object.class) );
		assertEquals( false, stringstring.isuserdata(Object.class) );
		assertEquals( false, stringint.isuserdata(Object.class) );
		assertEquals( false, stringdouble.isuserdata(Object.class) );
		assertEquals( false, thread.isuserdata(Object.class) );
		assertEquals( false, table.isuserdata(Object.class) );
		assertEquals( true, userdataobj.isuserdata(Object.class) );
		assertEquals( true, userdatacls.isuserdata(Object.class) );
		assertEquals( false, somefunc.isuserdata(Object.class) );
		assertEquals( false, someclosure.isuserdata(Object.class) );
	}
	
	public void testIsUserdataMyData() {
		assertEquals( false, somenil.isuserdata(MyData.class) );
		assertEquals( false, sometrue.isuserdata(MyData.class) );
		assertEquals( false, somefalse.isuserdata(MyData.class) );
		assertEquals( false, longdouble.isuserdata(MyData.class) );
		assertEquals( false, doubledouble.isuserdata(MyData.class) );
		assertEquals( false, stringstring.isuserdata(MyData.class) );
		assertEquals( false, stringint.isuserdata(MyData.class) );
		assertEquals( false, stringdouble.isuserdata(MyData.class) );
		assertEquals( false, thread.isuserdata(MyData.class) );
		assertEquals( false, table.isuserdata(MyData.class) );
		assertEquals( false, userdataobj.isuserdata(MyData.class) );
		assertEquals( true, userdatacls.isuserdata(MyData.class) );
		assertEquals( false, somefunc.isuserdata(MyData.class) );
		assertEquals( false, someclosure.isuserdata(MyData.class) );
	}
	
	
	// ===================== Coerce to Java =======================
	
	public void testToBoolean() {
		assertEquals( false, somenil.toboolean() );
		assertEquals( true, sometrue.toboolean() );
		assertEquals( false, somefalse.toboolean() );
		assertEquals( true, zero.toboolean() );
		assertEquals( true, intint.toboolean() );
		assertEquals( true, longdouble.toboolean() );
		assertEquals( true, doubledouble.toboolean() );
		assertEquals( true, stringstring.toboolean() );
		assertEquals( true, stringint.toboolean() );
		assertEquals( true, stringlong.toboolean() );
		assertEquals( true, stringdouble.toboolean() );
		assertEquals( true, thread.toboolean() );
		assertEquals( true, table.toboolean() );
		assertEquals( true, userdataobj.toboolean() );
		assertEquals( true, userdatacls.toboolean() );
		assertEquals( true, somefunc.toboolean() );
		assertEquals( true, someclosure.toboolean() );
	}
	
	public void testToByte() {
		assertEquals( (byte) 0, somenil.tobyte() );
		assertEquals( (byte) 0, somefalse.tobyte() );
		assertEquals( (byte) 0, sometrue.tobyte() );
		assertEquals( (byte) 0, zero.tobyte() );
		assertEquals( (byte) sampleint, intint.tobyte() );
		assertEquals( (byte) samplelong, longdouble.tobyte() );
		assertEquals( (byte) sampledouble, doubledouble.tobyte() );
		assertEquals( (byte) 0, stringstring.tobyte() );
		assertEquals( (byte) sampleint, stringint.tobyte() );
		assertEquals( (byte) samplelong, stringlong.tobyte() );
		assertEquals( (byte) sampledouble, stringdouble.tobyte() );
		assertEquals( (byte) 0, thread.tobyte() );
		assertEquals( (byte) 0, table.tobyte() );
		assertEquals( (byte) 0, userdataobj.tobyte() );
		assertEquals( (byte) 0, userdatacls.tobyte() );
		assertEquals( (byte) 0, somefunc.tobyte() );
		assertEquals( (byte) 0, someclosure.tobyte() );
	}
	
	public void testToChar() {
		assertEquals( (char) 0, somenil.tochar() );
		assertEquals( (char) 0, somefalse.tochar() );
		assertEquals( (char) 0, sometrue.tochar() );
		assertEquals( (char) 0, zero.tochar() );
		assertEquals( (int) (char) sampleint, (int) intint.tochar() );
		assertEquals( (int) (char) samplelong, (int) longdouble.tochar() );
		assertEquals( (int) (char) sampledouble, (int) doubledouble.tochar() );
		assertEquals( (char) 0, stringstring.tochar() );
		assertEquals( (int) (char) sampleint, (int) stringint.tochar() );
		assertEquals( (int) (char) samplelong, (int) stringlong.tochar() );
		assertEquals( (int) (char) sampledouble, (int) stringdouble.tochar() );
		assertEquals( (char) 0, thread.tochar() );
		assertEquals( (char) 0, table.tochar() );
		assertEquals( (char) 0, userdataobj.tochar() );
		assertEquals( (char) 0, userdatacls.tochar() );
		assertEquals( (char) 0, somefunc.tochar() );
		assertEquals( (char) 0, someclosure.tochar() );
	}
	
	public void testToDouble() {
		assertEquals( 0., somenil.todouble() );
		assertEquals( 0., somefalse.todouble() );
		assertEquals( 0., sometrue.todouble() );
		assertEquals( 0., zero.todouble() );
		assertEquals( (double) sampleint, intint.todouble() );
		assertEquals( (double) samplelong, longdouble.todouble() );
		assertEquals( (double) sampledouble, doubledouble.todouble() );
		assertEquals( (double) 0, stringstring.todouble() );
		assertEquals( (double) sampleint, stringint.todouble() );
		assertEquals( (double) samplelong, stringlong.todouble() );
		assertEquals( (double) sampledouble, stringdouble.todouble() );
		assertEquals( 0., thread.todouble() );
		assertEquals( 0., table.todouble() );
		assertEquals( 0., userdataobj.todouble() );
		assertEquals( 0., userdatacls.todouble() );
		assertEquals( 0., somefunc.todouble() );
		assertEquals( 0., someclosure.todouble() );
	}
	
	public void testToFloat() {
		assertEquals( 0.f, somenil.tofloat() );
		assertEquals( 0.f, somefalse.tofloat() );
		assertEquals( 0.f, sometrue.tofloat() );
		assertEquals( 0.f, zero.tofloat() );
		assertEquals( (float) sampleint, intint.tofloat() );
		assertEquals( (float) samplelong, longdouble.tofloat() );
		assertEquals( (float) sampledouble, doubledouble.tofloat() );
		assertEquals( (float) 0, stringstring.tofloat() );
		assertEquals( (float) sampleint, stringint.tofloat() );
		assertEquals( (float) samplelong, stringlong.tofloat() );
		assertEquals( (float) sampledouble, stringdouble.tofloat() );
		assertEquals( 0.f, thread.tofloat() );
		assertEquals( 0.f, table.tofloat() );
		assertEquals( 0.f, userdataobj.tofloat() );
		assertEquals( 0.f, userdatacls.tofloat() );
		assertEquals( 0.f, somefunc.tofloat() );
		assertEquals( 0.f, someclosure.tofloat() );
	}
	
	public void testToInt() {
		assertEquals( 0, somenil.toint() );
		assertEquals( 0, somefalse.toint() );
		assertEquals( 0, sometrue.toint() );
		assertEquals( 0, zero.toint() );
		assertEquals( (int) sampleint, intint.toint() );
		assertEquals( (int) samplelong, longdouble.toint() );
		assertEquals( (int) sampledouble, doubledouble.toint() );
		assertEquals( (int) 0, stringstring.toint() );
		assertEquals( (int) sampleint, stringint.toint() );
		assertEquals( (int) samplelong, stringlong.toint() );
		assertEquals( (int) sampledouble, stringdouble.toint() );
		assertEquals( 0, thread.toint() );
		assertEquals( 0, table.toint() );
		assertEquals( 0, userdataobj.toint() );
		assertEquals( 0, userdatacls.toint() );
		assertEquals( 0, somefunc.toint() );
		assertEquals( 0, someclosure.toint() );
	}
	
	public void testToLong() {
		assertEquals( 0L, somenil.tolong() );
		assertEquals( 0L, somefalse.tolong() );
		assertEquals( 0L, sometrue.tolong() );
		assertEquals( 0L, zero.tolong() );
		assertEquals( (long) sampleint, intint.tolong() );
		assertEquals( (long) samplelong, longdouble.tolong() );
		assertEquals( (long) sampledouble, doubledouble.tolong() );
		assertEquals( (long) 0, stringstring.tolong() );
		assertEquals( (long) sampleint, stringint.tolong() );
		assertEquals( (long) samplelong, stringlong.tolong() );
		assertEquals( (long) sampledouble, stringdouble.tolong() );
		assertEquals( 0L, thread.tolong() );
		assertEquals( 0L, table.tolong() );
		assertEquals( 0L, userdataobj.tolong() );
		assertEquals( 0L, userdatacls.tolong() );
		assertEquals( 0L, somefunc.tolong() );
		assertEquals( 0L, someclosure.tolong() );
	}
	
	public void testToShort() {
		assertEquals( (short) 0, somenil.toshort() );
		assertEquals( (short) 0, somefalse.toshort() );
		assertEquals( (short) 0, sometrue.toshort() );
		assertEquals( (short) 0, zero.toshort() );
		assertEquals( (short) sampleint, intint.toshort() );
		assertEquals( (short) samplelong, longdouble.toshort() );
		assertEquals( (short) sampledouble, doubledouble.toshort() );
		assertEquals( (short) 0, stringstring.toshort() );
		assertEquals( (short) sampleint, stringint.toshort() );
		assertEquals( (short) samplelong, stringlong.toshort() );
		assertEquals( (short) sampledouble, stringdouble.toshort() );
		assertEquals( (short) 0, thread.toshort() );
		assertEquals( (short) 0, table.toshort() );
		assertEquals( (short) 0, userdataobj.toshort() );
		assertEquals( (short) 0, userdatacls.toshort() );
		assertEquals( (short) 0, somefunc.toshort() );
		assertEquals( (short) 0, someclosure.toshort() );
	}
	
	public void testToString() {
		assertEquals( "nil", somenil.tojstring() );
		assertEquals( "false", somefalse.tojstring() );
		assertEquals( "true", sometrue.tojstring() );
		assertEquals( "0", zero.tojstring() );
		assertEquals( String.valueOf(sampleint), intint.tojstring() );
		assertEquals( String.valueOf(samplelong), longdouble.tojstring() );
		assertEquals( String.valueOf(sampledouble), doubledouble.tojstring() );
		assertEquals( samplestringstring, stringstring.tojstring() );
		assertEquals( String.valueOf(sampleint), stringint.tojstring() );
		assertEquals( String.valueOf(samplelong), stringlong.tojstring() );
		assertEquals( String.valueOf(sampledouble), stringdouble.tojstring() );
		assertEquals( "thread: ", thread.tojstring().substring(0,8) );
		assertEquals( "table: ", table.tojstring().substring(0,7) );
		assertEquals( sampleobject.toString(), userdataobj.tojstring() );
		assertEquals( sampledata.toString(), userdatacls.tojstring() );
		assertEquals( "function: ", somefunc.tojstring().substring(0,10) );
		assertEquals( "function: ", someclosure.tojstring().substring(0,10) );
	}
	
	public void testToUserdata() {
		assertEquals( null, somenil.touserdata() );
		assertEquals( null, somefalse.touserdata() );
		assertEquals( null, sometrue.touserdata() );
		assertEquals( null, zero.touserdata() );
		assertEquals( null, intint.touserdata() );
		assertEquals( null, longdouble.touserdata() );
		assertEquals( null, doubledouble.touserdata() );
		assertEquals( null, stringstring.touserdata() );
		assertEquals( null, stringint.touserdata() );
		assertEquals( null, stringlong.touserdata() );
		assertEquals( null, stringdouble.touserdata() );
		assertEquals( null, thread.touserdata() );
		assertEquals( null, table.touserdata() );
		assertEquals( sampleobject, userdataobj.touserdata() );
		assertEquals( sampledata, userdatacls.touserdata() );
		assertEquals( null, somefunc.touserdata() );
		assertEquals( null, someclosure.touserdata() );
	}

	
	
	// ===================== Optional argument conversion =======================


	private void throwsError(LuaValue obj, String method, Class argtype, Object argument ) {
		try {
			obj.getClass().getMethod(method,argtype).invoke(obj, argument );
		} catch (InvocationTargetException e) {
			if ( ! (e.getTargetException() instanceof LuaError) )
				fail("not a LuaError: "+e.getTargetException());
			return; // pass
		} catch ( Exception e ) {
			fail( "bad exception: "+e );
		}
		fail("failed to throw LuaError as required");
	}

	public void testOptBoolean() {
		assertEquals( true, somenil.optboolean(true) );
		assertEquals( false, somenil.optboolean(false) );
		assertEquals( true, sometrue.optboolean(false) );
		assertEquals( false, somefalse.optboolean(true) );
		throwsError( zero, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( intint, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( longdouble, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( doubledouble, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( somefunc, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( someclosure, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( stringstring, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( stringint, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( stringlong, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( stringdouble, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( thread, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( table, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( userdataobj, "optboolean", boolean.class, Boolean.FALSE );
		throwsError( userdatacls, "optboolean", boolean.class, Boolean.FALSE );
	}

	public void testOptClosure() {
		assertEquals( someclosure, somenil.optclosure(someclosure) );
		assertEquals( null, somenil.optclosure(null) );
		throwsError( sometrue, "optclosure", LuaClosure.class, someclosure );
		throwsError( somefalse, "optclosure", LuaClosure.class, someclosure );
		throwsError( zero, "optclosure", LuaClosure.class, someclosure );
		throwsError( intint, "optclosure", LuaClosure.class, someclosure );
		throwsError( longdouble, "optclosure", LuaClosure.class, someclosure );
		throwsError( doubledouble, "optclosure", LuaClosure.class, someclosure );
		throwsError( somefunc, "optclosure", LuaClosure.class, someclosure );
		assertEquals( someclosure, someclosure.optclosure(someclosure) );
		assertEquals( someclosure, someclosure.optclosure(null) );
		throwsError( stringstring, "optclosure", LuaClosure.class, someclosure );
		throwsError( stringint, "optclosure", LuaClosure.class, someclosure );
		throwsError( stringlong, "optclosure", LuaClosure.class, someclosure );
		throwsError( stringdouble, "optclosure", LuaClosure.class, someclosure );
		throwsError( thread, "optclosure", LuaClosure.class, someclosure );
		throwsError( table, "optclosure", LuaClosure.class, someclosure );
		throwsError( userdataobj, "optclosure", LuaClosure.class, someclosure );
		throwsError( userdatacls, "optclosure", LuaClosure.class, someclosure );
	}

	public void testOptDouble() {
		assertEquals( 33., somenil.optdouble(33.) );
		throwsError( sometrue, "optdouble", double.class, 33. );
		throwsError( somefalse, "optdouble", double.class, 33. );
		assertEquals( 0., zero.optdouble(33.) );
		assertEquals( (double) sampleint, intint.optdouble(33.) );
		assertEquals( (double) samplelong, longdouble.optdouble(33.) );
		assertEquals( sampledouble, doubledouble.optdouble(33.) );
		throwsError( somefunc, "optdouble", double.class, 33. );
		throwsError( someclosure, "optdouble", double.class, 33. );
		throwsError( stringstring, "optdouble", double.class, 33. );
		assertEquals( (double) sampleint, stringint.optdouble(33.) );
		assertEquals( (double) samplelong, stringlong.optdouble(33.) );
		assertEquals( sampledouble, stringdouble.optdouble(33.) );
		throwsError( thread, "optdouble", double.class, 33. );
		throwsError( table, "optdouble", double.class, 33. );
		throwsError( userdataobj, "optdouble", double.class, 33. );
		throwsError( userdatacls, "optdouble", double.class, 33. );
	}

	public void testOptFunction() {
		assertEquals( somefunc, somenil.optfunction(somefunc) );
		assertEquals( null, somenil.optfunction(null) );
		throwsError( sometrue, "optfunction", LuaFunction.class, somefunc );
		throwsError( somefalse, "optfunction", LuaFunction.class, somefunc );
		throwsError( zero, "optfunction", LuaFunction.class, somefunc );
		throwsError( intint, "optfunction", LuaFunction.class, somefunc );
		throwsError( longdouble, "optfunction", LuaFunction.class, somefunc );
		throwsError( doubledouble, "optfunction", LuaFunction.class, somefunc );
		assertEquals( somefunc, somefunc.optfunction(null) );
		assertEquals( someclosure, someclosure.optfunction(null) );
		assertEquals( somefunc, somefunc.optfunction(somefunc) );
		assertEquals( someclosure, someclosure.optfunction(somefunc) );
		throwsError( stringstring, "optfunction", LuaFunction.class, somefunc );
		throwsError( stringint, "optfunction", LuaFunction.class, somefunc );
		throwsError( stringlong, "optfunction", LuaFunction.class, somefunc );
		throwsError( stringdouble, "optfunction", LuaFunction.class, somefunc );
		throwsError( thread, "optfunction", LuaFunction.class, somefunc );
		throwsError( table, "optfunction", LuaFunction.class, somefunc );
		throwsError( userdataobj, "optfunction", LuaFunction.class, somefunc );
		throwsError( userdatacls, "optfunction", LuaFunction.class, somefunc );
	}

	public void testOptInt() {
		assertEquals( 33, somenil.optint(33) );
		throwsError( sometrue, "optint", int.class, new Integer(33) );
		throwsError( somefalse, "optint", int.class, new Integer(33) );
		assertEquals( 0, zero.optint(33) );
		assertEquals( sampleint, intint.optint(33) );
		assertEquals( (int) samplelong, longdouble.optint(33) );
		assertEquals( (int) sampledouble, doubledouble.optint(33) );
		throwsError( somefunc, "optint", int.class, new Integer(33) );
		throwsError( someclosure, "optint", int.class, new Integer(33) );
		throwsError( stringstring, "optint", int.class, new Integer(33) );
		assertEquals( sampleint, stringint.optint(33) );
		assertEquals( (int) samplelong, stringlong.optint(33) );
		assertEquals( (int) sampledouble, stringdouble.optint(33) );
		throwsError( thread, "optint", int.class, new Integer(33) );
		throwsError( table, "optint", int.class, new Integer(33) );
		throwsError( userdataobj, "optint", int.class, new Integer(33) );
		throwsError( userdatacls, "optint", int.class, new Integer(33) );
	}
	
	public void testOptInteger() {
		assertEquals( LuaValue.valueOf(33), somenil.optinteger(LuaValue.valueOf(33)) );
		throwsError( sometrue, "optinteger", LuaInteger.class, LuaValue.valueOf(33) );
		throwsError( somefalse, "optinteger", LuaInteger.class, LuaValue.valueOf(33) );
		assertEquals( zero, zero.optinteger(LuaValue.valueOf(33)) );
		assertEquals( LuaValue.valueOf( sampleint ), intint.optinteger(LuaValue.valueOf(33)) );
		assertEquals( LuaValue.valueOf( (int) samplelong ), longdouble.optinteger(LuaValue.valueOf(33)) );
		assertEquals( LuaValue.valueOf( (int) sampledouble ), doubledouble.optinteger(LuaValue.valueOf(33)) );
		throwsError( somefunc, "optinteger", LuaInteger.class, LuaValue.valueOf(33) );
		throwsError( someclosure, "optinteger", LuaInteger.class, LuaValue.valueOf(33) );
		throwsError( stringstring, "optinteger", LuaInteger.class, LuaValue.valueOf(33) );
		assertEquals( LuaValue.valueOf( sampleint), stringint.optinteger(LuaValue.valueOf(33)) );
		assertEquals( LuaValue.valueOf( (int) samplelong), stringlong.optinteger(LuaValue.valueOf(33)) );
		assertEquals( LuaValue.valueOf( (int) sampledouble), stringdouble.optinteger(LuaValue.valueOf(33)) );
		throwsError( thread, "optinteger", LuaInteger.class, LuaValue.valueOf(33) );
		throwsError( table, "optinteger", LuaInteger.class, LuaValue.valueOf(33) );
		throwsError( userdataobj, "optinteger", LuaInteger.class, LuaValue.valueOf(33) );
		throwsError( userdatacls, "optinteger", LuaInteger.class, LuaValue.valueOf(33) );
	}

	public void testOptLong() {
		assertEquals( 33L, somenil.optlong(33) );
		throwsError( sometrue, "optlong", long.class, new Long(33) );
		throwsError( somefalse, "optlong", long.class, new Long(33) );
		assertEquals( 0L, zero.optlong(33) );
		assertEquals( sampleint, intint.optlong(33) );
		assertEquals( (long) samplelong, longdouble.optlong(33) );
		assertEquals( (long) sampledouble, doubledouble.optlong(33) );
		throwsError( somefunc, "optlong", long.class, new Long(33) );
		throwsError( someclosure, "optlong", long.class, new Long(33) );
		throwsError( stringstring, "optlong", long.class, new Long(33) );
		assertEquals( sampleint, stringint.optlong(33) );
		assertEquals( (long) samplelong, stringlong.optlong(33) );
		assertEquals( (long) sampledouble, stringdouble.optlong(33) );
		throwsError( thread, "optlong", long.class, new Long(33) );
		throwsError( table, "optlong", long.class, new Long(33) );
		throwsError( userdataobj, "optlong", long.class, new Long(33) );
		throwsError( userdatacls, "optlong", long.class, new Long(33) );
	}
	
	public void testOptNumber() {
		assertEquals( LuaValue.valueOf(33), somenil.optnumber(LuaValue.valueOf(33)) );
		throwsError( sometrue, "optnumber", LuaNumber.class, LuaValue.valueOf(33) );
		throwsError( somefalse, "optnumber", LuaNumber.class, LuaValue.valueOf(33) );
		assertEquals( zero, zero.optnumber(LuaValue.valueOf(33)) );
		assertEquals( LuaValue.valueOf( sampleint ), intint.optnumber(LuaValue.valueOf(33)) );
		assertEquals( LuaValue.valueOf( samplelong ), longdouble.optnumber(LuaValue.valueOf(33)) );
		assertEquals( LuaValue.valueOf( sampledouble ), doubledouble.optnumber(LuaValue.valueOf(33)) );
		throwsError( somefunc, "optnumber", LuaNumber.class, LuaValue.valueOf(33) );
		throwsError( someclosure, "optnumber", LuaNumber.class, LuaValue.valueOf(33) );
		throwsError( stringstring, "optnumber", LuaNumber.class, LuaValue.valueOf(33) );
		assertEquals( LuaValue.valueOf( sampleint), stringint.optnumber(LuaValue.valueOf(33)) );
		assertEquals( LuaValue.valueOf( samplelong), stringlong.optnumber(LuaValue.valueOf(33)) );
		assertEquals( LuaValue.valueOf( sampledouble), stringdouble.optnumber(LuaValue.valueOf(33)) );
		throwsError( thread, "optnumber", LuaNumber.class, LuaValue.valueOf(33) );
		throwsError( table, "optnumber", LuaNumber.class, LuaValue.valueOf(33) );
		throwsError( userdataobj, "optnumber", LuaNumber.class, LuaValue.valueOf(33) );
		throwsError( userdatacls, "optnumber", LuaNumber.class, LuaValue.valueOf(33) );
	}
	
	public void testOptTable() {
		assertEquals( table, somenil.opttable(table) );
		assertEquals( null, somenil.opttable(null) );
		throwsError( sometrue, "opttable", LuaTable.class, table );
		throwsError( somefalse, "opttable", LuaTable.class, table );
		throwsError( zero, "opttable", LuaTable.class, table );
		throwsError( intint, "opttable", LuaTable.class, table );
		throwsError( longdouble, "opttable", LuaTable.class, table );
		throwsError( doubledouble, "opttable", LuaTable.class, table );
		throwsError( somefunc, "opttable", LuaTable.class, table );
		throwsError( someclosure, "opttable", LuaTable.class, table );
		throwsError( stringstring, "opttable", LuaTable.class, table );
		throwsError( stringint, "opttable", LuaTable.class, table );
		throwsError( stringlong, "opttable", LuaTable.class, table );
		throwsError( stringdouble, "opttable", LuaTable.class, table );
		throwsError( thread, "opttable", LuaTable.class, table );
		assertEquals( table, table.opttable(table) );
		assertEquals( table, table.opttable(null) );
		throwsError( userdataobj, "opttable", LuaTable.class, table );
		throwsError( userdatacls, "opttable", LuaTable.class, table );
	}
	
	public void testOptThread() {
		assertEquals( thread, somenil.optthread(thread) );
		assertEquals( null, somenil.optthread(null) );
		throwsError( sometrue, "optthread", LuaThread.class, thread );
		throwsError( somefalse, "optthread", LuaThread.class, thread );
		throwsError( zero, "optthread", LuaThread.class, thread );
		throwsError( intint, "optthread", LuaThread.class, thread );
		throwsError( longdouble, "optthread", LuaThread.class, thread );
		throwsError( doubledouble, "optthread", LuaThread.class, thread );
		throwsError( somefunc, "optthread", LuaThread.class, thread );
		throwsError( someclosure, "optthread", LuaThread.class, thread );
		throwsError( stringstring, "optthread", LuaThread.class, thread );
		throwsError( stringint, "optthread", LuaThread.class, thread );
		throwsError( stringlong, "optthread", LuaThread.class, thread );
		throwsError( stringdouble, "optthread", LuaThread.class, thread );
		throwsError( table, "optthread", LuaThread.class, thread );
		assertEquals( thread, thread.optthread(thread) );
		assertEquals( thread, thread.optthread(null) );
		throwsError( userdataobj, "optthread", LuaThread.class, thread );
		throwsError( userdatacls, "optthread", LuaThread.class, thread );
	}
	
	public void testOptJavaString() {
		assertEquals( "xyz", somenil.optjstring("xyz") );
		assertEquals( null, somenil.optjstring(null) );
		throwsError( sometrue, "optjstring", String.class, "xyz" );
		throwsError( somefalse, "optjstring", String.class, "xyz" );
		assertEquals( String.valueOf(zero), zero.optjstring("xyz") );
		assertEquals( String.valueOf(intint), intint.optjstring("xyz") );
		assertEquals( String.valueOf(longdouble), longdouble.optjstring("xyz") );
		assertEquals( String.valueOf(doubledouble), doubledouble.optjstring("xyz") );
		throwsError( somefunc, "optjstring", String.class, "xyz" );
		throwsError( someclosure, "optjstring", String.class, "xyz" );
		assertEquals( samplestringstring, stringstring.optjstring("xyz") );
		assertEquals( samplestringint, stringint.optjstring("xyz") );
		assertEquals( samplestringlong, stringlong.optjstring("xyz") );
		assertEquals( samplestringdouble, stringdouble.optjstring("xyz") );
		throwsError( thread, "optjstring", String.class, "xyz" );
		throwsError( table, "optjstring", String.class, "xyz" );
		throwsError( userdataobj, "optjstring", String.class, "xyz" );
		throwsError( userdatacls, "optjstring", String.class, "xyz" );
	}
	
	public void testOptLuaString() {
		assertEquals( LuaValue.valueOf("xyz"), somenil.optstring(LuaValue.valueOf("xyz")) );
		assertEquals( null, somenil.optstring(null) );
		throwsError( sometrue, "optstring", LuaString.class, LuaValue.valueOf("xyz") );
		throwsError( somefalse, "optstring", LuaString.class, LuaValue.valueOf("xyz") );
		assertEquals( LuaValue.valueOf("0"), zero.optstring(LuaValue.valueOf("xyz")) );
		assertEquals( stringint, intint.optstring(LuaValue.valueOf("xyz")) );
		assertEquals( stringlong, longdouble.optstring(LuaValue.valueOf("xyz")) );
		assertEquals( stringdouble, doubledouble.optstring(LuaValue.valueOf("xyz")) );
		throwsError( somefunc, "optstring", LuaString.class, LuaValue.valueOf("xyz") );
		throwsError( someclosure, "optstring", LuaString.class, LuaValue.valueOf("xyz") );
		assertEquals( stringstring, stringstring.optstring(LuaValue.valueOf("xyz")) );
		assertEquals( stringint, stringint.optstring(LuaValue.valueOf("xyz")) );
		assertEquals( stringlong, stringlong.optstring(LuaValue.valueOf("xyz")) );
		assertEquals( stringdouble, stringdouble.optstring(LuaValue.valueOf("xyz")) );
		throwsError( thread, "optstring", LuaString.class, LuaValue.valueOf("xyz") );
		throwsError( table, "optstring", LuaString.class, LuaValue.valueOf("xyz") );
		throwsError( userdataobj, "optstring", LuaString.class, LuaValue.valueOf("xyz") );
		throwsError( userdatacls, "optstring", LuaString.class, LuaValue.valueOf("xyz") );
	}
	
	public void testOptUserdata() {
		assertEquals( sampleobject, somenil.optuserdata(sampleobject) );
		assertEquals( sampledata, somenil.optuserdata(sampledata) );
		assertEquals( null, somenil.optuserdata(null) );
		throwsError( sometrue, "optuserdata", Object.class, sampledata );
		throwsError( somefalse, "optuserdata", Object.class, sampledata );
		throwsError( zero, "optuserdata", Object.class, sampledata );
		throwsError( intint, "optuserdata", Object.class, sampledata );
		throwsError( longdouble, "optuserdata", Object.class, sampledata );
		throwsError( doubledouble, "optuserdata", Object.class, sampledata );
		throwsError( somefunc, "optuserdata", Object.class, sampledata );
		throwsError( someclosure, "optuserdata", Object.class, sampledata );
		throwsError( stringstring, "optuserdata", Object.class, sampledata );
		throwsError( stringint, "optuserdata", Object.class, sampledata );
		throwsError( stringlong, "optuserdata", Object.class, sampledata );
		throwsError( stringdouble, "optuserdata", Object.class, sampledata );
		throwsError( table, "optuserdata", Object.class, sampledata );
		assertEquals( sampleobject, userdataobj.optuserdata(sampledata) );
		assertEquals( sampleobject, userdataobj.optuserdata(null) );
		assertEquals( sampledata, userdatacls.optuserdata(sampleobject) );
		assertEquals( sampledata, userdatacls.optuserdata(null) );
	}

	private void throwsErrorOptUserdataClass(LuaValue obj, Class arg1, Object arg2 ) {
		try {
			obj.getClass().getMethod("optuserdata", Class.class, Object.class ).invoke(obj, arg1, arg2);
		} catch (InvocationTargetException e) {
			if ( ! (e.getTargetException() instanceof LuaError) )
				fail("not a LuaError: "+e.getTargetException());
			return; // pass
		} catch ( Exception e ) {
			fail( "bad exception: "+e );
		}
		fail("failed to throw LuaError as required");
	}
	
	public void testOptUserdataClass() {
		assertEquals( sampledata, somenil.optuserdata(MyData.class, sampledata) );
		assertEquals( sampleobject, somenil.optuserdata(Object.class, sampleobject) );
		assertEquals( null, somenil.optuserdata(null) );
		throwsErrorOptUserdataClass( sometrue,  Object.class, sampledata );
		throwsErrorOptUserdataClass( zero,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( intint,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( longdouble,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( somefunc,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( someclosure,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( stringstring,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( stringint,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( stringlong,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( stringlong,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( stringdouble,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( table,  MyData.class, sampledata);
		throwsErrorOptUserdataClass( thread,  MyData.class, sampledata);
		assertEquals( sampleobject, userdataobj.optuserdata(Object.class, sampleobject) );
		assertEquals( sampleobject, userdataobj.optuserdata(null) );
		assertEquals( sampledata, userdatacls.optuserdata(MyData.class, sampledata) );
		assertEquals( sampledata, userdatacls.optuserdata(Object.class, sampleobject) );
		assertEquals( sampledata, userdatacls.optuserdata(null) );
		// should fail due to wrong class
		try {
			Object o = userdataobj.optuserdata(MyData.class, sampledata);
			fail( "did not throw bad type error" );
			assertTrue( o instanceof MyData );
		} catch ( LuaError le ) {
			assertEquals( "org.luaj.vm2.TypeTest$MyData expected, got userdata", le.getMessage() );
		}
	}
	
	public void testOptValue() {
		assertEquals( zero, somenil.optvalue(zero) );
		assertEquals( stringstring, somenil.optvalue(stringstring) );
		assertEquals( sometrue, sometrue.optvalue(LuaValue.TRUE) );
		assertEquals( somefalse, somefalse.optvalue(LuaValue.TRUE) );
		assertEquals( zero, zero.optvalue(LuaValue.TRUE) );
		assertEquals( intint, intint.optvalue(LuaValue.TRUE) );
		assertEquals( longdouble, longdouble.optvalue(LuaValue.TRUE) );
		assertEquals( somefunc, somefunc.optvalue(LuaValue.TRUE) );
		assertEquals( someclosure, someclosure.optvalue(LuaValue.TRUE) );
		assertEquals( stringstring, stringstring.optvalue(LuaValue.TRUE) );
		assertEquals( stringint, stringint.optvalue(LuaValue.TRUE) );
		assertEquals( stringlong, stringlong.optvalue(LuaValue.TRUE) );
		assertEquals( stringdouble, stringdouble.optvalue(LuaValue.TRUE) );
		assertEquals( thread, thread.optvalue(LuaValue.TRUE) );
		assertEquals( table, table.optvalue(LuaValue.TRUE) );
		assertEquals( userdataobj, userdataobj.optvalue(LuaValue.TRUE) );
		assertEquals( userdatacls, userdatacls.optvalue(LuaValue.TRUE) );
	}
	
	
	
	// ===================== Required argument conversion =======================


	private void throwsErrorReq(LuaValue obj, String method ) {
		try {
			obj.getClass().getMethod(method).invoke(obj);
		} catch (InvocationTargetException e) {
			if ( ! (e.getTargetException() instanceof LuaError) )
				fail("not a LuaError: "+e.getTargetException());
			return; // pass
		} catch ( Exception e ) {
			fail( "bad exception: "+e );
		}
		fail("failed to throw LuaError as required");
	}

	public void testCheckBoolean() {
		throwsErrorReq( somenil, "checkboolean" );
		assertEquals( true, sometrue.checkboolean() );
		assertEquals( false, somefalse.checkboolean() );
		throwsErrorReq( zero, "checkboolean" );
		throwsErrorReq( intint, "checkboolean" );
		throwsErrorReq( longdouble, "checkboolean" );
		throwsErrorReq( doubledouble, "checkboolean" );
		throwsErrorReq( somefunc, "checkboolean" );
		throwsErrorReq( someclosure, "checkboolean" );
		throwsErrorReq( stringstring, "checkboolean" );
		throwsErrorReq( stringint, "checkboolean" );
		throwsErrorReq( stringlong, "checkboolean" );
		throwsErrorReq( stringdouble, "checkboolean" );
		throwsErrorReq( thread, "checkboolean" );
		throwsErrorReq( table, "checkboolean" );
		throwsErrorReq( userdataobj, "checkboolean" );
		throwsErrorReq( userdatacls, "checkboolean" );
	}

	public void testCheckClosure() {
		throwsErrorReq( somenil, "checkclosure" );
		throwsErrorReq( sometrue, "checkclosure" );
		throwsErrorReq( somefalse, "checkclosure" );
		throwsErrorReq( zero, "checkclosure" );
		throwsErrorReq( intint, "checkclosure" );
		throwsErrorReq( longdouble, "checkclosure" );
		throwsErrorReq( doubledouble, "checkclosure" );
		throwsErrorReq( somefunc, "checkclosure" );
		assertEquals( someclosure, someclosure.checkclosure() );
		assertEquals( someclosure, someclosure.checkclosure() );
		throwsErrorReq( stringstring, "checkclosure" );
		throwsErrorReq( stringint, "checkclosure" );
		throwsErrorReq( stringlong, "checkclosure" );
		throwsErrorReq( stringdouble, "checkclosure" );
		throwsErrorReq( thread, "checkclosure" );
		throwsErrorReq( table, "checkclosure" );
		throwsErrorReq( userdataobj, "checkclosure" );
		throwsErrorReq( userdatacls, "checkclosure" );
	}

	public void testCheckDouble() {
		throwsErrorReq( somenil, "checkdouble" );
		throwsErrorReq( sometrue, "checkdouble" );
		throwsErrorReq( somefalse, "checkdouble" );
		assertEquals( 0., zero.checkdouble() );
		assertEquals( (double) sampleint, intint.checkdouble() );
		assertEquals( (double) samplelong, longdouble.checkdouble() );
		assertEquals( sampledouble, doubledouble.checkdouble() );
		throwsErrorReq( somefunc, "checkdouble" );
		throwsErrorReq( someclosure, "checkdouble" );
		throwsErrorReq( stringstring, "checkdouble" );
		assertEquals( (double) sampleint, stringint.checkdouble() );
		assertEquals( (double) samplelong, stringlong.checkdouble() );
		assertEquals( sampledouble, stringdouble.checkdouble() );
		throwsErrorReq( thread, "checkdouble" );
		throwsErrorReq( table, "checkdouble" );
		throwsErrorReq( userdataobj, "checkdouble" );
		throwsErrorReq( userdatacls, "checkdouble" );
	}

	public void testCheckFunction() {
		throwsErrorReq( somenil, "checkfunction" );
		throwsErrorReq( sometrue, "checkfunction" );
		throwsErrorReq( somefalse, "checkfunction" );
		throwsErrorReq( zero, "checkfunction" );
		throwsErrorReq( intint, "checkfunction" );
		throwsErrorReq( longdouble, "checkfunction" );
		throwsErrorReq( doubledouble, "checkfunction" );
		assertEquals( somefunc, somefunc.checkfunction() );
		assertEquals( someclosure, someclosure.checkfunction() );
		assertEquals( somefunc, somefunc.checkfunction() );
		assertEquals( someclosure, someclosure.checkfunction() );
		throwsErrorReq( stringstring, "checkfunction" );
		throwsErrorReq( stringint, "checkfunction" );
		throwsErrorReq( stringlong, "checkfunction" );
		throwsErrorReq( stringdouble, "checkfunction" );
		throwsErrorReq( thread, "checkfunction" );
		throwsErrorReq( table, "checkfunction" );
		throwsErrorReq( userdataobj, "checkfunction" );
		throwsErrorReq( userdatacls, "checkfunction" );
	}

	public void testCheckInt() {
		throwsErrorReq( somenil, "checkint" );
		throwsErrorReq( sometrue, "checkint" );
		throwsErrorReq( somefalse, "checkint" );
		assertEquals( 0, zero.checkint() );
		assertEquals( sampleint, intint.checkint() );
		assertEquals( (int) samplelong, longdouble.checkint() );
		assertEquals( (int) sampledouble, doubledouble.checkint() );
		throwsErrorReq( somefunc, "checkint" );
		throwsErrorReq( someclosure, "checkint" );
		throwsErrorReq( stringstring, "checkint" );
		assertEquals( sampleint, stringint.checkint() );
		assertEquals( (int) samplelong, stringlong.checkint() );
		assertEquals( (int) sampledouble, stringdouble.checkint() );
		throwsErrorReq( thread, "checkint" );
		throwsErrorReq( table, "checkint" );
		throwsErrorReq( userdataobj, "checkint" );
		throwsErrorReq( userdatacls, "checkint" );
	}
	
	public void testCheckInteger() {
		throwsErrorReq( somenil, "checkinteger" );
		throwsErrorReq( sometrue, "checkinteger" );
		throwsErrorReq( somefalse, "checkinteger" );
		assertEquals( zero, zero.checkinteger() );
		assertEquals( LuaValue.valueOf( sampleint ), intint.checkinteger() );
		assertEquals( LuaValue.valueOf( (int) samplelong ), longdouble.checkinteger() );
		assertEquals( LuaValue.valueOf( (int) sampledouble ), doubledouble.checkinteger() );
		throwsErrorReq( somefunc, "checkinteger" );
		throwsErrorReq( someclosure, "checkinteger" );
		throwsErrorReq( stringstring, "checkinteger" );
		assertEquals( LuaValue.valueOf( sampleint), stringint.checkinteger() );
		assertEquals( LuaValue.valueOf( (int) samplelong), stringlong.checkinteger() );
		assertEquals( LuaValue.valueOf( (int) sampledouble), stringdouble.checkinteger() );
		throwsErrorReq( thread, "checkinteger" );
		throwsErrorReq( table, "checkinteger" );
		throwsErrorReq( userdataobj, "checkinteger" );
		throwsErrorReq( userdatacls, "checkinteger" );
	}

	public void testCheckLong() {
		throwsErrorReq( somenil, "checklong" );
		throwsErrorReq( sometrue, "checklong" );
		throwsErrorReq( somefalse, "checklong" );
		assertEquals( 0L, zero.checklong() );
		assertEquals( sampleint, intint.checklong() );
		assertEquals( (long) samplelong, longdouble.checklong() );
		assertEquals( (long) sampledouble, doubledouble.checklong() );
		throwsErrorReq( somefunc, "checklong" );
		throwsErrorReq( someclosure, "checklong" );
		throwsErrorReq( stringstring, "checklong" );
		assertEquals( sampleint, stringint.checklong() );
		assertEquals( (long) samplelong, stringlong.checklong() );
		assertEquals( (long) sampledouble, stringdouble.checklong() );
		throwsErrorReq( thread, "checklong" );
		throwsErrorReq( table, "checklong" );
		throwsErrorReq( userdataobj, "checklong" );
		throwsErrorReq( userdatacls, "checklong" );
	}
	
	public void testCheckNumber() {
		throwsErrorReq( somenil, "checknumber" );
		throwsErrorReq( sometrue, "checknumber" );
		throwsErrorReq( somefalse, "checknumber" );
		assertEquals( zero, zero.checknumber() );
		assertEquals( LuaValue.valueOf( sampleint ), intint.checknumber() );
		assertEquals( LuaValue.valueOf( samplelong ), longdouble.checknumber() );
		assertEquals( LuaValue.valueOf( sampledouble ), doubledouble.checknumber() );
		throwsErrorReq( somefunc, "checknumber" );
		throwsErrorReq( someclosure, "checknumber" );
		throwsErrorReq( stringstring, "checknumber" );
		assertEquals( LuaValue.valueOf( sampleint), stringint.checknumber() );
		assertEquals( LuaValue.valueOf( samplelong), stringlong.checknumber() );
		assertEquals( LuaValue.valueOf( sampledouble), stringdouble.checknumber() );
		throwsErrorReq( thread, "checknumber" );
		throwsErrorReq( table, "checknumber" );
		throwsErrorReq( userdataobj, "checknumber" );
		throwsErrorReq( userdatacls, "checknumber" );
	}
	
	public void testCheckTable() {
		throwsErrorReq( somenil, "checktable" );
		throwsErrorReq( sometrue, "checktable" );
		throwsErrorReq( somefalse, "checktable" );
		throwsErrorReq( zero, "checktable" );
		throwsErrorReq( intint, "checktable" );
		throwsErrorReq( longdouble, "checktable" );
		throwsErrorReq( doubledouble, "checktable" );
		throwsErrorReq( somefunc, "checktable" );
		throwsErrorReq( someclosure, "checktable" );
		throwsErrorReq( stringstring, "checktable" );
		throwsErrorReq( stringint, "checktable" );
		throwsErrorReq( stringlong, "checktable" );
		throwsErrorReq( stringdouble, "checktable" );
		throwsErrorReq( thread, "checktable" );
		assertEquals( table, table.checktable() );
		assertEquals( table, table.checktable() );
		throwsErrorReq( userdataobj, "checktable" );
		throwsErrorReq( userdatacls, "checktable" );
	}
	
	public void testCheckThread() {
		throwsErrorReq( somenil, "checkthread" );
		throwsErrorReq( sometrue, "checkthread" );
		throwsErrorReq( somefalse, "checkthread" );
		throwsErrorReq( zero, "checkthread" );
		throwsErrorReq( intint, "checkthread" );
		throwsErrorReq( longdouble, "checkthread" );
		throwsErrorReq( doubledouble, "checkthread" );
		throwsErrorReq( somefunc, "checkthread" );
		throwsErrorReq( someclosure, "checkthread" );
		throwsErrorReq( stringstring, "checkthread" );
		throwsErrorReq( stringint, "checkthread" );
		throwsErrorReq( stringlong, "checkthread" );
		throwsErrorReq( stringdouble, "checkthread" );
		throwsErrorReq( table, "checkthread" );
		assertEquals( thread, thread.checkthread() );
		assertEquals( thread, thread.checkthread() );
		throwsErrorReq( userdataobj, "checkthread" );
		throwsErrorReq( userdatacls, "checkthread" );
	}
	
	public void testCheckJavaString() {
		throwsErrorReq( somenil, "checkjstring" );
		throwsErrorReq( sometrue, "checkjstring" );
		throwsErrorReq( somefalse, "checkjstring" );
		assertEquals( String.valueOf(zero), zero.checkjstring() );
		assertEquals( String.valueOf(intint), intint.checkjstring() );
		assertEquals( String.valueOf(longdouble), longdouble.checkjstring() );
		assertEquals( String.valueOf(doubledouble), doubledouble.checkjstring() );
		throwsErrorReq( somefunc, "checkjstring" );
		throwsErrorReq( someclosure, "checkjstring" );
		assertEquals( samplestringstring, stringstring.checkjstring() );
		assertEquals( samplestringint, stringint.checkjstring() );
		assertEquals( samplestringlong, stringlong.checkjstring() );
		assertEquals( samplestringdouble, stringdouble.checkjstring() );
		throwsErrorReq( thread, "checkjstring" );
		throwsErrorReq( table, "checkjstring" );
		throwsErrorReq( userdataobj, "checkjstring" );
		throwsErrorReq( userdatacls, "checkjstring" );
	}
	
	public void testCheckLuaString() {
		throwsErrorReq( somenil, "checkstring" );
		throwsErrorReq( sometrue, "checkstring" );
		throwsErrorReq( somefalse, "checkstring" );
		assertEquals( LuaValue.valueOf("0"), zero.checkstring() );
		assertEquals( stringint, intint.checkstring() );
		assertEquals( stringlong, longdouble.checkstring() );
		assertEquals( stringdouble, doubledouble.checkstring() );
		throwsErrorReq( somefunc, "checkstring" );
		throwsErrorReq( someclosure, "checkstring" );
		assertEquals( stringstring, stringstring.checkstring() );
		assertEquals( stringint, stringint.checkstring() );
		assertEquals( stringlong, stringlong.checkstring() );
		assertEquals( stringdouble, stringdouble.checkstring() );
		throwsErrorReq( thread, "checkstring" );
		throwsErrorReq( table, "checkstring" );
		throwsErrorReq( userdataobj, "checkstring" );
		throwsErrorReq( userdatacls, "checkstring" );
	}
	
	public void testCheckUserdata() {
		throwsErrorReq( somenil, "checkuserdata" );
		throwsErrorReq( sometrue, "checkuserdata" );
		throwsErrorReq( somefalse, "checkuserdata" );
		throwsErrorReq( zero, "checkuserdata" );
		throwsErrorReq( intint, "checkuserdata" );
		throwsErrorReq( longdouble, "checkuserdata" );
		throwsErrorReq( doubledouble, "checkuserdata" );
		throwsErrorReq( somefunc, "checkuserdata" );
		throwsErrorReq( someclosure, "checkuserdata" );
		throwsErrorReq( stringstring, "checkuserdata" );
		throwsErrorReq( stringint, "checkuserdata" );
		throwsErrorReq( stringlong, "checkuserdata" );
		throwsErrorReq( stringdouble, "checkuserdata" );
		throwsErrorReq( table, "checkuserdata" );
		assertEquals( sampleobject, userdataobj.checkuserdata() );
		assertEquals( sampleobject, userdataobj.checkuserdata() );
		assertEquals( sampledata, userdatacls.checkuserdata() );
		assertEquals( sampledata, userdatacls.checkuserdata() );
	}

	private void throwsErrorReqCheckUserdataClass(LuaValue obj, Class arg ) {
		try {
			obj.getClass().getMethod("checkuserdata", Class.class ).invoke(obj, arg);
		} catch (InvocationTargetException e) {
			if ( ! (e.getTargetException() instanceof LuaError) )
				fail("not a LuaError: "+e.getTargetException());
			return; // pass
		} catch ( Exception e ) {
			fail( "bad exception: "+e );
		}
		fail("failed to throw LuaError as required");
	}
	
	public void testCheckUserdataClass() {
		throwsErrorReqCheckUserdataClass( somenil,  Object.class );
		throwsErrorReqCheckUserdataClass( somenil,  MyData.class);
		throwsErrorReqCheckUserdataClass( sometrue,  Object.class );
		throwsErrorReqCheckUserdataClass( zero,  MyData.class);
		throwsErrorReqCheckUserdataClass( intint,  MyData.class);
		throwsErrorReqCheckUserdataClass( longdouble,  MyData.class);
		throwsErrorReqCheckUserdataClass( somefunc,  MyData.class);
		throwsErrorReqCheckUserdataClass( someclosure,  MyData.class);
		throwsErrorReqCheckUserdataClass( stringstring,  MyData.class);
		throwsErrorReqCheckUserdataClass( stringint,  MyData.class);
		throwsErrorReqCheckUserdataClass( stringlong,  MyData.class);
		throwsErrorReqCheckUserdataClass( stringlong,  MyData.class);
		throwsErrorReqCheckUserdataClass( stringdouble,  MyData.class);
		throwsErrorReqCheckUserdataClass( table,  MyData.class);
		throwsErrorReqCheckUserdataClass( thread,  MyData.class);
		assertEquals( sampleobject, userdataobj.checkuserdata(Object.class) );
		assertEquals( sampleobject, userdataobj.checkuserdata() );
		assertEquals( sampledata, userdatacls.checkuserdata(MyData.class) );
		assertEquals( sampledata, userdatacls.checkuserdata(Object.class) );
		assertEquals( sampledata, userdatacls.checkuserdata() );
		// should fail due to wrong class
		try {
			Object o = userdataobj.checkuserdata(MyData.class);
			fail( "did not throw bad type error" );
			assertTrue( o instanceof MyData );
		} catch ( LuaError le ) {
			assertEquals( "org.luaj.vm2.TypeTest$MyData expected, got userdata", le.getMessage() );
		}
	}
	
	public void testCheckValue() {
		throwsErrorReq( somenil, "checknotnil" );
		assertEquals( sometrue, sometrue.checknotnil() );
		assertEquals( somefalse, somefalse.checknotnil() );
		assertEquals( zero, zero.checknotnil() );
		assertEquals( intint, intint.checknotnil() );
		assertEquals( longdouble, longdouble.checknotnil() );
		assertEquals( somefunc, somefunc.checknotnil() );
		assertEquals( someclosure, someclosure.checknotnil() );
		assertEquals( stringstring, stringstring.checknotnil() );
		assertEquals( stringint, stringint.checknotnil() );
		assertEquals( stringlong, stringlong.checknotnil() );
		assertEquals( stringdouble, stringdouble.checknotnil() );
		assertEquals( thread, thread.checknotnil() );
		assertEquals( table, table.checknotnil() );
		assertEquals( userdataobj, userdataobj.checknotnil() );
		assertEquals( userdatacls, userdatacls.checknotnil() );
	}
	
}
