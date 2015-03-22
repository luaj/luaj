/*******************************************************************************
 * Copyright (c) 2015 Luaj.org. All rights reserved.
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

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/** LuaTable that has all read/write properties of a normal table, 
 * but falls back to values in an underlying delegate table on reads,
 * including wrapping any table result in another shadow table.
 * 
 * <p>This simplifies creation of a safe unique environment for user scripts
 * which falls back to a delegate shared globals table.
 */
public class ReadWriteShadowTable extends LuaTable  {
	/** The underlying table from which values are read when
	 * the table does not contain a key. The values in delegate
	 * should not be modified as a result of being a delegate for 
	 * this table, however if the values are userdata that expose
	 * mutators, those values may undergo mutations once exposed.
	 */
	public final LuaValue delegate;
	
	/** Construct a read-write shadow table around 'delegate' without
	 * copying elements, but retaining a reference to delegate.
	 * @param fallback The table containing values we would like to 
	 * reference without affecting the contents of that table.
	 */
	public ReadWriteShadowTable(LuaValue delegate) {
		this.delegate = delegate;
	}

	/** Normal table get, but return delegate value if not found. 
	 * If the delegate returns a table, wraps that in a read-write shadow
	 * and does a local rawset on that value before returning it. 
	 * @param key LuaValue to look up. 
	 */
	public LuaValue get(LuaValue key) {
		LuaValue value = super.get(key);
		if (value.isnil()) {
			value = delegate.get(key);
			if (value.istable())
				 value = new ReadWriteShadowTable(value);
			rawset(key, value);
		}
		return value;
	}
}