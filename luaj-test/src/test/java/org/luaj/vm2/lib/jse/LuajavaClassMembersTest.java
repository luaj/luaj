package org.luaj.vm2.lib.jse;

import junit.framework.TestCase;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;

public class LuajavaClassMembersTest extends TestCase {
	public static class A {
		protected A() {}
	}

	public static class B extends A {
		public byte   m_byte_field;
		public int    m_int_field;
		public double m_double_field;
		public String m_string_field;

		protected B() {}

		public B(int i) { m_int_field = i; }

		public String setString(String x) { return "setString(String) " + x; }

		public String getString() { return "abc"; }

		public int getint() { return 100000; }

		public String uniq() { return "uniq()"; }

		public String uniqs(String s) { return "uniqs(string:" + s + ")"; }

		public String uniqi(int i) { return "uniqi(int:" + i + ")"; }

		public String uniqsi(String s, int i) { return "uniqsi(string:" + s + ",int:" + i + ")"; }

		public String uniqis(int i, String s) { return "uniqis(int:" + i + ",string:" + s + ")"; }

		public String pick() { return "pick()"; }

		public String pick(String s) { return "pick(string:" + s + ")"; }

		public String pick(int i) { return "pick(int:" + i + ")"; }

		public String pick(String s, int i) { return "pick(string:" + s + ",int:" + i + ")"; }

		public String pick(int i, String s) { return "pick(int:" + i + ",string:" + s + ")"; }

		public static String staticpick() { return "static-pick()"; }

		public static String staticpick(String s) { return "static-pick(string:" + s + ")"; }

		public static String staticpick(int i) { return "static-pick(int:" + i + ")"; }

		public static String staticpick(String s, int i) { return "static-pick(string:" + s + ",int:" + i + ")"; }

		public static String staticpick(int i, String s) { return "static-pick(int:" + i + ",string:" + s + ")"; }
	}

	public static class C extends B {
		public C() {}

		public C(String s) { m_string_field = s; }

		public C(int i) { m_int_field = i; }

		public C(String s, int i) { m_string_field = s; m_int_field = i; }

		public int getint() { return 200000; }

		public String pick(String s) { return "class-c-pick(string:" + s + ")"; }

		public String pick(int i) { return "class-c-pick(int:" + i + ")"; }

		public static class D {
			public static String name() { return "name-of-D"; }
		}
	}

	static LuaValue ZERO  = LuaValue.ZERO;
	static LuaValue ONE   = LuaValue.ONE;
	static LuaValue PI    = LuaValue.valueOf(Math.PI);
	static LuaValue THREE = LuaValue.valueOf(3);
	static LuaValue NUMS  = LuaValue.valueOf(123);
	static LuaValue ABC   = LuaValue.valueOf("abc");
	static LuaValue SOMEA = CoerceJavaToLua.coerce(new A());
	static LuaValue SOMEB = CoerceJavaToLua.coerce(new B());
	static LuaValue SOMEC = CoerceJavaToLua.coerce(new C());

	public void testSetByteField() {
		B b = new B();
		JavaInstance i = new JavaInstance(b);
		i.set("m_byte_field", ONE);
		assertEquals(1, b.m_byte_field);
		assertEquals(ONE, i.get("m_byte_field"));
		i.set("m_byte_field", PI);
		assertEquals(3, b.m_byte_field);
		assertEquals(THREE, i.get("m_byte_field"));
		i.set("m_byte_field", ABC);
		assertEquals(0, b.m_byte_field);
		assertEquals(ZERO, i.get("m_byte_field"));
	}

	public void testSetDoubleField() {
		B b = new B();
		JavaInstance i = new JavaInstance(b);
		i.set("m_double_field", ONE);
		assertEquals(1., b.m_double_field);
		assertEquals(ONE, i.get("m_double_field"));
		i.set("m_double_field", PI);
		assertEquals(Math.PI, b.m_double_field);
		assertEquals(PI, i.get("m_double_field"));
		i.set("m_double_field", ABC);
		assertEquals(0., b.m_double_field);
		assertEquals(ZERO, i.get("m_double_field"));
	}

	public void testNoFactory() {
		JavaClass c = JavaClass.forClass(A.class);
		try {
			c.call();
			fail("did not throw lua error as expected");
		} catch (LuaError e) {
		}
	}

