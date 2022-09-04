
-- tostring replacement that assigns ids
local ts,id,nid,types = tostring,{},0,{table='tbl',thread='thr',userdata='uda',['function']='func'}
tostring = function(x)
	if not x or not types[type(x)] then return ts(x) end
	if not id[x] then nid=nid+1; id[x]=types[type(x)]..'.'..nid end
	return id[x]
end

-- wrap pcall to return one result
-- error message are tested elsewhere
local pc = pcall
local pcall = function(...)
	local s,e = pc(...)
	if s then return e end
	return false, type(e)
end

-- print
print()
print(11)
print("abc",123,nil,"pqr")
print( nil and 'T' or 'F' )
print( false and 'T' or 'F' )
print( 0 and 'T' or 'F' )

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
print( 'collectgarbage("count")', type(collectgarbage("count")))
print( 'collectgarbage("collect")', type(collectgarbage("collect")))
print( 'collectgarbage("count")', type(collectgarbage("count")))

-- dofile (not supported)
-- ipairs
print( 'pcall(ipairs)', pcall(ipairs) )
print( 'pcall(ipairs,nil)', pcall(ipairs,nil) )
print( 'pcall(ipairs,"a")', pcall(ipairs,"a") )
print( 'pcall(ipairs,1)', pcall(ipairs,1) )
for k,v in ipairs({}) do print('ipairs1',k,v)end
for k,v in ipairs({'one','two'}) do print('ipairs2',k,v)end
for k,v in ipairs({aa='aaa',bb='bbb'}) do print('ipairs3',k,v)end
for k,v in ipairs({aa='aaa',bb='bbb','one','two'}) do print('ipairs4',k,v)end
for k,v in ipairs({[30]='30',[20]='20'}) do print('ipairs5',k,v)end

-- load
t = { "print ", "'table ", "loaded'", "", " print'after empty string'" }
i = 0
f = function() i = i + 1; return t[i]; end
c,e = load(f)
if c then print('load: ', pcall(c)) else print('load failed:', e) end

-- loadfile
-- load
local lst = "print(3+4); return 8"
local chunk, err = load( lst )
print( 'load("'..lst..'")', chunk, err )
print( 'load("'..lst..'")()', chunk() ) 

-- pairs
print( 'pcall(pairs)', pcall(pairs) )
print( 'pcall(pairs,nil)', pcall(pairs,nil) )
print( 'pcall(pairs,"a")', pcall(pairs,"a") )
print( 'pcall(pairs,1)', pcall(pairs,1) )
for k,v in pairs({}) do print('pairs1',k,v)end
for k,v in pairs({'one','two'}) do print('pairs2',k,v)end
for k,v in pairs({aa='aaa'}) do print('pairs3',k,v)end
for k,v in pairs({aa='aaa','one','two'}) do print('pairs4',k,v)end
for k,v in pairs({[20]='30',[30]='20'}) do print('pairs5',k,v)end

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

-- rawget,rawset
local mt = {aa="aaa", bb="bbb"}
mt.__index = mt
mt.__newindex = mt
local s = {cc="ccc", dd="ddd", }
local t = {cc="ccc", dd="ddd"}
setmetatable(t,mt)
print( 'pcall(rawget)', pcall(rawget))
print( 'pcall(rawget,"a")', pcall(rawget,"a"))
print( 'pcall(rawget,s)', pcall(rawget,s))
print( 'pcall(rawget,t)', pcall(rawget,t))

function printtables()
	function printtable(name,t)
		print( '  '..name, t["aa"], t["bb"], t["cc"], t["dd"], t["ee"], t["ff"], t["gg"] )
		print( '  '..name, 
			rawget(t,"aa"), 
			rawget(t,"bb"), 
			rawget(t,"cc"), 
			rawget(t,"dd"), 
			rawget(t,"ee"), 
			rawget(t,"ff"), 
			rawget(t,"gg") ) 
	end
	printtable( 's', s )	
	printtable( 't', t )	
	printtable( 'mt', mt )	
end
printtables()
print( 'pcall(rawset,s,"aa","www")', rawset(s,"aa","www"))
printtables()
print( 'pcall(rawset,s,"cc","xxx")', rawset(s,"cc","xxx"))
printtables()
print( 'pcall(rawset,t,"aa","yyy")', rawset(t,"aa","yyy"))
printtables()
print( 'pcall(rawset,t,"dd","zzz")', rawset(t,"dd","zzz"))
printtables()

-- rawlen
print( 'pcall(rawlen, {})', pcall(rawlen, {}))
print( 'pcall(rawlen, {"a"})', pcall(rawlen, {'a'}))
print( 'pcall(rawlen, {"a","b"})', pcall(rawlen, {'a','b'}))
print( 'pcall(rawlen, "")', pcall(rawlen, ""))
print( 'pcall(rawlen, "a")', pcall(rawlen, 'a'))
print( 'pcall(rawlen, "ab")', pcall(rawlen, 'ab'))
print( 'pcall(rawlen, 1)', pcall(rawlen, 1))
print( 'pcall(rawlen, nil)', pcall(rawlen, nil))
print( 'pcall(rawlen)', pcall(rawlen))

