package lua.addon.luacompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import lua.CallInfo;
import lua.GlobalState;
import lua.Lua;
import lua.StackState;
import lua.VM;
import lua.io.Closure;
import lua.value.LBoolean;
import lua.value.LDouble;
import lua.value.LFunction;
import lua.value.LInteger;
import lua.value.LNil;
import lua.value.LNumber;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LValue;

public class LuaCompat extends LFunction {

	public static InputStream STDIN = null;
	public static PrintStream STDOUT = System.out;
	public static LTable      LOADED = new LTable();
	
	public static void install() {
		LTable globals = GlobalState.getGlobalsTable();
		installNames( globals, GLOBAL_NAMES,  GLOBALS_BASE  );

		// math lib
		LTable math = new LTable();
		installNames( math, MATH_NAMES,  MATH_BASE  );
		math.put( "huge", new LDouble( Double.MAX_VALUE ) );
		math.put( "pi", new LDouble( Math.PI ) );		
		globals.put( "math", math );
		
		// string lib
		LTable string = LString.getMetatable();
		installNames( string, STRING_NAMES,  STRING_BASE  );
		globals.put( "string", string );

		// packages lib
		LTable pckg = new LTable();
		installNames( pckg,  PACKAGE_NAMES, PACKAGES_BASE );
		globals.put( "package", pckg );
		pckg.put( "loaded", LOADED );	

		// table lib
		LTable table = new LTable();
		installNames( pckg,  TABLE_NAMES, TABLES_BASE );
		globals.put( "table", pckg );
	}

	private static void installNames( LTable table, String[] names, int indexBase ) {
		for ( int i=0; i<names.length; i++ )
			table.put( names[i], new LuaCompat(indexBase+i) );
	}

	public static final String[] GLOBAL_NAMES = {
		"assert",
		"loadfile",
		"tonumber",
		"rawget",
		"setfenv",
		"select",
		"collectgarbage",
		"dofile",
		"loadstring",
		"load",
		"tostring",
		"unpack",
		"next",
		"module",
		"require",
	};
	
	public static final String[] MATH_NAMES = {
		"abs",
		"cos",
		"max",
		"min",
		"modf",
		"sin",
		"sqrt"
	};
	
	public static final String[] STRING_NAMES = {
		"byte",
		"char",
		"dump",
		"find",
		"format",
		"gmatch",
		"gsub",
		"len",
		"lower",
		"match",
		"rep",
		"reverse",
		"sub",
		"upper",
	};

	public static final String[] PACKAGE_NAMES = {
		"loalib",
		"seeall",
	};
	
	public static final String[] TABLE_NAMES = {
		"concat",
		"insert",
		"maxn",
		"remove",
		"sort",
	};
	
	private static final int GLOBALS_BASE = 0;
	private static final int ASSERT         = GLOBALS_BASE + 0;
	private static final int LOADFILE       = GLOBALS_BASE + 1;
	private static final int TONUMBER       = GLOBALS_BASE + 2;
	private static final int RAWGET         = GLOBALS_BASE + 3;
	private static final int SETFENV        = GLOBALS_BASE + 4;
	private static final int SELECT         = GLOBALS_BASE + 5;
	private static final int COLLECTGARBAGE = GLOBALS_BASE + 6;
	private static final int DOFILE         = GLOBALS_BASE + 7;
	private static final int LOADSTRING     = GLOBALS_BASE + 8;
	private static final int LOAD           = GLOBALS_BASE + 9;
	private static final int TOSTRING       = GLOBALS_BASE + 10;
	private static final int UNPACK         = GLOBALS_BASE + 11;
	private static final int NEXT           = GLOBALS_BASE + 12;
	private static final int MODULE         = GLOBALS_BASE + 13;
	private static final int REQUIRE        = GLOBALS_BASE + 14;
	
	
	private static final int MATH_BASE = 20;
	private static final int ABS     = MATH_BASE + 0;
	private static final int COS     = MATH_BASE + 1;
	private static final int MAX     = MATH_BASE + 2;
	private static final int MIN     = MATH_BASE + 3;
	private static final int MODF    = MATH_BASE + 4;
	private static final int SIN     = MATH_BASE + 5;
	private static final int SQRT    = MATH_BASE + 6;
	
