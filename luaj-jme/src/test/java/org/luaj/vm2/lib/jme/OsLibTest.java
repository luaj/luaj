package org.luaj.vm2.lib.jme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.LuaValue;

class OsLibTest {

	LuaValue jme_lib;
	double   time;

	@BeforeEach
	public void setUp() {
		jme_lib = JmePlatform.standardGlobals().get("os");
		time = 998571302000L/1000.0;
	}

	void test(String format, String expected) {
		String actual = jme_lib.get("date").call(LuaValue.valueOf(format), LuaValue.valueOf(time)).tojstring();
		assertEquals(expected, actual);
	}

	@Test
	void testStringDateChars() { test("foo", "foo"); }

	@Test
	void testStringDate_a() { test("%a", "Thu"); }

	@Test
	void testStringDate_A() { test("%A", "Thursday"); }

	@Test
	void testStringDate_b() { test("%b", "Aug"); }

	@Test
	void testStringDate_B() { test("%B", "August"); }

	@Test
	void testStringDate_c() { test("%c", "Thu Aug 23 14:55:02 2001"); }

	@Test
	void testStringDate_d() { test("%d", "23"); }

	@Test
	void testStringDate_H() { test("%H", "14"); }

	@Test
	void testStringDate_I() { test("%I", "02"); }

	@Test
	void testStringDate_j() { test("%j", "235"); }

	@Test
	void testStringDate_m() { test("%m", "08"); }

	@Test
	void testStringDate_M() { test("%M", "55"); }

	@Test
	void testStringDate_p() { test("%p", "PM"); }

	@Test
	void testStringDate_S() { test("%S", "02"); }

	@Test
	void testStringDate_U() { test("%U", "33"); }

	@Test
	void testStringDate_w() { test("%w", "4"); }

	@Test
	void testStringDate_W() { test("%W", "34"); }

	@Test
	void testStringDate_x() { test("%x", "08/23/01"); }

	@Test
	void testStringDate_X() { test("%X", "14:55:02"); }

	@Test
	void testStringDate_y() { test("%y", "01"); }

	@Test
	void testStringDate_Y() { test("%Y", "2001"); }

	@Test
	void testStringDate_Pct() { test("%%", "%"); }

	static final double DAY = 24.*3600.;

	@Test
	void testStringDate_UW_neg4() { time -= 4*DAY; test("%c %U %W", "Sun Aug 19 14:55:02 2001 33 33"); }

	@Test
	void testStringDate_UW_neg3() { time -= 3*DAY; test("%c %U %W", "Mon Aug 20 14:55:02 2001 33 34"); }

	@Test
	void testStringDate_UW_neg2() { time -= 2*DAY; test("%c %U %W", "Tue Aug 21 14:55:02 2001 33 34"); }

	@Test
	void testStringDate_UW_neg1() { time -= DAY; test("%c %U %W", "Wed Aug 22 14:55:02 2001 33 34"); }

	@Test
	void testStringDate_UW_pos0() { time += 0; test("%c %U %W", "Thu Aug 23 14:55:02 2001 33 34"); }

	@Test
	void testStringDate_UW_pos1() { time += DAY; test("%c %U %W", "Fri Aug 24 14:55:02 2001 33 34"); }

	@Test
	void testStringDate_UW_pos2() { time += 2*DAY; test("%c %U %W", "Sat Aug 25 14:55:02 2001 33 34"); }

	@Test
	void testStringDate_UW_pos3() { time += 3*DAY; test("%c %U %W", "Sun Aug 26 14:55:02 2001 34 34"); }

	@Test
	void testStringDate_UW_pos4() { time += 4*DAY; test("%c %U %W", "Mon Aug 27 14:55:02 2001 34 35"); }

	@Test
	void testJseOsGetenvForEnvVariables() {
		LuaValue USER = LuaValue.valueOf("USER");
		LuaValue jme_user = jme_lib.get("getenv").call(USER);
		assertTrue(jme_user.isnil());
		System.out.println("User: " + jme_user);
	}

	void testJseOsGetenvForSystemProperties() {
//		System.setProperty("test.key.foo", "test.value.bar");
		LuaValue key = LuaValue.valueOf("test.key.foo");
		LuaValue value = LuaValue.valueOf("test.value.bar");
		LuaValue jme_value = jme_lib.get("getenv").call(key);
		assertEquals(value, jme_value);
	}
}
