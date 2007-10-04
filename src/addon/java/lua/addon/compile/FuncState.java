package lua.addon.compile;

import java.util.Hashtable;

import lua.Lua;
import lua.addon.compile.LexState.ConsControl;
import lua.addon.compile.LexState.expdesc;
import lua.io.LocVars;
import lua.io.Proto;
import lua.value.LBoolean;
import lua.value.LDouble;
import lua.value.LInteger;
import lua.value.LNil;
import lua.value.LNumber;
import lua.value.LString;
import lua.value.LValue;

public class FuncState extends LuaC {
	class upvaldesc {
		  byte k;
		  byte info;
	};

	static class BlockCnt {
		  BlockCnt previous;  /* chain */
		  IntPtr breaklist = new IntPtr();  /* list of jumps out of this loop */
		  byte nactvar;  /* # active locals outside the breakable structure */
		  boolean upval;  /* true if some variable in the block is an upvalue */
		  boolean isbreakable;  /* true if `block' is a loop */
		};
	
	Proto f;  /* current function header */
//	LTable h;  /* table to find (and reuse) elements in `k' */
	Hashtable htable;  /* table to find (and reuse) elements in `k' */
	FuncState prev;  /* enclosing function */
	LexState ls;  /* lexical state */
	Compiler L;  /* copy of the Lua state */
	BlockCnt bl;  /* chain of current blocks */
	int pc;  /* next position to code (equivalent to `ncode') */
	int lasttarget;   /* `pc' of last `jump target' */
	IntPtr jpc;  /* list of pending jumps to `pc' */
	int freereg;  /* first free register */
	int nk;  /* number of elements in `k' */
	int np;  /* number of elements in `p' */
	short nlocvars;  /* number of elements in `locvars' */
	byte nactvar;  /* number of active local variables */
	upvaldesc upvalues[] = new upvaldesc[LUAI_MAXUPVALUES];  /* upvalues */
	short actvar[] = new short[LUAI_MAXVARS];  /* declared-variable stack */
	int varargflags; /* whether varargs are needed */
	
	FuncState() {
	}
	
	
	// =============================================================
	// from lcode.h
	// =============================================================

	InstructionPtr getcodePtr(expdesc e) {
		return new InstructionPtr( f.code, e.u.s.info );
	}

	int getcode(expdesc e) {
		return f.code[e.u.s.info];
	}

	int codeAsBx(int o, int A, int sBx) {
		return codeABx(o,A,sBx+MAXARG_sBx);
	}

	void setmultret(expdesc e) {
		setreturns(e, LUA_MULTRET);
	}

	
	// =============================================================
	// from lparser.c
	// =============================================================

	LocVars getlocvar(int i) {
		return f.locvars[actvar[i]];
	}

	void checklimit(int v, int l, String msg) {
		if ( v > l )
			errorlimit( l, msg );
	}
	
	void errorlimit (int limit, String what) {
	  	String msg = (f.linedefined == 0) ?
	  	    L.pushfstring("main function has more than "+limit+" "+what) :
	  	    L.pushfstring("function at line "+f.linedefined+" has more than "+limit+" "+what);
	  	  ls.lexerror(msg, 0);
	}


	int indexupvalue(LString name, expdesc v) {
		int i;
		for (i = 0; i < f.nups; i++) {
			if (upvalues[i].k == v.k && upvalues[i].info == v.u.s.info) {
				_assert(f.upvalues[i] == name);
				return i;
			}
		}
		/* new one */
		checklimit(f.nups + 1, LUAI_MAXUPVALUES, "upvalues");
		if ( f.upvalues == null || f.nups + 1 > f.upvalues.length)
			f.upvalues = realloc( f.upvalues, f.nups*2+1 );
		f.upvalues[f.nups] = name;
		_assert (v.k == LexState.VLOCAL || v.k == LexState.VUPVAL);
		upvalues[f.nups] = new upvaldesc();
		upvalues[f.nups].k = (byte) (v.k);
		upvalues[f.nups].info = (byte) (v.u.s.info);
		return f.nups++;
	}
		
	int searchvar(LString n) {
		int i;
		for (i = nactvar - 1; i >= 0; i--) {
			if (n == getlocvar(i).varname)
				return i;
		}
		return -1; /* not found */
	}
		
