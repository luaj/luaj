package org.luaj.debug;

import java.io.PrintWriter;

import junit.framework.TestCase;

import org.luaj.debug.event.DebugEventListener;

public class RedirectOutputStreamTest extends TestCase {
    public void testRedirectOutputStream() {
        RedirectOutputStream redirectOut = new RedirectOutputStream(
                new DebugEventListener(){
                    public void notifyDebugEvent(DebugMessage event) {
                            assertEquals(event.toString(), "outputRedirect: true\r\na");
                    }
            
                });
        PrintWriter writer = new PrintWriter(redirectOut);
        writer.print(true);
        writer.println();
        writer.print('a');
        writer.close();
    }
}
