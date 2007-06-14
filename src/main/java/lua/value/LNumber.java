package lua.value;

import lua.Lua;

abstract
public class LNumber extends LValue {

	/** Compare for equivalence by using lua op comparator */
	public boolean equals(Object o) {
		if ( ! ( o instanceof LValue) )
			return false;
		LValue v = (LValue) o;
		return this.luaBinCmpUnknown(Lua.OP_EQ, v );
	}

}
