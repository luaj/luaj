
local print,tostring,_G = print,tostring,_G
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