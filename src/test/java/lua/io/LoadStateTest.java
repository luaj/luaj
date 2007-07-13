package lua.io;

import java.util.Random;

import junit.framework.TestCase;
import lua.value.LDouble;
import lua.value.LInteger;
import lua.value.LNumber;

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
			
			assertEquals( v, luaNumber.luaAsDouble() );
			
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
			return new LInteger( valueAsInt );
		} else {
			return new LDouble( value );
		}
	}
	
	public void testLongBitsToLuaNumberSpeed() {
		long[] BITS = new long[ 500000 ];
		Random r = new Random();
		
		for ( int i = 0; i < DOUBLE_VALUES.length; ++i ) {
			BITS[i] = Double.doubleToLongBits( DOUBLE_VALUES[i] );
		}
		for ( int i = DOUBLE_VALUES.length; i < BITS.length; i += 2 ) {
			BITS[i  ] = r.nextLong();
			BITS[i+1] = Double.doubleToLongBits( r.nextDouble() );
		}
		
		long startTime = System.currentTimeMillis();
		for ( int j = 0; j < BITS.length; ++j ) {
			LoadState.longBitsToLuaNumber( BITS[j] );
		}
		long endTime = System.currentTimeMillis();
		long complexConversionTime = endTime - startTime;
		
		startTime = System.currentTimeMillis();
		for ( int j = 0; j < BITS.length; ++j ) {
			simpleBitsToLuaNumber( BITS[j] );
		}
		endTime = System.currentTimeMillis();
		long simpleConversionTime = endTime - startTime;
		
		assertTrue( complexConversionTime < simpleConversionTime );
	}
}
