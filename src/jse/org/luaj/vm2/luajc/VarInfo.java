/**
 * 
 */
package org.luaj.vm2.luajc;

public class VarInfo {
	
	public static VarInfo INVALID = new VarInfo(-1,-1);
	
	public static VarInfo PARAM(int slot) {
		return new VarInfo(slot,-1) {
			public String toString() {
				return slot+".p";
			}
		};
	}
	
	public static VarInfo NIL(final int slot) {
		return new VarInfo(slot,-1) {
			public String toString() {
				return "nil";
			}
		};
	}

	public static VarInfo PHI(int slot, int pc) {
		return new VarInfo(slot,pc) {
			public String toString() {
				return super.toString()+"{}";
			}
		};
	}
	
	public final int slot; // where assigned
	public final int pc;   // where assigned, or -1 if for block inputs
	
	public UpvalInfo upvalue;    // not null if this var is an upvalue
	public boolean allocupvalue; // true if this variable allocates r/w upvalue storage
	
	public VarInfo(int slot, int pc) {
		this.slot = slot;
		this.pc = pc;
	}

	public String toString() {
		return slot<0? "x.x": 
			(slot+"."+pc);
	}
}