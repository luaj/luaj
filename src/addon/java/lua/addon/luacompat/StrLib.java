package lua.addon.luacompat;

import lua.VM;
import lua.value.LInteger;
import lua.value.LNil;
import lua.value.LString;

public class StrLib {
	/**
	 * string.byte (s [, i [, j]]) 
	 * 
	 * Returns the internal numerical codes of the
	 * characters s[i], s[i+1], ···, s[j]. The default value for i is 1; the
	 * default value for j is i.
	 * 
	 * Note that numerical codes are not necessarily portable across platforms.
	 * 
	 * @param vm the calling vm
	 */
	static void byte_( VM vm ) {
		LString ls = vm.getArgAsLuaString(1);
		int i = vm.getArgAsInt(2);
		int j = vm.getArgAsInt(3);
		int n = ls.length();
		i = Math.max(1, i);
		j = Math.min(n, (j==0? i: j));
		vm.setResult();
		for ( int k=i; k<=j; k++ ) 
			vm.push( new LInteger( ls.luaByte(k-1) ) );
	}

	/** 
	 * string.char (···)
	 * 
	 * Receives zero or more integers. Returns a string with length equal 
	 * to the number of arguments, in which each character has the internal 
	 * numerical code equal to its corresponding argument. 
	 * 
	 * Note that numerical codes are not necessarily portable across platforms.
	 * 
	 * @param vm the calling VM
	 */
	public static void char_( VM vm) {
		int nargs = vm.getArgCount();
		byte[] bytes = new byte[nargs];
		for ( int i=1; i<=nargs; i++ )
			vm.getArgAsInt(i);
		vm.setResult( new LString( bytes ) );
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
	static void dump( VM vm ) {
		vm.setResult( new LString("not supported") );
		vm.lua_error();
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
	static void find( VM vm ) {
		LString pattern = vm.getArgAsLuaString(1);
	}

	/** 
	 * string.format (formatstring, ···)
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
	static void format( VM vm ) {		
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
	static void gmatch( VM vm ) {		
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
	static void gsub( VM vm ) {		
	}

	/** 
	 * string.len (s)
	 * 
	 * Receives a string and returns its length. The empty string "" has length 0. 
	 * Embedded zeros are counted, so "a\000bc\000" has length 5. 
	 */
	static void len( VM vm ) {		
		vm.setResult( new LInteger( vm.getArgAsLuaString(1).length()) );
	}

	/** 
	 * string.lower (s)
	 * 
	 * Receives a string and returns a copy of this string with all uppercase letters 
	 * changed to lowercase. All other characters are left unchanged. 
	 * The definition of what an uppercase letter is depends on the current locale.
	 */
	static void lower( VM vm ) {		
		vm.setResult( new LString( vm.getArgAsString(1).toLowerCase() ) );
	}

	/**
	 * string.match (s, pattern [, init])
	 * 
	 * Looks for the first match of pattern in the string s. If it finds one, then match returns the captures from the pattern; otherwise it returns nil. If pattern specifies no captures, then the whole match is returned. A third, optional numerical argument init specifies where to start the search; its default value is 1 and may be negative.
	 */
	static void match( VM vm ) {		
	}

	/**
	 * string.rep (s, n)
	 * 
	 * Returns a string that is the concatenation of n copies of the string s. 
	 */
	static void rep( VM vm ) {
		LString s = vm.getArgAsLuaString( 0 );
		int n = vm.getArgAsInt( 1 );
		if ( n >= 0 ) {
			final byte[] bytes = new byte[ s.length() * n ];
			int len = s.length();
			for ( int offset = 0; offset < bytes.length; offset += len ) {
				s.copyInto( 0, bytes, offset, len );
			}
			
			vm.setResult( new LString( bytes ) );
		} else {
			vm.setResult( LNil.NIL );
		}
	}

	/** 
	 * string.reverse (s)
	 * 
	 * Returns a string that is the string s reversed. 
	 */
	static void reverse( VM vm ) {		
		LString s = vm.getArgAsLuaString(1);
		int n = s.length();
		byte[] b = new byte[n];
		for ( int i=0, j=n; i<n; i++, j-- ) 
			b[j] = (byte) s.luaByte(i);
		vm.setResult( new LString(b) );
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
	static void sub( VM vm ) {		
		final LString s = vm.getArgAsLuaString( 0 );
		final int len = s.length();
		
		int i = vm.getArgAsInt( 1 );
		if ( i < 0 ) {
			// start at -i characters from the end
			i = Math.max( len + i, 0 );
		} else if ( i > 0 ) {
			// start at character i - 1
			i = i - 1;
		}
		
		int j = ( vm.getArgCount() > 2 ) ? vm.getArgAsInt( 2 ): -1;
		if ( j < 0 ) {
			j = Math.max( i, len + j + 1 );
		} else {
			j = Math.min( Math.max( i, j ), len );
		}
		
		LString result = s.substring( i, j );
		vm.setResult( result );
	}
	
	/** 
	 * string.upper (s)
	 * 
	 * Receives a string and returns a copy of this string with all lowercase letters 
	 * changed to uppercase. All other characters are left unchanged. 
	 * The definition of what a lowercase letter is depends on the current locale.	
	 */
	static void upper( VM vm ) {
		vm.setResult( new LString( vm.getArgAsString(1).toUpperCase() ) );
	}



}