	public void testUniqueFactoryCoercible() {
		JavaClass c = JavaClass.forClass(B.class);
		assertEquals(JavaClass.class, c.getClass());
		LuaValue constr = c.get("new");
		assertEquals(JavaConstructor.class, constr.getClass());
		LuaValue v = constr.call(NUMS);
		Object b = v.touserdata();
		assertEquals(B.class, b.getClass());
		assertEquals(123, ((B) b).m_int_field);
		Object b0 = constr.call().touserdata();
		assertEquals(B.class, b0.getClass());
		assertEquals(0, ((B) b0).m_int_field);
	}

	public void testUniqueFactoryUncoercible() {
		JavaClass f = JavaClass.forClass(B.class);
		LuaValue constr = f.get("new");
		assertEquals(JavaConstructor.class, constr.getClass());
		try {
			LuaValue v = constr.call(LuaValue.userdataOf(new Object()));
			Object b = v.touserdata();
			// fail( "did not throw lua error as expected" );
			assertEquals(0, ((B) b).m_int_field);
		} catch (LuaError e) {
		}
	}

	public void testOverloadedFactoryCoercible() {
		JavaClass f = JavaClass.forClass(C.class);
		LuaValue constr = f.get("new");
		assertEquals(JavaConstructor.Overload.class, constr.getClass());
		Object c = constr.call().touserdata();
		Object ci = constr.call(LuaValue.valueOf(123)).touserdata();
		Object cs = constr.call(LuaValue.valueOf("abc")).touserdata();
		Object csi = constr.call(LuaValue.valueOf("def"), LuaValue.valueOf(456)).touserdata();
		assertEquals(C.class, c.getClass());
		assertEquals(C.class, ci.getClass());
		assertEquals(C.class, cs.getClass());
		assertEquals(C.class, csi.getClass());
		assertEquals(null, ((C) c).m_string_field);
		assertEquals(0, ((C) c).m_int_field);
		assertEquals("abc", ((C) cs).m_string_field);
		assertEquals(0, ((C) cs).m_int_field);
		assertEquals(null, ((C) ci).m_string_field);
		assertEquals(123, ((C) ci).m_int_field);
		assertEquals("def", ((C) csi).m_string_field);
		assertEquals(456, ((C) csi).m_int_field);
	}

	public void testOverloadedFactoryUncoercible() {
		JavaClass f = JavaClass.forClass(C.class);
		try {
			Object c = f.call(LuaValue.userdataOf(new Object()));
			// fail( "did not throw lua error as expected" );
			assertEquals(0, ((C) c).m_int_field);
			assertEquals(null, ((C) c).m_string_field);
		} catch (LuaError e) {
		}
	}

	public void testNoAttribute() {
		JavaClass f = JavaClass.forClass(A.class);
		LuaValue v = f.get("bogus");
		assertEquals(v, LuaValue.NIL);
		try {
			f.set("bogus", ONE);
			fail("did not throw lua error as expected");
		} catch (LuaError e) {
		}
	}

	public void testFieldAttributeCoercible() {
		JavaInstance i = new JavaInstance(new B());
		i.set("m_int_field", ONE);
		assertEquals(1, i.get("m_int_field").toint());
		i.set("m_int_field", THREE);
		assertEquals(3, i.get("m_int_field").toint());
		i = new JavaInstance(new C());
		i.set("m_int_field", ONE);
		assertEquals(1, i.get("m_int_field").toint());
		i.set("m_int_field", THREE);
		assertEquals(3, i.get("m_int_field").toint());
	}

	public void testUniqueMethodAttributeCoercible() {
		B b = new B();
		JavaInstance ib = new JavaInstance(b);
		LuaValue b_getString = ib.get("getString");
		LuaValue b_getint = ib.get("getint");
		assertEquals(JavaMethod.class, b_getString.getClass());
		assertEquals(JavaMethod.class, b_getint.getClass());
		assertEquals("abc", b_getString.call(SOMEB).tojstring());
		assertEquals(100000, b_getint.call(SOMEB).toint());
		assertEquals("abc", b_getString.call(SOMEC).tojstring());
		assertEquals(200000, b_getint.call(SOMEC).toint());
	}

