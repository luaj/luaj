package lua.addon.compile;

import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;

import lua.Lua;
import lua.addon.compile.FuncState.BlockCnt;
import lua.io.LocVars;
import lua.io.Proto;
import lua.value.LDouble;
import lua.value.LInteger;
import lua.value.LNumber;
import lua.value.LString;

public class LexState extends LuaC {
	
	protected static final String RESERVED_LOCAL_VAR_FOR_CONTROL = "(for control)";
    protected static final String RESERVED_LOCAL_VAR_FOR_STATE = "(for state)";
    protected static final String RESERVED_LOCAL_VAR_FOR_GENERATOR = "(for generator)";
    protected static final String RESERVED_LOCAL_VAR_FOR_STEP = "(for step)";
    protected static final String RESERVED_LOCAL_VAR_FOR_LIMIT = "(for limit)";
    protected static final String RESERVED_LOCAL_VAR_FOR_INDEX = "(for index)";
    
    // keywords array
    protected static final String[] RESERVED_LOCAL_VAR_KEYWORDS = new String[] {
        RESERVED_LOCAL_VAR_FOR_CONTROL,
        RESERVED_LOCAL_VAR_FOR_GENERATOR,
        RESERVED_LOCAL_VAR_FOR_INDEX,
        RESERVED_LOCAL_VAR_FOR_LIMIT,
        RESERVED_LOCAL_VAR_FOR_STATE,
        RESERVED_LOCAL_VAR_FOR_STEP
    };
    private static final Hashtable RESERVED_LOCAL_VAR_KEYWORDS_TABLE = new Hashtable();
    static {
    	for ( int i=0; i<RESERVED_LOCAL_VAR_KEYWORDS.length; i++ )
        	RESERVED_LOCAL_VAR_KEYWORDS_TABLE.put( RESERVED_LOCAL_VAR_KEYWORDS[i], Boolean.TRUE );
    }
                               
    private static final int EOZ    = (-1);
	private static final int MAXSRC = 80;
	private static final int MAX_INT = Integer.MAX_VALUE-2;
	private static final int UCHAR_MAX = 255; // TODO, convert to unicode CHAR_MAX? 
	private static final int LUAI_MAXCCALLS = 200;
	
	private static final String LUA_QS(String s) { return "'"+s+"'"; }
	private static final String LUA_QL(Object o) { return LUA_QS(String.valueOf(o)); }
	
	
	private static final int     LUA_COMPAT_LSTR   =    1; // 1 for compatibility, 2 for old behavior
	private static final boolean LUA_COMPAT_VARARG = true;	
    
    public static boolean isReservedKeyword(String varName) {
    	return RESERVED_LOCAL_VAR_KEYWORDS_TABLE.containsKey(varName);
    }
    
	/*
	** Marks the end of a patch list. It is an invalid value both as an absolute
	** address, and as a list link (would link an element to itself).
	*/
	static final int NO_JUMP = (-1);

	/*
	** grep "ORDER OPR" if you change these enums
	*/
	static final int 
	  OPR_ADD=0, OPR_SUB=1, OPR_MUL=2, OPR_DIV=3, OPR_MOD=4, OPR_POW=5,
	  OPR_CONCAT=6,
	  OPR_NE=7, OPR_EQ=8,
	  OPR_LT=9, OPR_LE=10, OPR_GT=11, OPR_GE=12,
	  OPR_AND=13, OPR_OR=14,
	  OPR_NOBINOPR=15;

	static final int 
		OPR_MINUS=0, OPR_NOT=1, OPR_LEN=2, OPR_NOUNOPR=3;

	/* exp kind */
	static final int 	  
	  VVOID = 0,	/* no value */
	  VNIL = 1,
	  VTRUE = 2,
	  VFALSE = 3,
	  VK = 4,		/* info = index of constant in `k' */
	  VKNUM = 5,	/* nval = numerical value */
	  VLOCAL = 6,	/* info = local register */
	  VUPVAL = 7,       /* info = index of upvalue in `upvalues' */
	  VGLOBAL = 8,	/* info = index of table, aux = index of global name in `k' */
	  VINDEXED = 9,	/* info = table register, aux = index register (or `k') */
	  VJMP = 10,		/* info = instruction pc */
	  VRELOCABLE = 11,	/* info = instruction pc */
	  VNONRELOC = 12,	/* info = result register */
	  VCALL = 13,	/* info = instruction pc */
	  VVARARG = 14;	/* info = instruction pc */
	
	/* semantics information */
	private static class SemInfo {
		LNumber r;
		LString ts;
	};

	private static class Token {
		int token;
		final SemInfo seminfo = new SemInfo();
		public void set(Token other) {
			this.token = other.token;
			this.seminfo.r = other.seminfo.r;
			this.seminfo.ts = other.seminfo.ts;
		}
	};
	
	int current;  /* current character (charint) */
	int linenumber;  /* input line counter */
	int lastline;  /* line of last token `consumed' */
	final Token t = new Token();  /* current token */
	final Token lookahead = new Token();  /* look ahead token */
	FuncState fs;  /* `FuncState' is private to the parser */
	Compiler L;
	Reader z;  /* input stream */
	char[] buff;  /* buffer for tokens */
	int nbuff; /* length of buffer */
	LString source;  /* current source name */
	char decpoint;  /* locale decimal point */

	/* ORDER RESERVED */
	final static String luaX_tokens [] = {
	    "and", "break", "do", "else", "elseif",
	    "end", "false", "for", "function", "if",
	    "in", "local", "nil", "not", "or", "repeat",
	    "return", "then", "true", "until", "while",
	    "..", "...", "==", ">=", "<=", "~=",
	    "<number>", "<name>", "<string>", "<eof>",
	};

	final static int 
		/* terminal symbols denoted by reserved words */
		TK_AND=257,  TK_BREAK=258, TK_DO=259, TK_ELSE=260, TK_ELSEIF=261, 
		TK_END=262, TK_FALSE=263, TK_FOR=264, TK_FUNCTION=265, TK_IF=266, 
		TK_IN=267, TK_LOCAL=268, TK_NIL=269, TK_NOT=270, TK_OR=271, TK_REPEAT=272,
		TK_RETURN=273, TK_THEN=274, TK_TRUE=275, TK_UNTIL=276, TK_WHILE=277,
		/* other terminal symbols */
		TK_CONCAT=278, TK_DOTS=279, TK_EQ=280, TK_GE=281, TK_LE=282, TK_NE=283, 
		TK_NUMBER=284, TK_NAME=285, TK_STRING=286, TK_EOS=287;

	final static int FIRST_RESERVED = TK_AND;
	final static int NUM_RESERVED = TK_WHILE+1-FIRST_RESERVED;
	
	final static Hashtable RESERVED = new Hashtable();
	static {
		for ( int i=0; i<NUM_RESERVED; i++ ) {
			LString ts = new LString( luaX_tokens[i] );
			RESERVED.put(ts, new Integer(FIRST_RESERVED+i));
		}
	}

	private boolean isalnum(int c) {
		return (c >= '0' && c <= '9') 
			|| (c >= 'a' && c <= 'z')
			|| (c >= 'A' && c <= 'Z')
			|| (c == '_');
		// return Character.isLetterOrDigit(c);
	}
	
