package org.luaj.vm2.luajc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.luaj.vm2.Lua;
import org.luaj.vm2.Print;
import org.luaj.vm2.Prototype;

/**
 * Prototype information for static single-assignment analysis
 */
public class ProtoInfo {

	public final String name;
	public final Prototype prototype;     // the prototype that this info is about
	public final ProtoInfo[] subprotos;   // one per enclosed prototype, or null
	public final BasicBlock[] blocks;     // basic block analysis of code branching
	public final BasicBlock[] blocklist;  // blocks in breadhth-first order
	public final VarInfo[][] vars;        // Each variable
	public final VarInfo[] params;        // Parameters and initial values of stack variables
	public final UpvalInfo[] upvals;      // from outer scope
	public final UpvalInfo[][] openups;   // per slot, upvalues allocated by this prototype

	
	public ProtoInfo(Prototype p, String name) {
		this(p,name,null);
	}
	
	private ProtoInfo(Prototype p, String name, UpvalInfo[] u) {
		this.name = name;
		this.prototype = p;
		this.upvals = u;
		this.subprotos = p.p!=null&&p.p.length>0? new ProtoInfo[p.p.length]: null;
		
		// find basic blocks
		this.blocks = BasicBlock.findBasicBlocks(p);
		this.blocklist = BasicBlock.sortDepthFirst(blocks);

		
		// params are inputs to first block
		this.params = new VarInfo[p.maxstacksize];
		for ( int slot=0; slot<p.maxstacksize; slot++ ) {
			VarInfo v = VarInfo.PARAM(slot);
			params[slot] = v;
			this.blocklist[0].mergeSlotInput(slot, v);
		}
		
		// find variables and block inputs
		this.vars = findVariables();
		findBasicBlockInputs();

		// find upvalues, create sub-prototypes
		this.openups = new UpvalInfo[p.maxstacksize][];
		findUpvalues();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		// prototpye name
		sb.append( "proto '"+name+"'\n" );
		
		// upvalues from outer scopes
		for ( int i=0, n=(upvals!=null? upvals.length: 0); i<n; i++ )
			sb.append( " up["+i+"]: "+upvals[i]+"\n" );
		
		// basic blocks
		for ( int i=0; i<blocklist.length; i++ ) {
			sb.append( "  block "+blocklist[i].toString() );
			sb.append( "\n" );
			
			// instructions
			for ( int pc=blocklist[i].pc0; pc<=blocklist[i].pc1; pc++ ) {

				// open upvalue storage
				for ( int j=0; j<prototype.maxstacksize; j++ ) {
					if ( vars[j][pc].pc == pc && vars[j][pc].allocupvalue ) {
						sb.append( "    open: "+vars[j][pc].upvalue+"\n" );
					}
				}
				
				// opcode
				sb.append( "     " );
				for ( int j=0; j<prototype.maxstacksize; j++ ) {
					VarInfo v = vars[j][pc];
					String u = (v.upvalue!=null? !v.upvalue.rw? "[C] ": (v.allocupvalue&&v.pc==pc? "[*] ": "[]  "): "    ");
					String s = String.valueOf(v);
					sb.append( s+u);
				}
				sb.append( "  " );
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ops = Print.ps;
				Print.ps = new PrintStream(baos);
				try {
					Print.printOpCode(prototype, pc);
				} finally {
					Print.ps.close();
					Print.ps = ops;					
				}
				sb.append( baos.toString() );
				sb.append( "\n" );
			}
		}
		
		// nested functions
		for ( int i=0, n=subprotos!=null? subprotos.length: 0; i<n; i++ ) {
			sb.append( subprotos[i].toString() );
		}
		
		return sb.toString();
	}

