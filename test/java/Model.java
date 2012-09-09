

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.VarArgFunction;

public class Model extends VarArgFunction {
	LuaValue[] u0;

	public void initupvalue1(LuaValue env) {
		u0 = this.newupl(env);
	}
}
