package lua.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lua.VM;
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
	private static final byte[] LUAC_HEADER_SIGNATURE = { '\033', 'L', 'u', 'a' };

	// values read from the header
	private int     luacVersion;
	private int     luacFormat;
	private boolean luacLittleEndian;
	private int     luacSizeofInt;
	private int     luacSizeofSizeT;
	private int     luacSizeofInstruction;
	private int     luacSizeofLuaNumber;
	private boolean luacIsNumberIntegral;

	/** input stream from which we are loading */
	private DataInputStream is;

	/** Name of what is being loaded? */
	String name;

	/** The VM doing the loading */
	VM L;
	
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

	int loadByte() throws IOException {
		return is.readUnsignedByte();
	}

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

	LString loadString() throws IOException {
		int size = loadInt();
		if ( size == 0 )
			return null;
		byte[] bytes = new byte[size];
		is.readFully( bytes );
		return new LString( bytes, 0, bytes.length - 1 );
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

	public void loadCode( Proto f ) throws IOException {
		int n = loadInt();
		int[] code = new int[n];
		for ( int i=0; i<n; i++ )
			code[i] = loadInt();
		f.code = code;
	}

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

	public Proto loadFunction(LString p) throws IOException {
		Proto f = new Proto();
//		this.L.push(f);
		f.source = loadString();
		if ( f.source == null )
			f.source = p;
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
		luacVersion = is.readByte();
		luacFormat = is.readByte();
		luacLittleEndian = (0 != is.readByte());
		luacSizeofInt = is.readByte();
		luacSizeofSizeT = is.readByte();
		luacSizeofInstruction = is.readByte();
		luacSizeofLuaNumber = is.readByte();
		luacIsNumberIntegral = (0 != is.readByte());
	}
	
	public static Proto undump( VM L, InputStream stream, String name ) throws IOException {
		
		// is this a source file? 
		stream.mark(1);
		if ( stream.read() != LUAC_HEADER_SIGNATURE[0] ) {
			stream.reset();
			// TODO: handle UTF-8 here!
			return lua.addon.compile.Compiler.compile( 
					new InputStreamReader(stream), 
					name );
		}

		// check signature
		for ( int i=1; i<4; i++ ) {
			if ( stream.read() != LUAC_HEADER_SIGNATURE[i] )
				throw new IllegalArgumentException("bad signature");
		}
		
		// load file
		String sname = name;
		if ( name.startsWith("@") || name.startsWith("=") )
			sname = name.substring(1);
		else if ( name.startsWith("\033") )
			sname = "binary string";
		LoadState s = new LoadState( L, stream, sname );
		s.loadHeader();
		LString literal = new LString("=?");
		return s.loadFunction( literal );
	}

	/** Private constructor for create a load state */
	private LoadState( VM L, InputStream stream, String name ) {
		this.L = L;
		this.name = name;
		this.is = new DataInputStream( stream );
	}
}