	private boolean isalpha(int c) {
		return (c >= 'a' && c <= 'z')
			|| (c >= 'A' && c <= 'Z');
	}
	
	private boolean isdigit(int c) {
		return (c >= '0' && c <= '9'); 
	}
	
	private boolean isspace(int c) {
		return (c <= ' ');
	}
	
	
	public LexState(Compiler state, Reader reader) {
		this.z = reader;
		this.buff = new char[32];
		this.L = state;
	}

	void nextChar() {
		try {
 			current = z.read();
		} catch ( IOException e ) {
			e.printStackTrace();
			current = EOZ;
		}
	}

	boolean currIsNewline() {
		return current == '\n' || current == '\r';
	}

	void save_and_next() {
		save( current );
		nextChar();
	}

	void save( int c) {
		if ( buff == null || nbuff + 1 > buff.length )
			buff = realloc( buff, nbuff*2+1 );
		buff[nbuff++] = (char) c;
	}


	String token2str( int token ) {
		if ( token < FIRST_RESERVED ) {
			return iscntrl(token)? 
					L.pushfstring( "char("+((int)token)+")" ):
					L.pushfstring( String.valueOf( (char) token ) );
		} else {
			return luaX_tokens[token-FIRST_RESERVED];
		}
	}

	private static boolean iscntrl(int token) {
		return token < ' ';
	}

	String txtToken(int token) {
		switch ( token ) {
		case TK_NAME:
		case TK_STRING:
		case TK_NUMBER:
			return new String( buff, 0, nbuff );
		default:
			return token2str( token );
		}
	}

	void lexerror( String msg, int token ) {
		String cid = chunkid( source.toString() ); // TODO: get source name from source
		L.pushfstring( cid+":"+linenumber+": "+msg );
		if ( token != 0 )
			L.pushfstring( "syntax error: "+msg+" near "+txtToken(token) );
		throw new RuntimeException(cid+":"+linenumber+": "+msg);
	}

	String chunkid( String source ) {
		 if ( source.startsWith("=") )
			 return source.substring(1);
		 String end = "";
		 if ( source.startsWith("@") ) {
			 source = source.substring(1);
		 } else {
			 source = "[string \""+source;
			 end = "\"]";
		 }
		 int n = source.length() + end.length(); 
		 if ( n > MAXSRC )
			 source = source.substring(0,MAXSRC-end.length()-3) + "...";
		 return source + end;
	}

	void syntaxerror( String msg ) {
		lexerror( msg, t.token );
	}

	LString newstring( char[] chars, int offset, int len) {
		return newstring( new String(chars, offset, len) );
	}

	LString newstring( String s ) {
		return L.newTString( s );
	}

	void inclinenumber() {
		int old = current;
		_assert( currIsNewline() );
		nextChar(); /* skip '\n' or '\r' */
		if ( currIsNewline() && current != old )
			nextChar(); /* skip '\n\r' or '\r\n' */
		if ( ++linenumber >= MAX_INT )
			syntaxerror("chunk has too many lines");
	}

	void setinput( Compiler L, int firstByte, Reader z, LString source ) {
		this.decpoint = '.';
		this.L = L;
		this.lookahead.token = TK_EOS; /* no look-ahead token */
		this.z = z;
		this.fs = null;
		this.linenumber = 1;
		this.lastline = 1;
		this.source = source;
		this.nbuff = 0;   /* initialize buffer */
		this.current = firstByte; /* read first char */
		this.skipShebang();
	}
	
	private void skipShebang() {
		if ( current == '#' )
			while (!currIsNewline() && current != EOZ)
				nextChar();
	}
	


	/*
	** =======================================================
	** LEXICAL ANALYZER
	** =======================================================
	*/


	boolean check_next(String set) {
		if (set.indexOf(current) < 0)
			return false;
		save_and_next();
		return true;
	}

	void buffreplace(char from, char to) {
		int n = nbuff;
		char[] p = buff;
		while ((--n) >= 0)
			if (p[n] == from)
				p[n] = to;
	}

	boolean str2d(String str, SemInfo seminfo) {
		double d;
		str = str.trim(); // TODO: get rid of this
		if ( str.startsWith("0x") ) {
			d = Long.parseLong(str.substring(2), 16);
		}
		else
			d = Double.parseDouble(str);
		seminfo.r = new LDouble(d);
		return true;
	}

	//
	// TODO: reexamine this source and see if it should be ported differently
	//
	// static void trydecpoint (LexState *ls, SemInfo *seminfo) {
	//	  /* format error: try to update decimal point separator */
	//	  struct lconv *cv = localeconv();
	//	  char old = this.decpoint;
	//	  this.decpoint = (cv ? cv->decimal_point[0] : '.');
	//	  buffreplace(ls, old, this.decpoint);  /* try updated decimal separator */
	//	  if (!luaO_str2d(luaZ_buffer(this.buff), &seminfo->r)) {
	//	    /* format error with correct decimal point: no more options */
	//	    buffreplace(ls, this.decpoint, '.');  /* undo change (for error message) */
	//	    luaX_lexerror(ls, "malformed number", TK_NUMBER);
	//	  }
	//	}
	//
	/*
	void trydecpoint(String str, SemInfo seminfo) {
		NumberFormat nf = NumberFormat.getInstance();
		try {
			Number n = nf.parse(str);
			double d = n.doubleValue();
			seminfo.r = new LDouble(d);
		} catch (ParseException e) {
			lexerror("malformed number", TK_NUMBER);
		}
	}
	*/

	void read_numeral(SemInfo seminfo) {
		_assert (isdigit(current));
		do {
			save_and_next();
		} while (isdigit(current) || current == '.');
		if (check_next("Ee")) /* `E'? */
			check_next("+-"); /* optional exponent sign */
		while (isalnum(current) || current == '_')
			save_and_next();
		save('\0');
		buffreplace('.', decpoint); /* follow locale for decimal point */
		String str = new String(buff, 0, nbuff);
//		if (!str2d(str, seminfo)) /* format error? */
//			trydecpoint(str, seminfo); /* try to update decimal point separator */
		str2d(str, seminfo);
	}

	int skip_sep() {
		int count = 0;
		int s = current;
		_assert (s == '[' || s == ']');
		save_and_next();
		while (current == '=') {
			save_and_next();
			count++;
		}
		return (current == s) ? count : (-count) - 1;
	}

