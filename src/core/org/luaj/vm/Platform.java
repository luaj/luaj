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

	/**
	 * When non-null, name of a Java Class that implements LuaState 
	 * and has a default constructor which will be used to construct 
	 * a new lua state using Platform.newLuaState()
	 * @see newLuaState 
	 */
    public static String LUA_STATE_CLASS_NAME = null;

    /** 
     * The singleton Platform instance in use by this JVM
     */
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
     * Creates a new instance of LuaState. 
     * 
     * If Platform.LUA_STATE_CLASS_NAME is not null, this method 
     * will attempt to create an instance using a construction such as 
     * 	(LuaState) Class.forName(Platform.LUA_STATE_CLASS_NAME).newInstance()
     * 
     * If Platform.LUA_STATE_CLASS_NAME is null or the specified class cannot 
     * be instantiated for some reason, a new instance of LuaState
     * will be created and used.
     * 
     * In either case, the method LuaState.init() will be invoked, followed by
     * Platform.installOptionalProperties(LuaState) on the new instance. 
     * 
     * @return a new instance of LuaState initialized via init() and installOptionalProperties()
     * 
     * @see LUA_STATE_CLASS_NAME
     * @see LuaState
     */
    public static LuaState newLuaState() {
        Platform p = Platform.getInstance();
        LuaState vm = null;
        if (LUA_STATE_CLASS_NAME != null) {
            try {
                vm = (LuaState) Class.forName(LUA_STATE_CLASS_NAME).newInstance();
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
     * Install optional libraries on the LuaState.
     * @param vm LuaState instance
     */
    abstract protected void installOptionalLibs(LuaState vm);
    
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
