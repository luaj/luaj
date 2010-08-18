print( '---- initial metatables' )
local values = { 1, false, coroutine.create( function() end ) }
for i=1,#values do
	print( debug.getmetatable( values[i] ) )
end

local mt = {
	__call=function(a,b,c) 
		print( 'mt.__call()', type(a), type(b), type(c), b, c )
		return '__call-result'
	end,
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



print( '---- final metatables' )
for i=1,#values do 
	print( debug.getmetatable( values[i] ) )
end
	