	void markupval(int level) {
		BlockCnt bl = this.bl;
		while (bl != null && bl.nactvar > level)
			bl = bl.previous;
		if (bl != null)
			bl.upval = true;
	}
		
	int singlevaraux(LString n, expdesc var, int base) {
		int v = searchvar(n); /* look up at current level */
		if (v >= 0) {
			var.init(LexState.VLOCAL, v);
			if (base == 0)
				markupval(v); /* local will be used as an upval */
			return LexState.VLOCAL;
		} else { /* not found at current level; try upper one */
			if (prev == null) { /* no more levels? */
				/* default is global variable */
				var.init(LexState.VGLOBAL, NO_REG); 
				return LexState.VGLOBAL;
			}
			if (prev.singlevaraux(n, var, 0) == LexState.VGLOBAL)
				return LexState.VGLOBAL;
			var.u.s.info = indexupvalue(n, var); /* else was LOCAL or UPVAL */
			var.k = LexState.VUPVAL; /* upvalue in this level */
			return LexState.VUPVAL;
		}
	}
	
	void enterblock (BlockCnt bl, boolean isbreakable) {
	  bl.breaklist.i = LexState.NO_JUMP;
	  bl.isbreakable = isbreakable;
	  bl.nactvar = this.nactvar;
	  bl.upval = false;
	  bl.previous = this.bl;
	  this.bl = bl;
	  _assert(this.freereg == this.nactvar);
	}

	//
//	void leaveblock (FuncState *fs) {
//	  BlockCnt *bl = this.bl;
//	  this.bl = bl.previous;
//	  removevars(this.ls, bl.nactvar);
//	  if (bl.upval)
//	    this.codeABC(OP_CLOSE, bl.nactvar, 0, 0);
//	  /* a block either controls scope or breaks (never both) */
//	  assert(!bl.isbreakable || !bl.upval);
//	  assert(bl.nactvar == this.nactvar);
//	  this.freereg = this.nactvar;  /* free registers */
//	  this.patchtohere(bl.breaklist);
//	}
	
	void leaveblock() {
		BlockCnt bl = this.bl;
		this.bl = bl.previous;
		ls.removevars(bl.nactvar);
		if (bl.upval)
			this.codeABC(OP_CLOSE, bl.nactvar, 0, 0);
		/* a block either controls scope or breaks (never both) */
		_assert (!bl.isbreakable || !bl.upval);
		_assert (bl.nactvar == this.nactvar);
		this.freereg = this.nactvar; /* free registers */
		this.patchtohere(bl.breaklist.i);
	}

	void closelistfield(ConsControl cc) {
		if (cc.v.k == LexState.VVOID)
			return; /* there is no list item */
		this.exp2nextreg(cc.v);
		cc.v.k = LexState.VVOID;
		if (cc.tostore == LFIELDS_PER_FLUSH) {
			this.setlist(cc.t.u.s.info, cc.na, cc.tostore); /* flush */
			cc.tostore = 0; /* no more items pending */
		}
	}

	boolean hasmultret(int k) {
		return ((k) == LexState.VCALL || (k) == LexState.VVARARG);
	}

	void lastlistfield (ConsControl cc) {
		if (cc.tostore == 0) return;
		if (hasmultret(cc.v.k)) {
		    this.setmultret(cc.v);
		    this.setlist(cc.t.u.s.info, cc.na, LUA_MULTRET);
		    cc.na--;  /** do not count last expression (unknown number of elements) */
		}
		else {
		    if (cc.v.k != LexState.VVOID)
		    	this.exp2nextreg(cc.v);
		    this.setlist(cc.t.u.s.info, cc.na, cc.tostore);
		}
	}
	
	
	
	// =============================================================
	// from lcode.c
	// =============================================================

	void nil(int from, int n) {
		InstructionPtr previous;
		if (this.pc > this.lasttarget) { /* no jumps to current position? */
			if (this.pc == 0) { /* function start? */
				if (from >= this.nactvar)
					return; /* positions are already clean */
			} else {
				previous = new InstructionPtr(this.f.code, this.pc - 1);
				if (GET_OPCODE(previous.get()) == OP_LOADNIL) {
					int pfrom = GETARG_A(previous.get());
					int pto = GETARG_B(previous.get());
					if (pfrom <= from && from <= pto + 1) { /* can connect both? */
						if (from + n - 1 > pto)
							SETARG_B(previous, from + n - 1);
						return;
					}
				}
			}
		}
		/* else no optimization */
		this.codeABC(OP_LOADNIL, from, from + n - 1, 0); 
	}


