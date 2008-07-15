-- utilities to check that args of various types pass or fail 
-- argument type checking

anylua = { nil, 'abc', 1.23, true, {aa=11,bb==22}, print }

somestring   = { 'abc' }
somenumber   = { 1.23 }
somestrnum   = { 'abc', 1.23 }
someboolean  = { true }
sometable    = { {aa=11,bb=22} }
somefunction = { print }
somenil      = { nil }

local function contains(set,val)
	local m = #set
	for i=1,m do
		if set[i] == val then 
			return true 
		end		
	end
	return false
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

local function expand(argsets, typesets, ...)	
	local n = typesets and #typesets or 0
	if n <= 0 then
		table.insert(argsets,{...})
		return argsets
	end

	local t = typesets[1]
	local s = {select(2,unpack(typesets))}
	local m = #t
	for i=1,m do
		expand(argsets, s, t[i], ...)
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

-- check that all combinations of arguments pass
function checkallpass( name, typesets )
	for i,v in arglists(typesets) do
		local sig = signature(name,v)
		local s,e = invoke( name, v )
		if s then 
			print( 'pass', sig )
		else
			print( 'fail', sig, e )
		end
	end
end

-- check that all combinations of arguments fail in some way, 
-- ignore error messages
function checkallfail( name, typesets )
	for i,v in arglists(typesets) do
		local sig = signature(name,v)
		local s,e,f,g = invoke( name, v )
		if not s then 
			print( 'ok', sig )
		else
			print( 'needcheck', sig, e, f, g )
		end
	end
end

-- check that all combinations of arguments fail in some way, 
-- ignore error messages
function checkallerrors( name, typesets, template )
	for i,v in arglists(typesets) do
		local sig = signature(name,v)
		local s,e,f,g = invoke( name, v )
		if not s then
			if string.match(e, template) then
				print( 'ok', sig, 'template='..template )
			else
				print( 'badmsg', sig, "template='"..template.."' actual='"..e.."'" )
			end
		else
			print( 'needcheck', sig, e, f, g )
		end
	end
end