	private static final int STRING_BASE = 30;
	private static final int BYTE    = STRING_BASE + 0;
	private static final int CHAR    = STRING_BASE + 1;
	private static final int DUMP    = STRING_BASE + 2;
	private static final int FIND    = STRING_BASE + 3;
	private static final int FORMAT  = STRING_BASE + 4;
	private static final int GMATCH  = STRING_BASE + 5;
	private static final int GSUB    = STRING_BASE + 6;
	private static final int LEN     = STRING_BASE + 7;
	private static final int LOWER   = STRING_BASE + 8;
	private static final int MATCH   = STRING_BASE + 9;
	private static final int REP     = STRING_BASE + 10;
	private static final int REVERSE = STRING_BASE + 11;
	private static final int SUB     = STRING_BASE + 12;
	private static final int UPPER   = STRING_BASE + 13;

	private static final int PACKAGES_BASE = 50;
	private static final int LOADLIB = PACKAGES_BASE + 0;
	private static final int SEEALL  = PACKAGES_BASE + 1;	
	
	private static final int TABLES_BASE = 60;
	private static final int CONCAT  = TABLES_BASE + 0;
	private static final int INSERT  = TABLES_BASE + 1;
	private static final int MAXN    = TABLES_BASE + 2;
	private static final int REMOVE  = TABLES_BASE + 3;
	private static final int SORT    = TABLES_BASE + 4;
	
	private final int id;

	private LuaCompat( int id ) {
		this.id = id;
	}
	
	public boolean luaStackCall( VM vm ) {
		switch ( id ) {
		case ASSERT: {
			if ( !vm.getArgAsBoolean(0) ) {
				String message;
				if ( vm.getArgCount() > 1 ) {
					message = vm.getArgAsString(1);
				} else {
					message = "assertion failed!";
				}
				throw new RuntimeException(message);
			}
			vm.setResult();
		}	break;
		case LOADFILE:
			loadfile(vm, vm.getArgAsString(0));
			break;
		case TONUMBER:
			vm.setResult( toNumber( vm ) );
			break;
		case RAWGET: {
			LValue t = vm.getArg(0);;
			LValue k = vm.getArg(1);
			LValue result = LNil.NIL;
			if ( t instanceof LTable ) {
				result = ( (LTable) t ).get( k );
			}
			vm.setResult( result );
		}	break;
		case SETFENV:
			setfenv( (StackState) vm );
			break;
		case SELECT:
			select( vm );
			break;
		case COLLECTGARBAGE:
			System.gc();
			vm.setResult();
			break;
		case DOFILE:
			dofile(vm);
			break;
		case LOADSTRING:
			loadstring(vm, vm.getArg(0), vm.getArgAsString(1));
			break;
		case LOAD:
			load(vm, vm.getArg(0), vm.getArgAsString(1));
			break;
		case TOSTRING:
			vm.setResult( tostring(vm, vm.getArg(0)) );
			break;
		case UNPACK:
			unpack(vm);
			break;
		case NEXT:
			vm.setResult( next(vm, vm.getArg(0), vm.getArgAsInt(1)) );
			break;
		case MODULE: 
			module(vm);
			break;
		case REQUIRE: 
			require(vm);
			break;
		
		// Math functions
		case ABS:
			vm.setResult( abs( vm.getArg( 0 ) ) );
			break;
		case COS:
			vm.setResult( new LDouble( Math.cos ( vm.getArgAsDouble( 0 ) ) ) );
			break;
		case MAX:
			vm.setResult( max( vm.getArg( 0 ), vm.getArg( 1 ) ) );
			break;
		case MIN:
			vm.setResult( min( vm.getArg( 0 ), vm.getArg( 1 ) ) );
			break;
		case MODF:
			modf( vm );
			break;
		case SIN:
			vm.setResult( new LDouble( Math.sin( vm.getArgAsDouble( 0 ) ) ) );
			break;
		case SQRT:
			vm.setResult( new LDouble( Math.sqrt( vm.getArgAsDouble( 0 ) ) ) );
			break;
			
		// String functions
		case BYTE:
			StrLib.byte_( vm );
			break;
		case CHAR:
			StrLib.char_( vm );
			break;
		case DUMP:
			StrLib.dump( vm );
			break;
		case FIND:
			StrLib.find( vm );
			break;
		case FORMAT:
			StrLib.format( vm );
			break;
		case GMATCH:
			StrLib.gmatch( vm );
			break;
		case GSUB:
			StrLib.gsub( vm );
			break;
		case LEN:
			StrLib.len( vm );
			break;
		case LOWER:
			StrLib.lower( vm );
			break;
		case MATCH:
			StrLib.match( vm );
			break;
		case REP:
			StrLib.rep( vm );
			break;
		case REVERSE:
			StrLib.reverse( vm );
			break;
		case SUB:
			StrLib.sub( vm );
			break;
		case UPPER:
			StrLib.upper( vm );
			break;

		// package functions
		case LOADLIB: 
			loadlib(vm);
			break;
		case SEEALL: 
			seeall(vm);
			break;

		// table library
		case CONCAT: 
			concat(vm);
			break;
		case INSERT: 
			insert(vm);
			break;
		case MAXN: 
			maxn(vm);
			break;
		case REMOVE: 
			remove(vm);
			break;
		case SORT: 
			sort(vm);
			break;
			
		default:
			luaUnsupportedOperation();
		}
		return false;
	}

