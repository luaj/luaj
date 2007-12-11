package org.luaj.debug.j2se;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.luaj.debug.net.j2se.DebugSupportImpl;
import org.luaj.vm.DebugNetSupport;
import org.luaj.vm.Platform;

public class J2sePlatform extends Platform {
    public Reader createReader(InputStream inputStream) {
        return new InputStreamReader(inputStream);
    }

    public InputStream openFile(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(new File(filePath));
            return fis;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Assumes J2SE platform, return the corresponding system
     * property
     */
    public String getProperty(String propertyName) {
        return System.getProperty(propertyName);
    }

    /**
     * Provides a J2SE DebugSupport instance.
     */
    public DebugNetSupport getDebugSupport() throws IOException {
        int port = getDebugPort();
        DebugSupportImpl debugSupport = new DebugSupportImpl(port);
        return debugSupport;
    }
}
