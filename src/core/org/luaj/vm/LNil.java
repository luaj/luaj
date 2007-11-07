package org.luaj.vm;


public final class LNil extends LValue {
	public static final LNil NIL = new LNil();
	
	public final LString luaAsString() {
		return luaGetTypeName();
	}

	public boolean toJavaBoolean() {
		return false;
	}

	public int luaGetType() {
		return Lua.LUA_TNIL;
	}
	
	public int toJavaInt() {
		return 0;
	}

	public String toJavaString() {
		return "nil";
	}

	public Byte toJavaBoxedByte() {
		return null;
	}

	public Character toJavaBoxedCharacter() {
		return null;
	}

	public Double toJavaBoxedDouble() {
		return null;
	}

	public Float toJavaBoxedFloat() {
		return null;
	}

	public Integer toJavaBoxedInteger() {
		return null;
	}

	public Long toJavaBoxedLong() {
		return null;
	}

	public Short toJavaBoxedShort() {
		return null;
	}	
}
