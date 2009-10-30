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


/** Upvalue used with Closure formulation */
public final class UpValue {

	LuaValue[] array; // initially the stack, becomes a holder 
	int index;
	boolean closed;
	UpValue next = null;
	
	public UpValue( LuaValue[] stack, int index, UpValue next ) {
		this.array = stack;
		this.index = index;
		this.next = next;
	}
	
	public String toString() {
		return (closed? "-": "+") + array[index];
	}
	
	public final LuaValue getValue() {
		return array[index];
	}
	
	public final void setValue( LuaValue value ) {
		array[index] = value;
	}
	
	public final boolean close( int limit ) {
		if ( (!closed) && (index>=limit) ) {
			array = new LuaValue[] { array[index] };
			index = 0;
			return (closed = true);
		} else {
			return false;
		}
	}
	
	public final boolean isClosed() {
		return closed;
	}
}
