package org.luaj.vm;

import junit.framework.TestCase;

import org.luaj.lib.j2se.CoerceJavaToLua;
import org.luaj.lib.j2se.CoerceLuaToJava;
import org.luaj.platform.J2sePlatform;

public class LuaJavaCoercionTest extends TestCase {

	private LuaState vm;
	private static LInteger ZERO  = LInteger.valueOf(0);
	private static LInteger ONE   = LInteger.valueOf(1);
	private static LInteger TWO   = LInteger.valueOf(2);
	private static LInteger THREE = LInteger.valueOf(3);
	private static LString LENGTH = LString.valueOf("length");
	
	protected void setUp() throws Exception {
		super.setUp();
		Platform.setInstance( new J2sePlatform() );
		org.luaj.compiler.LuaC.install();
		vm = Platform.newLuaState();
	}
	
	public void testJavaIntToLuaInt() {
		Integer i = Integer.valueOf(777);
		LValue v = CoerceJavaToLua.coerce(i);
		assertEquals( LInteger.class, v.getClass() );
		assertEquals( 777, v.toJavaInt() );
	}

	public void testLuaIntToJavaInt() {
		LInteger i = LInteger.valueOf(777);
		Object o = CoerceLuaToJava.coerceArg(i, int.class);
		assertEquals( Integer.class, o.getClass() );
		assertEquals( 777, ((Number)o).intValue() );
		o = CoerceLuaToJava.coerceArg(i, Integer.class);
		assertEquals( Integer.class, o.getClass() );
		assertEquals( new Integer(777), o );
	}
	
	public void testJavaStringToLuaString() {
		String s = new String("777");
		LValue v = CoerceJavaToLua.coerce(s);
		assertEquals( LString.class, v.getClass() );
		assertEquals( "777", v.toJavaString() );
	}

	public void testLuaStringToJavaString() {
		LString s = new LString("777");
		Object o = CoerceLuaToJava.coerceArg(s, String.class);
		assertEquals( String.class, o.getClass() );
		assertEquals( "777", o );
	}
	
	public void testJavaIntArrayToLuaTable() {
		int[] i = { 222, 333 };
		LValue v = CoerceJavaToLua.coerce(i);
		assertEquals( LUserData.class, v.getClass() );
		assertNotNull( v.luaGetMetatable() );
		assertEquals( LInteger.valueOf(222), v.luaGetTable(vm, ONE) );
		assertEquals( LInteger.valueOf(333), v.luaGetTable(vm, TWO) );
		assertEquals( TWO, v.luaGetTable(vm, LENGTH));
		assertEquals( LNil.NIL, v.luaGetTable(vm, THREE) );
		assertEquals( LNil.NIL, v.luaGetTable(vm, ZERO) );
		v.luaSetTable(vm, ONE, LInteger.valueOf(444));
		v.luaSetTable(vm, TWO, LInteger.valueOf(555));
		assertEquals( 444, i[0] );
		assertEquals( 555, i[1] );
		assertEquals( LInteger.valueOf(444), v.luaGetTable(vm, ONE) );
		assertEquals( LInteger.valueOf(555), v.luaGetTable(vm, TWO) );
		try {
			v.luaSetTable(vm, ZERO, LInteger.valueOf(777));
			fail( "array bound exception not thrown" );
		} catch ( LuaErrorException lee ) {
			// expected
		}
		try {
			v.luaSetTable(vm, THREE, LInteger.valueOf(777));
			fail( "array bound exception not thrown" );
		} catch ( LuaErrorException lee ) {
			// expected
		}
	}

