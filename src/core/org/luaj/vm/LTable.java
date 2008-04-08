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


/**
 * Simple implementation of table structure for Lua VM. Maintains both an array
 * part and a hash part. Does not attempt to achieve the same performance as the
 * C version.
 * 
 * Java code can put values in the table or get values out (bypassing the
 * metatable, if there is one) using put() and get(). There are specializations
 * of put() and get() for integers and Strings to avoid allocating wrapper
 * objects when possible.
 * 
 * remove() methods are private: setting a key's value to nil is the correct way
 * to remove an entry from the table.
 * 
 * TODO: Support for weak tables has to be shoehorned in here somehow.
 * 
 */
public class LTable extends LValue {
	
	/**
	 * Zero-length array to use instead of null, so that we don't need to
	 * check for null everywhere.
	 */
	private static final LValue[] EMPTY_ARRAY = new LValue[0];
	
	/**
	 * Minimum legal capacity for the hash portion. Note that the hash portion
	 * must never be filled to capacity or findSlot() will run forever.
	 */
	private static final int MIN_HASH_CAPACITY = 2;
	
	/**
	 * Array of keys in the hash part. When there is no hash part this is null.
	 * Elements of m_hashKeys are never LNil.NIL - they are null to indicate
	 * the hash slot is empty and some non-null, non-nil value otherwise.
	 */
	protected LValue[] m_hashKeys;
	
	/**
	 * Values in the hash part. Must be null when m_hashKeys is null and equal
	 * in size otherwise.
	 */
	protected LValue[] m_hashValues;
	
	/**
	 * m_hashEntries is the number of slots that are used. Must always be less
	 * than m_hashKeys.length.
	 */
	protected int m_hashEntries;
	
	/**
	 * Array of values to store the "array part" of the table, that is the
	 * entries with positive integer keys. Elements must never be null: "empty"
	 * slots are set to LNil.NIL.
	 */
	protected LValue[] m_vector;
	
	/**
	 * Number of values in m_vector that non-nil.
	 */
	protected int m_arrayEntries;
	
	private LTable m_metatable;

	private static final int INVALID_KEY_TO_NEXT = -2;
	
	/** Construct an empty LTable with no initial capacity. */
	public LTable() {
		m_vector = EMPTY_ARRAY;
	}

	/**
	 * Construct an empty LTable that is expected to contain entries with keys
	 * in the range 1 .. narray and nhash non-integer keys.
	 */
	public LTable( int narray, int nhash ) {
		if ( nhash > 0 ) {
			// Allocate arrays 25% bigger than nhash to account for load factor.
			final int capacity = Math.max( nhash + ( nhash >> 2 ), nhash + 1 );
			m_hashKeys = new LValue[capacity];
			m_hashValues = new LValue[capacity];
		}
		m_vector = new LValue[narray];
		for ( int i = 0; i < narray; ++i ) {
			m_vector[i] = LNil.NIL;
		}
	}
	
	/**
	 * Return total number of keys mapped to non-nil values. Not to be confused
	 * with luaLength, which returns some number n such that the value at n+1 is
	 * nil.
	 */
	public int size() {
		return m_hashEntries + m_arrayEntries;
	}

	/**
	 * Generic put method for all types of keys, but does not use the metatable.
	 */
	public void put( LValue key, LValue val ) {
		if ( key.isInteger() ) {
			// call the integer-specific put method
			put( key.toJavaInt(), val );
		} else if ( val == null || val.isNil() ) {
			// Remove the key if the value is nil. This comes after the check
			// for an integer key so that values are properly removed from
			// the array part.
			remove( key );
		} else {
			if ( checkLoadFactor() )
				rehash();
			int slot = findSlot( key );
			if ( fillHashSlot( slot, val ) )
				return;
			m_hashKeys[slot] = key;
		}
	}
	
	/**
	 * Utility method for putting a string-keyed value directly, typically for
	 * initializing a table. Bypasses the metatable, if any.
	 */
	public void put( String key, LValue value ) {
		put( new LString( key ), value );
	}
	
	/**
	 * Utility method for putting a string key, int value directly, typically for
	 * initializing a table. Bypasses the metatable, if any.
	 */
	public void put( String key, int value ) {
		put( new LString( key ), LInteger.valueOf(value) );
	}
		
