package org.luaj.vm2.lib.jse;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OsLib;
import org.luaj.vm2.lib.jme.JmePlatform;

import junit.framework.TestCase;

public class OsLibTest extends TestCase {

	LuaValue jme_lib;
	LuaValue jse_lib;
	double   time;

	public void setUp() {
		jse_lib = JsePlatform.standardGlobals().get("os");
		;
		jme_lib = JmePlatform.standardGlobals().get("os");
		;
		time = new java.util.Date(2001-1900, 7, 23, 14, 55, 02).getTime()/1000.0;
	}

	void t(String format, String expected) {
		String actual = jme_lib.get("date").call(LuaValue.valueOf(format), LuaValue.valueOf(time)).tojstring();
		assertEquals(expected, actual);
	}

	public void testStringDateChars() { t("foo", "foo"); }

	public void testStringDate_a() { t("%a", "Thu"); }

	public void testStringDate_A() { t("%A", "Thursday"); }

	public void testStringDate_b() { t("%b", "Aug"); }

	public void testStringDate_B() { t("%B", "August"); }

	public void testStringDate_c() { t("%c", "Thu Aug 23 14:55:02 2001"); }

	public void testStringDate_d() { t("%d", "23"); }

	public void testStringDate_H() { t("%H", "14"); }

	public void testStringDate_I() { t("%I", "02"); }

	public void testStringDate_j() { t("%j", "235"); }

	public void testStringDate_m() { t("%m", "08"); }

	public void testStringDate_M() { t("%M", "55"); }

	public void testStringDate_p() { t("%p", "PM"); }

	public void testStringDate_S() { t("%S", "02"); }

	public void testStringDate_U() { t("%U", "33"); }

	public void testStringDate_w() { t("%w", "4"); }

	public void testStringDate_W() { t("%W", "34"); }

	public void testStringDate_x() { t("%x", "08/23/01"); }

	public void testStringDate_X() { t("%X", "14:55:02"); }

	public void testStringDate_y() { t("%y", "01"); }

	public void testStringDate_Y() { t("%Y", "2001"); }

	public void testStringDate_Pct() { t("%%", "%"); }

	static final double DAY = 24.*3600.;

	public void testStringDate_UW_neg4() { time -= 4*DAY; t("%c %U %W", "Sun Aug 19 14:55:02 2001 33 33"); }

	public void testStringDate_UW_neg3() { time -= 3*DAY; t("%c %U %W", "Mon Aug 20 14:55:02 2001 33 34"); }

	public void testStringDate_UW_neg2() { time -= 2*DAY; t("%c %U %W", "Tue Aug 21 14:55:02 2001 33 34"); }

	public void testStringDate_UW_neg1() { time -= DAY; t("%c %U %W", "Wed Aug 22 14:55:02 2001 33 34"); }

	public void testStringDate_UW_pos0() { time += 0; t("%c %U %W", "Thu Aug 23 14:55:02 2001 33 34"); }

	public void testStringDate_UW_pos1() { time += DAY; t("%c %U %W", "Fri Aug 24 14:55:02 2001 33 34"); }

	public void testStringDate_UW_pos2() { time += 2*DAY; t("%c %U %W", "Sat Aug 25 14:55:02 2001 33 34"); }

	public void testStringDate_UW_pos3() { time += 3*DAY; t("%c %U %W", "Sun Aug 26 14:55:02 2001 34 34"); }

	public void testStringDate_UW_pos4() { time += 4*DAY; t("%c %U %W", "Mon Aug 27 14:55:02 2001 34 35"); }

	public void testJseOsGetenvForEnvVariables() {
		LuaValue USER = LuaValue.valueOf("USER");
		LuaValue jse_user = jse_lib.get("getenv").call(USER);
		LuaValue jme_user = jme_lib.get("getenv").call(USER);
		assertFalse(jse_user.isnil());
		assertTrue(jme_user.isnil());
		System.out.println("User: " + jse_user);
	}

	public void testJseOsGetenvForSystemProperties() {
		System.setProperty("test.key.foo", "test.value.bar");
		LuaValue key = LuaValue.valueOf("test.key.foo");
		LuaValue value = LuaValue.valueOf("test.value.bar");
		LuaValue jse_value = jse_lib.get("getenv").call(key);
		LuaValue jme_value = jme_lib.get("getenv").call(key);
		assertEquals(value, jse_value);
		assertEquals(value, jme_value);
	}
}
