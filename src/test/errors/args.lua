-- utilities to check that args of various types pass or fail 
-- argument type checking

akey      = 'aa'
astring   = 'abc'
astrnum   = '789'
anumber   = 1.23
aboolean  = true
atable    = {[akey]=456}
afunction = function() end
anil      = nil

anylua = { anil, astring, anumber, aboolean, atable, afunction }

somestring   = { astring }
somenumber   = { anumber }
somestrnum   = { anumber, astrnum }
someboolean  = { aboolean }
sometable    = { atable }
somefunction = { afunction }
somenil      = { anil }
somekey      = { akey }
notakey      = { astring, anumber, aboolean, atable, afunction }

local function contains(set,val)
	local m = #set
	for i=1,m do
		if set[i] == val then 
			return true 
		end		
	end
	return val == nil
end
	
local function except(some)
	local n = #anylua
	local z = {}
	local j = 1
	for i=1,n do
		if not contains(some, anylua[i]) then
			z[j] = anylua[i]
			j = j + 1
		end
	end
	return z
end

notastring   = except(somestring)
notanumber   = except(somenumber)
notastrnum   = except(somestrnum)
notaboolean  = except(someboolean)
notatable    = except(sometable)
notafunction = except(somefunction)
notanil      = except(somenil)

local function signature(name,arglist)
	local t = {}
	for i=1,#arglist do
		if type(arglist[i]) == 'table' then 
			t[i] = 'table'
		elseif type(arglist[i]) == 'function' then
			t[i] = 'function'
		else
			t[i] = tostring(arglist[i])
		end 
	end
	return name..'('..table.concat(t,',')..')'
end

local function dup(t)
	local s = {}
	for i=1,#t do
		s[i] = t[i]
	end
	return s
end

local function split(t)
	local s = {}
	local n = #t
	for i=1,n-1 do
		s[i] = t[i]
	end
	return s,t[n]
end

local function expand(argsets, typesets, ...)	
	local n = typesets and #typesets or 0
	if n <= 0 then
		table.insert(argsets,{...})
		return argsets
	end

	local s,v = split(typesets)
	for i=1,#v do
		expand(argsets, s, v[i], ...)
	end
	return argsets
end

local function arglists(typesets)
	local argsets = expand({},typesets)
	return ipairs(argsets)	
end

--[[
local function expand(arglists,fixed,varying,...)
	for i=1,#varying do
		local f = dup(fixed)
		
	end
end

local function arglists(typesets)
	local argsets = {}
	local args={}
	local n = typesets and #typesets or 0
	if n == 0 then
		table.insert( argsets, args )
	end
	for i=1,n do
		local t = typesets[i]
		for j=1,#t do
			args[i] = t[j]
			if i == n then 
				table.insert( argsets, duptable(args) )
			end
		end
	end
	return ipairs(argsets)	
end
--]]

local function lookup( name ) 
	return loadstring('return '..name)()
end

local function invoke( name, arglist )
	local s,c = pcall(lookup, name)
	if not s then return s,c end
	return pcall(c, unpack(arglist)) 
end

-- messages, banners
function banner(name)
	print( '====== '..tostring(name)..' ======' )
end

local function subbanner(name)
	print( '--- '..tostring(name) )
end

local ok = 'ok '
local fail = 'fail '
local needcheck = 'needcheck '
local badmsg = 'badmsg '

-- check that all combinations of arguments pass
function checkallpass( name, typesets )
	subbanner('checkallpass')
	for i,v in arglists(typesets) do
		local sig = signature(name,v)
		local s,e = invoke( name, v )
		if s then 
			print( ok, sig )
		else
			print( fail, sig, e )
		end
	end
end

-- check that all combinations of arguments fail in some way, 
-- ignore error messages
function checkallfail( name, typesets )
	subbanner('checkallfail')
	for i,v in arglists(typesets) do
		local sig = signature(name,v)
		local s,e = invoke( name, v )
		if not s then 
			print( ok, sig )
		else
			print( needcheck, sig, e )
		end
	end
end

-- check that all combinations of arguments fail in some way, 
-- ignore error messages
function checkallerrors( name, typesets, template )
	subbanner('checkallerrors')
	template = tostring(template)
	for i,v in arglists(typesets) do
		local sig = signature(name,v)
		local s,e = invoke( name, v )
		if not s then
			if string.match(e, template) then
				print( ok, sig, '...'..template..'...' )
			else
				print( badmsg, sig, "template='"..template.."' actual='"..e.."'" )
			end
		else
			print( needcheck, sig, e )
		end
	end
end