	/**
	 * Method for putting an integer-keyed value. Bypasses the metatable, if
	 * any.
	 */
	public void put( int key, LValue value ) {
		if (value == null || value.isNil()) {
			remove( key );
			return;
		}
		if ( key > 0 ) {
			final int index = key - 1;
			for ( ;; ) {
				if ( index < m_vector.length ) {
					if ( m_vector[index].isNil() ) {
						++m_arrayEntries;
					}
					m_vector[index] = value;
					return;
				} else if ( index < ( m_arrayEntries + 1 ) * 2 ) {
					resize( ( m_arrayEntries + 1 ) * 2 );
				} else {
					break;
				}
			}
		}
		
		// No room in array part, use hash part instead.
		if ( checkLoadFactor() )
			rehash();
		int slot = findSlot( key );
		if ( fillHashSlot( slot, value ) )
			return;
		m_hashKeys[ slot ] = LInteger.valueOf( key );
	}
	
	
	/**
	 * Utility method to directly get the value in a table, without metatable
	 * calls. Must never return null, use LNil.NIL instead.
	 */
	public LValue get( LValue key ) {
		if ( m_vector.length > 0 && key.isInteger() ) {
			final int index = key.toJavaInt() - 1;
			if ( index >= 0 && index < m_vector.length ) {
				return m_vector[index];
			}
		}
		
		if ( m_hashKeys == null )
			return LNil.NIL;
		
		int slot = findSlot( key );
		return ( m_hashKeys[slot] != null ) ? m_hashValues[slot] : LNil.NIL;
	}
	
	/** Utility method for retrieving an integer-keyed value */
	public LValue get( int key ) {
		if ( key > 0 && key <= m_vector.length ) {
			return m_vector[key - 1];
		}
		
		if ( m_hashKeys == null )
			return LNil.NIL;

		int slot = findSlot( key );
		return ( m_hashKeys[slot] != null ) ? m_hashValues[slot] : LNil.NIL;
	}
	
	
	/**
	 * Return true if the table contains an entry with the given key, false if
	 * not. Ignores the metatable.
	 */
	public boolean containsKey( LValue key ) {
		if ( m_vector.length > 0 && key.isInteger() ) {
			final int index = key.toJavaInt() - 1;
			if ( index >= 0 && index < m_vector.length ) {
				return ! m_vector[index].isNil();
			}
		}
		if ( m_hashKeys == null )
			return false;
		final int slot = findSlot( key );
		return m_hashKeys[ slot ] != null;
	}

	public void luaGetTable(LuaState vm, LValue table, LValue key) {
		LValue v = get(key);
		if ( v.isNil() && m_metatable != null ) {
			super.luaGetTable( vm, table, key );
		} else {
			vm.pushlvalue(v);
		}
	}
	
	public void luaSetTable(LuaState vm, LValue table, LValue key, LValue val) {
		if ( (!containsKey( key )) && m_metatable != null && m_metatable.containsKey(TM_NEWINDEX) )
			m_metatable.get(TM_NEWINDEX).luaSetTable( vm, table, key, val );
		else
			put(key,val);
	}
	
	/**
	 * Return the "length" of this table. This will not return the same result
	 * as the C version in all cases, but that's ok because the length operation
	 * on a table where the integer keys are sparse is vaguely defined.
	 */
	public int luaLength() {
		for ( int i = Math.max( 0, m_arrayEntries-1 ); i < m_vector.length; ++i ) {
			if ( ! m_vector[i].isNil() &&
					( i+1 == m_vector.length || m_vector[i+1].isNil() ) ) {
				return i+1;
			}
		}
		return 0;
	}
	
	/** Valid for tables */
	public LTable luaGetMetatable() {
		return this.m_metatable;
	}

	/** Valid for tables */
	public LValue luaSetMetatable(LValue metatable) {
		if ( m_metatable != null && m_metatable.containsKey(TM_METATABLE) )
			throw new LuaErrorException("cannot change a protected metatable");
		if ( metatable == null || metatable.isNil() )
			this.m_metatable = null;
		else if ( metatable.luaGetType() == Lua.LUA_TTABLE ) { 
			LTable t = (LTable) metatable;
			LValue m = t.get(TM_MODE);
			if ( "v".equals(m.toJavaString()) ) {
				LTable n = new LWeakTable(this);
				n.m_metatable = t;
				return n;
			}
			this.m_metatable = t;
		} 
		else
			throw new LuaErrorException("nil or table expected, got "+metatable.luaGetTypeName());
		return null;
	}

	public String toJavaString() {
		return "table: "+id();
	}

	public int luaGetType() {
		return Lua.LUA_TTABLE;
	}

