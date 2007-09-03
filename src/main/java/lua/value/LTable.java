package lua.value;

import lua.Lua;
import lua.VM;

/**
 * Simple implementation of table structure for Lua VM. Maintains both an array
 * part and a hash part. Does not attempt to acheive the same performance as the
 * C version.
 * 
 * Java code can put values in the table or get values out (bypassing the
 * metatable, if there is one) using put() and get(). There are specializations
 * of put() and get() for integers and Strings to avoid allocating wrapper
 * objects when possible.
 * 
 * remove() methods are private: setting a key's value to nil is the correct way
 * to remove an entry from the table.
 */
public class LTable extends LValue {

	public static final LString TYPE_NAME = new LString("table");
	
	/** Metatable tag for intercepting table gets */
	private static final LString TM_INDEX    = new LString("__index");
	
	/** Metatable tag for intercepting table sets */
	private static final LString TM_NEWINDEX = new LString("__newindex");
	
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
	private LValue[] m_hashKeys;
	
	/**
	 * Values in the hash part. Must be null when m_hashKeys is null and equal
	 * in size otherwise.
	 */
	private LValue[] m_hashValues;
	
	/**
	 * m_hashEntries is the number of slots that are used. Must always be less
	 * than m_hashKeys.length.
	 */
	private int m_hashEntries;
	
	/**
	 * Array of values to store the "array part" of the table, that is the
	 * entries with positive integer keys. Elements must never be null: "empty"
	 * slots are set to LNil.NIL.
	 */
	private LValue[] m_vector;
	
	/**
	 * Number of values in m_vector that non-nil.
	 */
	private int m_arrayEntries;
	