	void read_long_string(SemInfo seminfo, int sep) {
		int cont = 0;
		save_and_next(); /* skip 2nd `[' */
		if (currIsNewline()) /* string starts with a newline? */
			inclinenumber(); /* skip it */
		for (boolean endloop = false; !endloop;) {
			switch (current) {
			case EOZ:
				lexerror((seminfo != null) ? "unfinished long string"
						: "unfinished long comment", TK_EOS);
				break; /* to avoid warnings */
			case '[': {
				if (skip_sep() == sep) {
					save_and_next(); /* skip 2nd `[' */
					cont++;
					if (LUA_COMPAT_LSTR == 1) {
						if (sep == 0)
							lexerror("nesting of [[...]] is deprecated", '[');
					}
				}
				break;
			}
			case ']': {
				if (skip_sep() == sep) {
					save_and_next(); /* skip 2nd `]' */
					if (LUA_COMPAT_LSTR == 2) {
						cont--;
						if (sep == 0 && cont >= 0)
							break;
					}
					endloop = true;
				}
				break;
			}
			case '\n':
			case '\r': {
				save('\n');
				inclinenumber();
				if (seminfo == null)
					nbuff = 0; /* avoid wasting space */
				break;
			}
			default: {
				if (seminfo != null)
					save_and_next();
				else
					nextChar();
			}
			}
		}
		if (seminfo != null)
			seminfo.ts = newstring(buff, 2 + sep, nbuff - 2 * (2 + sep));
	}

	void read_string(int del, SemInfo seminfo) {
		save_and_next();
		while (current != del) {
			switch (current) {
			case EOZ:
				lexerror("unfinished string", TK_EOS);
				continue; /* to avoid warnings */
			case '\n':
			case '\r':
				lexerror("unfinished string", TK_STRING);
				continue; /* to avoid warnings */
			case '\\': {
				int c;
				nextChar(); /* do not save the `\' */
				switch (current) {
				case 'a': /* bell */
					c = '\u0007';
					break;
				case 'b': /* backspace */
					c = '\b';
					break;
				case 'f': /* form feed */
					c = '\f';
					break;
				case 'n': /* newline */
					c = '\n';
					break;
				case 'r': /* carriage return */
					c = '\r';
					break;
				case 't': /* tab */
					c = '\t';
					break;
				case 'v': /* vertical tab */
					c = '\u000B';
					break;
				case '\n': /* go through */
				case '\r':
					save('\n');
					inclinenumber();
					continue;
				case EOZ:
					continue; /* will raise an error next loop */
				default: {
					if (!isdigit(current))
						save_and_next(); /* handles \\, \", \', and \? */
					else { /* \xxx */
						int i = 0;
						c = 0;
						do {
							c = 10 * c + (current - '0');
							nextChar();
						} while (++i < 3 && isdigit(current));
						if (c > UCHAR_MAX)
							lexerror("escape sequence too large", TK_STRING);
						save(c);
					}
					continue;
				}
				}
				save(c);
				nextChar();
				continue;
			}
			default:
				save_and_next();
			}
		}
		save_and_next(); /* skip delimiter */
		seminfo.ts = newstring(buff, 1, nbuff - 2);
	}

	int llex(SemInfo seminfo) {
		nbuff = 0;
		while (true) {
			switch (current) {
			case '\n':
			case '\r': {
				inclinenumber();
				continue;
			}
			case '-': {
				nextChar();
				if (current != '-')
					return '-';
				/* else is a comment */
				nextChar();
				if (current == '[') {
					int sep = skip_sep();
					nbuff = 0; /* `skip_sep' may dirty the buffer */
					if (sep >= 0) {
						read_long_string(null, sep); /* long comment */
						nbuff = 0;
						continue;
					}
				}
				/* else short comment */
				while (!currIsNewline() && current != EOZ)
					nextChar();
				continue;
			}
			case '[': {
				int sep = skip_sep();
				if (sep >= 0) {
					read_long_string(seminfo, sep);
					return TK_STRING;
				} else if (sep == -1)
					return '[';
				else
					lexerror("invalid long string delimiter", TK_STRING);
			}
			case '=': {
				nextChar();
				if (current != '=')
					return '=';
				else {
					nextChar();
					return TK_EQ;
				}
			}
			case '<': {
				nextChar();
				if (current != '=')
					return '<';
				else {
					nextChar();
					return TK_LE;
				}
			}
			case '>': {
				nextChar();
				if (current != '=')
					return '>';
				else {
					nextChar();
					return TK_GE;
				}
			}
			case '~': {
				nextChar();
				if (current != '=')
					return '~';
				else {
					nextChar();
					return TK_NE;
				}
			}
			case '"':
			case '\'': {
				read_string(current, seminfo);
				return TK_STRING;
			}
			case '.': {
				save_and_next();
				if (check_next(".")) {
					if (check_next("."))
						return TK_DOTS; /* ... */
					else
						return TK_CONCAT; /* .. */
				} else if (!isdigit(current))
					return '.';
				else {
					read_numeral(seminfo);
					return TK_NUMBER;
				}
			}
			case EOZ: {
				return TK_EOS;
			}
			default: {
				if (isspace(current)) {
					_assert (!currIsNewline());
					nextChar();
					continue;
				} else if (isdigit(current)) {
					read_numeral(seminfo);
					return TK_NUMBER;
				} else if (isalpha(current) || current == '_') {
					/* identifier or reserved word */
					LString ts;
					do {
						save_and_next();
					} while (isalnum(current) || current == '_');
					ts = newstring(buff, 0, nbuff);
					if ( RESERVED.containsKey(ts) )
						return ((Integer)RESERVED.get(ts)).intValue();
					else {
						seminfo.ts = ts;
						return TK_NAME;
					}
				} else {
					int c = current;
					nextChar();
					return c; /* single-char tokens (+ - / ...) */
				}
			}
			}
		}
	}

	void next() {
		lastline = linenumber;
		if (lookahead.token != TK_EOS) { /* is there a look-ahead token? */
			t.set( lookahead ); /* use this one */
			lookahead.token = TK_EOS; /* and discharge it */
		} else
			t.token = llex(t.seminfo); /* read next token */
	}

	void lookahead() {
		_assert (lookahead.token == TK_EOS);
		lookahead.token = llex(lookahead.seminfo);
	}

	// =============================================================
	// from lcode.h
	// =============================================================
	
	
	// =============================================================
	// from lparser.c
	// =============================================================

	static class expdesc {
		int k; // expkind, from enumerated list, above
		static class U { // originally a union
			static class S {
				int info, aux;
			}
			final S s = new S();
			private LNumber _nval;
			public void setNval(LNumber r) {
				_nval = r;
			}
			public LNumber nval() {
				return (_nval == null? LInteger.valueOf(s.info): _nval);
			}
		};
		final U u = new U();
		final IntPtr t = new IntPtr(); /* patch list of `exit when true' */
		final IntPtr f = new IntPtr(); /* patch list of `exit when false' */
		void init( int k, int i ) {
			this.f.i = NO_JUMP;
			this.t.i = NO_JUMP;
			this.k = k;
			this.u.s.info = i;
		}

		boolean hasjumps() {
			return (t.i != f.i);
		}

		boolean isnumeral() {
			return (k == VKNUM && t.i == NO_JUMP && f.i == NO_JUMP);
		}

		public void setvalue(expdesc other) {
			this.k = other.k;
			this.u._nval = other.u._nval;
			this.u.s.info = other.u.s.info;
			this.u.s.aux = other.u.s.aux;
			this.t.i = other.t.i;
			this.f.i = other.f.i;
		}
	}
		
	boolean hasmultret(int k) {
		return ((k) == VCALL || (k) == VVARARG);
	}

	/*----------------------------------------------------------------------
	name		args	description
	------------------------------------------------------------------------*/
	
