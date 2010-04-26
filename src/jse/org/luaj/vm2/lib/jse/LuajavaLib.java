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


/** LuaJava-like bindings to Java scripting. 
 * 
 * TODO: coerce types on way in and out, pick method base on arg count ant types.
 */
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
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class LuajavaLib extends OneArgFunction {
	
	private static final int BINDCLASS      = 0;
	private static final int NEWINSTANCE	= 1;
	private static final int NEW			= 2;
	private static final int CREATEPROXY	= 3;
	private static final int LOADLIB		= 4;

	private static final String[] NAMES = {
		"bindClass", 
		"newInstance", 
		"new", 
		"createProxy", 
		"loadLib" };
	
	private static final Map classMetatables = new HashMap(); 

	private static final int METHOD_MODIFIERS_VARARGS = 0x80;

	private static LuaValue LENGTH = valueOf("length");
	
	public static void install(LuaValue globals) {
		globals.set("luajava", new LuajavaLib());
	}
	
	public LuajavaLib() {
	}

	public LuaValue call(LuaValue arg) {
		LuaTable t = new LuaTable();
		bindv( t, NAMES );
		env.set("luajava", t);
		return t;
	}

	protected Varargs oncallv(int opcode, Varargs args) {
		try {
			switch ( opcode ) {
			case BINDCLASS: {
				final Class clazz = Class.forName(args.checkjstring(1));
				return toUserdata( clazz, clazz );
			}
			case NEWINSTANCE:
			case NEW: {
				// get constructor
				final LuaValue c = args.checkvalue(1); 
				final Class clazz = (opcode==NEWINSTANCE? Class.forName(c.tojstring()): (Class) c.checkuserdata(Class.class));
				final long paramssig = LuajavaLib.paramsSignatureOf( args );
				final Constructor con = resolveConstructor( clazz, paramssig );
	
				// coerce args, construct instance 
				Object[] cargs = CoerceLuaToJava.coerceArgs( args, con.getParameterTypes() );
				Object o = con.newInstance( cargs );
					
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
	
	private static final class LMethod extends VarArgFunction {
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
				Object instance = args.touserdata(1);
				Varargs methargs = args.subargs(2);
				long paramssig = LuajavaLib.paramsSignatureOf(methargs);
				Method meth = resolveMethod( clazz, s, paramssig );

				// coerce the arguments
				Object[] margs = CoerceLuaToJava.coerceArgs( methargs, meth.getParameterTypes() );
				Object result = meth.invoke( instance, margs );
				
				// coerce the result
				return CoerceJavaToLua.coerce(result);
			} catch (InvocationTargetException ite) {
				throw new LuaError(ite.getTargetException());
			} catch (Exception e) {
				throw new LuaError(e);
			}
		}
	}

	private static Map consCache =
		new HashMap();
	
	private static Map consIndex =
		new HashMap();
	
	private static Constructor resolveConstructor(Class clazz, long paramssig ) {

		// get the cache
		Map cache = (Map) consCache.get( clazz );
		if ( cache == null )
			consCache.put( clazz, cache = new HashMap() );
		
		// look up in the cache
		Constructor c = (Constructor) cache.get( paramssig );
		if ( c != null )
			return c;

		// get index
		Map index = (Map) consIndex.get( clazz );
		if ( index == null ) {
			consIndex.put( clazz, index = new HashMap() );
			Constructor[] cons = clazz.getConstructors();
			for ( int i=0; i<cons.length; i++ ) {
				Constructor con = cons[i];
				Integer n = new Integer( con.getParameterTypes().length );
				List list = (List) index.get(n);
				if ( list == null )
					index.put( n, list = new ArrayList() );
				list.add( con );
			}
		}
		
		// figure out best list of arguments == supplied args
		Integer n = new Integer( LuajavaLib.paramsCountFromSig(paramssig) );
		List list = (List) index.get(n);
		if ( list == null )
			throw new IllegalArgumentException("no constructor with "+n+" args");

		// find constructor with best score
		int bests = Integer.MAX_VALUE;
		int besti = 0;
		for ( int i=0, size=list.size(); i<size; i++ ) {
			Constructor con = (Constructor) list.get(i);
			int paramType = LuajavaLib.paramTypeFromSig(paramssig, 0);
			int s = CoerceLuaToJava.scoreParamTypes(paramType, con.getParameterTypes());
			if ( s < bests ) {
				 bests = s;
				 besti = i;
			}
		}
		
		// put into cache
		c = (Constructor) list.get(besti);
		cache.put( paramssig, c );
		return c;
	}

	
	private static Map methCache = 
		new HashMap();
	
	private static Map methIndex = 
		new HashMap();

	private static Method resolveMethod(Class clazz, String methodName, long paramssig ) {

		// get the cache
		Map nameCache = (Map) methCache.get( clazz );
		if ( nameCache == null )
			methCache.put( clazz, nameCache = new HashMap() );
		Map cache = (Map) nameCache.get( methodName );
		if ( cache == null )
			nameCache.put( methodName, cache = new HashMap() );
		
		// look up in the cache
		Method m = (Method) cache.get( paramssig );
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
				Integer n = new Integer(meth.getParameterTypes().length);
				Map map = (Map) index.get(s);
				if ( map == null )
					index.put( s, map = new HashMap() );
				List list = (List) map.get(n);
				if ( list == null )
					map.put( n, list = new ArrayList() );
				list.add( meth );
			}
		}
		
		// figure out best list of arguments == supplied args
		Map map = (Map) index.get(methodName);
		if ( map == null )
			throw new IllegalArgumentException("no method named '"+methodName+"'");
		Integer n = new Integer( LuajavaLib.paramsCountFromSig( paramssig ) );
		List list = (List) map.get(n);
		if ( list == null )
			throw new IllegalArgumentException("no method named '"+methodName+"' with "+n+" args");

		// find constructor with best score
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
		cache.put( paramssig, m );
		return m;
	}
	
}