	public void testLuaTableToJavaIntArray() {
		LTable t = new LTable();
		t.put(1, LInteger.valueOf(222) );
		t.put(2, LInteger.valueOf(333) );
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
		LValue la = LInteger.valueOf(a);
		LTable tb = new LTable();
		LTable tc = new LTable();
		LValue va = CoerceJavaToLua.coerce(a);
		LValue vb = CoerceJavaToLua.coerce(b);
		LValue vc = CoerceJavaToLua.coerce(c);
		tc.put( ONE, new LTable() );
		
		int saa = CoerceLuaToJava.scoreParamTypes( new LValue[] { la }, new Class[] { int.class } );
		int sab = CoerceLuaToJava.scoreParamTypes( new LValue[] { la }, new Class[] { int[].class } );
		int sac = CoerceLuaToJava.scoreParamTypes( new LValue[] { la }, new Class[] { int[][].class } );
		assertTrue( saa < sab );
		assertTrue( saa < sac );
		int sba = CoerceLuaToJava.scoreParamTypes( new LValue[] { tb }, new Class[] { int.class } );
		int sbb = CoerceLuaToJava.scoreParamTypes( new LValue[] { tb }, new Class[] { int[].class } );
		int sbc = CoerceLuaToJava.scoreParamTypes( new LValue[] { tb }, new Class[] { int[][].class } );
		assertTrue( sbb < sba );
		assertTrue( sbb < sbc );
		int sca = CoerceLuaToJava.scoreParamTypes( new LValue[] { tc }, new Class[] { int.class } );
		int scb = CoerceLuaToJava.scoreParamTypes( new LValue[] { tc }, new Class[] { int[].class } );
		int scc = CoerceLuaToJava.scoreParamTypes( new LValue[] { tc }, new Class[] { int[][].class } );
		assertTrue( scc < sca );
		assertTrue( scc < scb );
		
		int vaa = CoerceLuaToJava.scoreParamTypes( new LValue[] { va }, new Class[] { int.class } );
		int vab = CoerceLuaToJava.scoreParamTypes( new LValue[] { va }, new Class[] { int[].class } );
		int vac = CoerceLuaToJava.scoreParamTypes( new LValue[] { va }, new Class[] { int[][].class } );
		assertTrue( vaa < vab );
		assertTrue( vaa < vac );
		int vba = CoerceLuaToJava.scoreParamTypes( new LValue[] { vb }, new Class[] { int.class } );
		int vbb = CoerceLuaToJava.scoreParamTypes( new LValue[] { vb }, new Class[] { int[].class } );
		int vbc = CoerceLuaToJava.scoreParamTypes( new LValue[] { vb }, new Class[] { int[][].class } );
		assertTrue( vbb < vba );
		assertTrue( vbb < vbc );
		int vca = CoerceLuaToJava.scoreParamTypes( new LValue[] { vc }, new Class[] { int.class } );
		int vcb = CoerceLuaToJava.scoreParamTypes( new LValue[] { vc }, new Class[] { int[].class } );
		int vcc = CoerceLuaToJava.scoreParamTypes( new LValue[] { vc }, new Class[] { int[][].class } );
		assertTrue( vcc < vca );
		assertTrue( vcc < vcb );
	}
	
	public static class SampleClass {
		public String sample() { return "void-args"; }
		public String sample(int a) { return "int-args "+a; }
		public String sample(int[] a) { return "int-array-args "+a[0]+","+a[1]; }
		public String sample(int[][] a) { return "int-array-array-args "+a[0][0]+","+a[0][1]+","+a[1][0]+","+a[1][1]; }
	}
	
	private static final LString SAMPLE = LString.valueOf("sample");
	
	public void testIntArrayParameterMatching() {
		LValue v = CoerceJavaToLua.coerce(new SampleClass());
		
		// get sample field, call with no arguments
		LValue method = v.luaGetTable(vm, SAMPLE);
		vm.pushlvalue(method);
		vm.pushlvalue(v);
		vm.call(1,1);
		assertEquals( "void-args", vm.tostring(-1) );
		
		// get sample field, call with no arguments
		vm.pushlvalue(method);
		vm.pushlvalue(v);
		vm.pushlvalue( CoerceJavaToLua.coerce(new Integer(123)));
		vm.call(2,1);
		assertEquals( "int-args 123", vm.tostring(-1) );
		
		// get sample field, call with no arguments
		vm.pushlvalue(method);
		vm.pushlvalue(v);
		vm.pushlvalue( CoerceJavaToLua.coerce(new int[]{345,678}) );
		vm.call(2,1);
		assertEquals( "int-array-args 345,678", vm.tostring(-1) );
		
		// get sample field, call with no arguments
		vm.pushlvalue(method);
		vm.pushlvalue(v);
		vm.pushlvalue( CoerceJavaToLua.coerce(new int[][]{{22,33},{44,55}}) );
		vm.call(2,1);
		assertEquals( "int-array-array-args 22,33,44,55", vm.tostring(-1) );
	}
	

}