	/*
	 * * prototypes for recursive non-terminal functions
	 */

	void error_expected(int token) {
		syntaxerror(L.pushfstring(LUA_QS(token2str(token)) + " expected"));
	}

	boolean testnext(int c) {
		if (t.token == c) {
			next();
			return true;
		} else
			return false;
	}

	void check(int c) {
		if (t.token != c)
			error_expected(c);
	}

	void checknext (int c) {
	  check(c);
	  next();
	}

	void check_condition(boolean c, String msg) {
		if (!(c))
			syntaxerror(msg);
	}


	void check_match(int what, int who, int where) {
		if (!testnext(what)) {
			if (where == linenumber)
				error_expected(what);
			else {
				syntaxerror(L.pushfstring(LUA_QS(token2str(what))
						+ " expected " + "(to close " + LUA_QS(token2str(who))
						+ " at line " + where + ")"));
			}
		}
	}

	LString str_checkname() {
		LString ts;
		check(TK_NAME);
		ts = t.seminfo.ts;
		next();
		return ts;
	}
	
	void codestring(expdesc e, LString s) {
		e.init(VK, fs.stringK(s));
	}

	void checkname(expdesc e) {
		codestring(e, str_checkname());
	}

	
	int registerlocalvar(LString varname) {
		FuncState fs = this.fs;
		Proto f = fs.f;
		if (f.locvars == null || fs.nlocvars + 1 > f.locvars.length)
			f.locvars = realloc( f.locvars, fs.nlocvars*2+1 );
		f.locvars[fs.nlocvars] = new LocVars(varname,0,0);
		return fs.nlocvars++;
	}

	
//
//	#define new_localvarliteral(ls,v,n) \
//	  this.new_localvar(luaX_newstring(ls, "" v, (sizeof(v)/sizeof(char))-1), n)
//
	void new_localvarliteral(String v, int n) {
		LString ts = newstring(v);
		new_localvar(ts, n);
	}

	void new_localvar(LString name, int n) {
		FuncState fs = this.fs;
		fs.checklimit(fs.nactvar + n + 1, FuncState.LUAI_MAXVARS, "local variables");
		fs.actvar[fs.nactvar + n] = (short) registerlocalvar(name);
	}

	void adjustlocalvars(int nvars) {
		FuncState fs = this.fs;
		fs.nactvar = (byte) (fs.nactvar + nvars);
		for (; nvars > 0; nvars--) {
			fs.getlocvar(fs.nactvar - nvars).startpc = fs.pc;
		}
	}

	void removevars(int tolevel) {
		FuncState fs = this.fs;
		while (fs.nactvar > tolevel)
			fs.getlocvar(--fs.nactvar).endpc = fs.pc;
	}
	
	void singlevar(expdesc var) {
		LString varname = this.str_checkname();
		FuncState fs = this.fs;
		if (fs.singlevaraux(varname, var, 1) == VGLOBAL)
			var.u.s.info = fs.stringK(varname); /* info points to global name */
	}
	
	void adjust_assign(int nvars, int nexps, expdesc e) {
		FuncState fs = this.fs;
		int extra = nvars - nexps;
		if (hasmultret(e.k)) {
			/* includes call itself */
			extra++;
			if (extra < 0)
				extra = 0;
			/* last exp. provides the difference */
			fs.setreturns(e, extra);
			if (extra > 1)
				fs.reserveregs(extra - 1);
		} else {
			/* close last expression */
			if (e.k != VVOID)
				fs.exp2nextreg(e);
			if (extra > 0) {
				int reg = fs.freereg;
				fs.reserveregs(extra);
				fs.nil(reg, extra);
			}
		}
	}
	
	void enterlevel() {
		if (++L.nCcalls > LUAI_MAXCCALLS)
			lexerror("chunk has too many syntax levels", 0);
	}
	
	void leavelevel() {
		L.nCcalls--;
	}
	
	void pushclosure(FuncState func, expdesc v) {
		FuncState fs = this.fs;
		Proto f = fs.f;
		if (f.p == null || fs.np + 1 > f.p.length)
			f.p = realloc( f.p, fs.np*2 + 1 );
		f.p[fs.np++] = func.f;
		v.init(VRELOCABLE, fs.codeABx(Lua.OP_CLOSURE, 0, fs.np - 1));
		for (int i = 0; i < func.f.nups; i++) {
			int o = (func.upvalues[i].k == VLOCAL) ? Lua.OP_MOVE
					: Lua.OP_GETUPVAL;
			fs.codeABC(o, 0, func.upvalues[i].info, 0);
		}
	}
	
	void open_func (FuncState fs) {
		  Compiler L = this.L;
		  Proto f = new Proto();
		  if ( this.fs!=null )
			  f.source = this.fs.f.source;
		  fs.f = f;
		  fs.prev = this.fs;  /* linked list of funcstates */
		  fs.ls = this;
		  fs.L = L;
		  this.fs = fs;
		  fs.pc = 0;
		  fs.lasttarget = -1;
		  fs.jpc = new IntPtr( NO_JUMP );
		  fs.freereg = 0;
		  fs.nk = 0;
		  fs.np = 0;
		  fs.nlocvars = 0;
		  fs.nactvar = 0;
		  fs.bl = null;
		  f.maxstacksize = 2;  /* registers 0/1 are always valid */
		  //fs.h = new LTable();
		  fs.htable = new Hashtable();
		  fs.varargflags = 0;
	}

	void close_func() {
		Compiler L = this.L;
		FuncState fs = this.fs;
		Proto f = fs.f;
		this.removevars(0);
		fs.ret(0, 0); /* final return */
		f.code = realloc(f.code, fs.pc);
		f.lineinfo = realloc(f.lineinfo, fs.pc);
		// f.sizelineinfo = fs.pc;
		f.k = realloc(f.k, fs.nk);
		f.p = realloc(f.p, fs.np);
		f.locvars = realloc(f.locvars, fs.nlocvars);
		// f.sizelocvars = fs.nlocvars;
		f.upvalues = realloc(f.upvalues, f.nups);
		_assert (CheckCode.checkcode(f));
		_assert (fs.bl == null);
		this.fs = fs.prev;
//		L.top -= 2; /* remove table and prototype from the stack */
		// /* last token read was anchored in defunct function; must reanchor it
		// */
		// if (fs!=null) ls.anchor_token();
	}

	/*============================================================*/
	/* GRAMMAR RULES */
	/*============================================================*/

	void field(expdesc v) {
		/* field -> ['.' | ':'] NAME */
		FuncState fs = this.fs;
		expdesc key = new expdesc();
		fs.exp2anyreg(v);
		this.next(); /* skip the dot or colon */
		this.checkname(key);
		fs.indexed(v, key);
	}
	
	void yindex(expdesc v) {
		/* index -> '[' expr ']' */
		this.next(); /* skip the '[' */
		this.expr(v);
		this.fs.exp2val(v);
		this.checknext(']');
	}


 /*
	** {======================================================================
	** Rules for Constructors
	** =======================================================================
	*/


	static class ConsControl {
		expdesc v = new expdesc(); /* last list item read */
		expdesc t; /* table descriptor */
		int nh; /* total number of `record' elements */
		int na; /* total number of array elements */
		int tostore; /* number of array elements pending to be stored */
	};


