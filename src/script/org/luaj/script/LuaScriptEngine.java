/*******************************************************************************
* Copyright (c) 2008 LuaJ. All rights reserved.
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
package org.luaj.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.luaj.compiler.LuaC;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LClosure;
import org.luaj.vm.LDouble;
import org.luaj.vm.LFunction;
import org.luaj.vm.LInteger;
import org.luaj.vm.LNil;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LString;
import org.luaj.vm.LTable;
import org.luaj.vm.LUserData;
import org.luaj.vm.LValue;
import org.luaj.vm.LoadState;
import org.luaj.vm.Lua;
import org.luaj.vm.LuaErrorException;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

/**
 * 
 * @author jim_roseborough
 */
public class LuaScriptEngine extends LFunction implements ScriptEngine, Compilable {
    
	private static final String __ENGINE_VERSION__   = "V0.39";
    private static final String __NAME__             = "Luaj";
    private static final String __SHORT_NAME__       = "Luaj";
    private static final String __LANGUAGE__         = "lua";
    private static final String __LANGUAGE_VERSION__ = "5.1";
    private static final String __ARGV__             = "arg";
    private static final String __FILENAME__         = "?";
    
    static {
    	Platform.setInstance(new J2sePlatform());
    	LuaC.install();
    }

    private static final ScriptEngineFactory myFactory = new LuaScriptEngineFactory();
    
    private ScriptContext defaultContext;

    private final LuaState luaState;

    public LuaScriptEngine() {
    	
    	// set up context
        setContext(new SimpleScriptContext());
        
        // set special values
        put(LANGUAGE_VERSION, __LANGUAGE_VERSION__);
        put(LANGUAGE, __LANGUAGE__);
        put(ENGINE, __NAME__);
        put(ENGINE_VERSION, __ENGINE_VERSION__);
        put(ARGV, __ARGV__);
        put(FILENAME, __FILENAME__);
        put(NAME, __SHORT_NAME__);
        put("THREADING", null);

        // create lua state
        luaState = Platform.getInstance().newLuaState();

        // connect up to bindings
        LTable metatable = new LTable();
        metatable.put("__index", this);
        metatable.put("__newindex", this);
        luaState._G.luaSetMetatable(metatable);
    }
    
    
    public Object eval(String script) throws ScriptException {
        return eval(new StringReader(script));
    }
    
    public Object eval(String script, ScriptContext context) throws ScriptException {
    	return eval(new StringReader(script), context);
    }
    
    public Object eval(String script, Bindings bindings) throws ScriptException {
        return eval(new StringReader(script), bindings);
    }
    
    public Object eval(Reader reader) throws ScriptException {
        return eval(reader, getContext());
    }
    
    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
    	return compile(reader).eval(scriptContext);
    }
    
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
    	ScriptContext c = getContext();
        Bindings current = c.getBindings(ScriptContext.ENGINE_SCOPE);
        c.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        Object result = eval(reader);
        c.setBindings(current, ScriptContext.ENGINE_SCOPE);
        return result;
    }
    
    public void put(String key, Object value) {
		Bindings b = getBindings(ScriptContext.ENGINE_SCOPE);
		if ( value == null )
			b.remove(key);
		else {
			if ( value instanceof Number ) {
				double d = ((Number)value).doubleValue();
				int id = (int) d;
				value = (d==id? LInteger.valueOf(id): new LDouble(d));
			} else if ( value instanceof String ) {
				value = LString.valueOf(value.toString());
			} else {
				value = new LUserData(value);
			}
			b.put(key,value);
		}
    }
    
    public Object get(String key) {
		Bindings b = getBindings(ScriptContext.ENGINE_SCOPE);
		Object o = b.get(key);
		if ( o == null )
			return null;
		LValue v = (LValue)o;
		switch ( v.luaGetType() ) {
		case Lua.LUA_TSTRING: return v.toJavaString();
		case Lua.LUA_TUSERDATA: return v.toJavaInstance();
		case Lua.LUA_TNUMBER: return v.isInteger()? (Object) v.toJavaBoxedInteger(): (Object) v.toJavaBoxedDouble();
		}
        return o;
    }
    
	public LValue __index(LuaState vm, LValue table, LValue key) {
		Bindings b = getBindings(ScriptContext.ENGINE_SCOPE);
		String k = key.toJavaString();
		Object v = b.get(k);
		return (LValue) v;
	}

	public void __newindex(LuaState vm, LValue table, LValue key, LValue val) {
		Bindings b = getBindings(ScriptContext.ENGINE_SCOPE);
		String k = key.toJavaString();
		Object v = (val.isNil()? null: val);
        b.put(k,v);
	}

    public Bindings getBindings(int scope) {
        return getContext().getBindings(scope);
    }
    
    public void setBindings(Bindings bindings, int scope) {
        getContext().setBindings(bindings, scope);
    }
    
    public Bindings createBindings() {
        return new SimpleBindings();
    }
    
    public ScriptContext getContext() {
        return defaultContext;
    }
    
    public void setContext(ScriptContext context) {
        defaultContext = context;
    }
    
    public ScriptEngineFactory getFactory() {
        return myFactory;
    }

	public CompiledScript compile(String script) throws ScriptException {
		return compile(new StringReader(script));
	}

	public CompiledScript compile(Reader reader) throws ScriptException {
		try {
	    	InputStream ris = new Utf8Encoder(reader);
	    	LineNumberInputStream is = new LineNumberInputStream( ris);
	    	try {
				final LPrototype p = LoadState.undump(luaState, is, "script");
				return new CompiledScript() {
					public Object eval(ScriptContext context) throws ScriptException {
						LClosure c = p.newClosure( luaState._G );
						luaState.doCall( c, new LValue[0] );
						return luaState.topointer(1);
					}
					public ScriptEngine getEngine() {
						return LuaScriptEngine.this;
					}
				};
			} catch ( LuaErrorException lee ) {
				throw new ScriptException(lee.getMessage(), "script", is.getLineNumber() );
			} finally { 
				is.close();
			}
		} catch ( Throwable t ) {
			throw new ScriptException("eval threw "+t.toString());
		}
	}

    private final class Utf8Encoder extends InputStream {
		private final Reader r;
		private final int[] buf = new int[2];
		private int n;

		private Utf8Encoder(Reader r) {
			this.r = r;
		}

		public int read() throws IOException {
			if ( n > 0 )
				return buf[--n];
			int c = r.read();
			if ( c < 0x80 )
				return c;
			n = 0;
			if ( c < 0x800 ) {
				buf[n++] = (0x80 | ( c      & 0x3f));				
				return     (0xC0 | ((c>>6)  & 0x1f));
			} else {
				buf[n++] = (0x80 | ( c      & 0x3f));				
				buf[n++] = (0x80 | ((c>>6)  & 0x3f));
				return     (0xE0 | ((c>>12) & 0x0f));
			}
		}
	}
}
