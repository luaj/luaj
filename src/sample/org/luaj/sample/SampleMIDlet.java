package org.luaj.sample;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.luaj.lib.j2me.Cldc10IoLib;
import org.luaj.platform.J2meMidp20Cldc11Platform;
import org.luaj.vm.LuaState;
import org.luaj.vm.Platform;

public class SampleMIDlet extends MIDlet {

	// the script will be loaded as a resource 
	private static final String DEFAULT_SCRIPT = "test1.lua";
	
	protected void startApp() throws MIDletStateChangeException {
		// get the script as an app property
		String script = this.getAppProperty("script");
		if ( script == null )
			script = DEFAULT_SCRIPT;
		
		// set up the j2me platform.  files will be loaded as resources
		Platform.setInstance( new J2meMidp20Cldc11Platform(this) );
		LuaState vm = Platform.newLuaState();
		
		// extend the basic vm to include the compiler and io packages
		org.luaj.compiler.LuaC.install();
		Cldc10IoLib.install(vm._G);
		
		// run the script
		vm.getglobal( "dofile" );
		vm.pushstring( script );
		vm.call( 1, 0 );
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

}
