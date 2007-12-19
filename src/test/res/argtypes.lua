-- object ids
package.path = "?.lua;src/test/res/?.lua"
require 'ids'

local names = {
	string= { sub=2, },
	math= { ceil=1, floor=1 },
	table={ insert=2, remove=2 },
}

local args = { 'str', 123, {}, function() end, print, nil }

local globals = _G
local ipairs = ipairs
local pairs = pairs

local function f( pkg, name, count ) 
	print( '-----'..pkg..'.'..name..'-----' )
	if not globals[pkg] then
		print( 'package not found: '..pkg )
		return
	end
	if not globals[pkg][name] then
		print( 'function not found: '..pkg..'.'..name )
		return
	end
	local function g( ... )
		return globals[pkg][name](...)
	end
	print( pcall( g ) )
	if count > 0 then
		for i,arg1 in ipairs(args) do
			print( pcall( g, arg1 ) )
			if count > 1 then
				for j,arg2 in ipairs(args) do
					print( pcall( g, arg1, arg2 ) )
				end
			end
		end
	end
end

function sortedkeys(t)
	local list = {}
	for k,v in pairs(t) do
		table.insert(list,k)
	end
	table.sort(list)
	return list
end
for j,pkg in ipairs(sortedkeys(names)) do
	local t = names[pkg]
	for i,name in ipairs(sortedkeys(t)) do
		local nargs = t[name]
		f( pkg, name, nargs )
	end
end