package lua.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import lua.StackState;
import lua.value.LBoolean;
import lua.value.LDouble;
import lua.value.LInteger;
import lua.value.LNil;
import lua.value.LNumber;
import lua.value.LString;
import lua.value.LValue;

/*
** Function Prototypes
*/
public class LoadState {

	/** mark for precompiled code (`<esc>Lua') */
	public static final String LUA_SIGNATURE	= "\033Lua";


	/** for header of binary files -- this is Lua 5.1 */
	public static final int LUAC_VERSION		= 0x51;

	/** for header of binary files -- this is the official format */
	public static final int LUAC_FORMAT		= 0;

	/** size of header of binary files */
	public static final int LUAC_HEADERSIZE		= 12;

	/** expected lua header bytes */
	private static final int LUAC_HEADER_SIGNATURE = ('\033'<<24) | ('L'<<16) | ('u'<<8) | ('a');

	// values read from the header
	private int     luacVersion;
	private int     luacFormat;
	private boolean luacLittleEndian;
	private int     luacSizeofInt;
	private int     luacSizeofSizeT;
	private int     luacSizeofInstruction;
	private int     luacSizeofLuaNumber;
	private boolean luacIsNumberIntegral;

	/** The lua state that is loading the code */
	private StackState L;

	/** input stream from which we are loading */
	private DataInputStream is;

	/** Name of what is being loaded? */
	String name;

	
	private static final int LUA_TNONE		= (-1);

	private static final int LUA_TNIL		= 0;
	private static final int LUA_TBOOLEAN		= 1;
	private static final int LUA_TLIGHTUSERDATA	= 2;
	private static final int LUA_TNUMBER		= 3;
	private static final int LUA_TSTRING		= 4;
	private static final int LUA_TTABLE		= 5;
	private static final int LUA_TFUNCTION		= 6;
	private static final int LUA_TUSERDATA		= 7;
	private static final int LUA_TTHREAD		= 8;
	
//	/*
//	** $Id$
//	** load precompiled Lua chunks
//	** See Copyright Notice in lua.h
//	*/
//
//	#include <string.h>
//
//	#define lundump_c
//	#define LUA_CORE
//
//	#include "lua.h"
//
//	#include "ldebug.h"
//	#include "ldo.h"
//	#include "lfunc.h"
//	#include "lmem.h"
//	#include "lobject.h"
//	#include "lstring.h"
//	#include "lundump.h"
//	#include "lzio.h"
//
//	typedef struct {
//	 lua_State* L;
//	 ZIO* Z;
//	 Mbuffer* b;
//	 const char* name;
//	} LoadState;
//
//	#ifdef LUAC_TRUST_BINARIES
//	#define IF(c,s)
//	#else
//	#define IF(c,s)		if (c) error(S,s)
//
//	static void error(LoadState* S, const char* why)
//	{
//	 luaO_pushfstring(S->L,"%s: %s in precompiled chunk",S->name,why);
//	 luaD_throw(S->L,LUA_ERRSYNTAX);
//	}
//	#endif
//
//	#define LoadMem(S,b,n,size)	LoadBlock(S,b,(n)*(size))
//	#define	LoadByte(S)		(lu_byte)LoadChar(S)
//	#define LoadVar(S,x)		LoadMem(S,&x,1,sizeof(x))
//	#define LoadVector(S,b,n,size)	LoadMem(S,b,n,size)
//
//	static void LoadBlock(LoadState* S, void* b, size_t size)
//	{
//	 size_t r=luaZ_read(S->Z,b,size);
//	 IF (r!=0, "unexpected end");
//	}
//
//	static int LoadChar(LoadState* S)
//	{
//	 char x;
//	 LoadVar(S,x);
//	 return x;
//	}
	int loadByte() throws IOException {
		return is.readUnsignedByte();
	}
//
//	static int LoadInt(LoadState* S)
//	{
//	 int x;
//	 LoadVar(S,x);
//	 IF (x<0, "bad integer");
//	 return x;
//	}
	int loadInt() throws IOException {
		if ( this.luacLittleEndian ) {
			int a = is.readUnsignedByte();
			int b = is.readUnsignedByte();
			int c = is.readUnsignedByte();
			int d = is.readUnsignedByte();
			return (d << 24) | (c << 16) | (b << 8) | a;
		} else {
			return is.readInt();
		}
	}
	
