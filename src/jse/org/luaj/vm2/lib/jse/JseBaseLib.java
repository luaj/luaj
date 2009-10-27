/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
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
package org.luaj.vm2.lib.jse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/** 
 * Base library implementation, targeted for JSE platforms.  
 * 
 * Implements the same library functions as org.luaj.lib.BaseLib, 
 * but looks in the current directory for files loaded via 
 * loadfile(), dofile() and require(). 
 *  
 * @see org.luaj.vm2.lib.jse.JseBaseLib
 */
public class JseBaseLib extends org.luaj.vm2.lib.BaseLib {

	static {
		STDIN = System.in;
	}

	/** Construct a JSE base library instance */
	public JseBaseLib() {
	}

	/** 
	 * Try to open a file in the current working directory, 
	 * or fall back to base opener if not found.
	 * 
	 * This implementation attempts to open the file using new File(filename).  
	 * It falls back to the base implementation that looks it up as a resource
	 * in the class path if not found as a plain file. 
	 *  
	 * @see org.luaj.vm2.lib.BaseLib
	 * @see org.luaj.vm2.lib.ResourceFinder
	 * 
	 * @param filename
	 * @return InputStream, or null if not found. 
	 */
	public InputStream findResource(String filename) {
		File f = new File(filename);
		if ( ! f.exists() )
			return super.findResource(filename);
		try {
			return new FileInputStream(f);
		} catch ( IOException ioe ) {
			return null;
		}
	}
}
