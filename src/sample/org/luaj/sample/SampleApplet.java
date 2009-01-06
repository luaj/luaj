/*******************************************************************************
* Copyright (c) 2009 LuaJ. All rights reserved.
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
package org.luaj.sample;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JApplet;

import org.luaj.lib.j2se.CoerceJavaToLua;
import org.luaj.lib.j2se.J2seIoLib;
import org.luaj.platform.J2sePlatform;
import org.luaj.vm.LValue;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

/**
 * Sample JApplet that runs a lua script in the init phase, and calls
 * global functions for applet lifecycle events.
 * 
 * The script(s) should be located relative to the document root in order 
 * for the lua scripts to be loaded.
 * 
 * The functions init(), start(), stop() and destroy() and the script itself 
 * are called with the applet as the first argument, so that the 
 * lua script can perform whatever operations it needs to using the applet 
 * as the main context.  
 * 
 * The applet looks runs the script identified by the applet parameter 'script' 
 * or 'main.lua' if there is no value for 'script' provided.
 */
public class SampleApplet extends JApplet {
	private static final long serialVersionUID = 1L;

	// the script will be loaded as a resource 
	private static final String DEFAULT_SCRIPT = "main.lua";

	private LuaState vm;
	private String base;
	
	private final LValue lthis = CoerceJavaToLua.coerce(this);

	public synchronized void init() {
		System.out.println("init() called");
		super.init();
		if ( vm == null ) {
	
			// get the script as an app property
			String script = this.getParameter("script");
			if ( script == null )
				script = DEFAULT_SCRIPT;
			base  = tobase( getDocumentBase().toExternalForm() );
			System.out.println("script: "+script);
			System.out.println("base: "+base);
	
			// set up the j2me platform.  files will be loaded as resources
			Platform.setInstance( new J2sePlatform() {
				public InputStream openFile(String fileName) {
					// System.out.println("openFile('"+fileName+"')");
					URLConnection c = null;
					try {
						URL u = new URL(base + fileName);
						c = u.openConnection();
						return c.getInputStream();
					} catch ( Exception e ) {
						e.printStackTrace( System.out );
						if ( c instanceof HttpURLConnection )
							((HttpURLConnection)c).disconnect();
						return null;
					}
				}			
			});
			vm = Platform.newLuaState();
			
			// extend the basic vm to include the compiler and io packages
			org.luaj.compiler.LuaC.install();
			J2seIoLib.install(vm._G);
			
			// run the script
			try {
				vm.getglobal( "loadfile" );
				vm.pushstring( script );
				vm.call( 1, 1 );
				vm.pushlvalue( lthis );
				vm.call( 1, 0 );
			} catch ( Throwable t ) {
				t.printStackTrace( System.err );
			}
		} 	
		callFunction( "init" );
	}
	
	private String tobase(String path) {
		int i = path.lastIndexOf('/');
		return i>=0? path.substring(0,i+1): path;
	}

	private boolean callFunction( String name ) {
		try {
			vm.getglobal( name );
			if ( ! vm.isfunction(-1) ) {
				vm.pop(1);
				return false;
			}
			else {
				vm.pushlvalue( lthis );
				vm.call( 1, 0 );
				return true;
			}
		} catch ( Throwable t ) {
			System.out.println(name+"():");
			t.printStackTrace( System.err );
			return true;
		}
	}
	
	public synchronized void start() {
		super.start();
		callFunction( "start" );
	}

	public synchronized void stop() {
		callFunction( "stop" );
		super.stop();
	}

	public synchronized void destroy() {
		callFunction( "destroy" );
		super.destroy();
	}
}
