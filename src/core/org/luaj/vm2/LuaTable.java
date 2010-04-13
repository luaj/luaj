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

public class LuaTable extends LuaValue {
	private static final int      MIN_HASH_CAPACITY = 2;
	private static final LuaString N = valueOf("n");
	
	protected LuaValue[] array;
	protected LuaValue[] hashKeys;
	protected LuaValue[] hashValues;
	private int hashEntries;
	private LuaValue m_metatable;
	
	public LuaTable() {
		array = NOVALS;
		hashKeys = NOVALS;
		hashValues = NOVALS;
	}

	public LuaTable(int narray, int nhash) {
		presize(narray, nhash);
	}

	public LuaTable(LuaValue[] named, LuaValue[] unnamed, Varargs lastarg) {
		int nn = (named!=null? named.length: 0);
		int nu = (unnamed!=null? unnamed.length: 0);
		int nl = (lastarg!=null? lastarg.narg(): 0);
		presize(nu+nl, nn-(nn>>1));
		for ( int i=0; i<nu; i++ )
			array[i] = unnamed[i].optvalue(null);
		if ( lastarg != null )
			for ( int i=0,n=lastarg.narg(); i<n; ++i )
				array[nu+i] = lastarg.arg(i+1).optvalue(null);
		for ( int i=0; i<nn; i+=2 )
			if (!named[i+1].isnil())
				rawset(named[i], named[i+1]);
	}

	public LuaTable(Varargs varargs) {
		this(varargs,1);
	}

	public LuaTable(Varargs varargs, int firstarg) {
		int nskip = firstarg-1;
		int n = Math.max(varargs.narg()-nskip,0);
		presize( n, 1 );
		set(N, valueOf(n));
		for ( int i=1; i<=n; i++ )
			set(i, varargs.arg(i+nskip));
	}

	private void presize(int narray, int nhash) {
		if ( nhash > 0 && nhash < MIN_HASH_CAPACITY )
			nhash = MIN_HASH_CAPACITY;
		array = (narray>0? new LuaValue[narray]: NOVALS);
		hashKeys = (nhash>0? new LuaValue[nhash]: NOVALS);
		hashValues = (nhash>0? new LuaValue[nhash]: NOVALS);
		hashEntries = 0;
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
	
	public void presize( int i ) {
		if ( i > array.length )
			array = resize( array, i );
	}

	private static LuaValue[] resize( LuaValue[] old, int n ) {
		LuaValue[] v = new LuaValue[n];
		System.arraycopy(old, 0, v, 0, old.length);
		return v;
	}
	
	public LuaValue getmetatable() {
		if ( m_metatable!=null )
			return m_metatable.rawget(METATABLE).optvalue(m_metatable);
		return m_metatable;
	}
	
	public LuaValue setmetatable(LuaValue metatable) {
		if ( m_metatable!=null && !m_metatable.rawget(METATABLE).isnil() )
			error("cannot change a protected metatable");
		m_metatable = metatable;
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
	
	private LuaValue hashget(LuaValue key) {
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
		key.checknotnil();
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

	public LuaValue remove(int pos) {
		if ( pos == 0 )
			pos = length();
		if ( pos < 1 || pos > array.length )
			return NONE;
		LuaValue v = rawget(pos);
		for ( LuaValue r=v; !r.isnil(); ) {
			r = rawget(pos+1);
			rawset(pos++, r);
		}
		return v.isnil()? NONE: v;
	}

	public void insert(int pos, LuaValue value) {
		if ( pos == 0 )
			pos = length()+1;
		while ( ! value.isnil() ) {
			LuaValue v = rawget( pos );
			rawset(pos++, value);
			value = v;
		}
	}

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

	public LuaValue getn() { 
		for ( int n=array.length; --n>0; )
			if ( array[n]!=null )
				return LuaInteger.valueOf(n+1);
		return ZERO;
	}

	/**
	 * Get the length of this table, as lua defines it.
	 */
	public int length() {
		int n=array.length+1,m=0;
		while ( !rawget(n).isnil() ) {
			m = n;
			n += array.length+hashEntries+1;
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
	
	public int maxn() {
		int n = 0;
		for ( int i=0; i<array.length; i++ )
			if ( array[i] != null )
				n = i+1;
		for ( int i=0; i<hashKeys.length; i++ ) {
			LuaValue v = hashKeys[i];
			if ( v!=null && v.isinttype() ) {
				int key = v.toint();
				if ( key > n )
					n = key;
			}
		}
		return n;
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
	 * @return key,value or nil
	 */
	public Varargs inext(LuaValue key) {
		int i = key.optint(0);
		return i<0 || i>=array.length || array[i]==null? 
				NIL: 
				varargsOf(LuaInteger.valueOf(i+1),array[i]);
	}
	
	/** 
	 * Call the supplied function once for each key-value pair
	 * 
	 * @param func function to call
	 */
	public LuaValue foreach(LuaValue func) {
		LuaValue v = NIL;
		for ( int i=0; i<array.length; i++ )
			if ( array[i] != null )
				if ( !(v = func.call(LuaInteger.valueOf(i+1), array[i])).isnil() )
					return v;
		for ( int i=0; i<hashKeys.length; i++ )
			if ( hashKeys[i] != null )
				if ( !(v = func.call(hashKeys[i], hashValues[i])).isnil() )
					return v;
		return v;
	}
	
	/** 
	 * Call the supplied function once for each key-value pair 
	 * in the contiguous array part
	 * 
	 * @param func
	 */
	public LuaValue foreachi(LuaValue func) {
		LuaValue v = NIL;
		for ( int i=0; i<array.length && array[i]!=null; i++ )
			if ( !(v = func.call(LuaInteger.valueOf(i+1), array[i])).isnil() )
				return v;
		return v;
	}
	
	// ======================= test hooks =================

	/** Value used in testing to provide the capacity of the array part */
	int arrayCapacity() {
		return array.length;
	}

	/** Value used in testing to provide the capacity of the hash part */
	int hashCapacity() {
		return hashKeys.length;
	}

	/** Value used in testing to provide the total count of elements */
	int keyCount() {
		int n = 0;
		for ( int i=0; i<array.length; i++ )
			if ( array[i] != null )
				++n;
		return n + hashEntries;
	}

	/** Value used in testing to enumerate the keys  */
	public LuaValue[] keys() {
		LuaValue[] vals = new LuaValue[keyCount()];
		int n = 0;
		for ( int i=0; i<array.length; i++ )
			if ( array[i] != null )
				vals[n++] = LuaInteger.valueOf(i+1);
		for ( int i=0; i<hashKeys.length; i++ )
			if ( hashKeys[i] != null )
				vals[n++] = hashKeys[i];
		return vals;
	}

	// ======================= hashset =================

	public void hashset(LuaValue key, LuaValue value) {
		if ( value.isnil() )
			hashRemove(key);
		else {
			if ( checkLoadFactor() )
				rehash();

			int slot = hashFindSlot( key );
			if ( hashFillSlot( slot, value ) )
				return;
			hashKeys[slot] = key;
			hashValues[slot] = value;
		}
	}
	
	public int hashFindSlot(LuaValue key) {		
		int i = ( key.hashCode() & 0x7FFFFFFF ) % hashKeys.length;
		
		// This loop is guaranteed to terminate as long as we never allow the
		// table to get 100% full.
		LuaValue k;
		while ( ( k = hashKeys[i] ) != null && !k.eq_b(key) ) {
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
	
	private void hashClearSlot( int i ) {
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
		return hashEntries+1 >= (hashCapacity - (hashCapacity>>3));
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
	
}
