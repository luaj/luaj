/*******************************************************************************
* Copyright (c) 2013 LuaJ. All rights reserved.
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

package org.luaj.vm2.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * Implementation of Bindings that may be used with Luaj ScriptEngine implementaiton.
 * 
 * <p>
 * Normally this will not be created directly, but instead will be created fro the script
 * engine using something like:
 * <pre>
 *       ScriptEngineManager manager = new ScriptEngineManager();
 *       ScriptEngine engine = manager.getEngineByExtension(".lua");
 *       Bindings luaj_bindings = engine.createBindings();
 * </pre>
 * 
 * <p>
 * Each instance of LuajBindings holds its own global state.  
 * Calls to get(), put(), and so on will coerce the lua values
 * into Java values on-the-fly, so that any performance or 
 * memory occur at that point.
 * 
 * <p>
 * Only Java String keys may be used for get(), put(), and similar
 * operations, or ClassCastException will be thrown. 
 * 
 * <p>
 * When iterating over the globals, only values which have string keys
 * will be returned.
 */
public class LuajBindings implements Bindings {

	/** Values that are in these bindings.  This table is linked to the metatable
	 * of the context when it is executed via the metatable.
	 */
	public final LuaTable env;

	/** Metatable to be used on bindings that will forards gets and sets into our table of bindings.
	 */
	final LuaTable metatable;
	
	/** Construct an empty LuajBindings.
	 * <p>
	 * Each LuajBindings has its own environment table, which will 
	 * delegate to the context on global reads during execution.
	 */
	public LuajBindings() {
		env = new LuaTable();
		metatable = new LuaTable();
		metatable.set(LuaValue.INDEX, env);
		metatable.set(LuaValue.NEWINDEX, new ThreeArgFunction() {
			public LuaValue call(LuaValue table, LuaValue key, LuaValue value) {
				env.rawset(key, value);
				return LuaValue.NONE;
			}
		});
	}
	
	/** Coerce a value from luaj types to Java types.
	 * @param luajValue any value that derives from LuaValue.
	 * @return If luajValue is: 
	 *  {@link #NIL}, null; 
	 *  {@link #LuaString}: String; 
	 *  {@link #LuaUserdata}: Object; 
	 *  {@link #LuaNumber}: Integer or Double; 
	 *  otherwise: the raw {@link #LuaValue}.
	 */
	public Object toJava(LuaValue luajValue) {
		switch ( luajValue.type() ) {
		case LuaValue.TNIL: return null;
		case LuaValue.TSTRING: return luajValue.tojstring();
		case LuaValue.TUSERDATA: return luajValue.checkuserdata(Object.class);
		case LuaValue.TNUMBER: return luajValue.isinttype()? 
				(Object) new Integer(luajValue.toint()): 
				(Object) new Double(luajValue.todouble());
		default: return luajValue;
		}
	}

	/** Coerce a value from Java types to luaj types.
	 * @param javaValue  Any Java value, including possibly null.
	 * @return LuaValue for this Java Value. If javaValue is 
	 *  null: {@link #NIL}; 
	 *  {@link #LuaValue}: the value; 
	 *  String: {@link #LuaString}; 
	 *  Integer: {@link #LuaInteger}; 
	 *  Double: {@link #LuaDouble}; 
	 *  array<Class>: {@link #LuaTable} containing list of values;
	 *  otherwise  {@link #LuaUserdata} from behavior of {@link CoerceJavaToLua.coerce(Object)}
	 * @see CoerceJavaToLua
	 */
	public LuaValue toLua(Object javaValue) {
		return javaValue == null? LuaValue.NIL:
			javaValue instanceof LuaValue? (LuaValue) javaValue:
			CoerceJavaToLua.coerce(javaValue);
	}

	@Override
	public void clear() {
		for (LuaValue key : env.keys())
			env.rawset(key,  LuaValue.NIL);
	}

	@Override
	public boolean containsValue(Object string_key) {
		return !env.rawget((String)string_key).isnil();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (LuaValue key : env.keys())
			if (key.type() == LuaValue.TSTRING)
				map.put(key.tojstring(), toJava(env.rawget(key)));
		return map.entrySet();
	}

	@Override
	public boolean isEmpty() {
		return keySet().isEmpty();
	}

	@Override
	public Set<String> keySet() {
		Set<String> set = new HashSet<String>();
		for (LuaValue key : env.keys())
			if (key.type() == LuaValue.TSTRING)
				set.add(key.tojstring());
		return set;
	}

	@Override
	public int size() {
		return keySet().size();
	}

	@Override
	public Collection<Object> values() {
		List<Object> values = new ArrayList<Object>();
		for (LuaValue key : env.keys())
			if (key.type() == LuaValue.TSTRING)
				values.add(toJava(env.rawget(key)));
		return values;
	}

	@Override
	public boolean containsKey(Object key) {
		return keySet().contains(key);
	}

	@Override
	public Object get(Object string_key) {
		return toJava(env.rawget((String)string_key));
	}

	@Override
	public Object put(String string_key, Object java_value) {
		Object previous = get(string_key);
		env.rawset((String)string_key, toLua(java_value));
		return previous;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> values) {
		for (java.util.Map.Entry<? extends String, ? extends Object> e : values.entrySet())
			env.rawset(e.getKey(), toLua(e.getValue()));
	}

	@Override
	public Object remove(Object string_key) {
		Object previous = get(string_key);
		env.rawset((String)string_key, LuaValue.NIL);
		return previous;
	}

}