	/**
	 * Helper method to get all the keys in this table in an array. Meant to be
	 * used instead of keys() (which returns an enumeration) when an array is
	 * more convenient. Note that for a very large table, getting an Enumeration
	 * instead would be more space efficient.
	 */
	public LValue[] getKeys() {
		LValue[] keys = new LValue[ m_arrayEntries + m_hashEntries ];
		int out = 0;
		
		for ( int i = 0; i < m_vector.length; ++i ) {
			if ( ! m_vector[ i ].isNil() ) {
				keys[ out++ ] = LInteger.valueOf( i + 1 );
			}
		}
		
		if ( m_hashKeys != null ) {
			for ( int i = 0; i < m_hashKeys.length; ++i ) {
				if ( m_hashKeys[ i ] != null )
					keys[ out++ ] = m_hashKeys[i];
			}
		}
		
		return keys;
	}
	
	/** Remove the value in the table with the given integer key. */
	protected void remove( int key ) {
		if ( key > 0 ) {
			final int index = key - 1;
			if ( index < m_vector.length ) {
				if ( ! m_vector[ index ].isNil() ) {
					m_vector[ index ] = LNil.NIL;
					--m_arrayEntries;
				}
				return;
			}
		}
		
		if ( m_hashKeys != null ) {
			int slot = findSlot( key );
			clearSlot( slot );
		}
	}
	
	protected void remove( LValue key ) {
		if ( m_hashKeys != null ) {
			int slot = findSlot( key );
			clearSlot( slot );
		}
	}
	
	private void clearSlot( int i ) {
		if ( m_hashKeys[ i ] != null ) {
			
			int j = i;
			while ( m_hashKeys[ j = ( ( j + 1 ) % m_hashKeys.length ) ] != null ) {
				final int k = hashToIndex( m_hashKeys[ j ].hashCode() );
				if ( ( j > i && ( k <= i || k > j ) ) ||
					 ( j < i && ( k <= i && k > j ) ) ) {
					m_hashKeys[ i ] = m_hashKeys[ j ];
					m_hashValues[ i ] = m_hashValues[ j ];
					i = j;
				}
			}
			
			--m_hashEntries;
			m_hashKeys[ i ] = null;
			m_hashValues[ i ] = null;
			
			if ( m_hashEntries == 0 ) {
				m_hashKeys = null;
				m_hashValues = null;
			}
		}
	}
	
	private int findSlot( LValue key ) {
		int i = hashToIndex( key.hashCode() );
		
		// This loop is guaranteed to terminate as long as we never allow the
		// table to get 100% full.
		LValue k;
		while ( ( k = m_hashKeys[i] ) != null &&
				!key.luaBinCmpUnknown( Lua.OP_EQ, k ) ) {
			i = ( i + 1 ) % m_hashKeys.length;
		}
		return i;
	}
	
	private int findSlot( int key ) {
		int i = hashToIndex( LInteger.hashCodeOf( key ) );
		
		// This loop is guaranteed to terminate as long as we never allow the
		// table to get 100% full.
		LValue k;
		while ( ( k = m_hashKeys[i] ) != null &&
				  !k.luaBinCmpInteger( Lua.OP_EQ, key ) ) {
			i = ( i + 1 ) % m_hashKeys.length;
		}
		return i;
	}
	
	/**
	 * @return true if the given slot was already occupied, false otherwise.
	 */
	private boolean fillHashSlot( int slot, LValue value ) {
		m_hashValues[ slot ] = value;
		if ( m_hashKeys[ slot ] != null ) {
			return true;
		} else {
			++m_hashEntries;
			return false;
		}
	}
	
	private int hashToIndex( int hash ) {
		return ( hash & 0x7FFFFFFF ) % m_hashKeys.length;
	}
	
	/**
	 * Should be called before inserting a value into the hash.
	 * 
	 * @return true if the hash portion of the LTable is at its capacity.
	 */
	private boolean checkLoadFactor() {
		if ( m_hashKeys == null )
			return true;
		// Using a load factor of 2/3 because that is easy to compute without
		// overflow or division.
		final int hashCapacity = m_hashKeys.length;
		return ( hashCapacity >> 1 ) >= ( hashCapacity - m_hashEntries );
	}
	
	private void rehash() {
		final int oldCapacity = ( m_hashKeys != null ) ? m_hashKeys.length : 0;
		final int newCapacity = ( oldCapacity > 0 ) ? 2 * oldCapacity : MIN_HASH_CAPACITY;
		
		final LValue[] oldKeys = m_hashKeys;
		final LValue[] oldValues = m_hashValues;
		
		m_hashKeys = new LValue[ newCapacity ];
		m_hashValues = new LValue[ newCapacity ];
		
		for ( int i = 0; i < oldCapacity; ++i ) {
			final LValue k = oldKeys[i];
			if ( k != null ) {
				final LValue v = oldValues[i];
				final int slot = findSlot( k );
				m_hashKeys[slot] = k;
				m_hashValues[slot] = v;
			}
		}
	}
	
