/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.vm2;

import java.util.Hashtable;

import org.luaj.vm2.lib.MathLib;

public class LuaDouble extends LuaNumber {

	public static final LuaDouble NAN    = new LuaDouble( Double.NaN );
	public static final LuaDouble POSINF = new LuaDouble( Double.POSITIVE_INFINITY );
	public static final LuaDouble NEGINF = new LuaDouble( Double.NEGATIVE_INFINITY );
	public static final String JSTR_NAN    = "nan";
	public static final String JSTR_POSINF = "inf";
	public static final String JSTR_NEGINF = "-inf";
	
	final double v;

	public static LuaNumber valueOf(double d) {
		int id = (int) d;
		return d==id? (LuaNumber) LuaInteger.valueOf(id): (LuaNumber) new LuaDouble(d);
	}
	
	/** Don't allow ints to be boxed by DoubleValues  */
	private LuaDouble(double d) {
		this.v = d;
	}

	public int hashCode() {
		long l = Double.doubleToLongBits(v);
		return ((int)(l>>32)) | (int) l;
	}
	
	public boolean islong() {
		return v == (long) v; 
	}
	
	public byte    tobyte()        { return (byte) (long) v; }
	public char    tochar()        { return (char) (long) v; }
	public double  todouble()      { return v; }
	public float   tofloat()       { return (float) v; }
	public int     toint()         { return (int) (long) v; }
	public long    tolong()        { return (long) v; }
	public short   toshort()       { return (short) (long) v; }

	public double      optdouble(double defval)        { return v; }
	public int         optint(int defval)              { return (int) (long) v;  }
	public LuaInteger  optinteger(LuaInteger defval)   { return LuaInteger.valueOf((int) (long)v); }
	public long        optlong(long defval)            { return (long) v; }
	
	public LuaInteger  checkinteger()                  { return LuaInteger.valueOf( (int) (long) v ); }
	
	// unary operators
	public LuaValue neg() { return valueOf(-v); }
	
	// object equality, used for key comparison
	public boolean equals(Object o) { return o instanceof LuaDouble? ((LuaDouble)o).v == v: false; }
	
	// arithmetic equality
	public boolean eq_b( LuaValue rhs )       { return rhs.eq_b(v); }
	public boolean eq_b( double rhs )      { return v == rhs; }
	public boolean eq_b( int rhs )         { return v == rhs; }
	
	// basic binary arithmetic
	public LuaValue   add( LuaValue rhs )        { return rhs.add(v); }
	public LuaValue   add( double lhs )     { return LuaDouble.valueOf(lhs + v); }
	public LuaValue   sub( LuaValue rhs )        { return rhs.subFrom(v); }
	public LuaValue   sub( double rhs )        { return LuaDouble.valueOf(v - rhs); }
	public LuaValue   sub( int rhs )        { return LuaDouble.valueOf(v - rhs); }
	public LuaValue   subFrom( double lhs )   { return LuaDouble.valueOf(lhs - v); }
	public LuaValue   mul( LuaValue rhs )        { return rhs.mul(v); }
	public LuaValue   mul( double lhs )   { return LuaDouble.valueOf(lhs * v); }
	public LuaValue   mul( int lhs )      { return LuaDouble.valueOf(lhs * v); }
	public LuaValue   pow( LuaValue rhs )        { return rhs.powWith(v); }
	public LuaValue   pow( double rhs )        { return MathLib.dpow(v,rhs); }
	public LuaValue   pow( int rhs )        { return MathLib.dpow(v,rhs); }
	public LuaValue   powWith( double lhs )   { return MathLib.dpow(lhs,v); }
	public LuaValue   powWith( int lhs )      { return MathLib.dpow(lhs,v); }
	public LuaValue   div( LuaValue rhs )        { return rhs.divInto(v); }
	public LuaValue   div( double rhs )        { return LuaDouble.ddiv(v,rhs); }
	public LuaValue   div( int rhs )        { return LuaDouble.ddiv(v,rhs); }
	public LuaValue   divInto( double lhs )   { return LuaDouble.ddiv(lhs,v); }
	public LuaValue   mod( LuaValue rhs )        { return rhs.modFrom(v); }
	public LuaValue   mod( double rhs )        { return LuaDouble.dmod(v,rhs); }
	public LuaValue   mod( int rhs )        { return LuaDouble.dmod(v,rhs); }
	public LuaValue   modFrom( double lhs )   { return LuaDouble.dmod(lhs,v); }
	
	/** lua division is always double, specific values for singularities */
	public static LuaValue ddiv(double lhs, double rhs) {
		return rhs!=0? valueOf( lhs / rhs ): lhs>0? POSINF: lhs==0? NAN: NEGINF;	
	}
	public static double ddiv_d(double lhs, double rhs) {
		return rhs!=0? lhs / rhs: lhs>0? Double.POSITIVE_INFINITY: lhs==0? Double.NaN: Double.NEGATIVE_INFINITY;	
	}
	
