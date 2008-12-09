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
package org.luaj.lib.j2se;


/** LuaJava-like bindings to Java scripting. 
 * 
 * TODO: coerce types on way in and out, pick method base on arg count ant types.
 */
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.luaj.vm.LFunction;
import org.luaj.vm.LNil;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;


public final class LuajavaLib extends LFunction {

	public static void install(LTable globals) {
		LTable luajava = new LTable();
		for ( int i=0; i<NAMES.length; i++ )
			luajava.put( NAMES[i], new LuajavaLib(i) );
		globals.put( "luajava", luajava );
	}

	private static final int INIT			= 0;
	private static final int BINDCLASS		= 1;
	private static final int NEWINSTANCE	= 2;
	private static final int NEW			= 3;
	private static final int CREATEPROXY	= 4;
	private static final int LOADLIB		= 5;
	
	private static final String[] NAMES = {
		"luajava",
		"bindClass", 
		"newInstance", 
		"new", 
		"createProxy", 
		"loadLib" };
	
	private static final Map classMetatables = new HashMap(); 
	
	private int id;

	public LuajavaLib() {		
	}
	
	private LuajavaLib( int id ) {			
		this.id = id;
	}

	public String toString() {
		return "luajava."+NAMES[id];
	}
	
	// perform a lua call
	public boolean luaStackCall(final LuaState vm) {
		String className;
		switch ( id ) {
		case INIT:
			install(vm._G);
			break;
		case BINDCLASS:
			className = vm.tostring(2);
			try {
				Class clazz = Class.forName(className);
				vm.resettop();
				vm.pushlvalue( toUserdata( clazz, clazz ) );
			} catch (Exception e) {
				throw new LuaErrorException(e);
			}
			break;
		case NEWINSTANCE:
		case NEW:
			try {
				// get constructor
				LValue c = vm.topointer(2); 
				Class clazz = (id==NEWINSTANCE? Class.forName(c.toJavaString()): (Class) c.toJavaInstance());
				ParamsList params = new ParamsList( vm );
				Constructor con = resolveConstructor( clazz, params );

				// coerce args 
				Object[] args = CoerceLuaToJava.coerceArgs( params.values, con.getParameterTypes() );
				Object o = con.newInstance( args );
				
				// set the result
				vm.resettop();
				vm.pushlvalue( toUserdata( o, clazz ) );
				
			} catch (Exception e) {
				throw new LuaErrorException(e);
			}
			break;
		case CREATEPROXY:
			final int ninterfaces = Math.max(0,vm.gettop()-2);
			if ( ninterfaces <= 0 )
				throw new LuaErrorException("no interfaces");
			final LValue lobj = vm.totable(-1);
			try {
				// get the interfaces
				final Class[] ifaces = new Class[ninterfaces];
				for ( int i=0; i<ninterfaces; i++ ) 
					ifaces[i] = Class.forName(vm.tostring(i+2));
				
				// create the invocation handler
				InvocationHandler handler = new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						vm.pushlvalue(lobj);
						vm.getfield( -1, LString.valueOf(method.getName()) );
						vm.remove( -2 );
						LValue result;
						if ( !vm.isnil( -1 ) ) {
							int n = ( args != null ) ? args.length : 0;
							for ( int i=0; i<n; i++ )
								vm.pushlvalue( CoerceJavaToLua.coerce(args[i]) );
							vm.call(n, 1);
						}
						result = vm.poplvalue();
						return CoerceLuaToJava.coerceArg(result, method.getReturnType());
					}
				};
				
				// create the proxy object
				Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), ifaces, handler);
				
				// return the proxy
				vm.resettop();
				vm.pushuserdata(proxy);				
				
			} catch (Exception e) {
				throw new LuaErrorException(e);
			}
			break;
		case LOADLIB:
			try {
				// get constructor
				String classname = vm.tostring(2);
				String methodname = vm.tostring(3);
				Class clazz = Class.forName(classname);
				Method method = clazz.getMethod(methodname, new Class[] { LuaState.class });
				Object result = method.invoke(clazz, new Object[] { vm });
				if ( result instanceof Integer ) {
					int nresults = ((Integer)result).intValue();
					int nremove = vm.gettop() - nresults;
					for ( int i=0; i<nremove; i++ )
						vm.remove(1);
				} else {
					vm.resettop();
				}
			} catch (Exception e) {
				throw new LuaErrorException(e);
			}
			break;
		default:
			throw new LuaErrorException("not yet supported: "+this);
		}
		return false;
	}

	public static class ParamsList {
		public final LValue[] values;
		public final Class[] classes;
		public int hash;
		ParamsList( LuaState vm ) {
			int n = Math.max(vm.gettop()-2,0);
			values = new LValue[n];
			classes = new Class[n];
			for ( int i=0; i<n; i++ ) {
				values[i] = vm.topointer(i-n);
				classes[i] = values[i].getClass();
				hash += classes[i].hashCode();
			}
		}
		public int hashCode() {
			return hash;
		}
		public boolean equals( Object o ) {
			return ( o instanceof ParamsList )? 
				Arrays.equals( classes, ((ParamsList) o).classes ):
				false;
		}
	}
		
	static LUserData toUserdata(Object instance, final Class clazz) {
		LTable mt = (LTable) classMetatables.get(clazz);
		if ( mt == null ) {
			mt = new LTable();
			mt.put( LValue.TM_INDEX, new LFunction() {
				public boolean luaStackCall(LuaState vm) {
					LValue table = vm.topointer(2);
					LValue key = vm.topointer(3);
					final String s = key.toJavaString();
					vm.resettop();
					try {
						Field f = clazz.getField(s);
						Object o = f.get(table.toJavaInstance());
						vm.pushlvalue( CoerceJavaToLua.coerce( o ) );
					} catch (NoSuchFieldException nsfe) {
						vm.pushlvalue( new LMethod(clazz,s) );
					} catch (Exception e) {
						throw new LuaErrorException(e);
					}
					return false;
				}
			});
			mt.put( LValue.TM_NEWINDEX, new LFunction() {
				public boolean luaStackCall(LuaState vm) {
					LValue table = vm.topointer(2);
					LValue key = vm.topointer(3);
					LValue val = vm.topointer(4);
					String s = key.toJavaString();
					try {
						Field f = clazz.getField(s);
						Object v = CoerceLuaToJava.coerceArg(val, f.getType());
						f.set(table.toJavaInstance(),v);
						vm.resettop();
					} catch (Exception e) {
						throw new LuaErrorException(e);
					}
					return false;
				}
			});
			classMetatables.put(clazz, mt);
		}
		return new LUserData(instance,mt);
	}
	
	private static final class LMethod extends LFunction {
		private final Class clazz;
		private final String s;
		private LMethod(Class clazz, String s) {
			this.clazz = clazz;
			this.s = s;
		}
		public String toString() {
			return clazz.getName()+"."+s+"()";
		}
		public boolean luaStackCall(LuaState vm) {
			try {
				// find the method 
				Object instance = vm.touserdata(2);
				ParamsList params = new ParamsList( vm );
				Method meth = resolveMethod( clazz, s, params );

				// coerce the arguments
				Object[] args = CoerceLuaToJava.coerceArgs( params.values, meth.getParameterTypes() );
				Object result = meth.invoke( instance, args );
				
				// coerce the result
				vm.resettop();
				vm.pushlvalue( CoerceJavaToLua.coerce(result) );
				return false;
			} catch (Exception e) {
				throw new LuaErrorException(e);
			}
		}
	}

	private static Map consCache =
		new HashMap();
	
	private static Map consIndex =
		new HashMap();
	
	private static Constructor resolveConstructor(Class clazz, ParamsList params ) {

		// get the cache
		Map cache = (Map) consCache.get( clazz );
		if ( cache == null )
			consCache.put( clazz, cache = new HashMap() );
		
		// look up in the cache
		Constructor c = (Constructor) cache.get( params );
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
		Integer n = new Integer( params.classes.length );
		List list = (List) index.get(n);
		if ( list == null )
			throw new IllegalArgumentException("no constructor with "+n+" args");

		// find constructor with best score
		int bests = Integer.MAX_VALUE;
		int besti = 0;
		for ( int i=0, size=list.size(); i<size; i++ ) {
			Constructor con = (Constructor) list.get(i);
			int s = CoerceLuaToJava.scoreParamTypes(params.values, con.getParameterTypes());
			if ( s < bests ) {
				 bests = s;
				 besti = i;
			}
		}
		
		// put into cache
		c = (Constructor) list.get(besti);
		cache.put( params, c );
		return c;
	}

	
	private static Map methCache = 
		new HashMap();
	
	private static Map methIndex = 
		new HashMap();

	private static Method resolveMethod(Class clazz, String methodName, ParamsList params ) {

		// get the cache
		Map nameCache = (Map) methCache.get( clazz );
		if ( nameCache == null )
			methCache.put( clazz, nameCache = new HashMap() );
		Map cache = (Map) nameCache.get( methodName );
		if ( cache == null )
			nameCache.put( methodName, cache = new HashMap() );
		
		// look up in the cache
		Method m = (Method) cache.get( params );
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
		Integer n = new Integer( params.classes.length );
		List list = (List) map.get(n);
		if ( list == null )
			throw new IllegalArgumentException("no method named '"+methodName+"' with "+n+" args");

		// find constructor with best score
		int bests = Integer.MAX_VALUE;
		int besti = 0;
		for ( int i=0, size=list.size(); i<size; i++ ) {
			Method meth = (Method) list.get(i);
			int s = CoerceLuaToJava.scoreParamTypes(params.values, meth.getParameterTypes());
			if ( s < bests ) {
				 bests = s;
				 besti = i;
			}
		}
		
		// put into cache
		m = (Method) list.get(besti);
		cache.put( params, m );
		return m;
	}
	
}