	private void resize( int newCapacity ) {
		final int oldCapacity = m_vector.length;
		LValue[] newVector = new LValue[ newCapacity ];
		System.arraycopy( m_vector, 0, newVector, 0, Math.min( oldCapacity, newCapacity ) );
		
		// We need to move keys from hash part to array part if array part is
		// getting bigger, and from array part to hash part if array is getting
		// smaller.
		if ( newCapacity > oldCapacity ) {
			for ( int i = oldCapacity; i < newCapacity; ++i ) {
				// Test for m_hashKeys != null must be inside the loop because
				// call to clearSlot may result in m_hashKeys becoming null
				// at any time.
				if ( m_hashKeys != null ) {
					int slot = findSlot( i+1 );
					if ( m_hashKeys[ slot ] != null ) {
						newVector[ i ] = m_hashValues[ slot ];
						clearSlot( slot );
						continue;
					}
				}
				// Make sure all array-part values are initialized to nil
				// so that we can just do one compare instead of two
				// whenever we need to check if a slot is full or not.
				newVector[ i ] = LNil.NIL;
			}
		} else {
			for ( int i = newCapacity; i < oldCapacity; ++i ) {
				LValue v = m_vector[i];
				if ( ! v.isNil() ) {
					if (checkLoadFactor())
						rehash();
					final int slot = findSlot( i+1 );
					m_hashKeys[ slot ] = LInteger.valueOf( i+1 );
					m_hashValues[ slot ] = v;
					++m_hashEntries;
				}
			}
		}
		
		m_vector = newVector;
	}
	
	// hooks for junit
	
	int getHashCapacity() {
		return ( m_hashKeys != null ) ? m_hashKeys.length : 0;
	}
	
	int getArrayCapacity() {
		return m_vector.length;
	}

	/**
	 * Insert element at a position in the list.
	 * @pos index to insert at, or 0 to insert at end.
	 */
	public void luaInsertPos(int pos, LValue value) {
		if ( pos > m_arrayEntries + 1 )
			put( pos, value );
		else {
			final int index = Math.max(0,pos==0? m_arrayEntries: pos-1);
			if ( m_arrayEntries + 1 > m_vector.length )
				resize( ( m_arrayEntries + 1 ) * 2 );
			if ( ! m_vector[index].isNil() ) {
				System.arraycopy(m_vector, index, m_vector, index+1, m_vector.length-1-index);
			}
			m_vector[index] = value;
			++m_arrayEntries;
		}
	}

	/**
	 * Remove an element from the list part of the table
	 * @param pos position to remove, or 0 to remove last element
	 */
	public LValue luaRemovePos(int pos) {
		if ( pos > m_arrayEntries ) {
			LValue val = get( pos );
			if ( ! val.isNil() )
				put( pos, LNil.NIL );
			return val;
		} else {
			final int n = m_vector.length - 1;
			final int index = Math.max(0,pos<=0? m_arrayEntries: pos)-1;
			if ( index < 0 )
				return LNil.NIL;
			LValue val = m_vector[index];
			System.arraycopy(m_vector, index+1, m_vector, index, n-index);
			m_vector[n] = LNil.NIL;
			--m_arrayEntries;
			return val;
		}
	}

	public LValue luaMaxN() {
		LValue result = LInteger.valueOf(0);
		
		for ( int i = m_vector.length - 1; i >= 0; i-- ) {
			if ( ! m_vector[i].isNil() ) {
				result = LInteger.valueOf(i + 1);
				break;
			}
		}
		
		if ( m_hashKeys != null ) {
			final int hlen = m_hashKeys.length;
			for ( int i = 0; i < hlen; ++i ) {
				LValue k = m_hashKeys[i];
				if ( k != null && k.luaGetType() == Lua.LUA_TNUMBER ) {
					if ( k.luaBinCmpUnknown(Lua.OP_LT, result ) ) {
						result = k;
					}
				}
			}
		}
		
		return result;
	}

	// ----------------- sort support -----------------------------
	//
	// implemented heap sort from wikipedia
	//
	public void luaSort(LuaState vm, LValue compare) {
		heapSort(m_arrayEntries, vm, compare);
	}
	