	/** lua module is always wrt double. */
	public static LuaValue dmod(double lhs, double rhs) {
		return rhs!=0? valueOf( lhs-rhs*Math.floor(lhs/rhs) ): NAN;
	}
	public static double dmod_d(double lhs, double rhs) {
		return rhs!=0? lhs-rhs*Math.floor(lhs/rhs): Double.NaN;
	}

	// relational operators
	public LuaValue   lt( LuaValue rhs )         { return rhs.gt_b(v)? LuaValue.TRUE: FALSE; }
	public LuaValue   lt( double rhs )      { return v < rhs? TRUE: FALSE; }
	public LuaValue   lt( int rhs )         { return v < rhs? TRUE: FALSE; }
	public boolean lt_b( LuaValue rhs )       { return rhs.gt_b(v); }
	public boolean lt_b( int rhs )         { return v < rhs; }
	public boolean lt_b( double rhs )      { return v < rhs; }
	public LuaValue   lteq( LuaValue rhs )       { return rhs.gteq_b(v)? LuaValue.TRUE: FALSE; }
	public LuaValue   lteq( double rhs )    { return v <= rhs? TRUE: FALSE; }
	public LuaValue   lteq( int rhs )       { return v <= rhs? TRUE: FALSE; }
	public boolean lteq_b( LuaValue rhs )     { return rhs.gteq_b(v); }
	public boolean lteq_b( int rhs )       { return v <= rhs; }
	public boolean lteq_b( double rhs )    { return v <= rhs; }
	public LuaValue   gt( LuaValue rhs )         { return rhs.lt_b(v)? LuaValue.TRUE: FALSE; }
	public LuaValue   gt( double rhs )      { return v > rhs? TRUE: FALSE; }
	public LuaValue   gt( int rhs )         { return v > rhs? TRUE: FALSE; }
	public boolean gt_b( LuaValue rhs )       { return rhs.lt_b(v); }
	public boolean gt_b( int rhs )         { return v > rhs; }
	public boolean gt_b( double rhs )      { return v > rhs; }
	public LuaValue   gteq( LuaValue rhs )       { return rhs.lteq_b(v)? LuaValue.TRUE: FALSE; }
	public LuaValue   gteq( double rhs )    { return v >= rhs? TRUE: FALSE; }
	public LuaValue   gteq( int rhs )       { return v >= rhs? TRUE: FALSE; }
	public boolean gteq_b( LuaValue rhs )     { return rhs.lteq_b(v); }
	public boolean gteq_b( int rhs )       { return v >= rhs; }
	public boolean gteq_b( double rhs )    { return v >= rhs; }
	
	// string comparison
	public int strcmp( LuaString rhs )      { typerror("attempt to compare number with string"); return 0; }
	
	// concatenation
	public String concat_s(LuaValue rhs)      { return rhs.concatTo_s(Double.toString(v)); }
	public String concatTo_s(String lhs)   { return lhs + v; }
		
	public String tojstring() {
		/*
		if ( v == 0.0 ) { // never occurs in J2me 
			long bits = Double.doubleToLongBits( v );
			return ( bits >> 63 == 0 ) ? "0" : "-0";
		}
		*/
		long l = (long) v;
		if ( l == v ) 
			return Long.toString(l);
		if ( Double.isNaN(v) )
			return JSTR_NAN;
		if ( Double.isInfinite(v) ) 
			return (v<0? JSTR_NEGINF: JSTR_POSINF);
		return Float.toString((float)v);
	}
	
	public LuaString strvalue() {
		return LuaString.valueOf(tojstring());
	}
	
	public LuaString optstring(LuaString defval) {
		return LuaString.valueOf(tojstring());
	}
		
	public LuaValue tostring() {
		return LuaString.valueOf(tojstring());
	}
	
	public String optjstring(String defval) {
		return tojstring();
	}
	
	public LuaNumber optnumber(LuaNumber defval) {
		return this; 
	}
	
	public boolean isnumber() {
		return true; 
	}
	
	public boolean isstring() {
		return true;
	}
	
	public LuaValue tonumber() {
		return this;
	}
	public int checkint()                { return (int) (long) v; }
	public long checklong()              { return (long) v; }
	public LuaNumber checknumber()       { return this; }
	public double checkdouble()          { return v; }
	
	public String checkjstring() { 
		return tojstring();
	}
	public LuaString checkstring() { 
		return LuaString.valueOf(tojstring());
	}
	
	public LuaValue checkvalidkey() {
		if ( Double.isNaN(v) )
			throw new LuaError("table index expected, got nan");
		return this; 
	}	
}
