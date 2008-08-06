package org.luaj.vm;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;





public class Print extends Lua {

	/** opcode names */
	private static final String STRING_FOR_NULL = "null";
	public static PrintStream ps = System.out;

	private static final String[] luaP_opnames = {
		  "MOVE",
		  "LOADK",
		  "LOADBOOL",
		  "LOADNIL",
		  "GETUPVAL",
		  "GETGLOBAL",
		  "GETTABLE",
		  "SETGLOBAL",
		  "SETUPVAL",
		  "SETTABLE",
		  "NEWTABLE",
		  "SELF",
		  "ADD",
		  "SUB",
		  "MUL",
		  "DIV",
		  "MOD",
		  "POW",
		  "UNM",
		  "NOT",
		  "LEN",
		  "CONCAT",
		  "JMP",
		  "EQ",
		  "LT",
		  "LE",
		  "TEST",
		  "TESTSET",
		  "CALL",
		  "TAILCALL",
		  "RETURN",
		  "FORLOOP",
		  "FORPREP",
		  "TFORLOOP",
		  "SETLIST",
		  "CLOSE",
		  "CLOSURE",
		  "VARARG",
		  null,
	};


	static void printString(PrintStream ps, final LString s) {
		final byte[] bytes = s.m_bytes;
		final int off = s.m_offset;
		
		ps.print('"');
		for (int i = 0, n = s.m_length; i < n; i++) {
			int c = bytes[i+off] & 0x0FF;
			if ( c >= ' ' && c <= '~' && c != '\"' && c != '\\' )
				ps.print((char) c);
			else {
				switch (c) {
					case '"':
						ps.print("\\\"");
						break;
					case '\\':
						ps.print("\\\\");
						break;
					case 0x0007: /* bell */
						ps.print("\\a");
						break;
					case '\b': /* backspace */
						ps.print("\\f");
						break;
					case '\f':  /* form feed */
						ps.print("\\f");
						break;
					case '\r': /* carriage return */
						ps.print("\\r");
						break;
					case '\n': /* newline */
						ps.print("\\n");
						break;
					case 0x000B: /* vertical tab */
						ps.print("\\v");
						break;
					default:
						ps.print('\\');
						ps.print(Integer.toString(1000 + c).substring(1));
						break;
				}
			}
		}
		ps.print('"');
	}

	static void printValue( PrintStream ps, LValue v ) {
		if ( v instanceof LString )
			printString( ps, v.luaAsString() );
		else if ( v instanceof LInteger ) {
			ps.print( v.toJavaInt() );
		} else if ( v instanceof LDouble ) {
			double d = v.toJavaDouble();
			if ( d == ((int)d) )
				ps.print( (int) d );
			else
				ps.print( d );
		} else if ( v instanceof LFunction ) {
			ps.print(v.getClass().getName());
		} else {
			ps.print( String.valueOf(v) );
		}
	}
	
	static void printConstant(PrintStream ps, LPrototype f, int i) {
		printValue( ps, f.k[i] );
	}

	public static void printCode(LPrototype f) {
		int[] code = f.code;
		int pc, n = code.length;
		for (pc = 0; pc < n; pc++) {
			printOpCode(f, pc);
			ps.println();
		}
	}

	public static void printOpCode(LPrototype f, int pc) {
		printOpCode(ps,f,pc);
	}
	
	public static void printOpCode(PrintStream ps, LPrototype f, int pc) {
		int[] code = f.code;
		int i = code[pc];
		int o = GET_OPCODE(i);
		int a = GETARG_A(i);
		int b = GETARG_B(i);
		int c = GETARG_C(i);
		int bx = GETARG_Bx(i);
		int sbx = GETARG_sBx(i);
		int line = getline(f, pc);
		ps.print("  " + (pc + 1) + "  ");
		if (line > 0)
			ps.print("[" + line + "]  ");
		else
			ps.print("[-]  ");
		ps.print(luaP_opnames[o] + "  ");
		switch (getOpMode(o)) {
		case iABC:
			ps.print( a );
			if (getBMode(o) != OpArgN)
				ps.print(" "+(ISK(b) ? (-1 - INDEXK(b)) : b));
			if (getCMode(o) != OpArgN)
				ps.print(" "+(ISK(c) ? (-1 - INDEXK(c)) : c));
			break;
		case iABx:
			if (getBMode(o) == OpArgK) {
				ps.print(a + " " + (-1 - bx));
			} else {
				ps.print(a + " " + (bx));
			}
			break;
		case iAsBx:
			if (o == OP_JMP)
				ps.print( sbx );
			else
				ps.print(a + " " + sbx);
			break;
		}
		switch (o) {
		case OP_LOADK:
			ps.print("  ; ");
			printConstant(ps, f, bx);
			break;
		case OP_GETUPVAL:
		case OP_SETUPVAL:
			ps.print("  ; ");
			if ( f.upvalues.length > b )
				printValue(ps, f.upvalues[b]);
			else
				ps.print( "-" );
			break;
		case OP_GETGLOBAL:
		case OP_SETGLOBAL:
			ps.print("  ; ");
			printConstant( ps, f, bx );
			break;
		case OP_GETTABLE:
		case OP_SELF:
			if (ISK(c)) {
				ps.print("  ; ");
				printConstant(ps, f, INDEXK(c));
			}
			break;
		case OP_SETTABLE:
		case OP_ADD:
		case OP_SUB:
		case OP_MUL:
		case OP_DIV:
		case OP_POW:
		case OP_EQ:
		case OP_LT:
		case OP_LE:
			if (ISK(b) || ISK(c)) {
				ps.print("  ; ");
				if (ISK(b))
					printConstant(ps, f, INDEXK(b));
				else
					ps.print("-");
				ps.print(" ");
				if (ISK(c))
					printConstant(ps, f, INDEXK(c));
				else
					ps.print("-");
			}
			break;
		case OP_JMP:
		case OP_FORLOOP:
		case OP_FORPREP:
			ps.print("  ; to " + (sbx + pc + 2));
			break;
		case OP_CLOSURE:
			ps.print("  ; " + f.p[bx].getClass().getName());
			break;
		case OP_SETLIST:
			if (c == 0)
				ps.print("  ; " + ((int) code[++pc]));
			else
				ps.print("  ; " + ((int) c));
			break;
		default:
			break;
		}
	}

