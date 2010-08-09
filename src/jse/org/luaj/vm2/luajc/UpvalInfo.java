/**
 * 
 */
package org.luaj.vm2.luajc;

public class UpvalInfo {
	ProtoInfo pi;    // where defined
	int slot;       // where defined
	int nvars;		// number of vars involved
	VarInfo var[];	// list of vars
	boolean rw;     // read-write
	
	public UpvalInfo(ProtoInfo pi, int pc, int slot) {
		this.pi = pi;
		this.slot = slot;
		this.nvars = 0;
		this.var = null;
		includeVars( pi.vars[slot][pc] );
		for ( int i=0; i<nvars; i++ )
			var[i].allocupvalue = testIsAllocUpvalue( var[i] );
		this.rw = nvars > 1;		
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append( pi.name );
		for ( int i=0; i<nvars; i++ ) {
			sb.append( i>0? ",": " " );
			sb.append( String.valueOf(var[i]));
		}
		if ( rw )
			sb.append( "(rw)" );
		return sb.toString();
	}
	
	private void includeVar(VarInfo v) {
		if ( v == null )
			return;
		if ( includes(v) )
			return;
		if ( nvars == 0 ) {
			var = new VarInfo[1];
		} else if ( nvars+1 >= var.length ) {
			VarInfo[] s = var;
			var = new VarInfo[nvars*2+1];
			System.arraycopy(s, 0, var, 0, nvars);				
		}
		var[nvars++] = v;
	}
	
	private boolean includes(VarInfo v) {
		for ( int i=0; i<nvars; i++ )
			if ( var[i] == v )
				return true;
		return false;
	}
	
	public void includeVars(VarInfo v) {
		int slot = this.slot;
		
		// check for previous assignment
		loop: while ( true ) {
			// invalid values terminate search
			if ( v == VarInfo.INVALID )
				return;

			// basic block for nil values is initial block
			BasicBlock b = pi.blocks[v.pc<0? 0: v.pc];
			
			// look for loops
			if ( v.upvalue == this ) {
				for ( int i=0, n=b.ninputs[slot]; i<n; i++ ) {
					v = b.inputs[slot][i];
					if ( v.upvalue != this )
						includeVars(v);
				}
				return;
			}

			// check for assignment to 2 upvalues at once
			if ( v.upvalue != null ) 
				throw new IllegalArgumentException("upvalue collision detected between "+v.upvalue+" and "+this);

			// assign the variable
			v.upvalue = this;
			this.includeVar(v);

			// nil values also terminate
			if ( v.pc == -1 )
				return;
			
			// find next variable within the basic block
			for ( int i=v.pc; i<=b.pc1; i++ ) {
				if ( pi.vars[slot][i] != v ) {
					v = pi.vars[slot][i];
					continue loop;
				}
			}
			
			// at end of basic block, go to next blocks
			for ( int i=0, n=b.next!=null? b.next.length: 0; i<n; i++ )
				includeVars(pi.vars[slot][b.next[i].pc0]);
			return;
		}
	}
	
	private boolean testIsAllocUpvalue(VarInfo v) {
		if ( v.pc < 0 )
			return true;
		BasicBlock b = pi.blocks[v.pc];
		if ( v.pc > b.pc0 )
			return pi.vars[slot][v.pc-1].upvalue != this;
		if ( b.ninputs[slot] <= 0 )
			return true;
		for ( int k=0, n=b.ninputs[slot]; k<n; k++ )
			if ( b.inputs[slot][k].upvalue != this )
				return true;
		return false;
	}

}