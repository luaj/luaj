package org.luaj.vm2.lib.jse;

import junit.framework.TestCase;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class LuaJavaCoercionTest extends TestCase {

	private static LuaValue _G;
	private static LuaValue ZERO   = LuaValue.ZERO;
	private static LuaValue ONE   = LuaValue.ONE;
	private static LuaValue TWO   = LuaValue.valueOf(2);
	private static LuaValue THREE = LuaValue.valueOf(3);
	private static LuaString LENGTH = LuaString.valueOf("length");
	
	protected void setUp() throws Exception {
		super.setUp();
		_G = JsePlatform.standardGlobals();
	}
	
	public void testJavaIntToLuaInt() {
		Integer i = Integer.valueOf(777);
		LuaValue v = CoerceJavaToLua.coerce(i);
		assertEquals( LuaInteger.class, v.getClass() );
		assertEquals( 777, v.toint() );
	}

	public void testLuaIntToJavaInt() {
		LuaInteger i = LuaInteger.valueOf(777);
		Object o = CoerceLuaToJava.coerceArg(i, int.class);
		assertEquals( Integer.class, o.getClass() );
		assertEquals( 777, ((Number)o).intValue() );
		o = CoerceLuaToJava.coerceArg(i, Integer.class);
		assertEquals( Integer.class, o.getClass() );
		assertEquals( new Integer(777), o );
	}
	
	public void testJavaStringToLuaString() {
		String s = new String("777");
		LuaValue v = CoerceJavaToLua.coerce(s);
		assertEquals( LuaString.class, v.getClass() );
		assertEquals( "777", v.toString() );
	}

	public void testLuaStringToJavaString() {
		LuaString s = LuaValue.valueOf("777");
		Object o = CoerceLuaToJava.coerceArg(s, String.class);
		assertEquals( String.class, o.getClass() );
		assertEquals( "777", o );
	}
	
	public void testJavaIntArrayToLuaTable() {
		int[] i = { 222, 333 };
		LuaValue v = CoerceJavaToLua.coerce(i);
		assertEquals( LuaUserdata.class, v.getClass() );
		assertNotNull( v.getmetatable() );
		assertEquals( LuaInteger.valueOf(222), v.get(ONE) );
		assertEquals( LuaInteger.valueOf(333), v.get(TWO) );
		assertEquals( TWO, v.get(LENGTH));
		assertEquals( LuaValue.NIL, v.get(THREE) );
		assertEquals( LuaValue.NIL, v.get(ZERO) );
		v.set(ONE, LuaInteger.valueOf(444));
		v.set(TWO, LuaInteger.valueOf(555));
		assertEquals( 444, i[0] );
		assertEquals( 555, i[1] );
		assertEquals( LuaInteger.valueOf(444), v.get(ONE) );
		assertEquals( LuaInteger.valueOf(555), v.get(TWO) );
		try {
			v.set(ZERO, LuaInteger.valueOf(777));
			fail( "array bound exception not thrown" );
		} catch ( LuaError lee ) {
			// expected
		}
		try {
			v.set(THREE, LuaInteger.valueOf(777));
			fail( "array bound exception not thrown" );
		} catch ( LuaError lee ) {
			// expected
		}
	}

	public void testLuaTableToJavaIntArray() {
		LuaTable t = new LuaTable();
		t.set(1, LuaInteger.valueOf(222) );
		t.set(2, LuaInteger.valueOf(333) );
		int[] i = null;
		Object o = CoerceLuaToJava.coerceArg(t, int[].class);
		assertEquals( int[].class, o.getClass() );
		i = (int[]) o;
		assertEquals( 2, i.length );
		assertEquals( 222, i[0] );
		assertEquals( 333, i[1] );
	}
	

	public void testArrayParamScoring() {
		int a = 5;
		int[] b = { 44, 66 };
		int[][] c = { { 11, 22 }, { 33, 44 } };
		LuaValue la = LuaInteger.valueOf(a);
		LuaTable tb = new LuaTable();
		LuaTable tc = new LuaTable();
		LuaValue va = CoerceJavaToLua.coerce(a);
		LuaValue vb = CoerceJavaToLua.coerce(b);
		LuaValue vc = CoerceJavaToLua.coerce(c);
		tc.set( 1, new LuaTable() );
		
		int saa = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(la), new Class[] { int.class } );
		int sab = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(la), new Class[] { int[].class } );
		int sac = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(la), new Class[] { int[][].class } );
		assertTrue( saa < sab );
		assertTrue( saa < sac );
		int sba = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(tb), new Class[] { int.class } );
		int sbb = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(tb), new Class[] { int[].class } );
		int sbc = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(tb), new Class[] { int[][].class } );
		assertTrue( sbb < sba );
		assertTrue( sbb < sbc );
		int sca = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(tc), new Class[] { int.class } );
		int scb = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(tc), new Class[] { int[].class } );
		int scc = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(tc), new Class[] { int[][].class } );
		assertTrue( scc < sca );
		assertTrue( scc < scb );
		
		int vaa = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(va), new Class[] { int.class } );
		int vab = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(va), new Class[] { int[].class } );
		int vac = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(va), new Class[] { int[][].class } );
		assertTrue( vaa < vab );
		assertTrue( vaa < vac );
		int vba = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(vb), new Class[] { int.class } );
		int vbb = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(vb), new Class[] { int[].class } );
		int vbc = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(vb), new Class[] { int[][].class } );
		assertTrue( vbb < vba );
		assertTrue( vbb < vbc );
		int vca = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(vc), new Class[] { int.class } );
		int vcb = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(vc), new Class[] { int[].class } );
		int vcc = CoerceLuaToJava.scoreParamTypes( LuajavaLib.paramsSignatureOf(vc), new Class[] { int[][].class } );
		assertTrue( vcc < vca );
		assertTrue( vcc < vcb );
	}
	
	public static class SampleClass {
		public String sample() { return "void-args"; }
		public String sample(int a) { return "int-args "+a; }
		public String sample(int[] a) { return "int-array-args "+a[0]+","+a[1]; }
		public String sample(int[][] a) { return "int-array-array-args "+a[0][0]+","+a[0][1]+","+a[1][0]+","+a[1][1]; }
	}
	
	
	public void testIntArrayParameterMatching() {
		LuaValue v = CoerceJavaToLua.coerce(new SampleClass());
		
		// get sample field, call with no arguments
		LuaValue result = v.method("sample");
		assertEquals( "void-args", result.toString() );
		
		// get sample field, call with one arguments
		LuaValue arg = CoerceJavaToLua.coerce(new Integer(123));
		result = v.method("sample",arg);
		assertEquals( "int-args 123", result.toString() );
		
		// get sample field, call with array argument
		arg = CoerceJavaToLua.coerce(new int[]{345,678});
		result = v.method("sample",arg);
		assertEquals( "int-array-args 345,678", result.toString() );
		
		// get sample field, call with two-d array argument
		arg = CoerceJavaToLua.coerce(new int[][]{{22,33},{44,55}});
		result = v.method("sample",arg);
		assertEquals( "int-array-array-args 22,33,44,55", result.toString() );
	}
	
	public static final class SomeException extends RuntimeException {
		public SomeException(String message) {
			super(message);
		}
	}
	
	public static final class SomeClass {
		public static void someMethod() {
			throw new SomeException( "this is some message" );
		}
	}
	
	public void testExceptionMessage() {
		String script = "return pcall( luajava.bindClass( \""+SomeClass.class.getName()+"\").someMethod )";
		Varargs vresult = _G.get("loadstring").call(LuaValue.valueOf(script)).invoke(LuaValue.NONE);
		LuaValue status = vresult.arg1();
		LuaValue message = vresult.arg(2);
		assertEquals( LuaValue.FALSE, status );		
		int index = message.toString().indexOf( "this is some message" );
		assertTrue( "bad message: "+message, index>=0 );		
	}

	public void testLuaErrorCause() {
		String script = "luajava.bindClass( \""+SomeClass.class.getName()+"\").someMethod()";
		LuaValue chunk = _G.get("loadstring").call(LuaValue.valueOf(script));
		try {
			chunk.invoke(LuaValue.NONE);
			fail( "call should not have succeeded" );
		} catch ( LuaError lee ) {
			Throwable c = lee.getCause();
			assertEquals( SomeException.class, c.getClass() );
		}
	}
	
	public interface VarArgsInterface {
		public String varargsMethod( String a, String ... v );
		public String arrayargsMethod( String a, String[] v );
	}
	
	public void testVarArgsProxy() {		
		String script = "return luajava.createProxy( \""+VarArgsInterface.class.getName()+"\", \n"+
			"{\n" +
			"	varargsMethod = function(a,...)\n" +
			"		return table.concat({a,...},'-')\n" +
			"	end,\n" +
			"	arrayargsMethod = function(a,array)\n" +
			"		return tostring(a)..(array and \n" +
			"			('-'..tostring(array.length)\n" +
			"			..'-'..tostring(array[1])\n" +
			"			..'-'..tostring(array[2])\n" +
			"			) or '-nil')\n" +
			"	end,\n" +
			"} )\n";
		Varargs chunk = _G.get("loadstring").call(LuaValue.valueOf(script));
		if ( ! chunk.arg1().toboolean() )
			fail( chunk.arg(2).toString() );
		LuaValue result = chunk.arg1().call();
		Object u = result.touserdata();
		VarArgsInterface v = (VarArgsInterface) u;
		assertEquals( "foo", v.varargsMethod("foo") );
		assertEquals( "foo-bar", v.varargsMethod("foo", "bar") );
		assertEquals( "foo-bar-etc", v.varargsMethod("foo", "bar", "etc") );
		assertEquals( "foo-0-nil-nil", v.arrayargsMethod("foo", new String[0]) );
		assertEquals( "foo-1-bar-nil", v.arrayargsMethod("foo", new String[] {"bar"}) );
		assertEquals( "foo-2-bar-etc", v.arrayargsMethod("foo", new String[] {"bar","etc"}) );
		assertEquals( "foo-3-bar-etc", v.arrayargsMethod("foo", new String[] {"bar","etc","etc"}) );
		assertEquals( "foo-nil", v.arrayargsMethod("foo", null) );
	}
	
	public void testBigNum() {
		String script = 
			"bigNumA = luajava.newInstance('java.math.BigDecimal','12345678901234567890');\n" +
			"bigNumB = luajava.newInstance('java.math.BigDecimal','12345678901234567890');\n" +
			"bigNumC = bigNumA:multiply(bigNumB);\n" +
			//"print(bigNumA:toString())\n" +
			//"print(bigNumB:toString())\n" +
			//"print(bigNumC:toString())\n" +
			"return bigNumA:toString(), bigNumB:toString(), bigNumC:toString()";
		Varargs chunk = _G.get("loadstring").call(LuaValue.valueOf(script));
		if ( ! chunk.arg1().toboolean() )
			fail( chunk.arg(2).toString() );
		Varargs results = chunk.arg1().invoke();
		int nresults = results.narg();
		String sa = results.tojstring(1);
		String sb = results.tojstring(2);
		String sc = results.tojstring(3);
		assertEquals( 3, nresults );
		assertEquals( "12345678901234567890", sa );
		assertEquals( "12345678901234567890", sb );
		assertEquals( "152415787532388367501905199875019052100", sc );
	}
	
}
