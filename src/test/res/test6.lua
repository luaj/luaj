package.path = "?.lua;src/test/res/?.lua"
require 'ids'

t = { 11, 22, 33, you='one', me='two' }

print( "---------" )
for a,b in pairs(t) do
	print( id(a), id(b) )
end
print( "----" )
print( "t[2]", t[2] )
print( "t.me", t.me )
print( "t.fred", t.fred )
print( "t['me']", t["me"] )
print( "t['fred']", t["fred"] )
print( "me", me )
print( "fred", fred )
print( "t[me]", t[me] )
print( "t[fred]", t[fred] )
function out(t,...)
	print(type(t),...)
end
-- basic metatable setting 
t = { 11, 22, 33, you='one', me='two' }
mt = { __index = out, __newindex = out }
setmetatable(t,mt)
a = t[11]
b = t.one
c = t[1]
d = t.you
t[4] = 'rat'
t[1] = 'pipe'
print( a, b, c, d )
print( "---------" )
for a,b in pairs(t) do
	print( id(a), id(b) )
end
print( "----" )

-- delegate to actual metatable
s = { }
mt = { __newindex = s, __index = _G }
setmetatable(t, mt)
print( t.you )
x = 'wow'
print( t.x )
me = 'here'
print( t.me )
t[5] = 99
print( "---------" )
for a,b in pairs(s) do
	print( id(a), id(b) )
end
print( "----" )

Vector = {}
Vector_mt = { __index = Vector }

function Vector:new(x,y)
   return setmetatable( {x=x, y=y}, Vector_mt)
end
 
function Vector:mag()
   return math.sqrt(self:dot(self))
 end
 
function Vector:dot(v)
   return self.x * v.x + self.y * v.y
end
 
v1 = Vector:new(3,4)
print( "---------" )
for a,b in pairs(v1) do
	print( id(a), id(b) )
end

print( "----" )

v2 = Vector:new(2,1)
print( v2:dot(v1) )

print( id(Vector) )

print( "=======" )
for a,b in spairs(Vector) do
	print( id(a), id(b) )
end
print( "----" )

print( id(v1), id(v2) )

print( id(Vector_mt), id(getmetatable(v1)), id(getmetatable(v2)) )

print( "---------" )
for a,b in pairs(Vector_mt) do
	print( id(a), id(b) )
end
print( "----" )

