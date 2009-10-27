/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
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
package org.luaj.vm2.luajc.antlr;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.luaj.vm2.luajc.JavaCodeGenerator;
import org.luaj.vm2.luajc.antlr.LuaLexer;
import org.luaj.vm2.luajc.antlr.LuaParser;
import org.luaj.vm2.luajc.lst.LSChunk;

/** 
 * Implementation of lua-to-java compiler using antlr 
 */
public class AntlrLuaJCompiler {

	private final String chunkname;
	
	public AntlrLuaJCompiler(String chunkname) {
		this.chunkname = chunkname;
	}

	public static String compile(InputStream script, String chunkname) throws RecognitionException, IOException {
		return new AntlrLuaJCompiler(chunkname).docompile( script );
	}

	private String docompile(InputStream script) throws RecognitionException, IOException {
		
		ANTLRInputStream input = new ANTLRInputStream(script);
		LuaLexer lexer = new LuaLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		LuaParser parser = new LuaParser(tokens);
		LSChunk chunk = parser.chunk(chunkname);
		return new JavaCodeGenerator().toJava( chunk );
	}

}
