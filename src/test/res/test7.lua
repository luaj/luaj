local function fixhash(msg)
  return string.gsub(msg, "@(%x+)", function(s) return "@"..(string.rep("x", 6)) end)
end

obj = luajava.newInstance("java.lang.Object")
print( fixhash( tostring(obj) ) )

sample = luajava.newInstance("org.luaj.sample.SampleClass")
print( fixhash( tostring(sample) ) )
sample.s = "Hello"
print( sample.s )
print( sample:getS() )

sample:setObj(obj)
print( obj == sample:getObj() )

sample:setS( "World" )
print( sample.s )

math = luajava.bindClass("java.lang.Math")
print("Square root of 9 is", math:sqrt(9.0))
