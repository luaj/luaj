package org.luaj.vm;

import java.util.Random;

import org.luaj.vm.LDouble;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNumber;
import org.luaj.vm.LoadState;

import junit.framework.TestCase;

public class LoadStateTest extends TestCase {
	double[] DOUBLE_VALUES = {
			0.0,
			1.0,
			2.5,
			10.0,
			16.0,
			16.125,
			-1.0,
			2.0,
			-2.0,
			-10.0,
			-0.25,
			-25,
			Integer.MAX_VALUE,
			Integer.MAX_VALUE - 1,
			(double)Integer.MAX_VALUE + 1.0,
			Integer.MIN_VALUE,
			Integer.MIN_VALUE + 1,
			(double)Integer.MIN_VALUE - 1.0,
			Double.NEGATIVE_INFINITY,
			Double.POSITIVE_INFINITY,
			Double.MAX_VALUE,
			Double.MAX_VALUE
	};
	
	public void testLongBitsToLuaNumber() {
		for ( int i = 0; i < DOUBLE_VALUES.length; ++i ) {
			double v = DOUBLE_VALUES[i];
			long bits = Double.doubleToLongBits( v );
			LNumber luaNumber = LoadState.longBitsToLuaNumber( bits );
			
			assertEquals( v, luaNumber.toJavaDouble(), 0 );
			
			if ( v != Integer.MIN_VALUE ) {
				// Special case of MIN_VALUE is probably not worth dealing with.
				// (Unlike zero, which is also a special case but much more common.)
				assertEquals( "Value "+v+" (at index "+i+") can be represented as integer but was not",
						luaNumber instanceof LInteger, v == (double)( (int) v ) );
			}
		}
	}
	
	private LNumber simpleBitsToLuaNumber( long bits ) {
		double value = Double.longBitsToDouble( bits );
		int valueAsInt = (int) value;
		
		if ( value == (double) valueAsInt ) {
			return LInteger.valueOf( valueAsInt );
		} else {
			return new LDouble( value );
		}
	}
	
	public void testLongBitsToLuaNumberSpeed() throws InterruptedException {
		long[] BITS = new long[ 500000 ];
		Random r = new Random();
		
		for ( int i = 0; i < DOUBLE_VALUES.length; ++i ) {
			BITS[i] = Double.doubleToLongBits( DOUBLE_VALUES[i] );
		}
		for ( int i = DOUBLE_VALUES.length; i < BITS.length; i += 2 ) {
			BITS[i  ] = r.nextLong();
			BITS[i+1] = Double.doubleToLongBits( r.nextDouble() );
		}

		
		long simpleConversionCount = 0;
		long complexConversionCount = 0;
			
		collectGarbage();
		long startTime = leadingEdgeTime();
		long endTime = startTime + 1000;
		int count = 0;
		int n = BITS.length;
		for ( ; currentTime()<endTime; count++ ) {
			for ( int j=0; j<n; j++ )
				simpleBitsToLuaNumber( BITS[j] );
		}
		simpleConversionCount += count;
		
		collectGarbage();
		startTime = leadingEdgeTime();
		endTime = startTime + 1000;
		count = 0;
		for ( ; currentTime()<endTime; count++ ) {
			for ( int j=0; j<n; j++ )
				LoadState.longBitsToLuaNumber( BITS[j] );
		}
		complexConversionCount += count;
		
		System.out.println("conversion counts: simple,complex="
				+simpleConversionCount+","+complexConversionCount);

		assertTrue( complexConversionCount >= simpleConversionCount );
	}

	private void collectGarbage() throws InterruptedException {
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		Thread.sleep(20);
		rt.gc();
		Thread.sleep(20);
	}

	private static long leadingEdgeTime() {
		long s,e = currentTime();
		for ( s=currentTime(); s==(e=currentTime()); )
			;
		return e;
	}

	private static long currentTime() {
		return System.currentTimeMillis();
	}

}
