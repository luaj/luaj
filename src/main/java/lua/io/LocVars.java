package lua.io;

import lua.value.LString;

public class LocVars {
	final LString m_varname;
	final int m_startpc;
	final int m_endpc;
	
	public LocVars(LString varname, int startpc, int endpc) {
		this.m_varname = varname;
		this.m_startpc = startpc;
		this.m_endpc = endpc;
	}

}
