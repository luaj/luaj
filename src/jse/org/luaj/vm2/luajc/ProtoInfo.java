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
	public final BasicBlock[] blocklist;  // blocks in breadth-first order
	public final VarInfo[] params;        // Parameters and initial values of stack variables
	public final VarInfo[][] vars;        // Each variable
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
		}
		
		// find variables
		this.vars = findVariables();
		replaceTrivialPhiVariables();

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
		for ( int pc0=blocks[0].pc0; pc0<prototype.code.length; pc0=blocks[pc0].pc1+1 ) {
			BasicBlock b = blocks[pc0];
			sb.append( "  block "+b.toString() );
			appendOpenUps( sb, -1 );
			
			// instructions
			for ( int pc=pc0; pc<=b.pc1; pc++ ) {
	
				// open upvalue storage
				appendOpenUps( sb, pc );
				
				// opcode
				sb.append( "     " );
				for ( int j=0; j<prototype.maxstacksize; j++ ) {
					VarInfo v = vars[j][pc];
					String u = (v==null? "": v.upvalue!=null? !v.upvalue.rw? "[C] ": (v.allocupvalue&&v.pc==pc? "[*] ": "[]  "): "    ");
					String s = v==null? "null   ": String.valueOf(v);
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

	private void appendOpenUps(StringBuffer sb, int pc) {
		for ( int j=0; j<prototype.maxstacksize; j++ ) {
			VarInfo v = (pc<0? params[j]: vars[j][pc]);
			if ( v != null && v.pc == pc && v.allocupvalue ) {
				sb.append( "    open: "+v.upvalue+"\n" );
			}
		}
	}

	private VarInfo[][] findVariables() {
		
		// create storage for variables.
		int n = prototype.code.length;
		int m = prototype.maxstacksize;
		VarInfo[][] v = new VarInfo[m][];
		for ( int i=0; i<v.length; i++ )
			v[i] = new VarInfo[n];		
		
		// process instructions
		for ( int bi=0; bi<blocklist.length; bi++ ) {
			BasicBlock b0 = blocklist[bi];
			
			// input from previous blocks
			int nprev = b0.prev!=null? b0.prev.length: 0;
			for ( int slot=0; slot<m; slot++ ) {
				VarInfo var = null;
				if ( nprev == 0 ) 
					var = params[slot];
				else if ( nprev == 1 )
					var = v[slot][b0.prev[0].pc1];
				else {
					for ( int i=0; i<nprev; i++ ) {
						BasicBlock bp = b0.prev[i];
						if ( v[slot][bp.pc1] == VarInfo.INVALID )
							var = VarInfo.INVALID;
					}
					if ( var == null )
						var = VarInfo.PHI(this, slot, b0.pc0);
				}
				v[slot][b0.pc0] = var;
			}

			// process instructions for this basic block
			for ( int pc=b0.pc0; pc<=b0.pc1; pc++ ) {
			
				// propogate previous for all but block boundaries
				if ( pc>b0.pc0 )
					for ( int j=0; j<m; j++ )
						v[j][pc] = v[j][pc-1];
				
				int a,b,c,nups;
				int ins = prototype.code[pc];
				int op = Lua.GET_OPCODE(ins);
	
				// account for assignments, references and invalidations
				switch ( op ) {
				case Lua.OP_LOADK:/*	A Bx	R(A) := Kst(Bx)					*/
				case Lua.OP_LOADBOOL:/*	A B C	R(A) := (Bool)B; if (C) pc++			*/
				case Lua.OP_GETUPVAL: /*	A B	R(A) := UpValue[B]				*/
				case Lua.OP_GETGLOBAL: /*	A Bx	R(A) := Gbl[Kst(Bx)]				*/
				case Lua.OP_NEWTABLE: /*	A B C	R(A) := {} (size = B,C)				*/
					a = Lua.GETARG_A( ins );
					v[a][pc] = new VarInfo(a,pc);
					break;
				
				case Lua.OP_MOVE:/*	A B	R(A) := R(B)					*/				
				case Lua.OP_UNM: /*	A B	R(A) := -R(B)					*/
				case Lua.OP_NOT: /*	A B	R(A) := not R(B)				*/
				case Lua.OP_LEN: /*	A B	R(A) := length of R(B)				*/
				case Lua.OP_TESTSET: /*	A B C	if (R(B) <=> C) then R(A) := R(B) else pc++	*/ 
					a = Lua.GETARG_A( ins );
					b = Lua.GETARG_B( ins );
					v[b][pc].isreferenced = true;
					v[a][pc] = new VarInfo(a,pc);
					break;
					
				case Lua.OP_ADD: /*	A B C	R(A) := RK(B) + RK(C)				*/
				case Lua.OP_SUB: /*	A B C	R(A) := RK(B) - RK(C)				*/
				case Lua.OP_MUL: /*	A B C	R(A) := RK(B) * RK(C)				*/
				case Lua.OP_DIV: /*	A B C	R(A) := RK(B) / RK(C)				*/
				case Lua.OP_MOD: /*	A B C	R(A) := RK(B) % RK(C)				*/
				case Lua.OP_POW: /*	A B C	R(A) := RK(B) ^ RK(C)				*/
					a = Lua.GETARG_A( ins );
					b = Lua.GETARG_B( ins );
					c = Lua.GETARG_C( ins );
					if (!Lua.ISK(b)) v[b][pc].isreferenced = true;
					if (!Lua.ISK(c)) v[c][pc].isreferenced = true;
					v[a][pc] = new VarInfo(a,pc);
					break;
					
				case Lua.OP_SETTABLE: /*	A B C	R(A)[RK(B)]:= RK(C)				*/
					a = Lua.GETARG_A( ins );
					b = Lua.GETARG_B( ins );
					c = Lua.GETARG_C( ins );
					v[a][pc].isreferenced = true;
					if (!Lua.ISK(b)) v[b][pc].isreferenced = true;
					if (!Lua.ISK(c)) v[c][pc].isreferenced = true;
					break;
					
				case Lua.OP_CONCAT: /*	A B C	R(A) := R(B).. ... ..R(C)			*/
					a = Lua.GETARG_A( ins );
					b = Lua.GETARG_B( ins );
					c = Lua.GETARG_C( ins );
					for ( ; b<=c; b++ )
						v[b][pc].isreferenced = true;
					v[a][pc] = new VarInfo(a,pc);
					break;
					
				case Lua.OP_FORPREP: /*	A sBx	R(A)-=R(A+2); pc+=sBx				*/
					a = Lua.GETARG_A( ins );
					v[a+2][pc].isreferenced = true;
					v[a][pc] = new VarInfo(a,pc);
					break;
					
				case Lua.OP_GETTABLE: /*	A B C	R(A) := R(B)[RK(C)]				*/
					a = Lua.GETARG_A( ins );
					b = Lua.GETARG_B( ins );
					c = Lua.GETARG_C( ins );
					v[b][pc].isreferenced = true;
					if (!Lua.ISK(c)) v[c][pc].isreferenced = true;
					v[a][pc] = new VarInfo(a,pc);
					break;
					
				case Lua.OP_SELF: /*	A B C	R(A+1) := R(B); R(A) := R(B)[RK(C)]		*/
					a = Lua.GETARG_A( ins );
					b = Lua.GETARG_B( ins );
					c = Lua.GETARG_C( ins );
					v[b][pc].isreferenced = true;
					if (!Lua.ISK(c)) v[c][pc].isreferenced = true;
					v[a][pc] = new VarInfo(a,pc);
					v[a+1][pc] = new VarInfo(a+1,pc);
					break;
					
				case Lua.OP_FORLOOP: /*	A sBx	R(A)+=R(A+2);
					if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }*/
					a = Lua.GETARG_A( ins );
					v[a][pc].isreferenced = true;
					v[a+2][pc].isreferenced = true;
					v[a][pc] = new VarInfo(a,pc);
					v[a][pc].isreferenced = true;
					v[a+1][pc].isreferenced = true;
					v[a+3][pc] = new VarInfo(a+3,pc);
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
					b = Lua.GETARG_B( ins );
					c = Lua.GETARG_C( ins );
					v[a][pc].isreferenced = true;
					v[a][pc].isreferenced = true;
					for ( int i=1; i<=b-1; i++ )
						v[a+i][pc].isreferenced = true;
					for ( int j=0; j<=c-2; j++, a++ )
						v[a][pc] = new VarInfo(a,pc);
					for ( ; a<m; a++ )
						v[a][pc] = VarInfo.INVALID;
					break;
					
				case Lua.OP_TAILCALL: /*	A B C	return R(A)(R(A+1), ... ,R(A+B-1))		*/
					a = Lua.GETARG_A( ins );
					b = Lua.GETARG_B( ins );
					v[a][pc].isreferenced = true;
					for ( int i=1; i<=b-1; i++ )
						v[a+i][pc].isreferenced = true;
					break;
					
				case Lua.OP_RETURN: /*	A B	return R(A), ... ,R(A+B-2)	(see note)	*/
					a = Lua.GETARG_A( ins );
					b = Lua.GETARG_B( ins );
					for ( int i=0; i<=b-2; i++ )
						v[a+i][pc].isreferenced = true;
					break;
					
				case Lua.OP_TFORLOOP: /*	A C	R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2)); 
				                        if R(A+3) ~= nil then R(A+2)=R(A+3) else pc++	*/ 
					a = Lua.GETARG_A( ins );
					c = Lua.GETARG_C( ins );
					v[a++][pc].isreferenced = true;
					v[a++][pc].isreferenced = true;
					v[a++][pc].isreferenced = true;
					for ( int j=0; j<c; j++, a++ )
						v[a][pc] = new VarInfo(a,pc);
					for ( ; a<m; a++ )
						v[a][pc] = VarInfo.INVALID;
					break;
					
				case Lua.OP_CLOSURE: /*	A Bx	R(A) := closure(KPROTO[Bx], R(A), ... ,R(A+n))	*/
					a = Lua.GETARG_A( ins );
					b = Lua.GETARG_Bx( ins );
					nups = prototype.p[b].nups;
					for ( int k=1; k<=nups; ++k ) {
						int i = prototype.code[pc+k];
						if ( (i&4) == 0 ) {
							b = Lua.GETARG_B(i);
							v[b][pc].isreferenced = true;
						}
					}
					v[a][pc] = new VarInfo(a,pc);
					for ( int k=1; k<=nups; ++k ) {
						for ( int j=0; j<m; j++ )
							v[j][pc+k] = v[j][pc];
					}
					pc += nups;
					break;
				case Lua.OP_CLOSE: /*	A 	close all variables in the stack up to (>=) R(A)*/
					a = Lua.GETARG_A( ins );
					for ( ; a<m; a++ )
						v[a][pc] = VarInfo.INVALID;
					break;
	
				case Lua.OP_SETLIST: /*	A B C	R(A)[(C-1)*FPF+i]:= R(A+i), 1 <= i <= B	*/
					a = Lua.GETARG_A( ins );
					b = Lua.GETARG_B( ins );
					v[a][pc].isreferenced = true;
					for ( int i=1; i<=b; i++ )
						v[a+i][pc].isreferenced = true;
					break;
					
				case Lua.OP_SETGLOBAL: /*	A Bx	Gbl[Kst(Bx)]:= R(A)				*/
				case Lua.OP_SETUPVAL: /*	A B	UpValue[B]:= R(A)				*/
				case Lua.OP_TEST: /*	A C	if not (R(A) <=> C) then pc++			*/ 
					a = Lua.GETARG_A( ins );
					v[a][pc].isreferenced = true;
					break;

				case Lua.OP_EQ: /*	A B C	if ((RK(B) == RK(C)) ~= A) then pc++		*/
				case Lua.OP_LT: /*	A B C	if ((RK(B) <  RK(C)) ~= A) then pc++  		*/
				case Lua.OP_LE: /*	A B C	if ((RK(B) <= RK(C)) ~= A) then pc++  		*/
					b = Lua.GETARG_B( ins );
					c = Lua.GETARG_C( ins );
					if (!Lua.ISK(b)) v[b][pc].isreferenced = true;
					if (!Lua.ISK(c)) v[c][pc].isreferenced = true;
					break;

				case Lua.OP_JMP: /*	sBx	pc+=sBx					*/
					break;
	
				default:
					throw new IllegalStateException("unhandled opcode: "+ins);
				}
			}
		}			
		return v;
	}

	private void replaceTrivialPhiVariables() {		
		for ( int i=0; i<blocklist.length; i++ ) {
			BasicBlock b0 = blocklist[i];
			for ( int slot=0; slot<prototype.maxstacksize; slot++ ) {
				VarInfo vold = vars[slot][b0.pc1];
				VarInfo vnew = vold.resolvePhiVariableValues();
				if ( vnew != null )
					substituteVariable( slot, vold, vnew );
			}
		}
	}					
	
	private void substituteVariable(int slot, VarInfo vold, VarInfo vnew) {
		for ( int i=0, n=prototype.code.length; i<n; i++ )
			replaceAll( vars[slot], vars[slot].length, vold, vnew );
	}

	private void replaceAll(VarInfo[] v, int n, VarInfo vold, VarInfo vnew) {
		for ( int i=0; i<n; i++ )
			if ( v[i] == vold )
				v[i] = vnew;
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
		UpvalInfo u = new UpvalInfo(this, pc, slot);
		for ( int i=0, n=prototype.code.length; i<n; ++i )
			if ( vars[slot][i] != null && vars[slot][i].upvalue == u )
				openups[slot][i] = u;
		return u;
	}

	public boolean isUpvalueAssign(int pc, int slot) {
		VarInfo v = pc<0? params[slot]: vars[slot][pc];
		return v != null && v.upvalue != null && v.upvalue.rw;
	}

	public boolean isUpvalueCreate(int pc, int slot) {
		VarInfo v = pc<0? params[slot]: vars[slot][pc];
		return v != null && v.upvalue != null && v.upvalue.rw && v.allocupvalue && pc == v.pc;
	}

	public boolean isUpvalueRefer(int pc, int slot) {
		// special case when both refer and assign in same instruction
		if ( pc >= 0 && vars[slot][pc] != null && vars[slot][pc].pc == pc )
			pc -= 1;
		VarInfo v = pc<0? params[slot]: vars[slot][pc];
		return v != null && v.upvalue != null && v.upvalue.rw;
	}

	public boolean isInitialValueUsed(int slot) {
		VarInfo v = params[slot];
		return v.isreferenced;
	}

	public boolean isReadWriteUpvalue(UpvalInfo u) {
		return u.rw;
	}
}
