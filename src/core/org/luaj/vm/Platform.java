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
import java.io.InputStreamReader;
import java.io.Reader;

import org.luaj.debug.DebugLuaState;
import org.luaj.debug.net.DebugSupport;
import org.luaj.debug.net.j2se.DebugSupportImpl;

/**
 * Singleton to manage platform-specific behaviors. 
 *
 * @deprecated - will probably be replaced with Config, LuaConfig or something similar.
 */
abstract public class Platform {
	private static Platform instance;

	/**
	 * Singleton to be used for platform operations.
	 * 
	 * The default Platform gets files as resources, 
	 * and converts them to characters using the default
	 * InputStreamReader class.
	 */
	public static Platform getInstance() {
        if (instance == null) {
            instance = new Platform() {
                    public Reader createReader(InputStream inputStream) {
                        return new InputStreamReader(inputStream);
                    }
    
                    public InputStream openFile(String fileName) {
                        return getClass().getResourceAsStream("/" + fileName);
                    }
    
                    /**
                     * Assumes J2SE platform, return the corresponding system property
                     */
                    public String getProperty(String propertyName,
                            String defaultValue) {
                        return System.getProperty(propertyName, defaultValue);
                    }
                    
                    /**
                     * Provides a J2SE DebugSupport instance.
                     */
                    public DebugSupport getDebugSupport() throws IOException {
                        String portStr = getProperty(DebugLuaState.PROPERTY_LUAJ_DEBUG_PORT, "-1");
                        try {
                            int port = Integer.parseInt(portStr);
                            return new DebugSupportImpl(port);
                        } catch (NumberFormatException e) {
                            throw new IOException("Bad port number: " + portStr);
                        }
                    }
                };
            }
            return instance;
        }

	/**
	 * Set the Platform instance.
	 * 
	 * This may be useful to define a file search path, 
	 * or custom character encoding conversion properties.
	 */
	public static void setInstance( Platform platform ) {
		instance = platform;
	}
	
	/**
	 * Return an InputStream or null if not found for a particular file name.
	 * @param fileName Name of the file to open
	 * @return InputStream or null if not found.
	 */
	abstract public InputStream openFile( String fileName );
	
	/**
	 * Create Reader from an InputStream
	 * @param inputStream InputStream to read from
	 * @return Reader instance to use for character input
	 */
	abstract public Reader createReader( InputStream inputStream );
	
	/**
	 * Returns the value for the given platform property. 
	 * @param propertyName Property name
	 * @param defaultValue Default property value
	 * @return Property value
	 */
	abstract public String getProperty(String propertyName, String defaultValue);
	
	/**
	 * Returns an platform dependent DebugSupport instance.
	 * @return an plaform dependent DebugSupport instance.
	 */
	abstract public DebugSupport getDebugSupport() throws IOException;
}
