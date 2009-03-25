
local print,tostring,_G,pcall,ipairs,isnumber = print,tostring,_G,pcall,ipairs,isnumber
local e,f,g,h,s
print( 'has debug', debug~=nil )
if not debug then error( 'no debug' ) end

print( '----- debug.getfenv, debug.setfenv' )
f = function(a)
	return 'f:'..tostring(a)..'|'..tostring(b)
end
s,e,g = pcall( debug.getfenv, f )
print( s, type(e), type(g), (e==G), pcall( f, 'abc' ) )
s,e,g = pcall( debug.setfenv, f, {b='def'} )
print( s, type(e), type(g), (e==G), pcall( f, 'abc' ) )
s,e,g = pcall( debug.getfenv, f )
print( s, type(e), type(g), (e==G), pcall( f, 'abc' ) )


print( '----- debug.getlocal, debug.setlocal' )
h = function(v,i,n)
	s = 'h-'..v..'-'..i
	local x1,y1 = debug.getlocal(v,i)
	local x2,y2 = debug.setlocal(v,i,n)
	local x3,y3 = debug.getlocal(v,i)
	return s..' -> '..v..'-'..i..' '.. 
		'get='..tostring(x1)..','..tostring(y1)..' '.. 
		'set='..tostring(x2)..','..tostring(y2)..' '.. 
		'get='..tostring(x3)..','..tostring(y3)..' ' 
end
g = function(...)
	local p,q,r=7,8,9
	local t = h(...)
	local b = table.concat({...},',')
	return t..'\tg locals='..p..','..q..','..r..' tbl={'..b..'}'
end
f = function(a,b,c)
	local d,e,f = 4,5,6
	local t = g(a,b,c)
	return t..'\tf locals='..','..a..','..b..','..c..','..d..','..e..','..f
end
do lvl=1,1
	for lcl=3,7 do
		print( pcall( f, lvl, lcl, '#' ) ) 
	end
end
do lvl=2,3
	for lcl=0,7 do
		print( pcall( f, lvl, lcl, '#' ) ) 
	end
end


print( '----- debug.getupvalue, debug.setupvalue' )
local m,n,o = 101,102,103
f = function(p,q,r)
	local p,q,r = 104,105,106
	local g = function(s,t,u)
		local v,w,x = 107,108,109
		return function()
			return m,n,o,p,q,r,v,w,x
		end
	end
	return g
end
g = f()
h = g()
local callh = function()
	local t = {}
	for i,v in ipairs( { pcall(h) } ) do
		t[i] = tostring(v)
	end
	return table.concat(t,',')
end 
print( 'h', h() )
local funs = { f, g, h }
local names = { 'f', 'g', 'h' }
for i=1,3 do
	local fun,name = funs[i],names[i]	
	for index=0,10 do
		local s1,x1,y1 = pcall( debug.getupvalue, fun, index )
		local s2,x2,y2 = pcall( debug.setupvalue, fun, index, 666000+i*111000+index )
		local s3,x3,y3 = pcall( debug.getupvalue, fun, index )
		print( name..' -> '..i..'-'..index..' '.. 
			'get='..tostring(s1)..','..tostring(x1)..','..tostring(y1)..' '.. 
			'set='..tostring(s2)..','..tostring(x2)..','..tostring(y2)..' '.. 
			'get='..tostring(s3)..','..tostring(x3)..','..tostring(y3)..' '..
			'tbl='..callh() )
	end
end

print( '----- debug.setmetatable, debug.getmetatable' )
local a = {a='bbb'}
local b = {}
local mt = {__index={b='ccc'}}
print( 'a.a='..tostring(a.a)..' a.b='..tostring(a.b)..' b.a='..tostring(b.a)..' b.b='..tostring(b.b)) 
local s1,x1,y1 = pcall( debug.getmetatable, a )
local s2,x2,y2 = pcall( debug.setmetatable, a, mt )
print( 'a.a='..tostring(a.a)..' a.b='..tostring(a.b)..' b.a='..tostring(b.a)..' b.b='..tostring(b.b)) 
local s3,x3,y3 = pcall( debug.getmetatable, a )
local s4,x4,y4 = pcall( debug.getmetatable, b )
local s5,x5,y5 = pcall( debug.setmetatable, a, nil )
print( 'a.a='..tostring(a.a)..' a.b='..tostring(a.b)..' b.a='..tostring(b.a)..' b.b='..tostring(b.b)) 
local s6,x6,y6 = pcall( debug.getmetatable, a )
if not s1 then print( 's1 error', x1 ) end
if not s2 then print( 's2 error', x2 ) end
if not s3 then print( 's3 error', x3 ) end
if not s4 then print( 's4 error', x4 ) end
if not s5 then print( 's5 error', x5 ) end
if not s6 then print( 's6 error', x6 ) end
print( 'get='..tostring(s1)..','..tostring(x1==nil)..','..tostring(y1) )
print( 'set='..tostring(s2)..','..tostring(x2==a)..','..tostring(y2) ) 
print( 'get='..tostring(s3)..','..tostring(x3==mt)..','..tostring(y3) ) 
print( 'get='..tostring(s4)..','..tostring(x4==nil)..','..tostring(y4) ) 
print( 'set='..tostring(s5)..','..tostring(x5==a)..','..tostring(y5) ) 
print( 'get='..tostring(s6)..','..tostring(x6==nil)..','..tostring(y6) ) 
print( pcall( debug.getmetatable, 1 ) )
-- print( pcall( debug.setmetatable, 1, {} ) )
-- print( pcall( debug.setmetatable, 1, nil ) )


--[[
local f = function(a)
	return 'f:'..tostring(a)..'|'..tostring(b)
end
local s,e


local printinfo = function(...)
	for i,a in ipairs(arg) do
		if type(a) == 'table' then
			print( '   source: '..tostring(a.source) )
			print( '   short_src: '..tostring(a.short_src) )
			print( '   what: '..tostring(a.what) )
			print( '   currentline: '..tostring(a.currentline) )
			print( '   linedefined: '..tostring(a.linedefined) )
			print( '   lastlinedefined: '..tostring(a.lastlinedefined) )
		else
			print( tostring(a) )
		end
	end
end

function test()
	local x = 5
	function f()
		x = x + 1
		return x
	end
	function g()
		x = x - 1
		return x
	end
	print(f())
	print(g())
	return f, g
end

local e,f,g = pcall( test )
print( 'e,f,g', e, type(f), type(g) )

printinfo( 'debug.getinfo(f,"Sl")', pcall(debug.getinfo, f, "Sl") ) 
printinfo( 'debug.getinfo(g,"Sl")', pcall(debug.getinfo, g, "Sl") ) 
printinfo( 'debug.getinfo(test,"Sl")', pcall(debug.getinfo, test, "Sl") )
--]]