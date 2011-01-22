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


import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

/** 
 * Subclass of {@link LibFunction} which implements the features of the luajava package. 
 * <p> 
 * Luajava is an approach to mixing lua and java using simple functions that bind 
 * java classes and methods to lua dynamically.  The API is documented on the 
 * <a href="http://www.keplerproject.org/luajava/">luajava</a> documentation pages.
 * <p>
 * Typically, this library is included as part of a call to either 
 * {@link JsePlatform#standardGlobals()}
 * <p>
 * To instantiate and use it directly, 
 * link it into your globals table via {@link LuaValue#load(LuaValue)} using code such as:
 * <pre> {@code
 * LuaTable _G = new LuaTable();
 * LuaThread.setGlobals(_G);
 * LuaC.install();
 * _G.load(new BaseLib());
 * _G.load(new PackageLib());
 * _G.load(new LuajavaLib());
 * _G.get("loadstring").call( LuaValue.valueOf( 
 * 		"sys = luajava.bindClass('java.lang.System')\n"+
 * 		"print ( sys:currentTimeMillis() )\n" ) ).call(); 
 * } </pre>
 * This example is not intended to be realistic - only to show how the {@link LuajavaLib} 
 * may be initialized by hand.  In practice, the {@code luajava} library is available 
 * on all JSE platforms via the call to {@link JsePlatform#standardGlobals()}
 * and the luajava api's are simply invoked from lua.    
 * <p>
 * This has been implemented to match as closely as possible the behavior in the corresponding library in C.
 * @see LibFunction
 * @see JsePlatform
 * @see JmePlatform
 * @see LuaC
 * @see <a href="http://www.keplerproject.org/luajava/manual.html#luareference">http://www.keplerproject.org/luajava/manual.html#luareference</a>
 */
public class LuajavaLib extends VarArgFunction {
	
	static final int INIT           = 0;
	static final int BINDCLASS      = 1;
	static final int NEWINSTANCE	= 2;
	static final int NEW			= 3;
	static final int CREATEPROXY	= 4;
	static final int LOADLIB		= 5;

	static final String[] NAMES = {
		"bindClass", 
		"newInstance", 
		"new", 
		"createProxy", 
		"loadLib",
	};
	
	static final Map classMetatables = new HashMap(); 

	static final int METHOD_MODIFIERS_VARARGS = 0x80;

	static final LuaValue LENGTH = valueOf("length");
	
	static final Map consCache = new HashMap();
	static final Map consIndex = new HashMap();
	static final Map methCache = new HashMap();
	static final Map methIndex = new HashMap();

	public LuajavaLib() {
	}

	public Varargs invoke(Varargs args) {
		try {
			switch ( opcode ) {
			case INIT: {
				LuaTable t = new LuaTable();
				bind( t, LuajavaLib.class, NAMES, BINDCLASS );
				env.set("luajava", t);
				PackageLib.instance.LOADED.set("luajava", t);
				return t;
			}
			case BINDCLASS: {
				final Class clazz = Class.forName(args.checkjstring(1));
				return toUserdata( clazz, clazz );
			}
			case NEWINSTANCE:
			case NEW: {
				// get constructor
				final LuaValue c = args.checkvalue(1); 
				final Class clazz = (opcode==NEWINSTANCE? Class.forName(c.tojstring()): (Class) c.checkuserdata(Class.class));
				final Varargs consargs = args.subargs(2);
				final long paramssig = LuajavaLib.paramsSignatureOf( consargs );
				final Constructor con = resolveConstructor( clazz, paramssig );
				final boolean isvarargs = ((con.getModifiers() & METHOD_MODIFIERS_VARARGS) != 0);
	
				// coerce args, construct instance 
				final Object[] cargs = CoerceLuaToJava.coerceArgs( consargs, con.getParameterTypes(), isvarargs );
				final Object o = con.newInstance( cargs );
					
				// return result
				return toUserdata( o, clazz );
			}
				
			case CREATEPROXY: {				
				final int niface = args.narg()-1;
				if ( niface <= 0 )
					throw new LuaError("no interfaces");
				final LuaValue lobj = args.checktable(niface+1);
				
				// get the interfaces
				final Class[] ifaces = new Class[niface];
				for ( int i=0; i<niface; i++ ) 
					ifaces[i] = Class.forName(args.checkjstring(i+1));
				
				// create the invocation handler
				InvocationHandler handler = new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						String name = method.getName();
						LuaValue func = lobj.get(name);
						if ( func.isnil() )
							return null;
						boolean isvarargs = ((method.getModifiers() & METHOD_MODIFIERS_VARARGS) != 0);
						int n = args!=null? args.length: 0; 
						LuaValue[] v;
						if ( isvarargs ) {								
							Object o = args[--n];
							int m = Array.getLength( o );
							v = new LuaValue[n+m];
							for ( int i=0; i<n; i++ )
								v[i] = CoerceJavaToLua.coerce(args[i]);
							for ( int i=0; i<m; i++ )
								v[i+n] = CoerceJavaToLua.coerce(Array.get(o,i));								
						} else {
							v = new LuaValue[n];
							for ( int i=0; i<n; i++ )
								v[i] = CoerceJavaToLua.coerce(args[i]);
						}
						LuaValue result = func.invoke(v).arg1();
						return CoerceLuaToJava.coerceArg(result, method.getReturnType());
					}
				};
				
				// create the proxy object
				Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), ifaces, handler);
				
