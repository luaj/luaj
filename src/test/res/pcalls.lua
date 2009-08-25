
-- sample lua function that returns values in reverse order
local function lc(a,b,c)
	return c,b,a
end

-- sample lua function that throws a lua error
local function le(a,b,c)
	error( 'sample error', 0 )
end

-- function that does a plain call to the underlying function
local function cp(f,a,b,c)
	global = f(a,b,c)
	return global
end

-- function that does a tail call to the underlying function
local function ct(f,a,b,c)
	return f(a,b,c)
end

-- wrap pcall to be more useful in testing
local pc = pcall
local pcall = function(...)
	local s,c = pc(...)
	return s, type(c)
end

-- lua calls
print( 'lc(22,33,44)', lc(22,33,44) )
print( 'pcall(lc,22,33,44)', pcall(lc,22,33,44) )
print( 'pcall(le,22,33,44)', pcall(le,22,33,44) )
print( 'cp(lc,22,33,44)', cp(lc,22,33,44) )
print( 'pcall(cp,lc,22,33,44)', pcall(cp,lc,22,33,44) )
print( 'pcall(cp,le,22,33,44)', pcall(cp,le,22,33,44) )
print( 'ct(lc,22,33,44)', ct(lc,22,33,44) )
print( 'pcall(ct,lc,22,33,44)', pcall(ct,lc,22,33,44) )
print( 'pcall(ct,le,22,33,44)', pcall(ct,le,22,33,44) )

print( "assert(true,'a','b','c')", assert( true, 'a', 'b', 'c' ) )
print( "pcall(assert,true,'a','b','c')", pcall(assert, true, 'a', 'b', 'c' ) )
print( "pcall(assert,false,'a','b','c')", pcall(assert, false, 'a', 'b', 'c' ) )

-- more error, pcall tests
print( 'pcall(error)', pcall(error) )
print( 'pcall(error,"msg")', pcall(error,"msg") )
print( 'pcall(error,"msg",1)', pcall(error,"msg",1) )
print( 'pcall(error,"msg",2)', pcall(error,"msg",2) )
local function le(level) 
	error("msg",level) 
end
function ge(level) 
	error("msg",level)
end
for i = 0,4 do
	print( 'pcall(le,i)', i, pcall(le,i) )
	print( 'pcall(ge,i)', i, pcall(ge,i) )
end

-- xpcall tests
local function goodfunction()	return 'from good function' end
local function badfunction()	error( 'from bad function') end
local function goodhandler(msg)	return '-->'..msg..'<--'    end
local function badhandler(msg)	error( 'from bad handler' ) end
print( 'xpcall(error,goodhandler)', ( xpcall(error,goodhandler) ) )
print( 'xpcall(goodfunction,goodhandler)', ( xpcall(goodfunction,goodhandler) ) )
print( 'xpcall(badfunction,goodhandler)', ( xpcall(badfunction,goodhandler) ) )
print( 'xpcall(goodfunction,badhandler)', ( xpcall(goodfunction,badhandler) ) )
print( 'xpcall(badfunction,badhandler)', ( xpcall(badfunction,badhandler) ) )
print( 'xpcall(goodfunction,nil)', ( xpcall(goodfunction,nil) ) )
print( 'xpcall(badfunction,nil)', ( xpcall(badfunction,nil) ) )
print( 'xpcall(goodfunction)', ( pcall( function() return xpcall(goodfunction) end ) ) )
print( 'xpcall(badfunction)', ( pcall( function() return xpcall(badfunction) end ) ) )

