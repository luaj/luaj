package lua.io;

import lua.value.LString;
import lua.value.LValue;

public class UpVal {

	private LString name;
	public LValue value;
	
	public UpVal( LString string ) {
		this.name = string;
	}
		
	public String toString() {
		return "up."+name;
	}

}
