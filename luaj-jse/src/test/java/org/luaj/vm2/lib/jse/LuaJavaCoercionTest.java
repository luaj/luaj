package org.luaj.vm2.lib.jse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

class LuaJavaCoercionTest {

	private static LuaValue  globals;
	private static LuaValue  ZERO   = LuaValue.ZERO;
	private static LuaValue  ONE    = LuaValue.ONE;
	private static LuaValue  TWO    = LuaValue.valueOf(2);
	private static LuaValue  THREE  = LuaValue.valueOf(3);
	private static LuaString LENGTH = LuaString.valueOf("length");

	@BeforeEach
	protected void setUp() throws Exception {
		globals = JsePlatform.standardGlobals();
	}

	@Test
	void testJavaIntToLuaInt() {
		Integer i = Integer.valueOf(777);
		LuaValue v = CoerceJavaToLua.coerce(i);
		assertEquals(LuaInteger.class, v.getClass());
		assertEquals(777, v.toint());
	}

	@Test
	void testLuaIntToJavaInt() {
		LuaInteger i = LuaInteger.valueOf(777);
		Object o = CoerceLuaToJava.coerce(i, int.class);
		assertEquals(Integer.class, o.getClass());
		assertEquals(777, ((Number) o).intValue());
		o = CoerceLuaToJava.coerce(i, Integer.class);
		assertEquals(Integer.class, o.getClass());
		assertEquals(new Integer(777), o);
	}

	@Test
	void testJavaStringToLuaString() {
		String s = new String("777");
		LuaValue v = CoerceJavaToLua.coerce(s);
		assertEquals(LuaString.class, v.getClass());
		assertEquals("777", v.toString());
	}

	@Test
	void testLuaStringToJavaString() {
		LuaString s = LuaValue.valueOf("777");
		Object o = CoerceLuaToJava.coerce(s, String.class);
		assertEquals(String.class, o.getClass());
		assertEquals("777", o);
	}

	@Test
	void testJavaClassToLuaUserdata() {
		LuaValue va = CoerceJavaToLua.coerce(ClassA.class);
		LuaValue va1 = CoerceJavaToLua.coerce(ClassA.class);
		LuaValue vb = CoerceJavaToLua.coerce(ClassB.class);
		assertSame(va, va1);
		assertNotSame(va, vb);
		LuaValue vi = CoerceJavaToLua.coerce(new ClassA());
		assertNotSame(va, vi);
		assertTrue(vi.isuserdata());
		assertTrue(vi.isuserdata(ClassA.class));
		assertFalse(vi.isuserdata(ClassB.class));
		LuaValue vj = CoerceJavaToLua.coerce(new ClassB());
		assertNotSame(vb, vj);
		assertTrue(vj.isuserdata());
		assertFalse(vj.isuserdata(ClassA.class));
		assertTrue(vj.isuserdata(ClassB.class));
	}

	static class ClassA {
	}

	static class ClassB {
	}

	@Test
	void testJavaIntArrayToLuaTable() {
		int[] i = { 222, 333 };
		LuaValue v = CoerceJavaToLua.coerce(i);
		assertEquals(JavaArray.class, v.getClass());
		assertEquals(LuaInteger.valueOf(222), v.get(ONE));
		assertEquals(LuaInteger.valueOf(333), v.get(TWO));
		assertEquals(TWO, v.get(LENGTH));
		assertEquals(LuaValue.NIL, v.get(THREE));
		assertEquals(LuaValue.NIL, v.get(ZERO));
		v.set(ONE, LuaInteger.valueOf(444));
		v.set(TWO, LuaInteger.valueOf(555));
		assertEquals(444, i[0]);
		assertEquals(555, i[1]);
		assertEquals(LuaInteger.valueOf(444), v.get(ONE));
		assertEquals(LuaInteger.valueOf(555), v.get(TWO));
		try {
			v.set(ZERO, LuaInteger.valueOf(777));
			fail("array bound exception not thrown");
		} catch (LuaError lee) {
			// expected
		}
		try {
			v.set(THREE, LuaInteger.valueOf(777));
			fail("array bound exception not thrown");
		} catch (LuaError lee) {
			// expected
		}
	}

