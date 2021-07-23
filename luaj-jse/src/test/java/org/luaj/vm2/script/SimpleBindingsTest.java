package org.luaj.vm2.script;

import javax.script.Bindings;
import javax.script.SimpleBindings;

class SimpleBindingsTest extends EngineTestCase {
	@Override
	protected Bindings createBindings() {
		return new SimpleBindings();
	}
}