	void recfield(ConsControl cc) {
		/* recfield -> (NAME | `['exp1`]') = exp1 */
		FuncState fs = this.fs;
		int reg = this.fs.freereg;
		expdesc key = new expdesc();
		expdesc val = new expdesc();
		int rkkey;
		if (this.t.token == TK_NAME) {
			fs.checklimit(cc.nh, MAX_INT, "items in a constructor");
			this.checkname(key);
		} else
			/* this.t.token == '[' */
			this.yindex(key);
		cc.nh++;
		this.checknext('=');
		rkkey = fs.exp2RK(key);
		this.expr(val);
		fs.codeABC(Lua.OP_SETTABLE, cc.t.u.s.info, rkkey, fs.exp2RK(val));
		fs.freereg = reg; /* free registers */
	}

	void listfield (ConsControl cc) {
	  this.expr(cc.v);
	  fs.checklimit(cc.na, MAX_INT, "items in a constructor");
	  cc.na++;
	  cc.tostore++;
	}


	void constructor(expdesc t) {
		/* constructor -> ?? */
		FuncState fs = this.fs;
		int line = this.linenumber;
		int pc = fs.codeABC(Lua.OP_NEWTABLE, 0, 0, 0);
		ConsControl cc = new ConsControl();
		cc.na = cc.nh = cc.tostore = 0;
		cc.t = t;
		t.init(VRELOCABLE, pc);
		cc.v.init(VVOID, 0); /* no value (yet) */
		fs.exp2nextreg(t); /* fix it at stack top (for gc) */
		this.checknext('{');
		do {
			_assert (cc.v.k == VVOID || cc.tostore > 0);
			if (this.t.token == '}')
				break;
			fs.closelistfield(cc);
			switch (this.t.token) {
			case TK_NAME: { /* may be listfields or recfields */
				this.lookahead();
				if (this.lookahead.token != '=') /* expression? */
					this.listfield(cc);
				else
					this.recfield(cc);
				break;
			}
			case '[': { /* constructor_item -> recfield */
				this.recfield(cc);
				break;
			}
			default: { /* constructor_part -> listfield */
				this.listfield(cc);
				break;
			}
			}
		} while (this.testnext(',') || this.testnext(';'));
		this.check_match('}', '{', line);
		fs.lastlistfield(cc);
		InstructionPtr i = new InstructionPtr(fs.f.code, pc);
		SETARG_B(i, luaO_int2fb(cc.na)); /* set initial array size */
		SETARG_C(i, luaO_int2fb(cc.nh));  /* set initial table size */
	}
	
	/*
	** converts an integer to a "floating point byte", represented as
	** (eeeeexxx), where the real value is (1xxx) * 2^(eeeee - 1) if
	** eeeee != 0 and (xxx) otherwise.
	*/
	static int luaO_int2fb (int x) {
	  int e = 0;  /* expoent */
	  while (x >= 16) {
	    x = (x+1) >> 1;
	    e++;
	  }
	  if (x < 8) return x;
	  else return ((e+1) << 3) | (((int)x) - 8);
	}


	/* }====================================================================== */

	void parlist () {
	  /* parlist -> [ param { `,' param } ] */
	  FuncState fs = this.fs;
	  Proto f = fs.f;
	  int nparams = 0;
	  f.is_vararg = false;
	  if (this.t.token != ')') {  /* is `parlist' not empty? */
	    do {
	      switch (this.t.token) {
	        case TK_NAME: {  /* param . NAME */
	          this.new_localvar(this.str_checkname(), nparams++);
	          break;
	        }
	        case TK_DOTS: {  /* param . `...' */
	          this.next();
	          if (LUA_COMPAT_VARARG) {
		          /* use `arg' as default name */
		          this.new_localvarliteral("arg", nparams++);
		          // f.is_vararg = VARARG_HASARG | VARARG_NEEDSARG;
		          fs.varargflags = VARARG_HASARG | VARARG_NEEDSARG;
	          } 
	          // f.is_vararg |= VARARG_ISVARARG;
	          fs.varargflags |= VARARG_ISVARARG;
	          f.is_vararg = true;
	          break;
	        }
	        default: this.syntaxerror("<name> or " + LUA_QL("...") + " expected");
	      }
	    } while (!f.is_vararg && this.testnext(','));
	  }
	  this.adjustlocalvars(nparams);
	  f.numparams = (fs.nactvar - (fs.varargflags & VARARG_HASARG));
	  fs.reserveregs(fs.nactvar);  /* reserve register for parameters */
	}


	void body(expdesc e, boolean needself, int line) {
		/* body -> `(' parlist `)' chunk END */
		FuncState new_fs = new FuncState();
		open_func(new_fs);
		new_fs.f.linedefined = line;
		this.checknext('(');
		if (needself) {
			new_localvarliteral("self", 0);
			adjustlocalvars(1);
		}
		this.parlist();
		this.checknext(')');
		this.chunk();
		new_fs.f.lastlinedefined = this.linenumber;
		this.check_match(TK_END, TK_FUNCTION, line);
		this.close_func();
		this.pushclosure(new_fs, e);
	}
	
	int explist1(expdesc v) {
		/* explist1 -> expr { `,' expr } */
		int n = 1; /* at least one expression */
		this.expr(v);
		while (this.testnext(',')) {
			fs.exp2nextreg(v);
			this.expr(v);
			n++;
		}
		return n;
	}


	void funcargs(expdesc f) {
		FuncState fs = this.fs;
		expdesc args = new expdesc();
		int base, nparams;
		int line = this.linenumber;
		switch (this.t.token) {
		case '(': { /* funcargs -> `(' [ explist1 ] `)' */
			if (line != this.lastline)
				this.syntaxerror("ambiguous syntax (function call x new statement)");
			this.next();
			if (this.t.token == ')') /* arg list is empty? */
				args.k = VVOID;
			else {
				this.explist1(args);
				fs.setmultret(args);
			}
			this.check_match(')', '(', line);
			break;
		}
		case '{': { /* funcargs -> constructor */
			this.constructor(args);
			break;
		}
		case TK_STRING: { /* funcargs -> STRING */
			this.codestring(args, this.t.seminfo.ts);
			this.next(); /* must use `seminfo' before `next' */
			break;
		}
		default: {
			this.syntaxerror("function arguments expected");
			return;
		}
		}
		_assert (f.k == VNONRELOC);
		base = f.u.s.info; /* base register for call */
		if (hasmultret(args.k))
			nparams = Lua.LUA_MULTRET; /* open call */
		else {
			if (args.k != VVOID)
				fs.exp2nextreg(args); /* close last argument */
			nparams = fs.freereg - (base + 1);
		}
		f.init(VCALL, fs.codeABC(Lua.OP_CALL, base, nparams + 1, 2));
		fs.fixline(line);
		fs.freereg = base+1;  /* call remove function and arguments and leaves
							 * (unless changed) one result */
	}


	/*
	** {======================================================================
	** Expression parsing
	** =======================================================================
	*/