	private void select( VM vm ) {
		LValue arg = vm.getArg( 0 );
		if ( arg instanceof LNumber ) {
			final int start;
			final int numResults;
			if ( ( start = arg.luaAsInt() ) > 0 &&
				 ( numResults = Math.max( vm.getArgCount() - start,
						 				  vm.getExpectedResultCount() ) ) > 0 ) {
				// since setResult trashes the arguments, we have to save them somewhere.
				LValue[] results = new LValue[numResults];
				for ( int i = 0; i < numResults; ++i ) {
					results[i] = vm.getArg( i+start );
				}
				vm.setResult();
				for ( int i = 0; i < numResults; ++i ) {
					vm.push( results[i] );
				}
				return;
			}
		} else if ( arg.luaAsString().equals( "#" ) ) {
			vm.setResult( new LInteger( vm.getArgCount() - 1 ) );
		}
		vm.setResult();
	}
	
	private LValue abs( final LValue v ) {
		LValue nv = v.luaUnaryMinus();
		return max( v, nv );
	}
	
	private LValue max( LValue lhs, LValue rhs ) {
		return rhs.luaBinCmpUnknown( Lua.OP_LT, lhs ) ? rhs: lhs;
	}
	
	private LValue min( LValue lhs, LValue rhs ) {
		return rhs.luaBinCmpUnknown( Lua.OP_LT, lhs ) ? lhs: rhs;
	}
	
	private void modf( VM vm ) {
		LValue arg = vm.getArg( 0 );
		double v = arg.luaAsDouble();
		double intPart = ( v > 0 ) ? Math.floor( v ) : Math.ceil( v );
		double fracPart = v - intPart;
		vm.setResult();
		vm.push( intPart );
		vm.push( fracPart );
	}
	
	private LValue toNumber( VM vm ) {
		LValue input = vm.getArg(0);
		if ( input instanceof LNumber ) {
			return input;
		} else if ( input instanceof LString ) {
			int base = 10;
			if ( vm.getArgCount()>1 ) {
				base = vm.getArgAsInt(1);
			}
			return ( (LString) input ).luaToNumber( base );
		}
		return LNil.NIL;
	}
	
	private void setfenv( StackState state ) {
		LValue f = state.getArg(0);
		LValue newenv = state.getArg(1);

		Closure c = null;
		
		// Lua reference manual says that first argument, f, can be a "Lua
		// function" or an integer. Lots of things extend LFunction, but only
		// instances of Closure are "Lua functions".
		if ( f instanceof Closure ) {
			c = (Closure) f;
		} else {
			int callStackDepth = f.luaAsInt();
			if ( callStackDepth > 0 ) {
				CallInfo frame = state.getStackFrame( callStackDepth );
				if ( frame != null ) {
					c = frame.closure;
				}
			} else {
				// This is supposed to set the environment of the current
				// "thread". But, we have not implemented coroutines yet.
				throw new RuntimeException( "not implemented" );
			}
		}
		
		if ( c != null ) {
			if ( newenv instanceof LTable ) {
				c.env = (LTable) newenv;
			}
			state.setResult( c );
			return;
		}
		
		state.setResult();
		return;
	}