	@Test
	void testLuaTableToJavaIntArray() {
		LuaTable t = new LuaTable();
		t.set(1, LuaInteger.valueOf(222));
		t.set(2, LuaInteger.valueOf(333));
		int[] i = null;
		Object o = CoerceLuaToJava.coerce(t, int[].class);
		assertEquals(int[].class, o.getClass());
		i = (int[]) o;
		assertEquals(2, i.length);
		assertEquals(222, i[0]);
		assertEquals(333, i[1]);
	}

	@Test
	void testIntArrayScoringTables() {
		int a = 5;
		LuaValue la = LuaInteger.valueOf(a);
		LuaTable tb = new LuaTable();
		tb.set(1, la);
		LuaTable tc = new LuaTable();
		tc.set(1, tb);

		int saa = CoerceLuaToJava.getCoercion(int.class).score(la);
		int sab = CoerceLuaToJava.getCoercion(int[].class).score(la);
		int sac = CoerceLuaToJava.getCoercion(int[][].class).score(la);
		assertTrue(saa < sab);
		assertTrue(saa < sac);
		int sba = CoerceLuaToJava.getCoercion(int.class).score(tb);
		int sbb = CoerceLuaToJava.getCoercion(int[].class).score(tb);
		int sbc = CoerceLuaToJava.getCoercion(int[][].class).score(tb);
		assertTrue(sbb < sba);
		assertTrue(sbb < sbc);
		int sca = CoerceLuaToJava.getCoercion(int.class).score(tc);
		int scb = CoerceLuaToJava.getCoercion(int[].class).score(tc);
		int scc = CoerceLuaToJava.getCoercion(int[][].class).score(tc);
		assertTrue(scc < sca);
		assertTrue(scc < scb);
	}

	@Test
	void testIntArrayScoringUserdata() {
		int a = 5;
		int[] b = { 44, 66 };
		int[][] c = { { 11, 22 }, { 33, 44 } };
		LuaValue va = CoerceJavaToLua.coerce(a);
		LuaValue vb = CoerceJavaToLua.coerce(b);
		LuaValue vc = CoerceJavaToLua.coerce(c);

		int vaa = CoerceLuaToJava.getCoercion(int.class).score(va);
		int vab = CoerceLuaToJava.getCoercion(int[].class).score(va);
		int vac = CoerceLuaToJava.getCoercion(int[][].class).score(va);
		assertTrue(vaa < vab);
		assertTrue(vaa < vac);
		int vba = CoerceLuaToJava.getCoercion(int.class).score(vb);
		int vbb = CoerceLuaToJava.getCoercion(int[].class).score(vb);
		int vbc = CoerceLuaToJava.getCoercion(int[][].class).score(vb);
		assertTrue(vbb < vba);
		assertTrue(vbb < vbc);
		int vca = CoerceLuaToJava.getCoercion(int.class).score(vc);
		int vcb = CoerceLuaToJava.getCoercion(int[].class).score(vc);
		int vcc = CoerceLuaToJava.getCoercion(int[][].class).score(vc);
		assertTrue(vcc < vca);
		assertTrue(vcc < vcb);
	}

	public static class SampleClass {
		public String sample() { return "void-args"; }

		public String sample(int a) { return "int-args " + a; }

		public String sample(int[] a) { return "int-array-args " + a[0] + "," + a[1]; }

		public String sample(int[][] a) {
			return "int-array-array-args " + a[0][0] + "," + a[0][1] + "," + a[1][0] + "," + a[1][1];
		}
	}

	@Test
	void testMatchVoidArgs() {
		LuaValue v = CoerceJavaToLua.coerce(new SampleClass());
		LuaValue result = v.method("sample");
		assertEquals("void-args", result.toString());
	}

	@Test
	void testMatchIntArgs() {
		LuaValue v = CoerceJavaToLua.coerce(new SampleClass());
		LuaValue arg = CoerceJavaToLua.coerce(new Integer(123));
		LuaValue result = v.method("sample", arg);
		assertEquals("int-args 123", result.toString());
	}

	@Test
	void testMatchIntArrayArgs() {
		LuaValue v = CoerceJavaToLua.coerce(new SampleClass());
		LuaValue arg = CoerceJavaToLua.coerce(new int[] { 345, 678 });
		LuaValue result = v.method("sample", arg);
		assertEquals("int-array-args 345,678", result.toString());
	}

