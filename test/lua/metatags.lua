print( '---- initial metatables' )
local anumber   = 111
local aboolean  = false
local afunction = function() end
local athread   = coroutine.create( afunction )
local values = { anumber, aboolean, afunction, athread }
for i=1,#values do
	print( debug.getmetatable( values[i] ) )
end
local ts = tostring
local tb,count = {},0
tostring = function(o)
	local t = type(o)
	if t~='thread' and t~='function' and t~='table' then return ts(o) end
	if not tb[o] then
		count = count + 1
		tb[o] = t..'.'..count
	end
	return tb[o]
end

local buildop = function(name)
	return function(a,b,c)
		print( 'mt.__'..name..'()', a, b, c )
		return '__'..name..'-result'
	end
end

local mt = {
	__call=buildop('call'),
	__add=buildop('add'),
	__sub=buildop('sub'),
	__mul=buildop('mul'),
	__div=buildop('div'),
	__pow=buildop('pow'),
	__mod=buildop('mod'),
	__unm=buildop('unm'),
	__len=buildop('neg'),
	__eq=buildop('eq'),
	__lt=buildop('lt'),
	__le=buildop('le'),
	__tostring=function(a,b)
		return 'mt.__tostring('..type(a)..','..type(b)..')'
	end,
	__metatable={},
	__index=buildop('index'),
	__newindex=buildop('newindex'),
}

-- pcall a function and check for a pattern in the error string
ecall = function(pattern, ...)
	local s,e = pcall(...)
	if not s then e = string.match(e,pattern) or e end
	return s,e
end 

print( '---- __call' )
for i=1,#values do
	local a = values[i]
	print( type(a), 'before', ecall( 'attempt to call', function() return a('a','b') end ) )
	print( debug.setmetatable( a, mt ) )
	print( type(a), 'after', pcall( function() return a() end ) )
	print( type(a), 'after', pcall( function() return a('a') end ) )
	print( type(a), 'after', pcall( function() return a('a','b') end ) )
	print( type(a), 'after', pcall( function() return a('a','b','c') end ) )
	print( type(a), 'after', pcall( function() return a('a','b','c','d') end ) )
	print( debug.setmetatable( a, nil ) )
end

