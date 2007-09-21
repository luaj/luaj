package lua.addon.compile;

import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;
import lua.Print;
import lua.StackState;
import lua.addon.compile.Compiler;
import lua.io.Closure;
import lua.io.Proto;
import lua.value.LValue;

public class SimpleTests extends TestCase {

    private void doTest( String script ) {
		Reader r = new StringReader( script );
		Proto p = Compiler.compile( r, "script" );
		assertNotNull( p );
		Print.printCode( p );
		
		// try running the code!
		StackState state = new StackState();
		Closure c = new Closure( state, p );
		state.doCall( c, new LValue[0] );
	
    }

    public void testTrivial() {
		String s = "print( 2 )\n";
		doTest( s );
	}
	
	public void testAlmostTrivial() {
		String s = "print( 2 )\n" +
				"print( 3 )\n";
		doTest( s );
	}
	
	public void testSimple() {
		String s = "print( 'hello, world' )\n"+
			"for i = 2,4 do\n" +
			"	print( 'i', i )\n" +
			"end\n";
		doTest( s );
	}
	
	public void testBreak() {
		String s = "a=1\n"+
			"while true do\n"+
			"  if a>10 then\n"+
			"     break\n"+
			"  end\n"+
			"  a=a+1\n"+
			"  print( a )\n"+
			"end\n";
		doTest( s );
	}
	
	public void testShebang() {
		String s = "#!../lua\n"+
			"print( 2 )\n";
		doTest( s );
	}
	
	public void testInlineTable() {
		String s = "A = {g=10}\n"+
			"print( A )\n";
		doTest( s );
	}

	public void testEqualsAnd() {
		String s = "print( 1 == b and b )\n";
		doTest( s );
	}
}
