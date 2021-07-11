package org.luaj.vm2.script;

import javax.script.Bindings;

abstract class DefaultBindingsTestCase extends EngineTestCase {
	@Override
	protected Bindings createBindings() {
		return e.createBindings();
	}
}
