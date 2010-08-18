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
	if t~='thread' and t~='function' then return ts(o) end
	if not tb[o] then
		count = count + 1
		tb[o] = t..'.'..count
	end
	return tb[o]
end

local buildunop = function(name)
	return function(a)
		print( 'mt.__'..name..'()', type(a), a )
		return '__'..name..'-result'
	end
end

local buildbinop = function(name)
	return function(a,b)
		print( 'mt.__'..name..'()', type(a), type(b), a, b )
		return '__'..name..'-result'
	end
end

local mt = {
	__call=function(a,b,c) 
		print( 'mt.__call()', type(a), type(b), type(c), b, c )
		return '__call-result'
	end,
	__add=buildbinop('add'),
	__sub=buildbinop('sub'),
	__mul=buildbinop('mul'),
	__div=buildbinop('div'),
	__pow=buildbinop('pow'),
	__mod=buildbinop('mod'),
	__unm=buildunop('unm'),
	__len=buildunop('neg'),
	__eq=buildbinop('eq'),
	__lt=buildbinop('lt'),
	__le=buildbinop('le'),
}

-- pcall a function and check for a pattern in the error string
ecall = function(pattern, ...)
	local s,e = pcall(...)
	if not s then e = string.match(e,pattern) or e end
	return s,e
end 

print( '---- __call' )
for i=1,#values do
	print( type(values[i]), 'before', ecall( 'attempt to call', function() return values[i]('a','b') end ) )
	print( debug.setmetatable( values[i], mt ) )
	print( type(values[i]), 'after', pcall( function() return values[i]() end ) )
	print( type(values[i]), 'after', pcall( function() return values[i]('a') end ) )
	print( type(values[i]), 'after', pcall( function() return values[i]('a','b') end ) )
	print( type(values[i]), 'after', pcall( function() return values[i]('a','b','c') end ) )
	print( type(values[i]), 'after', pcall( function() return values[i]('a','b','c','d') end ) )
	print( debug.setmetatable( values[i], nil ) )
end

print( '---- __add, __sub, __mul, __div, __pow, __mod' )
local groups = { {aboolean, aboolean}, {aboolean, athread}, {aboolean, afunction}, {aboolean, "abc"} }
for i=1,#groups do
	local a,b = groups[i][1], groups[i][2]
	print( type(values[i]), 'before', ecall( 'attempt to perform arithmetic', function() return a+b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to perform arithmetic', function() return b+a end ) )
	print( type(values[i]), 'before', ecall( 'attempt to perform arithmetic', function() return a-b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to perform arithmetic', function() return b-a end ) )
	print( type(values[i]), 'before', ecall( 'attempt to perform arithmetic', function() return a*b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to perform arithmetic', function() return b*a end ) )
	print( type(values[i]), 'before', ecall( 'attempt to perform arithmetic', function() return a^b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to perform arithmetic', function() return b^a end ) )
	print( type(values[i]), 'before', ecall( 'attempt to perform arithmetic', function() return a%b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to perform arithmetic', function() return b%a end ) )
	print( debug.setmetatable( a, mt ) )
	print( type(values[i]), 'after', pcall( function() return a+b end ) )
	print( type(values[i]), 'after', pcall( function() return b+a end ) )
	print( type(values[i]), 'after', pcall( function() return a-b end ) )
	print( type(values[i]), 'after', pcall( function() return b-a end ) )
	print( type(values[i]), 'after', pcall( function() return a*b end ) )
	print( type(values[i]), 'after', pcall( function() return b*a end ) )
	print( type(values[i]), 'after', pcall( function() return a^b end ) )
	print( type(values[i]), 'after', pcall( function() return b^a end ) )
	print( type(values[i]), 'after', pcall( function() return a%b end ) )
	print( type(values[i]), 'after', pcall( function() return b%a end ) )
	print( debug.setmetatable( a, nil ) )
end

print( '---- __len' )
values = { aboolean, afunction, athread }
for i=1,#values do
	print( type(values[i]), 'before', ecall( 'attempt to get length of ', function() return #values[i] end ) )
	print( debug.setmetatable( values[i], mt ) )
	print( type(values[i]), 'after', pcall( function() return #values[i] end ) )
	print( debug.setmetatable( values[i], nil ) )
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
	print( type(values[i]), 'before', pcall( function() return a==b end ) )
	print( type(values[i]), 'before', pcall( function() return a~=b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to compare', function() return a<b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to compare', function() return a<=b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to compare', function() return a>b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to compare', function() return a>=b end ) )
	print( debug.setmetatable( a, mt ) )
	print( debug.setmetatable( b, mt ) )
	print( type(values[i]), 'after', pcall( function() return a==b end ) )
	print( type(values[i]), 'after', pcall( function() return a~=b end ) )
	print( type(values[i]), 'after', pcall( function() return a<b end ) )
	print( type(values[i]), 'after', pcall( function() return a<=b end ) )
	print( type(values[i]), 'after', pcall( function() return a>b end ) )
	print( type(values[i]), 'after', pcall( function() return a>=b end ) )
	print( debug.setmetatable( a, nil ) )
	print( debug.setmetatable( b, nil ) )
end
print( '---- __eq, __lt, __le, different types' )
groups = { {aboolean, athread}, }
for i=1,#groups do
	local a,b = groups[i][1], groups[i][2]
	print( type(values[i]), 'before', pcall( function() return a==b end ) )
	print( type(values[i]), 'before', pcall( function() return a~=b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to compare', function() return a<b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to compare', function() return a<=b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to compare', function() return a>b end ) )
	print( type(values[i]), 'before', ecall( 'attempt to compare', function() return a>=b end ) )
	print( debug.setmetatable( a, mt ) )
	print( debug.setmetatable( b, mt ) )
	print( type(values[i]), 'after-a', pcall( function() return a==b end ) )
	print( type(values[i]), 'after-a', pcall( function() return a~=b end ) )
	print( type(values[i]), 'after-a', ecall( 'attempt to compare', function() return a<b end ) )
	print( type(values[i]), 'after-a', ecall( 'attempt to compare', function() return a<=b end ) )
	print( type(values[i]), 'after-a', ecall( 'attempt to compare', function() return a>b end ) )
	print( type(values[i]), 'after-a', ecall( 'attempt to compare', function() return a>=b end ) )
	print( debug.setmetatable( a, nil ) )
	print( debug.setmetatable( b, nil ) )
end