	private void heapSort(int count, LuaState vm, LValue cmpfunc) {
		heapify(count, vm, cmpfunc);
		for ( int end=count-1; end>0; ) {
			swap(end, 0);
			siftDown(0, --end, vm, cmpfunc);
		}
	}

	private void heapify(int count, LuaState vm, LValue cmpfunc) {
		for ( int start=count/2-1; start>=0; --start )
			siftDown(start, count - 1, vm, cmpfunc);
	}

	private void siftDown(int start, int end, LuaState vm, LValue cmpfunc) {
		for ( int root=start; root*2+1 <= end; ) { 
			int child = root*2+1; 
			if (child < end && compare(child, child + 1, vm, cmpfunc))
				++child; 
			if (compare(root, child, vm, cmpfunc)) {
				swap(root, child);
				root = child;
			} else
				return;
		}
	}

	private boolean compare(int i, int j, LuaState vm, LValue cmpfunc) {
		if ( ! cmpfunc.isNil() ) {
			vm.pushlvalue(cmpfunc);
			vm.pushlvalue(m_vector[i]);
			vm.pushlvalue(m_vector[j]);
			vm.call(2, 1);
			boolean result = vm.toboolean(-1);
			vm.resettop();
			return result;
		} else {
			return m_vector[j].luaBinCmpUnknown( Lua.OP_LT, m_vector[i] );
		}
	}
	
	private void swap(int i, int j) {
		LValue tmp = m_vector[i];
		m_vector[i] = m_vector[j];
		m_vector[j] = tmp;
	}

	/**
	 * Leave key,value pair on top, or nil if at end of list.
	 * @param vm the LuaState to leave the values on
	 * @param indexedonly TODO
	 * @param index index to start search
	 * @return true if next exists, false if at end of list
	 */
	public boolean next(LuaState vm, LValue key, boolean indexedonly ) {

		int n = (m_vector != null? m_vector.length: 0);
		int i = findindex(key, n, indexedonly);
		if ( i == INVALID_KEY_TO_NEXT )
			vm.error( "invalid key to 'next'" );
		
		// check vector part
		for ( ++i; i<n; ++i ) {
			if ( ! m_vector[i].isNil() ) {
				vm.pushinteger(i+1);
				vm.pushlvalue(m_vector[i]);
				return true;
			} else if ( indexedonly ) {
				vm.pushnil();
				return false;
			}
		}

		// check hash part
		if ( (! indexedonly) && (m_hashKeys != null) ) {
			int m = m_hashKeys.length;
			for ( i-=n; i<m; ++i ) {
				if ( m_hashKeys[i] != null ) {
					vm.pushlvalue(m_hashKeys[i]);
					vm.pushlvalue(m_hashValues[i]);
					return true;
				}
			}
		}
		
		// nothing found, push nil, return nil.
		vm.pushnil();
		return false;
	}

	private int findindex (LValue key, int n, boolean indexedonly) {

		// first iteration
		if ( key.isNil() )
			return -1;

		// is `key' inside array part?
		if ( key.isInteger() ) { 
			int i = key.toJavaInt();
			if ( (0 < i) && (i <= n) ) {
				if ( m_vector[i-1] == LNil.NIL )
					return INVALID_KEY_TO_NEXT;
				return i-1;				
			}
		}

		// vector only? 
		if ( indexedonly )
			return n;
		
		if ( m_hashKeys == null )
			return INVALID_KEY_TO_NEXT;
		
		// find slot
		int slot = findSlot(key);
		if ( m_hashKeys[slot] == null ) 
			return INVALID_KEY_TO_NEXT;
		
		return n + slot;
	}
	
	
	/**
	 * Executes the given f over all elements of table. For each element, f is
	 * called with the index and respective value as arguments. If f returns a
	 * non-nil value, then the loop is broken, and this value is returned as the
	 * final value of foreach.
	 * 
	 * @param vm
	 * @param function
	 * @param indexedonly is a table.foreachi() call, not a table.foreach() call
	 * @return
	 */
	public LValue foreach(LuaState vm, LFunction function, boolean indexedonly) {

		LValue key = LNil.NIL;
		while ( true ) {
			// push function onto stack
			vm.resettop();
			vm.pushlvalue(function);

			// get next value
			if ( ! next(vm,key,indexedonly) ) 
				return LNil.NIL;
			key = vm.topointer(2);
			
			// call function
			vm.call(2, 1);
			if ( ! vm.isnil(-1) )
				return vm.poplvalue();
		}
	}
}