	int jump() {
		int jpc = this.jpc.i; /* save list of jumps to here */
		this.jpc.i = LexState.NO_JUMP;
		IntPtr j = new IntPtr(this.codeAsBx(OP_JMP, 0, LexState.NO_JUMP));
		this.concat(j, jpc); /* keep them on hold */
		return j.i;
	}

	void ret(int first, int nret) {
		this.codeABC(OP_RETURN, first, nret + 1, 0);
	}

	int condjump(int /* OpCode */op, int A, int B, int C) {
		this.codeABC(op, A, B, C);
		return this.jump();
	}

	void fixjump(int pc, int dest) {
		InstructionPtr jmp = new InstructionPtr(this.f.code, pc);
		int offset = dest - (pc + 1);
		_assert (dest != LexState.NO_JUMP);
		if (Math.abs(offset) > MAXARG_sBx)
			ls.syntaxerror("control structure too long");
		SETARG_sBx(jmp, offset);
	}


	/*
	 * * returns current `pc' and marks it as a jump target (to avoid wrong *
	 * optimizations with consecutive instructions not in the same basic block).
	 */
	int getlabel() {
		this.lasttarget = this.pc;
		return this.pc;
	}


	int getjump(int pc) {
		int offset = GETARG_sBx(this.f.code[pc]);
		/* point to itself represents end of list */
		if (offset == LexState.NO_JUMP)
			/* end of list */
			return LexState.NO_JUMP;
		else
			/* turn offset into absolute position */
			return (pc + 1) + offset;
	}


	InstructionPtr getjumpcontrol(int pc) {
		InstructionPtr pi = new InstructionPtr(this.f.code, pc);
		if (pc >= 1 && testTMode(GET_OPCODE(pi.code[pi.idx - 1])))
			return new InstructionPtr(pi.code, pi.idx - 1);
		else
			return pi;
	}


	/*
	 * * check whether list has any jump that do not produce a value * (or
	 * produce an inverted value)
	 */
	boolean need_value(int list) {
		for (; list != LexState.NO_JUMP; list = this.getjump(list)) {
			int i = this.getjumpcontrol(list).get();
			if (GET_OPCODE(i) != OP_TESTSET)
				return true;
		}
		return false; /* not found */
	}


	boolean patchtestreg(int node, int reg) {
		InstructionPtr i = this.getjumpcontrol(node);
		if (GET_OPCODE(i.get()) != OP_TESTSET)
			/* cannot patch other instructions */
			return false;
		if (reg != NO_REG && reg != GETARG_B(i.get()))
			SETARG_A(i, reg);
		else
			/* no register to put value or register already has the value */
			i.set(CREATE_ABC(OP_TEST, GETARG_B(i.get()), 0, Lua.GETARG_C(i.get())));

		return true;
	}


	void removevalues(int list) {
		for (; list != LexState.NO_JUMP; list = this.getjump(list))
			this.patchtestreg(list, NO_REG);
	}

	void patchlistaux(int list, int vtarget, int reg, int dtarget) {
		while (list != LexState.NO_JUMP) {
			int next = this.getjump(list);
			if (this.patchtestreg(list, reg))
				this.fixjump(list, vtarget);
			else
				this.fixjump(list, dtarget); /* jump to default target */
			list = next;
		}
	}

	void dischargejpc() {
		this.patchlistaux(this.jpc.i, this.pc, NO_REG, this.pc);
		this.jpc.i = LexState.NO_JUMP;
	}

	void patchlist(int list, int target) {
		if (target == this.pc)
			this.patchtohere(list);
		else {
			_assert (target < this.pc);
			this.patchlistaux(list, target, NO_REG, target);
		}
	}

	void patchtohere(int list) {
		this.getlabel();
		this.concat(this.jpc, list);
	}

	void concat(IntPtr l1, int l2) {
		if (l2 == LexState.NO_JUMP)
			return;
		if (l1.i == LexState.NO_JUMP)
			l1.i = l2;
		else {
			int list = l1.i;
			int next;
			while ((next = this.getjump(list)) != LexState.NO_JUMP)
				/* find last element */
				list = next;
			this.fixjump(list, l2);
		}
	}

