/*******************************************************************************
* Copyright (c) 2008-2013 LuaJ. All rights reserved.
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

import java.io.*;
import javax.script.*;

import org.luaj.vm2.*;

/**
 * Implementation of the ScriptEngine interface which can compile and execute
 * scripts using luaj.
 * 
 * <p>
 * This engine requires the types of the Bindings and ScriptContext to be 
 * compatible with the engine.  For creating new client context use
 * ScriptEngine.createContext() which will return {@link LuajContext}, 
 * and for client bindings use the default engine scoped bindings or
 * construct a {@link LuajBindings} directly.
 */
public class LuaScriptEngine implements ScriptEngine, Compilable {
    
	private static final String __ENGINE_VERSION__   = Lua._VERSION;
    private static final String __NAME__             = "Luaj";
    private static final String __SHORT_NAME__       = "Luaj";
    private static final String __LANGUAGE__         = "lua";
    private static final String __LANGUAGE_VERSION__ = "5.2";
    private static final String __ARGV__             = "arg";
    private static final String __FILENAME__         = "?";
    
    private static final ScriptEngineFactory myFactory = new LuaScriptEngineFactory();
    
    private LuajContext context;

    public LuaScriptEngine() {
    	// set up context
    	context = new LuajContext();
    	context.setBindings(createBindings(), ScriptContext.ENGINE_SCOPE);
        setContext(context);
        
        // set special values
        put(LANGUAGE_VERSION, __LANGUAGE_VERSION__);
        put(LANGUAGE, __LANGUAGE__);
        put(ENGINE, __NAME__);
        put(ENGINE_VERSION, __ENGINE_VERSION__);
        put(ARGV, __ARGV__);
        put(FILENAME, __FILENAME__);
        put(NAME, __SHORT_NAME__);
        put("THREADING", null);
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
        return compile(reader).eval();
    }
    
    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
    	return compile(reader).eval(scriptContext);
    }
    
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
    	return compile(reader).eval(bindings);
    }
    
    public void put(String key, Object value) {
		Bindings b = getBindings(ScriptContext.ENGINE_SCOPE);
		b.put(key, value);
    }
    
    public Object get(String key) {
		Bindings b = getBindings(ScriptContext.ENGINE_SCOPE);
		return b.get(key);
    }

    public Bindings getBindings(int scope) {
        return getContext().getBindings(scope);
    }
    
    public void setBindings(Bindings bindings, int scope) {
        getContext().setBindings(bindings, scope);
    }
    
    public Bindings createBindings() {
        return new LuajBindings();
    }
    
    public ScriptContext getContext() {
        return context;
    }
    
    public void setContext(ScriptContext context) {
		if (!(context instanceof LuajContext))
			throw new IllegalArgumentException("LuaScriptEngine can only be used with LuajScriptContext");
    	this.context = (LuajContext) context;
    }
    
    public ScriptEngineFactory getFactory() {
        return myFactory;
    }

	public CompiledScript compile(String script) throws ScriptException {
		return compile(new StringReader(script));
	}
	
	public CompiledScript compile(Reader reader) throws ScriptException {
		try {
	    	InputStream is = new Utf8Encoder(reader);
	    	try {
	    		final Globals g = context.globals;
	    		final LuaFunction f = LoadState.load(is, "script", "bt", g);
	    		return new LuajCompiledScript(f, g);
			} catch ( LuaError lee ) {
				throw new ScriptException(lee.getMessage() );
			} finally { 
				is.close();
			}
		} catch ( Exception e ) {
			throw new ScriptException("eval threw "+e.toString());
		}
	}

	class LuajCompiledScript extends CompiledScript {
		final LuaFunction function;
		final Globals compiling_globals;
		LuajCompiledScript(LuaFunction function, Globals compiling_globals) {
			this.function = function;
			this.compiling_globals = compiling_globals;
		}

		public ScriptEngine getEngine() {
			return LuaScriptEngine.this;
		}

	    public Object eval() throws ScriptException {
	        return eval(getContext());
	    }
	    
	    public Object eval(Bindings bindings) throws ScriptException {
	    	ScriptContext c = getContext();
	        Bindings current = c.getBindings(ScriptContext.ENGINE_SCOPE);
	        try {
		        c.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		        Object result = eval(c);
		        return result;
	        } finally {
		        c.setBindings(current, ScriptContext.ENGINE_SCOPE);
	        }
	    }

	    public Object eval(ScriptContext context) throws ScriptException {
			Globals g = ((LuajContext) context).globals;
			LuaFunction f = function;
			if (g != compiling_globals) {
				if (f.isclosure())
					f = new LuaClosure(f.checkclosure().p, g);
				else {
					try {
						f = f.getClass().newInstance();
					} catch (Exception e) {
						throw new ScriptException(e);
					}
					f.initupvalue1(g);
				}
			}
			return f.invoke(LuaValue.NONE);
		}
	}

	// ------ convert char stream to byte stream for lua compiler ----- 

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
