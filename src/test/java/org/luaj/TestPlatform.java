package org.luaj;

import java.io.InputStream;

import org.luaj.platform.J2sePlatform;

public class TestPlatform extends J2sePlatform {
    public InputStream openFile(String fileName) {
        return getClass().getResourceAsStream("/" + fileName);
    }
}
