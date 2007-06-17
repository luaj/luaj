package lua;

import java.util.Hashtable;

import lua.value.LString;
import lua.value.LTable;
import lua.value.LValue;

/**
** `global state', shared by all threads of this state
*/
public class GlobalState {

//	typedef struct global_State {
//	  stringtable strt;  /* hash table for strings */
	Hashtable strt; /* hash table for strings */
//	  lua_Alloc frealloc;  /* function to reallocate memory */
//	  void *ud;         /* auxiliary data to `frealloc' */
//	  lu_byte currentwhite;
//	  lu_byte gcstate;  /* state of garbage collector */
//	  int sweepstrgc;  /* position of sweep in `strt' */
//	  GCObject *rootgc;  /* list of all collectable objects */
//	  GCObject **sweepgc;  /* position of sweep in `rootgc' */
//	  GCObject *gray;  /* list of gray objects */
//	  GCObject *grayagain;  /* list of objects to be traversed atomically */
//	  GCObject *weak;  /* list of weak tables (to be cleared) */
//	  GCObject *tmudata;  /* last element of list of userdata to be GC */
//	  Mbuffer buff;  /* temporary buffer for string concatentation */
	StringBuffer buff; /* temporary buffer for string concatentation */
//	  lu_mem GCthreshold;
//	  lu_mem totalbytes;  /* number of bytes currently allocated */
//	  lu_mem estimate;  /* an estimate of number of bytes actually in use */
//	  lu_mem gcdept;  /* how much GC is `behind schedule' */
//	  int gcpause;  /* size of pause between successive GCs */
//	  int gcstepmul;  /* GC `granularity' */
//	  lua_CFunction panic;  /* to be called in unprotected errors */
//	  TValue l_registry;
//	  struct lua_State *mainthread;
	StackState mainthread;
//	  UpVal uvhead;  /* head of double-linked list of all open upvalues */
//	  struct Table *mt[NUM_TAGS];  /* metatables for basic types */
//	  TString *tmname[TM_N];  /* array with tag-method names */
//	} global_State;
//
	public static LValue getGlobalsTable() {
		LTable table = new LTable();
		Builtin.addBuiltins( table );
		table.m_hash.put(new LString("_G"), table);
		return table;
	}
}
