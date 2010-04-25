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

public class WeakTable extends LuaTable {

	private final boolean weakKeys,weakValues;
	
	WeakTable( boolean weakKeys, boolean weakValues ) {
		this.weakKeys = weakKeys;
		this.weakValues = weakValues;
	}

	private static class WeakValue extends LuaValue {
		private final WeakReference ref;
		public WeakValue(LuaValue val) {
			ref = new WeakReference(val);
		}
		public int type() {
			return strongvalue().type();
		}
		public String typename() {
			return "weakvalue";
		}
		public LuaValue strongvalue() {
			Object o = ref.get();
			return o!=null? (LuaValue)o: NIL;
		}
		public String tojstring() {
			return strongvalue().tojstring();
		}
	}
	
	private static class WeakUserdata extends LuaValue {
		private WeakReference ref;
		private WeakReference mt;
		public WeakUserdata(Object val, LuaValue metatable) {
			this.ref = new WeakReference(val);
			this.mt = new WeakReference(metatable);
		}
		public int type() {
			return TVALUE;
		}
		public String typename() {
			return "weakuserdata";
		}
		public LuaValue strongvalue() {
			if ( ref != null ) {
				Object o = ref.get();
				if ( o != null )
					return userdataOf( o, (LuaValue) mt.get() );
			}
			ref = mt = null;
			return NIL;
		}
	}
	
	private static class WeakEntry extends LuaValue {
		private LuaValue key;
		private LuaValue val;
		private WeakEntry(LuaValue key, LuaValue val) {
			this.key = key;
			this.val = val;
		}
		public int type() {
			return LuaValue.TNIL;
		}
		public String typename() {
			return "weakentry";
		}
		public LuaValue strongkey() {
			LuaValue k = key.strongvalue();
			LuaValue v = val.strongvalue();
			if ( k.isnil() || v.isnil() )
				return key = val = NIL;
			return k;
		}
		public LuaValue strongvalue() {
			LuaValue k = key.strongvalue();
			LuaValue v = val.strongvalue();
			if ( k.isnil() || v.isnil() )
				return key = val = NIL;
			return v;
		}
		public boolean eq_b(LuaValue rhs) {
			return strongkey().eq_b(rhs);
		}
		public int hashCode() {
			return strongkey().hashCode(); 
		}
	}
	
	
	private boolean shouldWeaken( LuaValue value ) {
		switch ( value.type() ) {
		case LuaValue.TFUNCTION:
		case LuaValue.TTHREAD: 
		case LuaValue.TTABLE: 
		case LuaValue.TUSERDATA:
			return true;
		}
		return false;
	}

	private LuaValue toWeak( LuaValue value ) {
		switch ( value.type() ) {
		case LuaValue.TFUNCTION:
		case LuaValue.TTHREAD: 
		case LuaValue.TTABLE: return new WeakValue( value );
		case LuaValue.TUSERDATA: return new WeakUserdata( value.checkuserdata(), value.getmetatable() );
		default: return value;
		}
	}

	public LuaValue rawget(int key) {
		LuaValue v = super.rawget(key);
		if ( v.isnil() )
			return NIL;
		v = v.strongvalue();
		if ( v.isnil() ) {
			// TODO: mark table for culling? 
			super.rawset(key, NIL);
		}
		return v;
	}

	public LuaValue rawget(LuaValue key) {
		LuaValue v = super.rawget(key);
		if ( v.isnil() )
			return NIL;
		v = v.strongvalue();
		if ( v.isnil() ) {
			// TODO: mark table for culling? 
			super.rawset(key, NIL);
		}
		return v;
	}
	
	public void rawset(int key, LuaValue val) {
		if ( val.isnil() || !weakValues || !shouldWeaken(val) ) {
			super.rawset(key, val);
		} else {
			super.rawset(key, toWeak(val));
		}
	}

	public void rawset(LuaValue key, LuaValue val) {
		if ( val.isnil() ) {
			super.rawset(key, val);
		} else {
			boolean weakenKey = weakKeys   && shouldWeaken(key);
			boolean weakenVal = weakValues && shouldWeaken(val);
			if ( weakenKey ) {
				WeakEntry e = new WeakEntry( toWeak(key), weakenVal? toWeak(val): val);
				super.rawset(e, e);
			} else if ( weakenVal ) {
				super.rawset(key, toWeak(val));
			} else {
				super.rawset(key, val);
			}
		}
	}
	
	protected LuaTable changemode(boolean k, boolean v) {
		if ( k!=this.weakKeys || v!=weakValues )
			return recreateas(k,v);
		return this;
	}


}
