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
package org.luaj.vm2.lib.jse;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * Math library implementation for use on JSE platform.
 * 
 * Implements all "math" functions, including platform-specific
 * overrides for pow() and exp()
 */
public class JseMathLib extends org.luaj.vm2.lib.MathLib {
	
	public JseMathLib() {
		LibFunction.bind( this, new J2seMathFunc1().getClass(), new String[] {
			"acos", "asin", "atan", "cosh",  
			"exp", "log", "log10", "sinh",  
			"tanh" } );
		LibFunction.bind( this, new J2seMathFunc2().getClass(), new String[] {
			"atan2", "pow", } );
		
	}
	
	public static class J2seMathFunc1 extends OneArgFunction {
		public LuaValue call(LuaValue arg) {
			switch ( opcode ) {
			case 0: return valueOf(Math.acos(arg.todouble())); 
			case 1: return valueOf(Math.asin(arg.todouble())); 
			case 2: return valueOf(Math.atan(arg.todouble())); 
			case 3: return valueOf(Math.cosh(arg.todouble())); 
			case 4: return valueOf(Math.exp(arg.todouble())); 
			case 5: return valueOf(Math.log(arg.todouble())); 
			case 6: return valueOf(Math.log10(arg.todouble())); 
			case 7: return valueOf(Math.sinh(arg.todouble())); 
			case 8: return valueOf(Math.tanh(arg.todouble())); 
			}
			return NIL;
		}
	}
	
	public static class J2seMathFunc2 extends TwoArgFunction {
		public LuaValue call(LuaValue arg1,LuaValue arg2) {
			switch ( opcode ) {
			case 0: return valueOf(Math.atan2(arg1.todouble(), arg2.todouble()));
			case 1: return valueOf(Math.pow(arg1.todouble(), arg2.todouble()));
			}
			return NIL;
		}
	}

	/** Faster, better version of pow() used by arithmetic operator ^ */
	public double dpow_d(double a, double b) {
		return Math.pow(a, b);
	}
	
	
}
