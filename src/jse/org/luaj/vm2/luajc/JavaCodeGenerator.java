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
package org.luaj.vm2.luajc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.luajc.lst.LSChunk;
import org.luaj.vm2.luajc.lst.LSExpression;
import org.luaj.vm2.luajc.lst.LSField;
import org.luaj.vm2.luajc.lst.LSFunction;
import org.luaj.vm2.luajc.lst.LSIfStatement;
import org.luaj.vm2.luajc.lst.LSStatement;
import org.luaj.vm2.luajc.lst.LSVariable;
import org.luaj.vm2.luajc.lst.Name;
import org.luaj.vm2.luajc.lst.Scope;
import org.luaj.vm2.luajc.lst.LSExpression.BinopExpr;
import org.luaj.vm2.luajc.lst.LSExpression.FunctionExpr;
import org.luaj.vm2.luajc.lst.LSExpression.NumberConstant;
import org.luaj.vm2.luajc.lst.LSExpression.StringConstant;
import org.luaj.vm2.luajc.lst.LSExpression.TableConstructor;
import org.luaj.vm2.luajc.lst.LSExpression.UnopExpr;
import org.luaj.vm2.luajc.lst.LSExpression.VarargsRef;
import org.luaj.vm2.luajc.lst.LSStatement.BreakStat;
import org.luaj.vm2.luajc.lst.LSStatement.DoBlock;
import org.luaj.vm2.luajc.lst.LSStatement.ForList;
import org.luaj.vm2.luajc.lst.LSStatement.ForLoop;
import org.luaj.vm2.luajc.lst.LSStatement.FunctionCall;
import org.luaj.vm2.luajc.lst.LSStatement.LocalAssign;
import org.luaj.vm2.luajc.lst.LSStatement.LocalFunction;
import org.luaj.vm2.luajc.lst.LSStatement.RepeatUntil;
import org.luaj.vm2.luajc.lst.LSStatement.ReturnStat;
import org.luaj.vm2.luajc.lst.LSStatement.VarAssign;
import org.luaj.vm2.luajc.lst.LSStatement.VarNamedFunction;
import org.luaj.vm2.luajc.lst.LSStatement.WhileLoop;
import org.luaj.vm2.luajc.lst.LSVariable.CallFunction;
import org.luaj.vm2.luajc.lst.LSVariable.CallMethod;
import org.luaj.vm2.luajc.lst.LSVariable.Field;
import org.luaj.vm2.luajc.lst.LSVariable.Index;
import org.luaj.vm2.luajc.lst.LSVariable.Method;
import org.luaj.vm2.luajc.lst.LSVariable.NameReference;
import org.luaj.vm2.luajc.lst.LSVariable.Parentheses;

public class JavaCodeGenerator {

	public static String toJava(LSChunk chunk) {
		JavaCodeGenerator jcg = new JavaCodeGenerator();
		return jcg.writeChunk( chunk );
	}

	public final Stack<StringBuffer> stringBuffers = new Stack<StringBuffer>();
	public StringBuffer sb;
	private int indent = 0;
	
	public JavaCodeGenerator() {
		this.sb = new StringBuffer();
	}

	private String writeChunk(LSChunk chunk) {
		pushFunctionContext();
		writeln( "import org.luaj.vm2.*;" );
		writeln( "import org.luaj.vm2.lib.*;" );
		writeln();
		writeln( "public class "+chunk.chunkname+" extends VarArgFunction {" );
		++indent;
		writeln( "public Varargs invoke(Varargs $args) {");
		++indent;
		writeFunctionBody( chunk.function );
		--indent;
		writeln( "}");
		--indent;
		// TODO: write out chunk constants
		writeln( "}" );
		return popFunctionContext();
	}

	private void pushFunctionContext() {
		stringBuffers.push( sb = new StringBuffer() );
	}

	private String popFunctionContext() {
		String v = stringBuffers.pop().toString();
		sb = stringBuffers.isEmpty()? null: stringBuffers.lastElement();
		return v;
	}

	private void writeFunctionBody(LSFunction function) {
		if ( function.hasandlogic || function.hasorlogic ) 
			writeln( "LuaValue $t;" );		
		if ( function.hasvarargassign ) 
			writeln( "Varargs $v;" );		
		writeStatements( function.stats );
		if ( LSStatement.isNextStatementReachable( function.stats ) )
			writeln( "return NONE;" );
	}