	void checkstack(int n) {
		int newstack = this.freereg + n;
		if (newstack > this.f.maxstacksize) {
			if (newstack >= MAXSTACK)
				ls.syntaxerror("function or expression too complex");
			this.f.maxstacksize = (byte) newstack;
		}
	}

	void reserveregs(int n) {
		this.checkstack(n);
		this.freereg += n;
	}

	void freereg(int reg) {
		if (!ISK(reg) && reg >= this.nactvar) {
			this.freereg--;
			_assert (reg == this.freereg);
		}
	}

	void freeexp(expdesc e) {
		if (e.k == LexState.VNONRELOC)
			this.freereg(e.u.s.info);
	}

	int addk(LValue v) {
		int idx;
		if (this.htable.containsKey(v)) {
			idx = ((Integer) htable.get(v)).intValue();
		} else {
			idx = this.nk;
			this.htable.put(v, new Integer(idx));
			final Proto f = this.f;
			if (f.k == null || nk + 1 >= f.k.length)
				f.k = realloc( f.k, nk*2 + 1 );
			f.k[this.nk++] = v;
		}
		return idx;
	}

	int stringK(LString s) {
		return this.addk(s);
	}

	int numberK(LNumber r) {
		if ( r instanceof LDouble ) {
			double d = r.luaAsDouble();
			int i = (int) d;
			if ( d == (double) i ) 
				r = LInteger.valueOf(i);
		}
		return this.addk(r);
	}

	int boolK(boolean b) {
		return this.addk((b ? LBoolean.TRUE : LBoolean.FALSE));
	}

	int nilK() {
		return this.addk(LNil.NIL);
	}

	void setreturns(expdesc e, int nresults) {
		if (e.k == LexState.VCALL) { /* expression is an open function call? */
			SETARG_C(this.getcodePtr(e), nresults + 1);
		} else if (e.k == LexState.VVARARG) {
			SETARG_B(this.getcodePtr(e), nresults + 1);
			SETARG_A(this.getcodePtr(e), this.freereg);
			this.reserveregs(1);
		}
	}

	void setoneret(expdesc e) {
		if (e.k == LexState.VCALL) { /* expression is an open function call? */
			e.k = LexState.VNONRELOC;
			e.u.s.info = GETARG_A(this.getcode(e));
		} else if (e.k == LexState.VVARARG) {
			SETARG_B(this.getcodePtr(e), 2);
			e.k = LexState.VRELOCABLE; /* can relocate its simple result */
		}
	}

	void dischargevars(expdesc e) {
		switch (e.k) {
		case LexState.VLOCAL: {
			e.k = LexState.VNONRELOC;
			break;
		}
		case LexState.VUPVAL: {
			e.u.s.info = this.codeABC(OP_GETUPVAL, 0, e.u.s.info, 0);
			e.k = LexState.VRELOCABLE;
			break;
		}
		case LexState.VGLOBAL: {
			e.u.s.info = this.codeABx(OP_GETGLOBAL, 0, e.u.s.info);
			e.k = LexState.VRELOCABLE;
			break;
		}
		case LexState.VINDEXED: {
			this.freereg(e.u.s.aux);
			this.freereg(e.u.s.info);
			e.u.s.info = this
					.codeABC(OP_GETTABLE, 0, e.u.s.info, e.u.s.aux);
			e.k = LexState.VRELOCABLE;
			break;
		}
		case LexState.VVARARG:
		case LexState.VCALL: {
			this.setoneret(e);
			break;
		}
		default:
			break; /* there is one value available (somewhere) */
		}
	}

	int code_label(int A, int b, int jump) {
		this.getlabel(); /* those instructions may be jump targets */
		return this.codeABC(OP_LOADBOOL, A, b, jump);
	}

