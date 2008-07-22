package org.luaj.vm;

import junit.framework.TestCase;

import org.luaj.lib.MathLib;
import org.luaj.platform.J2meMidp20Cldc11Platform;
import org.luaj.platform.J2sePlatform;

public class MathLibTest extends TestCase {

	private Platform j2se;
	private Platform j2me;
	private boolean supportedOnJ2me;
	
	protected void setUp() throws Exception {
		j2se = new J2sePlatform();
		j2me = new org.luaj.platform.J2meMidp20Cldc11Platform(null);
		supportedOnJ2me = true;
	}

	public void testMathDPow() {
		assertEquals( 1, J2meMidp20Cldc11Platform.dpow(2, 0), 0 );
		assertEquals( 2, J2meMidp20Cldc11Platform.dpow(2, 1), 0 );
		assertEquals( 8, J2meMidp20Cldc11Platform.dpow(2, 3), 0 );
		assertEquals( -8, J2meMidp20Cldc11Platform.dpow(-2, 3), 0 );
		assertEquals( 1/8., J2meMidp20Cldc11Platform.dpow(2, -3), 0 );
		assertEquals( -1/8., J2meMidp20Cldc11Platform.dpow(-2, -3), 0 );
		assertEquals( 16, J2meMidp20Cldc11Platform.dpow(256,  .5), 0 );
		assertEquals(  4, J2meMidp20Cldc11Platform.dpow(256, .25), 0 );
		assertEquals( 64, J2meMidp20Cldc11Platform.dpow(256, .75), 0 );
		assertEquals( 1./16, J2meMidp20Cldc11Platform.dpow(256, - .5), 0 );
		assertEquals( 1./ 4, J2meMidp20Cldc11Platform.dpow(256, -.25), 0 );
		assertEquals( 1./64, J2meMidp20Cldc11Platform.dpow(256, -.75), 0 );
		assertEquals( Double.NaN, J2meMidp20Cldc11Platform.dpow(-256,  .5), 0 );
		assertEquals(   1, J2meMidp20Cldc11Platform.dpow(.5, 0), 0 );
		assertEquals(  .5, J2meMidp20Cldc11Platform.dpow(.5, 1), 0 );
		assertEquals(.125, J2meMidp20Cldc11Platform.dpow(.5, 3), 0 );
		assertEquals(   2, J2meMidp20Cldc11Platform.dpow(.5, -1), 0 );
		assertEquals(   8, J2meMidp20Cldc11Platform.dpow(.5, -3), 0 );
		assertEquals(1, J2meMidp20Cldc11Platform.dpow(0.0625, 0), 0 );
		assertEquals(0.00048828125, J2meMidp20Cldc11Platform.dpow(0.0625, 2.75), 0 );
	}
	
	public void testAbs() {
		tryMathOp( MathLib.ABS, 23.45 ); 
		tryMathOp( MathLib.ABS, -23.45 ); 
	}

	public void testCos() {
		tryTrigOps( MathLib.COS );
	}
	
	public void testCosh() {
		supportedOnJ2me = false;
		tryTrigOps( MathLib.COSH );
	}
	
	public void testDeg() {
		tryTrigOps( MathLib.DEG );
	}
	
	public void testExp() {
		supportedOnJ2me = false;
		tryMathOp( MathLib.EXP, 0 ); 
		tryMathOp( MathLib.EXP, 0.1 ); 
		tryMathOp( MathLib.EXP, .9 ); 
		tryMathOp( MathLib.EXP, 1. ); 
		tryMathOp( MathLib.EXP, 9 ); 
		tryMathOp( MathLib.EXP, -.1 ); 
		tryMathOp( MathLib.EXP, -.9 ); 
		tryMathOp( MathLib.EXP, -1. ); 
		tryMathOp( MathLib.EXP, -9 ); 
	}
	
	public void testLog() {
		supportedOnJ2me = false;
		tryMathOp( MathLib.LOG, 0.1 ); 
		tryMathOp( MathLib.LOG, .9 ); 
		tryMathOp( MathLib.LOG, 1. ); 
		tryMathOp( MathLib.LOG, 9 ); 
		tryMathOp( MathLib.LOG, -.1 ); 
		tryMathOp( MathLib.LOG, -.9 ); 
		tryMathOp( MathLib.LOG, -1. ); 
		tryMathOp( MathLib.LOG, -9 ); 
	}
	
	public void testLog10() {
		supportedOnJ2me = false;
		tryMathOp( MathLib.LOG10, 0.1 ); 
		tryMathOp( MathLib.LOG10, .9 ); 
		tryMathOp( MathLib.LOG10, 1. ); 
		tryMathOp( MathLib.LOG10, 9 ); 
		tryMathOp( MathLib.LOG10, 10 ); 
		tryMathOp( MathLib.LOG10, 100 ); 
		tryMathOp( MathLib.LOG10, -.1 ); 
		tryMathOp( MathLib.LOG10, -.9 ); 
		tryMathOp( MathLib.LOG10, -1. ); 
		tryMathOp( MathLib.LOG10, -9 ); 
		tryMathOp( MathLib.LOG10, -10 ); 
		tryMathOp( MathLib.LOG10, -100 ); 
	}
	
