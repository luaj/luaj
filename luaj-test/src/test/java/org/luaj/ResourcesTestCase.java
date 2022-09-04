package org.luaj;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

abstract class ResourcesTestCase {

	private String baseDir;

	protected Globals globals;

	@BeforeEach
	protected void setUp() {
		globals = JsePlatform.standardGlobals();
	}

	protected void setBaseDir(String baseDir) { this.baseDir = baseDir; }

	protected InputStream inputStreamOfFile(String file) throws IOException {
		return getClass().getClassLoader().getResourceAsStream(baseDir + "/" + file);
	}

	protected InputStream inputStreamOfLua(String name) throws IOException {
		return inputStreamOfFile(name + ".lua");
	}

	protected InputStream inputStreamOfResult(String name) throws IOException {
		return inputStreamOfFile(name + ".out");
	}

	protected InputStream inputStreamOfBytecode(String name) throws IOException {
		return inputStreamOfFile(name + ".lc");
	}
}