	void discharge2reg(expdesc e, int reg) {
		this.dischargevars(e);
		switch (e.k) {
		case LexState.VNIL: {
			this.nil(reg, 1);
			break;
		}
		case LexState.VFALSE:
		case LexState.VTRUE: {
			this.codeABC(OP_LOADBOOL, reg, (e.k == LexState.VTRUE ? 1 : 0),
					0);
			break;
		}
		case LexState.VK: {
			this.codeABx(OP_LOADK, reg, e.u.s.info);
			break;
		}
		case LexState.VKNUM: {
			this.codeABx(OP_LOADK, reg, this.numberK(e.u.nval()));
			break;
		}
		case LexState.VRELOCABLE: {
			InstructionPtr pc = this.getcodePtr(e);
			SETARG_A(pc, reg);
			break;
		}
		case LexState.VNONRELOC: {
			if (reg != e.u.s.info)
				this.codeABC(OP_MOVE, reg, e.u.s.info, 0);
			break;
		}
		default: {
			_assert (e.k == LexState.VVOID || e.k == LexState.VJMP);
			return; /* nothing to do... */
		}
		}
		e.u.s.info = reg;
		e.k = LexState.VNONRELOC;
	}

	void discharge2anyreg(expdesc e) {
		if (e.k != LexState.VNONRELOC) {
			this.reserveregs(1);
			this.discharge2reg(e, this.freereg - 1);
		}
	}

	void exp2reg(expdesc e, int reg) {
		this.discharge2reg(e, reg);
		if (e.k == LexState.VJMP)
			this.concat(e.t, e.u.s.info); /* put this jump in `t' list */
		if (e.hasjumps()) {
			int _final; /* position after whole expression */
			int p_f = LexState.NO_JUMP; /* position of an eventual LOAD false */
			int p_t = LexState.NO_JUMP; /* position of an eventual LOAD true */
			if (this.need_value(e.t.i) || this.need_value(e.f.i)) {
				int fj = (e.k == LexState.VJMP) ? LexState.NO_JUMP : this
						.jump();
				p_f = this.code_label(reg, 0, 1);
				p_t = this.code_label(reg, 1, 0);
				this.patchtohere(fj);
			}
			_final = this.getlabel();
			this.patchlistaux(e.f.i, _final, reg, p_f);
			this.patchlistaux(e.t.i, _final, reg, p_t);
		}
		e.f.i = e.t.i = LexState.NO_JUMP;
		e.u.s.info = reg;
		e.k = LexState.VNONRELOC;
	}

	void exp2nextreg(expdesc e) {
		this.dischargevars(e);
		this.freeexp(e);
		this.reserveregs(1);
		this.exp2reg(e, this.freereg - 1);
	}

	int exp2anyreg(expdesc e) {
		this.dischargevars(e);
		if (e.k == LexState.VNONRELOC) {
			if (!e.hasjumps())
				return e.u.s.info; /* exp is already in a register */
			if (e.u.s.info >= this.nactvar) { /* reg. is not a local? */
				this.exp2reg(e, e.u.s.info); /* put value on it */
				return e.u.s.info;
			}
		}
		this.exp2nextreg(e); /* default */
		return e.u.s.info;
	}

	void exp2val(expdesc e) {
		if (e.hasjumps())
			this.exp2anyreg(e);
		else
			this.dischargevars(e);
	}

	int exp2RK(expdesc e) {
		this.exp2val(e);
		switch (e.k) {
		case LexState.VKNUM:
		case LexState.VTRUE:
		case LexState.VFALSE:
		case LexState.VNIL: {
			if (this.nk <= MAXINDEXRK) { /* constant fit in RK operand? */
				e.u.s.info = (e.k == LexState.VNIL) ? this.nilK()
						: (e.k == LexState.VKNUM) ? this.numberK(e.u.nval())
								: this.boolK((e.k == LexState.VTRUE));
				e.k = LexState.VK;
				return RKASK(e.u.s.info);
			} else
				break;
		}
		case LexState.VK: {
			if (e.u.s.info <= MAXINDEXRK) /* constant fit in argC? */
				return RKASK(e.u.s.info);
			else
				break;
		}
		default:
			break;
		}
		/* not a constant in the right range: put it in a register */
		return this.exp2anyreg(e);
	}

