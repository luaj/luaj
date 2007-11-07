package org.luaj.vm;



/*
** Function Prototypes
*/
public class LPrototype {
 	public LPrototype() {
	}

	/* constants used by the function */
	public LValue[] k; 
	public int[] code;
	/* functions defined inside the function */
	public LPrototype[] p;
	/* map from opcodes to source lines */
	public int[] lineinfo;
	/* information about local variables */
	public LocVars[] locvars;
	/* upvalue names */
	public LString[] upvalues;
	public LString  source;
	public int nups;
	public int linedefined;
	public int lastlinedefined;
	public int numparams;
	public boolean is_vararg;
	public int maxstacksize;

}
