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

import java.util.Vector;

/**
 * Subclass of {@link LuaValue} for representing lua tables. 
 * <p>
 * Almost all API's implemented in {@link LuaTable} are defined and documented in {@link LuaValue}.
 * <p>
 * If a table is needed, the one of the type-checking functions can be used such as
 * {@link #istable()}, 
 * {@link #checktable()}, or
 * {@link #opttable(LuaTable)} 
 * <p>
 * The main table operations are defined on {@link LuaValue} 
 * for getting and setting values with and without metatag processing:
 * <ul>
 * <li>{@link #get(LuaValue)}</li>  
 * <li>{@link #set(LuaValue,LuaValue)}</li>  
 * <li>{@link #rawget(LuaValue)}</li>  
 * <li>{@link #rawset(LuaValue,LuaValue)}</li>
 * <li>plus overloads such as {@link #get(String)}, {@link #get(int)}, and so on</li>  
 * </ul>
 * <p>
 * To iterate over key-value pairs from Java, use
 * <pre> {@code
 * LuaValue k = LuaValue.NIL;
 * while ( true ) {
 *    Varargs n = table.next(k);
 *    if ( (k = n.arg1()).isnil() )
 *       break;
 *    LuaValue v = n.arg(2)
 *    process( k, v )
 * }}</pre>
 * 
 * <p>
 * As with other types, {@link LuaTable} instances should be constructed via one of the table constructor 
 * methods on {@link LuaValue}:
 * <ul>
 * <li>{@link LuaValue#tableOf()} empty table</li>
 * <li>{@link LuaValue#tableOf(int, int)} table with capacity</li>
 * <li>{@link LuaValue#listOf(LuaValue[])} initialize array part</li>
 * <li>{@link LuaValue#listOf(LuaValue[], Varargs)} initialize array part</li>
 * <li>{@link LuaValue#tableOf(LuaValue[])} initialize named hash part</li>
 * <li>{@link LuaValue#tableOf(Varargs, int)} initialize named hash part</li>
 * <li>{@link LuaValue#tableOf(LuaValue[], LuaValue[])} initialize array and named parts</li>
 * <li>{@link LuaValue#tableOf(LuaValue[], LuaValue[], Varargs)} initialize array and named parts</li>
 * </ul>
 * @see LuaValue
 */
public class LuaTable extends LuaValue {
	private static final int      MIN_HASH_CAPACITY = 2;
	private static final LuaString N = valueOf("n");
	
	/** the array values */
	protected LuaValue[] array;
	
	/** the hash keys */
	protected LuaValue[] hashKeys;
	
	/** the hash values */
	protected LuaValue[] hashValues;
	
	/** the number of hash entries */
	protected int hashEntries;
	
	/** metatable for this table, or null */
	protected LuaValue m_metatable;
	
	/** Construct empty table */
	public LuaTable() {
		array = NOVALS;
		hashKeys = NOVALS;
		hashValues = NOVALS;
	}
	
	/** 
	 * Construct table with preset capacity.
	 * @param narray capacity of array part
	 * @param nhash capacity of hash part
	 */
	public LuaTable(int narray, int nhash) {
		presize(narray, nhash);
	}

	/**
	 * Construct table with named and unnamed parts. 
	 * @param named Named elements in order {@code key-a, value-a, key-b, value-b, ... }
	 * @param unnamed Unnamed elements in order {@code value-1, value-2, ... } 
	 * @param lastarg Additional unnamed values beyond {@code unnamed.length}
	 */
	public LuaTable(LuaValue[] named, LuaValue[] unnamed, Varargs lastarg) {
		int nn = (named!=null? named.length: 0);
		int nu = (unnamed!=null? unnamed.length: 0);
		int nl = (lastarg!=null? lastarg.narg(): 0);
		presize(nu+nl, nn-(nn>>1));
		for ( int i=0; i<nu; i++ )
			rawset(i+1,unnamed[i]);
		if ( lastarg != null )
			for ( int i=1,n=lastarg.narg(); i<=n; ++i )
				rawset(nu+i,lastarg.arg(i));
		for ( int i=0; i<nn; i+=2 )
			if (!named[i+1].isnil())
				rawset(named[i], named[i+1]);
	}

