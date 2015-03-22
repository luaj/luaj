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

/** Utility class to construct a read-only LuaTable whose initial contents are 
 * initialized to some set of values and whose contents cannot be changed after
 * construction, allowing the table to be shared in a multi-threaded context.
 */
public final class ReadOnlyTable extends LuaTable {

	/** Empty read-only table with no metatable. */
	public static final ReadOnlyTable empty_read_only_table = 
			new ReadOnlyTable();

	/** Construct a ReadOnlyTable with a set of named key-value pairs.
	 * 'named' is a list of LuaValues, with the first being a key, the
	 * second being the corresponding value, and so on.  All key-value
	 * pairs are copied into the table as part of construction, 
	 * however only a shallow copy is done so values which are tables
	 * remain read-write even if accessed from this read-only table.
	 * @param named array of values in key,value,key,value order 
	 * which are the key-value pairs that will appear in this read-only table.
	 */
	public ReadOnlyTable(LuaValue[] named) {		
		presize(named.length/2, 0);
		for ( int i=0; i<named.length; i+=2 )
			if (!named[i+1].isnil())
				super.rawset(named[i], named[i+1]);
	}

	/** Construct a ReadOnlyTable with initial values contained in 'table'
	 * All key-value pairs in 'table' are copied, however only a shallow 
	 * copy is done so values which are tables remain read-write even 
	 * if they are returned by accessing this read-only table.
	 * @param table LuaTable containing values to copy to this read-only table.
	 */
	public ReadOnlyTable(LuaTable table) {
		this(table, null, false);
	}

	/** Construct a ReadOnlyTable with initial values contained in 'table'
	 * and a read-only metatable 'metatable'.
	 * All key-value pairs in 'table' are copied, however only a shallow 
	 * copy is done so values which are tables remain read-write even 
	 * if accessed from this read-only table.
	 * @param table LuaTable containing values to copy in initially.
	 * @param metatable ReadOnlyTable to be used as a metatable.
	 */
	public ReadOnlyTable(LuaTable table, ReadOnlyTable metatable) {
		this(table, metatable, false);
	}

	/** Construct a ReadOnlyTable with initial values contained in 'table'
	 * and a read-only metatable 'metatable', and optionally converting all
	 * contained keys and values that are tables into read-only tables.
	 * All key-value pairs in 'table' are copied, and when deepcopy is true, 
	 * tables are converted to ReadOnlyTable recursively.
	 * @param table LuaTable containing values to copy in initially.
	 * @param metatable ReadOnlyTable to be used as a metatable.
	 * @param deepcopy when true, also converts table keys and values to read-only tables.
	 */
	public ReadOnlyTable(LuaTable table, ReadOnlyTable metatable, boolean deepcopy) {
		presize(table.length(), 0);
		for (Varargs n = table.next(LuaValue.NIL); !n.arg1().isnil(); 
				n = table.next(n.arg1())) {
			LuaValue key = n.arg1();
			LuaValue value = n.arg(2);
			super.rawset(deepcopy && key.istable()? new ReadOnlyTable(key.checktable()): key,
					     deepcopy && value.istable()? new ReadOnlyTable(value.checktable()): value);
		}
		this.m_metatable = metatable;
	}

	/** Constructor for default instance which is an empty read-only table. */
	private ReadOnlyTable() {}	

	/** Throw error indicating this is a read-only table. */
	public LuaValue setmetatable(LuaValue metatable) {
		return error("table is read-only");
	}

	/** Throw error indicating this is a read-only table. */
	public void set(int key, LuaValue value) {
		error("table is read-only");
	}

	/** Throw error indicating this is a read-only table. */
	public void set(LuaValue key, LuaValue value) {
		error("table is read-only");
	}

	/** Throw error indicating this is a read-only table. */
	public void rawset(int key, LuaValue value) {
		error("table is read-only");
	}

	/** Throw error indicating this is a read-only table. */
	public void rawset(LuaValue key, LuaValue value) {
		error("table is read-only");
	}

	/** Throw error indicating this is a read-only table. */
	public LuaValue remove(int pos) {
		return error("table is read-only");
	}
}
