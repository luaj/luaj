package lua.addon.compile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;

import junit.framework.TestCase;

import lua.Print;
import lua.StackState;
import lua.addon.compile.Compiler;
import lua.addon.compile.DumpState;
import lua.io.LoadState;
import lua.io.Proto;

abstract 
public class AbstractUnitTests extends TestCase {
	
	private final String zipfile;
	private final String dir;
	
	public AbstractUnitTests(String zipfile, String dir) {
		this.zipfile = zipfile;
		this.dir = dir;
	}

	protected void doTest( String file ) {
		try {
			// load source from jar
			String path = "jar:file:" + zipfile + "!/" + dir + "/" + file;
			byte[] lua = bytesFromJar( path );
			
			// compile in memory
			InputStream is = new ByteArrayInputStream( lua );
	    	Proto p = Compiler.compile(is, dir+"/"+file);
	    	String actual = protoToString( p );
			
			// load expected value from jar
			byte[] luac = bytesFromJar( path + "c" );
			Proto e = loadFromBytes( luac, file );
	    	String expected = protoToString( e );

			// compare results
			assertEquals( expected, actual );
			
			// dump into memory
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DumpState.dump(p, baos, false);
			byte[] dumped = baos.toByteArray();
			
			// re-undump
			Proto p2 = loadFromBytes( dumped, file );
			String actual2 = protoToString( p2 );
			
			// compare again
			assertEquals( actual, actual2 );
			
		} catch (IOException e) {
			fail( e.toString() );
		}
	}
	
	protected byte[] bytesFromJar(String path) throws IOException {
		URL url = new URL(path);
		InputStream is = url.openStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int n;
		while ( (n = is.read(buffer)) >= 0 )
			baos.write( buffer, 0, n );
		is.close();
		return baos.toByteArray();
	}
	
	protected Proto loadFromBytes(byte[] bytes, String script) throws IOException {
		StackState state = new StackState();
		InputStream is = new ByteArrayInputStream( bytes );
		return LoadState.undump(state, is, script);
	}
	
	protected String protoToString(Proto p) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream( baos );
		Print.ps = ps;
		new Print().printFunction(p, true);
		return baos.toString();
	}
	

}