	/**
	 * Construct table of unnamed elements. 
	 * @param varargs Unnamed elements in order {@code value-1, value-2, ... } 
	 */
	public LuaTable(Varargs varargs) {
		this(varargs,1);
	}

	/**
	 * Construct table of unnamed elements. 
	 * @param varargs Unnamed elements in order {@code value-1, value-2, ... }
	 * @param firstarg the index in varargs of the first argument to include in the table 
	 */
	public LuaTable(Varargs varargs, int firstarg) {
		int nskip = firstarg-1;
		int n = Math.max(varargs.narg()-nskip,0);
		presize( n, 1 );
		set(N, valueOf(n));
		for ( int i=1; i<=n; i++ )
			set(i, varargs.arg(i+nskip));
	}
	
	public int type() {
		return LuaValue.TTABLE;
	}

	public String typename() {
		return "table";
	}
	
	public boolean istable() { 
		return true; 
	}
	
	public LuaTable checktable()     { 
		return this;
	}

	public LuaTable opttable(LuaTable defval)  {
		return this;
	}
	
	public void presize( int narray ) {
		if ( narray > array.length )
			array = resize( array, narray );
	}

	public void presize(int narray, int nhash) {
		if ( nhash > 0 && nhash < MIN_HASH_CAPACITY )
			nhash = MIN_HASH_CAPACITY;
		array = (narray>0? new LuaValue[narray]: NOVALS);
		hashKeys = (nhash>0? new LuaValue[nhash]: NOVALS);
		hashValues = (nhash>0? new LuaValue[nhash]: NOVALS);
		hashEntries = 0;
	}

	/** Resize the table */
	private static LuaValue[] resize( LuaValue[] old, int n ) {
		LuaValue[] v = new LuaValue[n];
		System.arraycopy(old, 0, v, 0, old.length);
		return v;
	}
	
	/** 
	 * Get the length of the array part of the table. 
	 * @return length of the array part, does not relate to count of objects in the table. 
	 */
	protected int getArrayLength() {
		return array.length;
	}

	/** 
	 * Get the length of the hash part of the table. 
	 * @return length of the hash part, does not relate to count of objects in the table. 
	 */
	protected int getHashLength() {
		return hashValues.length;
	}
	
	public LuaValue getmetatable() {
		return m_metatable;
	}
	
	public LuaValue setmetatable(LuaValue metatable) {
		m_metatable = metatable;
		LuaValue mode;
		if ( m_metatable!=null && (mode=m_metatable.rawget(MODE)).isstring() ) {
			String m = mode.tojstring();
			boolean k = m.indexOf('k')>=0;
			boolean v = m.indexOf('v')>=0;
			return changemode(k,v);
		}
		return this;
	}
	
	/** 
	 * Change the mode of a table
	 * @param weakkeys true to make the table have weak keys going forward
	 * @param weakvalues true to make the table have weak values going forward
	 * @return {@code this} or a new {@link WeakTable} if the mode change requires copying.
	 */
	protected LuaTable changemode(boolean weakkeys, boolean weakvalues) {
		if ( weakkeys || weakvalues )
			return new WeakTable(weakkeys, weakvalues, this);
		return this;
	}
	
	public LuaValue get( int key ) {
		LuaValue v = rawget(key);
		return v.isnil() && m_metatable!=null? gettable(this,valueOf(key)): v;
	}
	
	public LuaValue get( LuaValue key ) {
		LuaValue v = rawget(key);
		return v.isnil() && m_metatable!=null? gettable(this,key): v;
	}

	public LuaValue rawget( int key ) {
		if ( key>0 && key<=array.length ) 
			return array[key-1]!=null? array[key-1]: NIL;
		return hashget( LuaInteger.valueOf(key) );
	}
	
	public LuaValue rawget( LuaValue key ) {
		if ( key.isinttype() ) {
			int ikey = key.toint();
			if ( ikey>0 && ikey<=array.length ) 
				return array[ikey-1]!=null? array[ikey-1]: NIL;
		}
		return hashget( key );
	}
		
