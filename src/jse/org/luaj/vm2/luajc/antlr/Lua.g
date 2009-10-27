/*
 * Lua 5.1 grammar producing typed parse tree.
 *
 * Adapted from the grammar produced by Nicolai Mainiero, May 2007
 * 
 * see http://www.antlr.org/grammar/list
 */

grammar Lua;

options {
  backtrack=true;
}

@header { 
	package org.luaj.vm2.luajc.antlr;
	import org.luaj.vm2.luajc.lst.*; 
}
@lexer::header { 
	package org.luaj.vm2.luajc.antlr;
}
@members {
	LSChunk CHK = null;
}

chunk [String chunkname] returns [LSChunk c]
	@init { CHK = new LSChunk(chunkname); } 
	: funcblock[CHK.function] {$c=CHK;}
	;

funcblock [LSFunction f]
	scope { LSFunction func; }
	@init { $funcblock::func = $f;  } 
	: {CHK.pushScope("body");} block {$f.setStatements($block.stats); CHK.popScope("body"); }
	;
	
block returns [List<LSStatement> stats]
	@init { $stats = new ArrayList<LSStatement>(); }
	: (stat {$stats.add($stat.s);} (';')?)* 
		(laststat {$stats.add($laststat.s);} (';')?)?
	;

stat returns [LSStatement s]
	@init { Name name=null; List<Name> names=null; } 
	: varlist1 '=' explist1              { $s=LSStatement.varAssignStatement($varlist1.vars,$explist1.exprs, CHK.peekScope(), $funcblock::func); }
	| functioncall                       { $s=LSStatement.functionCallStatement($functioncall.v); } 
	| 'do' {CHK.pushScope("do");} block {CHK.popScope("do");} 'end'                   
	                                     { $s=LSStatement.doBlockStatement($block.stats); } 
	| 'while' exp 'do' {CHK.pushScope("while");} block {CHK.popScope("while");} 'end'
	                                     { $s=LSStatement.whileLoopStatement($exp.e,$block.stats); } 
	| 'repeat'  {CHK.pushScope("repeat");} block  {CHK.popScope("repeat");} 'until' exp         
	                                     { $s=LSStatement.repeatUntilStatement($block.stats,$exp.e); }
	| ifstat                             { $s=$ifstat.s; }
	| 'for' {CHK.pushScope("fori");} NAME {name=CHK.declare($NAME.text);} '=' e1=exp ',' e2=exp (',' e3=exp)? 'do' {CHK.pushScope("foriblock");} block {CHK.popScope("foriblock");} 'end' 
	                                     { $s=LSStatement.forLoopStatement(name,$e1.e,$e2.e,$e3.e,$block.stats,CHK.peekScope()); CHK.popScope("fori"); }
	| 'for' {CHK.pushScope("for");}  namelist {names=CHK.declare($namelist.names);} 'in' explist1 'do' {CHK.pushScope("forblock");} block {CHK.popScope("forblock");}'end'
	                                     { $s=LSStatement.forListStatement(names,$explist1.exprs,$block.stats,CHK.peekScope(), $funcblock::func); CHK.popScope("for");}
	| 'function' funcname funcbody       { $s=LSStatement.varFunctionStatement($funcname.v,$funcbody.f); }
	| 'local' 'function' NAME {name=CHK.declare($NAME.text);} funcbody   
	                                     { $s=LSStatement.localFunctionStatement(name,$funcbody.f); }
	| 'local' namelist ('=' explist1)?   { $s=LSStatement.localAssignStatement(CHK.declare($namelist.names),$explist1.exprs,CHK.peekScope(), $funcblock::func); }
	;

ifstat returns [LSStatement s]
	scope { LSIfStatement current; }
	: 'if' e1=exp 'then'  {CHK.pushScope("if");} b1=block {$ifstat::current=new LSIfStatement($e1.e,$b1.stats); CHK.popScope("if");} 
	  ('elseif' e2=exp 'then' {CHK.pushScope("elseif");}  b2=block {$ifstat::current.addElseif($e2.e,$b2.stats); CHK.popScope("elseif");})* 
	  ('else'  {CHK.pushScope("else");} b3=block {$ifstat::current.addElse($b3.stats); CHK.popScope("else");})? 
	  'end'
	  { $s=$ifstat::current; }
	;

laststat returns [LSStatement s]
	: 'return' (e=explist1)? {$s=LSStatement.returnStatement($funcblock::func,$e.exprs);} 
	| 'break'                {$s=LSStatement.breakStatement();}
	;