	void prefixexp(expdesc v) {
		/* prefixexp -> NAME | '(' expr ')' */
		switch (this.t.token) {
		case '(': {
			int line = this.linenumber;
			this.next();
			this.expr(v);
			this.check_match(')', '(', line);
			fs.dischargevars(v);
			return;
		}
		case TK_NAME: {
			this.singlevar(v);
			return;
		}
		default: {
			this.syntaxerror("unexpected symbol");
			return;
		}
		}
	}


	void primaryexp(expdesc v) {
		/*
		 * primaryexp -> prefixexp { `.' NAME | `[' exp `]' | `:' NAME funcargs |
		 * funcargs }
		 */
		FuncState fs = this.fs;
		this.prefixexp(v);
		for (;;) {
			switch (this.t.token) {
			case '.': { /* field */
				this.field(v);
				break;
			}
			case '[': { /* `[' exp1 `]' */
				expdesc key = new expdesc();
				fs.exp2anyreg(v);
				this.yindex(key);
				fs.indexed(v, key);
				break;
			}
			case ':': { /* `:' NAME funcargs */
				expdesc key = new expdesc();
				this.next();
				this.checkname(key);
				fs.self(v, key);
				this.funcargs(v);
				break;
			}
			case '(':
			case TK_STRING:
			case '{': { /* funcargs */
				fs.exp2nextreg(v);
				this.funcargs(v);
				break;
			}
			default:
				return;
			}
		}
	}


	void simpleexp(expdesc v) {
		/*
		 * simpleexp -> NUMBER | STRING | NIL | true | false | ... | constructor |
		 * FUNCTION body | primaryexp
		 */
		switch (this.t.token) {
		case TK_NUMBER: {
			v.init(VKNUM, 0);
			v.u.setNval(this.t.seminfo.r);
			break;
		}
		case TK_STRING: {
			this.codestring(v, this.t.seminfo.ts);
			break;
		}
		case TK_NIL: {
			v.init(VNIL, 0);
			break;
		}
		case TK_TRUE: {
			v.init(VTRUE, 0);
			break;
		}
		case TK_FALSE: {
			v.init(VFALSE, 0);
			break;
		}
		case TK_DOTS: { /* vararg */
			FuncState fs = this.fs;
			this.check_condition(fs.f.is_vararg, "cannot use " + LUA_QL("...")
					+ " outside a vararg function");
			// fs.f.is_vararg &= ~VARARG_NEEDSARG; /* don't need 'arg' */
			fs.varargflags &= ~VARARG_NEEDSARG; /* don't need 'arg' */
			fs.f.is_vararg = (fs.varargflags != 0);
			v.init(VVARARG, fs.codeABC(Lua.OP_VARARG, 0, 1, 0));
			break;
		}
		case '{': { /* constructor */
			this.constructor(v);
			return;
		}
		case TK_FUNCTION: {
			this.next();
			this.body(v, false, this.linenumber);
			return;
		}
		default: {
			this.primaryexp(v);
			return;
		}
		}
		this.next();
	}


	int getunopr(int op) {
		switch (op) {
		case TK_NOT:
			return OPR_NOT;
		case '-':
			return OPR_MINUS;
		case '#':
			return OPR_LEN;
		default:
			return OPR_NOUNOPR;
		}
	}


	int getbinopr(int op) {
		switch (op) {
		case '+':
			return OPR_ADD;
		case '-':
			return OPR_SUB;
		case '*':
			return OPR_MUL;
		case '/':
			return OPR_DIV;
		case '%':
			return OPR_MOD;
		case '^':
			return OPR_POW;
		case TK_CONCAT:
			return OPR_CONCAT;
		case TK_NE:
			return OPR_NE;
		case TK_EQ:
			return OPR_EQ;
		case '<':
			return OPR_LT;
		case TK_LE:
			return OPR_LE;
		case '>':
			return OPR_GT;
		case TK_GE:
			return OPR_GE;
		case TK_AND:
			return OPR_AND;
		case TK_OR:
			return OPR_OR;
		default:
			return OPR_NOBINOPR;
		}
	}

	static class Priority {
		final byte left; /* left priority for each binary operator */

		final byte right; /* right priority */

		public Priority(int i, int j) {
			left = (byte) i;
			right = (byte) j;
		}
	};
	
	static Priority[] priority = {  /* ORDER OPR */
	   new Priority(6, 6), new Priority(6, 6), new Priority(7, 7), new Priority(7, 7), new Priority(7, 7),  /* `+' `-' `/' `%' */
	   new Priority(10, 9), new Priority(5, 4),                 /* power and concat (right associative) */
	   new Priority(3, 3), new Priority(3, 3),                  /* equality and inequality */
	   new Priority(3, 3), new Priority(3, 3), new Priority(3, 3), new Priority(3, 3),  /* order */
	   new Priority(2, 2), new Priority(1, 1)                   /* logical (and/or) */
	};

	static final int UNARY_PRIORITY	= 8;  /* priority for unary operators */


	/*
	** subexpr -> (simpleexp | unop subexpr) { binop subexpr }
	** where `binop' is any binary operator with a priority higher than `limit'
	*/
	int subexpr(expdesc v, int limit) {
		int op;
		int uop;
		this.enterlevel();
		uop = getunopr(this.t.token);
		if (uop != OPR_NOUNOPR) {
			this.next();
			this.subexpr(v, UNARY_PRIORITY);
			fs.prefix(uop, v);
		} else
			this.simpleexp(v);
		/* expand while operators have priorities higher than `limit' */
		op = getbinopr(this.t.token);
		while (op != OPR_NOBINOPR && priority[op].left > limit) {
			expdesc v2 = new expdesc();
			int nextop;
			this.next();
			fs.infix(op, v);
			/* read sub-expression with higher priority */
			nextop = this.subexpr(v2, priority[op].right);
			fs.posfix(op, v, v2);
			op = nextop;
		}
		this.leavelevel();
		return op; /* return first untreated operator */
	}

	void expr(expdesc v) {
		this.subexpr(v, 0);
	}

	/* }==================================================================== */



	/*
	** {======================================================================
	** Rules for Statements
	** =======================================================================
	*/


	boolean block_follow (int token) {
		switch (token) {
		    case TK_ELSE: case TK_ELSEIF: case TK_END:
		    case TK_UNTIL: case TK_EOS:
		    	return true;
		    default: return false;
		}
	}


	void block () {
	  /* block -> chunk */
	  FuncState fs = this.fs;
	  BlockCnt bl = new BlockCnt();
	  fs.enterblock(bl, false);
	  this.chunk();
	  _assert(bl.breaklist.i == NO_JUMP);
	  fs.leaveblock();
	}


	/*
	** structure to chain all variables in the left-hand side of an
	** assignment
	*/
	static class LHS_assign {
		LHS_assign prev;
		/* variable (global, local, upvalue, or indexed) */
		expdesc v = new expdesc(); 
	};


