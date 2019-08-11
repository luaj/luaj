package org.luaj.vm2.lib.jse;

import org.luaj.vm2.compat.JavaCompat;

public class JavaCompatJSE extends JavaCompat {
    public long doubleToRawLongBits(double x) {
        return Double.doubleToRawLongBits(x);
    }
}