	long loadInt64() throws IOException {
		int a,b;
		if ( this.luacLittleEndian ) {
			a = loadInt();
			b = loadInt();
		} else {
			b = loadInt();
			a = loadInt();
		}
		return (((long)b)<<32) | (((long)a)&0xffffffffL);
	}
//
//	static lua_Number LoadNumber(LoadState* S)
//	{
//	 lua_Number x;
//	 LoadVar(S,x);
//	 return x;
//	}
//
//	static TString* LoadString(LoadState* S)
//	{
//	 size_t size;
//	 LoadVar(S,size);
//	 if (size==0)
//	  return NULL;
//	 else
//	 {
//	  char* s=luaZ_openspace(S->L,S->b,size);
//	  LoadBlock(S,s,size);
//	  return luaS_newlstr(S->L,s,size-1);		/* remove trailing '\0' */
//	 }
//	}
	LString loadString() throws IOException {
		int size = loadInt();
		if ( size == 0 )
			return null;
		byte[] bytes = new byte[size];
		is.readFully( bytes );
		String s = new String( bytes, 0, size-1 );
		return new LString( s );
	}
	
	static LNumber longBitsToLuaNumber( long bits ) {
		if ( ( bits & ( ( 1L << 63 ) - 1 ) ) == 0L ) {
			return new LInteger( 0 );
		}
		
		int e = (int)((bits >> 52) & 0x7ffL) - 1023;
		
		if ( e >= 0 && e < 31 ) {
			long f = bits & 0xFFFFFFFFFFFFFL;
			int shift = 52 - e;
			long intPrecMask = ( 1L << shift ) - 1;
			if ( ( f & intPrecMask ) == 0 ) {
				int intValue = (int)( f >> shift ) | ( 1 << e );
				return new LInteger( ( ( bits >> 63 ) != 0 ) ? -intValue : intValue );
			}
		}
		
		double value = Double.longBitsToDouble(bits);
		return new LDouble( value );
	}
	