	// closes the input stream, provided its not null or System.in
	private static void closeSafely(InputStream is) {
		try {
			if ( is != null && is != STDIN )
				is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// closes the output stream, provided its not null or STDOUT 
	private static void closeSafely(OutputStream os) {
		try {
			if ( os != null && os != STDOUT )
				os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// return true if laoded, false if error put onto the stack
	private static boolean loadis(VM vm, InputStream is, String chunkname ) {
		try {
			vm.setResult();
			if ( 0 != vm.lua_load(is, chunkname) ) {
				vm.setErrorResult( LNil.NIL, "cannot load "+chunkname+": "+vm.lua_tolvalue(-1) );
				return false;
			} else {
				return true;
			}
		} finally {
			closeSafely( is );
		}
	}
	

	// return true if loaded, false if error put onto stack
	public static boolean loadfile( VM vm, String fileName ) {
		InputStream is;
		
		String script;
		if ( ! "".equals(fileName) ) {
			script = fileName;
			is = vm.getClass().getResourceAsStream( "/"+script );
			if ( is == null ) {
				vm.setErrorResult( LNil.NIL, "cannot open "+fileName+": No such file or directory" );
				return false;
			}
		} else {
			is = STDIN;
			script = "-";
		}
		
		// use vm to load the script
		return loadis( vm, is, script );
	}
	
	// if load succeeds, return 0 for success, 1 for error (as per lua spec)
	private void dofile( VM vm ) {
		String filename = vm.getArgAsString(0);
		if ( loadfile( vm, filename ) ) {
			int s = vm.lua_pcall(1, 0);
			vm.setResult( new LInteger( s!=0? 1: 0 ) );
		} else {
			vm.lua_error("cannot open "+filename);
		}
	}

	// return true if loaded, false if error put onto stack
	private boolean loadstring(VM vm,  LValue string, String chunkname) {
		return loadis( vm, 
				string.luaAsString().toInputStream(), 
				("".equals(chunkname)? "(string)": chunkname) );
	}

	// return true if loaded, false if error put onto stack
	private boolean load(VM vm, LValue chunkPartLoader, String chunkname) {
		if ( ! (chunkPartLoader instanceof Closure) ) {
			vm.lua_error("not a closure: "+chunkPartLoader);
		}
		
		// load all the parts
		Closure c = (Closure) chunkPartLoader;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while ( true ) {
				vm.setResult(c);
				if ( 0 != vm.lua_pcall(0, 1) ) {
					vm.setErrorResult(LNil.NIL, vm.getArgAsString(0));
					return false;
				}
				LValue v = vm.getArg(0);
				if ( v == LNil.NIL )
					break;
				LString s = v.luaAsString();
				s.write(baos, 0, s.length());
			}

			// load the chunk
			return loadis( vm, 
					new ByteArrayInputStream( baos.toByteArray() ), 
					("".equals(chunkname)? "=(load)": chunkname) );
			
		} catch (IOException ioe) {
			vm.setErrorResult(LNil.NIL, ioe.getMessage());
			return false;
		} finally {
			closeSafely( baos );
		}
	}

	private LValue tostring(VM vm, LValue arg) {
		return arg.luaAsString();
	}

	/** unpack (list [, i [, j]])
	 * 
	 * Returns the elements from the given table. This function is equivalent to
	 *      return list[i], list[i+1], ···, list[j]
	 *      
	 * except that the above code can be written only for a fixed number of elements. 
	 * By default, i is 1 and j is the length of the list, as defined by the length operator (see §2.5.5).
	 */
	private void unpack(VM vm) {
		LValue v = vm.getArg(0);
		int i = vm.getArgAsInt(1);
		int j = vm.getArgAsInt(2);
		LTable list = (LTable) v;
		if ( i == 0 )
			i = 1;
		if ( j == 0 )
			j = list.luaLength().luaAsInt();
		vm.setResult();
		for ( int k=i; k<=j; k++ ) 
			vm.push( list.get(k) );
	}

	private LValue next(VM vm, LValue table, int index) {
		throw new java.lang.RuntimeException("next() not supported yet");
	}

	
	// ======================== Module, Package loading =============================
	
	public static void module( VM vm ) {		
		vm.lua_error( "module not implemented" );
	}

	/** 
	 * require (modname)
	 * 
	 * Loads the given module. The function starts by looking into the package.loaded table to 
	 * determine whether modname is already loaded. If it is, then require returns the value 
	 * stored at package.loaded[modname]. Otherwise, it tries to find a loader for the module.
	 * 
	 * To find a loader, require is guided by the package.loaders array. By changing this array, 
	 * we can change how require looks for a module. The following explanation is based on the 
	 * default configuration for package.loaders.
	 *  
	 * First require queries package.preload[modname]. If it has a value, this value 
	 * (which should be a function) is the loader. Otherwise require searches for a Lua loader 
	 * using the path stored in package.path. If that also fails, it searches for a C loader 
	 * using the path stored in package.cpath. If that also fails, it tries an all-in-one loader 
	 * (see package.loaders).
	 * 
	 * Once a loader is found, require calls the loader with a single argument, modname. 
	 * If the loader returns any value, require assigns the returned value to package.loaded[modname]. 
	 * If the loader returns no value and has not assigned any value to package.loaded[modname], 
	 * then require assigns true to this entry. In any case, require returns the final value of 
	 * package.loaded[modname]. 
	 * 
	 * If there is any error loading or running the module, or if it cannot find any loader for 
	 * the module, then require signals an error.
	 */	
	public static void require( VM vm ) {
		LString modname = vm.getArgAsLuaString(0);
		if ( LOADED.containsKey(modname) )
			vm.setResult( LOADED.get(modname) );
		else {
			String s = modname.toJavaString();
			if ( ! loadfile(vm, s+".luac") && ! loadfile(vm, s+".lua") )
				vm.lua_error( "not found: "+s );
			else if ( 0 == vm.lua_pcall(0, 1) ) {
				LValue result = vm.lua_tolvalue( -1 ); 
				if ( result != LNil.NIL )
					LOADED.put(modname, result);
				else if ( ! LOADED.containsKey(modname) )
					LOADED.put(modname, result = LBoolean.TRUE);
				vm.setResult( result );
			}
		}
	}

	public static void loadlib( VM vm ) {
		vm.lua_error( "loadlib not implemented" );
	}
	
	public static void seeall( VM vm ) {
		vm.lua_error( "seeall not implemented" );
	}
	

	// ============= tables support =============
	/** table.concat (table [, sep [, i [, j]]])
	 * 
	 * Given an array where all elements are strings or numbers, returns table[i]..sep..table[i+1] ··· sep..table[j]. 
	 * The default value for sep is the empty string, the default for i is 1, and the default for j is the length of the table. 
	 * If i is greater than j, returns the empty string.
	 */
	private void concat(VM vm) {
		LTable table = (LTable) vm.getArg(0);
		LString sep = vm.getArgAsLuaString(1);
		int i = vm.getArgAsInt(2);
		int j = vm.getArgAsInt(3);
		LValue[] keys = table.getKeys();
		if ( i == 0 ) 
			i = 1;
		if ( j == 0 ) 
			j = keys.length;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			for ( int k=i; k<=j; k++ ) {
				LValue v = table.get(keys[k-1]);
					v.luaAsString().write(baos);
					if ( k<j )
						sep.write( baos );
			}
			vm.setResult( new LString( baos.toByteArray() ) );
		} catch (IOException e) {
			vm.lua_error(e.getMessage());
		}
	}

	/** table.insert (table, [pos,] value)
	 * 
	 * Inserts element value at position pos in table, shifting up other elements to open space, if necessary. 
	 * The default value for pos is n+1, where n is the length of the table (see §2.5.5), so that a call 
	 * table.insert(t,x) inserts x at the end of table t.
	 */ 
	private void insert(VM vm) {
		int n = vm.getArgCount();
		LTable table = (LTable) vm.getArg(0);
		int pos = (n>2? vm.getArgAsInt(1): 0);
		LValue value = vm.getArg(n-1);
		table.luaInsertPos( pos, value );
	}


	/** table.maxn (table)
	 * 
	 * Returns the largest positive numerical index of the given table, or zero if the table has no positive numerical 
	 * indices. (To do its job this function does a linear traversal of the whole table.)
	 */ 
	private void maxn(VM vm) {
		LTable table = (LTable) vm.getArg(0);
		vm.setResult( new LInteger( table.luaMaxN() ) );
	}


	/** table.remove (table [, pos])
	 * 
	 * Removes from table the element at position pos, shifting down other elements to close the space, if necessary. 
	 * Returns the value of the removed element. The default value for pos is n, where n is the length of the table, 
	 * so that a call table.remove(t) removes the last element of table t.
	 */ 
	private void remove(VM vm) {
		int n = vm.getArgCount();
		LTable table = (LTable) vm.getArg(0);
		int pos = (n>1? vm.getArgAsInt(1): 0);
		table.luaRemovePos( pos );
	}

	/** table.sort (table [, comp])
	 * 
	 * Sorts table elements in a given order, in-place, from table[1] to table[n], where n is the length of the table. 
	 * If comp is given, then it must be a function that receives two table elements, and returns true when the first 
	 * is less than the second (so that not comp(a[i+1],a[i]) will be true after the sort). If comp is not given, 
	 * then the standard Lua operator &lt; is used instead.
	 *
	 * The sort algorithm is not stable; that is, elements considered equal by the given order may have their relative positions changed by the sort.
	 */ 
	private void sort(VM vm) {
		LTable table = (LTable) vm.getArg(0);
		table.luaSort();
	}
}
