package org.luaj.vm;

import java.lang.ref.WeakReference;

public class LWeakTable extends LTable {

	private static class LWeakValue extends LValue {
		private WeakReference ref;
		private LWeakValue(LValue v) {
			ref = new WeakReference(v);
		}
		public LValue toStrongReference() {
			return (LValue) ref.get();
		}
		public LValue value() {
			LValue v = (LValue) ref.get();
			return (v!=null? v: LNil.NIL);			
		}
		public String toJavaString() {
			return value().toJavaString();
		}
		public int luaGetType() {
			return value().luaGetType();
		}
		public boolean isNil() {
			return value().isNil();
		}
	}

	private static void makestrong(LuaState vm) {
		LValue v = vm.poplvalue();
		v = v.toStrongReference();
		vm.pushlvalue(v!=null? v: LNil.NIL);
	}

	/** Construct a new LTable with weak-reference keys */
	public LWeakTable() {		
	}

	/** Copy constructor */
	public LWeakTable(LTable copy) {
		this.m_arrayEntries = copy.m_arrayEntries;
		this.m_hashEntries = copy.m_hashEntries;
		if ( copy.m_vector != null ) {
			int n = copy.m_vector.length;
			this.m_vector = new LValue[n];
			for ( int i=0; i<n; i++ ) {
				this.m_vector[i] = ( copy.m_vector[i] != null?
					this.m_vector[i] = new LWeakValue(copy.m_vector[i]):
					LNil.NIL );
			}
		}
		if ( copy.m_hashKeys != null ) { 
			int n = copy.m_hashKeys.length;
			this.m_hashKeys = new LValue[n];
			this.m_hashValues = new LValue[n];
			for ( int i=0; i<n; i++ ) {
				if ( copy.m_hashKeys[i] != null ) { 
					this.m_hashKeys[i] = copy.m_hashKeys[i];
					this.m_hashValues[i] = new LWeakValue(copy.m_hashValues[i]);
				}
			}
		}
	}

	public LValue get(int key) {
		LValue v = super.get(key).toStrongReference();
		if ( v == null ) {
			super.remove(key);
			return LNil.NIL;
		}
		return v;
	}

	public LValue get(LValue key) {
		LValue v = super.get(key).toStrongReference();
		if ( v == null ) {
			super.remove(key);
			return LNil.NIL;
		}
		return v;
	}

	public void luaInsertPos(int pos, LValue value) {
		super.luaInsertPos(pos, new LWeakValue(value));
	}

	public void put(int key, LValue value) {
		super.put(key, new LWeakValue(value));
	}

	public void put(LValue key, LValue val) {
		super.put(key, new LWeakValue(val));
	}

	public void put(String key, LValue value) {
		super.put(key, new LWeakValue(value));
	}
	
	public boolean next(LuaState vm, LValue key, boolean indexedonly) {
		while ( super.next(vm, key, indexedonly) ) {
			makestrong(vm);
			if ( ! vm.isnil(-1) )
				return true;
			vm.pop(1);
			key = vm.poplvalue();
		}
		return false;
	}
}
