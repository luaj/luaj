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
package org.luaj.vm2.lib;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

abstract public class LibFunction extends LuaFunction {
	
	protected int opcode;
	protected String name;
	
	protected LibFunction() {		
	}
	
	public String tojstring() {
		return name != null? name: super.tojstring();
	}
	
	protected void bind(LuaValue env, Class factory,  String[] names ) {
		bind( env, factory, names, 0 );
	}
	
	protected void bind(LuaValue env, Class factory,  String[] names, int firstopcode ) {
		try {
			for ( int i=0, n=names.length; i<n; i++ ) {
				LibFunction f = (LibFunction) factory.newInstance();
				f.opcode = firstopcode + i;
				f.name = names[i];
				f.env = env;
				env.set(f.name, f);
			}
		} catch ( Exception e ) {
			throw new LuaError( "bind failed: "+e );
		}
	}
	
	
	protected void bind0(LuaValue env, String[] names) {
		bind(env, names, 0, 0);
	}
	protected void bind1(LuaValue env, String[] names) {
		bind(env, names, 1, 0);
	}
	protected void bind2(LuaValue env, String[] names) {
		bind(env, names, 2, 0);
	}
	protected void bind3(LuaValue env, String[] names) {
		bind(env, names, 3, 0);
	}
	protected void bindv(LuaValue env, String[] names) {
		bind(env, names, -1, 0);
	}
	protected void bind(LuaValue env,  String[] names, int numargs, int firstopcode ) {
		for ( int i=0, n=names.length; i<n; i++ ) {
			int opcode = firstopcode + i;
			String name = names[i];
			LibFunction binding;
			switch( numargs ) {
			case 0:  binding = bind0(name, opcode); break;
			case 1:  binding = bind1(name, opcode); break;
			case 2:  binding = bind2(name, opcode); break;
			case 3:  binding = bind3(name, opcode); break;
			default: binding = bindv(name, opcode); break;
			}
			env.set(names[i], binding);
		}
	}

	protected LibFunction bind0(String name, int opcode) {
		return new ZeroArgBinding(name, opcode, this);
	}
	protected LibFunction bind1(String name, int opcode) {
		return new OneArgBinding(name, opcode, this);
	}
	protected LibFunction bind2(String name, int opcode) {
		return new TwoArgBinding(name, opcode, this);
	}
	protected LibFunction bind3(String name, int opcode) {
		return new ThreeArgBinding(name, opcode, this);
	}
	protected LibFunction bindv(String name, int opcode) {
		return new VarArgBinding(name, opcode, this);
	}
	
	/** called when a zero-arg function is invoked */
	protected LuaValue oncall0(int opcode) {
		return NIL;
	}
	
	/** called when a one-arg function is invoked */
	protected LuaValue oncall1(int opcode, LuaValue arg) {
		return NIL;
	}
	/** called when a two-arg function is invoked */
	protected LuaValue oncall2(int opcode, LuaValue arg1, LuaValue arg2) {
		return NIL;
	}
	/** called when a three-arg function is invoked */
	protected LuaValue oncall3(int opcode, LuaValue arg1, LuaValue arg2, LuaValue arg3) {
		return NIL;
	}
	/** called when a var-arg function is invoked */
	protected Varargs oncallv(int opcode, Varargs args) {
		return NONE;
	}

	/** Binding to a one-arg function */
	private static class ZeroArgBinding extends LibFunction {
		private final LibFunction delegate;

		private ZeroArgBinding(String name, int opcode, LibFunction delegate) {
			this.name = name;
			this.opcode = opcode;
			this.delegate = delegate;
		}
		
		public String tojstring() {
			return name;
		}
		
		public LuaValue call() {
			return delegate.oncall0(opcode);
		}
		
		public LuaValue call(LuaValue arg) {
			return delegate.oncall0(opcode);
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			return delegate.oncall0(opcode);
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
			return delegate.oncall0(opcode);
		}

