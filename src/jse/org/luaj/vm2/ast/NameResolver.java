package org.luaj.vm2.ast;

import java.util.List;

import org.luaj.vm2.ast.Exp.NameExp;
import org.luaj.vm2.ast.Exp.VarExp;
import org.luaj.vm2.ast.NameScope.NamedVariable;
import org.luaj.vm2.ast.Stat.Assign;
import org.luaj.vm2.ast.Stat.GenericFor;
import org.luaj.vm2.ast.Stat.LocalAssign;
import org.luaj.vm2.ast.Stat.LocalFuncDef;
import org.luaj.vm2.ast.Stat.NumericFor;

/** 
 * Visitor that resolves names to scopes.
 * Each Name is resolved to a NamedVarible, possibly in a NameScope 
 * if it is a local, or in no named scope if it is a global. 
 */
public class NameResolver extends Visitor {

	private NameScope scope = null;

	private void pushScope() {
		scope = new NameScope(scope);
	}
	private void popScope() {
		scope = scope.outerScope;
	}
	
	public void visit(NameScope scope) {
	}	

	public void visit(Block block) {
		pushScope();
		block.scope = scope;
		super.visit(block);
		popScope();
	}
	
	public void visit(FuncBody body) {
		pushScope();
		scope.functionNestingCount++;
		body.scope = scope;
		if ( body.parlist.names != null )
			for ( Name n : body.parlist.names )
				defineLocalVar(n);
		super.visit(body);
		popScope();
	}

	public void visit(LocalFuncDef stat) {
		defineLocalVar(stat.name);
		super.visit(stat);
	}

	public void visit(NumericFor stat) {
		pushScope();
		stat.scope = scope;
		defineLocalVar(stat.name);
		super.visit(stat);
		popScope();
	}

	public void visit(GenericFor stat) {
		pushScope();
		stat.scope = scope;
		defineLocalVars( stat.names );
		super.visit(stat);
		popScope();
	}

	public void visit(NameExp exp) {
		NamedVariable v = scope.find(exp.name.name);
		exp.name.variable = v;
		if ( v.isLocal() && scope.functionNestingCount != v.definingScope.functionNestingCount )
			v.isupvalue = true;
		super.visit(exp);
	}
	
	public void visit(Assign stat) {
		super.visit(stat);
		for ( VarExp v : stat.vars )
			v.markHasAssignment();
	}

	public void visit(LocalAssign stat) {
		defineLocalVars( stat.names );
		super.visit(stat);
	}

	protected void defineLocalVars(List<Name> names) {
		for ( Name n : names ) 
			defineLocalVar(n);
	}

	protected void defineLocalVar(Name name) {
		name.variable = scope.define(name.name);
	}
}
