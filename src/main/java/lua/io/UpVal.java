package lua.io;

import lua.value.LString;
import lua.value.LValue;

public class UpVal {

	private LString name;
	public LValue[] stack;
	public int position;
	
	public UpVal( LString string, LValue[] stack, int i ) {
		this.name = string;
		this.stack = stack;
		this.position = i;
	}
	
	public String toString() {
		return "up."+name;
	}
	
	public LValue getValue() {
		return stack[ position ];
	}
	
	public void setValue( LValue value ) {
		stack[ position ] = value;
	}
	
	public boolean close( int limit ) {
		if ( position >= limit ) {
			final LValue v = stack[ position ];
			stack = new LValue[] { v };
			position = 0;
			return true;
		} else {
			return false;
		}
	}
}