	protected LuaValue hashget(LuaValue key) {
		if ( hashEntries > 0 ) {
			LuaValue v = hashValues[hashFindSlot(key)];
			return v!=null? v: NIL;
		}
		return NIL;
	}

	public void set( int key, LuaValue value ) {
		if ( m_metatable==null || ! rawget(key).isnil() || ! settable(this,LuaInteger.valueOf(key),value) )
			rawset(key, value);
	}

	/** caller must ensure key is not nil */
	public void set( LuaValue key, LuaValue value ) {
		key.checkvalidkey();
		if ( m_metatable==null || ! rawget(key).isnil() ||  ! settable(this,key,value) )
			rawset(key, value);
	}

	public void rawset( int key, LuaValue value ) {
		if ( ! arrayset(key, value) )
			hashset( LuaInteger.valueOf(key), value );
	}

	/** caller must ensure key is not nil */
	public void rawset( LuaValue key, LuaValue value ) {
		if ( !key.isinttype() || !arrayset(key.toint(), value) )
			hashset( key, value );
	}

	/** Set an array element */
	private boolean arrayset( int key, LuaValue value ) {
		if ( key>0 && key<=array.length ) {
			array[key-1] = (value.isnil()? null: value);
			return true;
		} else if ( key==array.length+1 && !value.isnil() ) {
			expandarray();
			array[key-1] = value;
			return true;
		}
		return false;
	}

	/** Expand the array part */
	private void expandarray() {
		int n = array.length;
		int m = Math.max(2,n*2);
		array = resize(array, m);
		for ( int i=n; i<m; i++ ) {
			LuaValue k = LuaInteger.valueOf(i+1);
			LuaValue v = hashget(k);
			if ( !v.isnil() ) {
				hashset(k, NIL);
				array[i] = v;
			}
		}
	}

	/** Remove the element at a position in a list-table
	 *  
	 * @param pos the position to remove
	 * @return The removed item, or {@link #NONE} if not removed
	 */
	public LuaValue remove(int pos) {
		int n = length();
		if ( pos == 0 )
			pos = n;
		else if (pos > n)
			return NONE;
		LuaValue v = rawget(pos);
		for ( LuaValue r=v; !r.isnil(); ) {
			r = rawget(pos+1);
			rawset(pos++, r);
		}
		return v.isnil()? NONE: v;
	}

	/** Insert an element at a position in a list-table
	 *  
	 * @param pos the position to remove
	 * @param value The value to insert
	 */
	public void insert(int pos, LuaValue value) {
		if ( pos == 0 )
			pos = length()+1;
		while ( ! value.isnil() ) {
			LuaValue v = rawget( pos );
			rawset(pos++, value);
			value = v;
		}
	}

	/** Concatenate the contents of a table efficiently, using {@link Buffer}
	 * 
	 * @param sep {@link LuaString} separater to apply between elements
	 * @param i the first element index
	 * @param j the last element index, inclusive
	 * @return {@link LuaString} value of the concatenation
	 */
	public LuaValue concat(LuaString sep, int i, int j) {
		Buffer  sb = new Buffer ();
		if ( i<=j ) {
			sb.append( get(i).checkstring() );
			while ( ++i<=j ) {
				sb.append( sep );
				sb.append( get(i).checkstring() );
			}
		}
		return sb.tostring();
	}

	public int length() {
		int a = getArrayLength();
		int n = a+1,m=0;
		while ( !rawget(n).isnil() ) {
			m = n;
			n += a+getHashLength()+1;
		}
		while ( n > m+1 ) {
			int k = (n+m) / 2;
			if ( !rawget(k).isnil() )
				m = k;
			else
				n = k;
		}
		return m;
	}
	
	public LuaValue len()  { 
		return LuaInteger.valueOf(length());
	}

	public int rawlen() { 
		return length(); 
	}

