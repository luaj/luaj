obj = luajava.newInstance("java.lang.Object")
print( obj )

obj = luajava.newInstance("org.luaj.sample.SampleClass")
print( obj )
obj.s = "Hello"
print( obj.s )
print( obj:getS() )

obj:setS( "World" )
print( obj.s )

math = luajava.bindClass("java.lang.Math")
print("Square root of 9 is", math:sqrt(9.0))
