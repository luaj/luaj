print( '---- initial metatables' )
local anumber   = 111
local aboolean  = false
local afunction = function() end
local athread   = coroutine.create( afunction )
local values = { athread, aboolean, afunction, athread }
for i=1,#values do
	print( debug.getmetatable( values[i] ) )
end
local ts = tostring
tostring = function(o)
	local t = type(o)
	return (t=='thread' or t=='function') and t or ts(o)
end

local buildbin = function(name)
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
	__add=buildbin('add'),
	__sub=buildbin('sub'),
	__mul=buildbin('mul'),
	__div=buildbin('div'),
	__pow=buildbin('pow'),
	__mod=buildbin('mod'),
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



print( '---- final metatables' )
for i=1,#values do 
	print( debug.getmetatable( values[i] ) )
end
	