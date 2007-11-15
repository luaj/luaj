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
package org.luaj.vm;

/**
 * RuntimeException that is thrown and caught in response to a lua error. 
 * This error does not indicate any problem with the normal functioning 
 * of the Lua VM, but rather indicates that the lua script being interpreted
 * has encountered a lua error, eigher via LuaState.error() or lua error() calls. 
 *  
 */
public class LuaErrorException extends RuntimeException {
	
	private static final String DEFAULT_MESSAGE = "lua error";

	/**
	 * Construct a LuaErrorException with the default message. 
	 */
	public LuaErrorException() {
		super(DEFAULT_MESSAGE);
	}

	/**
	 * Construct a LuaErrorException with a specific message.
	 *  
	 * @param message message to supply
	 */
	public LuaErrorException(String message) {
		super(message);
	}

	/**
	 * Construct a LuaErrorException in response to a Throwable that was caught
	 * and with the default message. 
	 */
	public LuaErrorException(Throwable cause) {
		super(DEFAULT_MESSAGE, cause);
	}

	/** 
	 * Construct a LuaErrorException in response to a Throwable that was caught
	 * and with a specific message.
	 *  
	 * @param message message to supply
	 * @param cause
	 */
	public LuaErrorException(String message, Throwable cause) {
		super(message, cause);
	}

}