	void storevar(expdesc var, expdesc ex) {
		switch (var.k) {
		case LexState.VLOCAL: {
			this.freeexp(ex);
			this.exp2reg(ex, var.u.s.info);
			return;
		}
		case LexState.VUPVAL: {
			int e = this.exp2anyreg(ex);
			this.codeABC(OP_SETUPVAL, e, var.u.s.info, 0);
			break;
		}
		case LexState.VGLOBAL: {
			int e = this.exp2anyreg(ex);
			this.codeABx(OP_SETGLOBAL, e, var.u.s.info);
			break;
		}
		case LexState.VINDEXED: {
			int e = this.exp2RK(ex);
			this.codeABC(OP_SETTABLE, var.u.s.info, var.u.s.aux, e);
			break;
		}
		default: {
			_assert (false); /* invalid var kind to store */
			break;
		}
		}
		this.freeexp(ex);
	}

	void self(expdesc e, expdesc key) {
		int func;
		this.exp2anyreg(e);
		this.freeexp(e);
		func = this.freereg;
		this.reserveregs(2);
		this.codeABC(OP_SELF, func, e.u.s.info, this.exp2RK(key));
		this.freeexp(key);
		e.u.s.info = func;
		e.k = LexState.VNONRELOC;
	}

	void invertjump(expdesc e) {
		InstructionPtr pc = this.getjumpcontrol(e.u.s.info);
		_assert (testTMode(GET_OPCODE(pc.get()))
				&& GET_OPCODE(pc.get()) != OP_TESTSET && Lua
				.GET_OPCODE(pc.get()) != OP_TEST);
		// SETARG_A(pc, !(GETARG_A(pc.get())));
		int a = GETARG_A(pc.get());
		int nota = (a!=0? 0: 1);
		SETARG_A(pc, nota);
	}

	int jumponcond(expdesc e, int cond) {
		if (e.k == LexState.VRELOCABLE) {
			int ie = this.getcode(e);
			if (GET_OPCODE(ie) == OP_NOT) {
				this.pc--; /* remove previous OP_NOT */
				return this.condjump(OP_TEST, GETARG_B(ie), 0, (cond!=0? 0: 1));
			}
			/* else go through */
		}
		this.discharge2anyreg(e);
		this.freeexp(e);
		return this.condjump(OP_TESTSET, NO_REG, e.u.s.info, cond);
	}

	void goiftrue(expdesc e) {
		int pc; /* pc of last jump */
		this.dischargevars(e);
		switch (e.k) {
		case LexState.VK:
		case LexState.VKNUM:
		case LexState.VTRUE: {
			pc = LexState.NO_JUMP; /* always true; do nothing */
			break;
		}
		case LexState.VFALSE: {
			pc = this.jump(); /* always jump */
			break;
		}
		case LexState.VJMP: {
			this.invertjump(e);
			pc = e.u.s.info;
			break;
		}
		default: {
			pc = this.jumponcond(e, 0);
			break;
		}
		}
		this.concat(e.f, pc); /* insert last jump in `f' list */
		this.patchtohere(e.t.i);
		e.t.i = LexState.NO_JUMP;
	}

	void goiffalse(expdesc e) {
		int pc; /* pc of last jump */
		this.dischargevars(e);
		switch (e.k) {
		case LexState.VNIL:
		case LexState.VFALSE: {
			pc = LexState.NO_JUMP; /* always false; do nothing */
			break;
		}
		case LexState.VTRUE: {
			pc = this.jump(); /* always jump */
			break;
		}
		case LexState.VJMP: {
			pc = e.u.s.info;
			break;
		}
		default: {
			pc = this.jumponcond(e, 1);
			break;
		}
		}
		this.concat(e.t, pc); /* insert last jump in `t' list */
		this.patchtohere(e.f.i);
		e.f.i = LexState.NO_JUMP;
	}

	void codenot(expdesc e) {
		this.dischargevars(e);
		switch (e.k) {
		case LexState.VNIL:
		case LexState.VFALSE: {
			e.k = LexState.VTRUE;
			break;
		}
		case LexState.VK:
		case LexState.VKNUM:
		case LexState.VTRUE: {
			e.k = LexState.VFALSE;
			break;
		}
		case LexState.VJMP: {
			this.invertjump(e);
			break;
		}
		case LexState.VRELOCABLE:
		case LexState.VNONRELOC: {
			this.discharge2anyreg(e);
			this.freeexp(e);
			e.u.s.info = this.codeABC(OP_NOT, 0, e.u.s.info, 0);
			e.k = LexState.VRELOCABLE;
			break;
		}
		default: {
			_assert (false); /* cannot happen */
			break;
		}
		}
		/* interchange true and false lists */
		{
			int temp = e.f.i;
			e.f.i = e.t.i;
			e.t.i = temp;
		}
		this.removevalues(e.f.i);
		this.removevalues(e.t.i);
	}

