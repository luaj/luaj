
local print = print
print( 'has debug', debug~=nil )

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
