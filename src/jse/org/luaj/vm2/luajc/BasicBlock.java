/**
 * 
 */
package org.luaj.vm2.luajc;

import java.util.Hashtable;
import java.util.Vector;

import org.luaj.vm2.Lua;
import org.luaj.vm2.Prototype;

public class BasicBlock {
	int pc0,pc1;        // range of program counter values for the block
	BasicBlock[] prev;  // previous basic blocks (0-n of these)
	BasicBlock[] next;  // next basic blocks (0, 1, or 2 of these)
	
	public BasicBlock(Prototype p, int pc0) {
		this.pc0 = this.pc1 = pc0;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();		
		sb.append( (pc0+1)+"-"+(pc1+1)
				+(prev!=null? "  prv: "+str(prev,1): "")
				+(next!=null? "  nxt: "+str(next,0): "")
				+"\n" );
		return sb.toString();
	}
	
	private String str(BasicBlock[] b, int p) {
		if ( b == null )
			return "";
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for ( int i=0, n=b.length; i<n; i++ ) {
			if ( i > 0 )
				sb.append( "," );
			sb.append( String.valueOf( p==1? b[i].pc1+1: b[i].pc0+1 ) );
		}
		sb.append(")");
		return sb.toString();
	}

	public static BasicBlock[] findBasicBlocks(Prototype p) {
		
		// mark beginnings, endings
		final int n = p.code.length;
		final boolean[] isbeg = new boolean[n];
		final boolean[] isend = new boolean[n];
		isbeg[0] = true;
		BranchVisitor bv = new BranchVisitor(isbeg) {
			public void visitBranch(int pc0, int pc1) {
				isend[pc0] = true;
				isbeg[pc1] = true;
			}
			public void visitReturn(int pc) {
				isend[pc] = true;
			}
		};
		visitBranches(p, bv); // 1st time to mark branches
		visitBranches(p, bv); // 2nd time to catch merges
			
		// create basic blocks
		final BasicBlock[] blocks = new BasicBlock[n];
		for ( int i=0; i<n; i++ ) {
			isbeg[i] = true;
			BasicBlock b = new BasicBlock(p,i);
			blocks[i] = b;
			while ( !isend[i] && i+1<n && !isbeg[i+1] )
				blocks[b.pc1=++i] = b;
		}
	
		// count previous, next
		final int[] nnext = new int[n];
		final int[] nprev = new int[n];
		visitBranches(p, new BranchVisitor(isbeg) {
			public void visitBranch(int pc0, int pc1) {
				nnext[pc0]++;
				nprev[pc1]++;
			}
		});
		
		// allocate and cross-reference
		visitBranches( p, new BranchVisitor(isbeg) {
			public void visitBranch(int pc0, int pc1) {
				if ( blocks[pc0].next == null ) blocks[pc0].next = new BasicBlock[nnext[pc0]];
				if ( blocks[pc1].prev == null ) blocks[pc1].prev = new BasicBlock[nprev[pc1]];
				blocks[pc0].next[--nnext[pc0]] = blocks[pc1];
				blocks[pc1].prev[--nprev[pc1]] = blocks[pc0];
			}
		});
		return blocks;
	}
	
	abstract public static class BranchVisitor {
		final boolean[] isbeg;
		public BranchVisitor(boolean[] isbeg) {
			this.isbeg = isbeg;
		}
		public void visitBranch( int frompc, int topc ) {}
		public void visitReturn( int atpc ) {}
	}
	
	public static void visitBranches( Prototype p, BranchVisitor visitor ) {
		int sbx,j,c;
		int[] code = p.code;
		int n = code.length;
		for ( int i=0; i<n; i++ ) {
			int ins = code[i];
			switch ( Lua.GET_OPCODE( ins ) ) {
			case Lua.OP_LOADBOOL:
				if ( 0 == Lua.GETARG_C(ins) )
					break;
				if ( Lua.GET_OPCODE(code[i+1]) == Lua.OP_JMP  )
					throw new IllegalArgumentException("OP_LOADBOOL followed by jump at "+i);
				visitor.visitBranch( i, i+2 );
				continue;
			case Lua.OP_EQ:
			case Lua.OP_LT:
			case Lua.OP_LE:
			case Lua.OP_TEST: 
			case Lua.OP_TESTSET:
			case Lua.OP_TFORLOOP:
				if ( Lua.GET_OPCODE(code[i+1]) != Lua.OP_JMP  )
					throw new IllegalArgumentException("test not followed by jump at "+i); 
				sbx = Lua.GETARG_sBx(code[i+1]);
				++i;
				j = i + sbx + 1;
				visitor.visitBranch( i, j );
				visitor.visitBranch( i, i+1 ); 				
				continue;
			case Lua.OP_FORLOOP:
				sbx = Lua.GETARG_sBx(ins);
				j = i + sbx + 1;
				visitor.visitBranch( i, j );
				visitor.visitBranch( i, i+1 ); 				
				continue;
			case Lua.OP_JMP:
			case Lua.OP_FORPREP:
				sbx = Lua.GETARG_sBx(ins);
				j = i + sbx + 1;
				visitor.visitBranch( i, j );
				continue;
			case Lua.OP_TAILCALL:
			case Lua.OP_RETURN:
				visitor.visitReturn( i );
				continue;
			}
			if ( i+1<n && visitor.isbeg[i+1] )
				visitor.visitBranch( i, i+1 );
		}
	}

	public static BasicBlock[] sortDepthFirst(BasicBlock[] blocks) {
		Vector list = new Vector();
		Hashtable seen = new Hashtable();
		Vector next = new Vector();
		BasicBlock b0,b1,b[];

		next.addElement( blocks[0] );
		seen.put( blocks[0], Boolean.TRUE );
		while ( (b = toBasicBlockArray(next)) != null ) {
			next.clear();
			for ( int i=0; i<b.length; i++ ) {
				list.addElement( b0 = b[i] );
				for ( int k=0, n=b0.next!=null? b0.next.length: 0; k<n; k++ ) {
					if ( seen.get( b1 = b0.next[k] ) == null ) {
						seen.put( b1, Boolean.TRUE );
						next.addElement( b1 );
					}
				}
			}
		}
		return toBasicBlockArray(list);
	}

	private static BasicBlock[] toBasicBlockArray(Vector list) {
		if ( list.size() <= 0 )
			return null;
		BasicBlock[] array = new BasicBlock[list.size()];
		list.copyInto(array);
		return array;
	}

}