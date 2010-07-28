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
package org.luaj.vm2.ast;

import java.util.List;

abstract public class Visitor {
	public void visit(Chunk chunk) { 
		chunk.block.accept(this); 
	};
	public void visit(Block block) {
		visit(block.scope);
		if ( block.stats != null )
			for ( Stat s: block.stats )
				s.accept(this);
	};
	public void visit(Stat.Assign stat) {
		visitVars(stat.vars);
		visitExps(stat.exps);
	}
	public void visit(Stat.Break breakstat) {
	}
	public void visit(Stat.FuncCallStat stat) {
		stat.funccall.accept(this);
	}
	public void visit(Stat.FuncDef stat) {
		stat.body.accept(this);
	}
	public void visit(Stat.GenericFor stat) {
		visit(stat.scope);
		visitNames(stat.names);
		visitExps(stat.exps);
		stat.block.accept(this);
	}
	public void visit(Stat.IfThenElse stat) {
		stat.ifexp.accept(this);
		stat.ifblock.accept(this);
		if ( stat.elseifblocks != null ) 
			for ( int i=0, n=stat.elseifblocks.size(); i<n; i++ ) {
				stat.elseifexps.get(i).accept(this);
				stat.elseifblocks.get(i).accept(this);
			}
		if ( stat.elseblock != null )
			visit( stat.elseblock );
	}
	public void visit(Stat.LocalAssign stat) {
		visitNames(stat.names);
		visitExps(stat.values);
	}
	public void visit(Stat.LocalFuncDef stat) {
		visit(stat.name);
		stat.body.accept(this);
	}
	public void visit(Stat.NumericFor stat) {
		visit(stat.scope);
		visit(stat.name);
		stat.initial.accept(this);
		stat.limit.accept(this);
		if ( stat.step != null )
			stat.step.accept(this);
		stat.block.accept(this);
	}
	public void visit(Stat.RepeatUntil stat) {
		stat.block.accept(this);
		stat.exp.accept(this);
	}
	public void visit(Stat.Return stat) {
		visitExps(stat.values);
	}
	public void visit(Stat.WhileDo stat) {
		stat.exp.accept(this);
		stat.block.accept(this);
	}
	public void visit(FuncBody body) {
		visit(body.scope);
		body.parlist.accept(this);
		body.block.accept(this);
	}
	public void visit(FuncArgs args) {
		visitExps(args.exps);
	}
	public void visit(TableField field) {
		if ( field.name != null );
			visit( field.name );
		if ( field.index != null )
			field.index.accept(this);
		field.rhs.accept(this);
	}
	public void visit(Exp.AnonFuncDef exp) {
		exp.body.accept(this);
	}
	public void visit(Exp.BinopExp exp) {
		exp.lhs.accept(this);
		exp.rhs.accept(this);
	}
	public void visit(Exp.Constant exp) {
	}
	public void visit(Exp.FieldExp exp) {
		exp.lhs.accept(this);
		visit(exp.name);
	}
	public void visit(Exp.FuncCall exp) {
		exp.lhs.accept(this);
		exp.args.accept(this);
	}
	public void visit(Exp.IndexExp exp) {
		exp.lhs.accept(this);
		exp.exp.accept(this);
	}
	public void visit(Exp.MethodCall exp) {
		exp.lhs.accept(this);
		visit(exp.name);
		exp.args.accept(this);
	}
	public void visit(Exp.NameExp exp) {
		visit(exp.name);
	}
	public void visit(Exp.ParensExp exp) {
		exp.exp.accept(this);
	}
	public void visit(Exp.UnopExp exp) {
		exp.rhs.accept(this);
	}
	public void visit(Exp.VarargsExp exp) {
	}
	public void visit(ParList pars) {
		visitNames(pars.names);
	}
	public void visit(TableConstructor table) {
		if( table.fields != null)
			for ( TableField f: table.fields )
				f.accept(this);
	}
	public void visitVars(List<Exp.VarExp> vars) {
		if ( vars != null )
			for ( Exp.VarExp v: vars )
				v.accept(this);
	}
	public void visitExps(List<Exp> exps) {
		if ( exps != null )
			for ( Exp e: exps )
				e.accept(this);
	}
	public void visitNames(List<Name> names) {
		if ( names != null )
			for ( Name n: names )
				visit(n);
	}
	public void visit(Name name) {
	}
	public void visit(String name) {
	}
	public void visit(NameScope scope) {
	}
}
