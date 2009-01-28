package.path = "?.lua;src/test/res/?.lua"
require 'ids'

-- The purpose of this test case is to demonstrate that
-- basic metatable operations on non-table types work.
-- i.e. that s.sub(s,...) could be used in place of string.sub(s,...)
local s = "hello"
print(s:sub(2,4))

local t = {}
function op(name,...)
	local a,b = pcall( setmetatable, t, ... )
	print( name, id(t), id(getmetatable(t)), id(a), a and id(b) or type(b) )
end
op('set{}  ',{})
op('set-nil',nil)
op('set{}  ',{})
op('set')
op('set{}  ',{})
op('set{}  ',{})
op('set{}{}',{},{})
op('set-nil',nil)
op('set{__}',{__metatable={}})
op('set{}  ',{})
op('set-nil',nil)
t = {}
op('set{}  ',{})
op('set-nil',nil)
op('set{__}',{__metatable='abc'})
op('set{}  ',{})
op('set-nil',nil)


local i = 1234
local t = setmetatable( {}, {
   	__mode="v",
   	__index=function(t,k)
   	local v = i
   	i = i + 1
   	rawset(t,k,v)
   	return v
   end,
} )

local l = { 'a', 'b', 'a', 'b', 'c', 'a', 'b', 'c', 'd' }
for i,key in ipairs(l) do
	print( 't.'..key, t[key] )
end