package org.luaj.vm;


public class CallInfo {

    public LClosure closure;
    public int base;
    public int top;
    public int pc;
    public int resultbase;
    public int nresults;

    public CallInfo(LClosure c, int base, int top, int resultoff, int nresults) {
        this.closure = c;
        this.base = base;
        this.top = top;
        this.resultbase = resultoff;
        this.nresults = nresults;
        this.pc = 0;
    }

}
