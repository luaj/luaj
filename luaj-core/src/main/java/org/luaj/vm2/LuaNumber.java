/*******************************************************************************
 * Copyright (c) 2009-2011 Luaj.org. All rights reserved.
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

/**
 * Base class for representing numbers as lua values directly.
 * <p>
 * The main subclasses are {@link LuaInteger} which holds values that fit in a
 * java int, and {@link LuaDouble} which holds all other number values.
 *
 * @see LuaInteger
 * @see LuaDouble
 * @see LuaValue
 *
 */
abstract public class LuaNumber extends LuaValue {

	/** Shared static metatable for all number values represented in lua. */
	public static LuaValue s_metatable;

	@Override
	public int type() {
		return TNUMBER;
	}

	@Override
	public String typename() {
		return "number";
	}

	@Override
	public LuaNumber checknumber() {
		return this;
	}

	@Override
	public LuaNumber checknumber(String errmsg) {
		return this;
	}

	@Override
	public LuaNumber optnumber(LuaNumber defval) {
		return this;
	}

	@Override
	public LuaValue tonumber() {
		return this;
	}

	@Override
	public boolean isnumber() {
		return true;
	}

	@Override
	public boolean isstring() {
		return true;
	}

	@Override
	public LuaValue getmetatable() {
		return s_metatable;
	}

	@Override
	public LuaValue concat(LuaValue rhs) { return rhs.concatTo(this); }

	@Override
	public Buffer concat(Buffer rhs) { return rhs.concatTo(this); }

	@Override
	public LuaValue concatTo(LuaNumber lhs) { return strvalue().concatTo(lhs.strvalue()); }

	@Override
	public LuaValue concatTo(LuaString lhs) { return strvalue().concatTo(lhs); }

}
