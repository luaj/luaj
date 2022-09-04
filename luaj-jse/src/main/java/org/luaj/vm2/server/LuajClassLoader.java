/*******************************************************************************
 * Copyright (c) 2015 Luaj.org. All rights reserved.
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
package org.luaj.vm2.server;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class loader that can be used to launch a lua script in a Java VM that has a
 * unique set of classes for org.luaj classes.
 * <P>
 * <em>Note: This class is experimental and subject to change in future
 * versions.</em>
 * <P>
 * By using a custom class loader per script, it allows the script to have its
 * own set of globals, including static values such as shared metatables that
 * cannot access lua values from other scripts because their classes are loaded
 * from different class loaders. Thus normally unsafe libraries such as luajava
 * can be exposed to scripts in a server environment using these techniques.
 * <P>
 * All classes in the package "org.luaj.vm2." are considered user classes, and
 * loaded into this class loader from their bytes in the class path. Other
 * classes are considered systemc classes and loaded via the system loader. This
 * class set can be extended by overriding {@link #isUserClass(String)}.
 * <P>
 * The {@link Launcher} interface is loaded as a system class by exception so
 * that the caller may use it to launch lua scripts.
 * <P>
 * By default {@link #NewLauncher()} creates a subclass of {@link Launcher} of
 * type {@link DefaultLauncher} which creates debug globals, runs the script,
 * and prints the return values. This behavior can be changed by supplying a
 * different implementation class to {@link #NewLauncher(Class)} which must
 * extend {@link Launcher}.
 *
 * @see Launcher
 * @see #NewLauncher()
 * @see #NewLauncher(Class)
 * @see DefaultLauncher
 * @since luaj 3.0.1
 */
public class LuajClassLoader extends ClassLoader {

	/**
	 * String describing the luaj packages to consider part of the user classes
	 */
	static final String luajPackageRoot = "org.luaj.vm2.";

	/**
	 * String describing the Launcher interface to be considered a system class
	 */
	static final String launcherInterfaceRoot = Launcher.class.getName();

	/** Local cache of classes loaded by this loader. */
	Map<String, Class<?>> classes = new HashMap<>();

	/**
	 * Construct a default {@link Launcher} instance that will load classes in
	 * its own {@link LuajClassLoader} using the default implementation class
	 * {@link DefaultLauncher}.
	 * <P>
	 * The {@link Launcher} that is returned will be a pristine luaj vm whose
	 * classes are loaded into this loader including static variables such as
	 * shared metatables, and should not be able to directly access variables
	 * from other Launcher instances.
	 *
	 * @return {@link Launcher} instance that can be used to launch scripts.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static Launcher NewLauncher() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return NewLauncher(DefaultLauncher.class);
	}

	/**
	 * Construct a {@link Launcher} instance that will load classes in its own
	 * {@link LuajClassLoader} using a user-supplied implementation class that
	 * implements {@link Launcher}.
	 * <P>
	 * The {@link Launcher} that is returned will be a pristine luaj vm whose
	 * classes are loaded into this loader including static variables such as
	 * shared metatables, and should not be able to directly access variables
	 * from other Launcher instances.
	 *
	 * @return instance of type 'launcher_class' that can be used to launch
	 *         scripts.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static Launcher NewLauncher(Class<? extends Launcher> launcher_class)
		throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		final LuajClassLoader loader = new LuajClassLoader();
		final Object instance = loader.loadAsUserClass(launcher_class.getName()).newInstance();
		return (Launcher) instance;
	}

	/**
	 * Test if a class name should be considered a user class and loaded by this
	 * loader, or a system class and loaded by the system loader.
	 *
	 * @param classname Class name to test.
	 * @return true if this should be loaded into this class loader.
	 */
	public static boolean isUserClass(String classname) {
		return classname.startsWith(luajPackageRoot) && !classname.startsWith(launcherInterfaceRoot);
	}

	@Override
	public Class<?> loadClass(String classname) throws ClassNotFoundException {
		if (classes.containsKey(classname))
			return classes.get(classname);
		if (!isUserClass(classname))
			return super.findSystemClass(classname);
		return loadAsUserClass(classname);
	}

	private Class<?> loadAsUserClass(String classname) throws ClassNotFoundException {
		final String path = classname.replace('.', '/').concat(".class");
		InputStream is = getResourceAsStream(path);
		if (is != null) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] b = new byte[1024];
				for (int n = 0; (n = is.read(b)) >= 0;)
					baos.write(b, 0, n);
				byte[] bytes = baos.toByteArray();
				Class<?> result = super.defineClass(classname, bytes, 0, bytes.length);
				classes.put(classname, result);
				return result;
			} catch (java.io.IOException e) {
				throw new ClassNotFoundException("Read failed: " + classname + ": " + e);
			}
		}
		throw new ClassNotFoundException("Not found: " + classname);
	}
}
