
package lua.addon.compile;

class InstructionPtr {
	final int[] code;
	final int idx;
	InstructionPtr(int[] code, int idx ) {
		this.code = code;
		this.idx = idx;
	}
	int get() {
		return code[idx];
	}
	void set(int value) {
		code[idx] = value;
	}
}