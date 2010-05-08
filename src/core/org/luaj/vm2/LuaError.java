/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
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
package org.luaj.vm2;

import java.util.Vector;

import org.luaj.vm2.lib.DebugLib;

/**
 * RuntimeException that is thrown and caught in response to a lua error. 
 * This error does not indicate any problem with the normal functioning 
 * of the Lua VM, but rather indicates that the lua script being interpreted
 * has encountered a lua error, eigher via LuaState.error() or lua error() calls. 
 *  
 */
public class LuaError extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private LuaValue msgvalue = null;
	private String traceback;

	/** Run the error hook if there is one */
	private static String errorHook(String msg) {
		LuaThread thread = LuaThread.getRunning();
		if ( thread.err != null ) { 
			LuaValue errfunc = thread.err;
			thread.err = null;
			try {
				return errfunc.call( LuaValue.valueOf(msg) ).tojstring();
			} catch ( Throwable t ) {
				return "error in error handling";
			} finally {
				thread.err = errfunc;
			}
		}
		return msg;
	}
	
	private Throwable cause;
	
	/**
	 * Construct a LuaErrorException in response to a Throwable that was caught
	 * indicating a problem with the VM rather than the lua code.
	 *  
	 * All errors generated from lua code should throw LuaError(String) instead. 
	 */
	public LuaError(Throwable cause) {
		super( errorHook( addFileLine( "vm error: "+cause ) ) );
		this.cause = cause;
		this.traceback = DebugLib.traceback(1);
	}

	/**
	 * Construct a LuaError with a specific message indicating a problem 
	 * within the lua code itself such as an argument type error.
	 *  
	 * @param message message to supply
	 */
	public LuaError(String message) {
		super( errorHook( addFileLine( message ) ) );
		this.traceback = DebugLib.traceback(1);
	}		

	/**
	 * @param message message to supply
	 * @param level where to supply line info from in call stack
	 */
	public LuaError(String message, int level) {
		super( errorHook( addFileLine( message, level ) ) );
		this.traceback = DebugLib.traceback(1);
	}	

	/** Add file and line info to a message at a particular level */
	private static String addFileLine( String message, int level ) {
		if ( message == null ) return null;
		if ( level == 0 ) return message;
		String fileline = DebugLib.fileline(level-1);
		return fileline!=null? fileline+": "+message: message;		
	}

	/** Add file and line info for the nearest enclosing closure */
	private static String addFileLine( String message ) {
		if ( message == null ) return null;
		String fileline = DebugLib.fileline();
		return fileline!=null? fileline+": "+message: message;		
	}
	
//	/** Get the message, including source line info if there is any */
//	public String getMessage() {		
//		String msg = super.getMessage();
//		return msg!=null && traceback!=null? traceback+": "+msg: msg;
//	}

	/** Print the message and stack trace */
	public void printStackTrace() {
		System.out.println( toString() );
		if ( traceback != null )
			System.out.println( traceback );
	}

	/** 
	 * Get the cause, if any.
	 */
	public Throwable getCause() {
		return cause;
	}


}