	@Test
	void testMatchIntArrayArrayArgs() {
		LuaValue v = CoerceJavaToLua.coerce(new SampleClass());
		LuaValue arg = CoerceJavaToLua.coerce(new int[][] { { 22, 33 }, { 44, 55 } });
		LuaValue result = v.method("sample", arg);
		assertEquals("int-array-array-args 22,33,44,55", result.toString());
	}

	public static final class SomeException extends RuntimeException {
		public SomeException(String message) {
			super(message);
		}
	}

	public static final class SomeClass {
		public static void someMethod() {
			throw new SomeException("this is some message");
		}
	}

	@Test
	void testExceptionMessage() {
		String script = "local c = luajava.bindClass( \"" + SomeClass.class.getName() + "\" )\n"
			+ "return pcall( c.someMethod, c )";
		Varargs vresult = globals.get("load").call(LuaValue.valueOf(script)).invoke(LuaValue.NONE);
		LuaValue status = vresult.arg1();
		LuaValue message = vresult.arg(2);
		assertEquals(LuaValue.FALSE, status);
		int index = message.toString().indexOf("this is some message");
		assertTrue(index >= 0, "bad message: " + message);
	}

	@Test
	void testLuaErrorCause() {
		String script = "luajava.bindClass( \"" + SomeClass.class.getName() + "\"):someMethod()";
		LuaValue chunk = globals.get("load").call(LuaValue.valueOf(script));
		try {
			chunk.invoke(LuaValue.NONE);
			fail("call should not have succeeded");
		} catch (LuaError lee) {
			Throwable c = lee.getCause();
			assertEquals(SomeException.class, c.getClass());
		}
	}

	public interface VarArgsInterface {
		public String varargsMethod(String a, String... v);

		public String arrayargsMethod(String a, String[] v);
	}

	@Test
	void testVarArgsProxy() {
		String script = "return luajava.createProxy( \"" + VarArgsInterface.class.getName() + "\", \n" + "{\n"
			+ "	varargsMethod = function(a,...)\n" + "		return table.concat({a,...},'-')\n" + "	end,\n"
			+ "	arrayargsMethod = function(a,array)\n" + "		return tostring(a)..(array and \n"
			+ "			('-'..tostring(array.length)\n" + "			..'-'..tostring(array[1])\n"
			+ "			..'-'..tostring(array[2])\n" + "			) or '-nil')\n" + "	end,\n" + "} )\n";
		Varargs chunk = globals.get("load").call(LuaValue.valueOf(script));
		if (!chunk.arg1().toboolean())
			fail(chunk.arg(2).toString());
		LuaValue result = chunk.arg1().call();
		Object u = result.touserdata();
		VarArgsInterface v = (VarArgsInterface) u;
		assertEquals("foo", v.varargsMethod("foo"));
		assertEquals("foo-bar", v.varargsMethod("foo", "bar"));
		assertEquals("foo-bar-etc", v.varargsMethod("foo", "bar", "etc"));
		assertEquals("foo-0-nil-nil", v.arrayargsMethod("foo", new String[0]));
		assertEquals("foo-1-bar-nil", v.arrayargsMethod("foo", new String[] { "bar" }));
		assertEquals("foo-2-bar-etc", v.arrayargsMethod("foo", new String[] { "bar", "etc" }));
		assertEquals("foo-3-bar-etc", v.arrayargsMethod("foo", new String[] { "bar", "etc", "etc" }));
		assertEquals("foo-nil", v.arrayargsMethod("foo", null));
	}

	@Test
	void testBigNum() {
		String script = "bigNumA = luajava.newInstance('java.math.BigDecimal','12345678901234567890');\n"
			+ "bigNumB = luajava.newInstance('java.math.BigDecimal','12345678901234567890');\n"
			+ "bigNumC = bigNumA:multiply(bigNumB);\n" +
			//"print(bigNumA:toString())\n" +
			//"print(bigNumB:toString())\n" +
			//"print(bigNumC:toString())\n" +
			"return bigNumA:toString(), bigNumB:toString(), bigNumC:toString()";
		Varargs chunk = globals.get("load").call(LuaValue.valueOf(script));
		if (!chunk.arg1().toboolean())
			fail(chunk.arg(2).toString());
		Varargs results = chunk.arg1().invoke();
		int nresults = results.narg();
		String sa = results.tojstring(1);
		String sb = results.tojstring(2);
		String sc = results.tojstring(3);
		assertEquals(3, nresults);
		assertEquals("12345678901234567890", sa);
		assertEquals("12345678901234567890", sb);
		assertEquals("152415787532388367501905199875019052100", sc);
	}