funcname returns [LSVariable v]
	: n=NAME {$v = LSVariable.nameVariable(CHK.reference($n.text,$funcblock::func));} 
		('.' n2=NAME {$v = $v.fieldVariable($n2.text);})*  
		(':' n3=NAME {$v = $v.methodVariable($n3.text);})? 
	;

varlist1 returns [List<LSVariable> vars]
	@init { $vars = new ArrayList<LSVariable>(); } 
	: v1=var {$vars.add($v1.v);}
		(',' v2=var {$vars.add($v2.v);})*
	;

namelist returns [List<String> names]
	: n=NAME {$names=new ArrayList<String>(); $names.add($n.text);} 
		(',' n2=NAME {$names.add($n2.text);})*
	;

explist1 returns [List<LSExpression> exprs]
	@init { $exprs = new ArrayList<LSExpression>(); } 
	: (e1=exp ',' {$exprs.add($e1.e);})* 
		e2=exp {$exprs.add($e2.e);}
	;

exp returns [LSExpression e]
	: ('nil'               { $e=LSExpression.ENIL; }
		| 'false'          { $e=LSExpression.EFALSE; }
		| 'true'           { $e=LSExpression.ETRUE; }
		| number           { $e=LSExpression.numberExpression($number.text); }
		| string           { $e=$string.e; }
		| '...'            { $e=LSExpression.varargsRef(); $funcblock::func.setUsesVarargs(); }
		| function         { $e=LSExpression.functionExpression($function.f); }
		| prefixexp        { $e=$prefixexp.v; }
		| tableconstructor { $e=$tableconstructor.e;}
		| unop e1=exp      { $e=LSExpression.unopExpression($unop.op,$e1.e,CHK.peekScope());}
		) (binop e2=exp    { $e=LSExpression.binopExpression($e,$binop.op,$e2.e,CHK.peekScope());})* 
	;

var returns [LSVariable v]
	scope { LSVariable current; }
	: (n=NAME {$var::current=LSVariable.nameVariable(CHK.reference($n.text,$funcblock::func));} 
	    | '(' exp ')' {$var::current=LSVariable.parenthesesVariable($exp.e);} varSuffix) 
	    	varSuffix*
	  {$v=$var::current;}
	;

varSuffix
	: (n=nameAndArgs[$var::current] {$var::current=$n.v;})* 
		('[' e=exp ']'     {$var::current=$var::current.indexVariable($e.e);} 
		| '.' n2=NAME     {$var::current=$var::current.fieldVariable($n2.text);}
		)
	;

prefixexp returns [LSVariable v]
	scope { LSVariable current; }
	: e=varOrExp {$prefixexp::current=$e.v;} 
		(n=nameAndArgs[$prefixexp::current] {$prefixexp::current=$n.v;})*
		{$v=$prefixexp::current;}
	;

functioncall returns [LSVariable v]
	scope { LSVariable current; }
	: e=varOrExp {$functioncall::current=$e.v;} 
		(n=nameAndArgs[$functioncall::current] {$functioncall::current=$n.v;})+
		{$v=$functioncall::current;}
	;

varOrExp returns [LSVariable v]
	: var         {$v=$var.v;}
	| '(' exp ')' {$v=LSVariable.parenthesesVariable($exp.e);}
	;

nameAndArgs [LSVariable vin] returns [LSVariable v]
	@init { String method=null; }
	: (':' n=NAME {method=$n.text;})? 
	       a=args {$v=((method==null)? 
	       		$vin.callFuncVariable($a.exprs):
	       		$vin.callMethVariable(method,$a.exprs));}
	;

args returns [List<LSExpression> exprs]
	@init { $exprs = new ArrayList<LSExpression>(); }
	:  '(' (e=explist1 {$exprs=$e.exprs;})? ')' 
	| t=tableconstructor {$exprs.add($t.e);} 
	| s=string           {$exprs.add($s.e);}
	;

function returns [LSFunction f]
	: 'function' b=funcbody {$f = $b.f;}
	;

funcbody returns [LSFunction f]
	@init { 
		$f = new LSFunction();
		$funcblock::func.functions.add($f);
	}
	: {CHK.pushScope("func",true);} '(' (parlist1 [f])? ')' funcblock[f] 'end' {CHK.popScope("func");} 
	;

parlist1 [LSFunction f]
	: namelist {f.setParameterNames(CHK.declare($namelist.names));} (',' '...' {f.isvararg=true;})? 
	| '...' {f.isvararg=true;}
	;

