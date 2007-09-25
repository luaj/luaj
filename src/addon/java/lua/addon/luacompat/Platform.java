package lua.addon.luacompat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Singleton to manage platform-specific behaviors. 
 *
 */
abstract public class Platform {
	private static Platform instance;

	/**
	 * Singleton to be used for platform operations.
	 * 
	 * The default Platform gets files as resources, 
	 * and converts them to characters using the default
	 * InputStreamReader class.
	 */
	public static Platform getInstance() {
		if ( instance == null ) {
			instance = new Platform() {
				public Reader createReader(InputStream inputStream) {
					return new InputStreamReader(inputStream);
				}
				public InputStream openFile(String fileName) {
					return getClass().getResourceAsStream("/"+fileName);
				}
			};
		}
		return instance;
	}

	/**
	 * Set the Platform instance.
	 * 
	 * This may be useful to define a file search path, 
	 * or custom character encoding conversion properties.
	 */
	public static void setInstance( Platform platform ) {
		instance = platform;
	}
	
	/**
	 * Return an InputStream or null if not found for a particular file name.
	 * @param fileName Name of the file to open
	 * @return InputStream or null if not found.
	 */
	abstract public InputStream openFile( String fileName );
	
	/**
	 * Create Reader from an InputStream
	 * @param inputStream InputStream to read from
	 * @return Reader instance to use for character input
	 */
	abstract public Reader createReader( InputStream inputStream );
}