	LNumber loadNumber() throws IOException {
		if ( this.luacIsNumberIntegral ) {
			int value = loadInt();
			return new LInteger( value );
		} else {
			return longBitsToLuaNumber( loadInt64() );
		}
	}
//
//	static void LoadCode(LoadState* S, Proto* f)
//	{
//	 int n=LoadInt(S);
//	 f->code=luaM_newvector(S->L,n,Instruction);
//	 f->sizecode=n;
//	 LoadVector(S,f->code,n,sizeof(Instruction));
//	}
	public void loadCode( Proto f ) throws IOException {
		int n = loadInt();
		int[] code = new int[n];
		for ( int i=0; i<n; i++ )
			code[i] = loadInt();
		f.code = code;
	}
//
//	static Proto* LoadFunction(LoadState* S, TString* p);
//
//	static void LoadConstants(LoadState* S, Proto* f)
//	{
//	 int i,n;
//	 n=LoadInt(S);
//	 f->k=luaM_newvector(S->L,n,TValue);
//	 f->sizek=n;
//	 for (i=0; i<n; i++) setnilvalue(&f->k[i]);
//	 for (i=0; i<n; i++)
//	 {
//	  TValue* o=&f->k[i];
//	  int t=LoadChar(S);
//	  switch (t)
//	  {
//	   case LUA_TNIL:
//	   	setnilvalue(o);
//		break;
//	   case LUA_TBOOLEAN:
//	   	setbvalue(o,LoadChar(S));
//		break;
//	   case LUA_TNUMBER:
//		setnvalue(o,LoadNumber(S));
//		break;
//	   case LUA_TSTRING:
//		setsvalue2n(S->L,o,LoadString(S));
//		break;
//	   default:
//		IF (1, "bad constant");
//		break;
//	  }
//	 }
//	 n=LoadInt(S);
//	 f->p=luaM_newvector(S->L,n,Proto*);
//	 f->sizep=n;
//	 for (i=0; i<n; i++) f->p[i]=NULL;
//	 for (i=0; i<n; i++) f->p[i]=LoadFunction(S,f->source);
//	}
	void loadConstants(Proto f) throws IOException {
		int n = loadInt();
		LValue[] values = new LValue[n];
		for ( int i=0; i<n; i++ ) {
			switch ( loadByte() ) {
			case LUA_TNIL:
				values[i] = LNil.NIL;
				break;
			case LUA_TBOOLEAN:
				values[i] = (0 != loadByte()? LBoolean.TRUE: LBoolean.FALSE);
				break;
			case LUA_TNUMBER:
				values[i] = loadNumber();
				break;
			case LUA_TSTRING:
				values[i] = loadString();
				break;
			default:
				throw new IllegalStateException("bad constant");
			}
		}
		f.k = values;
		
		n = loadInt();
		Proto[] protos = new Proto[n];
		for ( int i=0; i<n; i++ )
			protos[i] = loadFunction(f.source);
		f.p = protos;
	}
//
//	static void LoadDebug(LoadState* S, Proto* f)
//	{
//	 int i,n;
//	 n=LoadInt(S);
//	 f->lineinfo=luaM_newvector(S->L,n,int);
//	 f->sizelineinfo=n;
//	 LoadVector(S,f->lineinfo,n,sizeof(int));
	
//	 n=LoadInt(S);
//	 f->locvars=luaM_newvector(S->L,n,LocVar);
//	 f->sizelocvars=n;
//	 for (i=0; i<n; i++) f->locvars[i].varname=NULL;
//	 for (i=0; i<n; i++)
//	 {
//	  f->locvars[i].varname=LoadString(S);
//	  f->locvars[i].startpc=LoadInt(S);
//	  f->locvars[i].endpc=LoadInt(S);
//	 }
	
//	 n=LoadInt(S);
//	 f->upvalues=luaM_newvector(S->L,n,TString*);
//	 f->sizeupvalues=n;
//	 for (i=0; i<n; i++) f->upvalues[i]=NULL;
//	 for (i=0; i<n; i++) f->upvalues[i]=LoadString(S);
//	}
	void loadDebug( Proto f ) throws IOException {
		int n = loadInt();
		f.lineinfo = new int[n];
		for ( int i=0; i<n; i++ )
			f.lineinfo[i] = loadInt();
		
		n = loadInt();
		f.locvars = new LocVars[n];
		for ( int i=0; i<n; i++ ) {
			LString varname = loadString();
			int startpc = loadInt();
			int endpc = loadInt();
			f.locvars[i] = new LocVars(varname, startpc, endpc);
		}
		
		n = loadInt();
		f.upvalues = new LString[n];
		for ( int i=0; i<n; i++ ) {
			f.upvalues[i] = loadString();
		}
	}
//
//	static Proto* LoadFunction(LoadState* S, TString* p)
//	{
//	 Proto* f=luaF_newproto(S->L);
//	 setptvalue2s(S->L,S->L->top,f); incr_top(S->L);
//	 f->source=LoadString(S); if (f->source==NULL) f->source=p;
//	 f->linedefined=LoadInt(S);
//	 f->lastlinedefined=LoadInt(S);
//	 f->nups=LoadByte(S);
//	 f->numparams=LoadByte(S);
//	 f->is_vararg=LoadByte(S);
//	 f->maxstacksize=LoadByte(S);
//	 LoadCode(S,f);
//	 LoadConstants(S,f);
//	 LoadDebug(S,f);
//	 IF (!luaG_checkcode(f), "bad code");
//	 S->L->top--;
//	 return f;
//	}
	public Proto loadFunction(LString p) throws IOException {
		Proto f = new Proto(this.L);
//		this.L.push(f);
		f.source = loadString();
		f.linedefined = loadInt();
		f.lastlinedefined = loadInt();
		f.nups = loadByte();
		f.numparams = loadByte();
		f.is_vararg = (0 != loadByte());
		f.maxstacksize = loadByte();
		loadCode(f);
		loadConstants(f);
		loadDebug(f);
		
		// TODO: add check here, for debugging purposes, I believe
		// see ldebug.c
//		 IF (!luaG_checkcode(f), "bad code");
		
//		 this.L.pop();
		 return f;
	}
//
//	static void LoadHeader(LoadState* S)
//	{
//	 char h[LUAC_HEADERSIZE];
//	 char s[LUAC_HEADERSIZE];
//	 luaU_header(h);
//	 LoadBlock(S,s,LUAC_HEADERSIZE);
//	 IF (memcmp(h,s,LUAC_HEADERSIZE)!=0, "bad header");
//	}
	public void loadHeader() throws IOException {
		int sig = is.readInt();
		luacVersion = is.readByte();
		luacFormat = is.readByte();
		luacLittleEndian = (0 != is.readByte());
		luacSizeofInt = is.readByte();
		luacSizeofSizeT = is.readByte();
		luacSizeofInstruction = is.readByte();
		luacSizeofLuaNumber = is.readByte();
		luacIsNumberIntegral = (0 != is.readByte());
		if ( sig != LUAC_HEADER_SIGNATURE )
			throw new IllegalArgumentException("bad signature");
	}
//
//	/*
//	** load precompiled chunk
//	*/
//	Proto* luaU_undump (lua_State* L, ZIO* Z, Mbuffer* buff, const char* name)
//	{
//	 LoadState S;
//	 if (*name=='@' || *name=='=')
//	  S.name=name+1;
//	 else if (*name==LUA_SIGNATURE[0])
//	  S.name="binary string";
//	 else
//	  S.name=name;
//	 S.L=L;
//	 S.Z=Z;
//	 S.b=buff;
//	 LoadHeader(&S);
//	 return LoadFunction(&S,luaS_newliteral(L,"=?"));
//	}
	
	public static Proto undump( StackState L, InputStream stream, String name ) throws IOException {
		String sname = name;
		if ( name.startsWith("@") || name.startsWith("=") )
			sname = name.substring(1);
		else if ( name.startsWith("\033") )
			sname = "binary string";
		LoadState s = new LoadState( L, stream, sname );
		s.loadHeader();
		LString literal = new LString(L, "=?");
		return s.loadFunction( literal );
	}

	/** Private constructor for create a load state */
	private LoadState( StackState L, InputStream stream, String name ) {
		this.L = L;
		this.name = name;
		this.is = new DataInputStream( stream );
	}
}
