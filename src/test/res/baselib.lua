-- unit tests for functions in BaseLib.java

-- error, pcall
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
-- print
-- rawget
-- rawset
-- select
-- tonumber
print( 'pcall(tonumber)', pcall(tostring) )
print( 'pcall(tonumber,nil)', pcall(tonumber,nil) )
print( 'pcall(tonumber,"abc")', pcall(tonumber,"abc") )
print( 'pcall(tonumber,"123")', pcall(tonumber,"123") )
print( 'pcall(tonumber,"123",10)', pcall(tonumber,"123", 10) )
print( 'pcall(tonumber,"123",8)', pcall(tonumber,"123", 8) )
print( 'pcall(tonumber,"123",6)', pcall(tonumber,"123", 6) )
print( 'pcall(tonumber,"10101",4)', pcall(tonumber,"10101", 4) )
print( 'pcall(tonumber,"10101",3)', pcall(tonumber,"10101", 3) )
print( 'pcall(tonumber,"10101",2)', pcall(tonumber,"10101", 2) )
print( 'pcall(tonumber,"1a1",16)', pcall(tonumber,"1a1", 16) )
print( 'pcall(tonumber,"1a1",32)', pcall(tonumber,"1a1", 32) )
print( 'pcall(tonumber,"1a1",54)', pcall(tonumber,"1a1", 54) )
print( 'pcall(tonumber,"1a1",1)', pcall(tonumber,"1a1", 1) )
print( 'pcall(tonumber,"1a1",0)', pcall(tonumber,"1a1", 0) )
print( 'pcall(tonumber,"1a1",-1)', pcall(tonumber,"1a1", -1) )
print( 'pcall(tonumber,"1a1","32")', pcall(tonumber,"1a1", "32") )
print( 'pcall(tonumber,"123","456")', pcall(tonumber,"123","456") )
print( 'pcall(tonumber,"1a1",10)', pcall(tonumber,"1a1", 10) )
print( 'pcall(tonumber,"151",4)', pcall(tonumber,"151", 4) )
print( 'pcall(tonumber,"151",3)', pcall(tonumber,"151", 3) )
print( 'pcall(tonumber,"151",2)', pcall(tonumber,"151", 2) )
print( 'pcall(tonumber,"123",8,8)', pcall(tonumber,"123", 8, 8) )
print( 'pcall(tonumber,123)', pcall(tonumber,123) )
print( 'pcall(tonumber,true)', pcall(tonumber,true) )
print( 'pcall(tonumber,false)', pcall(tonumber,false) )
print( 'pcall(tonumber,tonumber)', pcall(tonumber,tonumber) )
print( 'pcall(tonumber,function() end)', pcall(tonumber,function() end) )
print( 'pcall(tonumber,{"one","two",a="aa",b="bb"})', pcall(tonumber,{"one","two",a="aa",b="bb"}) )

-- tostring
print( 'pcall(tostring)', pcall(tostring) )
print( 'pcall(tostring,nil)', pcall(tostring,nil) )
print( 'pcall(tostring,"abc")', pcall(tostring,"abc") )
print( 'pcall(tostring,"abc","def")', pcall(tostring,"abc","def") )
print( 'pcall(tostring,123)', pcall(tostring,123) )
print( 'pcall(tostring,true)', pcall(tostring,true) )
print( 'pcall(tostring,false)', pcall(tostring,false) )
print( 'tostring(tostring):sub(1,10)', tostring(tostring):sub(1,10) )
print( 'tostring(function() end)', tostring(function() end):sub(1,10) )
print( 'tostring({"one","two",a="aa",b="bb"})', tostring({"one","two",a="aa",b="bb"}):sub(1,7) )

-- unpack
print( 'pcall(unpack)', pcall(unpack) );
print( 'pcall(unpack,nil)', pcall(unpack,nil) );
print( 'pcall(unpack,"abc")', pcall(unpack,"abc") );
print( 'pcall(unpack,1)', pcall(unpack,1) );
print( 'unpack({"aa"})', unpack({"aa"}) );
print( 'unpack({"aa","bb"})', unpack({"aa","bb"}) );
print( 'unpack({"aa","bb","cc"})', unpack({"aa","bb","cc"}) );
local t = {"aa","bb","cc","dd","ee","ff"}
print( 'pcall(unpack,t)', pcall(unpack,t) );
print( 'pcall(unpack,t,2)', pcall(unpack,t,2) );
print( 'pcall(unpack,t,2,5)', pcall(unpack,t,2,5) );
print( 'pcall(unpack,t,2,6)', pcall(unpack,t,2,6) );
print( 'pcall(unpack,t,2,7)', pcall(unpack,t,2,7) );
print( 'pcall(unpack,t,1)', pcall(unpack,t,1) );
print( 'pcall(unpack,t,1,5)', pcall(unpack,t,1,5) );
print( 'pcall(unpack,t,1,6)', pcall(unpack,t,1,6) );
print( 'pcall(unpack,t,1,7)', pcall(unpack,t,1,7) );
print( 'pcall(unpack,t,0)', pcall(unpack,t,0) );
print( 'pcall(unpack,t,0,5)', pcall(unpack,t,0,5) );
print( 'pcall(unpack,t,0,6)', pcall(unpack,t,0,6) );
print( 'pcall(unpack,t,0,7)', pcall(unpack,t,0,7) );
print( 'pcall(unpack,t,-1)', pcall(unpack,t,-1) );
print( 'pcall(unpack,t,-1,5)', pcall(unpack,t,-1,5) );
print( 'pcall(unpack,t,-1,6)', pcall(unpack,t,-1,6) );
print( 'pcall(unpack,t,-1,7)', pcall(unpack,t,-1,7) );
print( 'pcall(unpack,t,2,4)', pcall(unpack,t,2,4) );
print( 'pcall(unpack,t,2,5)', pcall(unpack,t,2,5) );
print( 'pcall(unpack,t,2,6)', pcall(unpack,t,2,6) );
print( 'pcall(unpack,t,2,7)', pcall(unpack,t,2,7) );
print( 'pcall(unpack,t,2,8)', pcall(unpack,t,2,8) );
print( 'pcall(unpack,t,2,2)', pcall(unpack,t,2,0) );
print( 'pcall(unpack,t,2,1)', pcall(unpack,t,2,0) );
print( 'pcall(unpack,t,2,0)', pcall(unpack,t,2,0) );
print( 'pcall(unpack,t,2,-1)', pcall(unpack,t,2,-1) );
t[0] = 'zz'
t[-1] = 'yy'
t[-2] = 'xx'
print( 'pcall(unpack,t,0)', pcall(unpack,t,0) );
print( 'pcall(unpack,t,2,0)', pcall(unpack,t,2,0) );
print( 'pcall(unpack,t,2,-1)', pcall(unpack,t,2,-1) );
print( 'pcall(unpack,t,"3")', pcall(unpack,t,"3") );
print( 'pcall(unpack,t,"a")', pcall(unpack,t,"a") );
print( 'pcall(unpack,t,function() end)', pcall(unpack,t,function() end) );

-- _VERSION
print( '_VERSION', _VERSION )
