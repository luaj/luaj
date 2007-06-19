obj = luajava.newInstance("java.lang.Object")
print( obj )

obj = luajava.newInstance("SampleClass")
print( obj )
obj.s = "Hello"
print( obj.s )
print( obj.getS() )

obj.setS( "World" )
print( obj.s )
