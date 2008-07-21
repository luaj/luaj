package org.luaj.platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.microedition.midlet.MIDlet;

import org.luaj.debug.net.j2me.DebugSupportImpl;
import org.luaj.lib.MathLib;
import org.luaj.vm.DebugNetSupport;
import org.luaj.vm.LDouble;
import org.luaj.vm.LNumber;
import org.luaj.vm.LuaErrorException;
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

    public DebugNetSupport getDebugSupport() throws IOException {
        String host = getDebugHost();
        int port = getDebugPort();
        return new DebugSupportImpl(host, port);
    }
    
    public String getProperty(String key) {
        return midlet.getAppProperty(key);
    }

    protected void installOptionalLibs(LuaState vm) {
        vm.installStandardLibs();
    }
    
    public LNumber mathPow(double lhs, double rhs) {
    	return LDouble.valueOf(dpow(lhs,rhs));
    }

	public double mathop(int id, double a, double b) {
		switch ( id ) {
		case MathLib.ATAN2: return a==0? (b>0? Math.PI/2: b>0? -Math.PI/2: 0): Math.atan(b/a);
		case MathLib.FMOD: return a - (b * ((int)(a/b)));
		case MathLib.LDEXP: return a * dpow(2, b);
		case MathLib.POW: return dpow(a, b);
		}
    	throw new LuaErrorException( "unsupported math op" );
	}
	
	public double mathop(int id, double x) {
		switch ( id ) {
		case MathLib.ABS: return Math.abs(x);
		//case MathLib.ACOS: return Math.acos(x);
		//case MathLib.ASIN: return Math.asin(x);
		//case MathLib.ATAN: return Math.atan(x);
		case MathLib.COS: return Math.cos(x);
		case MathLib.COSH: return (Math.exp(x) + Math.exp(-x)) / 2;
		case MathLib.DEG: return Math.toDegrees(x);
		case MathLib.EXP: return Math.exp(x);
		case MathLib.LOG: return Math.log(x);
		case MathLib.LOG10: return Math.log10(x);
		case MathLib.RAD: return Math.toRadians(x);
		case MathLib.SIN: return Math.sin(x);
		case MathLib.SINH: return (Math.exp(x) - Math.exp(-x)) / 2;
		case MathLib.SQRT: return Math.sqrt(x);
		case MathLib.TAN: return Math.tan(x);
		case MathLib.TANH: {
			double e = Math.exp(2*x);
			return (e-1) / (e+1);
		}
		}
		throw new LuaErrorException( "unsupported math op" );
	}
	
	public static double dpow(double a, double b) {
		if ( b < 0 )
			return 1 / dpow( a, -b );
		double p = 1;
		int whole = (int) b;
		for ( double v=a; whole > 0; whole>>=1, v*=v )
			if ( (whole & 1) != 0 )
				p *= v;
		if ( (b -= whole) > 0 ) {
			int frac = (int) (0x10000 * b);
			for ( ; (frac&0xffff)!=0; frac<<=1 ) {
				a = Math.sqrt(a);
				if ( (frac & 0x8000) != 0 )
					p *= a;
			}
		}
		return p;
	}
	
}