	private void writeStatements(List<LSStatement> statements) {
		for ( LSStatement s : statements ) {
			writeStatement( s );
		}
	}

	private void writeStatement(LSStatement s) {
		if ( s==null ) return;
		switch ( s.type ) {
		case functionCall:     writeFunctionCall(    (LSStatement.FunctionCall)     s ); break;
		case doBlock:          writeDoBlock(         (LSStatement.DoBlock)          s ); break;
		case whileLoop:        writeWhileLoop(       (LSStatement.WhileLoop)        s ); break;
		case repeatUntil:      writeRepeatUntil(     (LSStatement.RepeatUntil)      s ); break;
		case varAssign:        writeVarAssign(       (LSStatement.VarAssign)        s ); break;
		case forLoop:          writeForLoop(         (LSStatement.ForLoop)          s ); break;
		case forList:          writeForList(         (LSStatement.ForList)          s ); break;
		case varNamedFunction: writeVarNamedFunction((LSStatement.VarNamedFunction) s ); break;
		case localFunction:    writeLocalFunction(   (LSStatement.LocalFunction)    s ); break;
		case localAssign:      writeLocalAssign(     (LSStatement.LocalAssign)      s ); break;
		case returnStat:       writeReturnStat(      (LSStatement.ReturnStat)       s ); break;
		case breakStat:        writeBreakStat(       (LSStatement.BreakStat)        s ); break;
		case ifStat:           writeIfStat(          (LSIfStatement)                s ); break;
		}
	}

	private void writeFunctionCall(FunctionCall s) {
		writeindent();
		write( eval(s.variable)+";" );
		writeln();
	}

	private void writeDoBlock(DoBlock s) {
		writeln( "{" );
		++indent;
		writeStatements( s.statements );
		--indent;
		writeln( "}" );
	}

	private void writeWhileLoop(WhileLoop s) {
		writeln( "while ("+eval(s.condition)+".toboolean()) {" );
		++indent;
		writeStatements( s.statements );
		--indent;
		writeln( "}" );
	}

	private void writeRepeatUntil(RepeatUntil s) {
		writeln( "do {" );
		++indent;
		writeStatements( s.statements );
		--indent;
		writeln( "} while (!"+eval(s.condition)+");" );
	}

	private void writeForLoop(ForLoop s) {
		writeln( "{" );
		++indent;
		// TODO: name handling, also upvalues!
		String index = javaName( s.index );
		String limit = javaName( s.scope.declare("$limit") );
		String step  = javaName( s.scope.declare("$step") );
		writeln( "LuaValue "+index+"="+eval(s.initial)+";" );
		writeln( "final LuaValue "+limit+"="+eval(s.limit)+";" );
		if ( s.step != null ) {
			writeln( "final LuaValue "+step+"="+eval(s.step)+";" );
			writeln( "final boolean "+step+"$b="+step+".gt_b(0);" );
		}
		if ( s.step != null ) {
			writeln( "for ( ; "+index+".testfor_b("+limit+","+step+"$b); "+index+"="+index+".add("+step+") ) {" );
		} else {
			writeln( "for ( ; "+index+".lteq_b("+limit+"); "+index+"="+index+".add(1) ) {" );
		}
		++indent;
		writeStatements( s.statements );
		--indent;
		writeln( "}" );
		--indent;
		writeln( "}" );
	}

	private void writeForList(ForList s) {
		writeln( "{" );
		++indent;
		List<String> exprs = evalExpressions(s.expressions, 3, s.scope);
		// TODO: upvalues handling!
		String fun = javaName( s.scope.declare("$f") );
		String sta = javaName( s.scope.declare("$s") );
		String var = javaName( s.scope.declare("$var") );
		String res = javaName( s.scope.declare("$res") );
		writeln( "LuaValue "+fun+"="+exprs.get(0)+";" );
		writeln( "LuaValue "+sta+"="+exprs.get(1)+";" );
		writeln( "LuaValue "+var+"="+exprs.get(2)+";" );
		writeln( "while ( true ) {" );
		++indent;
		writeln( "Varargs "+res+" = "+fun+".invoke(varargsOf("+sta+","+var+"));" );
		for ( int i=1, n=s.names.size(); i<=n; i++ )	
			writeln( "LuaValue "+javaName(s.names.get(i-1))+"="+res+".arg("+i+");" );
		writeln( var+"="+javaName(s.names.get(0))+";" );
		writeln( "if ( "+var+".isnil() ) break;" );
		writeStatements( s.statements );
		--indent;
		writeln( "}" );
		--indent;
		writeln( "}" );
	}

