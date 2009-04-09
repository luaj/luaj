/*******************************************************************************
* Copyright (c) 2009 LuaJ. All rights reserved.
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

import java.io.IOException;

import org.luaj.vm.LFunction;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;

/**
 * Base implementation of OsLib, with simplified stub functions
 * for library functions that cannot be implemented uniformly 
 * on J2se and J2me.   
 * 
 * This can be installed as-is on either platform, or extended 
 * and refined to be used in a complete J2se implementation. 
 */
public class OsLib extends LFunction {
	public static String TMP_PREFIX    = ".luaj";
	public static String TMP_SUFFIX    = "tmp";

	/** 
	 * Create a function stub with a specific index. 
	 * This is a factory method so that subclasses can 
	 * use install(LTable,OsLib) to install themselves.
	 */
	protected OsLib newInstance( int index ) {
		return new OsLib(index);
	}
	
	public static final String[] NAMES = {
		"os",
		"clock",
		"date",
		"difftime",
		"execute",
		"exit",
		"getenv",
		"remove",
		"rename",
		"setlocale",
		"time",
		"tmpname",
	};
	
	private static final int INSTALL   = 0;
	private static final int CLOCK     = 1;
	private static final int DATE      = 2;
	private static final int DIFFTIME  = 3;
	private static final int EXECUTE   = 4;
	private static final int EXIT      = 5;
	private static final int GETENV    = 6;
	private static final int REMOVE    = 7;
	private static final int RENAME    = 8;
	private static final int SETLOCALE = 9;
	private static final int TIME      = 10;
	private static final int TMPNAME   = 11;

	/** 
	 * Install the os library using the OsLib class as a factory 
	 */
	public static void install( LTable globals ) {
		install(globals, new OsLib());
	}
	
	/** 
	 * Initialize the os libaray using the subblied instance as a factory.
	 * @param factory instance to use as a factory for function stubs.  
	 */
	public static void install( LTable globals, OsLib factory ) {
		LTable os = new LTable();
		for ( int i=CLOCK; i<=TMPNAME; i++ )
			os.put(NAMES[i], factory.newInstance(i));
		globals.put( "os", os );
		PackageLib.setIsLoaded("os", os);
	}

	private static final long t0 = System.currentTimeMillis();
	private static long tmpnames = t0;

	private final int id;

	protected OsLib() {
		id = 0;
	}

	protected OsLib( int id ) {
		this.id = id;
	}

	public LString luaAsString() {
		return new LString(toJavaString());
	}
	
	public String toJavaString() {
		return "os."+toString();
	}

	public String toString() {
		return NAMES[id]+"()";
	}

	public int invoke( LuaState vm ) {
		LValue v;
		long t,t2;
		int c;
		String s;
		try {
			switch ( id ) {
				case INSTALL:
					install(vm._G, this);
					return 0;
				case CLOCK:
					vm.pushnumber(clock());
					return 1;
				case DATE:
					s = vm.optstring(1, null);
					t = vm.optlong(2,-1);
					vm.pushlvalue( date(s, t==-1? System.currentTimeMillis(): t) );
					return 1;
				case DIFFTIME:
					t2 = vm.checklong(1);
					t = vm.checklong(2);
					vm.pushnumber(difftime(t2,t));
					return 1;
				case EXECUTE:
					c = execute(vm.optstring(1, null));
					vm.pushinteger(c);
					return 1;
				case EXIT:
					exit(vm.optint(1, 0));
					return 0;
				case GETENV:
					s = getenv(vm.checkstring(1));
					vm.pushstring(s);
					return 1;
				case REMOVE:
					remove(vm.checkstring(1));
					vm.pushboolean(true);
					return 1;
				case RENAME:
					rename(vm.checkstring(1), vm.checkstring(2));
					vm.pushboolean(true);
					return 1;
				case SETLOCALE:
					s = setlocale(vm.optstring(1,null), vm.optstring(2, "all"));
					if ( s != null )
						vm.pushstring(s);
					else
						vm.pushnil();
					return 1;
				case TIME:
					t = time(vm.isnoneornil(1)? null: vm.checktable(1));
					vm.pushnumber(t);
					return 1;
				case TMPNAME:
					vm.pushstring(tmpname());
					return 1;
				default:
					LuaState.vmerror( "bad os id" );
					return 0;
			}
		} catch ( IOException e ) {
			vm.pushnil();
			vm.pushstring(e.getMessage());
			return 2;
		}
	}

	/**
	 * @return an approximation of the amount in seconds of CPU time used by 
	 * the program.
	 */
	protected double clock() {
		return (System.currentTimeMillis()-t0) / 1000.;
	}

