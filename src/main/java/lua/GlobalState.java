package lua;

import java.util.Hashtable;

import lua.value.LTable;

/**
** `global state', shared by all threads of this state
*/
public class GlobalState {

//	typedef struct global_State {
	Hashtable strt; /* hash table for strings */
	StringBuffer buff; /* temporary buffer for string concatentation */
//	  lu_mem totalbytes;  /* number of bytes currently allocated */
//	  lu_mem estimate;  /* an estimate of number of bytes actually in use */
//	  lua_CFunction panic;  /* to be called in unprotected errors */
//	  TValue l_registry;
//	  struct lua_State *mainthread;
	StackState mainthread;
//	  UpVal uvhead;  /* head of double-linked list of all open upvalues */
//	  struct Table *mt[NUM_TAGS];  /* metatables for basic types */
//	  TString *tmname[TM_N];  /* array with tag-method names */
//	} global_State;
//
	private static LTable _G;
	
	static {
		resetGlobals();
	}
	
	static public void resetGlobals() {
		_G = new LTable();
		_G .put( "_G", _G );
		Builtin.addBuiltins( _G  );
	}
	
	public static LTable getGlobalsTable() {
		return _G;
	}
}
