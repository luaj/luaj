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

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;


public class JavaBytecodeGenerator {
	public static boolean DUMPCLASSES = "true".equals(System.getProperty("DUMPCLASSES"));
	
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
		
	private static final Class[] NO_INNER_CLASSES = {};
	
	public static String toLegalJavaName(String string) {
		String better = string.replaceAll("[^\\w]", "_");
		return string.equals(better)? string: "$"+better;
	}
	
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

	private byte[] generateBytecode(Prototype p, String classname, String filename)
			throws IOException {

		// compile our class next
		ClassGen cg = new ClassGen(classname, STR_FUNCV, filename,
				Constants.ACC_PUBLIC | Constants.ACC_SUPER, null);
		ConstantPoolGen cp = cg.getConstantPool(); // cg creates constant pool

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

		// static initializer for static constants
		{
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
					il.append(new PUSH(cp, ki.toString()));
					il.append(factory.createInvoke(STR_LUASTRING, "valueOf",
							TYPE_LUASTRING, new Type[] { Type.STRING },
							Constants.INVOKESTATIC));
					il.append(factory.createPutStatic(classname,
							k[i].getName(), k[i].getType()));
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
		}

		// upvalues are fields
		int nu = p.upvalues.length;
		Field[] u = new Field[nu];
		for (int i = 0; i < nu; i++) {
			String name = getUpvalueName(p.upvalues, i);
			FieldGen fg = new FieldGen(Constants.ACC_PUBLIC, // | ACC_FINAL, //
																// access
					TYPE_LOCALUPVALUE, // type
					name, // name
					cp);
			u[i] = fg.getField();
			cg.addField(u[i]);
		}

		// implement LuaValue.invoke(Varargs args)
		InstructionList il = new InstructionList();
		MethodGen mg = new MethodGen(
				Constants.ACC_PUBLIC | Constants.ACC_FINAL, // access flags
				TYPE_VARARGS, // return type
				new Type[] { TYPE_VARARGS }, // argument types
				new String[] { "args" }, // arg names
				"invoke", STR_LUAVALUE, // method, class
				il, cp);
		InstructionFactory factory = new InstructionFactory(cg);

		// common iterators used for code
		int[] code = p.code;
		int nc = code.length;
		int pc, i, a, b, c;

		// create locals
		int np = p.numparams;
		int nl = p.maxstacksize;
		LocalVariableGen[] locals = new LocalVariableGen[nl];


		// initialize locals
		LocalVariableGen nil = null;
//		LocalVariableGen reg[] = new LocalVariableGen[nl];
//		LocalVariableGen regup[] = new LocalVariableGen[nl];
		boolean isup[] = new boolean[nl];
		markups(p, isup, code, 0, 0);
		
		for (int j = 0; j < nl; j++) {
			  
			String name = j < p.locvars.length && p.locvars[j].varname != null ? 
					toLegalJavaName(p.locvars[j].varname.toString()):
					"r" + j;

			locals[j] = mg.addLocalVariable(name, isup[j] ? TYPE_LOCALUPVALUE
					: TYPE_LUAVALUE, null, null);

			if (isup[j]) { // upvalue storage
				il.append(new PUSH(cp, 1));
				il.append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
				il.append(InstructionConstants.DUP);
				il.append(new ASTORE(locals[j].getIndex()));
				il.append(new PUSH(cp, 0));
				// leave array & index 0 on stack
			}
			// put initial value onto stack
			if (j < np) {
				il.append(new ALOAD(1));
				il.append(new PUSH(cp, j + 1));
				il.append(factory.createInvoke(STR_VARARGS, "arg",
						TYPE_LUAVALUE, new Type[] { Type.INT },
						Constants.INVOKEVIRTUAL));
			} else {
				nil = il_initLocalNil(mg, factory, il, nil);
				il.append(new ALOAD(nil.getIndex()));
			}
			if (isup[j]) { // upvalue local, array is already on stack
				il.append(InstructionConstants.AASTORE);
			} else { // plain local
				il.append(new ASTORE(locals[j].getIndex()));
			}
		}

		// trim varargs to those that are in excess of what is needed
		if ((p.is_vararg & Lua.VARARG_ISVARARG) != 0 && np > 0) {
			il.append(new ALOAD(1));
			il.append(new PUSH(cp, 1 + np));
			il.append(factory.createInvoke(STR_VARARGS, "subargs",
					TYPE_VARARGS, new Type[] { Type.INT },
					Constants.INVOKEVIRTUAL));
			il.append(new ASTORE(1));
		}

		// this local variable stores varargs,
		// and is only created by functions that need it.
		// TODO: only include if necessary
		LocalVariableGen v = null;
		int vbase = 0;
		v = mg.addLocalVariable("v", TYPE_VARARGS, null, null);
		il.append(factory.createFieldAccess(STR_LUAVALUE, "NONE",
				TYPE_LUAVALUE, Constants.GETSTATIC));
		il.append(new ASTORE(v.getIndex()));

		// storage for goto locations
		int[] targets = new int[nc];
		BranchInstruction[] branches = new BranchInstruction[nc];
		InstructionHandle[] ih = new InstructionHandle[nc];

		// each lua bytecode gets some java bytecodes
		for (pc = 0; pc < nc; pc++) {
			  
				// pull out instruction
				i = code[pc];
				a = A(i);
				
				// process the op code
				switch ( OP(i) ) {
				
				case Lua.OP_MOVE:/*	A B	R(A):= R(B)					*/
					ih[pc] = 
					il_append_new_ALOAD(cp,il, (locals[B(i)]));
					il_append_new_ASTORE(cp,il, (locals[a]));
					break;
					
				case Lua.OP_LOADK:/*	A Bx	R(A):= Kst(Bx)					*/
					ih[pc] = 
					il.append(factory.createFieldAccess(classname, k[Bx(i)].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
					il_append_new_ASTORE(cp,il, (locals[a]));
					break;
					
				case Lua.OP_LOADBOOL:/*	A B C	R(A):= (Bool)B: if (C) pc++			*/
					//stack[a] = (B(i)!=0)? LuaValue.TRUE: LuaValue.FALSE;
					ih[pc] = 
					il.append(factory.createFieldAccess(STR_LUAVALUE, ((B(i)!=0)? "TRUE": "FALSE"), TYPE_LUABOOLEAN, Constants.GETSTATIC));
					il_append_new_ASTORE(cp,il, (locals[a]));
	                if (C(i) != 0) {
	                    // pc++; /* skip next instruction (if C) */
	                	branches[pc] = new GOTO(null);
	                	targets[pc] = pc + 2;
	                }
	                break;
	
				case Lua.OP_LOADNIL: /*	A B	R(A):= ...:= R(B):= nil			*/
					ih[pc] = 
					il.append(factory.createFieldAccess(STR_LUAVALUE, "NIL", TYPE_LUAVALUE, Constants.GETSTATIC));
					for ( b=B(i); a<=b; ) {
						il.append(InstructionConstants.DUP);
						il_append_new_ASTORE(cp,il, (locals[a++]));
					}
					il.append(InstructionConstants.POP);
					break;
					
				case Lua.OP_GETUPVAL: /*	A B	R(A):= UpValue[B]				*/
					ih[pc] = 
					il.append(InstructionConstants.THIS);
					il.append(factory.createFieldAccess(classname, u[B(i)].getName(), TYPE_LOCALUPVALUE, Constants.GETFIELD));
					il.append(new PUSH(cp,0));
					il.append(InstructionConstants.AALOAD);
					il_append_new_ASTORE(cp,il, (locals[a]));
	                break;
					
				case Lua.OP_GETGLOBAL: /*	A Bx	R(A):= Gbl[Kst(Bx)]				*/
					ih[pc] = 
					il.append(InstructionConstants.THIS);
					il.append(factory.createFieldAccess(classname, "env", TYPE_LUAVALUE, Constants.GETFIELD));
					il.append(factory.createFieldAccess(classname, k[Bx(i)].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
	                il.append(factory.createInvoke(STR_LUAVALUE, "get", TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
					il_append_new_ASTORE(cp,il, (locals[a]));
					break;
					
				case Lua.OP_GETTABLE: /*	A B C	R(A):= R(B)[RK(C)]				*/
	                // stack[a] = stack[B(i)].get((c=C(i))>0xff? k[c&0x0ff]: stack[c]);
					ih[pc] = 
					il_append_new_ALOAD(cp,il, (locals[B(i)]));
					if ((c=C(i))>0xff)
						il.append(factory.createFieldAccess(classname, k[c&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
					else
						il_append_new_ALOAD(cp,il, (locals[c]));
	                il.append(factory.createInvoke(STR_LUAVALUE, "get", TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
					il_append_new_ASTORE(cp,il, (locals[a]));
					break;
					
				case Lua.OP_SETGLOBAL: /*	A Bx	Gbl[Kst(Bx)]:= R(A)				*/
	                // env.set(k[Bx(i)], stack[a]);
					ih[pc] = 
					il.append(InstructionConstants.THIS);
					il.append(factory.createFieldAccess(classname, "env", TYPE_LUAVALUE, Constants.GETFIELD));
					il.append(factory.createFieldAccess(classname, k[Bx(i)].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
					il_append_new_ALOAD(cp,il, (locals[a]));
	                il.append(factory.createInvoke(STR_LUAVALUE, "set", Type.VOID, new Type[] { TYPE_LUAVALUE, TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
					break;
					
				case Lua.OP_SETUPVAL: /*	A B	UpValue[B]:= R(A)				*/
					// upValues[B(i)].setValue(stack[a]);
					ih[pc] = 
					il.append(InstructionConstants.THIS);
					il.append(factory.createFieldAccess(classname, u[B(i)].getName(), TYPE_LOCALUPVALUE, Constants.GETFIELD));
					il.append(new PUSH(cp,0));
					il_append_new_ALOAD(cp,il, (locals[a]));
					il.append(InstructionConstants.AASTORE);
					break;
					
				case Lua.OP_SETTABLE: /*	A B C	R(A)[RK(B)]:= RK(C)				*/
					// stack[a].set(((b=B(i))>0xff? k[b&0x0ff]: stack[b]), (c=C(i))>0xff? k[c&0x0ff]: stack[c]);
					ih[pc] = 
					il_append_new_ALOAD(cp,il, (locals[a]));
					if ((b=B(i))>0xff)
						il.append(factory.createFieldAccess(classname, k[b&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
					else
						il_append_new_ALOAD(cp,il, (locals[b]));
					if ((c=C(i))>0xff)
						il.append(factory.createFieldAccess(classname, k[c&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
					else
						il_append_new_ALOAD(cp,il, (locals[c]));
	                il.append(factory.createInvoke(STR_LUAVALUE, "set", Type.VOID, new Type[] { TYPE_LUAVALUE, TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
					break;
					
				case Lua.OP_NEWTABLE: /*	A B C	R(A):= {} (size = B,C)				*/
					// stack[a] = new LuaTable(B(i),C(i));
					ih[pc] = 
					il.append(new PUSH(cp, B(i)));
					il.append(new PUSH(cp, C(i)));
	                il.append(factory.createInvoke(STR_LUAVALUE, "tableOf", TYPE_LUATABLE, new Type[] { Type.INT, Type.INT }, Constants.INVOKESTATIC));
					il_append_new_ASTORE(cp,il, (locals[a]));
					break;
					
				case Lua.OP_SELF: /*	A B C	R(A+1):= R(B): R(A):= R(B)[RK(C)]		*/
					// stack[a+1] = (o = stack[B(i)]);
					ih[pc] = 
					il_append_new_ALOAD(cp,il, (locals[B(i)]));
					il_append_new_ASTORE(cp,il, (locals[a+1]));
					// stack[a] = o.get((c=C(i))>0xff? k[c&0x0ff]: stack[c]);
					if ((c=C(i))>0xff)
						il.append(factory.createFieldAccess(classname, k[c&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
					else
						il_append_new_ALOAD(cp,il, (locals[c]));
	                il.append(factory.createInvoke(STR_LUAVALUE, "get", TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
					il_append_new_ASTORE(cp,il, (locals[a]));
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
						il_append_new_ALOAD(cp,il, (locals[b]));
					if ((c=C(i))>0xff)
						il.append(factory.createFieldAccess(classname, k[c&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
					else
						il_append_new_ALOAD(cp,il, (locals[c]));
	                il.append(factory.createInvoke(STR_LUAVALUE, op, TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
					il_append_new_ASTORE(cp,il, (locals[a]));
					break;
				}	
				case Lua.OP_UNM: /*	A B	R(A):= -R(B)					*/
					// stack[a] = stack[B(i)].neg();
					ih[pc] = 
					il_append_new_ALOAD(cp,il, (locals[B(i)]));
	                il.append(factory.createInvoke(STR_LUAVALUE, "neg", TYPE_LUAVALUE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
					il_append_new_ASTORE(cp,il, (locals[a]));
					break;
					
				case Lua.OP_NOT: /*	A B	R(A):= not R(B)				*/
					// stack[a] = stack[B(i)].not();
					ih[pc] = 
					il_append_new_ALOAD(cp,il, (locals[B(i)]));
	                il.append(factory.createInvoke(STR_LUAVALUE, "not", TYPE_LUAVALUE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
					il_append_new_ASTORE(cp,il, (locals[a]));
					break;
					
				case Lua.OP_LEN: /*	A B	R(A):= length of R(B)				*/
					// stack[a] = stack[B(i)].len();
					ih[pc] = 
					il_append_new_ALOAD(cp,il, (locals[B(i)]));
	                il.append(factory.createInvoke(STR_LUAVALUE, "len", TYPE_LUAVALUE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
					il_append_new_ASTORE(cp,il, (locals[a]));
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
							il_append_new_ALOAD(cp,il, (locals[b++]));
			                il.append(factory.createInvoke(STR_LUAVALUE, "checkstring", TYPE_LUASTRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
			                il.append(factory.createInvoke(STR_BUFFER, "append", Type.VOID, new Type[] { TYPE_LUASTRING }, Constants.INVOKEVIRTUAL));
						}
						
						// store
						// stack[a] = sb.tostrvalue();
		                il.append(factory.createInvoke(STR_BUFFER, "tostrvalue", TYPE_LUASTRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
						il_append_new_ASTORE(cp,il, (locals[a]));
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
						il_append_new_ALOAD(cp,il, (locals[b]));
					if ((c=C(i))>0xff)
						il.append(factory.createFieldAccess(classname, k[c&0x0ff].getName(), TYPE_LUAVALUE, Constants.GETSTATIC));
					else
						il_append_new_ALOAD(cp,il, (locals[c]));
	                il.append(factory.createInvoke(STR_LUAVALUE, op, Type.BOOLEAN, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
	                branches[pc] = (a!=0)? new IFEQ(null): new IFNE(null);
					targets[pc] = pc + 2;
					il.append(branches[pc]);
					break;
				}	
				case Lua.OP_TEST: /*	A C	if not (R(A) <=> C) then pc++			*/ 
					ih[pc] = 
					il_append_new_ALOAD(cp,il, (locals[a]));
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
					il_append_new_ALOAD(cp,il, (locals[B(i)]));
	                il.append(factory.createInvoke(STR_LUAVALUE, "toboolean", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	                branches[pc] = (C(i)!=0)? new IFEQ(null): new IFNE(null);
					targets[pc] = pc + 2;
					il.append(branches[pc]);
					il_append_new_ALOAD(cp,il, (locals[B(i)]));
					il_append_new_ASTORE(cp,il, (locals[a]));
					break;
					
				case Lua.OP_CALL: /*	A B C	R(A), ... ,R(A+C-2):= R(A)(R(A+1), ... ,R(A+B-1)) */
				case Lua.OP_TAILCALL: /*	A B C	return R(A)(R(A+1), ... ,R(A+B-1))		*/
					ih[pc] = 
					il_append_new_ALOAD(cp,il, (locals[a]));
					b = B(i);
					c = C(i);
					switch ( b ) {
					case 1: // noargs
						il.append(factory.createFieldAccess(STR_LUAVALUE, "NONE", TYPE_LUAVALUE, Constants.GETSTATIC));
						break;
					case 2: // one arg
						il_append_new_ALOAD(cp,il, (locals[a+1]));
						break;
					case 3: // two args
						il_append_new_ALOAD(cp,il, (locals[a+1]));
						il_append_new_ALOAD(cp,il, (locals[a+2]));
						il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { TYPE_LUAVALUE, TYPE_VARARGS }, Constants.INVOKESTATIC));
						break;
					case 4: // three args
						il_append_new_ALOAD(cp,il, (locals[a+1]));
						il_append_new_ALOAD(cp,il, (locals[a+2]));
						il_append_new_ALOAD(cp,il, (locals[a+3]));
						il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { TYPE_LUAVALUE, TYPE_LUAVALUE, TYPE_VARARGS }, Constants.INVOKESTATIC));
						break;
					default: // fixed arg count
						il.append(new PUSH(cp, b-1));
						il.append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
						for ( int j=0; j<b-1; ++j ) {
							il.append(InstructionConstants.DUP);
							il.append(new PUSH(cp, j));
							il_append_new_ALOAD(cp,il, (locals[a+1+j]));
							il.append(new AASTORE());
						}
						il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { new ArrayType( TYPE_LUAVALUE, 1 ) }, Constants.INVOKESTATIC));
						break;
					case 0: 
						// previous varargs, use any args up to vbase + contents of 'v'
						if ( vbase <= a+1 ) {
							il.append(new ALOAD(v.getIndex()));
						} else {
							il.append(new PUSH(cp, vbase-(a+1)));
							il.append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
							for ( int ai=a+1; ai<vbase; ai++ ) {
								il.append(InstructionConstants.DUP);
								il.append(new PUSH(cp, ai-(a+1)));
								il_append_new_ALOAD(cp,il, (locals[ai]));
								il.append(new AASTORE());
							}
							il.append(new ALOAD(v.getIndex()));
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
							il.append(factory.createFieldAccess(STR_LUAVALUE, "NONE", TYPE_LUAVALUE, Constants.GETSTATIC));
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
						this.il_append_new_ASTORE(cp,il, locals[a]);
						break;
					default: // fixed result count
						for ( int j=1; j<c; j++ ) { 
							il.append(InstructionConstants.DUP);
							il.append(new PUSH(cp, j));
							il.append(factory.createInvoke(STR_VARARGS, "arg", TYPE_LUAVALUE, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
							il_append_new_ASTORE(cp,il, (locals[a+j-1]));
						}
						il.append(InstructionConstants.POP);
						break;
					case 0: // vararg return, remember result base
						if ( v == null ) 
							v = mg.addLocalVariable("v", TYPE_VARARGS, null, null);
						il.append(new ASTORE(v.getIndex()));
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
							il.append(factory.createFieldAccess(STR_LUAVALUE, "NONE", TYPE_LUAVALUE, Constants.GETSTATIC));
							il.append(InstructionConstants.ARETURN);
							break;
						case 2: // one result
							ih[pc] = 
							il_append_new_ALOAD(cp,il, (locals[a]));
							il.append(InstructionConstants.ARETURN);
							break;
						case 3: // two args
							ih[pc] = 
							il_append_new_ALOAD(cp,il, (locals[a]));
							il_append_new_ALOAD(cp,il, (locals[a+1]));
							il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { TYPE_LUAVALUE, TYPE_VARARGS }, Constants.INVOKESTATIC));
							il.append(InstructionConstants.ARETURN);
							break;
						case 4: // three args
							ih[pc] = 
							il_append_new_ALOAD(cp,il, (locals[a]));
							il_append_new_ALOAD(cp,il, (locals[a+1]));
							il_append_new_ALOAD(cp,il, (locals[a+2]));
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
								il_append_new_ALOAD(cp,il, (locals[ai]));
								il.append(new AASTORE());
							}
							il.append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, new Type[] { new ArrayType( TYPE_LUAVALUE, 1 ) }, Constants.INVOKESTATIC));
							il.append(InstructionConstants.ARETURN);
							break;
						case 0: // use previous top
							if ( vbase <= a ) {
								ih[pc] = 
								il.append(new ALOAD(v.getIndex()));
							} else {
								ih[pc] = 
								il.append(new PUSH(cp, vbase-(a)));
								il.append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
								for ( int ai=a; ai<vbase; ai++ ) {
									il.append(InstructionConstants.DUP);
									il.append(new PUSH(cp, ai-a));
									il_append_new_ALOAD(cp,il, (locals[ai]));
									il.append(new AASTORE());
								}
								il.append(new ALOAD(v.getIndex()));
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
					il_append_new_ALOAD(cp,il, (locals[a]));
					il.append(factory.createInvoke(STR_LUAVALUE, "checknumber", TYPE_LUANUMBER, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
					il_append_new_ALOAD(cp,il, (locals[a+2]));
					il.append(factory.createInvoke(STR_LUAVALUE, "checknumber", TYPE_LUANUMBER, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
					il.append(InstructionConstants.DUP);
					il_append_new_ASTORE(cp,il, (locals[a+2]));
					il.append(factory.createInvoke(STR_LUAVALUE, "sub", TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
					il_append_new_ASTORE(cp,il, (locals[a]));
					
					// convert limit to number
					il_append_new_ALOAD(cp,il, (locals[a+1]));
					il.append(factory.createInvoke(STR_LUAVALUE, "checknumber", TYPE_LUANUMBER, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
					il_append_new_ASTORE(cp,il, (locals[a+1]));
					
					// branch to bottom of loop
					branches[pc] = new GOTO(null);
					targets[pc] = pc + 1 + (Bx(i))-0x1ffff;
					il.append(branches[pc]);
					break;
				}
					
				case Lua.OP_FORLOOP: /*	A sBx	R(A)+=R(A+2): if R(A) <?= R(A+1) then { pc+=sBx: R(A+3)=R(A) }*/
				{
					ih[pc] = 
					il_append_new_ALOAD(cp,il, (locals[a]));
					il_append_new_ALOAD(cp,il, (locals[a+2]));
					il.append(factory.createInvoke(STR_LUAVALUE, "add", TYPE_LUAVALUE, new Type[] { TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
					il.append(InstructionConstants.DUP);
					il.append(InstructionConstants.DUP);
					il_append_new_ASTORE(cp,il, (locals[a]));
					il_append_new_ASTORE(cp,il, (locals[a+3]));
					il_append_new_ALOAD(cp,il, (locals[a+1]));
					il_append_new_ALOAD(cp,il, (locals[a+2]));
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
					il_append_new_ALOAD(cp,il, (locals[a]));
					il_append_new_ALOAD(cp,il, (locals[a+1]));
					il_append_new_ALOAD(cp,il, (locals[a+2]));
					il.append(factory.createInvoke(STR_LUAVALUE, "invoke", TYPE_VARARGS, new Type[] { TYPE_LUAVALUE, TYPE_VARARGS }, Constants.INVOKEVIRTUAL));

					// store values
					c=C(i);
					for ( int j=1; j<=c; j++ ) {
						il.append(InstructionConstants.DUP);
						il.append(new PUSH(cp, j));
		                il.append(factory.createInvoke(STR_VARARGS, "arg", TYPE_LUAVALUE, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
						il_append_new_ASTORE(cp,il, (locals[a+2+j]));
					}
					il.append(InstructionConstants.POP);

					// check termination
					il_append_new_ALOAD(cp,il, (locals[a+3]));
					il.append(InstructionConstants.DUP);
					il_append_new_ASTORE(cp,il, (locals[a+2]));
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
						il_append_new_ALOAD(cp,il, (locals[a]));		                
		                if ( (b=B(i)) == 0 ) {
		                	for ( int j=1; a+j<vbase; j++ ) {
								il.append(InstructionConstants.DUP);
								il.append(new PUSH(cp,offset+j));
								il_append_new_ALOAD(cp,il, (locals[a+j]));	                
								il.append(factory.createInvoke(STR_LUAVALUE, "rawset", Type.VOID, new Type[] { Type.INT, TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
		                	}
							il.append(new ALOAD(v.getIndex()));	                
							il.append(factory.createInvoke(STR_LUAVALUE, "rawsetlist", Type.VOID, new Type[] { Type.INT, TYPE_VARARGS }, Constants.INVOKEVIRTUAL));
		                } else {
							il.append(InstructionConstants.DUP);
							il.append(new PUSH(cp,offset+b));
							il.append(factory.createInvoke(STR_LUAVALUE, "presize", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
		                    for (int j=1; j<=b; j++) {
								il.append(InstructionConstants.DUP);
								il.append(new PUSH(cp,offset+j));
								il_append_new_ALOAD(cp,il, (locals[a+j]));	                
								il.append(factory.createInvoke(STR_LUAVALUE, "rawset", Type.VOID, new Type[] { Type.INT, TYPE_LUAVALUE }, Constants.INVOKEVIRTUAL));
		                    }
							il.append(InstructionConstants.POP);	                
		                }
					}
					break;
					
				case Lua.OP_CLOSE: /*	A 	close all variables in the stack up to (>=) R(A)*/
					for ( int j=nl; --j>=a; ) {
						if ( isup[j] ) {
							il.append(new PUSH(cp, 1));
							il.append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
							il.append(new ASTORE(locals[j].getIndex()));
						}
					}
					// markups( p, isup, code, pc+1, a );
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
						il.append(InstructionConstants.THIS);
						il.append(factory.createFieldAccess(classname, "env", TYPE_LUAVALUE, Constants.GETFIELD));
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
								il.append( new ALOAD(locals[b].getIndex()) );
							}
							String destname = getUpvalueName( newp.upvalues, j );
							il.append(factory.createFieldAccess(protoname, destname, TYPE_LOCALUPVALUE, Constants.PUTFIELD));
						}
						il_append_new_ASTORE(cp,il, locals[a]);
					}
					break;
					
				case Lua.OP_VARARG: /*	A B	R(A), R(A+1), ..., R(A+B-1) = vararg		*/
					b = B(i);
					ih[pc] = 
					il.append(new ALOAD(1)); // args
					if ( b == 0 ) {
						if ( v == null ) 
							v = mg.addLocalVariable("v", TYPE_VARARGS, null, null);
						il.append(new ASTORE(v.getIndex()));
						vbase = a;
					} else {
						for ( int j=1; j<b; ++j ) {
							il.append(InstructionConstants.DUP);
							il.append(new PUSH(cp, j));
			                il.append(factory.createInvoke(STR_VARARGS, "arg", TYPE_LUAVALUE, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
							il_append_new_ASTORE(cp,il, (locals[a+j-1]));
						}
						il.append(InstructionConstants.POP);
					}
					break;				
				}
		  }

		// resolve branches
		for (pc = 0; pc < nc; pc++) {
			if (branches[pc] != null) {
				branches[pc].setTarget(ih[targets[pc]]);
			}
		}
		
		// add line numbers
		if ( p.lineinfo != null && p.lineinfo.length >= nc) {
			for ( pc=0; pc<nc; pc++ ) {
				if ( ih[pc] != null )
					mg.addLineNumber( ih[pc], p.lineinfo[pc] );
			}
		}
		
		mg.setMaxStack();
		cg.addMethod(mg.getMethod());
		il.dispose(); // Allow instruction handles to be reused
		cg.addEmptyConstructor(Constants.ACC_PUBLIC);

		// convert to class bytes
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		cg.getJavaClass().dump(baos);
		final byte[] bytes = baos.toByteArray();

		// write the bytes for debugging!
		if (DUMPCLASSES) {
			FileOutputStream fos = new FileOutputStream(classname + ".class");
			fos.write(bytes);
			fos.close();
		}

		return bytes;
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

	private String getUpvalueName(LuaString[] upvalues, int i) {
		return upvalues != null && i < upvalues.length && upvalues[i] != null ? 
				"$" + upvalues[i].toString():
				"u" + i;
	}

	private LocalVariableGen il_initLocalNil(MethodGen mg,
			InstructionFactory factory, InstructionList il, LocalVariableGen nil) {
		if (nil == null) {
			nil = mg.addLocalVariable("nil", TYPE_LUAVALUE, null, null);
			nil.setStart(il.append(factory.createFieldAccess(STR_LUAVALUE,
					"NIL", TYPE_LUAVALUE, Constants.GETSTATIC)));
			il.append(new ASTORE(nil.getIndex()));
		}
		return nil;
	}

	private InstructionHandle il_append_new_ALOAD(ConstantPoolGen cp,
			InstructionList il, LocalVariableGen local) {
		InstructionHandle ih;
		ih = il.append(new ALOAD(local.getIndex()));
		if (local.getType() == TYPE_LOCALUPVALUE) {
			il.append(new PUSH(cp, 0));
			il.append(InstructionConstants.AALOAD);
		}
		return ih;
	}

	private void il_append_new_ASTORE(ConstantPoolGen cp, InstructionList il,
			LocalVariableGen local) {
		if (local.getType() == TYPE_LOCALUPVALUE) {
			il.append(new ALOAD(local.getIndex()));
			il.append(InstructionConstants.SWAP);
			il.append(new PUSH(cp, 0));
			il.append(InstructionConstants.SWAP);
			il.append(InstructionConstants.AASTORE);
		} else {
			il.append(new ASTORE(local.getIndex()));
		}
	}
}