	private static int getline(LPrototype f, int pc) {
		return f.lineinfo[pc];
	}

	static void printHeader(LPrototype f) {
		String s = String.valueOf(f.source);
		if (s.startsWith("@") || s.startsWith("="))
			s = s.substring(1);
		else if ("\033Lua".equals(s))
			s = "(bstring)";
		else
			s = "(string)";
		String a = (f.linedefined == 0) ? "main" : "function";
		ps.print("\n%" + a + " <" + s + ":" + f.linedefined + ","
				+ f.lastlinedefined + "> (" + f.code.length + " instructions, "
				+ f.code.length * 4 + " bytes at " + id(f) + ")\n");
		ps.print(f.numparams + " param, " + f.maxstacksize + " slot, "
				+ f.upvalues.length + " upvalue, ");
		ps.print(f.locvars.length + " local, " + f.k.length
				+ " constant, " + f.p.length + " function\n");
	}

	static void printConstants(LPrototype f) {
		int i, n = f.k.length;
		ps.print("constants (" + n + ") for " + id(f) + ":\n");
		for (i = 0; i < n; i++) {
			ps.print("  " + (i + 1) + "  ");
			printValue( ps, f.k[i] );
			ps.print( "\n");
		}
	}

	static void printLocals(LPrototype f) {
		int i, n = f.locvars.length;
		ps.print("locals (" + n + ") for " + id(f) + ":\n");
		for (i = 0; i < n; i++) {
			ps.println("  "+i+"  "+f.locvars[i].varname+" "+(f.locvars[i].startpc+1)+" "+(f.locvars[i].endpc+1));
		}
	}

	static void printUpValues(LPrototype f) {
		int i, n = f.upvalues.length;
		ps.print("upvalues (" + n + ") for " + id(f) + ":\n");
		for (i = 0; i < n; i++) {
			ps.print("  " + i + "  " + f.upvalues[i] + "\n");
		}
	}

	public void printFunction(LPrototype f, boolean full) {
		int i, n = f.p.length;
		printHeader(f);
		printCode(f);
		if (full) {
			printConstants(f);
			printLocals(f);
			printUpValues(f);
		}
		for (i = 0; i < n; i++)
			printFunction(f.p[i], full);
	}

	private static void format( String s, int maxcols ) {
		int n = s.length();
		if ( n > maxcols )
			ps.print( s.substring(0,maxcols) );
		else {
			ps.print( s );
			for ( int i=maxcols-n; --i>=0; )
				ps.print( ' ' );
		}
	}
	
	public static void printState(LuaState state, int base, int top, int max,
			LClosure cl, int pc) {
		
		// print opcode into buffer
		PrintStream previous = ps;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ps = new PrintStream( baos );
		printOpCode( cl.p, pc );
		ps.flush();
		ps.close();
		ps = previous;
		format( baos.toString(), 40 );

		ps.print( " b,t=(" );
		format( String.valueOf(base), 3 );
		ps.print( "," );
		format( String.valueOf(top), 3 );
		ps.print( ") " );
		
		// print stack
		int i=0; 
		for ( ; i<base; i++ )
			ps.print('.');
		ps.print('[');
		for ( ; i<max; i++ ) {
			Object v = state.stack[i];
			ps.print( v!=null? String.valueOf(v): STRING_FOR_NULL );
			if ( i+1 == top )
				ps.print(']');
			ps.print( " | " );
		}
		ps.println();
	}

	private static String id(LPrototype f) {
		return "Proto";
	}


}
