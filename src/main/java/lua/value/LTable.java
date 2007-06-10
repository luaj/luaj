package lua.value;

import java.util.Hashtable;
import java.util.Vector;

public class LTable extends LValue {
	
	private Hashtable m_hash = new Hashtable();
	private Vector m_array = new Vector();
	
	public LTable() {
	}
	
	public LTable(int narray, int nhash) {
	}

	public void luaSetTable(LValue key, LValue val) {
		m_hash.put( key.luaAsString(), val );
		m_array.add( val );
	}

	public LValue luaGetTable(LValue key) {
		return (LValue) m_hash.get( key.luaAsString() );
	}
	
	public String luaAsString() {
		return m_hash.toString();
	}
	
	/** Built-in opcode LEN, for Strings and Tables */
	public LValue luaLength() {
		return new LInteger( m_array.size() );
	}
}
