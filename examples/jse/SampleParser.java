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
	
	// Sample ParseException subclass that stores the file, line, and column info.
	static public class FileLineColumnParseException extends ParseException {
		private static final long serialVersionUID = 1L;
		public final String file;
		public final int line;
		public final int column;
		public FileLineColumnParseException(String file, int line, int column, String message) {
			super(message);
			this.file = file;
			this.line = line;
			this.column = column;
		}
	}
	
	static public void main(String[] args) {
		if (args.length == 0) {
			System.out.println("usage: SampleParser luafile");
			return;
		}
		try {
			final String file = args[0];
			
			// Create a custom LuaParser subclass that intercepts parse exceptions and 
			// extracts the line and column number information from the parse token.
			LuaParser parser = new LuaParser(new FileInputStream(file)) {
				  /** Generate ParseException. */
				  public ParseException generateParseException() {
				    Token errortok = token.next;
				    int line = errortok.beginLine;
				    int column = errortok.beginColumn;
				    String mess = (errortok.kind == 0) ? tokenImage[0] : errortok.image;
				    return new FileLineColumnParseException(file, line, column, mess);
				  }
			};
			
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
			
		} catch ( FileLineColumnParseException e ) {
			System.out.println( "Parse failed at identified line, column: "+e );
			System.out.println( "File: "+e.file );
			System.out.println( "Line: "+e.line );
			System.out.println( "Column: "+e.column );
			System.out.println( "Message: "+e.getMessage() );
			
		} catch ( ParseException e ) {
			System.out.println( "Parse failed at unknown location: "+e );
			e.printStackTrace();
			
		} catch ( IOException e ) {
			System.out.println( "IOException occurred: "+e );
			e.printStackTrace();
		}
	}
}