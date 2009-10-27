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
import java.io.IOException;

/**
 * Implementation of the lua os library for J2se.
 * 
 * <p>Implements features specific to the J2se environment:
 * <bl>
 * <li>execute()</li>
 * <li>remove()</li>
 * <li>rename()</li>
 * <li>tmpname()</li>
 * </bl>
 * 
 * @see org.luaj.vm2.lib.OsLib
 */
public class JseOsLib extends org.luaj.vm2.lib.OsLib {
	
	/** return code indicating the execute() threw an I/O exception */
	public static int EXEC_IOEXCEPTION = -1;
	
	/** return code indicating the execute() was interrupted */
	public static int EXEC_INTERRUPTED = -2;
	
	/** return code indicating the execute() threw an unknown exception */
	public static int EXEC_ERROR       = -3;
	
	/** public constructor */
	public JseOsLib() {
	}

	protected int execute(String command) {
		Runtime r = Runtime.getRuntime();
		try {
			final Process p = r.exec(command);
			try {
				p.waitFor();
				return p.exitValue();
			} finally {
				p.destroy();
			}
		} catch (IOException ioe) {
			return EXEC_IOEXCEPTION;
		} catch (InterruptedException e) {
			return EXEC_INTERRUPTED;
		} catch (Throwable t) {
			return EXEC_ERROR;
		}		
	}

	protected void remove(String filename) throws IOException {
		File f = new File(filename);
		if ( ! f.exists() )
			throw new IOException("No such file or directory");
		if ( ! f.delete() )
			throw new IOException("Failed to delete");
	}

	protected void rename(String oldname, String newname) throws IOException {
		File f = new File(oldname);
		if ( ! f.exists() )
			throw new IOException("No such file or directory");
		if ( ! f.renameTo(new File(newname)) )
			throw new IOException("Failed to delete");
	}

	protected String tmpname() {
		try {
			java.io.File f = java.io.File.createTempFile(TMP_PREFIX ,TMP_SUFFIX);
			return f.getName();
		} catch ( IOException ioe ) {
			return super.tmpname();
		}
	}
	
}