	private VarInfo[][] findVariables() {
		
		// create storage for variables.
		int n = prototype.code.length;
		int m = prototype.maxstacksize;
		VarInfo[][] v = new VarInfo[m][];
		for ( int i=0; i<v.length; i++ )
			v[i] = new VarInfo[n];		
		
		// process instructions
		for ( int pc=0; pc<n; pc++ ) {
			
			// propogate previous value except at block boundaries
			if ( pc>0 && blocks[pc].pc0 != pc )
				for ( int j=0; j<m; j++ )
					v[j][pc] = v[j][pc-1];
			
			// account for assignments and invalidations
			int a,b,c;
			int ins = prototype.code[pc];
			switch ( Lua.GET_OPCODE( ins ) ) {
			case Lua.OP_MOVE:/*	A B	R(A) := R(B)					*/				
			case Lua.OP_LOADK:/*	A Bx	R(A) := Kst(Bx)					*/
			case Lua.OP_LOADBOOL:/*	A B C	R(A) := (Bool)B; if (C) pc++			*/
			case Lua.OP_GETUPVAL: /*	A B	R(A) := UpValue[B]				*/
			case Lua.OP_GETGLOBAL: /*	A Bx	R(A) := Gbl[Kst(Bx)]				*/
			case Lua.OP_GETTABLE: /*	A B C	R(A) := R(B)[RK(C)]				*/
			case Lua.OP_NEWTABLE: /*	A B C	R(A) := {} (size = B,C)				*/
			case Lua.OP_ADD: /*	A B C	R(A) := RK(B) + RK(C)				*/
			case Lua.OP_SUB: /*	A B C	R(A) := RK(B) - RK(C)				*/
			case Lua.OP_MUL: /*	A B C	R(A) := RK(B) * RK(C)				*/
			case Lua.OP_DIV: /*	A B C	R(A) := RK(B) / RK(C)				*/
			case Lua.OP_MOD: /*	A B C	R(A) := RK(B) % RK(C)				*/
			case Lua.OP_POW: /*	A B C	R(A) := RK(B) ^ RK(C)				*/
			case Lua.OP_UNM: /*	A B	R(A) := -R(B)					*/
			case Lua.OP_NOT: /*	A B	R(A) := not R(B)				*/
			case Lua.OP_LEN: /*	A B	R(A) := length of R(B)				*/
			case Lua.OP_CONCAT: /*	A B C	R(A) := R(B).. ... ..R(C)			*/
			case Lua.OP_TESTSET: /*	A B C	if (R(B) <=> C) then R(A) := R(B) else pc++	*/ 
			case Lua.OP_FORPREP: /*	A sBx	R(A)-=R(A+2); pc+=sBx				*/
				a = Lua.GETARG_A( ins );
				v[a][pc] = new VarInfo(a,pc);
				break;
			case Lua.OP_SELF: /*	A B C	R(A+1) := R(B); R(A) := R(B)[RK(C)]		*/
				a = Lua.GETARG_A( ins );
				v[a][pc] = new VarInfo(a,pc);
				v[a+1][pc] = new VarInfo(a+1,pc);
				break;
			case Lua.OP_FORLOOP: /*	A sBx	R(A)+=R(A+2);
				if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }*/
				a = Lua.GETARG_A( ins );
				v[a][pc] = new VarInfo(a,pc);
				v[a+3][pc] = new VarInfo(a+1,pc);
				break;
			case Lua.OP_LOADNIL: /*	A B	R(A) := ... := R(B) := nil			*/
				a = Lua.GETARG_A( ins );
				b = Lua.GETARG_B( ins );
				for ( ; a<=b; a++ )
					v[a][pc] = new VarInfo(a,pc);
				break;
			case Lua.OP_VARARG: /*	A B	R(A), R(A+1), ..., R(A+B-1) = vararg		*/			
				a = Lua.GETARG_A( ins );
				b = Lua.GETARG_B( ins );
				for ( int j=1; j<b; j++, a++ )
					v[a][pc] = new VarInfo(a,pc);
				if ( b == 0 ) 
					for ( ; a<m; a++ )
						v[a][pc] = VarInfo.INVALID;
				break;
			case Lua.OP_CALL: /*	A B C	R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1)) */
				a = Lua.GETARG_A( ins );
				c = Lua.GETARG_C( ins );
				for ( int j=0; j<=c-2; j++, a++ )
					v[a][pc] = new VarInfo(a,pc);
				for ( ; a<m; a++ )
					v[a][pc] = VarInfo.INVALID;
				break;
			case Lua.OP_TFORLOOP: /*	A C	R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2)); 
			                        if R(A+3) ~= nil then R(A+2)=R(A+3) else pc++	*/ 
				a = Lua.GETARG_A( ins );
				c = Lua.GETARG_C( ins );
				a += 3;
				for ( int j=0; j<c; j++, a++ )
					v[a][pc] = new VarInfo(a,pc);
				for ( ; a<m; a++ )
					v[a][pc] = VarInfo.INVALID;
				break;
			case Lua.OP_CLOSURE: /*	A Bx	R(A) := closure(KPROTO[Bx], R(A), ... ,R(A+n))	*/
				a = Lua.GETARG_A( ins );
				b = Lua.GETARG_Bx( ins );
				v[a][pc] = new VarInfo(a,pc);
				for ( int k=prototype.p[b].nups; --k>=0; ) {
					++pc;
					for ( int j=0; j<m; j++ )
						v[j][pc] = v[j][pc-1];
				}
				break;
			case Lua.OP_CLOSE:
				a = Lua.GETARG_A( ins );
				for ( ; a<m; a++ )
					v[a][pc] = VarInfo.INVALID;
				break;
			}
		}
		return v;
	}

