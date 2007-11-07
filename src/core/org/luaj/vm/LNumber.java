package org.luaj.vm;


abstract
public class LNumber extends LValue {

	/** Compare for equivalence by using lua op comparator */
	public boolean equals(Object o) {
		if ( ! ( o instanceof LValue) )
			return false;
		LValue v = (LValue) o;
		return this.luaBinCmpUnknown(Lua.OP_EQ, v );
	}

	public int luaGetType() {
		return Lua.LUA_TNUMBER;
	}
	
	/**
	 * Returns false by default for non-LNumbers, but subclasses of LNumber must
	 * override.
	 */
	public abstract boolean isInteger();
	
	/** Convert to a Byte value */
	public Byte toJavaBoxedByte() {
		return new Byte(toJavaByte());
	}

	/** Convert to a boxed Character value */
	public Character toJavaBoxedCharacter() {
		return new Character(toJavaChar());
	}

	/** Convert to a boxed Double value */
	public Double toJavaBoxedDouble() {
		return new Double(toJavaDouble());
	}

	/** Convert to a boxed Float value */
	public Float toJavaBoxedFloat() {
		return new Float(toJavaFloat());
	}

	/** Convert to a boxed Integer value */
	public Integer toJavaBoxedInteger() {
		return new Integer(toJavaInt());
	}

	/** Convert to a boxed Long value */
	public Long toJavaBoxedLong() {
		return new Long(toJavaLong());
	}

	/** Convert to a boxed Short value */
	public Short toJavaBoxedShort() {
		return new Short(toJavaShort());
	}
	
}
