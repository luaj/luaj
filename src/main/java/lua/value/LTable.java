package lua.value;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import lua.StackState;

public class LTable extends LValue {
	
	private Hashtable m_hash = new Hashtable();
	
	public LTable() {
	}
	
	public LTable(int narray, int nhash) {
	}

	public void luaSetTable(LValue key, LValue val) {
		m_hash.put( key, val );
	}

	public LValue luaGetTable(LValue key) {
		return (LValue) m_hash.get( key );
	}
	
	public String luaAsString() {
		return m_hash.toString();
	}
	
	/** Built-in opcode LEN, for Strings and Tables */
	public LValue luaLength() {
		return new LInteger( m_hash.size() );
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
		public void luaStackCall(StackState state, int base, int nresults) {
			if ( e.hasMoreElements() ) {
				LValue key = (LValue) e.nextElement();
				state.stack[base] = key;
				state.stack[base+1] = t.luaGetTable(key);
			} else {
				state.stack[base] = LNil.NIL;
			}
			
		}
	}

}