	void indexed(expdesc t, expdesc k) {
		t.u.s.aux = this.exp2RK(k);
		t.k = LexState.VINDEXED;
	}

	boolean constfolding(int op, expdesc e1, expdesc e2) {
		LNumber v1, v2, r;
		if (!e1.isnumeral() || !e2.isnumeral())
			return false;
		v1 = e1.u.nval();
		v2 = e2.u.nval();
		switch (op) {
		case OP_ADD:
		case OP_SUB:
		case OP_MUL:
		case OP_DIV:
		case OP_MOD:
			r = (LNumber) v2.luaBinOpUnknown(op, v1);
			break;
			/* TODO: replace this with something reasonable.
		case OP_POW:
			r = new LDouble( Math.pow(v1.luaAsDouble(), v2.luaAsDouble() ) );
			break;
			//*/
		case OP_UNM:
			r = (LNumber) v1.luaUnaryMinus();
			break;
		case OP_LEN:
			return false; /* no constant folding for 'len' */
		default:
			_assert (false);
			r = null;
			break;
		}
		if (Double.NaN == r.luaAsDouble())
			return false; /* do not attempt to produce NaN */
		e1.u.setNval( r );
		return true;
	}

	void codearith(int op, expdesc e1, expdesc e2) {
		if (constfolding(op, e1, e2))
			return;
		else {
			int o2 = (op != OP_UNM && op != OP_LEN) ? this.exp2RK(e2)
					: 0;
			int o1 = this.exp2RK(e1);
			if (o1 > o2) {
				this.freeexp(e1);
				this.freeexp(e2);
			} else {
				this.freeexp(e2);
				this.freeexp(e1);
			}
			e1.u.s.info = this.codeABC(op, 0, o1, o2);
			e1.k = LexState.VRELOCABLE;
		}
	}

	void codecomp(int /* OpCode */op, int cond, expdesc e1, expdesc e2) {
		int o1 = this.exp2RK(e1);
		int o2 = this.exp2RK(e2);
		this.freeexp(e2);
		this.freeexp(e1);
		if (cond == 0 && op != OP_EQ) {
			int temp; /* exchange args to replace by `<' or `<=' */
			temp = o1;
			o1 = o2;
			o2 = temp; /* o1 <==> o2 */
			cond = 1;
		}
		e1.u.s.info = this.condjump(op, cond, o1, o2);
		e1.k = LexState.VJMP;
	}

	void prefix(int /* UnOpr */op, expdesc e) {
		expdesc e2 = new expdesc();
		e2.init(LexState.VKNUM, 0);
		switch (op) {
		case LexState.OPR_MINUS: {
			if (e.k == LexState.VK)
				this.exp2anyreg(e); /* cannot operate on non-numeric constants */
			this.codearith(OP_UNM, e, e2);
			break;
		}
		case LexState.OPR_NOT:
			this.codenot(e);
			break;
		case LexState.OPR_LEN: {
			this.exp2anyreg(e); /* cannot operate on constants */
			this.codearith(OP_LEN, e, e2);
			break;
		}
		default:
			_assert (false);
		}
	}

	void infix(int /* BinOpr */op, expdesc v) {
		switch (op) {
		case LexState.OPR_AND: {
			this.goiftrue(v);
			break;
		}
		case LexState.OPR_OR: {
			this.goiffalse(v);
			break;
		}
		case LexState.OPR_CONCAT: {
			this.exp2nextreg(v); /* operand must be on the `stack' */
			break;
		}
		case LexState.OPR_ADD:
		case LexState.OPR_SUB:
		case LexState.OPR_MUL:
		case LexState.OPR_DIV:
		case LexState.OPR_MOD:
		case LexState.OPR_POW: {
			if (!v.isnumeral())
				this.exp2RK(v);
			break;
		}
		default: {
			this.exp2RK(v);
			break;
		}
		}
	}