	/*
	** check whether, in an assignment to a local variable, the local variable
	** is needed in a previous assignment (to a table). If so, save original
	** local value in a safe place and use this safe copy in the previous
	** assignment.
	*/
	void check_conflict (LHS_assign lh, expdesc v) {
		FuncState fs = this.fs;
		int extra = fs.freereg;  /* eventual position to save local variable */
		boolean conflict = false;
		for (; lh!=null; lh = lh.prev) {
			if (lh.v.k == VINDEXED) {
				if (lh.v.u.s.info == v.u.s.info) {  /* conflict? */
					conflict = true;
					lh.v.u.s.info = extra;  /* previous assignment will use safe copy */
				}
				if (lh.v.u.s.aux == v.u.s.info) {  /* conflict? */
					conflict = true;
					lh.v.u.s.aux = extra;  /* previous assignment will use safe copy */
				}
			}
		}
		if (conflict) {
			fs.codeABC(Lua.OP_MOVE, fs.freereg, v.u.s.info, 0); /* make copy */
			fs.reserveregs(1);
		}
	}


	void assignment (LHS_assign lh, int nvars) {
		expdesc e = new expdesc();
		this.check_condition(VLOCAL <= lh.v.k && lh.v.k <= VINDEXED,
	                      "syntax error");
		if (this.testnext(',')) {  /* assignment -> `,' primaryexp assignment */
		    LHS_assign nv = new LHS_assign();
		    nv.prev = lh;
		    this.primaryexp(nv.v);
		    if (nv.v.k == VLOCAL)
		      this.check_conflict(lh, nv.v);
		    this.assignment(nv, nvars+1);
		}
		else {  /* assignment . `=' explist1 */
		    int nexps;
		    this.checknext('=');
		    nexps = this.explist1(e);
		    if (nexps != nvars) {
		      this.adjust_assign(nvars, nexps, e);
		      if (nexps > nvars)
		        this.fs.freereg -= nexps - nvars;  /* remove extra values */
	    }
	    else {
	    	fs.setoneret(e);  /* close last expression */
	    	fs.storevar(lh.v, e);
	    	return;  /* avoid default */
	    }
	  }
	  e.init(VNONRELOC, this.fs.freereg-1);  /* default assignment */
	  fs.storevar(lh.v, e);
	}


	int cond() {
		/* cond -> exp */
		expdesc v = new expdesc();
		/* read condition */
		this.expr(v);
		/* `falses' are all equal here */
		if (v.k == VNIL)
			v.k = VFALSE;
		fs.goiftrue(v);
		return v.f.i;
	}


	void breakstat() {
		FuncState fs = this.fs;
		BlockCnt bl = fs.bl;
		boolean upval = false;
		while (bl != null && !bl.isbreakable) {
			upval |= bl.upval;
			bl = bl.previous;
		}
		if (bl == null)
			this.syntaxerror("no loop to break");
		if (upval)
			fs.codeABC(Lua.OP_CLOSE, bl.nactvar, 0, 0);
		fs.concat(bl.breaklist, fs.jump());
	}


	void whilestat (int line) {
		/* whilestat -> WHILE cond DO block END */
		FuncState fs = this.fs;
		int whileinit;
		int condexit;
		BlockCnt bl = new BlockCnt();
		this.next();  /* skip WHILE */
		whileinit = fs.getlabel();
		condexit = this.cond();
		fs.enterblock(bl, true);
		this.checknext(TK_DO);
		this.block();
		fs.patchlist(fs.jump(), whileinit);
		this.check_match(TK_END, TK_WHILE, line);
		fs.leaveblock();
		fs.patchtohere(condexit);  /* false conditions finish the loop */
	}

	void repeatstat(int line) {
		/* repeatstat -> REPEAT block UNTIL cond */
		int condexit;
		FuncState fs = this.fs;
		int repeat_init = fs.getlabel();
		BlockCnt bl1 = new BlockCnt();
		BlockCnt bl2 = new BlockCnt();
		fs.enterblock(bl1, true); /* loop block */
		fs.enterblock(bl2, false); /* scope block */
		this.next(); /* skip REPEAT */
		this.chunk();
		this.check_match(TK_UNTIL, TK_REPEAT, line);
		condexit = this.cond(); /* read condition (inside scope block) */
		if (!bl2.upval) { /* no upvalues? */
			fs.leaveblock(); /* finish scope */
			fs.patchlist(condexit, repeat_init); /* close the loop */
		} else { /* complete semantics when there are upvalues */
			this.breakstat(); /* if condition then break */
			fs.patchtohere(condexit); /* else... */
			fs.leaveblock(); /* finish scope... */
			fs.patchlist(fs.jump(), repeat_init); /* and repeat */
		}
		fs.leaveblock(); /* finish loop */
	}


	int exp1() {
		expdesc e = new expdesc();
		int k;
		this.expr(e);
		k = e.k;
		fs.exp2nextreg(e);
		return k;
	}


	void forbody(int base, int line, int nvars, boolean isnum) {
		/* forbody -> DO block */
		BlockCnt bl = new BlockCnt();
		FuncState fs = this.fs;
		int prep, endfor;
		this.adjustlocalvars(3); /* control variables */
		this.checknext(TK_DO);
		prep = isnum ? fs.codeAsBx(Lua.OP_FORPREP, base, NO_JUMP) : fs.jump();
		fs.enterblock(bl, false); /* scope for declared variables */
		this.adjustlocalvars(nvars);
		fs.reserveregs(nvars);
		this.block();
		fs.leaveblock(); /* end of scope for declared variables */
		fs.patchtohere(prep);
		endfor = (isnum) ? fs.codeAsBx(Lua.OP_FORLOOP, base, NO_JUMP) : fs
				.codeABC(Lua.OP_TFORLOOP, base, 0, nvars);
		fs.fixline(line); /* pretend that `Lua.OP_FOR' starts the loop */
		fs.patchlist((isnum ? endfor : fs.jump()), prep + 1);
	}