	public void testRad() {
		tryMathOp( MathLib.RAD, 0 ); 
		tryMathOp( MathLib.RAD, 0.1 ); 
		tryMathOp( MathLib.RAD, .9 ); 
		tryMathOp( MathLib.RAD, 1. ); 
		tryMathOp( MathLib.RAD, 9 ); 
		tryMathOp( MathLib.RAD, 10 ); 
		tryMathOp( MathLib.RAD, 100 ); 
		tryMathOp( MathLib.RAD, -.1 ); 
		tryMathOp( MathLib.RAD, -.9 ); 
		tryMathOp( MathLib.RAD, -1. ); 
		tryMathOp( MathLib.RAD, -9 ); 
		tryMathOp( MathLib.RAD, -10 ); 
		tryMathOp( MathLib.RAD, -100 ); 
	}
	
	public void testSin() {
		tryTrigOps( MathLib.SIN );
	}
	
	public void testSinh() {
		supportedOnJ2me = false;
		tryTrigOps( MathLib.SINH );
	}
	
	public void testSqrt() {
		tryMathOp( MathLib.SQRT, 0 ); 
		tryMathOp( MathLib.SQRT, 0.1 ); 
		tryMathOp( MathLib.SQRT, .9 ); 
		tryMathOp( MathLib.SQRT, 1. ); 
		tryMathOp( MathLib.SQRT, 9 ); 
		tryMathOp( MathLib.SQRT, 10 ); 
		tryMathOp( MathLib.SQRT, 100 );
	}
	public void testTan() {
		tryTrigOps( MathLib.TAN );
	}
	
	public void testTanh() {
		supportedOnJ2me = false;
		tryTrigOps( MathLib.TANH );
	}
	
	public void testAtan2() {
		supportedOnJ2me = false;
		tryDoubleOps( MathLib.ATAN2, false );
	}
	
	public void testFmod() {
		tryDoubleOps( MathLib.FMOD, false );
	}
	
	public void testPow() {
		tryDoubleOps( MathLib.POW, true );
	}
	
	private void tryDoubleOps( int id, boolean positiveOnly ) {
		// y>0, x>0
		tryMathOp( id, 0.1, 4.0 ); 
		tryMathOp( id, .9, 4.0 ); 
		tryMathOp( id, 1., 4.0 ); 
		tryMathOp( id, 9, 4.0 ); 
		tryMathOp( id, 10, 4.0 ); 
		tryMathOp( id, 100, 4.0 );
		
		// y>0, x<0
		tryMathOp( id, 0.1, -4.0 ); 
		tryMathOp( id, .9, -4.0 ); 
		tryMathOp( id, 1., -4.0 ); 
		tryMathOp( id, 9, -4.0 ); 
		tryMathOp( id, 10, -4.0 ); 
		tryMathOp( id, 100, -4.0 );
		
		if ( ! positiveOnly ) {
			// y<0, x>0
			tryMathOp( id, -0.1, 4.0 ); 
			tryMathOp( id, -.9, 4.0 ); 
			tryMathOp( id, -1., 4.0 ); 
			tryMathOp( id, -9, 4.0 ); 
			tryMathOp( id, -10, 4.0 ); 
			tryMathOp( id, -100, 4.0 );
			
			// y<0, x<0
			tryMathOp( id, -0.1, -4.0 ); 
			tryMathOp( id, -.9, -4.0 ); 
			tryMathOp( id, -1., -4.0 ); 
			tryMathOp( id, -9, -4.0 ); 
			tryMathOp( id, -10, -4.0 ); 
			tryMathOp( id, -100, -4.0 );
		}
		
		// degenerate cases
		tryMathOp( id, 0, 1 ); 
		tryMathOp( id, 1, 0 ); 
		tryMathOp( id, -1, 0 ); 
		tryMathOp( id, 0, -1 ); 
		tryMathOp( id, 0, 0 ); 
	}
	
	private void tryTrigOps(int id) {
		tryMathOp( id, 0 ); 
		tryMathOp( id, Math.PI/8 ); 
		tryMathOp( id, Math.PI*7/8 ); 
		tryMathOp( id, Math.PI*8/8 ); 
		tryMathOp( id, Math.PI*9/8 ); 
		tryMathOp( id, -Math.PI/8 ); 
		tryMathOp( id, -Math.PI*7/8 ); 
		tryMathOp( id, -Math.PI*8/8 ); 
		tryMathOp( id, -Math.PI*9/8 ); 
	}
	
	private void tryMathOp(int id, double x) {		
		try {
			double expected = j2se.mathop(id, LDouble.valueOf(x)).toJavaDouble();
			double actual = j2me.mathop(id, LDouble.valueOf(x)).toJavaDouble();
			if ( supportedOnJ2me )
				assertEquals( expected, actual, 1.e-5 );
			else
				this.fail("j2me should throw exception for math op "+id+" but returned "+actual);
		} catch ( LuaErrorException lee ) {
			if ( supportedOnJ2me )
				throw lee;
		}
	}
	
	
	private void tryMathOp(int id, double a, double b) {
		try {
			double expected = j2se.mathop(id, LDouble.valueOf(a), LDouble.valueOf(b)).toJavaDouble();
			double actual = j2me.mathop(id, LDouble.valueOf(a), LDouble.valueOf(b)).toJavaDouble();
			if ( supportedOnJ2me )
				assertEquals( expected, actual, 1.e-5 );
			else
				this.fail("j2me should throw exception for math op "+id+" but returned "+actual);
		} catch ( LuaErrorException lee ) {
			if ( supportedOnJ2me )
				throw lee;
		}
	}	
}