	void posfix(int op, expdesc e1, expdesc e2) {
		switch (op) {
		case LexState.OPR_AND: {
			_assert (e1.t.i == LexState.NO_JUMP); /* list must be closed */
			this.dischargevars(e2);
			this.concat(e2.f, e1.f.i);
			// *e1 = *e2;
			e1.setvalue(e2);
			break;
		}
		case LexState.OPR_OR: {
			_assert (e1.f.i == LexState.NO_JUMP); /* list must be closed */
			this.dischargevars(e2);
			this.concat(e2.t, e1.t.i);
			// *e1 = *e2;
			e1.setvalue(e2);
			break;
		}
		case LexState.OPR_CONCAT: {
			this.exp2val(e2);
			if (e2.k == LexState.VRELOCABLE
					&& GET_OPCODE(this.getcode(e2)) == OP_CONCAT) {
				_assert (e1.u.s.info == GETARG_B(this.getcode(e2)) - 1);
				this.freeexp(e1);
				SETARG_B(this.getcodePtr(e2), e1.u.s.info);
				e1.k = LexState.VRELOCABLE;
				e1.u.s.info = e2.u.s.info;
			} else {
				this.exp2nextreg(e2); /* operand must be on the 'stack' */
				this.codearith(OP_CONCAT, e1, e2);
			}
			break;
		}
		case LexState.OPR_ADD:
			this.codearith(OP_ADD, e1, e2);
			break;
		case LexState.OPR_SUB:
			this.codearith(OP_SUB, e1, e2);
			break;
		case LexState.OPR_MUL:
			this.codearith(OP_MUL, e1, e2);
			break;
		case LexState.OPR_DIV:
			this.codearith(OP_DIV, e1, e2);
			break;
		case LexState.OPR_MOD:
			this.codearith(OP_MOD, e1, e2);
			break;
		case LexState.OPR_POW:
			this.codearith(OP_POW, e1, e2);
			break;
		case LexState.OPR_EQ:
			this.codecomp(OP_EQ, 1, e1, e2);
			break;
		case LexState.OPR_NE:
			this.codecomp(OP_EQ, 0, e1, e2);
			break;
		case LexState.OPR_LT:
			this.codecomp(OP_LT, 1, e1, e2);
			break;
		case LexState.OPR_LE:
			this.codecomp(OP_LE, 1, e1, e2);
			break;
		case LexState.OPR_GT:
			this.codecomp(OP_LT, 0, e1, e2);
			break;
		case LexState.OPR_GE:
			this.codecomp(OP_LE, 0, e1, e2);
			break;
		default:
			_assert (false);
		}
	}


	void fixline(int line) {
		this.f.lineinfo[this.pc - 1] = line;
	}


	int code(int instruction, int line) {
		Proto f = this.f;
		this.dischargejpc(); /* `pc' will change */
		/* put new instruction in code array */
		if (f.code == null || this.pc + 1 > f.code.length)
			f.code = LexState.realloc(f.code, this.pc * 2 + 1);
		f.code[this.pc] = instruction;
		/* save corresponding line information */
		if (f.lineinfo == null || this.pc + 1 > f.lineinfo.length)
			f.lineinfo = LexState.realloc(f.lineinfo,
					this.pc * 2 + 1);
		f.lineinfo[this.pc] = line;
		return this.pc++;
	}


	int codeABC(int o, int a, int b, int c) {
		_assert (getOpMode(o) == iABC);
		_assert (getBMode(o) != OpArgN || b == 0);
		_assert (getCMode(o) != OpArgN || c == 0);
		return this.code(CREATE_ABC(o, a, b, c), this.ls.lastline);
	}


	int codeABx(int o, int a, int bc) {
		_assert (getOpMode(o) == iABx || getOpMode(o) == iAsBx);
		_assert (getCMode(o) == OpArgN);
		return this.code(CREATE_ABx(o, a, bc), this.ls.lastline);
	}


	void setlist(int base, int nelems, int tostore) {
		int c = (nelems - 1) / LFIELDS_PER_FLUSH + 1;
		int b = (tostore == LUA_MULTRET) ? 0 : tostore;
		_assert (tostore != 0);
		if (c <= MAXARG_C)
			this.codeABC(OP_SETLIST, base, b, c);
		else {
			this.codeABC(OP_SETLIST, base, b, 0);
			this.code(c, this.ls.lastline);
		}
		this.freereg = base + 1; /* free registers with list values */
	}
	  
}