	public void testUniqueMethodAttributeArgsCoercible() {
		B b = new B();
		JavaInstance ib = new JavaInstance(b);
		LuaValue uniq = ib.get("uniq");
		LuaValue uniqs = ib.get("uniqs");
		LuaValue uniqi = ib.get("uniqi");
		LuaValue uniqsi = ib.get("uniqsi");
		LuaValue uniqis = ib.get("uniqis");
		assertEquals(JavaMethod.class, uniq.getClass());
		assertEquals(JavaMethod.class, uniqs.getClass());
		assertEquals(JavaMethod.class, uniqi.getClass());
		assertEquals(JavaMethod.class, uniqsi.getClass());
		assertEquals(JavaMethod.class, uniqis.getClass());
		assertEquals("uniq()", uniq.call(SOMEB).tojstring());
		assertEquals("uniqs(string:abc)", uniqs.call(SOMEB, ABC).tojstring());
		assertEquals("uniqi(int:1)", uniqi.call(SOMEB, ONE).tojstring());
		assertEquals("uniqsi(string:abc,int:1)", uniqsi.call(SOMEB, ABC, ONE).tojstring());
		assertEquals("uniqis(int:1,string:abc)", uniqis.call(SOMEB, ONE, ABC).tojstring());
		assertEquals("uniqis(int:1,string:abc)",
			uniqis.invoke(LuaValue.varargsOf(new LuaValue[] { SOMEB, ONE, ABC, ONE })).arg1().tojstring());
	}

	public void testOverloadedMethodAttributeCoercible() {
		B b = new B();
		JavaInstance ib = new JavaInstance(b);
		LuaValue p = ib.get("pick");
		assertEquals("pick()", p.call(SOMEB).tojstring());
		assertEquals("pick(string:abc)", p.call(SOMEB, ABC).tojstring());
		assertEquals("pick(int:1)", p.call(SOMEB, ONE).tojstring());
		assertEquals("pick(string:abc,int:1)", p.call(SOMEB, ABC, ONE).tojstring());
		assertEquals("pick(int:1,string:abc)", p.call(SOMEB, ONE, ABC).tojstring());
		assertEquals("pick(int:1,string:abc)",
			p.invoke(LuaValue.varargsOf(new LuaValue[] { SOMEB, ONE, ABC, ONE })).arg1().tojstring());
	}

	public void testUnboundOverloadedMethodAttributeCoercible() {
		B b = new B();
		JavaInstance ib = new JavaInstance(b);
		LuaValue p = ib.get("pick");
		assertEquals(JavaMethod.Overload.class, p.getClass());
		assertEquals("pick()", p.call(SOMEC).tojstring());
		assertEquals("class-c-pick(string:abc)", p.call(SOMEC, ABC).tojstring());
		assertEquals("class-c-pick(int:1)", p.call(SOMEC, ONE).tojstring());
		assertEquals("pick(string:abc,int:1)", p.call(SOMEC, ABC, ONE).tojstring());
		assertEquals("pick(int:1,string:abc)", p.call(SOMEC, ONE, ABC).tojstring());
		assertEquals("pick(int:1,string:abc)",
			p.invoke(LuaValue.varargsOf(new LuaValue[] { SOMEC, ONE, ABC, ONE })).arg1().tojstring());
	}

	public void testOverloadedStaticMethodAttributeCoercible() {
		B b = new B();
		JavaInstance ib = new JavaInstance(b);
		LuaValue p = ib.get("staticpick");
		assertEquals("static-pick()", p.call(SOMEB).tojstring());
		assertEquals("static-pick(string:abc)", p.call(SOMEB, ABC).tojstring());
		assertEquals("static-pick(int:1)", p.call(SOMEB, ONE).tojstring());
		assertEquals("static-pick(string:abc,int:1)", p.call(SOMEB, ABC, ONE).tojstring());
		assertEquals("static-pick(int:1,string:abc)", p.call(SOMEB, ONE, ABC).tojstring());
		assertEquals("static-pick(int:1,string:abc)",
			p.invoke(LuaValue.varargsOf(new LuaValue[] { SOMEB, ONE, ABC, ONE })).arg1().tojstring());
	}

	public void testGetInnerClass() {
		C c = new C();
		JavaInstance ic = new JavaInstance(c);
		LuaValue d = ic.get("D");
		assertFalse(d.isnil());
		assertSame(d, JavaClass.forClass(C.D.class));
		LuaValue e = ic.get("E");
		assertTrue(e.isnil());
	}
}