	public interface IA {
	}

	public interface IB extends IA {
	}

	public interface IC extends IB {
	}

	public static class A implements IA {
	}

	public static class B extends A implements IB {
		public String set(Object x) { return "set(Object) "; }

		public String set(String x) { return "set(String) " + x; }

		public String set(A x) { return "set(A) "; }

		public String set(B x) { return "set(B) "; }

		public String set(C x) { return "set(C) "; }

		public String set(byte x) { return "set(byte) " + x; }

		public String set(char x) { return "set(char) " + (int) x; }

		public String set(short x) { return "set(short) " + x; }

		public String set(int x) { return "set(int) " + x; }

		public String set(long x) { return "set(long) " + x; }

		public String set(float x) { return "set(float) " + x; }

		public String set(double x) { return "set(double) " + x; }

		public String setr(double x) { return "setr(double) " + x; }

		public String setr(float x) { return "setr(float) " + x; }

		public String setr(long x) { return "setr(long) " + x; }

		public String setr(int x) { return "setr(int) " + x; }

		public String setr(short x) { return "setr(short) " + x; }

		public String setr(char x) { return "setr(char) " + (int) x; }

		public String setr(byte x) { return "setr(byte) " + x; }

		public String setr(C x) { return "setr(C) "; }

		public String setr(B x) { return "setr(B) "; }

		public String setr(A x) { return "setr(A) "; }

		public String setr(String x) { return "setr(String) " + x; }

		public String setr(Object x) { return "setr(Object) "; }

		public Object getObject() { return new Object(); }

		public String getString() { return "abc"; }

		public byte[] getbytearray() { return new byte[] { 1, 2, 3 }; }

		public A getA() { return new A(); }

		public B getB() { return new B(); }

		public C getC() { return new C(); }

		public byte getbyte() { return 1; }

		public char getchar() { return 65000; }

		public short getshort() { return -32000; }

		public int getint() { return 100000; }

		public long getlong() { return 50000000000L; }

		public float getfloat() { return 6.5f; }

		public double getdouble() { return Math.PI; }
	}

	public static class C extends B implements IC {
	}

	public static class D extends C implements IA {
	}

	@Test
	void testOverloadedJavaMethodObject() { doOverloadedMethodTest("Object", ""); }

	@Test
	void testOverloadedJavaMethodString() { doOverloadedMethodTest("String", "abc"); }

	@Test
	void testOverloadedJavaMethodA() { doOverloadedMethodTest("A", ""); }

	@Test
	void testOverloadedJavaMethodB() { doOverloadedMethodTest("B", ""); }

	@Test
	void testOverloadedJavaMethodC() { doOverloadedMethodTest("C", ""); }

	@Test
	void testOverloadedJavaMethodByte() { doOverloadedMethodTest("byte", "1"); }

	@Test
	void testOverloadedJavaMethodChar() { doOverloadedMethodTest("char", "65000"); }

	@Test
	void testOverloadedJavaMethodShort() { doOverloadedMethodTest("short", "-32000"); }

	@Test
	void testOverloadedJavaMethodInt() { doOverloadedMethodTest("int", "100000"); }

	@Test
	void testOverloadedJavaMethodLong() { doOverloadedMethodTest("long", "50000000000"); }

	@Test
	void testOverloadedJavaMethodFloat() { doOverloadedMethodTest("float", "6.5"); }

	@Test
	void testOverloadedJavaMethodDouble() { doOverloadedMethodTest("double", "3.141592653589793"); }

