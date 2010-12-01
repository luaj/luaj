

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class ScriptEngineSample {
    
    public static void main(String [] args) {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine e = sem.getEngineByExtension(".lua");
        ScriptEngineFactory f = e.getFactory();
        System.out.println( "Engine name: " +f.getEngineName() );
        System.out.println( "Engine Version: " +f.getEngineVersion() );
        System.out.println( "LanguageName: " +f.getLanguageName() );
        System.out.println( "Language Version: " +f.getLanguageVersion() );
        String statement = f.getOutputStatement("\"hello, world\"");
        System.out.println(statement);
        try {
            e.eval(statement);
            
            e.put("x", 25);
            e.eval("y = math.sqrt(x)");
            System.out.println( "y="+e.get("y") );

            e.put("x", 2);
            e.eval("y = math.sqrt(x)");
            System.out.println( "y="+e.get("y") );
            
            CompiledScript cs = ((Compilable)e).compile("y = math.sqrt(x); return y");
            Bindings b = e.createBindings();
            b.put("x", 3);
            System.out.println( "eval: "+cs.eval(b) );
            System.out.println( "y="+b.get("y") );

            SimpleBindings sb = new SimpleBindings();
            sb.put("x", 144);
            System.out.println( "eval: "+cs.eval(sb) );
            System.out.println( "y="+sb.get("y") );

            try {
            	e.eval("\n\nbogus example\n\n");
            } catch ( ScriptException se ) {
            	System.out.println("script threw ScriptException as expected, message is '"+se.getMessage()+"'");
            }
            
            
        } catch (ScriptException ex) {
            ex.printStackTrace();
        }
    }

}
