package org.luaj.vm2.compat;

public class JavaCompat {
    public static final JavaCompat INSTANCE;

    static {
        JavaCompat instance;
        try {
            instance = (JavaCompat) Class.forName("org.luaj.vm2.lib.jse.JavaCompatJSE").newInstance();
        } catch (Throwable t) {
            instance = new JavaCompat();
        }
        INSTANCE = instance;
    }

    public long doubleToRawLongBits(double x) {
        return Double.doubleToLongBits(x);
    }
}