print( '---- __add, __sub, __mul, __div, __pow, __mod' )
local groups = { {aboolean, aboolean}, {aboolean, athread}, {aboolean, afunction}, {aboolean, "abc"} }
for i=1,#groups do
	local a,b = groups[i][1], groups[i][2]
	print( type(a), type(b), 'before', ecall( 'attempt to perform arithmetic', function() return a+b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to perform arithmetic', function() return b+a end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to perform arithmetic', function() return a-b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to perform arithmetic', function() return b-a end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to perform arithmetic', function() return a*b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to perform arithmetic', function() return b*a end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to perform arithmetic', function() return a^b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to perform arithmetic', function() return b^a end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to perform arithmetic', function() return a%b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to perform arithmetic', function() return b%a end ) )
	print( debug.setmetatable( a, mt ) )
	print( type(a), type(b), 'after', pcall( function() return a+b end ) )
	print( type(a), type(b), 'after', pcall( function() return b+a end ) )
	print( type(a), type(b), 'after', pcall( function() return a-b end ) )
	print( type(a), type(b), 'after', pcall( function() return b-a end ) )
	print( type(a), type(b), 'after', pcall( function() return a*b end ) )
	print( type(a), type(b), 'after', pcall( function() return b*a end ) )
	print( type(a), type(b), 'after', pcall( function() return a^b end ) )
	print( type(a), type(b), 'after', pcall( function() return b^a end ) )
	print( type(a), type(b), 'after', pcall( function() return a%b end ) )
	print( type(a), type(b), 'after', pcall( function() return b%a end ) )
	print( debug.setmetatable( a, nil ) )
end

print( '---- __len' )
values = { aboolean, afunction, athread }
for i=1,#values do
	local a = values[i]
	print( type(a), 'before', ecall( 'attempt to get length of ', function() return #a end ) )
	print( debug.setmetatable( a, mt ) )
	print( type(a), 'after', pcall( function() return #a end ) )
	print( debug.setmetatable( a, nil ) )
end

print( '---- __neg' )
values = { aboolean, afunction, athread, "abcd" }
for i=1,#values do
	print( type(values[i]), 'before', ecall( 'attempt to get length of ', function() return #values[i] end ) )
	print( debug.setmetatable( values[i], mt ) )
	print( type(values[i]), 'after', pcall( function() return #values[i] end ) )
	print( debug.setmetatable( values[i], nil ) )
end

print( '---- __eq, __lt, __le, same types' )
local bfunction = function() end
local bthread = coroutine.create( bfunction )
local groups 
groups = { {afunction, bfunction}, {true, true}, {true, false}, {afunction, bfunction}, {athread, bthread}, }
for i=1,#groups do
	local a,b = groups[i][1], groups[i][2]
	print( type(a), type(b), 'before', pcall( function() return a==b end ) )
	print( type(a), type(b), 'before', pcall( function() return a~=b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to compare', function() return a<b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to compare', function() return a<=b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to compare', function() return a>b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to compare', function() return a>=b end ) )
	print( debug.setmetatable( a, mt ) )
	print( debug.setmetatable( b, mt ) )
	print( type(a), type(b), 'after', pcall( function() return a==b end ) )
	print( type(a), type(b), 'after', pcall( function() return a~=b end ) )
	print( type(a), type(b), 'after', pcall( function() return a<b end ) )
	print( type(a), type(b), 'after', pcall( function() return a<=b end ) )
	print( type(a), type(b), 'after', pcall( function() return a>b end ) )
	print( type(a), type(b), 'after', pcall( function() return a>=b end ) )
	print( debug.setmetatable( a, nil ) )
	print( debug.setmetatable( b, nil ) )
end

print( '---- __eq, __lt, __le, different types' )
groups = { {aboolean, athread}, }
for i=1,#groups do
	local a,b = groups[i][1], groups[i][2]
	print( type(a), type(b), 'before', pcall( function() return a==b end ) )
	print( type(a), type(b), 'before', pcall( function() return a~=b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to compare', function() return a<b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to compare', function() return a<=b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to compare', function() return a>b end ) )
	print( type(a), type(b), 'before', ecall( 'attempt to compare', function() return a>=b end ) )
	print( debug.setmetatable( a, mt ) )
	print( debug.setmetatable( b, mt ) )
	print( type(a), type(b), 'after-a', pcall( function() return a==b end ) )
	print( type(a), type(b), 'after-a', pcall( function() return a~=b end ) )
	print( type(a), type(b), 'after-a', ecall( 'attempt to compare', function() return a<b end ) )
	print( type(a), type(b), 'after-a', ecall( 'attempt to compare', function() return a<=b end ) )
	print( type(a), type(b), 'after-a', ecall( 'attempt to compare', function() return a>b end ) )
	print( type(a), type(b), 'after-a', ecall( 'attempt to compare', function() return a>=b end ) )
	print( debug.setmetatable( a, nil ) )
	print( debug.setmetatable( b, nil ) )
end

print( '---- __tostring' )
values = { aboolean, afunction, athread }
for i=1,#values do
	local a = values[i]
	print( debug.setmetatable( a, mt ) )
	print( type(a), 'after', pcall( function() return ts(a) end ) )
	print( debug.setmetatable( a, nil ) )
end

print( '---- __metatable' )
values = { aboolean, afunction, athread }
for i=1,#values do
	local a = values[i]
	print( type(a), 'before', pcall( function() return debug.getmetatable(a), getmetatable(a) end ) )
	print( debug.setmetatable( a, mt ) )
	print( type(a), 'after', pcall( function() return debug.getmetatable(a), getmetatable(a) end ) )
	print( debug.setmetatable( a, nil ) )
end

print( '---- __index, __newindex' )
values = { aboolean, anumber, afunction, athread }
for i=1,#values do
	local a = values[i]
	print( type(a), 'before', ecall( 'attempt to index', function() return a.foo end ) )
	print( type(a), 'before', ecall( 'attempt to index', function() return a[123] end ) )
	print( type(a), 'before', ecall( 'index', function() a.foo = 'bar' end ) )
	print( type(a), 'before', ecall( 'index', function() a[123] = 'bar' end ) )
	print( type(a), 'before', ecall( 'attempt to index', function() return a:foo() end ) )
	print( debug.setmetatable( a, mt ) )
	print( type(a), 'after', pcall( function() return a.foo end ) )
	print( type(a), 'after', pcall( function() return a[123] end ) )
	print( type(a), 'after', pcall( function() a.foo = 'bar' end ) )
	print( type(a), 'after', pcall( function() a[123] = 'bar' end ) )
	print( type(a), 'after', ecall( 'attempt to call', function() return a:foo() end ) )
	print( debug.setmetatable( a, nil ) )
end

