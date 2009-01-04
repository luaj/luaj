local f = luajava.bindClass("java.lang.Float")
print(f:toHexString(0.5))
print(f:valueOf(0.5))
print(f:valueOf("0.5"))

