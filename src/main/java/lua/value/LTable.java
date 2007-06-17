package lua.value;

import java.util.Enumeration;
import java.util.Hashtable;

import lua.StackState;

public class LTable extends LValue {

	/** Metatable tag for intercepting table gets */
	private static final LString TM_INDEX    = new LString("__index");
	
	/** Metatable tag for intercepting table sets */
	private static final LString TM_NEWINDEX = new LString("__newindex");

	public Hashtable m_hash = new Hashtable();
	private LTable m_metatable;
	
	public LTable() {
	}
	
	public LTable(int narray, int nhash) {
	}

	public void luaSetTable(StackState state, int base, LValue table, LValue key, LValue val) {
		if ( m_metatable != null ) {
			if ( ! m_hash.containsKey(key) ) {
				LValue event = (LValue) m_metatable.m_hash.get( TM_NEWINDEX );
				if ( event != null && event != LNil.NIL ) {
					event.luaSetTable( state, base, table, key, val );
					return;
				}
			}
		}
		m_hash.put( key, val );
	}

	public void luaGetTable(StackState state, int base, LValue table, LValue key) {
		LValue val = (LValue) m_hash.get(key);
		if ( val == null || val == LNil.NIL ) {
			if ( m_metatable != null ) {
				LValue event = (LValue) m_metatable.m_hash.get( TM_INDEX );
				if ( event != null && event != LNil.NIL ) {
					event.luaGetTable( state, base, table, key );
					return;
				}
			}
			val = LNil.NIL;
		}
		state.stack[base] = val;
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
		this.m_metatable = (LTable) metatable;
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
				LValue val = (LValue) t.m_hash.get(key);
				state.stack[base] = key;
				state.stack[base+1] = val;
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
