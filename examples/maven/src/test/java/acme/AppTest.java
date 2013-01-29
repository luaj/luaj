package acme;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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

    public void testAppCanBeExecuted() {
        App.main(new String[0]);
    }
}
