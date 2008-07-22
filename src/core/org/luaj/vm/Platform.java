/*******************************************************************************
 * Copyright (c) 2007 LuaJ. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Singleton to manage platform-specific behaviors. 
 * <p>
 * Here is the sample code to set up the platform instance and create a new
 * LuaState instance.
 * <pre>
 *    Platform.setInstance(new J2meMidp20Cldc11Platform());
 *    LuaState vm = Platform.newLuaState();
 * </pre>
 */
abstract public class Platform {

    public static String DEBUG_CLASS_NAME = "org.luaj.debug.DebugLuaState";
    
    public static final String PROPERTY_LUAJ_DEBUG = "Luaj-Debug"; 
    public static final String PROPERTY_LUAJ_DEBUG_SUSPEND_AT_START = "Luaj-Debug-SuspendAtStart";
    public static final String PROPERTY_LUAJ_DEBUG_HOST = "Luaj-Debug-Host";
    public static final String PROPERTY_LUAJ_DEBUG_PORT = "Luaj-Debug-Port";
    public static final String PROPERTY_LUAJ_DEBUG_CLASS = "Luaj-Debug-Class"; 
    
    private static Platform instance;

    
    /**
     * Singleton to be used for platform operations.
     * 
     * The default Platform gets files as resources, and converts them to
     * characters using the default InputStreamReader class.
     */
    public static Platform getInstance() {
        if (instance == null) {
            throw new RuntimeException("Platform instance is null. Use Platform.setInstance(Platform p) to set the instance first.");
        }
        
        return instance;
    }

    /**
     * Set the Platform instance.
     * 
     * This may be useful to define a file search path, or custom character
     * encoding conversion properties.
     */
    public static void setInstance(Platform platform) {
        instance = platform;
    }

    /**
     * Creates a new instance of LuaState. If debug properties are present,
     * DebugLuaState (a LuaState with debugging capabilities) will be created.
     * 
     * @return a new instance of LuaState
     */
    public static LuaState newLuaState() {
        Platform p = Platform.getInstance();
        String isDebugStr = p.getProperty(PROPERTY_LUAJ_DEBUG);
        boolean isDebug = (isDebugStr != null && "true".equalsIgnoreCase(isDebugStr));

        LuaState vm = null;
        if (isDebug) {
            try {
            	String c = p.getProperty(PROPERTY_LUAJ_DEBUG_CLASS);
                vm = (LuaState) Class.forName(c!=null? c: DEBUG_CLASS_NAME).newInstance();
            } catch (Exception e) {
                System.out.println("Warning: no debug support, " + e);
            }
        }

        if (vm == null)
            vm = new LuaState();

        vm.init();
        p.installOptionalLibs(vm);
        
        return vm;
    }

    /** Get the name of the platform 
     */
    abstract public String getName();
    
    /**
     * Return an InputStream or null if not found for a particular file name.
     * 
     * @param fileName
     *                Name of the file to open
     * @return InputStream or null if not found.
     */
    abstract public InputStream openFile(String fileName);

    /**
     * Create Reader from an InputStream
     * 
     * @param inputStream
     *                InputStream to read from
     * @return Reader instance to use for character input
     */
    abstract public Reader createReader(InputStream inputStream);

    /**
     * Returns the value for the given platform property.
     * 
     * @param propertyName
     *                Property name
     * @return Property value
     */
    abstract public String getProperty(String propertyName);

    /**
     * Returns an platform dependent DebugSupport instance.
     * 
     * @return an platform dependent DebugSupport instance.
     */
    abstract public DebugNetSupport getDebugSupport() throws IOException;

    /**
     * Install optional libraries on the LuaState.
     * @param vm LuaState instance
     */
    abstract protected void installOptionalLibs(LuaState vm);
    
    /**
     * Convenience method for the subclasses to figure out the debug host.
     * @return the debug host property. If it is not present, null is returned.
     */
    protected String getDebugHost() {
        String host = getProperty(PROPERTY_LUAJ_DEBUG_HOST);
        return host;
    }

    /**
     * Convenience method for the subclasses to figure out the debug port.
     * @return -1 if the port is not found in the platform properties; the port
     * as an integer if it is present in the platform properties and valid. 
     */
    protected int getDebugPort() {
        String portStr = getProperty(PROPERTY_LUAJ_DEBUG_PORT);
        int port = -1;
        if (portStr != null) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {}
        }
        return port;
    }

    /** 
     * Compute math.pow() for two numbers using double math when available. 
     * @param lhs LNumber base
     * @param rhs LNumber exponent
     * @return base ^ exponent as a LNumber, throw RuntimeException if not implemented
     */
    abstract public LNumber mathPow(LNumber base, LNumber exponent);

    /**
     * Compute a math operation that takes a single double argument and returns a double
     * @param id the math op, from MathLib constants
     * @param x the argument
     * @return the value as an LNumber
     * @throws LuaErrorException if the id is not supported by this platform.
     */
	abstract public LNumber mathop(int id, LNumber x);

    /**
     * Compute a math operation that takes a two double arguments and returns a double
     * @param id the math op, from MathLib constants
     * @param x the first argument as an LNumber
     * @param y the second arugment as an LNumber
     * @return the value as an LNumber
     * @throws LuaErrorException if the id is not supported by this platform.
     */
	abstract public LNumber mathop(int id, LNumber x, LNumber y);
	

	/** Throw an error indicating the math operation is not accepted */
	public LNumber unsupportedMathOp() {
		throw new LuaErrorException("math op not supported on "+getName());
	}
	
}