	/**
	 * Get the next element after a particular key in the table 
	 * @return key,value or nil
	 */
	public Varargs next( LuaValue key ) {
		int i = 0;
		do {
			// find current key index
			if ( ! key.isnil() ) {
				if ( key.isinttype() ) { 
					i = key.toint();
					if ( i>0 && i<=array.length ) {
						if ( array[i-1] == null )
							error( "invalid key to 'next'" );
						break;
					}
				}
				if ( hashKeys.length == 0 )
					error( "invalid key to 'next'" );
				i = hashFindSlot(key);
				if ( hashKeys[i] == null )
					error( "invalid key to 'next'" );
				i += 1+array.length;
			}
		} while ( false );
		
		// check array part
		for ( ; i<array.length; ++i )
			if ( array[i] != null )
				return varargsOf(LuaInteger.valueOf(i+1),array[i]);

		// check hash part
		for ( i-=array.length; i<hashKeys.length; ++i )
			if ( hashKeys[i] != null )
				return varargsOf(hashKeys[i],hashValues[i]);
		
		// nothing found, push nil, return nil.
		return NIL;
	}
		
	/**
	 * Get the next element after a particular key in the 
	 * contiguous array part of a table 
	 * @return key,value or none
	 */
	public Varargs inext(LuaValue key) {
		int k = key.checkint() + 1;
		LuaValue v = rawget(k);
		return v.isnil()? NONE: varargsOf(LuaInteger.valueOf(k),v);
	}

	/**
	 * Set a hashtable value
	 * @param key key to set
	 * @param value value to set
	 */
	public void hashset(LuaValue key, LuaValue value) {
		if ( value.isnil() )
			hashRemove(key);
		else {
			if ( hashKeys.length == 0 ) {
				hashKeys = new LuaValue[ MIN_HASH_CAPACITY ];
				hashValues = new LuaValue[ MIN_HASH_CAPACITY ];
			}
			int slot = hashFindSlot( key );
			if ( hashFillSlot( slot, value ) )
				return;
			hashKeys[slot] = key;
			hashValues[slot] = value;
			if ( checkLoadFactor() )
				rehash();
		}
	}
	
	/** 
	 * Find the hashtable slot to use
	 * @param key key to look for
	 * @return slot to use
	 */
	public int hashFindSlot(LuaValue key) {		
		int i = ( key.hashCode() & 0x7FFFFFFF ) % hashKeys.length;
		
		// This loop is guaranteed to terminate as long as we never allow the
		// table to get 100% full.
		LuaValue k;
		while ( ( k = hashKeys[i] ) != null && !k.raweq(key) ) {
			i = ( i + 1 ) % hashKeys.length;
		}
		return i;
	}

	private boolean hashFillSlot( int slot, LuaValue value ) {
		hashValues[ slot ] = value;
		if ( hashKeys[ slot ] != null ) {
			return true;
		} else {
			++hashEntries;
			return false;
		}
	}
	
	private void hashRemove( LuaValue key ) {
		if ( hashKeys.length > 0 ) {
			int slot = hashFindSlot( key );
			hashClearSlot( slot );
		}
	}
	
	/**
	 * Clear a particular slot in the table
	 * @param i slot to clear.
	 */
	protected void hashClearSlot( int i ) {
		if ( hashKeys[ i ] != null ) {
			
			int j = i;
			int n = hashKeys.length; 
			while ( hashKeys[ j = ( ( j + 1 ) % n ) ] != null ) {
				final int k = ( ( hashKeys[ j ].hashCode() )& 0x7FFFFFFF ) % n;
				if ( ( j > i && ( k <= i || k > j ) ) ||
					 ( j < i && ( k <= i && k > j ) ) ) {
					hashKeys[ i ] = hashKeys[ j ];
					hashValues[ i ] = hashValues[ j ];
					i = j;
				}
			}
			
			--hashEntries;
			hashKeys[ i ] = null;
			hashValues[ i ] = null;
			
			if ( hashEntries == 0 ) {
				hashKeys = NOVALS;
				hashValues = NOVALS;
			}
		}
	}

	private boolean checkLoadFactor() {
		// Using a load factor of (n+1) >= 7/8 because that is easy to compute without
		// overflow or division.
		final int hashCapacity = hashKeys.length;
		return hashEntries >= (hashCapacity - (hashCapacity>>3));
	}

