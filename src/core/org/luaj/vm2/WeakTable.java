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

import java.lang.ref.WeakReference;

import org.luaj.vm2.lib.TwoArgFunction;

public class WeakTable extends LuaTable {
	private LuaTable backing;
	private boolean weakkeys,weakvalues;
	
	public WeakTable(boolean weakkeys, boolean weakvalues) {
		this(weakkeys, weakvalues, 0, 0);
	}
	protected WeakTable(boolean weakkeys, boolean weakvalues, int narray, int nhash) {
		this.backing = new LuaTable(narray, nhash);
		this.weakkeys = weakkeys;
		this.weakvalues = weakvalues;
	}
	protected WeakTable(boolean weakkeys, boolean weakvalues, LuaTable source) {
		this(weakkeys, weakvalues, source.getArrayLength(), source.getHashLength());
		Varargs n;
		LuaValue k = NIL;
		while ( !(k = ((n = source.next(k)).arg1())).isnil() )
			rawset(k, n.arg(2));
		m_metatable = source.m_metatable;
	}
	
	public void presize( int narray ) {
		backing.presize(narray);
	}

	public void presize(int narray, int nhash) {
		backing.presize(narray, nhash);
	}
	
	protected int getArrayLength() {
		return backing.getArrayLength();
	}

	protected int getHashLength() {
		return backing.getHashLength();
	}
	
	protected WeakTable changemode(boolean weakkeys, boolean weakvalues) {
		this.weakkeys = weakkeys;
		this.weakvalues = weakvalues;
		return this;
	}
	
	LuaValue weaken( LuaValue value ) {
		switch ( value.type() ) {
			case LuaValue.TFUNCTION:
			case LuaValue.TTHREAD:
			case LuaValue.TTABLE:
				return new WeakValue(value);
			case LuaValue.TUSERDATA:
				return new WeakUserdata(value);
			default:
				return value;
		}
	}
	
	public void rawset( int key, LuaValue value ) {
		if ( weakvalues )
			value = weaken( value );
		backing.set(key, value);
	}

	/** caller must ensure key is not nil */
	public void rawset( LuaValue key, LuaValue value ) {
		if ( weakvalues )
			value = weaken( value );
		if ( weakkeys ) {
			switch ( key.type() ) {
				case LuaValue.TFUNCTION:
				case LuaValue.TTHREAD:
				case LuaValue.TTABLE:
				case LuaValue.TUSERDATA:
					key = value = new WeakEntry(this, key, value);
					break;
				default:
					break;
			}
		}
		backing.set(key, value);
	}
	

	public LuaValue rawget( int key ) {
		return rawget(valueOf(key));
	}
	
	public LuaValue rawget( LuaValue key ) {
		LuaValue v = backing.rawget(key);
		if ( v.isnil() )
			return NIL;
		v = v.strongvalue();
		if ( v.isnil() )
			backing.rawset(key, NIL);
		return v;
	}
	
	public int maxn() {
		return backing.maxn();
	}


	/**
	 * Get the next element after a particular key in the table 
	 * @return key,value or nil
	 */
	public Varargs next( LuaValue key ) {
		while ( true ) {
			Varargs n = backing.next(key);
			LuaValue k = n.arg1();
			if ( k.isnil() )
				return NIL;
			LuaValue ks = k.strongkey();
			LuaValue vs = n.arg(2).strongvalue();
			if ( ks.isnil() || vs.isnil() ) {
				backing.rawset(ks, NIL);
			} else {
				return varargsOf(ks,vs);
			}
		}
	}
	
	/**
	 * Get the next element after a particular key in the 
	 * contiguous array part of a table 
	 * @return key,value or nil
	 */
	public Varargs inext(LuaValue key) {
		int k = key.optint(0)+1;
		LuaValue v = this.rawget(k);
		return v.isnil()? NIL: varargsOf(valueOf(k),v);
	}
	
	// ----------------- sort support -----------------------------
	public void sort(final LuaValue comparator) {
		backing.sort( new TwoArgFunction() {
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return comparator.call( arg1.strongvalue(), arg2.strongvalue() );
			}
		} );
	}

	static class WeakValue extends LuaValue {
		final WeakReference ref;

		protected WeakValue(LuaValue value) {
			ref = new WeakReference(value);
		}

		public int type() {
			illegal("type","weak value");
			return 0;
		}

		public String typename() {
			illegal("typename","weak value");
			return null;
		}
		
		public String toString() {
			return "weak<"+ref.get()+">";
		}

		public LuaValue strongkey() {
			Object o = ref.get();
			return o!=null? (LuaValue)o: NIL;
		}
		
		public LuaValue strongvalue() {
			Object o = ref.get();
			return o!=null? (LuaValue)o: NIL;
		}
		
		public boolean eq_b(LuaValue rhs) {
			Object o = ref.get();
			return o!=null && rhs.eq_b((LuaValue)o);
		}
	}
	
	static final class WeakUserdata extends WeakValue {
		private final WeakReference ob;
		private final WeakReference mt;

		private WeakUserdata(LuaValue value) {
			super(value);
			ob = new WeakReference(value.touserdata());
			LuaValue udmt = value.getmetatable();
			mt = udmt!=null? new WeakReference(udmt): null;
		}
		
		public LuaValue strongvalue() {
			Object u = ref.get();
			if ( u != null )
				return (LuaValue) u;
			Object o = ob.get();
			Object m = mt!=null? mt.get(): null;
			return o!=null? m!=null? userdataOf(o,(LuaValue)m): userdataOf(o): NIL;
		}
		
		public boolean eq_b(LuaValue rhs) {
			return rhs.isuserdata() && (rhs.touserdata() == ob.get());
		}
		
		public boolean isuserdata() {
			return true;
		}
		
		public Object touserdata() {
			return ob.get();
		}
	}

	static final class WeakEntry extends LuaValue {
		final WeakTable table;
		final LuaValue weakkey;
		final int keyhash;

		private WeakEntry(WeakTable table, LuaValue key, LuaValue weakvalue) {
			this.table = table;
			this.weakkey = table.weaken(key);
			this.keyhash = key.hashCode();

			// store an association from table to value in the key's metatable
			LuaValue mt = key.getmetatable();
			if ( mt == null )
				key.setmetatable(mt=new LuaTable(0,1));
			mt.set(table, weakvalue);
		}

		// when looking up the value, look in the keys metatable
		public LuaValue strongvalue() {
			LuaValue key = weakkey.strongkey();
			if ( key.isnil() )
				return NIL;
			LuaValue mt = key.getmetatable();
			if ( mt == null )
				return NIL;
			LuaValue weakvalue = mt.get(table);
			return weakvalue.strongvalue();
		}


		public int type() {
			illegal("type","weak entry");
			return 0;
		}

		public String typename() {
			illegal("typename","weak entry");
			return null;
		}
		
		public String toString() {
			return "weak<"+strongkey()+","+strongvalue()+">";
		}
		
		public int hashCode() {
			return keyhash;
		}
		
		public boolean eq_b(LuaValue rhs) {
			return rhs.eq_b(weakkey.strongkey());
		}
	}
}
