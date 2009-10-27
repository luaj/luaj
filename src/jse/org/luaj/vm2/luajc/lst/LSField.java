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
package org.luaj.vm2.luajc.lst;

public class LSField {
	public enum Type {
		keyValue,
		nameValue,
		listValue,
	}

	public final Type type;
	
	LSField(Type type) {
		this.type = type;
	}
	
	public static LSField keyValueField(LSExpression key, LSExpression value) {
		return new KeyValue(key, value);
	}

	public static LSField nameValueField(String name, LSExpression value) {
		return new NameValue(name, value);
	}

	public static LSField valueField(LSExpression value) {
		return new ListValue(value);
	}
	
	/** table constructor field with an explicit key index value */
	public static class KeyValue extends LSField {
		public final LSExpression key;
		public final LSExpression value;
		public KeyValue(LSExpression key, LSExpression value) {
			super( Type.keyValue );
			this.key = key;
			this.value = value;
			value.setNumReturns(1);
		}
		public String toString() { return "["+key+"]="+value; }
	}
	
	
	/** table constructor field with an named field for key */
	public static class NameValue extends LSField {
		public final String name;
		public final LSExpression value;
		public NameValue(String name, LSExpression value) {
			super( Type.nameValue );
			this.name = name;
			this.value = value;
			value.setNumReturns(1);
		}
		public String toString() { return name+"="+value; }
	}
	
	/** table constructor field with an implied index key */
	public static class ListValue extends LSField {
		public final LSExpression value;
		public ListValue(LSExpression value) {
			super( Type.listValue );
			this.value = value;
		}
		public void setNumReturns(int i) {
			value.setNumReturns(i);
		}
		public String toString() { return value.toString(); }
	}

	public void setNumReturns(int i) {
	}
	
}
