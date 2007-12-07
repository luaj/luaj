package.path = "?.lua;src/test/res/?.lua"
require 'ids'

-- The purpose of this test case is to demonstrate that
-- basic metatable operations on non-table types work.
-- i.e. that s.sub(s,...) could be used in place of string.sub(s,...)
local s = "hello"
print(s:sub(2,4))

local t = {}
function op(name,...)
	a,b,c,d = pcall( setmetatable, t, ... )
	print( name, id(t), id(getmetatable(t)), id(a), id(b), id(c), id(d) )
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
