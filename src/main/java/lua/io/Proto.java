package lua.io;

import lua.StackState;
import lua.value.LValue;
import lua.value.LString;

/*
** Function Prototypes
*/
public class Proto {
	public Proto(StackState l) {
	}
	public Proto() {
	}

	public LValue[] k; /* constants used by the function */
//	  TValue *k;  /* constants used by the function */
//	  Instruction *code;
	public int[] code;
//	  struct Proto **p;  /* functions defined inside the function */
	public Proto[] p;
//	  int *lineinfo;  /* map from opcodes to source lines */
	public int[] lineinfo;
//	  struct LocVar *locvars;  /* information about local variables */
	public LocVars[] locvars;
//	  TString **upvalues;  /* upvalue names */
	public LString[] upvalues;
	public LString  source;
	public int nups;
	public int sizeupvalues;
	public int sizek;  /* size of `k' */
	public int sizecode;
	public int sizep;  /* size of `p' */
	public int linedefined;
	public int lastlinedefined;
//	  GCObject *gclist;
	public int numparams;
	public boolean is_vararg;
	public int maxstacksize;

}