	private final static Set<String> reserved = new HashSet<String>();
	static {
		String[] specials = {
				// keywors used by our code generator
				"name", 	"opcode",	"env",
				
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
		for ( int i=0; i<specials.length; i++ )
			reserved.add( specials[i] );
		java.lang.reflect.Field[] f = LibFunction.class.getFields(); 
		for ( int i=0; i<f.length; i++ ) 
			reserved.add( f[i].getName() );
	}
	
	private String javaName(Name name) {
		return name.innerrevision>0? 
					name.luaname+"$"+name.innerrevision: 
					reserved.contains(name.luaname)? (name.luaname+"$"): name.luaname;
	}

	private void writeVarNamedFunction(VarNamedFunction s) {
		String funcdef = evalFuncbody(s.funcbody);
		writeAssign( s.funcname, funcdef );
	}

	private String evalFuncbody(LSFunction funcbody) {
		pushFunctionContext();
		int n = funcbody.paramnames!=null? funcbody.paramnames.size(): 0;
		boolean isvararg = (funcbody.isvararg || n > 3);
		if ( isvararg ) {
			write( "new VarArgFunction(env) {\n" );
			++indent;
			writeln( "public Varargs invoke(Varargs $args) {" );
			++indent;
			for ( int i=0; i<n; i++ ) {
				Name name = funcbody.paramnames.get(i);
				if ( name.isupvalue )
					writeln( "final LuaValue[] "+javaName(funcbody.paramnames.get(i))+"={$args.arg("+(i+1)+")};" );
				else
					writeln( "LuaValue "+javaName(funcbody.paramnames.get(i))+"=$args.arg("+(i+1)+");" );
			}
			if ( (n > 0 && funcbody.usesvarargs) || funcbody.needsarg )
				writeln( "$args = $args.subargs("+(n+1)+");" );
			if ( funcbody.needsarg )
				writeln( "LuaValue arg = new LuaTable($args);" );
			else if ( funcbody.hasarg ) 
				writeln( "LuaValue arg = NIL;" );
			writeFunctionBody(funcbody);
			--indent;
			writeln( "}" );
			--indent;
			writeindent();
			write( "}" );
		} else {
			write( 
				n==0? "new ZeroArgFunction(env) {\n": 
				n==1? "new OneArgFunction(env) {\n": 
				n==2? "new TwoArgFunction(env) {\n": 
				      "new ThreeArgFunction(env) {\n" );
			++indent;
			writeindent();
			write( "public LuaValue call(");
			for ( int i=0; i<n; i++ ) {
				if (i>0) write( "," );
				Name name = funcbody.paramnames.get(i);
				if ( name.isupvalue )
					write( "LuaValue "+javaName(name)+"$u" );
				else
					write( "LuaValue "+javaName(name) );
			}
			write( ") {" );
			writeln();
			++indent;
			
			// upvalues
			for ( int i=0; i<n; i++ ) {
				Name name = funcbody.paramnames.get(i);
				if ( name.isupvalue )
					writeln( "final LuaValue[] "+javaName(name)+"={"+javaName(name)+"$u};" );
			}

			// function body
			writeFunctionBody(funcbody);
			--indent;
			writeln( "}" );
			--indent;
			writeindent();
			write( "}" );
		}
		return popFunctionContext();
	}

	private void writeVarAssign(VarAssign s) {
		int nassign = s.variables.size();
		List<String> exprs = evalExpressions(s.expressions, nassign, s.scope);
		for ( int i=0; i<nassign; i++ )
			writeAssign( s.variables.get(i), exprs.get(i) );
		for ( int i=nassign; i<exprs.size(); i++ )
			writeln( exprs.get(i) );
	}

	private void writeLocalFunction(LocalFunction s) {
		String funcdef = evalFuncbody(s.funcbody);
		if ( s.name.isupvalue ) {
			writeln( "final LuaValue[] "+javaName(s.name)+"={null};" );
			writeln( javaName(s.name)+"[0]="+funcdef+";" );
		} else
			writeln( "LuaValue "+javaName(s.name)+"="+funcdef+";" );
	}
	
	private void writeLocalAssign(LocalAssign s) {
		int nassign = s.names.size();
		List<String> exprs = evalExpressions(s.expressions, nassign, s.scope);
		for ( int i=0; i<nassign; i++ ) {
			Name name= s.names.get(i);
			if ( name.isupvalue )
				writeln( "final LuaValue[] "+javaName(name)+"={"+exprs.get(i)+"};" );
			else
				writeln( "LuaValue "+javaName(name)+"="+exprs.get(i)+";" );
		}
		for ( int i=nassign; i<exprs.size(); i++ )
			writeln( exprs.get(i)+";" );
	}
	
	/** Evaluate expressions for use in assignment 
	 * @param scope */
	private List<String> evalExpressions(List<LSExpression> exprs, int nassign, Scope scope) {
		int nexpr = (exprs!=null? exprs.size(): 0);
		List<String> e = new ArrayList<String>(nexpr);
		boolean hasvarargs = false;
		for ( int i=0; i<nexpr || i<nassign; i++ ) {
			if ( i<nexpr-1 || nassign <= nexpr ) {
				e.add( eval( exprs.get(i) ) );
			} else if ( i==nexpr-1 ) {
				int nr = exprs.get(i).getNumReturns();
				hasvarargs = (nr==-1) || (nr>1);
				if ( hasvarargs )
					e.add( "($v="+eval(exprs.get(i))+").arg1()" );
				else
					e.add( eval(exprs.get(i)) );
			} else if (hasvarargs) {
				e.add( "$v.arg("+(i-nexpr+2)+")" );
			} else {
				e.add( "NIL" );
			}
		}
		return e;
	}
	

	private void writeReturnStat(ReturnStat s) {
		int n = s.expressions!=null? s.expressions.size(): 0;
		if ( ! s.function.isvararg )
			writeln( n==0? "return NONE;": "return "+eval(s.expressions.get(0))+";" );
		else {
			writeindent();
			switch ( n ) {
			case 0:  
				write( "return NONE;" ); 
				break;
			case 1:  
				write( "return "+eval( s.expressions.get(0))+";" ); 
				break;
			case 2: case 3: {
				write( "return varargsOf(" );
				for ( int i=0; i<n; i++ ) {
					if (i>0) write( "," );
					write( eval( s.expressions.get(i)) );
				}
				write( ");" );
				break;
			}
			default: {
				write( "return varargsOf(new LuaValue[] {" );
				for ( int i=0; i<n-1; i++ ) {
					if (i>0) write( "," );
					write( eval( s.expressions.get(i)) );
				}
				write( "},"+eval(s.expressions.get(n-1))+");" );
				break;
			}
			}
			writeln();
		}
	}

	private void writeBreakStat(BreakStat s) {
		writeln( "break;" );
	}

	private void writeIfStat(LSIfStatement s) {
		writeln( "if ("+eval_bool(s.condition)+") {" );
		++indent;
		writeStatements( s.statements );
		if ( s.elseifs != null ) {
			for ( LSIfStatement.ElseIf elseif : s.elseifs ) {
				--indent;
				writeln( "} else if ("+eval_bool(elseif.condition)+") {" );
				++indent;
				writeStatements( elseif.statements );
			}
		}
		if ( s.elsestatements != null ) {
			--indent;
			writeln( "} else {" );
			++indent;
			writeStatements( s.elsestatements );
		}
		--indent;
		writeln( "}" );
	}

	//-------------------------------------------
	// assignment using variables
	//-------------------------------------------
	
	/** Write assignment of a particular variable value */
	private void writeAssign(LSVariable v, String expression) {
		switch ( v.type ) {
		case nameVariable:         writeNameAssign(    (LSVariable.NameReference)        v, expression); break;
		case fieldVariable:        writeFieldAssign(   (LSVariable.Field)       v, expression); break;
		case methodVariable:       writeMethodAssign(  (LSVariable.Method)      v, expression); break;
		case parenthesesVariable:  writeParenAssign(   (LSVariable.Parentheses) v, expression); break;
		case indexVariable:        writeIndexAssign(   (LSVariable.Index)       v, expression); break;
		case callFunctionVariable: writeCallFuncAssign((LSVariable.CallFunction)v, expression); break;
		case callMethodVariable:   writeCallMethAssign((LSVariable.CallMethod)  v, expression); break;
		}		
	}
	
	private void writeNameAssign(NameReference v, String expression) {
		if ( v.name.isGlobal() )
			writeln( "env.set(\""+v.name.luaname+"\","+expression+");");
		else if ( v.name.isupvalue )
			writeln( javaName(v.name)+"[0]="+expression+";");
		else
			writeln( javaName(v.name)+"="+expression+";");
	}

	private void writeFieldAssign(Field v, String expression) {
		String base = eval(v.variable); 
		writeln( base+".set(\""+v.field+"\","+expression+");");
	}

	private void writeMethodAssign(Method v, String expression) {
		String base = eval(v.variable); 
		writeln( base+".set(\""+v.method+"\","+expression+");");
	}

	private void writeParenAssign(Parentheses v, String expression) {
		throw new IllegalArgumentException("no assignment for parenthesis expressions");
	}

	private void writeIndexAssign(Index v, String expression) {
		String base = eval(v.variable); 
		writeln( base+".set("+eval(v.expression)+","+expression+");");
	}

	private void writeCallFuncAssign(CallFunction v, String expression) {
		throw new IllegalArgumentException("no assignment for call function expressions");
	}

	private void writeCallMethAssign(CallMethod v, String expression) {
		throw new IllegalArgumentException("no assignment for call method expressions");
	}
	
	//-------------------------------------------
	// write out expressions
	//-------------------------------------------
	
	private String eval_bool(LSExpression e) {
		return eval(e)+".toboolean()";
	}
	
	/** evaluate the expression to a particular operand type */
	private String eval(LSExpression e) {
		if ( e==null ) return "NONE";
		switch ( e.type ) {
		case nilConstant:          return "NIL";
		case trueConstant:         return "TRUE";
		case falseConstant:        return "FALSE";
		case unop:                 return evalUnop(    (LSExpression.UnopExpr)         e);
		case binop:                return evalBinop(   (LSExpression.BinopExpr)        e);
		case functionExpr:         return evalFunction((LSExpression.FunctionExpr)     e);
		case tableConstructor:     return evalTable(   (LSExpression.TableConstructor) e);
		case numberConstant:       return evalNumber(  (LSExpression.NumberConstant)   e);
		case stringConstant:       return evalString(  (LSExpression.StringConstant)   e);
		
		// variable types
		case nameVariable:		   return evalNameRef( (LSVariable.NameReference) e);
		case fieldVariable:        return evalField(   (LSVariable.Field)         e);
		case methodVariable:       return evalMethod(  (LSVariable.Method)        e);
		case parenthesesVariable:  return evalParen(   (LSVariable.Parentheses)   e);
		case indexVariable:        return evalIndex(   (LSVariable.Index)         e);
		case callFunctionVariable: return evalCallFunc((LSVariable.CallFunction)  e);
		case callMethodVariable:   return evalCallMeth((LSVariable.CallMethod)    e);
		case varargsRef:           return evalVarargs( (LSExpression.VarargsRef)  e);
		
		default: throw new IllegalArgumentException("unknown expression type: "+e.type);
		}
	}

	private String evalUnop(UnopExpr e) {
		switch ( e.op.type ) {
		case neg: return eval( e.rhs )+".neg()"; 
		case not: return eval( e.rhs )+".not()"; 
		case len: return eval( e.rhs )+".len()"; 
		}
		throw new IllegalArgumentException("unknown unary operand: "+e.op );
	}

	private String evalBinop(BinopExpr e) {
		switch ( e.op.type ) {
		case pow:    return eval(e.lhs)+".pow("+eval(e.rhs)+")";
		case mul:    return eval(e.lhs)+".mul("+eval(e.rhs)+")";
		case div:    return eval(e.lhs)+".div("+eval(e.rhs)+")";
		case mod:    return eval(e.lhs)+".mod("+eval(e.rhs)+")";
		case add:    return eval(e.lhs)+".add("+eval(e.rhs)+")";
		case sub:    return eval(e.lhs)+".sub("+eval(e.rhs)+")";
		case concat: return eval(e.lhs)+".concat("+eval(e.rhs)+")";
		case lt:     return eval(e.lhs)+".lt("+eval(e.rhs)+")";
		case lteq:   return eval(e.lhs)+".lteq("+eval(e.rhs)+")";
		case gt:     return eval(e.lhs)+".gt("+eval(e.rhs)+")";
		case gteq:   return eval(e.lhs)+".gteq("+eval(e.rhs)+")";
		case eq:     return eval(e.lhs)+".eq("+eval(e.rhs)+")";
		case neq:    return eval(e.lhs)+".neq("+eval(e.rhs)+")";
		case and:    return "(($t="+eval(e.lhs)+").toboolean()? "+eval(e.rhs)+": $t)";
		case or:     return "(($t="+eval(e.lhs)+").toboolean()? $t: "+eval(e.rhs)+")";
		default: throw new IllegalStateException("unknoqn binary operator: "+e.op);
		}
	}

	private String evalFunction(FunctionExpr e) {
		return evalFuncbody(e.function);
	}

	private String evalTable(TableConstructor e) {
		StringBuffer named = new StringBuffer();
		StringBuffer numbered = new StringBuffer();
		LSExpression varargsLastListValue = null;
		for ( int i=0, n=e.fields.size(); i<n; i++ ) {
			LSField f = e.fields.get(i);
			switch ( f.type ) {
			case keyValue: 
				LSField.KeyValue k = (LSField.KeyValue) f;
				named.append( eval(k.key)+","+eval(k.value)+",");
				break;
			case nameValue:
				LSField.NameValue nv = (LSField.NameValue) f;
				named.append( "valueOf(\""+nv.name+"\"),"+eval(nv.value)+",");
				break;
			case listValue:
				LSField.ListValue l = (LSField.ListValue) f;
				int nr = l.value.getNumReturns();
				if ( i<n-1 && (nr==1) )
					numbered.append( eval(l.value)+",");
				else
					varargsLastListValue = l.value;
				break;
			}
		}
		// TODO: generated more targeted constructor
		return "tableOf("
				+(named   .length()>0? "new LuaValue[] {"+named   +"}": "null")+","
				+(numbered.length()>0? "new LuaValue[] {"+numbered+"}": "null")
				+(varargsLastListValue!=null? ","+eval(varargsLastListValue): "")+")";
	}

	private String evalNumber(NumberConstant e) {
		// TODO: internalize constants
		return "valueOf("+e.value+")";
	}

	private String evalString(StringConstant e) {
		// TODO: internalize constants
		return "valueOf("+toStrValueInitializer(e.bytes)+")";
	}

	private String toStrValueInitializer(byte[] bytes) {
		int n = bytes.length;
		StringBuffer sb = new StringBuffer(n+2);		
		
		// check for characters beyond ascii 128
		for ( int i=0; i<n; i++ )
			if (bytes[i]<0) {
				sb.append( "new byte[]{" );
				for ( int j=0; j<n; j++ ) {
					if ( j>0 ) sb.append(",");
					byte b = bytes[j];
					switch ( b ) {
						case '\n': sb.append( "'\\n'" ); break; 
						case '\r': sb.append( "'\\r'" ); break; 
						case '\t': sb.append( "'\\t'" ); break; 
						case '\\': sb.append( "'\\\\'" ); break;
						default:
							if ( b >= ' ' ) {
								sb.append( '\'');
								sb.append( (char) b );
								sb.append( '\'');
							} else {
								sb.append( String.valueOf((int)b) );
							}
						break;
					}					
				}
				sb.append( "}" );
				return sb.toString();
			}

		sb.append('"');
		for ( int i=0; i<n; i++ ) {
			byte b = bytes[i];
			switch ( b ) {
				case '\b': sb.append( "\\b" ); break; 
				case '\f': sb.append( "\\f" ); break; 
				case '\n': sb.append( "\\n" ); break; 
				case '\r': sb.append( "\\r" ); break; 
				case '\t': sb.append( "\\t" ); break;
				case '"':  sb.append( "\\\"" ); break;
				case '\\': sb.append( "\\\\" ); break;
				default:
					if ( b >= ' ' ) {
						sb.append( (char) b ); break;
					} else {
						// convert from UTF-8
						int u = 0xff & (int) b;
						if ( u>=0xc0 && i+1<n ) {
							if ( u>=0xe0 && i+2<n ) {
								u = ((u & 0xf) << 12) | ((0x3f & bytes[i+1]) << 6) | (0x3f & bytes[i+2]);
								i+= 2;
							} else {
								u = ((u & 0x1f) << 6) | (0x3f & bytes[++i]);
							}
						}
						sb.append( "\\u" );
						sb.append( Integer.toHexString(0x10000+u).substring(1) );
					}
			}
		}
		sb.append('"');
		return sb.toString();
	}


	private String evalNameRef(NameReference v) {
		if ( v.name.isGlobal() )
			return "env.get(\""+v.name.luaname+"\")";
		else if ( v.name.isupvalue )
			return javaName(v.name)+"[0]";
		else
			return javaName(v.name);
	}
	
	private String evalField(Field e) {
		return eval(e.variable)+".get(\""+e.field+"\")";
	}

	private String evalMethod(Method e) {
		// FIXME: check api, fix this
		return eval(e.variable)+".get(\""+e.method+"\")";
	}

	private String evalParen(Parentheses e) {
		return eval(e.expression)+".arg1()";
	}

	private String evalIndex(Index e) {
		return eval(e.variable)+".get("+eval(e.expression)+")";
	}

	private String evalCallFunc(CallFunction e) {
		int n = e.parameters.size();
		boolean isVarargsReturn = e.numReturns < 0 || e.numReturns > 1;
		boolean isVarargsCall = n>0 && e.parameters.get(n-1).getNumReturns() < 0;
		String base = eval(e.variable);
		if ( n <= 3 && !isVarargsReturn && !isVarargsCall ) {
			return base+".call("+evalParamList(e.parameters)+")";
		} else {
			String coerce = e.numReturns==1? ".arg1()": "";
			switch ( n ) {
			case 0:
				return base+".invoke()"+coerce; 
			case 1:
			case 2:
			case 3:
				return base+".invoke("+evalParamList(e.parameters)+")"+coerce; 
			default:
				if ( isVarargsCall ) {
					LSExpression last = e.parameters.remove(n-1);
					return base+".invoke(new LuaValue[]{"+evalParamList(e.parameters)+"},"+eval(last)+")"+coerce;
				} else {
					return base+".invoke(new LuaValue[]{"+evalParamList(e.parameters)+"})"+coerce;
				}
			}
		}
	}

	private String evalCallMeth(CallMethod e) {
		int n = e.parameters.size();
		String base = eval(e.variable);
		if ( n <= 3 && e.numReturns == 0 || e.numReturns == 1 ) {
			return base+".method(\""+e.method+"\""+(e.parameters.size()>0? ",": "")+evalParamList(e.parameters)+")";
		} else {
			return base+".invokemethod(\""+e.method+"\",new LuaValue[]{"+evalParamList(e.parameters)+"})";
		}
	}
	
	private String evalVarargs(VarargsRef e) {
		switch ( e.numReturns ) {
		case 0: return "NIL";
		case 1: return "$args.arg1()";
		default: return "$args";
		}
	}
	
	private String evalParamList(List<LSExpression> parameters) {
		if ( parameters == null || parameters.size() == 0 )
			return "";
		StringBuffer p = new StringBuffer();
		for ( int i=0, n=parameters.size(); i<n; i++ ) {
			if (i>0) p.append(",");
			p.append( eval( parameters.get(i) ) );
		}
		return p.toString();
	}

	
	//-------------------------------------------
	// write individual strings and lines
	//-------------------------------------------


	private void writeindent() {
		for ( int i=0; i<indent; i++ )
			sb.append( "   " );
	}
	private void write( String str ) {
		sb.append( str );
	}
	private void writeln() {
		sb.append( '\n' );
	}
	private void writeln( String line ) {
		writeindent();
		write( line );
		writeln();
	}

}
