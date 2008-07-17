-- utilities to check that args of various types pass or fail 
-- argument type checking
local ok = '-\t'
local fail = 'fail '
local needcheck = 'needcheck '
local badmsg = 'badmsg '


akey      = 'aa'
astring   = 'abc'
astrnum   = '789'
anumber   = 1.23
ainteger  = 345
adouble   = 123.456
aboolean  = true
atable    = {[akey]=456}
afunction = function() end
anil      = nil
athread   = coroutine.create(afunction)

anylua = { nil, astring, anumber, aboolean, atable, afunction, athread }

somestring   = { astring, anumber }
somenumber   = { anumber, astrnum }
someboolean  = { aboolean }
sometable    = { atable }
somefunction = { afunction }
somenil      = { anil }
somekey      = { akey }
notakey      = { astring, anumber, aboolean, atable, afunction }

notastring   = { nil, aboolean, atable, afunction, athread }
notanumber   = { nil, astring, aboolean, atable, afunction, athread }
notaboolean  = { nil, astring, anumber, atable, afunction, athread }
notatable    = { nil, astring, anumber, aboolean, afunction, athread }
notafunction = { nil, astring, anumber, aboolean, atable, athread }
notathread   = { nil, astring, anumber, aboolean, atable, afunction }
notanil      = { astring, anumber, aboolean, atable, afunction, athread }

nonstring   = { aboolean, atable, afunction, athread }
nonnumber   = { astring, aboolean, atable, afunction, athread }
nonboolean  = { astring, anumber, atable, afunction, athread }
nontable    = { astring, anumber, aboolean, afunction, athread }
nonfunction = { astring, anumber, aboolean, atable, athread }
nonthread   = { astring, anumber, aboolean, atable, afunction }
nonkey      = { astring, anumber, aboolean, atable, afunction }

local structtypes = { 
	['table']='<table>',
	['function']='<function>',
	['thread']='<thread>',
	['userdata']='<userdata>',
}

local function signature(name,arglist)
	local t = {}
	for i=1,#arglist do
		local ai = arglist[i]
		local ti = type(ai)
		t[i] = structtypes[ti] or tostring(ai)
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
function checkallerrors( name, typesets, template )
	subbanner('checkallerrors')
	template = tostring(template)
	for i,v in arglists(typesets) do
		local sig = signature(name,v)
		local s,e = invoke( name, v )
		if not s then
			if string.find(e, template, 1, true) then
				print( ok, sig, '...'..template..'...' )
			else
				print( badmsg, sig, "template='"..template.."' actual='"..e.."'" )
			end
		else
			print( needcheck, sig, e )
		end
	end
end
