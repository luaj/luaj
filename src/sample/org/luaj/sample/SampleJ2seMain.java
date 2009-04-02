package org.luaj.sample;
import org.luaj.platform.*;
import org.luaj.vm.*;

public class SampleJ2seMain {

	public static void main(String[] args) {
		String script = (args.length>0? args[0]: "src/test/res/swingapp.lua");
		Platform.setInstance( new J2sePlatform() );
		LuaState vm = Platform.newLuaState();
		// uncomment to install the debug library
		// org.luaj.lib.DebugLib.install(vm);
		org.luaj.compiler.LuaC.install();
		vm.getglobal( "dofile" );
		vm.pushstring( script );
		vm.call( 1, 0 );
	}
}
