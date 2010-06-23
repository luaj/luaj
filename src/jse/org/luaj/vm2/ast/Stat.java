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

abstract
public class Stat {
	public abstract void accept(Visitor visitor);

	public static Stat block(Block block) {
		return block;
	}

	public static Stat whiledo(Exp exp, Block block) {
		return new WhileDo(exp, block);
	}

	public static Stat repeatuntil(Block block, Exp exp) {
		return new RepeatUntil(block, exp);
	}	
	
	public static Stat breakstat() {
		return new Break();
	}

	public static Stat returnstat(List<Exp> exps) {
		return new Return(exps);
	}

	public static Stat assignment(List<VarExp> vars, List<Exp> exps) {
		return new Assign(vars,exps);
	}

	public static Stat functioncall(FuncCall funccall) {
		return new FuncCallStat(funccall);
	}

	public static Stat localfunctiondef(String name, FuncBody funcbody) {
		return new LocalFuncDef(name, funcbody);
	}

	public static Stat fornumeric(String name, Exp initial, Exp limit, Exp step, Block block) {
		return new NumericFor(name, initial, limit, step, block);
	}

	public static Stat functiondef(FuncName funcname, FuncBody funcbody) {
		return new FuncDef( funcname, funcbody );
	}

	public static Stat forgeneric(List<Name> names, List<Exp> exps, Block block) {
		return  new GenericFor(names, exps, block);
	}

	public static Stat localassignment(List<Name> names, List<Exp> values) {
		return new LocalAssign(names, values);
	}

	public static Stat ifthenelse(Exp ifexp, Block ifblock, List<Exp> elseifexps, List<Block> elseifblocks, Block elseblock) {
		return new IfThenElse(ifexp, ifblock, elseifexps, elseifblocks, elseblock);
	}

	public static class Assign extends Stat {
		public final List<VarExp> vars;
		public final List<Exp> exps;
		
		public Assign(List<VarExp> vars, List<Exp> exps) {
			this.vars = vars;
			this.exps = exps;
		}

		public void accept(Visitor visitor) {
			visitor.visit(this);
		}

	}

	public static class WhileDo extends Stat {
		public final Exp exp;
		public final Block block;
		public WhileDo( Exp exp, Block block ) {
			this.exp = exp;
			this.block = block;
		}
		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}	
	
	public static class RepeatUntil extends Stat {
		public final Block block;
		public final Exp exp;
		public RepeatUntil( Block block, Exp exp ) {
			this.block = block;
			this.exp = exp;
		}
		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}

	public static class Break extends Stat {
		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}

	public static class Return extends Stat {
		public final List<Exp> values;
		public Return(List<Exp> values) {
			this.values = values;
		}

		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}

	public static class FuncCallStat extends Stat {
		public final FuncCall funccall;
		public FuncCallStat(FuncCall funccall) {
			this.funccall = funccall;
		}

		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}

	public static class LocalFuncDef extends Stat {
		public final Name name;
		public final FuncBody body;
		public LocalFuncDef(String name, FuncBody body) {
			this.name = new Name(name);
			this.body = body;
		}

		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}

	public static class FuncDef extends Stat {
		public final FuncName name;
		public final FuncBody body;
		public FuncDef(FuncName name, FuncBody body) {
			this.name = name;
			this.body = body;
		}

		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}

	public static class GenericFor extends Stat {
		public List<Name> names;
		public List<Exp> exps;
		public Block block;
		public GenericFor(List<Name> names, List<Exp> exps, Block block) {
			this.names = names;
			this.exps = exps;
			this.block = block;
		}

		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}

	public static class NumericFor extends Stat {
		public final Name name;
		public final Exp initial,limit,step;
		public final Block block;
		public NumericFor(String name, Exp initial, Exp limit, Exp step, Block block) {
			this.name = new Name(name);
			this.initial = initial;
			this.limit = limit;
			this.step = step;
			this.block = block;
		}

		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}

	public static class LocalAssign extends Stat {
		public final List<Name> names;
		public final List<Exp> values;
		public LocalAssign(List<Name> names, List<Exp> values) {
			this.names = names;
			this.values = values;
		}

		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}

	public static class IfThenElse extends Stat {
		public final Exp ifexp;
		public final Block ifblock;
		public final List<Exp> elseifexps;
		public final List<Block> elseifblocks;
		public final Block elseblock;
		public IfThenElse(Exp ifexp, Block ifblock, List<Exp> elseifexps,
				List<Block> elseifblocks, Block elseblock) {
			this.ifexp = ifexp;
			this.ifblock = ifblock;
			this.elseifexps = elseifexps;
			this.elseifblocks = elseifblocks;
			this.elseblock = elseblock;
		}

		public void accept(Visitor visitor) {
			visitor.visit( this );
		}
	}
}
