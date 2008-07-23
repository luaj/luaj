

-- unit tests for module() function
local ids = {}
local function id(obj)
	if not obj or type(obj) == 'number' or type(obj) == 'string' then
		return obj
	end
	local v = ids[obj]
	if v then
		return v
	end
	table.insert(ids,obj)
	ids[obj] = type(obj)..'.'..tostring(#ids)
	return ids[obj]
end 

-- module tests
local pr = print
local pkg = package
local g = _G
local md = module
local rq = require
local sa = package.seeall
local gfe = getfenv
local gmt = getmetatable
local function envs()
	return id(gfe(0)), id(gfe(1)), id(gfe(2)), 
		 id(gmt(gfe(0))), id(gmt(gfe(1))), id(gmt(gfe(2)))
end
local function trymodule(name)
	pr( '_G['..name..']', id(g[name]) )
	pr( 'pkg.loaded['..name..']', id(pkg.loaded[name]) )
	pr( 'envs', envs() )
	md(name)
	pr( 'envs', envs() )
	pr( 'status,result', status, result )
	pr( '_G['..name..']', id(g[name]) )
	local t = pkg.loaded[name]
	pr( 't=pkg.loaded['..name..']', id(t) )
	pr( 't._M, t._NAME, t._PACKAGE', id(t._M), id(t._NAME), id(t._PACKAGE) )
	pr( 'rq('..name..')', id( rq(name) ) )
	pr( 'print', id(print) )
	pr( 'seeall(t)', sa(t) )
	pr( 'print, _G['..name..']', id(print), id(g[name]) )
end
trymodule('abc.def.ghi')
trymodule('abc.def.ghi')
trymodule('abc.def')
trymodule('abc.def.lmn')
trymodule('abc.def')
trymodule('abc')
package.loaded['opq.rst'] = {}
trymodule('opq.rst')
trymodule('opq.rst')
uvw = { xyz="x1y1z1" }
--print( "uvw", id(uvw) )
--print( "uvw.xyz", id(uvw.xyz) )
--print( "uvw.abc", id(uvw.abc) )
print( pcall( trymodule, 'uvw.xyz' ) )
print( pcall( trymodule, 'uvw' ) )
print( pcall( trymodule, 'uvw.abc' ) )
print( "uvw", id(uvw) )
print( "uvw.xyz", id(uvw.xyz) )
print( "uvw.abc", id(uvw.abc) )

function f()
 module( 'fff', package.seeall )
 a = 'aaa'
 print( a )
 end
f()
f()
print( a )
print( getfenv(f)['a'] )
print( a )

-- do metatables work with package.loaded and require? 
print( 'setting metatable for package.loaded' )
print( 'package.loaded.mypreload', package.loaded.mypreload )
print( 'setmetatable') 
pcall( setmetatable, package.loaded, { __index={mypreload=12345}})
print( 'package.loaded.mypreload', package.loaded.mypreload )
print( "require, 'mypreload'", pcall( require, 'mypreload' ) )
print( 'package.loaded.mypreload', package.loaded.mypreload )
print( 'resetting metatable for package.loaded' )
print( 'setmetatable') 
pcall( setmetatable, package.loaded, nil )
print( "require, 'mypreload'", (pcall( require, 'mypreload' )) )
print( 'package.loaded.mypreload', package.loaded.mypreload )