	void fornum(LString varname, int line) {
		/* fornum -> NAME = exp1,exp1[,exp1] forbody */
		FuncState fs = this.fs;
		int base = fs.freereg;
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_INDEX, 0);
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_LIMIT, 1);
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_STEP, 2);
		this.new_localvar(varname, 3);
		this.checknext('=');
		this.exp1(); /* initial value */
		this.checknext(',');
		this.exp1(); /* limit */
		if (this.testnext(','))
			this.exp1(); /* optional step */
		else { /* default step = 1 */
			fs.codeABx(Lua.OP_LOADK, fs.freereg, fs.numberK(LInteger.valueOf(1)));
			fs.reserveregs(1);
		}
		this.forbody(base, line, 1, true);
	}


	void forlist(LString indexname) {
		/* forlist -> NAME {,NAME} IN explist1 forbody */
		FuncState fs = this.fs;
		expdesc e = new expdesc();
		int nvars = 0;
		int line;
		int base = fs.freereg;
		/* create control variables */
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_GENERATOR, nvars++);
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_STATE, nvars++);
		this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_CONTROL, nvars++);
		/* create declared variables */
		this.new_localvar(indexname, nvars++);
		while (this.testnext(','))
			this.new_localvar(this.str_checkname(), nvars++);
		this.checknext(TK_IN);
		line = this.linenumber;
		this.adjust_assign(3, this.explist1(e), e);
		fs.checkstack(3); /* extra space to call generator */
		this.forbody(base, line, nvars - 3, false);
	}


	void forstat(int line) {
		/* forstat -> FOR (fornum | forlist) END */
		FuncState fs = this.fs;
		LString varname;
		BlockCnt bl = new BlockCnt();
		fs.enterblock(bl, true); /* scope for loop and control variables */
		this.next(); /* skip `for' */
		varname = this.str_checkname(); /* first variable name */
		switch (this.t.token) {
		case '=':
			this.fornum(varname, line);
			break;
		case ',':
		case TK_IN:
			this.forlist(varname);
			break;
		default:
			this.syntaxerror(LUA_QL("=") + " or " + LUA_QL("in") + " expected");
		}
		this.check_match(TK_END, TK_FOR, line);
		fs.leaveblock(); /* loop scope (`break' jumps to this point) */
	}


	int test_then_block() {
		/* test_then_block -> [IF | ELSEIF] cond THEN block */
		int condexit;
		this.next(); /* skip IF or ELSEIF */
		condexit = this.cond();
		this.checknext(TK_THEN);
		this.block(); /* `then' part */
		return condexit;
	}


	void ifstat(int line) {
		/* ifstat -> IF cond THEN block {ELSEIF cond THEN block} [ELSE block]
		 * END */
		FuncState fs = this.fs;
		int flist;
		IntPtr escapelist = new IntPtr(NO_JUMP);
		flist = test_then_block(); /* IF cond THEN block */
		while (this.t.token == TK_ELSEIF) {
			fs.concat(escapelist, fs.jump());
			fs.patchtohere(flist);
			flist = test_then_block(); /* ELSEIF cond THEN block */
		}
		if (this.t.token == TK_ELSE) {
			fs.concat(escapelist, fs.jump());
			fs.patchtohere(flist);
			this.next(); /* skip ELSE (after patch, for correct line info) */
			this.block(); /* `else' part */
		} else
			fs.concat(escapelist, flist);
		fs.patchtohere(escapelist.i);
		this.check_match(TK_END, TK_IF, line);
	}

	void localfunc() {
		expdesc v = new expdesc();
		expdesc b = new expdesc();
		FuncState fs = this.fs;
		this.new_localvar(this.str_checkname(), 0);
		v.init(VLOCAL, fs.freereg);
		fs.reserveregs(1);
		this.adjustlocalvars(1);
		this.body(b, false, this.linenumber);
		fs.storevar(v, b);
		/* debug information will only see the variable after this point! */
		fs.getlocvar(fs.nactvar - 1).startpc = fs.pc;
	}


	void localstat() {
		/* stat -> LOCAL NAME {`,' NAME} [`=' explist1] */
		int nvars = 0;
		int nexps;
		expdesc e = new expdesc();
		do {
			this.new_localvar(this.str_checkname(), nvars++);
		} while (this.testnext(','));
		if (this.testnext('='))
			nexps = this.explist1(e);
		else {
			e.k = VVOID;
			nexps = 0;
		}
		this.adjust_assign(nvars, nexps, e);
		this.adjustlocalvars(nvars);
	}


	boolean funcname(expdesc v) {
		/* funcname -> NAME {field} [`:' NAME] */
		boolean needself = false;
		this.singlevar(v);
		while (this.t.token == '.')
			this.field(v);
		if (this.t.token == ':') {
			needself = true;
			this.field(v);
		}
		return needself;
	}


	void funcstat(int line) {
		/* funcstat -> FUNCTION funcname body */
		boolean needself;
		expdesc v = new expdesc();
		expdesc b = new expdesc();
		this.next(); /* skip FUNCTION */
		needself = this.funcname(v);
		this.body(b, needself, line);
		fs.storevar(v, b);
		fs.fixline(line); /* definition `happens' in the first line */
	}


	void exprstat() {
		/* stat -> func | assignment */
		FuncState fs = this.fs;
		LHS_assign v = new LHS_assign();
		this.primaryexp(v.v);
		if (v.v.k == VCALL) /* stat -> func */
			SETARG_C(fs.getcodePtr(v.v), 1); /* call statement uses no results */
		else { /* stat -> assignment */
			v.prev = null;
			this.assignment(v, 1);
		}
	}

	void retstat() {
		/* stat -> RETURN explist */
		FuncState fs = this.fs;
		expdesc e = new expdesc();
		int first, nret; /* registers with returned values */
		this.next(); /* skip RETURN */
		if (block_follow(this.t.token) || this.t.token == ';')
			first = nret = 0; /* return no values */
		else {
			nret = this.explist1(e); /* optional return values */
			if (hasmultret(e.k)) {
				fs.setmultret(e);
				if (e.k == VCALL && nret == 1) { /* tail call? */
					SET_OPCODE(fs.getcodePtr(e), Lua.OP_TAILCALL);
					_assert (Lua.GETARG_A(fs.getcode(e)) == fs.nactvar);
				}
				first = fs.nactvar;
				nret = Lua.LUA_MULTRET; /* return all values */
			} else {
				if (nret == 1) /* only one single value? */
					first = fs.exp2anyreg(e);
				else {
					fs.exp2nextreg(e); /* values must go to the `stack' */
					first = fs.nactvar; /* return all `active' values */
					_assert (nret == fs.freereg - first);
				}
			}
		}
		fs.ret(first, nret);
	}


	boolean statement() {
		int line = this.linenumber; /* may be needed for error messages */
		switch (this.t.token) {
		case TK_IF: { /* stat -> ifstat */
			this.ifstat(line);
			return false;
		}
		case TK_WHILE: { /* stat -> whilestat */
			this.whilestat(line);
			return false;
		}
		case TK_DO: { /* stat -> DO block END */
			this.next(); /* skip DO */
			this.block();
			this.check_match(TK_END, TK_DO, line);
			return false;
		}
		case TK_FOR: { /* stat -> forstat */
			this.forstat(line);
			return false;
		}
		case TK_REPEAT: { /* stat -> repeatstat */
			this.repeatstat(line);
			return false;
		}
		case TK_FUNCTION: {
			this.funcstat(line); /* stat -> funcstat */
			return false;
		}
		case TK_LOCAL: { /* stat -> localstat */
			this.next(); /* skip LOCAL */
			if (this.testnext(TK_FUNCTION)) /* local function? */
				this.localfunc();
			else
				this.localstat();
			return false;
		}
		case TK_RETURN: { /* stat -> retstat */
			this.retstat();
			return true; /* must be last statement */
		}
		case TK_BREAK: { /* stat -> breakstat */
			this.next(); /* skip BREAK */
			this.breakstat();
			return true; /* must be last statement */
		}
		default: {
			this.exprstat();
			return false; /* to avoid warnings */
		}
		}
	}

	void chunk() {
		/* chunk -> { stat [`;'] } */
		boolean islast = false;
		this.enterlevel();
		while (!islast && !block_follow(this.t.token)) {
			islast = this.statement();
			this.testnext(';');
			_assert (this.fs.f.maxstacksize >= this.fs.freereg 
					&& this.fs.freereg >= this.fs.nactvar);
			this.fs.freereg = this.fs.nactvar; /* free registers */
		}
		this.leavelevel();
	}

	/* }====================================================================== */
		
}