	private void findBasicBlockInputs() {
		
		for ( boolean changed=true; changed; ) {
			changed = false;
			
			// send inputs to next stage
			for ( int i=0; i<blocklist.length; i++ ) {
				BasicBlock b0 = blocklist[i];
				for ( int k=0, n=b0.next!=null? b0.next.length: 0; k<n; k++ ) {
					BasicBlock b1 = b0.next[k];
					for ( int slot=0; slot<prototype.maxstacksize; slot++ ) {
						VarInfo v = vars[slot][b0.pc1];
						if ( v != null ) {
							if ( b1.mergeSlotInput(slot, v) ) {
								if ( vars[slot][b1.pc0] == null )
									if ( b1.ninputs[slot] == 2 || v == VarInfo.INVALID )
										createInputVar( b1, slot );
								changed = true;
							}
						}
					}
				}
			}
			
			// propogate up to one more variable per slot
			if ( ! changed ) {
				eachslot: for ( int slot=0; slot<prototype.maxstacksize; slot++ ) {
					for ( int i=0; i<blocklist.length; i++ ) {
						BasicBlock b0 = blocklist[i];
						if ( vars[slot][b0.pc0] == null ) {
							createInputVar( b0, slot );
							changed = true;
							continue eachslot;
						}
					}
				}
			}
		}
	}

	private void createInputVar(BasicBlock b, int slot) {
		int n = b.ninputs[slot];
		VarInfo v = 
			n==-1? VarInfo.INVALID: 
			n==0? VarInfo.NIL(slot): 
			n==1? b.inputs[slot][0]: 
				VarInfo.PHI(slot,b.pc0);
		for ( int pc=b.pc0; pc<=b.pc1 && vars[slot][pc] == null; ++pc )
			vars[slot][pc] = v;
	}
	
	private void findUpvalues() {
		int[] code = prototype.code;
		int n = code.length;
		
		// propogate to inner prototypes
		for ( int pc=0; pc<n; pc++ ) {
			if ( Lua.GET_OPCODE(code[pc]) == Lua.OP_CLOSURE ) {
				int bx = Lua.GETARG_Bx(code[pc]);
				Prototype newp = prototype.p[bx];
				UpvalInfo[] newu = newp.nups>0? new UpvalInfo[newp.nups]: null;
				String newname = name + "$" + bx;
				for ( int j=0; j<newp.nups; ++j ) {
					int i = code[++pc];
					int b = Lua.GETARG_B(i);
					newu[j] = (i&4) != 0? upvals[b]: findOpenUp(pc,b);
				}
				subprotos[bx] = new ProtoInfo(newp, newname, newu);
			}
		}
		
		// mark all upvalues that are written locally as read/write
		for ( int pc=0; pc<n; pc++ ) {
			if ( Lua.GET_OPCODE(code[pc]) == Lua.OP_SETUPVAL )
				upvals[Lua.GETARG_B(code[pc])].rw = true;
		}
	}
	
	private UpvalInfo findOpenUp(int pc, int slot) {
		if ( openups[slot] == null )
			openups[slot] = new UpvalInfo[prototype.code.length];
		if ( openups[slot][pc] != null )
			return openups[slot][pc];
		return new UpvalInfo(this, pc, slot);
	}

	public boolean isUpvalueAssign(int pc, int slot) {
		VarInfo v = pc<0? params[slot]: vars[slot][pc];
//		return v.upvalue != null && v.upvalue.rw;
		return v != null && v.upvalue != null;
	}

	public boolean isUpvalueCreate(int pc, int slot) {
		VarInfo v = pc<0? params[slot]: vars[slot][pc];
//		return v.upvalue != null && v.upvalue.rw && v.allocupvalue && pc == v.pc;
		return v != null && v.upvalue != null && v.allocupvalue && pc == v.pc;
	}

	public boolean isUpvalueRefer(int pc, int slot) {
		// TODO: when it is a CALL
		VarInfo v = pc<0? params[slot]: vars[slot][pc];
//		return v.upvalue != null && v.upvalue.rw;
		return v != null && v.upvalue != null;
	}

	public boolean isInitialValueUsed(int slot) {
		return true;
	}
}
