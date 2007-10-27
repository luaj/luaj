package lua;

import lua.io.Closure;

public class CallInfo {

	public Closure closure;
	public int base;
    public int top;
	public int pc;
	public int resultbase;
	public int nresults;

	public CallInfo(Closure c, int base, int resultoff, int nresults) {
		this.closure = c;
		this.base = base;
		this.resultbase = resultoff;
		this.nresults = nresults;
		this.pc = 0;
	}

}
