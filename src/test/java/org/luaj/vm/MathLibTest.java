package org.luaj.vm;

import junit.framework.TestCase;

import org.luaj.platform.J2meMidp10Cldc10Platform;

public class MathLibTest extends TestCase {

	public void testMathDPow() {
		assertEquals( 1, J2meMidp10Cldc10Platform.dpow(2, 0), 0 );
		assertEquals( 2, J2meMidp10Cldc10Platform.dpow(2, 1), 0 );
		assertEquals( 8, J2meMidp10Cldc10Platform.dpow(2, 3), 0 );
		assertEquals( -8, J2meMidp10Cldc10Platform.dpow(-2, 3), 0 );
		assertEquals( 1/8., J2meMidp10Cldc10Platform.dpow(2, -3), 0 );
		assertEquals( -1/8., J2meMidp10Cldc10Platform.dpow(-2, -3), 0 );
		assertEquals( 16, J2meMidp10Cldc10Platform.dpow(256,  .5), 0 );
		assertEquals(  4, J2meMidp10Cldc10Platform.dpow(256, .25), 0 );
		assertEquals( 64, J2meMidp10Cldc10Platform.dpow(256, .75), 0 );
		assertEquals( 1./16, J2meMidp10Cldc10Platform.dpow(256, - .5), 0 );
		assertEquals( 1./ 4, J2meMidp10Cldc10Platform.dpow(256, -.25), 0 );
		assertEquals( 1./64, J2meMidp10Cldc10Platform.dpow(256, -.75), 0 );
		assertEquals( Double.NaN, J2meMidp10Cldc10Platform.dpow(-256,  .5), 0 );
		assertEquals(   1, J2meMidp10Cldc10Platform.dpow(.5, 0), 0 );
		assertEquals(  .5, J2meMidp10Cldc10Platform.dpow(.5, 1), 0 );
		assertEquals(.125, J2meMidp10Cldc10Platform.dpow(.5, 3), 0 );
		assertEquals(   2, J2meMidp10Cldc10Platform.dpow(.5, -1), 0 );
		assertEquals(   8, J2meMidp10Cldc10Platform.dpow(.5, -3), 0 );
		assertEquals(1, J2meMidp10Cldc10Platform.dpow(0.0625, 0), 0 );
		assertEquals(0.00048828125, J2meMidp10Cldc10Platform.dpow(0.0625, 2.75), 0 );
	}
}
