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
package org.luaj.vm2.luajc;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.AASTORE;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.luaj.vm2.Buffer;
import org.luaj.vm2.LocVars;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;


public class JavaBytecodeGenerator {
	public static boolean DUMPCLASSES = "true".equals(System.getProperty("DUMPCLASSES"));

	public static boolean gendebuginfo = true;

	private static final String STR_FUNCV = VarArgFunction.class.getName();
	private static final String STR_VARARGS = Varargs.class.getName();
	private static final String STR_LUAVALUE = LuaValue.class.getName();
	private static final String STR_LUASTRING = LuaString.class.getName();
	private static final String STR_LUAINTEGER = LuaInteger.class.getName();
	private static final String STR_LUADOUBLE = LuaDouble.class.getName();
	private static final String STR_LUANUMBER = LuaNumber.class.getName();
	private static final String STR_LUABOOLEAN = LuaBoolean.class.getName();
	private static final String STR_LUATABLE = LuaTable.class.getName();
	private static final String STR_BUFFER = Buffer.class.getName();
	private static final String STR_STRING = String.class.getName();

	private static final ObjectType TYPE_VARARGS = new ObjectType(STR_VARARGS);
	private static final ObjectType TYPE_LUAVALUE = new ObjectType(STR_LUAVALUE);
	private static final ObjectType TYPE_LUASTRING = new ObjectType(STR_LUASTRING);
	private static final ObjectType TYPE_LUAINTEGER = new ObjectType(STR_LUAINTEGER);
	private static final ObjectType TYPE_LUADOUBLE = new ObjectType(STR_LUADOUBLE);
	private static final ObjectType TYPE_LUANUMBER = new ObjectType(STR_LUANUMBER);
	private static final ObjectType TYPE_LUABOOLEAN = new ObjectType(STR_LUABOOLEAN);
	private static final ObjectType TYPE_LUATABLE = new ObjectType(STR_LUATABLE);
	private static final ObjectType TYPE_BUFFER = new ObjectType(STR_BUFFER);
	
	private static final ArrayType TYPE_LOCALUPVALUE = new ArrayType( TYPE_LUAVALUE, 1 );
	private static final ArrayType TYPE_CHARARRAY = new ArrayType( Type.CHAR, 1 );
	
	private static final Class[] NO_INNER_CLASSES = {};
	
	/**
	 * Turn a lua prototype into a Java class using the Bcel Java Bytecode Generator
	 * @param p Prototype to encode as java, must not have upvalues itself.
	 * @param name  String name to apply, without extensions
	 * @return Java Class that extends LuaValue that can be instatiated via newInstance
	 * @throws Exception
	 */
	public static Class toJavaBytecode( Prototype p, String classname, String filename ) throws Exception {
		return new JavaBytecodeGenerator().loadPrototype( p, classname, filename );
	}

	private final Hashtable prototypes;
	private final ClassLoader classLoader;	
	