	private void doOverloadedMethodTest(String typename, String value) {
		String script = "local a = luajava.newInstance('" + B.class.getName() + "');\n" + "local b = a:set(a:get"
			+ typename + "())\n" + "local c = a:setr(a:get" + typename + "())\n" + "return b,c";
		Varargs chunk = globals.get("load").call(LuaValue.valueOf(script));
		if (!chunk.arg1().toboolean())
			fail(chunk.arg(2).toString());
		Varargs results = chunk.arg1().invoke();
		int nresults = results.narg();
		assertEquals(2, nresults);
		LuaValue b = results.arg(1);
		LuaValue c = results.arg(2);
		String sb = b.tojstring();
		String sc = c.tojstring();
		assertEquals("set(" + typename + ") " + value, sb);
		assertEquals("setr(" + typename + ") " + value, sc);
	}

	@Test
	void testClassInheritanceLevels() {
		assertEquals(0, CoerceLuaToJava.inheritanceLevels(Object.class, Object.class));
		assertEquals(1, CoerceLuaToJava.inheritanceLevels(Object.class, String.class));
		assertEquals(1, CoerceLuaToJava.inheritanceLevels(Object.class, A.class));
		assertEquals(2, CoerceLuaToJava.inheritanceLevels(Object.class, B.class));
		assertEquals(3, CoerceLuaToJava.inheritanceLevels(Object.class, C.class));

		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(A.class, Object.class));
		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(A.class, String.class));
		assertEquals(0, CoerceLuaToJava.inheritanceLevels(A.class, A.class));
		assertEquals(1, CoerceLuaToJava.inheritanceLevels(A.class, B.class));
		assertEquals(2, CoerceLuaToJava.inheritanceLevels(A.class, C.class));

		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(B.class, Object.class));
		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(B.class, String.class));
		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(B.class, A.class));
		assertEquals(0, CoerceLuaToJava.inheritanceLevels(B.class, B.class));
		assertEquals(1, CoerceLuaToJava.inheritanceLevels(B.class, C.class));

		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(C.class, Object.class));
		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(C.class, String.class));
		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(C.class, A.class));
		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(C.class, B.class));
		assertEquals(0, CoerceLuaToJava.inheritanceLevels(C.class, C.class));
	}

	@Test
	void testInterfaceInheritanceLevels() {
		assertEquals(1, CoerceLuaToJava.inheritanceLevels(IA.class, A.class));
		assertEquals(1, CoerceLuaToJava.inheritanceLevels(IB.class, B.class));
		assertEquals(2, CoerceLuaToJava.inheritanceLevels(IA.class, B.class));
		assertEquals(1, CoerceLuaToJava.inheritanceLevels(IC.class, C.class));
		assertEquals(2, CoerceLuaToJava.inheritanceLevels(IB.class, C.class));
		assertEquals(3, CoerceLuaToJava.inheritanceLevels(IA.class, C.class));
		assertEquals(1, CoerceLuaToJava.inheritanceLevels(IA.class, D.class));
		assertEquals(2, CoerceLuaToJava.inheritanceLevels(IC.class, D.class));
		assertEquals(3, CoerceLuaToJava.inheritanceLevels(IB.class, D.class));

		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(IB.class, A.class));
		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(IC.class, A.class));
		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(IC.class, B.class));
		assertEquals(CoerceLuaToJava.SCORE_UNCOERCIBLE, CoerceLuaToJava.inheritanceLevels(IB.class, IA.class));
		assertEquals(1, CoerceLuaToJava.inheritanceLevels(IA.class, IB.class));
	}

	@Test
	void testCoerceJavaToLuaLuaValue() {
		assertSame(LuaValue.NIL, CoerceJavaToLua.coerce(LuaValue.NIL));
		assertSame(LuaValue.ZERO, CoerceJavaToLua.coerce(LuaValue.ZERO));
		assertSame(LuaValue.ONE, CoerceJavaToLua.coerce(LuaValue.ONE));
		assertSame(LuaValue.INDEX, CoerceJavaToLua.coerce(LuaValue.INDEX));
		LuaTable table = LuaValue.tableOf();
		assertSame(table, CoerceJavaToLua.coerce(table));
	}

	@Test
	void testCoerceJavaToLuaByeArray() {
		byte[] bytes = "abcd".getBytes();
		LuaValue value = CoerceJavaToLua.coerce(bytes);
		assertEquals(LuaString.class, value.getClass());
		assertEquals(LuaValue.valueOf("abcd"), value);
	}
}
