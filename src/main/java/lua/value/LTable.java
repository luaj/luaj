package lua.value;

import java.util.Enumeration;
import java.util.Hashtable;

import lua.StackState;

public class LTable extends LValue {
	
	private Hashtable m_hash = new Hashtable();
	private LValue m_metatable;
	
	public LTable() {
	}
	
	public LTable(int narray, int nhash) {
	}

	public void luaSetTable(LValue key, LValue val) {
		m_hash.put( key, val );
	}

	public LValue luaGetTable(LValue key) {
		Object o = m_hash.get(key);
		return (o!=null? (LValue)o: LNil.NIL);
	}
	
	public String luaAsString() {
		return m_hash.toString();
	}
	
	/** Built-in opcode LEN, for Strings and Tables */
	public LValue luaLength() {
		return new LInteger( m_hash.size() );
	}
	
	/** Valid for tables */
	public LValue luaGetMetatable() {
		return this.m_metatable;
	}

	/** Valid for tables */
	public void luaSetMetatable(LValue metatable) {
		this.m_metatable = metatable;
	}
	
	/** Valid for tables */
	public LValue luaPairs() {
		Enumeration e = m_hash.keys();
		return new LTableIterator(this,e);
	}
	
	/** Iterator for tables */
	private static final class LTableIterator extends LValue {
		private final LTable t;
		private final Enumeration e;

		private LTableIterator(LTable t, Enumeration e) {
			this.t = t;
			this.e = e;
		}

		// perform a lua call
		public void luaStackCall(StackState state, int base, int top, int nresults) {
			if ( e.hasMoreElements() ) {
				LValue key = (LValue) e.nextElement();
				state.stack[base] = key;
				state.stack[base+1] = t.luaGetTable(key);
				state.top = base+2;
			} else {
				state.stack[base] = LNil.NIL;
				state.top = base+1;
			}
			if ( nresults >= 0 )
				state.adjustTop(base + nresults);
		}
	}

}
