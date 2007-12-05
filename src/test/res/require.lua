-- unit tests for require() function
local ids = {}
local ti = table.insert
local function id(obj)
	if not obj or type(obj) == 'number' or type(obj) == 'string' then
		return obj
	end
	local v = ids[obj]
	if v then
		return v
	end
	ti(ids,obj)
	ids[obj] = type(obj)..'.'..tostring(#ids)
	return ids[obj]
end 

-- tests on require
package.path='?.lua;src/test/res/?.lua'
function f( name )
	print( module( 'testmod', package.seeall ) )
	print( 'before', id(sample), id(bogus), id(_G[name]) );
	local status,result = pcall( require, name )
	if status then 
		print( 'pcall(require,"'..name..'")', status, id(result) )
		print( 'after', id(sample), id(bogus), id(_G[name]) );
	else
		print( 'pcall(require,"'..name..'")', status )
	end
end
f('sample')
print( 'main', id(sample), id(bogus), id(custom) );
f('sample')
print( 'main', id(sample), id(bogus), id(custom) );
f('bogus')
print( 'main', id(sample), id(bogus), id(custom) );

-- custom loader chain
for i=1,3 do
	print( i,id(package.loaders[i]) )
end
function loader1( ... ) 
	print ('in loader1', ...)
	return "loader1 didn't find anything"
end
function loader2( name, ... )
	print ('in loader2', ...)
	if name ~= 'custom' then
		message =  "name is not 'custom'"
		print ('loader2 is returning', message )
		return message
	end
	table = {}
	result = function()
		print( 'in loader function, returning', id(table) )
		return table 
	end
	print ('loader2 is returning', id(result) )
	return result
end
function loader3( ... )
	print ('in loader3', ...)
	return nil
end
package.loaders = { loader1, loader2, loader3 }
f( 'bogus' )
print( 'main', id(sample), id(bogus), id(custom) );
f( 'custom' )
print( 'main', id(sample), id(bogus), id(custom) );