	private void rehash() {
		final int oldCapacity = hashKeys.length;
		final int newCapacity = oldCapacity+(oldCapacity>>2)+MIN_HASH_CAPACITY;
		
		final LuaValue[] oldKeys = hashKeys;
		final LuaValue[] oldValues = hashValues;
		
		hashKeys = new LuaValue[ newCapacity ];
		hashValues = new LuaValue[ newCapacity ];
		
		for ( int i = 0; i < oldCapacity; ++i ) {
			final LuaValue k = oldKeys[i];
			if ( k != null ) {
				final LuaValue v = oldValues[i];
				final int slot = hashFindSlot( k );
				hashKeys[slot] = k;
				hashValues[slot] = v;
			}
		}
	}
	
	// ----------------- sort support -----------------------------
	//
	// implemented heap sort from wikipedia
	//
	// Only sorts the contiguous array part. 
	//
	/** Sort the table using a comparator.
	 * @param comparator {@link LuaValue} to be called to compare elements.
	 */
	public void sort(LuaValue comparator) {
		int n = array.length;
		while ( n > 0 && array[n-1] == null )
			--n;
		if ( n > 1 ) 
			heapSort(n, comparator);
	}

	private void heapSort(int count, LuaValue cmpfunc) {
		heapify(count, cmpfunc);
		for ( int end=count-1; end>0; ) {
			swap(end, 0);
			siftDown(0, --end, cmpfunc);
		}
	}

	private void heapify(int count, LuaValue cmpfunc) {
		for ( int start=count/2-1; start>=0; --start )
			siftDown(start, count - 1, cmpfunc);
	}

	private void siftDown(int start, int end, LuaValue cmpfunc) {
		for ( int root=start; root*2+1 <= end; ) { 
			int child = root*2+1; 
			if (child < end && compare(child, child + 1, cmpfunc))
				++child; 
			if (compare(root, child, cmpfunc)) {
				swap(root, child);
				root = child;
			} else
				return;
		}
	}

	private boolean compare(int i, int j, LuaValue cmpfunc) {
		LuaValue a = array[i];
		LuaValue b = array[j];
		if ( a == null || b == null )
			return false;
		if ( ! cmpfunc.isnil() ) {
			return cmpfunc.call(a,b).toboolean();
		} else {
			return a.lt_b(b);
		}
	}
	
	private void swap(int i, int j) {
		LuaValue a = array[i];
		array[i] = array[j];
		array[j] = a;
	}
	
	/** This may be deprecated in a future release.  
	 * It is recommended to count via iteration over next() instead
	 * @return count of keys in the table 
	 * */
	public int keyCount() {
		LuaValue k = LuaValue.NIL;
		for ( int i=0; true; i++ ) {
			Varargs n = next(k);
			if ( (k = n.arg1()).isnil() )
				return i;
		}
	}
	
	/** This may be deprecated in a future release.  
	 * It is recommended to use next() instead 
	 * @return array of keys in the table 
	 * */
	public LuaValue[] keys() {
		Vector l = new Vector();
		LuaValue k = LuaValue.NIL;
		while ( true ) {
			Varargs n = next(k);
			if ( (k = n.arg1()).isnil() )
				break;
			l.addElement( k );
		}
		LuaValue[] a = new LuaValue[l.size()];
		l.copyInto(a);
		return a;
	}
	
	// equality w/ metatable processing
	public LuaValue eq( LuaValue val ) {  return eq_b(val)? TRUE: FALSE; }
	public boolean eq_b( LuaValue val )  {
		if ( this == val ) return true;
		if ( m_metatable == null || !val.istable() ) return false;
		LuaValue valmt = val.getmetatable();
		return valmt!=null && LuaValue.eqmtcall(this, m_metatable, val, valmt); 
	}

	/** Unpack all the elements of this table */
	public Varargs unpack() {
		return unpack(1, this.length());
	}

	/** Unpack all the elements of this table from element i */
	public Varargs unpack(int i) {
		return unpack(i, this.length());
	}

	/** Unpack the elements from i to j inclusive */
	public Varargs unpack(int i, int j) {
		int n = j + 1 - i;
		switch (n) {
		case 0: return NONE;
		case 1: return get(i);
		case 2: return varargsOf(get(i), get(i+1));
		default:
			if (n < 0)
				return NONE;
			LuaValue[] v = new LuaValue[n];
			while (--n >= 0)
				v[n] = get(i+n);
			return varargsOf(v);
		}
	}
}
