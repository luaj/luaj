package org.luaj.jit;

import org.luaj.vm.LClosure;
import org.luaj.vm.LPrototype;
import org.luaj.vm.LTable;
import org.luaj.vm.LuaState;

abstract
public class JitPrototype extends LPrototype {
	
	protected LPrototype p;
	
	public JitPrototype() {
		super();
	}
	
	protected void unimplemented() {
		throw new RuntimeException("unimplemented");
	}
	
	protected void setLuaPrototype(LPrototype lp) {
		this.p = lp;
	}

	public LClosure newClosure(LTable env) {
		return new JitClosure(this, env);
	}

	protected static final class JitClosure extends LClosure {
		private final JitPrototype jp;
		public JitClosure(JitPrototype jitPrototype, LTable env) {
			super( jitPrototype.p, env );
			this.jp = jitPrototype;
		}
		public boolean luaStackCall(LuaState vm) {
			jp.jitCall(vm,env,this);
			return false;
		}
	}
	
	public abstract void jitCall( LuaState vm, LTable env, JitClosure jcl );	
}
