package org.luaj.vm2.lib.jse;

import org.luaj.vm2.lib.OsLib;

import junit.framework.TestCase;

public class OsLibTest extends TestCase {

	OsLib lib;
	double time;
	
	public void setUp() {
		lib = new OsLib();
		time = new java.util.Date(2001-1900, 7, 23, 14, 55, 02).getTime() / 1000.0;
	}

	void t(String format, String expected) {
		String actual = lib.date(format, time);
		assertEquals(expected, actual);
	}
	
	public void testStringDateChars() { t("foo", "foo"); } 
	public void testStringDate_a() { t("%a", "Thu"); } 
	public void testStringDate_A() { t("%A", "Thursday"); } 
	public void testStringDate_b() { t("%b", "Aug"); } 
	public void testStringDate_B() { t("%B", "August"); } 
	public void testStringDate_c() { t("%c", "Thu Aug 23 14:55:02 2001"); } 
	public void testStringDate_C() { t("%C", "20"); } 
	public void testStringDate_d() { t("%d", "23"); } 
	public void testStringDate_D() { t("%D", "08/23/01"); } 
	public void testStringDate_e() { t("%e", "23"); } 
	public void testStringDate_F() { t("%F", "2001-08-23"); } 
	public void testStringDate_g() { t("%g", "%g"); } // not implemented.
	public void testStringDate_G() { t("%G", "%G"); } // not implemented.
	public void testStringDate_h() { t("%h", "Aug"); } 
	public void testStringDate_H() { t("%H", "14"); } 
	public void testStringDate_I() { t("%I", "02"); } 
	public void testStringDate_j() { t("%j", "235"); } 
	public void testStringDate_m() { t("%m", "08"); } 
	public void testStringDate_M() { t("%M", "55"); } 
	public void testStringDate_n() { t("%n", "\n"); } 
	public void testStringDate_p() { t("%p", "PM"); } 
	public void testStringDate_r() { t("%r", "02:55:02 pm"); } 
	public void testStringDate_R() { t("%R", "14:55"); } 
	public void testStringDate_S() { t("%S", "02"); } 
	public void testStringDate_t() { t("%t", "\t"); } 
	public void testStringDate_T() { t("%T", "14:55:02"); } 
	public void testStringDate_u() { t("%u", "4"); } 
	public void testStringDate_U() { t("%U", "33"); }
	public void testStringDate_V() { t("%V", "%V"); } // not implemented.
	public void testStringDate_w() { t("%w", "4"); } 
	public void testStringDate_W() { t("%W", "34"); } 
	public void testStringDate_x() { t("%x", "08/23/01"); } 
	public void testStringDate_X() { t("%X", "14:55:02"); } 
	public void testStringDate_y() { t("%y", "01"); } 
	public void testStringDate_Y() { t("%Y", "2001"); } 
	public void testStringDate_z() { t("%z", "-480"); } 
	public void testStringDate_Z() { t("%Z", ""); } 
	public void testStringDate_Pct() { t("%%", "%"); } 

	static final double DAY = 24. * 3600.;
	public void testStringDate_UW_neg4() { time-=4*DAY; t("%c %U %W", "Sun Aug 19 14:55:02 2001 33 33"); } 
	public void testStringDate_UW_neg3() { time-=3*DAY; t("%c %U %W", "Mon Aug 20 14:55:02 2001 33 34"); } 
	public void testStringDate_UW_neg2() { time-=2*DAY; t("%c %U %W", "Tue Aug 21 14:55:02 2001 33 34"); } 
	public void testStringDate_UW_neg1() { time-=DAY; t("%c %U %W", "Wed Aug 22 14:55:02 2001 33 34"); } 
	public void testStringDate_UW_pos0() { time+=0; t("%c %U %W", "Thu Aug 23 14:55:02 2001 33 34"); } 
	public void testStringDate_UW_pos1() { time+=DAY; t("%c %U %W", "Fri Aug 24 14:55:02 2001 33 34"); } 
	public void testStringDate_UW_pos2() { time+=2*DAY; t("%c %U %W", "Sat Aug 25 14:55:02 2001 33 34"); } 
	public void testStringDate_UW_pos3() { time+=3*DAY; t("%c %U %W", "Sun Aug 26 14:55:02 2001 34 34"); } 
	public void testStringDate_UW_pos4() { time+=4*DAY; t("%c %U %W", "Mon Aug 27 14:55:02 2001 34 35"); } 
}