printtables()
print( 's["ee"]="ppp"' ); s["ee"]="ppp"
printtables()
print( 's["cc"]="qqq"' ); s["cc"]="qqq"
printtables()
print( 't["ff"]="rrr"' ); t["ff"]="rrr"
printtables()
print( 't["dd"]="sss"' ); t["dd"]="sss"
printtables()
print( 'mt["gg"]="ttt"' ); mt["gg"]="ttt"
printtables()


-- select
print( 'pcall(select)', pcall(select) )
print( 'select(1,11,22,33,44,55)', select(1,11,22,33,44,55) )
print( 'select(2,11,22,33,44,55)', select(2,11,22,33,44,55) )
print( 'select(3,11,22,33,44,55)', select(3,11,22,33,44,55) )
print( 'select(4,11,22,33,44,55)', select(4,11,22,33,44,55) )
print( 'pcall(select,5,11,22,33,44,55)', pcall(select,5,11,22,33,44,55) )
print( 'pcall(select,6,11,22,33,44,55)', pcall(select,6,11,22,33,44,55) )
print( 'pcall(select,7,11,22,33,44,55)', pcall(select,7,11,22,33,44,55) )
print( 'pcall(select,0,11,22,33,44,55)', pcall(select,0,11,22,33,44,55) )
print( 'pcall(select,-1,11,22,33,44,55)', pcall(select,-1,11,22,33,44,55) )
print( 'pcall(select,-2,11,22,33,44,55)', pcall(select,-2,11,22,33,44,55) )
print( 'pcall(select,-4,11,22,33,44,55)', pcall(select,-4,11,22,33,44,55) )
print( 'pcall(select,-5,11,22,33,44,55)', pcall(select,-5,11,22,33,44,55) )
print( 'pcall(select,-6,11,22,33,44,55)', pcall(select,-6,11,22,33,44,55) )
print( 'pcall(select,1)', pcall(select,1) )
print( 'pcall(select,select)', pcall(select,select) )
print( 'pcall(select,{})', pcall(select,{}) )
print( 'pcall(select,"2",11,22,33)', pcall(select,"2",11,22,33) )
print( 'pcall(select,"abc",11,22,33)', pcall(select,"abc",11,22,33) )


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
print( 'pcall(tonumber,"123.456")', pcall(tonumber,"123.456") )
print( 'pcall(tonumber," 123.456")', pcall(tonumber," 123.456") )
print( 'pcall(tonumber," 234qwer")', pcall(tonumber," 234qwer") )
print( 'pcall(tonumber,"0x20")', pcall(tonumber,"0x20") )
print( 'pcall(tonumber," 0x20")', pcall(tonumber," 0x20") )
print( 'pcall(tonumber,"0x20 ")', pcall(tonumber,"0x20 ") )
print( 'pcall(tonumber," 0x20 ")', pcall(tonumber," 0x20 ") )
print( 'pcall(tonumber,"0X20")', pcall(tonumber,"0X20") )
print( 'pcall(tonumber," 0X20")', pcall(tonumber," 0X20") )
print( 'pcall(tonumber,"0X20 ")', pcall(tonumber,"0X20 ") )
print( 'pcall(tonumber," 0X20 ")', pcall(tonumber," 0X20 ") )
print( 'pcall(tonumber,"0x20",10)', pcall(tonumber,"0x20",10) )
print( 'pcall(tonumber,"0x20",16)', pcall(tonumber,"0x20",16) )
print( 'pcall(tonumber,"0x20",8)', pcall(tonumber,"0x20",8) )

-- tostring
print( 'pcall(tostring)', pcall(tostring) )
print( 'pcall(tostring,nil)', pcall(tostring,nil) )
print( 'pcall(tostring,"abc")', pcall(tostring,"abc") )
print( 'pcall(tostring,"abc","def")', pcall(tostring,"abc","def") )
print( 'pcall(tostring,123)', pcall(tostring,123) )
print( 'pcall(tostring,true)', pcall(tostring,true) )
print( 'pcall(tostring,false)', pcall(tostring,false) )
print( 'tostring(tostring)', type(tostring(tostring)) )
print( 'tostring(function() end)', type(tostring(function() end)) )
print( 'tostring({"one","two",a="aa",b="bb"})', type(tostring({"one","two",a="aa",b="bb"})) )

-- _VERSION
print( '_VERSION', type(_VERSION) )

-- xpcall
local errfunc = function( detail ) 
	print( '  in errfunc', type(detail) )
	return 'response-from-xpcall' 
end
local badfunc = function() error( 'error-from-badfunc' ) end
local wrappedbad = function() pcall( badfunc ) end
print( 'pcall(badfunc)', pcall(badfunc) )
print( 'pcall(badfunc,errfunc)', pcall(badfunc,errfunc) )
print( 'pcall(badfunc,badfunc)', pcall(badfunc,badfunc) )
print( 'pcall(wrappedbad)', pcall(wrappedbad) )
print( 'pcall(wrappedbad,errfunc)', pcall(wrappedbad,errfunc) )
print( 'pcall(xpcall(badfunc))', pcall(xpcall,badfunc) )
print( 'pcall(xpcall(badfunc,errfunc))', pcall(xpcall,badfunc,errfunc) )
print( 'pcall(xpcall(badfunc,badfunc))', pcall(xpcall,badfunc,badfunc) )
print( 'pcall(xpcall(wrappedbad))', pcall(xpcall,wrappedbad) )
print( 'xpcall(wrappedbad,errfunc)', xpcall(wrappedbad,errfunc) )

