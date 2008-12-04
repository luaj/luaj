package org.luaj.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.luaj.lib.MathLib;
import org.luaj.lib.j2se.J2seIoLib;
import org.luaj.lib.j2se.LuajavaLib;
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
    
    public String getProperty(String propertyName) {
        return System.getProperty(propertyName);
    }

    protected void installOptionalLibs(LuaState vm) {
        vm.installStandardLibs();
		J2seIoLib.install(vm._G);
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
    
    public LNumber mathPow(LNumber base, LNumber exp) {
        return LDouble.numberOf(Math.pow(base.toJavaDouble(),exp.toJavaDouble()));
    }

	public LNumber mathop(int id, LNumber la, LNumber lb) {
		double a = la.toJavaDouble();
		double b = lb.toJavaDouble();
		double z = 0;
		switch ( id ) {
		default: return unsupportedMathOp();
		case MathLib.ATAN2: z = Math.atan2(a, b); break;
		case MathLib.FMOD: z = a - (b * ((int)(a/b))); break;
		case MathLib.POW: z = Math.pow(a, b); break;
		}
		return LDouble.numberOf(z);
	}
	
	public LNumber mathop(int id, LNumber lx) {
		double x = lx.toJavaDouble();
		double z = 0;
		switch ( id ) {
		default: return unsupportedMathOp();
		case MathLib.ABS: z = Math.abs(x); break;
		case MathLib.ACOS: z = Math.acos(x); break;
		case MathLib.ASIN: z = Math.asin(x); break;
		case MathLib.ATAN: z = Math.atan(x); break;
		case MathLib.COS: z = Math.cos(x); break;
		case MathLib.COSH: z = Math.cosh(x); break;
		case MathLib.DEG: z = Math.toDegrees(x); break;
		case MathLib.EXP: z = Math.exp(x); break;
		case MathLib.LOG: z = Math.log(x); break;
		case MathLib.LOG10: z = Math.log10(x); break;
		case MathLib.RAD: z = Math.toRadians(x); break;
		case MathLib.SIN: z = Math.sin(x); break;
		case MathLib.SINH: z = Math.sinh(x); break;
		case MathLib.SQRT: z = Math.sqrt(x); break;
		case MathLib.TAN: z = Math.tan(x); break;
		case MathLib.TANH: z = Math.tanh(x); break;
		}
		return LDouble.numberOf(z);
	}
}