tableconstructor returns [LSExpression e]
	@init { List<LSField> fields = new ArrayList<LSField>(); }
	: '{' (fieldlist[fields])? '}' {$e=LSExpression.tableConstructorExpression(fields);}
	;

fieldlist [List<LSField> fields]
	: field [fields] (fieldsep field [fields])* (fieldsep)?
	;

field [List<LSField> fields]
	: '[' k=exp ']' '=' e=exp {$fields.add(LSField.keyValueField($k.e,$e.e));}  
	| n=NAME '=' e=exp        {$fields.add(LSField.nameValueField($n.text,$e.e));}
	| e=exp                   {$fields.add(LSField.valueField($e.e));}
	;

fieldsep 
	: ',' 
	| ';'
	;

binop returns [BinOp op]
	: '+'  	{$op=BinOp.ADD;}
	| '-'  	{$op=BinOp.SUB;} 
	| '*'  	{$op=BinOp.MUL;} 
	| '/'  	{$op=BinOp.DIV;} 
	| '^'  	{$op=BinOp.POW;} 
	| '%'  	{$op=BinOp.MOD;} 
	| '..' 	{$op=BinOp.CONCAT;} 
	| '<'  	{$op=BinOp.LT;}
	| '<=' 	{$op=BinOp.LTEQ;} 
	| '>'  	{$op=BinOp.GT;}
	| '>='  {$op=BinOp.GTEQ;} 
	| '=='  {$op=BinOp.EQ;} 
	| '~='  {$op=BinOp.NEQ;} 
	| 'and' {$op=BinOp.AND; $funcblock::func.hasandlogic=true;} 
	| 'or'  {$op=BinOp.OR;  $funcblock::func.hasorlogic=true;}
	;

unop  returns [UnOp op]
	: '-'    {$op=UnOp.NEG;}
	| 'not'  {$op=UnOp.NOT;} 
	| '#'    {$op=UnOp.LEN;}
	;

number 
	: ('-')? INT 
	| ('-')? FLOAT1 
	| ('-')? FLOAT2 
	| ('-')? FLOAT3 
	| ('-')? EXP 
	| HEX
	;

string returns [LSExpression e]	
	: NORMALSTRING {$e=LSExpression.normalStringExpression($NORMALSTRING.text);}
	| CHARSTRING   {$e=LSExpression.charStringExpression($CHARSTRING.text);}
	| LONGSTRING   {$e=LSExpression.longStringExpression($LONGSTRING.text);}
	;


// LEXER


NAME	:('a'..'z'|'A'..'Z'|'_')(options{greedy=true;}:	'a'..'z'|'A'..'Z'|'_'|'0'..'9')*
	;

INT	: ('0'..'9')+;

FLOAT1 	:'.' INT ;

FLOAT2 	:INT '.' ;

FLOAT3 	:INT '.' INT ;

EXP	: (INT | FLOAT1 | FLOAT2 | FLOAT3) ('E'|'e') ('-'|'+')? INT;

HEX	:'0' ('x' | 'X') ('0'..'9'| 'a'..'f' | 'A'..'F')+ ;

	

NORMALSTRING
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"' 
    ;

CHARSTRING
   :	'\'' ( EscapeSequence | ~('\\'|'\'') )* '\''
   ;

LONGSTRING
	:	'['('=')*'[' ( EscapeSequence | ~('\\'|']') )* ']'('=')*']'
	;

fragment
EscapeSequence
    :   '\\' ('a'|'b'|'f'|'n'|'r'|'t'|'v'|'\"'|'\''|'\\'|'\n')
    |   DecimalEscape
    ;
    
fragment
DecimalEscape
    :   '\\' ('0'..'9') (('0'..'9') ('0'..'9')?)?
    ;
    
    
COMMENT
    :   '--[[' ( options {greedy=false;} : . )* '--]]' {skip();}
    |   '--[=[' ( options {greedy=false;} : . )* '--]==]' {skip();}
    |   '--[==[' ( options {greedy=false;} : . )* '--]==]' {skip();}
    |   '--[===[' ( options {greedy=false;} : . )* '--]===]' {skip();}
    ;
    
LINE_COMMENT
    : '--' ~('\n'|'\r')* '\r'? '\n' {skip();}
    ;
    
    
WS  :  (' '|'\t'|'\u000C') {skip();}
    ;
    
NEWLINE	: ('\r')? '\n' {skip();}
	;