	private LTable m_metatable;
	
	
	
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
			put( key.luaAsInt(), val );
		} else if ( val == null || val == LNil.NIL ) {
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
		if (value == null || value == LNil.NIL) {
			remove( key );
			return;
		}
		if (checkLoadFactor())
			rehash();
		int slot = findSlot( key );
		if (fillHashSlot( slot, value ))
			return;
		m_hashKeys[slot] = new LString( key );
	}
	
	/**
	 * Method for putting an integer-keyed value. Bypasses the metatable, if
	 * any.
	 */
	public void put( int key, LValue value ) {
		if (value == null || value == LNil.NIL) {
			remove( key );
			return;
		}
		if ( key > 0 ) {
			final int index = key - 1;
			for ( ;; ) {
				if ( index < m_vector.length ) {
					if ( m_vector[index] == LNil.NIL ) {
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
		m_hashKeys[ slot ] = new LInteger( key );
	}
	
	
	/**
	 * Utility method to directly get the value in a table, without metatable
	 * calls. Must never return null, use LNil.NIL instead.
	 */
	public LValue get( LValue key ) {
		if ( m_vector.length > 0 && key.isInteger() ) {
			final int index = key.luaAsInt() - 1;
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
		
		int slot = findSlot( key );
		return ( m_hashKeys[slot] != null ) ? m_hashValues[slot] : LNil.NIL;
	}
	
	
	/**
	 * Return true if the table contains an entry with the given key, false if
	 * not. Ignores the metatable.
	 */
	public boolean containsKey( LValue key ) {
		if ( m_vector.length > 0 && key.isInteger() ) {
			final int index = key.luaAsInt() - 1;
			if ( index >= 0 && index < m_vector.length ) {
				final LValue v = m_vector[index];
				return v != LNil.NIL;
			}
		}
		if ( m_hashKeys == null )
			return false;
		final int slot = findSlot( key );
		return m_hashKeys[ slot ] != null;
	}
	
	public void luaGetTable(VM vm, LValue table, LValue key) {
		LValue v = get(key);
		if ( v == LNil.NIL && m_metatable != null ) {
			LValue event = m_metatable.get( TM_INDEX );
			if ( event != null && event != LNil.NIL ) {
				event.luaGetTable( vm, table, key );
				return;
			}
		}
		vm.push(v);
	}
	
	public void luaSetTable(VM vm, LValue table, LValue key, LValue val) {
		if ( !containsKey( key ) && m_metatable != null ) {
			LValue event = m_metatable.get( TM_NEWINDEX );
			if ( event != null && event != LNil.NIL ) {
				event.luaSetTable( vm, table, key, val );
				return;
			}
		}
		put(key, val);
	}
	
	/**
	 * Return the "length" of this table. This will not return the same result
	 * as the C version in all cases, but that's ok because the length operation
	 * on a table where the integer keys are sparse is vaguely defined.
	 */
	public LValue luaLength() {
		for ( int i = Math.max( 0, m_arrayEntries-1 ); i < m_vector.length; ++i ) {
			if ( m_vector[i] != LNil.NIL &&
					( i+1 == m_vector.length || m_vector[i+1] == LNil.NIL ) ) {
				return new LInteger( i+1 );
			}
		}
		return new LInteger( 0 );
	}
	
	/** Valid for tables */
	public LValue luaGetMetatable() {
		return this.m_metatable;
	}

	/** Valid for tables */
	public void luaSetMetatable(LValue metatable) {
		this.m_metatable = (LTable) metatable;
	}

	public String luaAsString() {
		return "table: "+id();
	}

	public LString luaGetType() {
		return TYPE_NAME;
	}
	
	/** Valid for tables */
	public LValue luaPairs() {
		return new LTableIterator(this);
	}
	
	/** Iterator for tables */
	static final class LTableIterator extends LFunction {
		private final LTable t;
		private int arrayIndex;
		private int hashIndex;
		
		private LTableIterator(LTable t) {
			this.t = t;
			this.arrayIndex = 0;
			this.hashIndex = 0;
		}
		
		// perform a lua call
		public boolean luaStackCall(VM vm) {
			vm.setResult();
			int i;
			while ( ( i = arrayIndex++ ) < t.m_vector.length ) {
				if ( t.m_vector[i] != LNil.NIL ) {
					vm.push( new LInteger( arrayIndex ) );
					vm.push( t.m_vector[ i ] );
					return false;
				}
			}
			if ( t.m_hashKeys != null ) {
				while ( ( i = hashIndex++ ) < t.m_hashKeys.length ) {
					if ( t.m_hashKeys[i] != null ) {
						vm.push( t.m_hashKeys[i] );
						vm.push( t.m_hashValues[i] );
						return false;
					}
				}
			}
			return false;
		}
	}

	/** Remove the value in the table with the given integer key. */
	private void remove( int key ) {
		if ( key > 0 ) {
			final int index = key - 1;
			if ( index < m_vector.length ) {
				if ( m_vector[ index ] != LNil.NIL ) {
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
	
	private void remove( String key ) {
		if ( m_hashKeys != null ) {
			int slot = findSlot( key );
			clearSlot( slot );
		}
	}
	
	private void remove( LValue key ) {
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

	private int findSlot( String key ) {
		// NOTE: currently LString uses the String's hashCode.
		int i = hashToIndex( key.hashCode() );
		
		// This loop is guaranteed to terminate as long as we never allow the
		// table to get 100% full.
		LValue k;
		while ( ( k = m_hashKeys[i] ) != null &&
				  !k.luaBinCmpString( Lua.OP_EQ, key ) ) {
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
			if ( m_hashKeys != null ) {
				for ( int i = oldCapacity; i < newCapacity; ++i ) {
					int slot = findSlot( i+1 );
					if ( m_hashKeys[ slot ] != null ) {
						newVector[ i ] = m_hashValues[ slot ];
						m_hashKeys[ i ] = null;
						--m_hashEntries;
					} else {
						// Make sure all array-part values are initialized to nil
						// so that we can just do one compare instead of two
						// whenever we need to check if a slot is full or not.
						newVector[ i ] = LNil.NIL;
					}
				}
			} else {
				for ( int i = oldCapacity; i < newCapacity; ++i ) {
					newVector[ i ] = LNil.NIL;
				}
			}
		} else {
			for ( int i = newCapacity; i < oldCapacity; ++i ) {
				LValue v = m_vector[i];
				if ( v != LNil.NIL ) {
					if (checkLoadFactor())
						rehash();
					final int slot = findSlot( i+1 );
					m_hashKeys[ slot ] = new LInteger( i+1 );
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
	
	LValue[] getKeys() {
		LValue[] keys = new LValue[ m_arrayEntries + m_hashEntries ];
		int out = 0;
		
		for ( int i = 0; i < m_vector.length; ++i ) {
			if ( m_vector[ i ] != LNil.NIL ) {
				keys[ out++ ] = new LInteger( i + 1 );
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
}
