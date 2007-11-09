/*******************************************************************************
* Copyright (c) 2007 LuaJ. All rights reserved.
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
