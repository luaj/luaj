package org.luaj.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.luaj.debug.net.j2se.DebugSupportImpl;
import org.luaj.lib.MathLib;
import org.luaj.lib.j2se.LuajavaLib;
import org.luaj.vm.DebugNetSupport;
import org.luaj.vm.LDouble;
import org.luaj.vm.LNumber;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaErrorException;
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

	public double mathop(int id, double a, double b) {
		switch ( id ) {
		case MathLib.ATAN2: return Math.atan2(a, b);
		case MathLib.FMOD: return a - (b * ((int)(a/b)));
		case MathLib.LDEXP: return a * Math.pow(2, b);
		case MathLib.POW: return Math.pow(a, b);
		}
    	throw new LuaErrorException( "unsupported math op" );
	}
	
	public double mathop(int id, double x) {
		switch ( id ) {
		case MathLib.ABS: return Math.abs(x);
		case MathLib.ACOS: return Math.acos(x);
		case MathLib.ASIN: return Math.asin(x);
		case MathLib.ATAN: return Math.atan(x);
		case MathLib.COS: return Math.cos(x);
		case MathLib.COSH: return Math.cosh(x);
		case MathLib.DEG: return Math.toDegrees(x);
		case MathLib.EXP: return Math.exp(x);
		case MathLib.LOG: return Math.log(x);
		case MathLib.LOG10: return Math.log10(x);
		case MathLib.RAD: return Math.toRadians(x);
		case MathLib.SIN: return Math.sin(x);
		case MathLib.SINH: return Math.sinh(x);
		case MathLib.SQRT: return Math.sqrt(x);
		case MathLib.TAN: return Math.tan(x);
		case MathLib.TANH: return Math.tanh(x);
		}
		throw new LuaErrorException( "unsupported math op" );
	}
}
