package org.luaj.platform;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.microedition.midlet.MIDlet;

import org.luaj.vm.LNumber;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

public class J2meMidp10Cldc10Platform extends Platform {
    protected MIDlet midlet;
    
	public String getName() {
		return "j2me";
	}
	
    public J2meMidp10Cldc10Platform(MIDlet midlet) {
        this.midlet = midlet;
    }

    public Reader createReader(InputStream inputStream) {
        return new InputStreamReader(inputStream);
    }

    public InputStream openFile(String fileName) {
        if (!fileName.startsWith("/"))
            fileName = "/" + fileName;
        InputStream is = this.getClass().getResourceAsStream(fileName);
        return is;
    }
    public String getProperty(String key) {
        return midlet.getAppProperty(key);
    }

    protected void installOptionalLibs(LuaState vm) {
        vm.installStandardLibs();
    }

	public LNumber mathPow(LNumber base, LNumber exponent) {
		return unsupportedMathOp();
	}
	
	public LNumber mathop(int id, LNumber x, LNumber y) {
		return unsupportedMathOp();
	}

	public LNumber mathop(int id, LNumber x) {
		return unsupportedMathOp();
	}
}
