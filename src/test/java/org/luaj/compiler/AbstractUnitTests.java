package org.luaj.compiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import junit.framework.TestCase;

import org.luaj.TestPlatform;
import org.luaj.debug.Print;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LoadState;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

abstract public class AbstractUnitTests extends TestCase {

    private final String zipfile;
    private final String dir;

    public AbstractUnitTests(String zipfile, String dir) {
        this.zipfile = zipfile;
        this.dir = dir;
    }

    protected void setUp() throws Exception {
        super.setUp();
        Platform.setInstance(new TestPlatform());
    }

    protected void doTest(String file) {
        try {
            // load source from jar
            String path = "jar:file:" + zipfile + "!/" + dir + "/" + file;
            byte[] lua = bytesFromJar(path);

            // compile in memory
            InputStream is = new ByteArrayInputStream(lua);
            LPrototype p = LuaC.compile(is, dir + "/" + file);
            String actual = protoToString(p);

            // load expected value from jar
            byte[] luac = bytesFromJar(path + "c");
            LPrototype e = loadFromBytes(luac, file);
            String expected = protoToString(e);

            // compare results
            assertEquals(expected, actual);

            // dump into memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DumpState.dump(p, baos, false);
            byte[] dumped = baos.toByteArray();

            // re-undump
            LPrototype p2 = loadFromBytes(dumped, file);
            String actual2 = protoToString(p2);

            // compare again
            assertEquals(actual, actual2);

        } catch (IOException e) {
            fail(e.toString());
        }
    }

    protected byte[] bytesFromJar(String path) throws IOException {
        URL url = new URL(path);
        InputStream is = url.openStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int n;
        while ((n = is.read(buffer)) >= 0)
            baos.write(buffer, 0, n);
        is.close();
        return baos.toByteArray();
    }

    protected LPrototype loadFromBytes(byte[] bytes, String script)
            throws IOException {
        LuaState state = Platform.newLuaState();
        InputStream is = new ByteArrayInputStream(bytes);
        return LoadState.undump(state, is, script);
    }

    protected String protoToString(LPrototype p) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        Print.ps = ps;
        new Print().printFunction(p, true);
        return baos.toString();
    }

}
