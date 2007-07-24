package lua.value;

import java.util.Enumeration;
import java.util.Hashtable;

import lua.VM;

public class LTable extends LValue {

	public static final LString TYPE_NAME = new LString("table");
	
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

	/** Utility method for putting a value directly, typically for initializing a table */
	public void put(String key, LValue value) {
		m_hash.put( new LString(key), value );
	}

	/** Utility method for putting something in a numbered slot */
	public void put(int i, LValue value) {
		m_hash.put( new Integer(i), value );
	}

	/** Utility method to directly get the value in a table, without metatable calls */
	public LValue get(String key) {
		return (LValue) m_hash.get( new LString(key) );
	}
	
	public void luaSetTable(VM vm, LValue table, LValue key, LValue val) {
		if ( m_metatable != null ) {
			if ( ! m_hash.containsKey(key) ) {
				LValue event = (LValue) m_metatable.m_hash.get( TM_NEWINDEX );
				if ( event != null && event != LNil.NIL ) {
					event.luaSetTable( vm, table, key, val );
					return;
				}
			}
		}
		m_hash.put( key, val );
	}

	public void luaGetTable(VM vm, LValue table, LValue key) {
		LValue val = (LValue) m_hash.get(key);
		if ( val == null || val == LNil.NIL ) {
			if ( m_metatable != null ) {
				LValue event = (LValue) m_metatable.m_hash.get( TM_INDEX );
				if ( event != null && event != LNil.NIL ) {
					event.luaGetTable( vm, table, key );
					return;
				}
			}
			val = LNil.NIL;
		}
		
		// stack.stack[base] = val;
		vm.push(val);
	}
	
	public String toString() {
		return m_hash.toString();
	}
	
	public String luaAsString() {
		return "table: "+id();
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
	private static final class LTableIterator extends LFunction {
		private final LTable t;
		private final Enumeration e;

		private LTableIterator(LTable t, Enumeration e) {
			this.t = t;
			this.e = e;
		}

		// perform a lua call
		public void luaStackCall(VM vm) {
			if ( e.hasMoreElements() ) {
				LValue key = (LValue) e.nextElement();
				LValue val = (LValue) t.m_hash.get(key);
				vm.setResult();
				vm.push( key );
				vm.push( val );
			}
		}
	}

	public LString luaGetType() {
		return TYPE_NAME;
	}

}