	/**
	 * Returns the number of seconds from time t1 to time t2. 
	 * In POSIX, Windows, and some other systems, this value is exactly t2-t1.
	 * @param t2
	 * @param t1
	 * @return diffeence in time values, in seconds
	 */
	protected double difftime(long t2, long t1) {
		return (t2 - t1) / 1000.;
	}

	/**
	 * If the time argument is present, this is the time to be formatted 
	 * (see the os.time function for a description of this value). 
	 * Otherwise, date formats the current time.
	 * 
	 * If format starts with '!', then the date is formatted in Coordinated 
	 * Universal Time. After this optional character, if format is the string 
	 * "*t", then date returns a table with the following fields: year 
	 * (four digits), month (1--12), day (1--31), hour (0--23), min (0--59), 
	 * sec (0--61), wday (weekday, Sunday is 1), yday (day of the year), 
	 * and isdst (daylight saving flag, a boolean).
	 * 
	 * If format is not "*t", then date returns the date as a string, 
	 * formatted according to the same rules as the C function strftime.
	 * 
	 * When called without arguments, date returns a reasonable date and 
	 * time representation that depends on the host system and on the 
	 * current locale (that is, os.date() is equivalent to os.date("%c")).
	 *  
	 * @param format 
	 * @param time time since epoch, or -1 if not supplied
	 * @return a LString or a LTable containing date and time, 
	 * formatted according to the given string format.
	 */
	protected LValue date(String format, long time) {
		return LString.valueOf( new java.util.Date(time).toString() );
	}

	/** 
	 * This function is equivalent to the C function system. 
	 * It passes command to be executed by an operating system shell. 
	 * It returns a status code, which is system-dependent. 
	 * If command is absent, then it returns nonzero if a shell 
	 * is available and zero otherwise.
	 * @param command command to pass to the system
	 */ 
	protected int execute(String command) {
		return 0;
	}

	/**
	 * Calls the C function exit, with an optional code, to terminate the host program. 
	 * @param code
	 */
	protected void exit(int code) {
		System.exit(code);
	}

	/**
	 * Returns the value of the process environment variable varname, 
	 * or null if the variable is not defined. 
	 * @param varname
	 * @return
	 */
	protected String getenv(String varname) {
		return System.getProperty(varname);
	}

	/**
	 * Deletes the file or directory with the given name. 
	 * Directories must be empty to be removed. 
	 * If this function fails, it throws and IOException
	 *  
	 * @param filename
	 * @throws IOException if it fails
	 */
	protected void remove(String filename) throws IOException {
		throw new IOException( "not implemented" );
	}

	/**
	 * Renames file or directory named oldname to newname. 
	 * If this function fails,it throws and IOException
	 *  
	 * @param oldname old file name
	 * @param newname new file name
	 * @throws IOException if it fails
	 */
	protected void rename(String oldname, String newname) throws IOException {
		throw new IOException( "not implemented" );
	}

	/**
	 * Sets the current locale of the program. locale is a string specifying 
	 * a locale; category is an optional string describing which category to change: 
	 * "all", "collate", "ctype", "monetary", "numeric", or "time"; the default category 
	 * is "all". 
	 * 
	 * If locale is the empty string, the current locale is set to an implementation-
	 * defined native locale. If locale is the string "C", the current locale is set 
	 * to the standard C locale.
	 * 
	 * When called with null as the first argument, this function only returns the 
	 * name of the current locale for the given category.
	 *  
	 * @param locale
	 * @param category
	 * @return the name of the new locale, or null if the request 
	 * cannot be honored.
	 */
	protected String setlocale(String locale, String category) {
		return null;
	}

	/**
	 * Returns the current time when called without arguments, 
	 * or a time representing the date and time specified by the given table. 
	 * This table must have fields year, month, and day, 
	 * and may have fields hour, min, sec, and isdst 
	 * (for a description of these fields, see the os.date function).
	 * @param table
	 * @return long value for the time
	 */
	protected long time(LTable table) {
		return System.currentTimeMillis();
	}

	/**
	 * Returns a string with a file name that can be used for a temporary file. 
	 * The file must be explicitly opened before its use and explicitly removed 
	 * when no longer needed.
	 * 
	 * On some systems (POSIX), this function also creates a file with that name, 
	 * to avoid security risks. (Someone else might create the file with wrong 
	 * permissions in the time between getting the name and creating the file.) 
	 * You still have to open the file to use it and to remove it (even if you 
	 * do not use it). 
	 * 
	 * @return String filename to use
	 */
	protected String tmpname() {
		return TMP_PREFIX+(tmpnames++)+TMP_SUFFIX;
	}
}
