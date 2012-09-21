

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

public class ScriptEngineSample {
    
    public static void main(String [] args) {
    	// Set the property 'luaj.debug' before getting the engine to get 
    	// the debug libraries, which will be slower, but may provide stack traces
    	// when luaJC is not used.
    	// System.setProperty("luaj.debug", "true");

        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine e = sem.getEngineByExtension(".lua");
        ScriptEngineFactory f = e.getFactory();

        // uncomment to enable the lua-to-java bytecode compiler 
        // (requires bcel library in class path)
        // org.luaj.vm2.luajc.LuaJC.install();
    	
        System.out.println( "Engine name: " +f.getEngineName() );
        System.out.println( "Engine Version: " +f.getEngineVersion() );
        System.out.println( "LanguageName: " +f.getLanguageName() );
        System.out.println( "Language Version: " +f.getLanguageVersion() );
        String statement = f.getOutputStatement("\"hello, world\"");
        System.out.println(statement);
        try {
        	// Simple evaluations.  Each tiny script is compiled then evaluated.
            e.eval(statement);
            
            e.put("x", 25);
            e.eval("y = math.sqrt(x)");
            System.out.println( "y="+e.get("y") );

            e.put("x", 2);
            e.eval("y = math.sqrt(x)");
            System.out.println( "y="+e.get("y") );

            e.put("f", new OneArgFunction() {
				public LuaValue call(LuaValue arg) {
					System.out.println("arg "+arg.tojstring());
					return LuaValue.valueOf(123);
				}
            });
            System.out.println("eval: "+e.eval("return f('abc')"));

            // Example of compiled script tha can be reused once compiled. 
            CompiledScript cs = ((Compilable)e).compile("y = math.sqrt(x); return y");

            Bindings b1 = e.createBindings();
            Bindings b2 = e.createBindings();
            b1.put("x", 3);
            b2.put("x", 144);
            System.out.println( "eval: "+cs.eval(b1) );
            System.out.println( "eval: "+cs.eval(b2) );
            System.out.println( "y="+b1.get("y") );
            System.out.println( "y="+b2.get("y") );
            
            // Bindings that use a client-specified Bindings type.
            Bindings sb = new SimpleBindings();
            sb.put("x", 2);
            System.out.println( "eval: "+cs.eval(sb) );

            // Example of how exceptions are generated.
            try {
            	e.eval("\n\nbuggy lua code\n\n");
            } catch ( ScriptException se ) {
            	System.out.println("script exception thrown for buggy script, message: '"+se.getMessage()+"'");
            }
            
            testEngineBindings(e);
            testClientBindings(e);
            testUserClasses(e);
            
        } catch (ScriptException ex) {
            ex.printStackTrace();
        }
    }

    public static class SomeUserClass {
    	public String toString() {
    		return "user-class-instance-"+this.hashCode();
    	}
    }
    
    public static void testEngineBindings(ScriptEngine e) throws ScriptException {
    	testBindings(e, e.createBindings());
    }
    public static void testClientBindings(ScriptEngine e) throws ScriptException {
    	testBindings(e, new SimpleBindings());
    }
    public static void testBindings(ScriptEngine e, Bindings b) throws ScriptException {
        CompiledScript cs = ((Compilable)e).compile(
        		"print( 'somejavaint', type(somejavaint), somejavaint )\n" +
        		"print( 'somejavadouble', type(somejavadouble), somejavadouble )\n" +
        		"print( 'somejavastring', type(somejavastring), somejavastring )\n" +
        		"print( 'somejavaobject', type(somejavaobject), somejavaobject )\n" +
        		"print( 'somejavaarray', type(somejavaarray), somejavaarray, somejavaarray[1] )\n" +
        		"someluaint = 444\n" +
        		"someluadouble = 555.666\n" +
        		"someluastring = 'def'\n" +
        		"someluauserdata = somejavaobject\n" +
        		"someluatable = { 999, 111 }\n" +
        		"someluafunction = function(x) print( 'hello, world', x ) return 678 end\n" +
        		"" );
        b.put("somejavaint", 111);
        b.put("somejavadouble", 222.333);
        b.put("somejavastring", "abc");
        b.put("somejavaobject", new SomeUserClass());
        b.put("somejavaarray", new int[] { 777, 888 } );
        System.out.println( "eval: "+cs.eval(b) );
        Object someluaint = b.get("someluaint");
        Object someluadouble = b.get("someluaint");
        Object someluastring = b.get("someluastring");
        Object someluauserdata = b.get("someluauserdata");
        Object someluatable = b.get("someluatable");
        Object someluafunction = b.get("someluafunction");
        System.out.println( "someluaint: "+someluaint.getClass()+" "+someluaint );
        System.out.println( "someluadouble: "+someluadouble.getClass()+" "+someluadouble );
        System.out.println( "someluastring: "+someluastring.getClass()+" "+someluastring );
        System.out.println( "someluauserdata: "+someluauserdata.getClass()+" "+someluauserdata );
        System.out.println( "someluatable: "+someluatable.getClass()+" "+someluatable );
        System.out.println( "someluafunction: "+someluafunction.getClass()+" "+someluafunction );
        System.out.println( "someluafunction(345): "+((LuaValue) someluafunction).call(LuaValue.valueOf(345)) );
    }

    public static void testUserClasses(ScriptEngine e) throws ScriptException {
        CompiledScript cs = ((Compilable)e).compile(
        		"test = test or luajava.newInstance(\"java.lang.String\", \"test\")\n" +
        		"print( 'test', type(test), test, tostring(test) )\n" +
        		"return tostring(test)");
        Bindings b = e.createBindings();
        Object resultstring = cs.eval(b);
        b.put("test", new SomeUserClass());
        Object resultuserclass = cs.eval(b);
        System.out.println( "eval(string): "+resultstring.getClass()+" "+resultstring );
        System.out.println( "eval(userclass): "+resultuserclass.getClass()+" "+resultuserclass );        
    }
    
}
