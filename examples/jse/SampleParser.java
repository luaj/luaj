/** 
 * Sample luaj program that uses the LuaParser class for parsing, and intercepts the 
 * generated ParseExceptions and fills in the file, line and column information where 
 * the exception occurred.
 */
import java.io.*;

import org.luaj.vm2.ast.*;
import org.luaj.vm2.ast.Exp.AnonFuncDef;
import org.luaj.vm2.ast.Stat.FuncDef;
import org.luaj.vm2.ast.Stat.LocalFuncDef;
import org.luaj.vm2.parser.*;


public class SampleParser {
	
	static public void main(String[] args) {
		if (args.length == 0) {
			System.out.println("usage: SampleParser luafile");
			return;
		}
		try {
			final String file = args[0];
			
			// Create a custom LuaParser subclass that intercepts parse exceptions and 
			// extracts the line and column number information from the parse token.
			LuaParser parser = new LuaParser(new FileInputStream(file));
			
			// Perform the parsing.
			Chunk chunk = parser.Chunk();
			
			// Print out line info for all function definitions.
			chunk.accept( new Visitor() {
				public void visit(AnonFuncDef exp) {
					System.out.println("Anonymous function definition at " 
							+ exp.beginLine + "." + exp.beginColumn + "," 
							+ exp.endLine + "." + exp.endColumn);
				}

				public void visit(FuncDef stat) {
					System.out.println("Function definition '" + stat.name.name.name + "' at " 
							+ stat.beginLine + "." + stat.beginColumn + "," 
							+ stat.endLine + "." + stat.endColumn);

					System.out.println("\tName location " 
							+ stat.name.beginLine + "." + stat.name.beginColumn + "," 
							+ stat.name.endLine + "." + stat.name.endColumn);
				}

				public void visit(LocalFuncDef stat) {
					System.out.println("Local function definition '" + stat.name.name + "' at " 
							+ stat.beginLine + "." + stat.beginColumn + "," 
							+ stat.endLine + "." + stat.endColumn);
				}
			} );
			
		} catch ( ParseException e ) {
			System.out.println( "Parse failed : "+e );
			System.out.println( "Begin Line: "+e.currentToken.beginColumn );
			System.out.println( "Begin Column: "+e.currentToken.endColumn );
			System.out.println( "End Line: "+e.currentToken.beginColumn );
			System.out.println( "End Column: "+e.currentToken.endColumn );
			System.out.println( "Token Image: '"+e.currentToken.image+"'" );
			System.out.println( "Message: "+e.getMessage() );
			
		} catch ( IOException e ) {
			System.out.println( "IOException occurred: "+e );
			e.printStackTrace();
		}
	}
}