	JavaBytecodeGenerator() {
		// load the file
		prototypes = new Hashtable();
		classLoader = new ClassLoader() {
	         public Class findClass(String classname) throws ClassNotFoundException {
	        	 Object o = prototypes.get(classname);
	        	 if ( o instanceof Object[]) {
					try {
						Object[] data = (Object[]) o;
						Prototype p = (Prototype) data[0];
						String filename = (String) data[1];
		        		byte[] b = generateBytecode( p, classname, filename );
		        		prototypes.put(classname, Boolean.TRUE);
		        	 	return defineClass(classname, b, 0, b.length);
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	 }
	        	 return super.findClass(classname);
	         }

	    };
	}
		
	private Class loadPrototype(Prototype p, String classname, String filename) throws ClassNotFoundException {
		indexPrototype( p, classname, filename );
		return classLoader.loadClass(classname);
	}
		
	private void indexPrototype(Prototype p, String classname, String filename) {
		if ( prototypes.containsKey(classname) )
			return;
		prototypes.put(classname, new Object[] { p, filename } );
		Class[] inners = (p.p!=null && p.p.length>0? new Class[p.p.length]: NO_INNER_CLASSES);
		for ( int i=0, n=inners.length; i<n; i++ )  
			indexPrototype( p.p[i], classname+"$"+i, filename );
	}

	private static final int OP(int i) {
		return i & 0x3f;
	}
	private static final int A(int i) {
		return (i >> 6) & 0xff;
	}
	private static final int B(int i) {
		return i >>> 23;
	}
	private static final int Bx(int i) {
		return i >>> 14;
	}
	private static final int C(int i) {
		return (i >> 14) & 0x1ff;
	}	

	byte[] generateBytecode(Prototype p, String classname, String filename)
			throws IOException {
		
		// use fresh context
		byte[] bytes = new Context(p,classname,filename).bytes;
		
		// write the bytes for debugging!
		if (DUMPCLASSES) {
			FileOutputStream fos = new FileOutputStream(classname + ".class");
			fos.write(bytes);
			fos.close();
		}
		
		// return the byte array
		return bytes;
	} 
	
	private static final class Context {
	
		// parameters
		private final Prototype p;
		private final String classname;
		private final String filename;

		// bcel variables
		private final ClassGen cg;
		private final ConstantPoolGen cp;

		// constants in lua file
		private final Field[] k;

		// upvalues are also fields
		private final Field[] u;
		
		// the class bytes - this is the last thing we compute
		private final byte[] bytes; 

		// locals 
		private final int np; // num params
		private final int nl; // num locals
		private final LocalVariableGen[] locals;
		
		// main instruction list for main function
		private final InstructionList init;
		private final InstructionList il;
		private final MethodGen mg;
		private final InstructionFactory factory;

		// variable that holds vararg results
		LocalVariableGen nil = null;
		LocalVariableGen none = null;
		LocalVariableGen env = null;
		LocalVariableGen vret = null;
		int vbase = -1;
		
		// first branch or top of loop
		private final int firstbranch;
		
		// true if needs the "arg" variable
		private final boolean needsarg; 
		
		// locals that are upvalues
		private final boolean isup[];
		private final boolean isinited[];
		
		// current program counter
		private int pc;
		
		// construct a context, and generate class bytes
		private Context(Prototype p, String classname, String filename) throws IOException {
			// parameters
			this.p = p;
			this.classname = classname;
			this.filename = filename;
			
			// compile our class next
			cg = new ClassGen(classname, STR_FUNCV, filename,
					Constants.ACC_PUBLIC | Constants.ACC_SUPER, null);
			cp = cg.getConstantPool(); // cg creates constant pool
	
			// static initializer for static constants
			this.k = createLuaConstantFields();
	
			// upvalues are fields
			this.u = createUpvalueFields();

			// lua slots map to java locals
			np = p.numparams;
			nl = p.maxstacksize;
			locals = new LocalVariableGen[nl];
			
			// implement LuaValue.onInvoke(Varargs args)
			init = new InstructionList();
			il = new InstructionList();
			mg = new MethodGen(
					Constants.ACC_PUBLIC | Constants.ACC_FINAL, // access flags
					TYPE_VARARGS, // return type
					new Type[] { TYPE_VARARGS }, // argument types
					new String[] { "args" }, // arg names
					"onInvoke", STR_LUAVALUE, // method, class
					il, cp);
			factory = new InstructionFactory(cg);
	
			// common iterators used for code
			int[] code = p.code;
			int nc = code.length;
			int i, a, b, c;
	
			// initialize locals
			isup = new boolean[nl];
			isinited = new boolean[nl];
			markups(p, isup, code, 0, 0);

			// find first branch or jump-back-to
			firstbranch = findfirstbranch();

			// initialize upvalud slots			
			needsarg = ((p.is_vararg & Lua.VARARG_NEEDSARG) != 0);
	
			// storage for goto locations
			int[] targets = new int[nc];
			BranchInstruction[] branches = new BranchInstruction[nc];
			InstructionHandle[] ih = new InstructionHandle[nc];
	
			// each lua bytecode gets some java bytecodes
			for (pc = 0; pc < nc; pc++) {

					// make sure locals are initialized
					if ( pc == firstbranch )
						for ( int j=0; j<locals.length; j++ )
							initLocal( j, true );
				
					// pull out instruction
					i = code[pc];
					a = A(i);

					// process the op code
					switch ( OP(i) ) {
					
					case Lua.OP_MOVE:/*	A B	R(A):= R(B)					*/
						ih[pc] = 
						loadLocal(B(i));
						storeLocal(a);
						break;
						
					case Lua.OP_LOADK:/*	A Bx	R(A):= Kst(Bx)					*/
						ih[pc] = 
						il.append(factory.createFieldAccess(classname, k[Bx(i)].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
						storeLocal(a);
						break;
						
					case Lua.OP_LOADBOOL:/*	A B C	R(A):= (Bool)B: if (C) pc++			*/
						//stack[a] = (B(i)!=0)? LuaValue.TRUE: LuaValue.FALSE;
						ih[pc] = 
						il.append(factory.createFieldAccess(STR_LUAVALUE, ((B(i)!=0)? "TRUE": "FALSE"), TYPE_LUABOOLEAN, Constants.GETSTATIC));
						storeLocal(a);
		                if (C(i) != 0) {
		                    // pc++; /* skip next instruction (if C) */
		                	branches[pc] = new GOTO(null);
		                	targets[pc] = pc + 2;
							il.append(branches[pc]);
		                }
		                break;
		
					case Lua.OP_LOADNIL: /*	A B	R(A):= ...:= R(B):= nil			*/
						ih[pc] =
						loadNil(il);
						for ( b=B(i); a<=b; ) {
							il.append(InstructionConstants.DUP);
							storeLocal(a++);
						}
						il.append(InstructionConstants.POP);
						break;
						
					case Lua.OP_GETUPVAL: /*	A B	R(A):= UpValue[B]				*/
						ih[pc] = 
						il.append(InstructionConstants.THIS);
						il.append(factory.createFieldAccess(classname, u[B(i)].getName(), TYPE_LOCALUPVALUE, Constants.GETFIELD));
						il.append(new PUSH(cp,0));
						il.append(InstructionConstants.AALOAD);
						storeLocal(a);
		                break;
						
					case Lua.OP_GETGLOBAL: /*	A Bx	R(A):= Gbl[Kst(Bx)]				*/
						ih[pc] = 
						loadEnv(il);
						il.append(factory.createFieldAccess(classname, k[Bx(i)].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
		                il.append(factory.createInvoke(STR_LUAVALUE, "get", TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
						storeLocal(a);
						break;
						
					case Lua.OP_GETTABLE: /*	A B C	R(A):= R(B)[RK(C)]				*/
		                // stack[a] = stack[B(i)].get((c=C(i))>0xff? k[c&0x0ff]: stack[c]);
						ih[pc] = 
						loadLocal(B(i));
						if ((c=C(i))>0xff)
							il.append(factory.createFieldAccess(classname, k[c&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
						else
							loadLocal(c);
		                il.append(factory.createInvoke(STR_LUAVALUE, "get", TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
						storeLocal(a);
						break;
						
					case Lua.OP_SETGLOBAL: /*	A Bx	Gbl[Kst(Bx)]:= R(A)				*/
		                // env.set(k[Bx(i)], stack[a]);
						ih[pc] = 
						loadEnv(il);
						il.append(factory.createFieldAccess(classname, k[Bx(i)].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
						loadLocal(a);
		                il.append(factory.createInvoke(STR_LUAVALUE, "set", Type.VOID, new Type[] { TYPE_LUAVALUE, TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
						break;
						
					case Lua.OP_SETUPVAL: /*	A B	UpValue[B]:= R(A)				*/
						// upValues[B(i)].setValue(stack[a]);
						ih[pc] = 
						il.append(InstructionConstants.THIS);
						il.append(factory.createFieldAccess(classname, u[B(i)].getName(), TYPE_LOCALUPVALUE, Constants.GETFIELD));
						il.append(new PUSH(cp,0));
						loadLocal(a);
						il.append(InstructionConstants.AASTORE);
						break;
						
					case Lua.OP_SETTABLE: /*	A B C	R(A)[RK(B)]:= RK(C)				*/
						// stack[a].set(((b=B(i))>0xff? k[b&0x0ff]: stack[b]), (c=C(i))>0xff? k[c&0x0ff]: stack[c]);
						ih[pc] = 
						loadLocal(a);
						if ((b=B(i))>0xff)
							il.append(factory.createFieldAccess(classname, k[b&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
						else
							loadLocal(b);
						if ((c=C(i))>0xff)
							il.append(factory.createFieldAccess(classname, k[c&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
						else
							loadLocal(c);
		                il.append(factory.createInvoke(STR_LUAVALUE, "set", Type.VOID, new Type[] { TYPE_LUAVALUE, TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
						break;
						
					case Lua.OP_NEWTABLE: /*	A B C	R(A):= {} (size = B,C)				*/
						// stack[a] = new LuaTable(B(i),C(i));
						ih[pc] = 
						il.append(new PUSH(cp, B(i)));
						il.append(new PUSH(cp, C(i)));
		                il.append(factory.createInvoke(STR_LUAVALUE, "tableOf", TYPE_LUATABLE, new Type[] { Type.INT, Type.INT }, Constants.INVOKESTATIC));
						storeLocal(a);
						break;
						
					case Lua.OP_SELF: /*	A B C	R(A+1):= R(B): R(A):= R(B)[RK(C)]		*/
						// stack[a+1] = (o = stack[B(i)]);
						ih[pc] = 
						loadLocal(B(i));
						il.append(InstructionConstants.DUP);
						storeLocal(a+1);
						// stack[a] = o.get((c=C(i))>0xff? k[c&0x0ff]: stack[c]);
						if ((c=C(i))>0xff)
							il.append(factory.createFieldAccess(classname, k[c&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
						else
							loadLocal(c);
		                il.append(factory.createInvoke(STR_LUAVALUE, "get", TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
						storeLocal(a);
						break;
						
					case Lua.OP_ADD: /*	A B C	R(A):= RK(B) + RK(C)				*/
					case Lua.OP_SUB: /*	A B C	R(A):= RK(B) - RK(C)				*/
					case Lua.OP_MUL: /*	A B C	R(A):= RK(B) * RK(C)				*/
					case Lua.OP_DIV: /*	A B C	R(A):= RK(B) / RK(C)				*/
					case Lua.OP_MOD: /*	A B C	R(A):= RK(B) % RK(C)				*/
					case Lua.OP_POW: /*	A B C	R(A):= RK(B) ^ RK(C)				*/
					{
						String op;
						switch (OP(i)) {
							default: 
							case Lua.OP_ADD: op = "add"; break;
							case Lua.OP_SUB: op = "sub"; break;
							case Lua.OP_MUL: op = "mul"; break;
							case Lua.OP_DIV: op = "div"; break;
							case Lua.OP_MOD: op = "mod"; break;
							case Lua.OP_POW: op = "pow"; break;
						}
						// stack[a] = ((b=B(i))>0xff? k[b&0x0ff]: stack[b]).add((c=C(i))>0xff? k[c&0x0ff]: stack[c]);
						if ((b=B(i))>0xff)
							ih[pc] = 
							il.append(factory.createFieldAccess(classname, k[b&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
						else
							ih[pc] = 
							loadLocal(b);
						if ((c=C(i))>0xff)
							il.append(factory.createFieldAccess(classname, k[c&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
						else
							loadLocal(c);
		                il.append(factory.createInvoke(STR_LUAVALUE, op, TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
						storeLocal(a);
						break;
					}	
					case Lua.OP_UNM: /*	A B	R(A):= -R(B)					*/
						// stack[a] = stack[B(i)].neg();
						ih[pc] = 
						loadLocal(B(i));
		                il.append(factory.createInvoke(STR_LUAVALUE, "neg", TYPE_LUAVALUE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
						storeLocal(a);
						break;
						
					case Lua.OP_NOT: /*	A B	R(A):= not R(B)				*/
						// stack[a] = stack[B(i)].not();
						ih[pc] = 
						loadLocal(B(i));
		                il.append(factory.createInvoke(STR_LUAVALUE, "not", TYPE_LUAVALUE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
						storeLocal(a);
						break;
						
					case Lua.OP_LEN: /*	A B	R(A):= length of R(B)				*/
						// stack[a] = stack[B(i)].len();
						ih[pc] = 
						loadLocal(B(i));
		                il.append(factory.createInvoke(STR_LUAVALUE, "len", TYPE_LUAVALUE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
						storeLocal(a);
						break;
						
					case Lua.OP_CONCAT: /*	A B C	R(A):= R(B).. ... ..R(C)			*/
						{
							b = B(i);
							c = C(i);
							
							// Buffer sb = new Buffer();
							ih[pc] = 
							il.append(factory.createNew(TYPE_BUFFER));
							il.append(InstructionConstants.DUP);
							il.append(factory.createInvoke(STR_BUFFER, "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
							
							// concatenate
							for ( ; b<=c; ) {
								// sb.append( stack[b++].checkstring() );
								il.append(InstructionConstants.DUP);
								loadLocal(b++);
				                il.append(factory.createInvoke(STR_LUAVALUE, "checkstring", TYPE_LUASTRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
				                il.append(factory.createInvoke(STR_BUFFER, "append", Type.VOID, new Type[] { TYPE_LUASTRING }, Constants.INVOKEVIRTUAL));
							}
							
							// store
							// stack[a] = sb.tostrvalue();
			                il.append(factory.createInvoke(STR_BUFFER, "tostrvalue", TYPE_LUASTRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
							storeLocal(a);
						}
						break;
						
					case Lua.OP_JMP: /*	sBx	pc+=sBx					*/
						// pc  += (Bx(i))-0x1ffff;
						branches[pc] = new GOTO(null);
						targets[pc] = pc + 1 + (Bx(i))-0x1ffff;
						ih[pc] = 
						il.append(branches[pc]);
						break;
						
					case Lua.OP_EQ: /*	A B C	if ((RK(B) == RK(C)) ~= A) then pc++		*/
					case Lua.OP_LT: /*	A B C	if ((RK(B) <  RK(C)) ~= A) then pc++  		*/
					case Lua.OP_LE: /*	A B C	if ((RK(B) <= RK(C)) ~= A) then pc++  		*/
					{
						String op;
						switch (OP(i)) {
						default: 
						case Lua.OP_EQ: op = "eq_b"; break;
						case Lua.OP_LT: op = "lt_b"; break;
						case Lua.OP_LE: op = "lteq_b"; break;
						}
						if ((b=B(i))>0xff)
							ih[pc] = 
							il.append(factory.createFieldAccess(classname, k[b&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
						else
							ih[pc] = 
							loadLocal(b);
						if ((c=C(i))>0xff)
							il.append(factory.createFieldAccess(classname, k[c&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
						else
							loadLocal(c);
		                il.append(factory.createInvoke(STR_LUAVALUE, op, Type.BOOLEAN, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
		                branches[pc] = (a!=0)? new IFEQ(null): new IFNE(null);
						targets[pc] = pc + 2;
						il.append(branches[pc]);
						break;
					}	
					case Lua.OP_TEST: /*	A C	if not (R(A) <=> C) then pc++			*/ 
						ih[pc] = 
						loadLocal(a);
		                il.append(factory.createInvoke(STR_LUAVALUE, "toboolean", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		                branches[pc] = (C(i)!=0)? new IFEQ(null): new IFNE(null);
						targets[pc] = pc + 2;
						il.append(branches[pc]);
						break;
						
					case Lua.OP_TESTSET: /*	A B C	if (R(B) <=> C) then R(A):= R(B) else pc++	*/
						/* note: doc appears to be reversed */
						//if ( (o=stack[B(i)]).toboolean() != (C(i)!=0) ) 
						//	++pc;
						//else
						//	stack[a] = o; // TODO: should be sBx? 
						ih[pc] = 
						loadLocal(B(i));
		                il.append(factory.createInvoke(STR_LUAVALUE, "toboolean", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		                branches[pc] = (C(i)!=0)? new IFEQ(null): new IFNE(null);
						targets[pc] = pc + 2;
						il.append(branches[pc]);
						loadLocal(B(i));
						storeLocal(a);
						break;
						
					case Lua.OP_CALL: /*	A B C	R(A), ... ,R(A+C-2):= R(A)(R(A+1), ... ,R(A+B-1)) */
					case Lua.OP_TAILCALL: /*	A B C	return R(A)(R(A+1), ... ,R(A+B-1))		*/
						ih[pc] = 
						loadLocal(a);
						b = B(i);
						c = C(i);
						switch ( b ) {
						case 1: // noargs
							loadNone(il);
							break;
						case 2: // one arg
							loadLocal(a+1);
							break;
						case 3: // two args
							loadLocal(a+1);
							loadLocal(a+2);
							il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { TYPE_LUAVALUE, TYPE_VARARGS }, Constants.INVOKESTATIC));
							break;
						case 4: // three args
							loadLocal(a+1);
							loadLocal(a+2);
							loadLocal(a+3);
							il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { TYPE_LUAVALUE, TYPE_LUAVALUE, TYPE_VARARGS }, Constants.INVOKESTATIC));
							break;
						default: // fixed arg count
							il.append(new PUSH(cp, b-1));
							il.append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
							for ( int j=0; j<b-1; ++j ) {
								il.append(InstructionConstants.DUP);
								il.append(new PUSH(cp, j));
								loadLocal(a+1+j);
								il.append(new AASTORE());
							}
							il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { new ArrayType( TYPE_LUAVALUE, 1 ) }, Constants.INVOKESTATIC));
							break;
						case 0: 
							// previous varargs, use any args up to vbase + contents of 'v'
							if ( vbase <= a+1 ) {
								loadVarreturns(il);
							} else {
								il.append(new PUSH(cp, vbase-(a+1)));
								il.append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
								for ( int ai=a+1; ai<vbase; ai++ ) {
									il.append(InstructionConstants.DUP);
									il.append(new PUSH(cp, ai-(a+1)));
									loadLocal(ai);
									il.append(new AASTORE());
								}
								loadVarreturns(il);
								il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { new ArrayType( TYPE_LUAVALUE, 1 ), TYPE_VARARGS }, Constants.INVOKESTATIC));
							}
							break;
						}
							
						// invoke function
						// v = stack[a].invoke(v);
						il.append(factory.createInvoke(STR_LUAVALUE, "invoke", TYPE_VARARGS, new Type[] { TYPE_VARARGS }, Constants.INVOKEVIRTUAL));
	
						// tail calls return for now
						if ( (OP(i)) == Lua.OP_TAILCALL ) {
							switch ( c ) { // TODO: can this ever be non-varargs? 
							case 1: // no results used
								il.append(InstructionConstants.POP);
								loadNone(il);
								break;
							case 2: // one result returned
								il.append(factory.createInvoke(STR_VARARGS, "arg1", TYPE_LUAVALUE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
								break;
							default: // varargs return, save in varargs
								break;
							}
							il.append(InstructionConstants.ARETURN);
							break;
						}
	
						// process results
						switch ( c ) {
						case 1: // no results used
							il.append(InstructionConstants.POP);
							break;
						case 2: // one result
							il.append(factory.createInvoke(STR_VARARGS, "arg1", TYPE_LUAVALUE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
							this.storeLocal(a);
							break;
						default: // fixed result count
							for ( int j=1; j<c; j++ ) { 
								il.append(InstructionConstants.DUP);
								il.append(new PUSH(cp, j));
								il.append(factory.createInvoke(STR_VARARGS, "arg", TYPE_LUAVALUE, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
								storeLocal(a+j-1);
							}
							il.append(InstructionConstants.POP);
							break;
						case 0: // vararg return, remember result base
							storeVarreturns(il);
							vbase = a;
							break;
						}
						break;
						
					case Lua.OP_RETURN: /*	A B	return R(A), ... ,R(A+B-2)	(see note)	*/
						// closeUpValues();
						b = B(i);
							switch ( b ) {
							case 1: // no results return
								ih[pc] = 
								loadNone(il);
								il.append(InstructionConstants.ARETURN);
								break;
							case 2: // one result
								ih[pc] = 
								loadLocal(a);
								il.append(InstructionConstants.ARETURN);
								break;
							case 3: // two args
								ih[pc] = 
								loadLocal(a);
								loadLocal(a+1);
								il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { TYPE_LUAVALUE, TYPE_VARARGS }, Constants.INVOKESTATIC));
								il.append(InstructionConstants.ARETURN);
								break;
							case 4: // three args
								ih[pc] = 
								loadLocal(a);
								loadLocal(a+1);
								loadLocal(a+2);
								il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { TYPE_LUAVALUE, TYPE_LUAVALUE, TYPE_VARARGS }, Constants.INVOKESTATIC));
								il.append(InstructionConstants.ARETURN);
								break;
							default: // fixed result count
								ih[pc] = 
								il.append(new PUSH(cp, b-1));
								il.append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
								for ( int ai=a; ai<a+b-1; ai++ ) {
									il.append(InstructionConstants.DUP);
									il.append(new PUSH(cp, ai-a));
									loadLocal(ai);
									il.append(new AASTORE());
								}
								il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { new ArrayType( TYPE_LUAVALUE, 1 ) }, Constants.INVOKESTATIC));
								il.append(InstructionConstants.ARETURN);
								break;
							case 0: // use previous top
								if ( vbase <= a ) {
									ih[pc] = 
									loadVarreturns(il);
								} else {
									ih[pc] = 
									il.append(new PUSH(cp, vbase-(a)));
									il.append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
									for ( int ai=a; ai<vbase; ai++ ) {
										il.append(InstructionConstants.DUP);
										il.append(new PUSH(cp, ai-a));
										loadLocal(ai);
										il.append(new AASTORE());
									}
									loadVarreturns(il);
									il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { new ArrayType( TYPE_LUAVALUE, 1 ), TYPE_VARARGS }, Constants.INVOKESTATIC));
								}
								il.append(InstructionConstants.ARETURN);
								break;
							}
							break;
						
					case Lua.OP_FORPREP: /*	A sBx	R(A)-=R(A+2): pc+=sBx				*/
					{
						// convert init and step to numbers, decrement init
						ih[pc] = 
						loadLocal(a);
						il.append(factory.createInvoke(STR_LUAVALUE, "checknumber", TYPE_LUANUMBER, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
						loadLocal(a+2);
						il.append(factory.createInvoke(STR_LUAVALUE, "checknumber", TYPE_LUANUMBER, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
						il.append(InstructionConstants.DUP);
						storeLocal(a+2);
						il.append(factory.createInvoke(STR_LUAVALUE, "sub", TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
						storeLocal(a);
						
						// convert limit to number
						loadLocal(a+1);
						il.append(factory.createInvoke(STR_LUAVALUE, "checknumber", TYPE_LUANUMBER, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
						storeLocal(a+1);
						
						// branch to bottom of loop
						branches[pc] = new GOTO(null);
						targets[pc] = pc + 1 + (Bx(i))-0x1ffff;
						il.append(branches[pc]);
						break;
					}
						
					case Lua.OP_FORLOOP: /*	A sBx	R(A)+=R(A+2): if R(A) <?= R(A+1) then { pc+=sBx: R(A+3)=R(A) }*/
					{
						ih[pc] = 
						loadLocal(a);
						loadLocal(a+2);
						il.append(factory.createInvoke(STR_LUAVALUE, "add", TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
						il.append(InstructionConstants.DUP);
						il.append(InstructionConstants.DUP);
						storeLocal(a);
						storeLocal(a+3);
						loadLocal(a+1);
						loadLocal(a+2);
						il.append(factory.createInvoke(STR_LUAVALUE, "testfor_b", Type.BOOLEAN, new Type[] { TYPE_LUAVALUE, TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
	
						// do test
		                branches[pc] = new IFNE(null);
						targets[pc] = pc + 1 + (Bx(i))-0x1ffff;
						il.append(branches[pc]);
						break;
					}
					
					case Lua.OP_TFORLOOP: /*
										 * A C R(A+3), ... ,R(A+2+C):= R(A)(R(A+1),
										 * R(A+2)): if R(A+3) ~= nil then R(A+2)=R(A+3)
										 * else pc++
										 */
						ih[pc] = 
						loadLocal(a);
						loadLocal(a+1);
						loadLocal(a+2);
						il.append(factory.createInvoke(STR_LUAVALUE, "invoke", TYPE_VARARGS, new Type[] { TYPE_LUAVALUE, TYPE_VARARGS }, Constants.INVOKEVIRTUAL));
	
						// store values
						c=C(i);
						for ( int j=1; j<=c; j++ ) {
							il.append(InstructionConstants.DUP);
							il.append(new PUSH(cp, j));
			                il.append(factory.createInvoke(STR_VARARGS, "arg", TYPE_LUAVALUE, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
							storeLocal(a+2+j);
						}
						il.append(InstructionConstants.POP);
	
						// check termination
						loadLocal(a+3);
						il.append(InstructionConstants.DUP);
						storeLocal(a+2);
		                il.append(factory.createInvoke(STR_LUAVALUE, "isnil", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		                branches[pc] = new IFNE(null);
						targets[pc] = pc + 2;
						il.append(branches[pc]);
						break;
						
					case Lua.OP_SETLIST: /*	A B C	R(A)[(C-1)*FPF+i]:= R(A+i), 1 <= i <= B	*/
						{
							// in this case, next code is not evaluated
			                if ( (c=C(i)) == 0 )
			                    c = code[pc++];
			                int offset = (c-1) * Lua.LFIELDS_PER_FLUSH;
			                //o = stack[a];
							ih[pc] = 
							loadLocal(a);		                
			                if ( (b=B(i)) == 0 ) {
			                	int j=1;
			                	for ( ; a+j<vbase; j++ ) {
									il.append(InstructionConstants.DUP);
									il.append(new PUSH(cp,offset+j));
									loadLocal(a+j);	                
									il.append(factory.createInvoke(STR_LUAVALUE, "rawset", Type.VOID, new Type[] { Type.INT, TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
			                	}
								il.append(new PUSH(cp,offset+j));
								loadVarreturns(il);
								il.append(factory.createInvoke(STR_LUAVALUE, "rawsetlist", Type.VOID, new Type[] { Type.INT, TYPE_VARARGS }, Constants.INVOKEVIRTUAL));
			                } else {
								il.append(InstructionConstants.DUP);
								il.append(new PUSH(cp,offset+b));
								il.append(factory.createInvoke(STR_LUAVALUE, "presize", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
			                    for (int j=1; j<=b; j++) {
									il.append(InstructionConstants.DUP);
									il.append(new PUSH(cp,offset+j));
									loadLocal(a+j);	                
									il.append(factory.createInvoke(STR_LUAVALUE, "rawset", Type.VOID, new Type[] { Type.INT, TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
			                    }
								il.append(InstructionConstants.POP);	                
			                }
						}
						break;
						
					case Lua.OP_CLOSE: /*	A 	close all variables in the stack up to (>=) R(A)*/
						ih[pc] = il.append(InstructionConstants.NOP); // for branching
						for ( int j=nl; --j>=a; ) {
							isinited[j] = true;
							locals[j] = null;
						}
						markups( p, isup, code, pc+1, a );
						break;
						
					case Lua.OP_CLOSURE: /*	A Bx	R(A):= closure(KPROTO[Bx], R(A), ... ,R(A+n))	*/
						{
							b = Bx(i);
							Prototype newp = p.p[b];
							String protoname = classname+"$"+b;
	
							// instantiate the class
							ih[pc] = 
								il.append(factory.createNew(new ObjectType(protoname)));
								il.append(InstructionConstants.DUP);
								il.append(factory.createInvoke(protoname, "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
	
							// set the environment
							il.append(InstructionConstants.DUP);
							loadEnv( il );
							il.append(factory.createInvoke(STR_LUAVALUE, "setfenv", Type.VOID, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
							
							// initialize upvalues of new instance
							for ( int j=0, nup=newp.nups; j<nup; ++j ) {
								i = code[++pc];
								b = B(i);
								il.append(InstructionConstants.DUP);
								//newcl.upValues[j] = (i&4) != 0? 
								//		upValues[b]:
								//		findUpValue(stack,b);	
								if ( (i&4) != 0 ) {
									il.append(InstructionConstants.THIS);
									String srcname = u[b].getName();
									il.append(factory.createFieldAccess(classname, srcname, TYPE_LOCALUPVALUE, Constants.GETFIELD));
								} else {
									this.initLocal(b, true);
									il.append( new ALOAD(locals[b].getIndex()) );
								}
								String destname = getUpvalueName( newp.upvalues, j );
								il.append(factory.createFieldAccess(protoname, destname, TYPE_LOCALUPVALUE, Constants.PUTFIELD));
							}
							storeLocal(a);
						}
						break;
						
					case Lua.OP_VARARG: /*	A B	R(A), R(A+1), ..., R(A+B-1) = vararg		*/
						b = B(i);
						ih[pc] = 
						il.append(new ALOAD(1)); // args
						if ( b == 0 ) {	
							if ( np > 0) {
								il.append(new PUSH(cp, 1 + np));
								il.append(factory.createInvoke(STR_VARARGS, "subargs",
										TYPE_VARARGS, new Type[] { Type.INT },
										Constants.INVOKEVIRTUAL));
							}
							storeVarreturns(il);
							vbase = a;
						} else {
							for ( int j=1; j<b; ++j ) {
								il.append(InstructionConstants.DUP);
								il.append(new PUSH(cp, j+np));
				                il.append(factory.createInvoke(STR_VARARGS, "arg", TYPE_LUAVALUE, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
								storeLocal(a+j-1);
							}
							il.append(InstructionConstants.POP);
						}
						break;				
					}
			  }
	
			// self-check
			
			// resolve branches
			for (pc = 0; pc < nc; pc++) {
				if (branches[pc] != null) {
					if ( ih[targets[pc]] == null )
						 throw new IllegalArgumentException("no target at "+targets[pc]+" op="+OP(code[targets[pc]]));
					branches[pc].setTarget(ih[targets[pc]]);
				}
			}
			
			// add line numbers
			if ( gendebuginfo ) {
				if ( p.lineinfo != null && p.lineinfo.length >= nc) {
					for ( pc=0; pc<nc; pc++ ) {
						if ( ih[pc] != null )
							mg.addLineNumber( ih[pc], p.lineinfo[pc] );
					}
				}
			}
			
			// add initialization at front
			il.insert( il.getStart(), init );
			
			// complete the class
			mg.setMaxStack();
			cg.addMethod(mg.getMethod());
			il.dispose(); // Allow instruction handles to be reused
			cg.addEmptyConstructor(Constants.ACC_PUBLIC);
	
			// convert to class bytes
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			cg.getJavaClass().dump(baos);
			this.bytes = baos.toByteArray();
		}
	
		private int findfirstbranch() {
			int n = p.code.length;
			int firsttest = n;
			for ( int pc=0; pc<n; pc++ ) {
				int i = p.code[pc];
				int o = OP(i);
				if ( Lua.testTMode(o) )
					firsttest = Math.min(firsttest,pc);
				switch ( OP(i) ) {
				case Lua.OP_JMP:
				case Lua.OP_FORLOOP:
				case Lua.OP_FORPREP:
					int target = pc+1+Bx(i)-0x1ffff;
					firsttest = Math.min(firsttest,target);
					break;
				}
			}
			return firsttest;
		}

		private void initNil() {
			if ( nil != null ) return;
			nil = mg.addLocalVariable("$nil", TYPE_VARARGS, null, null);
			init.append(factory.createFieldAccess(STR_LUAVALUE, "NIL", TYPE_LUAVALUE, Constants.GETSTATIC));
			nil.setStart( init.append(new ASTORE(nil.getIndex())) );
		}
		
		private InstructionHandle loadNil(InstructionList list) {
			initNil();
			InstructionHandle h = list.append(new ALOAD(nil.getIndex()));
			nil.setEnd(h);
			return h;
		}
		
		private void initNone() {
			if ( none != null ) return;
			none = mg.addLocalVariable("$none", TYPE_VARARGS, null, null);
			init.append(factory.createFieldAccess(STR_LUAVALUE, "NONE", TYPE_LUAVALUE, Constants.GETSTATIC));
			none.setStart( init.append(new ASTORE(none.getIndex())) );
		}
		
		private InstructionHandle loadNone(InstructionList list) {
			initNone();
			InstructionHandle h = list.append(new ALOAD(none.getIndex()));
			none.setEnd(h);
			return h;
		}
		
		private void initEnv() {
			if ( env != null ) return;
			env = mg.addLocalVariable("$env", TYPE_LUAVALUE, null, null);
			init.append(InstructionConstants.THIS);
			init.append(factory.createFieldAccess(classname, "env", TYPE_LUAVALUE, Constants.GETFIELD));
			env.setStart( init.append(new ASTORE(env.getIndex())) );
		}
		
		private InstructionHandle loadEnv(InstructionList list) {
			initEnv();
			InstructionHandle h = list.append(new ALOAD(env.getIndex()));
			env.setEnd(h);
			return h;
		}

		private void initVarreturns( boolean isload ) {
			if ( vret != null ) return;
			vret = mg.addLocalVariable("$vret", TYPE_VARARGS, null, null);
			vret.setStart( il.getEnd() );
			if ( isload ) {
				initNone();
				init.append(new ALOAD(none.getIndex()));
				init.append(new ASTORE(vret.getIndex()));
			}
		}
		private InstructionHandle loadVarreturns(InstructionList list) {
			initVarreturns( true );
			InstructionHandle h = list.append(new ALOAD(vret.getIndex()));
			vret.setEnd(h);
			return h;
		}
		
		// intialize the vararg result variable
		private void storeVarreturns(InstructionList list) {
			initVarreturns( false );
			InstructionHandle h = list.append(new ASTORE(vret.getIndex()));
			vret.setEnd(h);
		}
		
		private Field[] createLuaConstantFields() {

			// add static constants
			int nk = p.k.length;
			Field[] k = new Field[nk];
			for (int i = 0; i < nk; i++) {
				FieldGen fg = new FieldGen(Constants.ACC_STATIC
						| Constants.ACC_FINAL, // access
						TYPE_LUAVALUE, // type
						"k" + (i + 1), // name
						cp);
				k[i] = fg.getField();
				cg.addField(k[i]);
			}

			// add static initializer method
			InstructionList il = new InstructionList();
			MethodGen mg = new MethodGen(Constants.ACC_STATIC, Type.VOID,
					new Type[] {}, new String[] {}, "<clinit>", cg
							.getClassName(), il, cg.getConstantPool());
			InstructionFactory factory = new InstructionFactory(cg);

			// initialze the constants
			for (int i = 0; i < nk; i++) {
				LuaValue ki = p.k[i];
				switch (ki.type()) {
				case LuaValue.TNIL:
					il.append(factory.createFieldAccess(STR_LUAVALUE, "NIL",
							TYPE_LUAVALUE, Constants.GETSTATIC));
					il.append(factory.createPutStatic(classname,
							k[i].getName(), k[i].getType()));
					break;
				case LuaValue.TBOOLEAN:
					String b = ki.toboolean() ? "TRUE" : "FALSE";
					il.append(factory.createFieldAccess(STR_LUAVALUE, b,
							TYPE_LUABOOLEAN, Constants.GETSTATIC));
					il.append(factory.createPutStatic(classname,
							k[i].getName(), k[i].getType()));
					break;
				case LuaValue.TSTRING:
					LuaString ls = ki.checkstring(); 
					if ( ls.isValidUtf8() ) {
						il.append(new PUSH(cp, ki.toString()));
						il.append(factory.createInvoke(STR_LUASTRING, "valueOf",
								TYPE_LUASTRING, new Type[] { Type.STRING },
								Constants.INVOKESTATIC));
					} else {
						char[] c = new char[ls.m_length];
						for ( int j=0; j<ls.m_length; j++ ) 
							c[j] = (char) (0xff & (int) (ls.m_bytes[ls.m_offset+j]));
						il.append(new PUSH(cp, new String(c)));
						il.append(factory.createInvoke(STR_STRING, "toCharArray",
								TYPE_CHARARRAY, Type.NO_ARGS,
								Constants.INVOKEVIRTUAL));
						il.append(factory.createInvoke(STR_LUASTRING, "valueOf",
								TYPE_LUASTRING, new Type[] { TYPE_CHARARRAY },
								Constants.INVOKESTATIC));
					}
					il.append(factory.createPutStatic(classname, k[i].getName(), k[i].getType()));
					break;
				case LuaValue.TNUMBER:
					if (ki.isinttype()) {
						il.append(new PUSH(cp, ki.toint()));
						il.append(factory
								.createInvoke(STR_LUAINTEGER, "valueOf",
										TYPE_LUAINTEGER,
										new Type[] { Type.INT },
										Constants.INVOKESTATIC));
						il.append(factory.createPutStatic(classname, k[i]
								.getName(), k[i].getType()));
					} else {
						il.append(new PUSH(cp, ki.todouble()));
						il.append(factory.createInvoke(STR_LUADOUBLE,
								"valueOf", TYPE_LUANUMBER,
								new Type[] { Type.DOUBLE },
								Constants.INVOKESTATIC));
						il.append(factory.createPutStatic(classname, k[i]
								.getName(), k[i].getType()));
					}
					break;
				default:
					throw new RuntimeException("illegal constant type: "
							+ ki.type());
				}
			}

			il.append(InstructionConstants.RETURN);
			mg.setMaxStack();
			cg.addMethod(mg.getMethod());
			il.dispose(); // Allow instruction handles to be reused
			
			// return the initialized field
			return k;			
		}

		private Field[] createUpvalueFields() {
			int nu = p.nups;
			Field[] u = new Field[nu];
			LuaString[] upvalues = p.upvalues;
			for (int i = 0; i < nu; i++) {
				String name = getUpvalueName( upvalues, i );
				FieldGen fg = new FieldGen(Constants.ACC_PUBLIC, // access
						TYPE_LOCALUPVALUE, // type
						name, // name
						cp);
				u[i] = fg.getField();
				cg.addField(u[i]);
			}
			return u;
		}

		private String getUpvalueName(LuaString[] upvalues, int i) {
			if ( upvalues == null || i >= upvalues.length || upvalues[i] == null )
				return "u"+i;
			return "$"+upvalues[i].toString();
		}

		// find the upvalues implied by the subsequent instructions
		private void markups(Prototype p, boolean[] isup, int[] code, int startpc, int startregister) {
			int last = isup.length;
			for ( int j=startregister; j<last; j++ )
				isup[j] = false;
			for ( int pc=startpc; pc<code.length; ++pc ) {
				switch ( OP(code[pc]) ) {
				case Lua.OP_CLOSURE:
					int b = Bx(code[pc]);
					Prototype newp = p.p[b];
					for (int j = 0, nup = newp.nups; j < nup; ++j) {
						int i = code[++pc];
						if ((i & 4) == 0) 
							isup[B(i)] = true;
					}
					break;
				case Lua.OP_CLOSE:
					int a = A(code[pc]);
					if ( a < last ) {
						last = a;
						if ( last <= 0 )
							return;
					}
					break;
				}
				
			}
		}

		private void loadInitialValue( int j) {
			if (j < np) {
				init.append(new ALOAD(1));
				init.append(new PUSH(cp, j + 1));
				init.append(factory.createInvoke(STR_VARARGS, "arg", TYPE_LUAVALUE, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
			} else if ( (j == np) && needsarg ) {
				init.append(new ALOAD(1));
				init.append(new PUSH(cp, np+1));
				init.append(factory.createInvoke(STR_LUAVALUE, "tableOf", TYPE_LUATABLE, new Type[] { TYPE_VARARGS, Type.INT }, Constants.INVOKESTATIC));
			} else {
				loadNil(init);
			}
		}
		
		private String getlocalname(LocVars[] locvars, int j) {
			int number = j+1;
			if ( gendebuginfo ) {
				for (int i = 0; i < locvars.length; i++) {
					if (pc < locvars[i].endpc) { /* is variable active? */
						if (--number == 0)
							return locvars[i].varname.toString();
					}
				}
			}
			return "$"+j;
		}
		
		
		public static String toLegalJavaName(String string) {
			String better = string.replaceAll("[^\\w$]", "_");
			return string.equals(better)? string: "$"+better;
		}
		
		private void initLocal(int j, boolean isload) {
			if ( locals[j] != null )
				return;
			
			// create variable
			String name = toLegalJavaName( getlocalname(p.locvars, j) ) + (isup[j]? "$u": "");
			locals[j] = mg.addLocalVariable(name, isup[j] ? TYPE_LOCALUPVALUE : TYPE_LUAVALUE, null, null);

			// upvalue storage
			if ( locals[j].getType() == TYPE_LOCALUPVALUE ) {
				init.append(new PUSH(cp, 1));
				init.append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
				if ( isload && ! isinited[j] ) { 
					init.append(InstructionConstants.DUP);
					init.append(new PUSH(cp, 0));
					loadInitialValue( j );
					init.append(InstructionConstants.AASTORE);
				}
				init.append(new ASTORE(locals[j].getIndex()));
			} else if ( isload && ! isinited[j]  ) {
				loadInitialValue( j );
				init.append(new ASTORE(locals[j].getIndex()));
			}
			isinited[j] = true;
		}
		
		private InstructionHandle loadLocal(int j) {
			InstructionHandle ih;
			initLocal( j, true );
			ih = il.append(new ALOAD(locals[j].getIndex()));
			if (locals[j].getType() == TYPE_LOCALUPVALUE) {
				il.append(new PUSH(cp, 0));
				il.append(InstructionConstants.AALOAD);
			}
			return ih;
		}
	
		private void storeLocal(int j) {
			initLocal( j, false );
			if (locals[j].getType() == TYPE_LOCALUPVALUE) {
				il.append(new ALOAD(locals[j].getIndex()));
				il.append(InstructionConstants.SWAP);
				il.append(new PUSH(cp, 0));
				il.append(InstructionConstants.SWAP);
				il.append(InstructionConstants.AASTORE);
			} else {
				il.append(new ASTORE(locals[j].getIndex()));
			}
		}
	}
}