				// return the proxy
				return LuaValue.userdataOf( proxy );
			}
			case LOADLIB: {
				// get constructor
				String classname = args.checkjstring(1);
				String methodname = args.checkjstring(2);
				Class clazz = Class.forName(classname);
				Method method = clazz.getMethod(methodname, new Class[] {});
				Object result = method.invoke(clazz, new Object[] {});
				if ( result instanceof LuaValue ) {
					return (LuaValue) result;
				} else {
					return NIL;
				}
			}
			default:
				throw new LuaError("not yet supported: "+this);
			}
		} catch (LuaError e) {
			throw e;
		} catch (InvocationTargetException ite) {
			throw new LuaError(ite.getTargetException());
		} catch (Exception e) {
			throw new LuaError(e);
		}
	}

	// params signature is
	// - low 6-bits are number of parameters
	// - each of next 9 6-bit fields encode a parameter type:
	//     - low 4 bits are lua type
	//     - high 2 bits are number of indexes deep (0,1,2, or 3)
	
	public static long paramsSignatureOf( Varargs args ) {
		long sig = 0;
		int narg = args.narg();
		int n = Math.min( narg, 9 );
		for ( int i=1; i<=n; i++ ) {
			LuaValue a = args.arg(i);
			sig |= (paramTypeOf(a) << (i*6));
		}
		return sig | Math.min( narg, 0x3F );
	}
	
	public static int paramTypeOf( LuaValue arg ) {
		int type = arg.type();
		int tabledepth = 0;
		if ( type == TTABLE ) {
			for ( tabledepth=1; (type=(arg=arg.get(1)).type()) == TTABLE && (tabledepth<3);  )
				++tabledepth;
		}
		if ( type == TNUMBER && arg.isinttype() )
			type = TINT;
		if ( type == TUSERDATA ) {
			Class c = arg.touserdata().getClass();
			for ( ; c.isArray() && (tabledepth<3);  ) {
				c = c.getComponentType();
				++tabledepth;
			}
		}
		return (type & 0xF) | (tabledepth << 4);
	}
	
	public static int paramsCountFromSig( long paramssig ) {
		return ((int) paramssig) & 0x3F;
	}

	public static int paramTypeFromSig(long paramssig, int argindex) {
		return ((int) (paramssig>>(6*(argindex+1)))) & 0x3F;
	}
		
	public static int paramBaseTypeFromParamType(int paramType) {
		int t = paramType & 0xf;
		return t == (TINT & 0xF)? TINT: t;
	}
		
	public static int paramDepthFromParamType(int paramType) {
		return (paramType >> 4) & 0x3;
	}

	public static int paramComponentTypeOfParamType(int paramType) {
		int b = paramBaseTypeFromParamType( paramType );
		int d = paramDepthFromParamType( paramType );
		d = d>0? d-1: 0;
		return (d<<4) | (b&0xF);
	}
		
	static LuaUserdata toUserdata(Object instance, final Class clazz) {
		LuaTable mt = (LuaTable) classMetatables.get(clazz);
		if ( mt == null ) {
			mt = new LuaTable();
			mt.set( LuaValue.INDEX, new TwoArgFunction() {
				private Map methods;
				public LuaValue call(LuaValue table, LuaValue key) {
					Object instance = table.touserdata();
					if ( key.isinttype() ) {
						if ( clazz.isArray() ) {
							int index = key.toint() - 1;
							if ( index >= 0 && index < Array.getLength(instance) )
								return CoerceJavaToLua.coerce( Array.get(instance, index) );
							return NIL;
						}
					}
					final String s = key.tojstring();
					try {
						Field f = clazz.getField(s);
						Object o = f.get(instance);
						return CoerceJavaToLua.coerce( o );
					} catch (NoSuchFieldException nsfe) {
						if ( clazz.isArray() && key.equals(LENGTH) )
							return LuaValue.valueOf( Array.getLength(instance) );
						if ( methods == null )
							methods = new HashMap();
						LMethod m = (LMethod) methods.get(s);
						if ( m == null ) {
							m = new LMethod(clazz,s);
							// not safe - param list needs to 
							// distinguish between more cases
							// methods.put(s, m);
						}
						return m;
					} catch (Exception e) {
						throw new LuaError(e);
					}
				}
			});
			mt.set( LuaValue.NEWINDEX, new ThreeArgFunction() {
				public LuaValue call(LuaValue table, LuaValue key, LuaValue val) {
					Object instance = table.touserdata();
					if ( key.isinttype() ) {
						if ( clazz.isArray() ) {
							Object v = CoerceLuaToJava.coerceArg(val, clazz.getComponentType());
							int index = key.toint() - 1;
							if ( index >= 0 && index < Array.getLength(instance) )
								Array.set(instance, index, v);
							else 
								throw new LuaError("array bounds exceeded "+index);
							return NIL;
						}
					}
					String s = key.tojstring();
					try {
						Field f = clazz.getField(s);
						Object v = CoerceLuaToJava.coerceArg(val, f.getType());
						f.set(table.checkuserdata(Object.class),v);
					} catch (Exception e) {
						throw new LuaError(e);
					}
					return NONE;
				}
			});
			classMetatables.put(clazz, mt);
		}
		return LuaValue.userdataOf(instance,mt);
	}
	
	static final class LMethod extends VarArgFunction {
		private final Class clazz;
		private final String s;
		private LMethod(Class clazz, String s) {
			this.clazz = clazz;
			this.s = s;
		}
		public String tojstring() {
			return clazz.getName()+"."+s+"()";
		}
		public Varargs invoke(Varargs args) {
			try {
				// find the method 
				final Object instance = args.touserdata(1);
				final Varargs methargs = args.subargs(2);
				final long paramssig = LuajavaLib.paramsSignatureOf(methargs);
				final Method meth = resolveMethod( clazz, s, paramssig );
				final boolean isvarargs = ((meth.getModifiers() & METHOD_MODIFIERS_VARARGS) != 0);

				// coerce the arguments
				final Object[] margs = CoerceLuaToJava.coerceArgs( methargs, meth.getParameterTypes(), isvarargs );
				final Object result = meth.invoke( instance, margs );
				
				// coerce the result
				return CoerceJavaToLua.coerce(result);
			} catch (InvocationTargetException ite) {
				throw new LuaError(ite.getTargetException());
			} catch (Exception e) {
				throw new LuaError(e);
			}
		}
	}

	static Constructor resolveConstructor(Class clazz, long paramssig ) {

		// get the cache
		Map cache = (Map) consCache.get( clazz );
		if ( cache == null )
			consCache.put( clazz, cache = new HashMap() );
		
		// look up in the cache
		Constructor c = (Constructor) cache.get( Long.valueOf(paramssig) );
		if ( c != null )
			return c;

		// get index
		Constructor[] cons = (Constructor[]) consIndex.get( clazz );
		if ( cons == null ) {
			consIndex.put( clazz, cons = clazz.getConstructors() );
			if ( cons == null )
				throw new IllegalArgumentException("no public constructors");
		}

		// find constructor with best score
		int bests = Integer.MAX_VALUE;
		int besti = 0;
		for ( int i=0, size=cons.length; i<size; i++ ) {
			Constructor con = cons[i];
			int s = CoerceLuaToJava.scoreParamTypes(paramssig, con.getParameterTypes());
			if ( s < bests ) {
				 bests = s;
				 besti = i;
			}
		}
		
		// put into cache
		c = cons[besti];
		cache.put( Long.valueOf(paramssig), c );
		return c;
	}
	
	static Method resolveMethod(Class clazz, String methodName, long paramssig ) {

		// get the cache
		Map nameCache = (Map) methCache.get( clazz );
		if ( nameCache == null )
			methCache.put( clazz, nameCache = new HashMap() );
		Map cache = (Map) nameCache.get( methodName );
		if ( cache == null )
			nameCache.put( methodName, cache = new HashMap() );
		
		// look up in the cache
		Method m = (Method) cache.get( Long.valueOf(paramssig) );
		if ( m != null )
			return m;

		// get index
		Map index = (Map) methIndex.get( clazz );
		if ( index == null ) {
			methIndex.put( clazz, index = new HashMap() );
			Method[] meths = clazz.getMethods();
			for ( int i=0; i<meths.length; i++ ) {
				Method meth = meths[i];
				String s = meth.getName();
				List list = (List) index.get(s);
				if ( list == null )
					index.put( s, list = new ArrayList() );
				list.add( meth );
			}
		}
		
		// figure out best list of arguments == supplied args
		List list = (List) index.get(methodName);
		if ( list == null )
			throw new IllegalArgumentException("no method named '"+methodName+"'");

		// find method with best score
		int bests = Integer.MAX_VALUE;
		int besti = 0;
		for ( int i=0, size=list.size(); i<size; i++ ) {
			Method meth = (Method) list.get(i);
			int s = CoerceLuaToJava.scoreParamTypes(paramssig, meth.getParameterTypes());
			if ( s < bests ) {
				 bests = s;
				 besti = i;
			}
		}
		
		// put into cache
		m = (Method) list.get(besti);
		cache.put( Long.valueOf(paramssig), m );
		return m;
	}
	
}