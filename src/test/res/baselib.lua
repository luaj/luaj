-- unit tests for functions in BaseLib.java

-- assert
print( 'assert(true)', assert(true) )
print( 'pcall(assert,true)', pcall(assert,true) )
print( 'pcall(assert,false)', pcall(assert,false) )
print( 'pcall(assert,nil)', pcall(assert,nil) )
print( 'pcall(assert,true,"msg")', pcall(assert,true,"msg") )
print( 'pcall(assert,false,"msg")', pcall(assert,false,"msg") )
print( 'pcall(assert,nil,"msg")', pcall(assert,nil,"msg") )
print( 'pcall(assert,false,"msg","msg2")', pcall(assert,false,"msg","msg2") )

-- collectgarbage (not supported)
-- dofile (not supported)

-- error
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

-- _G
print( '_G["abc"] (before)', _G["abc"] )
abc='def'
print( '_G["abc"] (after)', _G["abc"] )

-- type
print( 'type(nil)', type(nil) )
print( 'type("a")', type("a") )
print( 'type(1)', type(1) )
print( 'type(1.5)', type(1.5) )
print( 'type(function() end)', type(function() end) )
print( 'type({})', type({}) )
print( 'type(true)', type(true) )
print( 'type(false)', type(false) )
print( 'pcall(type,type)', pcall(type,type) )
print( 'pcall(type)', pcall(type) )
print( '(function() return pcall(type) end)()', (function() return pcall(type) end)() )
local function la()	return pcall(type) end
print( 'la()', la() )
function ga() return pcall(type) end
print( 'ga()', ga() )

-- getfenv, setfenv: tested in setfenv.lua
-- getmetatable, setmetatable
ta = { aa1="aaa1", aa2="aaa2" }
tb = { bb1="bbb1", bb2="bbb2" }
print( 'getmetatable(ta)', getmetatable(ta) )
print( 'getmetatable(tb)', getmetatable(tb) )
print( 'setmetatable(ta),{cc1="ccc1"}', type( setmetatable(ta,{cc1="ccc1"}) ) )
print( 'setmetatable(tb),{dd1="ddd1"}', type( setmetatable(tb,{dd1="ddd1"}) ) )
print( 'getmetatable(ta)["cc1"]', getmetatable(ta)["cc1"] )
print( 'getmetatable(tb)["dd1"]', getmetatable(tb)["dd1"] )
print( 'getmetatable(1)', getmetatable(1) )
print( 'pcall(setmetatable,1)', pcall(setmetatable,1) )
print( 'pcall(setmetatable,nil)', pcall(setmetatable,nil) )
print( 'pcall(setmetatable,"ABC")', pcall(setmetatable,"ABC") )
print( 'pcall(setmetatable,function() end)', pcall(setmetatable,function() end) )

-- ipairs
-- load
-- loadfile
-- loadstring
-- next
-- pairs
-- pcall
-- print
-- rawget
-- rawset
-- select
-- tonumber
-- tostring
-- unpack
--[[
print( 'pcall(unpack)', pcall(unpack) );
print( 'unpack({"aa"})', unpack({"aa"}) );
print( 'unpack({"aa","bb"})', unpack({"aa","bb"}) );
print( 'unpack({"aa","bb","cc"})', unpack({"aa","bb","cc"}) );

-- _VERSION
print( '_VERSION', _VERSION )
--]]
