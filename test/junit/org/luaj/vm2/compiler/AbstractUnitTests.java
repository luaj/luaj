package org.luaj.vm2.compiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import junit.framework.TestCase;

import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Print;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.lib.JsePlatform;

abstract public class AbstractUnitTests extends TestCase {

    private final String dir;
    private final String jar;
    private LuaTable _G;

    public AbstractUnitTests(String zipfile, String dir) {
		URL zip = getClass().getResource(zipfile);
		this.jar = "jar:" + zip.toExternalForm()+ "!/";
        this.dir = dir;
    }

    protected void setUp() throws Exception {
        super.setUp();
        _G = JsePlatform.standardGlobals();
        LuaC.install();
    }

    protected void doTest(String file) {
        try {
            // load source from jar
            String path = jar + dir + "/" + file;
            byte[] lua = bytesFromJar(path);

            // compile in memory
            InputStream is = new ByteArrayInputStream(lua);
            Prototype p = LuaC.compile(is, "@" + dir + "/" + file);
            String actual = protoToString(p);

            // load expected value from jar
            byte[] luac = bytesFromJar(path + "c");
            Prototype e = loadFromBytes(luac, file);
            String expected = protoToString(e);

            // compare results
            assertEquals(expected, actual);

            // dump into memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DumpState.dump(p, baos, false);
            byte[] dumped = baos.toByteArray();

            // re-undump
            Prototype p2 = loadFromBytes(dumped, file);
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

    protected Prototype loadFromBytes(byte[] bytes, String script)
            throws IOException {
        InputStream is = new ByteArrayInputStream(bytes);
        return LoadState.compile(is, script);
    }

    protected String protoToString(Prototype p) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        Print.ps = ps;
        new Print().printFunction(p, true);
        return baos.toString();
    }

}
