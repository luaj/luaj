/*******************************************************************************
* Copyright (c) 2008 LuaJ. All rights reserved.
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

import java.lang.ref.WeakReference;

import org.luaj.vm.LNil;
import org.luaj.vm.LValue;

public class LWeakTable extends LTable {

	public LWeakTable() {
		super();
	}

	public LWeakTable(int narray, int nhash) {
		super(narray, nhash);
	}

	public LWeakTable(LTable copy) {
		super( copy.array.length, copy.hashKeys.length );
		for ( int i=0, k=1, n=copy.array.length; i<n; i++, k++ )
			this.put( k, copy.get(k) );
		for ( int i=0, n=copy.hashKeys.length; i<n; i++ ) {
			LValue k = copy.hashKeys[i];
			if ( k != null )
				this.put( k, copy.get(k) );
		}
	}

	protected LValue normalizeGet(Object val) {
		if ( val instanceof WeakReference )
			val = ((WeakReference)val).get();
		else if ( val != null ) {
			LUserData ud = (LUserData) val;
			Object o = ((WeakReference) ud.m_instance).get();
			if ( o != null )
				val = new LUserData(o, ud.m_metatable);
			else
				val = LNil.NIL;
		}
		return val==null? LNil.NIL: (LValue) val;
	}

	protected Object normalizePut(LValue val) {
		if ( val.isNil() ) {
			return null;
		} else if ( val.isUserData() ) {
			LUserData ud = (LUserData) val;
			return new LUserData(new WeakReference(ud.m_instance), ud.m_metatable);
		} else {
			return new WeakReference(val);
		}
	}

	public boolean next(LuaState vm, LValue key, boolean indexedonly) {
		while ( super.next(vm, key, indexedonly) ) {
			if ( ! vm.isnil(-1) )
				return true;
			vm.pop(1);
			key = vm.poplvalue();
		}
		return false;
	}

	protected void rehash() {
		final LValue[] keys = this.hashKeys;
		final Object[] values = this.hashValues;
		final int n = hashKeys.length;
		
		for ( int i = 0; i < n; ++i ) {
			if ( keys[i] != null && normalizeGet(values[i]).isNil() ) {
				// key has dropped out, clear the slot
				// It's necessary to call hashClearSlot instead of just nulling
				// out the slot because the table must be left in a consistent
				// state if an OutOfMemoryError occurs later in the rehash
				// process.
				hashClearSlot(i);
				// If number of hash entries gets to zero, hashClearSlot will
				// set hashKeys back to an empty array. Check for that so we
				// don't have an out-of-bounds index operation.
				if ( hashKeys != keys )
					break;
			}
		}
		
		// check load factor again since rehash might not be required if enough
		// entries dropped out.
		if ( checkLoadFactor() )
			super.rehash();
	}
	
}
