/*******************************************************************************
* Copyright (c) 2007 LuaJ. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.luaj.compiler.DumpState;
import org.luaj.vm.LClosure;
import org.luaj.vm.LFunction;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaState;


public class StringLib extends LFunction {
	
	private static final String[] NAMES = {
		"string",
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
	
	private static final int INSTALL = 0;
	private static final int BYTE    = 1;
	private static final int CHAR    = 2;
	private static final int DUMP    = 3;
	private static final int FIND    = 4;
	private static final int FORMAT  = 5;
	private static final int GMATCH  = 6;
	private static final int GSUB    = 7;
	private static final int LEN     = 8;
	private static final int LOWER   = 9;
	private static final int MATCH   = 10;
	private static final int REP     = 11;
	private static final int REVERSE = 12;
	private static final int SUB     = 13;
	private static final int UPPER   = 14;	

	public static void install( LTable globals ) {
		LTable string = LString.getMetatable();
		for ( int i=1; i<NAMES.length; i++ )
			string.put(NAMES[i], new StringLib(i));
		globals.put( "string", string );
		PackageLib.setIsLoaded("string", string);
	}

	private final int id;

	public StringLib() {
		this.id = 0;
	}
	
	private StringLib( int id ) {
		this.id = id;
	}
	
	public String toString() {
		return NAMES[id]+"()";
	}
		
	public boolean luaStackCall( LuaState vm ) {
		switch ( id ) {
		case INSTALL:
			install( vm._G );
			break;
		case BYTE:
			StringLib.byte_( vm );
			break;
		case CHAR:
			StringLib.char_( vm );
			break;
		case DUMP:
			StringLib.dump( vm );
			break;
		case FIND:
			StringLib.find( vm );
			break;
		case FORMAT:
			StringLib.format( vm );
			break;
		case GMATCH:
			StringLib.gmatch( vm );
			break;
		case GSUB:
			StringLib.gsub( vm );
			break;
		case LEN:
			StringLib.len( vm );
			break;
		case LOWER:
			StringLib.lower( vm );
			break;
		case MATCH:
			StringLib.match( vm );
			break;
		case REP:
			StringLib.rep( vm );
			break;
		case REVERSE:
			StringLib.reverse( vm );
			break;
		case SUB:
			StringLib.sub( vm );
			break;
		case UPPER:
			StringLib.upper( vm );
			break;
			
		default:
			vm.error( "bad id" );
		}
		return false;
	}
	
	/**
	 * string.byte (s [, i [, j]]) 
	 * 
	 * Returns the internal numerical codes of the
	 * characters s[i], s[i+1], ..., s[j]. The default value for i is 1; the
	 * default value for j is i.
	 * 
	 * Note that numerical codes are not necessarily portable across platforms.
	 * 
	 * @param vm the calling vm
	 */
	static void byte_( LuaState vm ) {
		LString s = vm.checklstring(2);
		int l = s.m_length;
		int posi = posrelat( vm.optint(3,1), l );
		int pose = posrelat( vm.optint(4,posi), l );
		vm.resettop();
		int n,i;
		if (posi <= 0) posi = 1;
		if (pose > l) pose = l;
		if (posi > pose) return;  /* empty interval; return no values */
		n = (int)(pose -  posi + 1);
		if (posi + n <= pose)  /* overflow? */
		    vm.error("string slice too long");
		vm.checkstack(n);
		for (i=0; i<n; i++)
			vm.pushinteger(s.luaByte(posi+i-1));
	}

	/** 
	 * string.char (...)
	 * 
	 * Receives zero or more integers. Returns a string with length equal 
	 * to the number of arguments, in which each character has the internal 
	 * numerical code equal to its corresponding argument. 
	 * 
	 * Note that numerical codes are not necessarily portable across platforms.
	 * 
	 * @param vm the calling VM
	 */
	public static void char_( LuaState vm) {
		int n = vm.gettop()-1;
		byte[] bytes = new byte[n];
		for ( int i=0, a=2; i<n; i++, a++ ) {
			int c = vm.checkint(a);
			vm.argcheck((c>=0 && c<256), a, "invalid value");
			bytes[i] = (byte) c;
		}
		vm.resettop();
		vm.pushlstring( bytes );
	}
		
	/** 
	 * string.dump (function)
	 * 
	 * Returns a string containing a binary representation of the given function, 
	 * so that a later loadstring on this string returns a copy of the function. 
	 * function must be a Lua function without upvalues.
	 *  
	 * TODO: port dumping code as optional add-on
	 */
	static void dump( LuaState vm ) {
		LFunction f = vm.checkfunction(2);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			DumpState.dump( ((LClosure)f).p, baos, true );
			vm.resettop();
			vm.pushlstring(baos.toByteArray());
		} catch (IOException e) {
			vm.error( e.getMessage() );
		}
	}

	/** 
	 * string.find (s, pattern [, init [, plain]])
	 * 
	 * Looks for the first match of pattern in the string s. 
	 * If it finds a match, then find returns the indices of s 
	 * where this occurrence starts and ends; otherwise, it returns nil. 
	 * A third, optional numerical argument init specifies where to start the search; 
	 * its default value is 1 and may be negative. A value of true as a fourth, 
	 * optional argument plain turns off the pattern matching facilities, 
	 * so the function does a plain "find substring" operation, 
	 * with no characters in pattern being considered "magic". 
	 * Note that if plain is given, then init must be given as well.
	 * 
	 * If the pattern has captures, then in a successful match the captured values 
	 * are also returned, after the two indices.
	 */
	static void find( LuaState vm ) {
		str_find_aux( vm, true );
	}

	/** 
	 * string.format (formatstring, ...)
	 * 
	 * Returns a formatted version of its variable number of arguments following 
	 * the description given in its first argument (which must be a string). 
	 * The format string follows the same rules as the printf family of standard C functions. 
	 * The only differences are that the options/modifiers *, l, L, n, p, and h are not supported 
	 * and that there is an extra option, q. The q option formats a string in a form suitable 
	 * to be safely read back by the Lua interpreter: the string is written between double quotes, 
	 * and all double quotes, newlines, embedded zeros, and backslashes in the string are correctly 
	 * escaped when written. For instance, the call
	 *   string.format('%q', 'a string with "quotes" and \n new line')
	 *
	 * will produce the string:
	 *    "a string with \"quotes\" and \
	 *    new line"
	 *    
	 * The options c, d, E, e, f, g, G, i, o, u, X, and x all expect a number as argument, 
	 * whereas q and s expect a string. 
	 * 
	 * This function does not accept string values containing embedded zeros, 
	 * except as arguments to the q option. 
	 */
	static void format( LuaState vm ) {
		LString fmt = vm.checklstring( 2 );
		final int n = fmt.length();
		LBuffer result = new LBuffer(n);
		int arg = 2;
		
		for ( int i = 0; i < n; ) {
			int c = fmt.luaByte( i++ );
			if ( c != L_ESC ) {
				result.append( (byte) c );
			} else if ( i < n ) {
				if ( ( c = fmt.luaByte( i ) ) == L_ESC ) {
					++i;
					result.append( (byte)L_ESC );
				} else {
					arg++;
					FormatDesc fdsc = new FormatDesc(vm, fmt, i );
					i += fdsc.length;
					switch ( fdsc.conversion ) {
					case 'c':
						fdsc.format( result, (byte)vm.checkint( arg ) );
						break;
					case 'i':
					case 'd':
						fdsc.format( result, vm.checkint( arg ) );
						break;
					case 'o':
					case 'u':
					case 'x':
					case 'X':
						fdsc.format( result, vm.checklong( arg ) );
						break;
					case 'e':
					case 'E':
					case 'f':
					case 'g':
					case 'G':
						fdsc.format( result, vm.checkdouble( arg ) );
						break;
					case 'q':
						addquoted( result, vm.checklstring( arg ) );
						break;
					case 's': {
						LString s = vm.checklstring( arg );
						if ( fdsc.precision == -1 && s.length() >= 100 ) {
							result.append( s );
						} else {
							fdsc.format( result, s );
						}
					}	break;
					default:
						vm.error("invalid option '%"+(char)fdsc.conversion+"' to 'format'");
						break;
					}
				}
			}
		}
		
		vm.resettop();
		vm.pushlstring( result.toLuaString() );
	}
	
	private static void addquoted(LBuffer buf, LString s) {
		int c;
		buf.append( (byte) '"' );
		for ( int i = 0, n = s.length(); i < n; i++ ) {
			switch ( c = s.luaByte( i ) ) {
			case '"': case '\\': case '\n':
				buf.append( (byte)'\\' );
				buf.append( (byte)c );
				break;
			case '\r':
				buf.append( "\\r" );
				break;
			case '\0':
				buf.append( "\\000" );
				break;
			default:
				buf.append( (byte) c );
			break;
			}
		}
		buf.append( (byte) '"' );
	}
	
	private static final String FLAGS = "-+ #0";
	
	private static class FormatDesc {
		
		private boolean leftAdjust;
		private boolean zeroPad;
		private boolean explicitPlus;
		private boolean space;
		private boolean alternateForm;
		private static final int MAX_FLAGS = 5;
		
		private int width;
		private int precision;
		
		public final int conversion;
		public final int length;
		
		public FormatDesc(LuaState vm, LString strfrmt, final int start) {
			int p = start, n = strfrmt.length();
			int c = 0;
			
			boolean moreFlags = true;
			while ( moreFlags ) {
				switch ( c = ( (p < n) ? strfrmt.luaByte( p++ ) : 0 ) ) {
				case '-': leftAdjust = true; break;
				case '+': explicitPlus = true; break;
				case ' ': space = true; break;
				case '#': alternateForm = true; break;
				case '0': zeroPad = true; break;
				default: moreFlags = false; break;
				}
			}
			if ( p - start > MAX_FLAGS )
				vm.error("invalid format (repeated flags)");
			
			width = -1;
			if ( Character.isDigit( (char)c ) ) {
				width = c - '0';
				c = ( (p < n) ? strfrmt.luaByte( p++ ) : 0 );
				if ( Character.isDigit( (char) c ) ) {
					width = width * 10 + (c - '0');
					c = ( (p < n) ? strfrmt.luaByte( p++ ) : 0 );
				}
			}
			
			precision = -1;
			if ( c == '.' ) {
				c = ( (p < n) ? strfrmt.luaByte( p++ ) : 0 );
				if ( Character.isDigit( (char) c ) ) {
					precision = c - '0';
					c = ( (p < n) ? strfrmt.luaByte( p++ ) : 0 );
					if ( Character.isDigit( (char) c ) ) {
						precision = precision * 10 + (c - '0');
						c = ( (p < n) ? strfrmt.luaByte( p++ ) : 0 );
					}
				}
			}
			
			if ( Character.isDigit( (char) c ) )
				vm.error("invalid format (width or precision too long)");
			
			zeroPad &= !leftAdjust; // '-' overrides '0'
			conversion = c;
			length = p - start;
		}
		
		public void format(LBuffer buf, byte c) {
			// TODO: not clear that any of width, precision, or flags apply here.
			buf.append(c);
		}
		
		public void format(LBuffer buf, long number) {
			String digits;
			
			if ( number == 0 && precision == 0 ) {
				digits = "";
			} else {
				int radix;
				switch ( conversion ) {
				case 'x':
				case 'X':
					radix = 16;
					break;
				case 'o':
					radix = 8;
					break;
				default:
					radix = 10;
					break;
				}
				digits = Long.toString( number, radix );
				if ( conversion == 'X' )
					digits = digits.toUpperCase();
			}
			
			int minwidth = digits.length();
			int ndigits = minwidth;
			int nzeros;
			
			if ( number < 0 ) {
				ndigits--;
			} else if ( explicitPlus || space ) {
				minwidth++;
			}
			
			if ( precision > ndigits )
				nzeros = precision - ndigits;
			else if ( precision == -1 && zeroPad && width > minwidth )
				nzeros = width - minwidth;
			else
				nzeros = 0;
			
			minwidth += nzeros;
			int nspaces = width > minwidth ? width - minwidth : 0;
			
			if ( !leftAdjust )
				pad( buf, ' ', nspaces );
			
			if ( number < 0 ) {
				if ( nzeros > 0 ) {
					buf.append( (byte)'-' );
					digits = digits.substring( 1 );
				}
			} else if ( explicitPlus ) {
				buf.append( (byte)'+' );
			} else if ( space ) {
				buf.append( (byte)' ' );
			}
			
			if ( nzeros > 0 )
				pad( buf, '0', nzeros );
			
			buf.append( digits );
			
			if ( leftAdjust )
				pad( buf, ' ', nspaces );
		}
		
		public void format(LBuffer buf, double x) {
			// TODO
			buf.append( String.valueOf( x ) );
		}
		
		public void format(LBuffer buf, LString s) {
			int nullindex = s.indexOf( (byte)'\0', 0 );
			if ( nullindex != -1 )
				s = s.substring( 0, nullindex );
			buf.append(s);
		}
		
		public static final void pad(LBuffer buf, char c, int n) {
			byte b = (byte)c;
			while ( n-- > 0 )
				buf.append(b);
		}
	}
	
	/** 
	 * string.gmatch (s, pattern)
	 * 
	 * Returns an iterator function that, each time it is called, returns the next captures 
	 * from pattern over string s. If pattern specifies no captures, then the 
	 * whole match is produced in each call. 
	 * 
	 * As an example, the following loop
	 *   s = "hello world from Lua"
	 *   for w in string.gmatch(s, "%a+") do
	 *      print(w)
	 *   end
	 *   
	 * will iterate over all the words from string s, printing one per line. 
	 * The next example collects all pairs key=value from the given string into a table:
	 *   t = {}
	 *   s = "from=world, to=Lua"
	 *   for k, v in string.gmatch(s, "(%w+)=(%w+)") do
	 *     t[k] = v
	 *   end
	 *
	 * For this function, a '^' at the start of a pattern does not work as an anchor, 
	 * as this would prevent the iteration.
	 */
	static void gmatch( LuaState vm ) {
		LString src = vm.checklstring( 2 );
		LString pat = vm.checklstring( 3 );
		vm.resettop();
		vm.pushlvalue( new GMatchAux(vm, src, pat) );
	}

	static class GMatchAux extends LFunction {
		private final int srclen;
		private final MatchState ms;
		private int soffset;
		public GMatchAux(LuaState vm, LString src, LString pat) {
			this.srclen = src.length();
			this.ms = new MatchState(vm, src, pat);
			this.soffset = 0;
		}
		public boolean luaStackCall(LuaState vm) {
			vm.resettop();
			for ( ; soffset<srclen; soffset++ ) {
				ms.reset();
				int res = ms.match(soffset, 0);
				if ( res >=0 ) {
					int soff = soffset;
					soffset = res;
					ms.push_captures( true, soff, res );
					return false;
				}
			}
			vm.pushnil();
			return false;
		}
	}

	
	/** 
	 * string.gsub (s, pattern, repl [, n])
	 * Returns a copy of s in which all (or the first n, if given) occurrences of the 
	 * pattern have been replaced by a replacement string specified by repl, which 
	 * may be a string, a table, or a function. gsub also returns, as its second value, 
	 * the total number of matches that occurred.
	 * 
	 * If repl is a string, then its value is used for replacement. 
	 * The character % works as an escape character: any sequence in repl of the form %n, 
	 * with n between 1 and 9, stands for the value of the n-th captured substring (see below). 
	 * The sequence %0 stands for the whole match. The sequence %% stands for a single %. 
	 *  
	 * If repl is a table, then the table is queried for every match, using the first capture 
	 * as the key; if the pattern specifies no captures, then the whole match is used as the key. 
	 * 
	 * If repl is a function, then this function is called every time a match occurs, 
	 * with all captured substrings passed as arguments, in order; if the pattern specifies 
	 * no captures, then the whole match is passed as a sole argument. 
	 * 
	 * If the value returned by the table query or by the function call is a string or a number, 
	 * then it is used as the replacement string; otherwise, if it is false or nil, 
	 * then there is no replacement (that is, the original match is kept in the string). 
	 * 
	 * Here are some examples:
	 * 	     x = string.gsub("hello world", "(%w+)", "%1 %1")
	 * 	     --> x="hello hello world world"
	 * 
	 *	     x = string.gsub("hello world", "%w+", "%0 %0", 1)
	 *	     --> x="hello hello world"
	 *
	 *	     x = string.gsub("hello world from Lua", "(%w+)%s*(%w+)", "%2 %1")
	 *	     --> x="world hello Lua from"
	 *
	 *	     x = string.gsub("home = $HOME, user = $USER", "%$(%w+)", os.getenv)
	 *	     --> x="home = /home/roberto, user = roberto"
	 *
	 *	     x = string.gsub("4+5 = $return 4+5$", "%$(.-)%$", function (s)
	 *	           return loadstring(s)()
	 *       end)
	 *	     --> x="4+5 = 9"
	 *
	 *	     local t = {name="lua", version="5.1"}
	 *	     x = string.gsub("$name-$version.tar.gz", "%$(%w+)", t)
	 *	     --> x="lua-5.1.tar.gz"
	 */
	static void gsub( LuaState vm ) {
		LString src = vm.checklstring(2);
		final int srclen = src.length();
		LString p = vm.checklstring(3);
		LValue repl = vm.topointer( 4 );
		int max_s = vm.optint( 5, srclen + 1 );
		final boolean anchor = p.length() > 0 && p.charAt( 0 ) == '^';
		
		LBuffer lbuf = new LBuffer( srclen );
		MatchState ms = new MatchState( vm, src, p );
		
		int soffset = 0;
		int n = 0;
		while ( n < max_s ) {
			ms.reset();
			int res = ms.match( soffset, anchor ? 1 : 0 );
			if ( res != -1 ) {
				n++;
				ms.add_value( lbuf, soffset, res, repl );
			}
			if ( res != -1 && res > soffset )
				soffset = res;
			else if ( soffset < srclen )
				lbuf.append( (byte) src.luaByte( soffset++ ) );
			else
				break;
			if ( anchor )
				break;
		}
		lbuf.append( src.substring( soffset, srclen ) );
		vm.resettop();
		vm.pushlstring( lbuf.toLuaString() );
		vm.pushinteger( n );
	}
	
	/** 
	 * string.len (s)
	 * 
	 * Receives a string and returns its length. The empty string "" has length 0. 
	 * Embedded zeros are counted, so "a\000bc\000" has length 5. 
	 */
	static void len( LuaState vm ) {
		int l = vm.checklstring(2).length();
		vm.resettop();
		vm.pushinteger( l );
	}

	/** 
	 * string.lower (s)
	 * 
	 * Receives a string and returns a copy of this string with all uppercase letters 
	 * changed to lowercase. All other characters are left unchanged. 
	 * The definition of what an uppercase letter is depends on the current locale.
	 */
	static void lower( LuaState vm ) {
		String s = vm.checkstring(2).toLowerCase();
		vm.resettop();
		vm.pushstring( s );
	}

	/**
	 * string.match (s, pattern [, init])
	 * 
	 * Looks for the first match of pattern in the string s. If it finds one,
	 * then match returns the captures from the pattern; otherwise it returns
	 * nil. If pattern specifies no captures, then the whole match is returned.
	 * A third, optional numerical argument init specifies where to start the
	 * search; its default value is 1 and may be negative.
	 */
	static void match( LuaState vm ) {
		str_find_aux( vm, false );
	}
	
	/**
	 * string.rep (s, n)
	 * 
	 * Returns a string that is the concatenation of n copies of the string s. 
	 */
	static void rep( LuaState vm ) {
		LString s = vm.checklstring(2);
		int n = vm.checkint( 3 );
		vm.resettop();
		if ( n >= 0 ) {
			final byte[] bytes = new byte[ s.length() * n ];
			int len = s.length();
			for ( int offset = 0; offset < bytes.length; offset += len ) {
				s.copyInto( 0, bytes, offset, len );
			}
			vm.pushlstring( bytes );
		}
	}

	/** 
	 * string.reverse (s)
	 * 
	 * Returns a string that is the string s reversed. 
	 */
	static void reverse( LuaState vm ) {		
		LString s = vm.checklstring(2);
		int n = s.length();
		byte[] b = new byte[n];
		for ( int i=0, j=n-1; i<n; i++, j-- )
			b[j] = (byte) s.luaByte(i);
		vm.resettop();
		vm.pushlstring( b );
	}

	/** 
	 * string.sub (s, i [, j])
	 * 
	 * Returns the substring of s that starts at i and continues until j; 
	 * i and j may be negative. If j is absent, then it is assumed to be equal to -1 
	 * (which is the same as the string length). In particular, the call 
	 *    string.sub(s,1,j) 
	 * returns a prefix of s with length j, and 
	 *   string.sub(s, -i) 
	 * returns a suffix of s with length i.
	 */
	static void sub( LuaState vm ) {
		final LString s = vm.checklstring(2);
		final int l = s.length();
		
		int start = posrelat( vm.checkint( 3 ), l );
		int end = posrelat( vm.optint( 4, -1 ), l );
		
		if ( start < 1 )
			start = 1;
		if ( end > l )
			end = l;
		
		vm.resettop();
		if ( start <= end ) {
			LString result = s.substring( start-1 , end );
			vm.pushlstring( result );
		} else {
			vm.pushstring( "" );
		}
	}
	
	/** 
	 * string.upper (s)
	 * 
	 * Receives a string and returns a copy of this string with all lowercase letters 
	 * changed to uppercase. All other characters are left unchanged. 
	 * The definition of what a lowercase letter is depends on the current locale.	
	 */
	static void upper( LuaState vm ) {
		String s = vm.checkstring(2).toUpperCase();
		vm.resettop();
		vm.pushstring(s);
	}
	
	/**
	 * This utility method implements both string.find and string.match.
	 */
	static void str_find_aux( LuaState vm, boolean find ) {
		LString s = vm.checklstring(2);
		LString pat = vm.checklstring(3);
		int init = vm.optint( 4 , 1 );
		
		if ( init > 0 ) {
			init = Math.min( init - 1, s.length() );
		} else if ( init < 0 ) {
			init = Math.max( 0, s.length() + init );
		}
		
		boolean fastMatch = find && ( vm.toboolean( 5 ) || pat.indexOfAny( SPECIALS ) == -1 );
		vm.resettop();
		
		if ( fastMatch ) {
			int result = s.indexOf( pat, init );
			if ( result != -1 ) {
				vm.pushinteger( result + 1 );
				vm.pushinteger( result + pat.length() );
				return;
			}
		} else {
			MatchState ms = new MatchState( vm, s, pat );
			
			boolean anchor = false;
			int poff = 0;
			if ( pat.luaByte( 0 ) == '^' ) {
				anchor = true;
				poff = 1;
			}
			
			int soff = init;
			do {
				int res;
				ms.reset();
				if ( ( res = ms.match( soff, poff ) ) != -1 ) {
					if ( find ) {
						vm.pushinteger( soff + 1 );
						vm.pushinteger( res );
						ms.push_captures( false, soff, res );
					} else {
						ms.push_captures( true, soff, res );
					}
					return;
				}
			} while ( soff++ < s.length() && !anchor );
		}
		vm.pushnil();
	}
	
	private static int posrelat( int pos, int len ) {
		return ( pos >= 0 ) ? pos : len + pos + 1;
	}
	
	// Pattern matching implementation
	
	private static final int L_ESC = '%';
	private static final LString SPECIALS = new LString("^$*+?.([%-");
	private static final int MAX_CAPTURES = 32;
	
	private static final int CAP_UNFINISHED = -1;
	private static final int CAP_POSITION = -2;
	
	private static final byte MASK_ALPHA		= 0x01;
	private static final byte MASK_LOWERCASE	= 0x02;
	private static final byte MASK_UPPERCASE	= 0x04;
	private static final byte MASK_DIGIT		= 0x08;
	private static final byte MASK_PUNCT		= 0x10;
	private static final byte MASK_SPACE		= 0x20;
	private static final byte MASK_CONTROL		= 0x40;
	private static final byte MASK_HEXDIGIT		= (byte)0x80;
	
	private static final byte[] CHAR_TABLE;
	
	static {
		CHAR_TABLE = new byte[256];
		
		for ( int i = 0; i < 256; ++i ) {
			final char c = (char) i;
			CHAR_TABLE[i] = (byte)( ( Character.isDigit( c ) ? MASK_DIGIT : 0 ) |
							( Character.isLowerCase( c ) ? MASK_LOWERCASE : 0 ) |
							( Character.isUpperCase( c ) ? MASK_UPPERCASE : 0 ) |
							( ( c < ' ' || c == 0x7F ) ? MASK_CONTROL : 0 ) );
			if ( ( c >= 'a' && c <= 'f' ) || ( c >= 'A' && c <= 'F' ) || ( c >= '0' && c <= '9' ) ) {
				CHAR_TABLE[i] |= MASK_HEXDIGIT;
			}
			if ( ( c >= '!' && c <= '/' ) || ( c >= ':' && c <= '@' ) ) {
				CHAR_TABLE[i] |= MASK_PUNCT;
			}
			if ( ( CHAR_TABLE[i] & ( MASK_LOWERCASE | MASK_UPPERCASE ) ) != 0 ) {
				CHAR_TABLE[i] |= MASK_ALPHA;
			}
		}
		
		CHAR_TABLE[' '] = MASK_SPACE;
		CHAR_TABLE['\r'] |= MASK_SPACE;
		CHAR_TABLE['\n'] |= MASK_SPACE;
		CHAR_TABLE['\t'] |= MASK_SPACE;
		CHAR_TABLE[0x0C /* '\v' */ ] |= MASK_SPACE;
		CHAR_TABLE['\f'] |= MASK_SPACE;
	};
	
	private static class MatchState {
		final LString s;
		final LString p;
		final LuaState vm;
		int level;
		int[] cinit;
		int[] clen;
		
		MatchState( LuaState vm, LString s, LString pattern ) {
			this.s = s;
			this.p = pattern;
			this.vm = vm;
			this.level = 0;
			this.cinit = new int[ MAX_CAPTURES ];
			this.clen = new int[ MAX_CAPTURES ];
		}
		
		void reset() {
			level = 0;
		}
		
		private void add_s( LBuffer lbuf, LString news, int soff, int e ) {
			int l = news.length();
			for ( int i = 0; i < l; ++i ) {
				byte b = (byte) news.luaByte( i );
				if ( b != L_ESC ) {
					lbuf.append( (byte) b );
				} else {
					++i; // skip ESC
					b = (byte) news.luaByte( i );
					if ( !Character.isDigit( (char) b ) ) {
						lbuf.append( b );
					} else if ( b == '0' ) {
						lbuf.append( s.substring( soff, e ) );
					} else {
						push_onecapture( b - '1', soff, e );
						lbuf.append( vm.topointer( -1 ).luaAsString() );
						vm.pop( 1 );
					}
				}
			}
		}
		
		public void add_value( LBuffer lbuf, int soffset, int end, LValue repl ) {
			switch ( repl.luaGetType() ) {
			case Lua.LUA_TSTRING:
			case Lua.LUA_TNUMBER:
				add_s( lbuf, repl.luaAsString(), soffset, end );
				return;
				
			case Lua.LUA_TFUNCTION:
				vm.pushlvalue( repl );
				int n = push_captures( true, soffset, end );
				vm.call( n, 1 );
				break;
				
			case Lua.LUA_TTABLE:
				// Need to call push_onecapture here for the error checking
				push_onecapture( 0, soffset, end );
				LValue k = vm.topointer( -1 );
				vm.pop( 1 );
				vm.pushlvalue( ((LTable) repl).luaGetTable( vm, k ) );
				break;
				
			default:
				vm.error( "bad argument: string/function/table expected" );
				return;
			}
			
			repl = vm.topointer( -1 );
			if ( !repl.toJavaBoolean() ) {
				repl = s.substring( soffset, end );
			} else if ( ! repl.isString() ) {
				vm.error( "invalid replacement value (a "+repl.luaGetTypeName()+")" );
			}
			vm.pop( 1 );
			lbuf.append( repl.luaAsString() );
		}
		
		int push_captures( boolean wholeMatch, int soff, int end ) {
			int nlevels = ( this.level == 0 && wholeMatch ) ? 1 : this.level;
			for ( int i = 0; i < nlevels; ++i ) {
				push_onecapture( i, soff, end );
			}
			return nlevels;
		}
		
		private void push_onecapture( int i, int soff, int end ) {
			if ( i >= this.level ) {
				if ( i == 0 ) {
					vm.pushlstring( s.substring( soff, end ) );
				} else {
					vm.error( "invalid capture index" );
				}
			} else {
				int l = clen[i];
				if ( l == CAP_UNFINISHED ) {
					vm.error( "unfinished capture" );
				}
				if ( l == CAP_POSITION ) {
					vm.pushinteger( cinit[i] + 1 );
				} else {
					int begin = cinit[i];
					vm.pushlstring( s.substring( begin, begin + l ) );
				}
			}
		}
		
		private int check_capture( int l ) {
			l -= '1';
			if ( l < 0 || l >= level || this.clen[l] == CAP_UNFINISHED ) {
				vm.error("invalid capture index");
			}
			return l;
		}
		
		private int capture_to_close() {
			int level = this.level;
			for ( level--; level >= 0; level-- )
				if ( clen[level] == CAP_UNFINISHED )
					return level;
			vm.error("invalid pattern capture");
			return 0;
		}
		
		int classend( int poffset ) {
			switch ( p.luaByte( poffset++ ) ) {
			case L_ESC:
				if ( poffset == p.length() ) {
					vm.error( "malformed pattern (ends with %)" );
				}
				return poffset + 1;
				
			case '[':
				if ( p.luaByte( poffset ) == '^' ) poffset++;
				do {
					if ( poffset == p.length() ) {
						vm.error( "malformed pattern (missing ])" );
					}
					if ( p.luaByte( poffset++ ) == L_ESC && poffset != p.length() )
						poffset++;
				} while ( p.luaByte( poffset ) != ']' );
				return poffset + 1;
			default:
				return poffset;
			}
		}
		
		static boolean match_class( int c, int cl ) {
			final char lcl = Character.toLowerCase( (char) cl );
			int cdata = CHAR_TABLE[c];
			
			boolean res;
			switch ( lcl ) {
			case 'a': res = ( cdata & MASK_ALPHA ) != 0; break;
			case 'd': res = ( cdata & MASK_DIGIT ) != 0; break;
			case 'l': res = ( cdata & MASK_LOWERCASE ) != 0; break;
			case 'u': res = ( cdata & MASK_UPPERCASE ) != 0; break;
			case 'c': res = ( cdata & MASK_CONTROL ) != 0; break;
			case 'p': res = ( cdata & MASK_PUNCT ) != 0; break;
			case 's': res = ( cdata & MASK_SPACE ) != 0; break;
			case 'w': res = ( cdata & ( MASK_ALPHA | MASK_DIGIT ) ) != 0; break;
			case 'x': res = ( cdata & MASK_HEXDIGIT ) != 0; break;
			case 'z': res = ( c == 0 ); break;
			default: return cl == c;
			}
			return ( lcl == cl ) ? res : !res;
		}
		
		boolean matchbracketclass( int c, int poff, int ec ) {
			boolean sig = true;
			if ( p.luaByte( poff + 1 ) == '^' ) {
				sig = false;
				poff++;
			}
			while ( ++poff < ec ) {
				if ( p.luaByte( poff ) == L_ESC ) {
					poff++;
					if ( match_class( c, p.luaByte( poff ) ) )
						return sig;
				}
				else if ( ( p.luaByte( poff + 1 ) == '-' ) && ( poff + 2 < ec ) ) {
					poff += 2;
					if ( p.luaByte( poff - 2 ) <= c && c <= p.luaByte( poff ) )
						return sig;
				}
				else if ( p.luaByte( poff ) == c ) return sig;
			}
			return !sig;
		}
		
		boolean singlematch( int c, int poff, int ep ) {
			switch ( p.luaByte( poff ) ) {
			case '.': return true;
			case L_ESC: return match_class( c, p.luaByte( poff + 1 ) );
			case '[': return matchbracketclass( c, poff, ep - 1 );
			default: return p.luaByte( poff ) == c;
			}
		}
		
		/**
		 * Perform pattern matching. If there is a match, returns offset into s
		 * where match ends, otherwise returns -1.
		 */
		int match( int soffset, int poffset ) {
			while ( true ) {
				// Check if we are at the end of the pattern - 
				// equivalent to the '\0' case in the C version, but our pattern
				// string is not NUL-terminated.
				if ( poffset == p.length() )
					return soffset;
				switch ( p.luaByte( poffset ) ) {
				case '(':
					if ( ++poffset < p.length() && p.luaByte( poffset ) == ')' )
						return start_capture( soffset, poffset + 1, CAP_POSITION );
					else
						return start_capture( soffset, poffset, CAP_UNFINISHED );
				case ')':
					return end_capture( soffset, poffset + 1 );
				case L_ESC:
					if ( poffset + 1 == p.length() )
						vm.error("malformed pattern (ends with '%')");
					switch ( p.luaByte( poffset + 1 ) ) {
					case 'b':
						soffset = matchbalance( soffset, poffset + 2 );
						if ( soffset == -1 ) return -1;
						poffset += 4;
						continue;
					case 'f': {
						poffset += 2;
						if ( p.luaByte( poffset ) != '[' ) {
							vm.error("Missing [ after %f in pattern");
						}
						int ep = classend( poffset );
						int previous = ( soffset == 0 ) ? -1 : s.luaByte( soffset - 1 );
						if ( matchbracketclass( previous, poffset, ep - 1 ) ||
							 matchbracketclass( s.luaByte( soffset ), poffset, ep - 1 ) )
							return -1;
						poffset = ep;
						continue;
					}
					default: {
						int c = p.luaByte( poffset + 1 );
						if ( Character.isDigit( (char) c ) ) {
							soffset = match_capture( soffset, c );
							if ( soffset == -1 )
								return -1;
							return match( soffset, poffset + 2 );
						}
					}
					}
				case '$':
					if ( poffset + 1 == p.length() )
						return ( soffset == s.length() ) ? soffset : -1;
				}
				int ep = classend( poffset );
				boolean m = soffset < s.length() && singlematch( s.luaByte( soffset ), poffset, ep );
				int pc = ( ep < p.length() ) ? p.luaByte( ep ) : '\0';
				
				switch ( pc ) {
				case '?':
					int res;
					if ( m && ( ( res = match( soffset + 1, ep + 1 ) ) != -1 ) )
						return res;
					poffset = ep + 1;
					continue;
				case '*':
					return max_expand( soffset, poffset, ep );
				case '+':
					return ( m ? max_expand( soffset + 1, poffset, ep ) : -1 );
				case '-':
					return min_expand( soffset, poffset, ep );
				default:
					if ( !m )
						return -1;
					soffset++;
					poffset = ep;
					continue;
				}
			}
		}
		
		int max_expand( int soff, int poff, int ep ) {
			int i = 0;
			while ( soff + i < s.length() &&
					singlematch( s.luaByte( soff + i ), poff, ep ) )
				i++;
			while ( i >= 0 ) {
				int res = match( soff + i, ep + 1 );
				if ( res != -1 )
					return res;
				i--;
			}
			return -1;
		}
		
		int min_expand( int soff, int poff, int ep ) {
			for ( ;; ) {
				int res = match( soff, ep + 1 );
				if ( res != -1 )
					return res;
				else if ( soff < s.length() && singlematch( s.luaByte( soff ), poff, ep ) )
					soff++;
				else return -1;
			}
		}
		
		int start_capture( int soff, int poff, int what ) {
			int res;
			int level = this.level;
			if ( level >= MAX_CAPTURES ) {
				vm.error( "too many captures" );
			}
			cinit[ level ] = soff;
			clen[ level ] = what;
			this.level = level + 1;
			if ( ( res = match( soff, poff ) ) == -1 )
				this.level--;
			return res;
		}
		
		int end_capture( int soff, int poff ) {
			int l = capture_to_close();
			int res;
			clen[l] = soff - cinit[l];
			if ( ( res = match( soff, poff ) ) == -1 )
				clen[l] = CAP_UNFINISHED;
			return res;
		}
		
		int match_capture( int soff, int l ) {
			l = check_capture( l );
			int len = clen[ l ];
			if ( ( s.length() - soff ) >= len &&
				 LString.equals( s, cinit[l], s, soff, len ) )
				return soff + len;
			else
				return -1;
		}
		
		int matchbalance( int soff, int poff ) {
			final int plen = p.length();
			if ( poff == plen || poff + 1 == plen ) {
				vm.error( "unbalanced pattern" );
			}
			if ( s.luaByte( soff ) != p.luaByte( poff ) )
				return -1;
			else {
				int b = p.luaByte( poff );
				int e = p.luaByte( poff + 1 );
				int cont = 1;
				while ( ++soff < s.length() ) {
					if ( s.luaByte( soff ) == e ) {
						if ( --cont == 0 ) return soff + 1;
					}
					else if ( s.luaByte( soff ) == b ) cont++;
				}
			}
			return -1;
		}
	}
}
