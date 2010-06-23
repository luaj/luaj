package org.luaj.vm2.ast;

abstract public class Visitor {
	public void visit(Chunk chunk) { 
		chunk.block.accept(this); 
	};
	public void visit(Block block) {
		if ( block.stats != null )
			for ( Stat s : block.stats )
				s.accept( this );
	};
	public void visit(Stat.Assign assign) {
	}
	public void visit(Stat.Break breakstat) {
	}
	public void visit(Stat.FuncCallStat stat) {
	}
	public void visit(Stat.FuncDef stat) {
	}
	public void visit(Stat.GenericFor stat) {
	}
	public void visit(Stat.IfThenElse stat) {
	}
	public void visit(Stat.LocalAssign stat) {
	}
	public void visit(Stat.LocalFuncDef stat) {
	}
	public void visit(Stat.NumericFor stat) {
	}
	public void visit(Stat.RepeatUntil stat) {
	}
	public void visit(Stat.Return stat) {
	}
	public void visit(Stat.WhileDo stat) {
	}
	public void visit(FuncCall funcCall) {
	}
	public void visit(FuncBody funcBody) {
	}
	public void visit(FuncArgs funcArgs) {
	}
	public void visit(Field field) {
	}
	public void visit(Exp exp) {
	}
	public void visit(Exp.AnonFuncDef exp) {
	}
	public void visit(Exp.BinopExp exp) {
	}
	public void visit(Exp.Constant exp) {
	}
	public void visit(Exp.UnopExp exp) {
	}
	public void visit(Exp.VarargsExp exp) {
	}
	public void visit(PrimaryExp exp) {
	}
	public void visit(ParList parList) {
	}
	public void visit(PostfixOp postfixOp) {
	}
	public void visit(VarExp exp) {
	}
}
