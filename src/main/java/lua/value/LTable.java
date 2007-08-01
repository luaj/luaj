package lua.value;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import lua.VM;

public class LTable extends LValue {

	public static final LString TYPE_NAME = new LString("table");
	
	/** Metatable tag for intercepting table gets */
	private static final LString TM_INDEX    = new LString("__index");
	
	/** Metatable tag for intercepting table sets */
	private static final LString TM_NEWINDEX = new LString("__newindex");

	private Hashtable m_hash = new Hashtable();
	
	private Vector m_vector;  // if non-null then size() > 0
	
	// stride and offset are needed only for the
	// implementation where m_vector stores keys
	// {offset, stride + offset, ..., (size-1)*stride + offset}
	// where size = m_vector.size()
	//
	// private int stride = 1;  // always non-0; if m_vector.size() == 1 then stride == 1
	// private int offset;  // if m_vector.size() == 1 then offset is the single integer key
	
	private LTable m_metatable;
	
	public LTable() {
	}
	
	public LTable(int narray, int nhash) {
	}

	/** Utility method for putting a string-keyed value directly, typically for initializing a table */
	public void put(String key, LValue value) {
		m_hash.put( new LString(key), value );
	}
	
	public void rawSet(LValue key, LValue val) {
		
		if (key instanceof LInteger) {
			int iKey = ((LInteger) key).luaAsInt() - 1;
			
			// implementation where m_vector stores keys
			// {1, ..., size}
			// where size = m_vector.size()
			//
			if (m_vector == null) {
				if (iKey == 0) {
					m_vector = new Vector();
					m_vector.add(val);
					return;
				}
			} else if (iKey >= 0) {
				int size = m_vector.size();
				if (iKey < size) {
					m_vector.set(iKey, val);
					return;
				} else if (iKey == size) {
					m_vector.add(iKey, val);
					return;
				}
			}
			
			/*
			// implementation where m_vector stores keys
			// {offset, stride + offset, ..., (size-1)*stride + offset}
			// where size = m_vector.size()
			//
			if (m_vector == null) {
				offset = iKey;
				m_vector = new Vector();
				m_vector.add(val);
				return;
			} else {
				int size = m_vector.size();
				int multiple = iKey - offset;
				if (multiple >= 0) {
					int i = multiple / stride;
					if ((i < size) && (i * stride == multiple)) {
						m_vector.set(i, val);
						return;
					}
				} else if (size == 1) {
					stride = iKey - offset;
					m_vector.add(val);
					return;
				} else if (iKey == stride * size + offset) {
					m_vector.add(val);
					return;
				}
			}
			*/
		}
		
		m_hash.put( key, val );

		/* TODO: this is old incorrect code, kept here until metatables are fixed
		if ( m_metatable != null ) {
			if ( ! m_hash.containsKey(key) ) {
				LValue event = (LValue) m_metatable.m_hash.get( TM_NEWINDEX );
				if ( event != null && event != LNil.NIL ) {
					event.luaSetTable( vm, table, key, val );
					return;
				}
			}
		}
		*/
	}
	
	public boolean containsKey(LValue key) {
		if (m_vector != null) {
			if (key instanceof LInteger) {
				int iKey = ((LInteger) key).luaAsInt();
				if ((iKey >= 1) && (iKey <= m_vector.size())) {
					return m_vector.elementAt(iKey-1) != LNil.NIL;
				}
			}
		}
		return m_hash.containsKey( key );
	}
	
	/** Utility method to directly get the value in a table, without metatable calls */
	public LValue rawGet(LValue key) {
		
		if (m_vector != null) {
			if (key instanceof LInteger) {
				int iKey = ((LInteger) key).luaAsInt() - 1;
				
				// implementation where m_vector stores keys
				// {0, ..., size-1}
				// where size = m_vector.size()
				//
				if ((iKey >= 0) && (iKey < m_vector.size())) {
					return (LValue) m_vector.get(iKey);
				}
				
				/*
				// implementation where m_vector stores keys
				// {offset, stride + offset, ..., (size-1)*stride + offset}
				// where size = m_vector.size()
				//
				int multiple = iKey - offset;
				if (multiple >= 0) {
					int i = multiple / stride;
					if ((i < m_vector.size()) && (i * stride == multiple)) {
						vm.push((LValue) m_vector.get(i));
						return;
					}
				}
				*/
			}
		}
		
		LValue v = (LValue) m_hash.get(key);
		return ( v != null ) ? v : LNil.NIL;
	}
	
	public void luaGetTable(VM vm, LValue table, LValue key) {
		LValue v = rawGet(key);
		if ( v == LNil.NIL && m_metatable != null ) {
			LValue event = m_metatable.rawGet( TM_INDEX );
			if ( event != null && event != LNil.NIL ) {
				event.luaGetTable( vm, table, key );
				return;
			}
		}
		// TODO: table is unused -- is this correct?
		// stack.stack[base] = val;
		vm.push(v!=null? v: LNil.NIL);
	}
	
	public void luaSetTable(VM vm, LValue table, LValue key, LValue val) {
		if ( !containsKey( key ) && m_metatable != null ) {
			LValue event = m_metatable.rawGet( TM_NEWINDEX );
			if ( event != null && event != LNil.NIL ) {
				event.luaSetTable( vm, table, key, val );
				return;
			}
		}
		rawSet(key, val);
	}
	
	/* TODO: why was this overridden in the first place?
	public String toString() {
		return m_hash.toString();
	}
	*/
	
	public String luaAsString() {
		return "table: "+id();
	}
	
	/** Built-in opcode LEN, for Strings and Tables */
	public LValue luaLength() {
		int hashSize = m_hash.size();
		return new LInteger(
				m_vector == null ? hashSize : hashSize + m_vector.size()); 
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
		return new LTableIterator(this);
	}
	
	/** Iterator for tables */
	private static final class LTableIterator extends LFunction {
		private final LTable t;
		private final Enumeration e;
		private int i;

		private LTableIterator(LTable t) {
			this.t = t;
			this.e = t.m_hash.keys();
			this.i = (t.m_vector == null) ? -1 : 0;
		}

		// perform a lua call
		public boolean luaStackCall(VM vm) {
			vm.setResult();
			if ((i >= 0) && (i < t.m_vector.size())) {
				vm.push(new LInteger(i+1));
				vm.push((LValue) t.m_vector.get(i));
				++i;
			} else if ( e.hasMoreElements() ) {
				LValue key = (LValue) e.nextElement();
				vm.push( key );
				vm.push((LValue) t.m_hash.get(key));
			}
			return false;
		}
	}

	public LString luaGetType() {
		return TYPE_NAME;
	}

}
