package org.luaj.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.luaj.debug.net.j2se.DebugSupportImpl;
import org.luaj.lib.j2se.LuajavaLib;
import org.luaj.vm.DebugNetSupport;
import org.luaj.vm.LDouble;
import org.luaj.vm.LNumber;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

public class J2sePlatform extends Platform {
	
	public String getName() {
		return "j2se";
	}
	
    public Reader createReader(InputStream inputStream) {
        return new InputStreamReader(inputStream);
    }    
    
    public DebugNetSupport getDebugSupport() throws IOException {
        DebugNetSupport debugNetSupport = new DebugSupportImpl(getDebugPort());
        return debugNetSupport;
    }

    public String getProperty(String propertyName) {
        return System.getProperty(propertyName);
    }

    protected void installOptionalLibs(LuaState vm) {
        vm.installStandardLibs();
        LuajavaLib.install(vm._G);
    }

    public InputStream openFile(String fileName) {
        File file = new File(fileName);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    
    public LNumber mathPow(double lhs, double rhs) {
        double d = Math.pow(lhs, rhs);
        return LDouble.valueOf(d);
    }
}
