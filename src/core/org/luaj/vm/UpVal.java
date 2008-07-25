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


public class UpVal {

	LuaState state;
	int position;
	LValue value;
	
	public UpVal( LuaState state, int i ) {
		this.state = state;
		this.position = i;
	}
	
	public String toString() {
		return "up."+position;
	}
	
	public LValue getValue() {
		if ( state == null )
			return value;
		else
			return state.stack[ position ];
	}
	
	public void setValue( LValue value ) {
		if ( state == null )
			this.value = value;
		else
			state.stack[ position ] = value;
	}
	
	public boolean close( int limit ) {
		if ( position >= limit ) {
			value = state.stack[ position ];
			state = null;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isClosed() {
		return state == null;
	}
}
