-- The point of this test is that when an upvalue is created, it may
-- need to be inserted in the middle of the list, rather than always
-- appended at the end. Otherwise, it may not be found when it is
-- needed by another closure.

local function test()
   local x = 3
   local y = 5
   local z = 7
   
   local function f()
      print("y=", y)
   end
   
   local function g()
      print("z=", z)
   end
   
   local function h()
      print("x=", x)
   end
   
   local function setter(x1, y1, z1)
      x = x1
      y = y1
      z = z1
   end
   
   return f, g, h, setter
end

local f, g, h, setter = test()

h()
f()
g()

setter("x", "y", "z")

h()
f()
g()
