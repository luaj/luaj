package org.luaj.vm;

import junit.framework.TestCase;

public class MathLibTest extends TestCase {

	public void testMathDPow() {
		assertEquals( 1, LDouble.dpow(2, 0), 0 );
		assertEquals( 2, LDouble.dpow(2, 1), 0 );
		assertEquals( 8, LDouble.dpow(2, 3), 0 );
		assertEquals( -8, LDouble.dpow(-2, 3), 0 );
		assertEquals( 1/8., LDouble.dpow(2, -3), 0 );
		assertEquals( -1/8., LDouble.dpow(-2, -3), 0 );
		assertEquals( 16, LDouble.dpow(256,  .5), 0 );
		assertEquals(  4, LDouble.dpow(256, .25), 0 );
		assertEquals( 64, LDouble.dpow(256, .75), 0 );
		assertEquals( 1./16, LDouble.dpow(256, - .5), 0 );
		assertEquals( 1./ 4, LDouble.dpow(256, -.25), 0 );
		assertEquals( 1./64, LDouble.dpow(256, -.75), 0 );
		assertEquals( Double.NaN, LDouble.dpow(-256,  .5), 0 );
		assertEquals(   1, LDouble.dpow(.5, 0), 0 );
		assertEquals(  .5, LDouble.dpow(.5, 1), 0 );
		assertEquals(.125, LDouble.dpow(.5, 3), 0 );
		assertEquals(   2, LDouble.dpow(.5, -1), 0 );
		assertEquals(   8, LDouble.dpow(.5, -3), 0 );
		assertEquals(1, LDouble.dpow(0.0625, 0), 0 );
		assertEquals(0.00048828125, LDouble.dpow(0.0625, 2.75), 0 );
	}
}