		public Varargs invoke(Varargs varargs) {
			return delegate.oncall0(opcode);
		}
	} 
	
	/** Binding to a one-arg function */
	private static class OneArgBinding extends LibFunction {
		private final LibFunction delegate;

		private OneArgBinding(String name, int opcode, LibFunction delegate) {
			this.name = name;
			this.opcode = opcode;
			this.delegate = delegate;
		}
		
		public String tojstring() {
			return name;
		}
		
		public LuaValue call() {
			return delegate.oncall1(opcode,NIL);
		}
		
		public LuaValue call(LuaValue arg) {
			return delegate.oncall1(opcode,arg);
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			return delegate.oncall1(opcode,arg1);
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
			return delegate.oncall1(opcode,arg1);
		}

		public Varargs invoke(Varargs varargs) {
			return delegate.oncall1(opcode,varargs.arg1());
		}
	} 	
	
	/** Binding to a two-arg function */
	private static class TwoArgBinding extends LibFunction {
		private final LibFunction delegate;

		private TwoArgBinding(String name, int opcode, LibFunction delegate) {
			this.name = name;
			this.opcode = opcode;
			this.delegate = delegate;
		}
		
		public String tojstring() {
			return name;
		}
		
		public LuaValue call() {
			return delegate.oncall2(opcode,NIL,NIL);
		}
		
		public LuaValue call(LuaValue arg) {
			return delegate.oncall2(opcode,arg,NIL);
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			return delegate.oncall2(opcode,arg1,arg2);
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
			return delegate.oncall2(opcode,arg1,arg2);
		}

		public Varargs invoke(Varargs varargs) {
			return delegate.oncall2(opcode,varargs.arg1(),varargs.arg(2));
		}
	} 	
	
	/** Binding to a three-arg function */
	private static class ThreeArgBinding extends LibFunction {
		private final LibFunction delegate;

		private ThreeArgBinding(String name, int opcode, LibFunction delegate) {
			this.name = name;
			this.opcode = opcode;
			this.delegate = delegate;
		}
		
		public String tojstring() {
			return name;
		}
		
		public LuaValue call() {
			return delegate.oncall3(opcode,NIL,NIL,NIL);
		}
		
		public LuaValue call(LuaValue arg) {
			return delegate.oncall3(opcode,arg,NIL,NIL);
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			return delegate.oncall3(opcode,arg1,arg2,NIL);
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
			return delegate.oncall3(opcode,arg1,arg2,arg3);
		}

		public Varargs invoke(Varargs varargs) {
			return delegate.oncall3(opcode,varargs.arg1(),varargs.arg(2),varargs.arg(3));
		}
	} 	
	
	/** Binding to a var-arg function */
	private static class VarArgBinding extends LibFunction {
		private final LibFunction delegate;

		private VarArgBinding(String name, int opcode, LibFunction delegate) {
			this.name = name;
			this.opcode = opcode;
			this.delegate = delegate;
		}

		public String tojstring() {
			return name;
		}
		
		public LuaValue call() {
			return delegate.oncallv(opcode,NONE).arg1();
		}
		
		public LuaValue call(LuaValue arg) {
			return delegate.oncallv(opcode,arg).arg1();
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			return delegate.oncallv(opcode,varargsOf(arg1,arg2)).arg1();
		}

		public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
			return delegate.oncallv(opcode,varargsOf(arg1,arg2,arg3)).arg1();
		}

		public Varargs invoke(Varargs varargs) {
			return delegate.oncallv(opcode,varargs);
		}
	} 	
	
	// -------- code generation helper functions --------
	
	// allocate storage for upvalue, leave it empty
	protected static LuaValue[] newupe() {
		return new LuaValue[1];
	}

	// allocate storage for upvalue, initialize with nil
	protected static LuaValue[] newupn() {
		return new LuaValue[] { NIL };
	}
	
	// allocate storage for upvalue, initialize with value
	protected static LuaValue[] newupl(LuaValue v) {
		return new LuaValue[] { v };
	}
} 
