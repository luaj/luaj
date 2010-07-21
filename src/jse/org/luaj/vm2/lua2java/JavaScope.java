/*******************************************************************************
* Copyright (c) 2010 Luaj.org. All rights reserved.
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
package org.luaj.vm2.lua2java;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.luaj.vm2.Lua;
import org.luaj.vm2.ast.Block;
import org.luaj.vm2.ast.Chunk;
import org.luaj.vm2.ast.FuncBody;
import org.luaj.vm2.ast.NameScope;
import org.luaj.vm2.ast.Visitor;
import org.luaj.vm2.ast.Exp.BinopExp;
import org.luaj.vm2.ast.Stat.Return;


public class JavaScope extends NameScope {

	private static final int MAX_CONSTNAME_LEN = 8;
	public static final Set<String> SPECIALS = new HashSet<String>();

	private static final String[] specials = {
			// keywords used by our code generator
			"name", 	"opcode",	"env",	"arg", 
			
			// java keywords
			"abstract", "continue", "for",  	"new",		"switch",
			"assert", 	"default", 	"goto", 	"package", 	"synchronized",
			"boolean", 	"do", 		"if", 		"private", 	"this",
			"break", 	"double", 	"implements", "protected", 	"throw",
			"byte", 	"else", 	"import", 	"public", 	"throws",
			"case", 	"enum", 	"instanceof", "return", "transient",
			"catch", 	"extends", 	"int", 		"short", 	"try",
			"char", 	"final", 	"interface", "static", 	"void",
			"class", 	"finally", 	"long", 	"strictfp", "volatile",
			"const", 	"float", 	"native", 	"super", 	"while",
	};
	
	static {
		for ( int i=0; i<specials.length; i++ )
			SPECIALS.add(specials[i]);
		// TODO: add any via reflection from LuaValue
	}

	public int nreturns;
	public boolean needsbinoptmp;

	final Set<String> staticnames;
	final Set<String> javanames = new HashSet<String>();
	final Map<Object,String> astele2javaname = new HashMap<Object,String>();
//	final public List<String> tmpvars = new ArrayList<String>();
	
	private JavaScope(Set<String> staticnames, JavaScope outerScope) {		
		super(outerScope);	
		this.staticnames = staticnames;
	}
	
	public static JavaScope newJavaScope(Chunk chunk) {
		return new JavaScope(new HashSet<String>(), null).initialize(chunk.block, -1);
	}
	
	public JavaScope pushJavaScope(FuncBody body) {
		return new JavaScope(staticnames, this).initialize(body.block, 0);
	}
	
	public JavaScope popJavaScope() {
		return (JavaScope) outerScope;
	}
	
	final String getJavaName(NamedVariable nv) {
		if ( astele2javaname.containsKey(nv) )
			return astele2javaname.get(nv);
		return allocateJavaName( nv, nv.name );
	}

//	public String getTempVar(BinopExp exp) {
//		return astele2javaname.containsKey(exp)? astele2javaname.get(exp): "$t";
//	}
//
//	public void allocateTempVar(BinopExp exp) {
//		tmpvars.add( allocateJavaName( exp, "t" ) );
//	}
//	
	final private String allocateJavaName(Object astele, String proposal) {
		for ( int i=0; true; i++ ) {
			String jname = proposal+(i==0? "": "$"+i);
			if ( ! javanames.contains(jname) && ! SPECIALS.contains(jname)  ) {
				javanames.add(jname);
				astele2javaname.put(astele,jname);
				return jname;
			}
		}
	}
	
	public String createConstantName(String proposal) {
		proposal = toLegalJavaName(proposal);
		for ( int i=0; true; i++ ) {
			String jname = proposal+(i==0? "": "$"+i);
			if ( ! javanames.contains(jname) && ! SPECIALS.contains(jname) && !staticnames.contains(jname) ) {
				javanames.add(jname);
				staticnames.add(jname);
				return jname;
			}
		}
	}

	public static String toLegalJavaName(String string) {
		String better = string.replaceAll("[^\\w]", "_");
		if ( better.length() > MAX_CONSTNAME_LEN )
			better = better.substring(0,MAX_CONSTNAME_LEN);
//		if ( !Character.isJavaIdentifierStart( better.charAt(0) ) )
//			better = "_"+better;
		return "$"+better;
	}
	
	private JavaScope initialize(Block block, int nreturns) {
		NewScopeVisitor v = new NewScopeVisitor(nreturns);
		block.accept( v );
		this.nreturns = v.nreturns;
		this.needsbinoptmp = v.needsbinoptmp;
		return this;
	}

	class NewScopeVisitor extends Visitor {
		int nreturns = 0;
		boolean needsbinoptmp = false;
		NewScopeVisitor(int nreturns) {
			this.nreturns = nreturns;
		}
		public void visit(FuncBody body) {}
		public void visit(Return s) {
			int n = s.nreturns();
			nreturns = (nreturns<0||n<0? -1: Math.max(n,nreturns));
		}
		public void visit(BinopExp exp) {
			switch ( exp.op ) {
			case Lua.OP_AND: case Lua.OP_OR:
				needsbinoptmp = true;
				break;
			}
			super.visit(exp);
		}
	}	
}
