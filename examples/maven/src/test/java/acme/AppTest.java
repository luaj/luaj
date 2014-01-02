package acme;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import acme.App;

/**
 * Skeleton unit test for App, required for maven to be used to build the app.
 */
public class AppTest 
    extends TestCase
{
    public AppTest( String testName ) {
        super( testName );
    }

    public static Test suite() {
        return new TestSuite( AppTest.class );
    }

    public void testMainProgramExecution() {
        App.main(new String[0]);
    }
    
    public void testScriptEngineEvaluation() throws ScriptException {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine e = sem.getEngineByExtension(".lua");
        String result = e.eval("return math.pi").toString().substring(0,8);
        assertEquals("3.141592", result);
    }

    public void testDirectEvaluation() {
    	String script = "return math.pow(..., 3)";
		Globals globals = JsePlatform.standardGlobals();
		LuaValue chunk = globals.load(script, "cube");
        int result = chunk.call(LuaValue.valueOf(5)).toint();
        assertEquals(125, result);
    }
}
