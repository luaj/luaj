

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jme.JmePlatform;


public class SampleMIDlet extends MIDlet {

	// the script will be loaded as a resource 
	private static final String DEFAULT_SCRIPT = "hello.lua";
	
	protected void startApp() throws MIDletStateChangeException {
		// get the script as an app property
		String script = this.getAppProperty("script");
		if ( script == null )
			script = DEFAULT_SCRIPT;
		
		// create an environment to run in
		Globals globals = JmePlatform.standardGlobals();
		globals.get("require").call( LuaValue.valueOf(script) );
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

}
